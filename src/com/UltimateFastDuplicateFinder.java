package com;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class UltimateFastDuplicateFinder {

    private static final int BUFFER_SIZE = 16 * 1024 * 1024; // 16 МБ блок чтения

    public static void main(String[] args) {
        String inputFilePath = "/run/media/valery/01DCF036DAB27160/Codes/04810268033988-1500k_N188218_2171_1";
        String outputFilePath = "/run/media/valery/01DCF036DAB27160/Codes/report.txt";

        long expectedInsertions = 150_000_000L;
        int maxDuplicatesToWrite = 100;

        System.out.println("Макс. память JVM: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " МБ");
        System.out.println("Инициализация Битового Фильтра Блума (потребление всего ~350 МБ RAM)...");

        // Оптимальный размер битового массива для 150 млн элементов с минимальной погрешностью
        long numBits = expectedInsertions * 15;
        long[] bitArray = new long[(int) ((numBits + 63) / 64)];

        long lineCount = 0;
        long totalDuplicateCount = 0;
        int writtenDuplicateCount = 0;

        System.out.println("Запуск сканирования файла...");
        long startTime = System.currentTimeMillis();

        try (FileChannel fileChannel = FileChannel.open(Paths.get(inputFilePath), StandardOpenOption.READ);
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
            byte[] lineBuffer = new byte[256];
            int bufferPos = 0;

            while (fileChannel.read(buffer) > 0) {
                buffer.flip();
                int limit = buffer.limit();
                for (int i = 0; i < limit; i++) {
                    byte b = buffer.get();

                    if (b == '\n' || b == '\r') {
                        if (bufferPos > 0) {
                            lineCount++;

                            // Генерируем два разных быстрых хэша для Фильтра Блума
                            long hash1 = fastHash64(lineBuffer, bufferPos, 0xcbf29ce484222325L);
                            long hash2 = fastHash64(lineBuffer, bufferPos, 0x811c9dc5L);

                            // Проверяем наличие элемента в фильтре по 3 независимым индексам
                            boolean mightContain = true;
                            for (int k = 0; k < 3; k++) {
                                long combinedHash = hash1 + k * hash2;
                                long bitIndex = (combinedHash & Long.MAX_VALUE) % numBits;

                                int arrayIdx = (int) (bitIndex / 64);
                                int bitShift = (int) (bitIndex % 64);

                                if ((bitArray[arrayIdx] & (1L << bitShift)) == 0) {
                                    mightContain = false;
                                    // Если хоть одного бита нет — это точно новый элемент, взводим бит
                                    bitArray[arrayIdx] |= (1L << bitShift);
                                }
                            }

                            if (mightContain) {
                                totalDuplicateCount++;

                                if (writtenDuplicateCount < maxDuplicatesToWrite) {
                                    String code = new String(lineBuffer, 0, bufferPos, java.nio.charset.StandardCharsets.UTF_8).trim();
                                    writer.write(code);
                                    writer.newLine();
                                    writtenDuplicateCount++;
                                }
                            }

                            if (lineCount % 30_000_000 == 0) {
                                System.out.printf("Обработано строк: %,d...%n", lineCount);
                            }
                            bufferPos = 0;
                        }
                    } else {
                        if (bufferPos < lineBuffer.length) {
                            lineBuffer[bufferPos++] = b;
                        }
                    }
                }
                buffer.clear();
            }

            // Хвостик файла
            if (bufferPos > 0) {
                lineCount++;
                long hash1 = fastHash64(lineBuffer, bufferPos, 0xcbf29ce484222325L);
                long hash2 = fastHash64(lineBuffer, bufferPos, 0x811c9dc5L);
                boolean mightContain = true;
                for (int k = 0; k < 3; k++) {
                    long combinedHash = hash1 + k * hash2;
                    long bitIndex = (combinedHash & Long.MAX_VALUE) % numBits;
                    int arrayIdx = (int) (bitIndex / 64);
                    int bitShift = (int) (bitIndex % 64);
                    if ((bitArray[arrayIdx] & (1L << bitShift)) == 0) {
                        mightContain = false;
                        bitArray[arrayIdx] |= (1L << bitShift);
                    }
                }
                if (mightContain) {
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
        System.out.println("\n--- Результаты Фильтра Блума + NIO ---");
        System.out.printf("Всего строк обработано: %,d%n", lineCount);
        System.out.printf("Всего обнаружено дубликатов: %,d%n", totalDuplicateCount);
        System.out.printf("Время выполнения: %,d мс (~%.2f сек)%n",
                (endTime - startTime), (endTime - startTime) / 1000.0);
    }

    /**
     * Модифицированный быстрый хэш FNV-1a с поддержкой кастомного seed
     */
    private static long fastHash64(byte[] bytes, int length, long seed) {
        long hash = seed;
        for (int i = 0; i < length; i++) {
            hash ^= (bytes[i] & 0xff);
            hash *= 0x100000001b3L;
        }
        return hash;
    }
}

