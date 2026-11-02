package com;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

public class DataMatrixBigCompare {
    private static String pathToDBFile = "/run/media/valery/01DCF036DAB27160/Codes/04810268033988-1500k_N188218_2171_1";
    private static String pathToCodesFile = "/run/media/valery/01DCF036DAB27160/Codes/04810268033988-1500k_N188218_2171";
    private static String pathToReportDirectory = "/run/media/valery/01DCF036DAB27160/Codes";

    public static void main() {
        DataMatrixBigCompare dataMatrixBigCompare = new DataMatrixBigCompare();
        dataMatrixBigCompare.bigCompare();
    }

    public DataMatrixBigCompare(){
        this(pathToDBFile, pathToCodesFile, pathToReportDirectory);
    }

    public DataMatrixBigCompare(String pathToDBFile, String pathToCodesFile, String pathToReportDirectory) {
        DataMatrixBigCompare.pathToDBFile = pathToDBFile;
        DataMatrixBigCompare.pathToCodesFile = pathToCodesFile;
        DataMatrixBigCompare.pathToReportDirectory = pathToReportDirectory;
    }

    public void bigCompare() {
        Set<String> validSet = new HashSet<>(2_000_000);

        try (BufferedReader reader = new BufferedReader(new FileReader(pathToCodesFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                validSet.add(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Error reading Codes");
        }

        LongAdder processedBillionCount = new LongAdder();
        LongAdder matchCount = new LongAdder();

        long startTime = System.currentTimeMillis();

        try (Stream<String> lines = Files.lines(Paths.get(pathToDBFile), StandardCharsets.US_ASCII);
             PrintWriter writer =
                     new PrintWriter(new BufferedWriter(new FileWriter(pathToReportDirectory + "/report.txt")))) {
            lines
                    .map(String::trim)
                    .filter(line->!line.isEmpty())
                    .forEach(line -> {
                        processedBillionCount.increment();
                        if (validSet.contains(line)) {
                            matchCount.increment();
//
//                            if (matchCount.sum() < 100) {
//                                synchronized (writer) {
//                                    writer.println(line);
//                                }
//                            } else if (matchCount.sum() == 100){
//                                writer.println("Дублирующихся кодов слишком много!\n" +
//                                        "Дальнейшая запись в отчет не ведется.");
//                            }
                        }
//
//                        long processed = processedBillionCount.sum();
//                        if (processed % 10_000_000 == 0) {
//                            System.out.printf("Processed lines: %,d...\n", processed);
//                        }
                    });
        } catch (Exception e) {
            System.err.println("Error scanning DB file");
        }

        long endTime = System.currentTimeMillis();
        System.out.println("\n--- Comparison done! ---");
        System.out.printf("Total codes checked from DB: %,d\n", processedBillionCount.sum());
        System.out.printf("Found duplicates: %,d\n", matchCount.sum());
        System.out.printf("Time spent: %,d sec.\n", (endTime - startTime) / 1000);
    }
}
