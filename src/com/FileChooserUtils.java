package com;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class FileChooserUtils {
    public static File selectFile(Window ownerWindow){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a File");

        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Text Files", "*.txt");
        FileChooser.ExtensionFilter noExtension = new FileChooser.ExtensionFilter("No Extension", "*.*");
//        fileChooser.getExtensionFilters().addAll(filter, noExtension);
//        fileChooser.setSelectedExtensionFilter(filter);

        return fileChooser.showOpenDialog(ownerWindow);
    }
}
