package com;

import javafx.scene.control.Alert;
import javafx.stage.Window;

public class AlertUtils {

    public void showInfo(Window ownerWindow){
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setContentText("Нет повторений!");
        info.initOwner(ownerWindow);
        info.showAndWait();
    }

    public void showAlert(Window ownerWindow, long l) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("Найдены повторения!\n" + l);
        alert.initOwner(ownerWindow);
        alert.showAndWait();
    }
}
