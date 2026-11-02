package com.model;

import com.ParallelBinarySearchMatcher;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TryFXModel {
    private final StringProperty pathToDBFile = new SimpleStringProperty(this, "pathToDBFile", null);
    private final StringProperty pathToCodesFile = new SimpleStringProperty(this, "pathToCodesFile", null);
    private final StringProperty pathToReportDirectory = new SimpleStringProperty(this, "pathToReportDirectory", null);

    public final StringProperty PathToDBFileProperty(){
        return pathToDBFile;
    }

    public final StringProperty PathToCodesFileProperty(){
        return pathToCodesFile;
    }

    public final StringProperty PathToReportDirectoryProperty(){
        return pathToReportDirectory;
    }

    public final String REPORT = "\\report.txt";

    private final LongProperty counter = new SimpleLongProperty(this, "counter", 0L);

    public final LongProperty counterProperty(){return counter;}

    public ParallelBinarySearchMatcher parallelBinarySearchMatcher;
}
