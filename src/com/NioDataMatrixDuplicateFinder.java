package com;

import org.eclipse.collections.impl.set.mutable.primitive.LongHashSet;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class NioDataMatrixDuplicateFinder {

    // Размер окна отображения в памяти — 1 ГБ (должен быть меньше Integer.MAX_VALUE)
    private static final long MAP_SIZE = 64 * 1024 * 1024;

    public static void main(String[] args) {
        String inputFilePath = "/run/media/valery/01DCF036DAB27160/Codes/04810268033988-1500k_N188218_2171_1";
        String outputFilePath = "/run/media/valery/01DCF036DAB27160/Codes/report.txt";

        int expectedSize = 150_000_000;
        int maxDuplicatesToWrite = 100;

        System.out.println("Инициализация LongHashSet...");
        LongHashSet uniqueHashes = LongHashSet.newSetWith(expectedSize);

        long lineCount = 0;
        long totalDuplicateCount = 0;
        int writtenDuplicateCount = 0;

        System.out.println("Отображение файла в память и запуск сканирования...");
        long startTime = System.currentTimeMillis();

        // Открываем FileChannel для чтения и обычный BufferedWriter для записи (так как пишем всего 100 строк)
        try (FileChannel fileChannel = FileChannel.open(Paths.get(inputFilePath), StandardOpenOption.READ);
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            long fileSize = fileChannel.size();
            long position = 0;

            // Буфер для сборки текущей строки без создания лишних объектов String
            byte[] lineBuffer = new byte[128];
            int bufferPos = 0;

            while (position < fileSize) {
                // Вычисляем размер текущего сегмента (последний кусок будет меньше MAP_SIZE)
                long remaining = fileSize - position;
                long bytesToMap = Math.min(MAP_SIZE, remaining);

                MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, position, bytesToMap);

                while (buffer.hasRemaining()) {
                    byte b = buffer.get();

                    if (b == '\n' || b == '\r') {
                        if (bufferPos > 0) {
                            lineCount++;

                            // Считаем хэш напрямую из массива байт (не создавая String!)
                            long hash = murmurHash3_x64_64(lineBuffer, bufferPos);

                            if (!uniqueHashes.add(hash)) {
                                totalDuplicateCount++;

                                if (writtenDuplicateCount < maxDuplicatesToWrite) {
                                    // Создаем строку ТОЛЬКО для тех редких случаев, когда нужно записать дубликат
                                    String code = new String(lineBuffer, 0, bufferPos, java.nio.charset.StandardCharsets.UTF_8).trim();
                                    writer.write(code);
                                    writer.newLine();
                                    writtenDuplicateCount++;

                                    if (writtenDuplicateCount == maxDuplicatesToWrite) {
                                        System.out.println("Лимит записи (100 дубликатов) исчерпан.");
                                    }
                                }
                            }
                            bufferPos = 0; // Сбрасываем указатель буфера строки

                            if (lineCount % 10_000_000 == 0) {
                                System.out.printf("Обработано строк: %,d...%n", lineCount);
                            }
                        }
                    } else {
                        // Защита от переполнения буфера (если строка аномально длинная)
                        if (bufferPos < lineBuffer.length) {
                            lineBuffer[bufferPos++] = b;
                        }
                    }
                }

                // Сдвигаем позицию в файле на фактически обработанные байты в этом окне
                // Важно: если строка разорвалась на границе окна, этот код корректно продолжит чтение в следующем окне
                position += bytesToMap;
            }

            // Обработка последней строки, если файл не заканчивался на перенос строки
            if (bufferPos > 0) {
                lineCount++;
                long hash = murmurHash3_x64_64(lineBuffer, bufferPos);
                if (!uniqueHashes.add(hash)) {
                    totalDuplicateCount++;
                    if (writtenDuplicateCount < maxDuplicatesToWrite) {
                        String code = new String(lineBuffer, 0, bufferPos, java.nio.charset.StandardCharsets.UTF_8).trim();
                        writer.write(code);
                        writer.newLine();
                    }
                }
            }
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("\n--- Результаты NIO анализа ---");
        System.out.printf("Всего строк обработано: %,d%n", lineCount);
        System.out.printf("Всего обнаружено дубликатов: %,d%n", totalDuplicateCount);
        System.out.printf("Время выполнения: %,d мс (~%.2f мин)%n",
                (endTime - startTime), (endTime - startTime) / 60000.0);
    }

    /**
     * Оптимизированный MurmurHash3, работающий напрямую с массивом байт
     */
    private static long murmurHash3_x64_64(byte[] bytes, int length) {
        long hash = 0xc70f6907L;
        for (int i = 0; i < length; i++) {
            hash ^= bytes[i];
            hash *= 0x5bd1e995;
            hash ^= hash >>> 15;
        }
        return hash;
    }
}

