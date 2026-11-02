package com.view;

import com.AlertUtils;
import com.DirectoryChooserFX;
import com.FileChooserUtils;
import com.ParallelBinarySearchMatcher;
import com.model.DMCrossCheckFXModel;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

public class Presenter {
    private final DMCrossCheckFXModel model;
    private final DMCrossCheckFXView view;
    private final AlertUtils alert;

    public Presenter(DMCrossCheckFXModel model, DMCrossCheckFXView view, AlertUtils alert) {
        this.model = model;
        this.view = view;
        this.alert = alert;

        attachEvents();
        bindFieldsToModel();
    }

    private void attachEvents() {

        view.getB1().setOnAction(e -> model.setPathToDBFile(getPath()));
        view.getB2().setOnAction(e -> model.setPathToCodesFile(getPath()));
        view.getB3().setOnAction(e -> {
            String selectedDir = getDirectory();
            if (!selectedDir.isEmpty()) {
                model.setPathToReportDirectory(selectedDir);
            }

            // Логика авто-выбора папки, если пользователь отменил выбор
            String codesFile = model.getPathToCodesFile();
            if (codesFile != null && !codesFile.isBlank()) {
                String folder = Paths.get(codesFile).getParent().toAbsolutePath().toString();
                model.setPathToReportDirectory(folder);
            } else {
                model.setPathToReportDirectory("");
            }
        });

        view.getStartButton().setOnAction(e -> handleSearchStart());
    }

    private void handleSearchStart() {
        String codesPath = model.getPathToCodesFile();
        String dbPath = model.getPathToDBFile();
        String reportDir = model.getPathToReportDirectory();
        String fullReportPath = reportDir + DMCrossCheckFXModel.REPORT_FILE_NAME;

        // 1. Отвязываем валидацию и блокируем кнопку
        view.getStartButton().disableProperty().unbind();
        view.getStartButton().setDisable(true);

        // 2. Показываем прогресс-бар
        ProgressBar pb = view.getProgressBar();
        pb.setVisible(true);

        // 3. Создаем фоновую задачу
        Task<Long> searchTask = new Task<>() {
            @Override
            protected Long call() throws Exception {
                // Если ваш матчер поддерживает пошаговый прогресс, логика пишется так:
                // Во время долгого цикла внутри doSearch вы могли бы вызывать updateProgress.
                // Если матчер неделимый, JavaFX Task позволяет сделать индикатор "бегающим":
                updateProgress(-1, 1); // Включает режим "indeterminate" (анимация ожидания)

                ParallelBinarySearchMatcher matcher = new ParallelBinarySearchMatcher(codesPath, dbPath, fullReportPath);
                matcher.doSearch();

                updateProgress(1, 1); // Завершаем прогресс
                return matcher.getTotalMatches().get();
            }
        };

        // 4. Связываем прогресс-бар со свойствами задачи (выполняется автоматически!)
        pb.progressProperty().bind(searchTask.progressProperty());

        // Вспомогательный метод очистки интерфейса после завершения задачи
        Runnable cleanup = () -> {
            pb.progressProperty().unbind(); // Обязательно отвязываем свойство
            pb.setVisible(false);           // Скрываем прогресс-бар
            pb.setProgress(0);              // Сбрасываем значение

            // Возвращаем автоматическую валидацию кнопки "Старт"
            view.getStartButton().disableProperty().bind(
                    model.pathToDBFileProperty().isEmpty()
                            .or(model.pathToCodesFileProperty().isEmpty())
            );
        };

        searchTask.setOnSucceeded(event -> {
            long totalMatches = searchTask.getValue();
            model.setCounter(totalMatches);
            cleanup.run();
            checkResult();
        });

        searchTask.setOnFailed(event -> {
            cleanup.run();
            searchTask.getException().printStackTrace();
        });

        // 5. Запуск потока
        new Thread(searchTask).start();
    }



    private Task<Long> createSearchTask(String codesPath, String dbPath, String fullReportPath) {
        Task<Long> searchTask = new Task<>() {
            @Override
            protected Long call() throws Exception {
                ParallelBinarySearchMatcher matcher = new ParallelBinarySearchMatcher(codesPath, dbPath, fullReportPath);
                matcher.doSearch();
                return matcher.getTotalMatches().get();
            }
        };

        searchTask.setOnSucceeded(event -> {
            long totalMatches = searchTask.getValue();
            model.setCounter(totalMatches);
            view.getStartButton().setDisable(false);
            checkResult();
        });

        searchTask.setOnFailed(event -> {
            view.getStartButton().setDisable(false);
            // Здесь можно обработать ошибку через alert
            searchTask.getException().printStackTrace();
        });
        return searchTask;
    }

    private String getPath() {
        Window ownerWindow = view.getScene().getWindow();
        Optional<File> path = Optional.ofNullable(FileChooserUtils.selectFile(ownerWindow));
        return path.map(File::getAbsolutePath).orElse("");
    }

    private String getDirectory() {
//        Window ownerWindow = view.getScene().getWindow();
//        Optional<File> path = Optional.ofNullable(DirectoryChooserFX.selectDirectory(ownerWindow));
//        return path.map(File::getAbsolutePath).orElse("");
        return Optional.ofNullable(DirectoryChooserFX.selectDirectory(getWindow()))
                .map(File::getAbsolutePath)
                .orElse("");
    }

    private void bindFieldsToModel() {
        view.getPathToDBFile().textProperty().bind(model.pathToDBFileProperty());
        view.getPathToCodesFile().textProperty().bind(model.pathToCodesFileProperty());
        view.getPathToReportDirectory().textProperty().bind(model.pathToReportDirectoryProperty());

        // ВАЛИДАЦИЯ: Кнопка заблокирована, если хотя бы одно из полей пустое или содержит только пробелы
        view.getStartButton().disableProperty().bind(
                model.pathToDBFileProperty().isEmpty()
                        .or(model.pathToCodesFileProperty().isEmpty())
        );
    }

    public void checkResult() {
        Window ownerWindow = getWindow();
        if (model.getCounter() == 0) {
            alert.showInfo(ownerWindow);
        } else {
            alert.showAlert(ownerWindow, model.getCounter());
        }
    }

    private Window getWindow() {
        if (view.getScene() == null) return null;
        return view.getScene().getWindow();
    }
}
