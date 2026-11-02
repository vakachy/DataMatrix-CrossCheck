package com.view;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

public class DMCrossCheckFXView extends GridPane {

    private static final String LABEL_STYLE =
            "-fx-text-fill: green; " +
                    "-fx-font-size: 10pt; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-color: #f0f0f0; " +
                    "-fx-padding: 5px 10px; " +
                    "-fx-border-color: #cccccc; " +
                    "-fx-border-radius: 4px;";

    private static final Insets DEFAULT_MARGIN = new Insets(10);
    private static final Insets LABEL_MARGIN = new Insets(20, 10, 5, 10);

    // Labels
    private final Label pathToDBFileLbl = createLabel("Выбрать файл, содержащий базу кодов для проверки", HPos.LEFT, false);
    private final Label pathToCodesFileLbl = createLabel("Выбрать файл, содержащий новую базу кодов для печати", HPos.CENTER, false);
    private final Label pathToReportLbl = createLabel("Выбрать папку, для сохранения отчета\n(если не выбрать, то отчет сохраняется в папке с кодами для печати)", HPos.CENTER, true);

    // Text Fields
    private final TextField pathToDBFile = createTextField("Путь к выбранному файлу");
    private final TextField pathToCodesFile = createTextField("Путь к выбранному файлу");
    private final TextField pathToReportDirectory = createTextField("Путь к выбранной папке");

    // Buttons
    private final Button b1 = createButton("Выбрать файл");
    private final Button b2 = createButton("Выбрать файл");
    private final Button b3 = createButton("Выбрать папку");
    private final Button startButton = createButton("Старт");

    private final ProgressBar progressBar = new ProgressBar(0);

    public DMCrossCheckFXView() {
        layoutForm();
    }

    // --- ГЕТТЕРЫ ДЛЯ КОНТРОЛЛЕРА ---

    public TextField getPathToDBFile() { return pathToDBFile; }
    public TextField getPathToCodesFile() { return pathToCodesFile; }
    public TextField getPathToReportDirectory() { return pathToReportDirectory; }

    public Button getB1() { return b1; }
    public Button getB2() { return b2; }
    public Button getB3() { return b3; }
    public Button getStartButton() { return startButton; }

    public ProgressBar getProgressBar() { return progressBar; }

    // -------------------------------

    private Label createLabel(String text, HPos hPos, boolean wrapText) {
        Label label = new Label(text);
        label.setStyle(LABEL_STYLE);
        label.setWrapText(wrapText);
        label.setMaxWidth(Double.MAX_VALUE);
        GridPane.setValignment(label, VPos.BOTTOM);
        GridPane.setHalignment(label, hPos);
        GridPane.setMargin(label, LABEL_MARGIN);
        return label;
    }

    private TextField createTextField(String promptText) {
        TextField tf = new TextField();
        tf.setDisable(true);
        tf.setPromptText(promptText);
        GridPane.setMargin(tf, DEFAULT_MARGIN);
        return tf;
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        GridPane.setMargin(btn, DEFAULT_MARGIN);
        return btn;
    }

    public void layoutForm() {
        // Добавление элементов в GridPane
        add(pathToDBFileLbl, 0, 0);
        add(pathToDBFile, 0, 1);
        add(b1, 1, 1);

        add(pathToCodesFileLbl, 0, 2);
        add(pathToCodesFile, 0, 3);
        add(b2, 1, 3);

        add(pathToReportLbl, 0, 4);
        add(pathToReportDirectory, 0, 5);
        add(b3, 1, 5);

        // Настройка прогресс-бара
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setVisible(false); // Изначально скрываем его
        add(progressBar, 0, 6); // Размещаем в столбце 0 (широком), строка 6
        GridPane.setMargin(progressBar, new Insets(10));

        // Сдвигаем кнопку "Старт" на строку 7 и центрируем
        GridPane.setValignment(startButton, VPos.BOTTOM);
        GridPane.setHalignment(startButton, HPos.CENTER);
        startButton.setMaxHeight(50);
        add(startButton, 0, 7, 2, 1); // Растянем кнопку на 2 столбца для красоты

        // Настройка столбцов
        ColumnConstraints col0 = new ColumnConstraints();
        col0.setHgrow(Priority.ALWAYS);
        getColumnConstraints().add(col0);

        // Настройка строк
        getRowConstraints().addAll(
                new RowConstraints(80),  // 0
                new RowConstraints(),    // 1
                new RowConstraints(80),  // 2
                new RowConstraints(),    // 3
                new RowConstraints(100), // 4
                new RowConstraints(),    // 5
                new RowConstraints(),    // 6 (для ProgressBar)
                applyVGrowAlways(new RowConstraints()) // 7 (для кнопки Старт)
        );

//        // Кнопка "Старт" (b4)
//        GridPane.setValignment(startButton, VPos.BOTTOM);
//        GridPane.setHalignment(startButton, HPos.CENTER);
//        startButton.setMaxHeight(50);
//        add(startButton, 1, 6);
    }

    private RowConstraints applyVGrowAlways(RowConstraints rc) {
        rc.setVgrow(Priority.ALWAYS);
        return rc;
    }
}
