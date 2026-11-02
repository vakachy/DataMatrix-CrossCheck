package com;

import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.impl.factory.primitive.LongSets;
import org.eclipse.collections.impl.set.mutable.primitive.LongHashSet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class UltraFastDuplicateFinder {

    // Читаем огромными блоками по 4 МБ
    private static final int BUFFER_SIZE = 4 * 1024 * 1024;

    public static void main(String[] args) {
        System.out.println("Макс. память JVM: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " МБ");
        String inputFilePath = "datamatrix_codes.txt";
        String outputFilePath = "duplicates_found.txt";

        int expectedSize = 150_000_000;
        int maxDuplicatesToWrite = 100;

        System.out.println("Инициализация LongHashSet...");
        LongHashSet uniqueHashes = new LongHashSet(expectedSize);

        long lineCount = 0;
        long totalDuplicateCount = 0;
        int writtenDuplicateCount = 0;

        System.out.println("Запуск ультра-быстрого сканирования блока за блоком...");
        long startTime = System.currentTimeMillis();

        try (FileChannel fileChannel = FileChannel.open(Paths.get(inputFilePath), StandardOpenOption.READ);
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            // Выделяем direct-буфер вне кучи JVM для прямой работы с диском
            ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

            byte[] lineBuffer = new byte[128]; // Буфер под один код DataMatrix
            int bufferPos = 0;

            while (fileChannel.read(buffer) > 0) {
                buffer.flip(); // Переключаем буфер в режим чтения

                // Получаем доступ к подлежащим байтам через массив, если это возможно,
                // либо читаем пачкой. Для direct-буфера быстрее всего работать через цикл по локальной копии:
                int limit = buffer.limit();
                for (int i = 0; i < limit; i++) {
                    byte b = buffer.get();

                    if (b == '\n' || b == '\r') {
                        if (bufferPos > 0) {
                            lineCount++;

                            // Считаем быстрый и надежный 64-битный хэш (FNV-1a или быстрый Murmur)
                            long hash = fast64Hash(lineBuffer, bufferPos);

                            if (!uniqueHashes.add(hash)) {
                                totalDuplicateCount++;

                                if (writtenDuplicateCount < maxDuplicatesToWrite) {
                                    String code = new String(lineBuffer, 0, bufferPos, java.nio.charset.StandardCharsets.UTF_8).trim();
                                    writer.write(code);
                                    writer.newLine();
                                    writtenDuplicateCount++;
                                }
                            }
                            bufferPos = 0;

                            if (lineCount % 20_000_000 == 0) {
                                System.out.printf("Обработано строк: %,d...%n", lineCount);
                            }
                        }
                    } else {
                        if (bufferPos < lineBuffer.length) {
                            lineBuffer[bufferPos++] = b;
                        }
                    }
                }
                buffer.clear(); // Очищаем буфер для следующего чтения с диска
            }

            // Обработка хвостика файла
            if (bufferPos > 0) {
                lineCount++;
                long hash = fast64Hash(lineBuffer, bufferPos);
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
        System.out.println("\n--- Результаты оптимизированного анализа ---");
        System.out.printf("Всего строк обработано: %,d%n", lineCount);
        System.out.printf("Всего обнаружено дубликатов: %,d%n", totalDuplicateCount);
        System.out.printf("Время выполнения: %,d мс (~%.2f сек)%n",
                (endTime - startTime), (endTime - startTime) / 1000.0);
    }

    /**
     * Экстремально быстрый 64-битный хэш алгоритма FNV-1a.
     * Он работает значительно быстрее побайтового MurmurHash и не имеет коллизий на таком объеме.
     */
    private static long fast64Hash(byte[] bytes, int length) {
        long hash = 0xcbf29ce484222325L; // FNV_offset_basis
        for (int i = 0; i < length; i++) {
            hash ^= (bytes[i] & 0xff);
            hash *= 0x100000001b3L; // FNV_prime
        }
        return hash;
    }
}
