package com;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ParallelBinarySearchMatcher {

    private final int CODE_SIZE = 33; // Длина вашего DataMatrix кода в байтах
    private final int CHUNK_SIZE = 4 * 1024 * 1024; // Блок чтения большого файла (8 МБ)
    private final int WRITE_LIMIT = 200; // ИЗМЕНЕНИЕ: Записать в файл только первые 200 совпадений

    private final int THREADS = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executor = Executors.newFixedThreadPool(THREADS);
    private final AtomicLong totalMatches = new AtomicLong(0);
    // ИЗМЕНЕНИЕ: Отдельный атомарный счетчик для контроля количества уже записанных в файл байт/строк
    private final AtomicLong writtenMatches = new AtomicLong(0);

    String smallFile;
    String largeFile;
    String outputFile;

    public ParallelBinarySearchMatcher() {
        this(null, null, null);
    }

    public ParallelBinarySearchMatcher(String smallFile, String largeFile, String outputFile){
        this.smallFile = smallFile;
        this.largeFile = largeFile;
        this.outputFile = outputFile;
    }

    public AtomicLong getTotalMatches() {
        return totalMatches;
    }

    public void doSearch(){
        try {
            long startTime = System.currentTimeMillis();

            System.out.println("Шаг 1: Загрузка и сортировка малого файла...");
            byte[] smallData = loadAndSortSmallFile(smallFile);
            int totalSmallCodes = smallData.length / CODE_SIZE;
            System.out.println("Загружено кодов: " + totalSmallCodes + " | Задействовано ядер CPU: " + THREADS);

            System.out.println("Шаг 2: Полное параллельное сканирование (запись первых " + WRITE_LIMIT + " строк)...");
            matchLargeFileParallel(largeFile, outputFile, smallData, totalSmallCodes);

            long endTime = System.currentTimeMillis();
            System.out.println("----------------------------------------");
            System.out.println("[УСПЕХ] Полная многопоточная обработка завершена.");
            System.out.println("Всего найдено совпадений во всем файле: " + totalMatches.get());
            System.out.println("Записано в файл результатов (" + outputFile + "): " + writtenMatches.get() + " строк.");
            System.out.println("Время работы: " + (endTime - startTime) / 1000.0 + " сек.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }


    private void matchLargeFileParallel(String largeFile, String outputFile, byte[] smallData, int totalSmallCodes) throws IOException, InterruptedException, ExecutionException {
        try (FileInputStream fis = new FileInputStream(largeFile);
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {

            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            int carryOverLen = 0;
            byte[] carryOverBuffer = new byte[CODE_SIZE + 2];

            List<Future<byte[]>> futures = new ArrayList<>();

            // Программа идет до конца файла, не останавливая цикл
            while ((bytesRead = fis.read(buffer, carryOverLen, CHUNK_SIZE - carryOverLen)) != -1) {
                int totalBytesInChunk = bytesRead + carryOverLen;

                int lastNewLineIdx = -1;
                for (int i = totalBytesInChunk - 1; i >= 0; i--) {
                    if (buffer[i] == '\n') {
                        lastNewLineIdx = i;
                        break;
                    }
                }

                if (lastNewLineIdx == -1) {
                    continue;
                }

                byte[] chunkToProcess = new byte[lastNewLineIdx + 1];
                System.arraycopy(buffer, 0, chunkToProcess, 0, chunkToProcess.length);

                carryOverLen = totalBytesInChunk - (lastNewLineIdx + 1);
                if (carryOverLen > 0) {
                    System.arraycopy(buffer, lastNewLineIdx + 1, carryOverBuffer, 0, carryOverLen);
                }

                futures.add(executor.submit(new ChunkProcessor(chunkToProcess, smallData, totalSmallCodes)));

                if (carryOverLen > 0) {
                    System.arraycopy(carryOverBuffer, 0, buffer, 0, carryOverLen);
                }

                if (futures.size() >= THREADS * 2) {
                    writeResults(futures, bos);
                }
            }

            // Обрабатываем последние порции данных
            writeResults(futures, bos);
            bos.flush();
        }
    }

    private void writeResults(List<Future<byte[]>> futures, BufferedOutputStream bos) throws InterruptedException, ExecutionException, IOException {
        for (Future<byte[]> future : futures) {
            byte[] result = future.get(); // Ждем завершения потока в любом случае, чтобы учесть все совпадения

            if (result != null && result.length > 0) {
                long currentWritten = writtenMatches.get();

                // ИЗМЕНЕНИЕ: Проверяем, достигнут ли лимит записи в файл
                if (currentWritten < WRITE_LIMIT) {
                    int codesInResult = result.length / (CODE_SIZE + 1);

                    if (currentWritten + codesInResult <= WRITE_LIMIT) {
                        // Весь пакет помещается в лимит записи
                        bos.write(result);
                        writtenMatches.addAndGet(codesInResult);
                    } else {
                        // Пакет превышает лимит: берем только недостающую часть
                        int allowedCodes = (int) (WRITE_LIMIT - currentWritten);
                        if (allowedCodes > 0) {
                            int bytesToWrite = allowedCodes * (CODE_SIZE + 1);
                            bos.write(result, 0, bytesToWrite);
                            writtenMatches.addAndGet(allowedCodes);
                        }
                    }
                }
                // Файл больше не пишется, но данные потока отработали и общий счетчик totalMatches внутри них уже обновился
            }
        }
        futures.clear();
    }

    private class ChunkProcessor implements Callable<byte[]> {
        private final byte[] chunk;
        private final byte[] smallData;
        private final int totalSmallCodes;

        public ChunkProcessor(byte[] chunk, byte[] smallData, int totalSmallCodes) {
            this.chunk = chunk;
            this.smallData = smallData;
            this.totalSmallCodes = totalSmallCodes;
        }

        @Override
        public byte[] call() {
            byte[] localResult = new byte[chunk.length / 10 + 64];
            int resultPos = 0;

            byte[] currentCode = new byte[CODE_SIZE];
            int codePos = 0;
            int localMatchesCount = 0;

            for (int i = 0; i < chunk.length; i++) {
                byte b = chunk[i];
                if (b == '\n' || b == '\r') {
                    if (codePos == CODE_SIZE) {
                        if (binarySearch(smallData, currentCode, totalSmallCodes)) {

                            // ИЗМЕНЕНИЕ: Формируем локальный массив результатов только если лимит файла еще не набран.
                            // Это экономит память на поздних этапах сканирования файла.
                            if (writtenMatches.get() < WRITE_LIMIT) {
                                if (resultPos + CODE_SIZE + 1 >= localResult.length) {
                                    localResult = Arrays.copyOf(localResult, localResult.length * 2);
                                }
                                System.arraycopy(currentCode, 0, localResult, resultPos, CODE_SIZE);
                                resultPos += CODE_SIZE;
                                localResult[resultPos++] = '\n';
                            }

                            localMatchesCount++; // Счетчик общего количества продолжает расти в любом случае
                        }
                    }
                    codePos = 0;
                } else {
                    if (codePos < CODE_SIZE) {
                        currentCode[codePos++] = b;
                    }
                }
            }

            if (localMatchesCount > 0) {
                // ИЗМЕНЕНИЕ: Вне зависимости от записи, мы ВСЕГДА плюсуем все находки в общий глобальный счетчик
                totalMatches.addAndGet(localMatchesCount);
                return Arrays.copyOf(localResult, resultPos);
            }
            return null;
        }
    }

    // Методы binarySearch, compareBytes и loadAndSortSmallFile остаются без изменений...
    private boolean binarySearch(byte[] array, byte[] key, int totalElements) {
        int low = 0; int high = totalElements - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = compareBytes(array, mid * CODE_SIZE, key);
            if (cmp < 0) low = mid + 1;
            else if (cmp > 0) high = mid - 1;
            else return true;
        }
        return false;
    }

    private int compareBytes(byte[] array, int arrayOffset, byte[] key) {
        for (int i = 0; i < CODE_SIZE; i++) {
            int a = array[arrayOffset + i] & 0xFF;
            int b = key[i] & 0xFF;
            if (a != b) return a - b;
        }
        return 0;
    }

/*    private static byte[] loadAndSortSmallFile(String filePath) throws IOException {
        // Шаг 1: Сначала быстро считаем количество строк в файле, чтобы выделить точный объем памяти
        int count = 0;
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath))) {
            int b;
            while ((b = bis.read()) != -1) {
                if (b == '\n') {
                    count++;
                }
            }
        }

        // Выделяем память под плоский массив
        byte[] data = new byte[count * CODE_SIZE];

        // Шаг 2: Читаем данные повторно и заполняем массив только полезными байтами ASCII
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath))) {
            int b;
            int codePos = 0;
            int currentCodeIndex = 0;
            byte[] currentCode = new byte[CODE_SIZE];

            while ((b = bis.read()) != -1) {
                // Пропускаем символы переноса строк и пробелы по краям (аналог trim())
                if (b == '\n' || b == '\r') {
                    if (codePos == CODE_SIZE) {
                        // Переносим собранный код в общий массив
                        System.arraycopy(currentCode, 0, data, currentCodeIndex * CODE_SIZE, CODE_SIZE);
                        currentCodeIndex++;
                    }
                    codePos = 0; // Сбрасываем позицию для следующей строки
                } else if (b == ' ' && codePos == 0) {
                    // Пропускаем начальные пробелы (trim слева)
                    continue;
                } else {
                    // Записываем полезный байт, если не превысили CODE_SIZE
                    if (codePos < CODE_SIZE) {
                        currentCode[codePos++] = (byte) b;
                    }
                }
            }

            // На случай, если файл не заканчивался переносом строки, но последний код был валидным
            if (codePos == CODE_SIZE && currentCodeIndex < count) {
                System.arraycopy(currentCode, 0, data, currentCodeIndex * CODE_SIZE, CODE_SIZE);
            }
        }

        // Шаг 3: Сортировка (сохраняем неизменной, так как она работает на нативных байтах)
        byte[][] temp = new byte[count][CODE_SIZE];
        for (int i = 0; i < count; i++) {
            System.arraycopy(data, i * CODE_SIZE, temp[i], 0, CODE_SIZE);
        }

        Arrays.sort(temp, (o1, o2) -> {
            for (int i = 0; i < CODE_SIZE; i++) {
                int a = o1[i] & 0xFF;
                int b = o2[i] & 0xFF;
                if (a != b) return a - b;
            }
            return 0;
        });

        for (int i = 0; i < count; i++) {
            System.arraycopy(temp[i], 0, data, i * CODE_SIZE, CODE_SIZE);
        }

        return data;
    }*/

    private byte[] loadAndSortSmallFile(String filePath) throws IOException {
        // Создаем динамически расширяемый байтовый массив в RAM.
        // Задаем начальную емкость (например, 1 МБ), чтобы реже расширять массив под капотом
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 1024);

        int count = 0;

        // Читаем файл ВСЕГО ОДИН РАЗ
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath))) {
            int b;
            int codePos = 0;
            byte[] currentCode = new byte[CODE_SIZE];

            while ((b = bis.read()) != -1) {
                // Имитация trim() и отсечение переносов строк
                if (b == '\n' || b == '\r') {
                    if (codePos == CODE_SIZE) {
                        // Пишем 31 байт накопленного кода в наш динамический буфер
                        baos.write(currentCode, 0, CODE_SIZE);
                        count++;
                    }
                    codePos = 0;
                } else if (b == ' ' && codePos == 0) {
                    // Пропускаем пробелы перед кодом
                    continue;
                } else {
                    if (codePos < CODE_SIZE) {
                        currentCode[codePos++] = (byte) b;
                    }
                }
            }

            // Страховка на случай, если файл не заканчивался переносом строки
            if (codePos == CODE_SIZE) {
                baos.write(currentCode, 0, CODE_SIZE);
                count++;
            }
        }

        // Извлекаем из «резинового» буфера один готовый плоский массив байт
        byte[] data = baos.toByteArray();

        // Сортировка данных на месте (in-place) через двумерную таблицу
        byte[][] temp = new byte[count][CODE_SIZE];
        for (int i = 0; i < count; i++) {
            System.arraycopy(data, i * CODE_SIZE, temp[i], 0, CODE_SIZE);
        }

        Arrays.sort(temp, (o1, o2) -> {
            for (int i = 0; i < CODE_SIZE; i++) {
                int a = o1[i] & 0xFF;
                int b = o2[i] & 0xFF;
                if (a != b) return a - b;
            }
            return 0;
        });

        for (int i = 0; i < count; i++) {
            System.arraycopy(temp[i], 0, data, i * CODE_SIZE, CODE_SIZE);
        }

        return data;
    }

}
