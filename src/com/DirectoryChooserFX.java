package com;

import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;

public class DirectoryChooserFX {
        public static File selectDirectory(Window ownerWindow){
            // 1. Instantiate the dedicated DirectoryChooser
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select a Folder");
            return directoryChooser.showDialog(ownerWindow);
        }
}
