package com;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CodesVerificator {
    private static final Path file = Paths.get("/run/media/valery/01DCF036DAB27160/Codes/04810268033988-1500k_N188218_2171");
    public static final Pattern DM_PATTERN = Pattern.compile("^01[0-9]{14}21[A-Za-z0-9%#&'()*+,-./:;<=>?!_]{8}\\x1d93[A-Za-z0-9%#&'()*+,-./:;<=>?!_]{4}.*$");

    static void main() {
        AtomicLong totalInputLines = new AtomicLong();
        AtomicLong invalidLinesCount = new AtomicLong();
        AtomicLong lineNumber = new AtomicLong();
/*
        // First variant of validation using BufferedReader
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file.toFile()), StandardCharsets.UTF_8), 8 * 1024 * 1024)) {
            String line;
            while ((line = br.readLine()) != null) {
                totalInputLines.getAndIncrement();
                lineNumber++;
                line = line.trim();

                if (line.isEmpty()) {
                    totalInputLines.getAndDecrement();
                    continue;
                }
                if (!DM_PATTERN.matcher(line).matches()){
                    invalidLinesCount.getAndIncrement();
                    System.out.println(line + "   " + lineNumber);
                }

            }
        } catch (Exception e) {
            System.out.println("ERROR");
        }

        System.out.println(totalInputLines + "/" + invalidLinesCount);*/



        // Second variant of validation using Stream API
        try (Stream<String> lines = Files.lines(file.toAbsolutePath())){
            lines.forEach(line->{
                totalInputLines.getAndIncrement();
                lineNumber.getAndIncrement();
                if (line.trim().isEmpty()) {
                    totalInputLines.getAndDecrement();
                }else if(!DM_PATTERN.matcher(line).matches()){
                    invalidLinesCount.getAndIncrement();
                    System.out.println(line + "   " + lineNumber.get());
                }});
        } catch (Exception e) {
            System.out.println("ERROR");
        }

        System.out.println(totalInputLines.get() + "/" + invalidLinesCount.get());
    }
}
