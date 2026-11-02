package com;

import com.model.DMCrossCheckFXModel;
import com.view.Presenter;
import com.view.DMCrossCheckFXView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DMCrossCheckFXApplication extends Application {
    static void main() {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        DMCrossCheckFXModel model = new DMCrossCheckFXModel();
        DMCrossCheckFXView view = new DMCrossCheckFXView();
        AlertUtils alert = new AlertUtils();

        Scene root = new Scene(view,800, 500);

        Presenter presenter = new Presenter(model, view, alert);



        stage.setScene(root);
        stage.centerOnScreen();
        stage.setTitle("Поиск дубликатов кодов DataMatrix");
        stage.setResizable(false);
        stage.show();

//        presenter.checkResult();
    }
}
