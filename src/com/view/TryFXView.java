package com.view;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

public class TryFXView extends GridPane {

    // Labels
    Label pathToDBFileLbl = new Label("Выбрать файл, содержащий базу кодов для проверки");
    Label pathToCodesFileLbl = new Label(" Выбрать файл, содержащий новую базу кодов для печати");
    Label pathToReportLbl = new Label(" Выбрать папку, для сохранения отчета\n(если не выбрать, то отчет сохраняется в папке с кодами для печати)");
    // Text Fields
    TextField pathToDBFile = new TextField();
    TextField pathToCodesFile = new TextField();
    TextField pathToReportDirectory = new TextField();
    // Buttons
    Button b1 = new Button("Выбрать файл");
    Button b2 = new Button("Выбрать файл");
    Button b3 = new Button("Выбрать папку");
    Button b4 = new Button("Старт");
    public TryFXView() {
        layoutForm();
    }

    public void layoutForm() {

        // Prompt text
        String prompt_1 = "Путь к выбранному файлу";
        String prompt_2 = "Путь к выбранной папке";

        pathToDBFileLbl.setStyle("-fx-text-fill: green; -fx-font-size: 10pt; -fx-font-weight: bold; -fx-background-color: #f0f0f0; -fx-padding: 5px 10px; -fx-border-color: #cccccc; -fx-border-radius: 4px;");
        this.add(pathToDBFileLbl, 0, 0);
        GridPane.setValignment(pathToDBFileLbl, VPos.BOTTOM);
        GridPane.setHalignment(pathToDBFileLbl, HPos.LEFT);
        pathToDBFileLbl.setMaxWidth(Double.MAX_VALUE);
        GridPane.setMargin(pathToDBFileLbl, new Insets(20, 10, 5, 10));
        this.add(pathToDBFile, 0, 1);
        GridPane.setMargin(pathToDBFile, new Insets(10));
        pathToDBFile.setDisable(true);
        pathToDBFile.setPromptText(prompt_1);
        this.add(b1, 1, 1);
        b1.setMaxWidth(Double.MAX_VALUE);
        GridPane.setMargin(b1, new Insets(10));

        pathToCodesFileLbl.setStyle("-fx-text-fill: green; -fx-font-size: 10pt; -fx-font-weight: bold; -fx-background-color: #f0f0f0; -fx-padding: 5px 10px; -fx-border-color: #cccccc; -fx-border-radius: 4px;");
        this.add(pathToCodesFileLbl, 0, 2);

        GridPane.setValignment(pathToCodesFileLbl, VPos.BOTTOM);
        GridPane.setHalignment(pathToCodesFileLbl, HPos.CENTER);
        pathToCodesFileLbl.setMaxWidth(Double.MAX_VALUE);
        GridPane.setMargin(pathToCodesFileLbl, new Insets(20, 10, 5, 10));
        this.add(pathToCodesFile, 0, 3);
        GridPane.setMargin(pathToCodesFile, new Insets(10));
        pathToCodesFile.setDisable(true);
        pathToCodesFile.setPromptText(prompt_1);
        this.add(b2, 1, 3);
        b2.setMaxWidth(Double.MAX_VALUE);
        GridPane.setMargin(b2, new Insets(10));

        pathToReportLbl.setStyle("-fx-text-fill: green; -fx-font-size: 10pt; -fx-font-weight: bold; -fx-background-color: #f0f0f0; -fx-padding: 5px 10px; -fx-border-color: #cccccc; -fx-border-radius: 4px;");
        pathToReportLbl.setWrapText(true);
        this.add(pathToReportLbl, 0, 4);
        GridPane.setValignment(pathToReportLbl, VPos.BOTTOM);
        GridPane.setHalignment(pathToReportLbl, HPos.CENTER);
        pathToReportLbl.setMaxWidth(Double.MAX_VALUE);
        GridPane.setMargin(pathToReportLbl, new Insets(20, 10, 5, 10));
        this.add(pathToReportDirectory, 0, 5);
        GridPane.setMargin(pathToReportDirectory, new Insets(10));
        pathToReportDirectory.setDisable(true);
        pathToReportDirectory.setPromptText(prompt_2);
        this.add(b3, 1, 5);
        b3.setMaxWidth(Double.MAX_VALUE);
        GridPane.setMargin(b3, new Insets(10));

        ColumnConstraints column0 = new ColumnConstraints();
        column0.setHgrow(Priority.ALWAYS);
        this.getColumnConstraints().add(column0);

        RowConstraints row0 = new RowConstraints(80);
        this.getRowConstraints().add(row0);

        RowConstraints row1 = new RowConstraints();
        this.getRowConstraints().add(row1);

        RowConstraints row2 = new RowConstraints(80);
        this.getRowConstraints().add(row2);

        RowConstraints row3 = new RowConstraints();
        this.getRowConstraints().add(row3);

        RowConstraints row4 = new RowConstraints(100);
        this.getRowConstraints().add(row4);

        RowConstraints row5 = new RowConstraints();
        this.getRowConstraints().add(row5);

        RowConstraints row6 = new RowConstraints();
        row6.setVgrow(Priority.ALWAYS);
        this.getRowConstraints().add(row6);


        GridPane.setValignment(b4, VPos.BOTTOM);
        GridPane.setHalignment(b4, HPos.CENTER);
        this.add(b4, 1, 6);
        b4.setMaxHeight(50);
        b4.setMaxWidth(Double.MAX_VALUE);
        GridPane.setMargin(b4, new Insets(10));

//        this.setGridLinesVisible(true);
    }
}
