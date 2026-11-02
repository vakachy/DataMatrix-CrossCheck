package com.model;

import com.ParallelBinarySearchMatcher;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DMCrossCheckFXModel {

    // Константы пишутся в UPPER_CASE и обычно делаются static
    public static final String REPORT_FILE_NAME = "\\report.txt";

    // Свойства JavaFX (Properties)
    private final StringProperty pathToDBFile = new SimpleStringProperty(this, "pathToDBFile");
    private final StringProperty pathToCodesFile = new SimpleStringProperty(this, "pathToCodesFile");
    private final StringProperty pathToReportDirectory = new SimpleStringProperty(this, "pathToReportDirectory");
    private final LongProperty counter = new SimpleLongProperty(this, "counter", 0L);

    // Бизнес-логика (инкапсулируем поле)
    private ParallelBinarySearchMatcher parallelBinarySearchMatcher;

    // --- СТАНДАРТНЫЕ МЕТОДЫ ДЛЯ СВЯЗЫВАНИЯ (JavaFX Bean Property Convention) ---

    // pathToDBFile
    public final StringProperty pathToDBFileProperty() { return pathToDBFile; }
    public final String getPathToDBFile() { return pathToDBFile.get(); }
    public final void setPathToDBFile(String value) { pathToDBFile.set(value); }

    // pathToCodesFile
    public final StringProperty pathToCodesFileProperty() { return pathToCodesFile; }
    public final String getPathToCodesFile() { return pathToCodesFile.get(); }
    public final void setPathToCodesFile(String value) { pathToCodesFile.set(value); }

    // pathToReportDirectory
    public final StringProperty pathToReportDirectoryProperty() { return pathToReportDirectory; }
    public final String getPathToReportDirectory() { return pathToReportDirectory.get(); }
    public final void setPathToReportDirectory(String value) { pathToReportDirectory.set(value); }

    // counter
    public final LongProperty counterProperty() { return counter; }
    public final long getCounter() { return counter.get(); }
    public final void setCounter(long value) { counter.set(value); }

    // Геттер и сеттер для матчера
    public ParallelBinarySearchMatcher getParallelBinarySearchMatcher() {
        return parallelBinarySearchMatcher;
    }

    public void setParallelBinarySearchMatcher(ParallelBinarySearchMatcher parallelBinarySearchMatcher) {
        this.parallelBinarySearchMatcher = parallelBinarySearchMatcher;
    }
}
