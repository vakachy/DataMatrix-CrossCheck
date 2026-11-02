package com;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileContentCopier {
    private static final String pathToDBFile = "/run/media/valery/01DCF036DAB27160/Codes/04810268033988-1500k_N188218_2171_1";
    static int totalIterations = 100;

    static void main() {
        FileContentCopier fc = new FileContentCopier();
        fc.copy(pathToDBFile);
    }

    public void copy(String pathToDBFile) {
        int writeBufferSize = 8 * 1024 * 1024;

        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(pathToDBFile));

            try (BufferedOutputStream bos =
                         new BufferedOutputStream(new FileOutputStream(pathToDBFile, true), writeBufferSize)) {
                for (int i = 0; i < totalIterations; i++) {
                    bos.write(fileContent);
                }
                bos.flush();
            }
        } catch (IOException e) {
            e.getStackTrace();
        }
    }
}
