package com.robin.pos.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProgressDialog {
    private Stage dialogStage;
    private ProgressBar progressBar;
    private ProgressIndicator progressIndicator;
    private Label lblTitle;
    private Label lblMessage;
    private Label lblPercentage;
    private VBox mainContainer;

    public ProgressDialog() {
        crearDialogStage();
        inicializarComponentes();
        configurarLayout();
    }

    private void crearDialogStage() {
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setResizable(false);
        dialogStage.setAlwaysOnTop(true);
    }

    private void inicializarComponentes() {
        // Label de título
        lblTitle = new Label("Procesando");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTitle.setTextFill(Color.WHITE);

        // Label de mensaje
        lblMessage = new Label("Iniciando...");
        lblMessage.setTextFill(Color.WHITE);
        lblMessage.setWrapText(true);

        // Label de porcentaje
        lblPercentage = new Label("0%");
        lblPercentage.setTextFill(Color.WHITE);
        lblPercentage.setFont(Font.font("System", FontWeight.BOLD, 12));

        // Progress Bar
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(8);
        progressBar.setStyle("-fx-accent: #4CAF50; -fx-background-color: #37474F;");

        // Progress Indicator (para modo indeterminado)
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(40, 40);
        progressIndicator.setStyle("-fx-progress-color: #4CAF50;");
        progressIndicator.setVisible(false);
    }

    private void configurarLayout() {
        // Header con título
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 20, 10, 20));
        header.setStyle("-fx-background-color: #2196F3;");
        header.getChildren().add(lblTitle);

        // Contenedor de progreso
        HBox progressContainer = new HBox(10);
        progressContainer.setAlignment(Pos.CENTER_LEFT);
        progressContainer.setPadding(new Insets(10, 20, 5, 20));

        VBox progressInfo = new VBox(5);
        progressInfo.getChildren().addAll(lblMessage, progressBar);

        progressContainer.getChildren().addAll(progressInfo, lblPercentage);

        // Footer con indicador
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(5, 20, 15, 20));
        footer.getChildren().add(progressIndicator);

        // Contenedor principal
        mainContainer = new VBox();
        mainContainer.setBackground(new Background(new BackgroundFill(
                Color.web("#263238"),
                new CornerRadii(5),
                Insets.EMPTY
        )));
        mainContainer.setBorder(new Border(new BorderStroke(
                Color.web("#37474F"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(5),
                new BorderWidths(1)
        )));
        mainContainer.setEffect(new javafx.scene.effect.DropShadow(10, Color.BLACK));

        mainContainer.getChildren().addAll(header, progressContainer, footer);

        Scene scene = new Scene(mainContainer);
        scene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(scene);
    }

    public void show() {
        dialogStage.sizeToScene();
        dialogStage.centerOnScreen();
        dialogStage.show();
    }

    public void close() {
        dialogStage.close();
    }

    public void setTitle(String title) {
        lblTitle.setText(title);
    }

    public void setMessage(String message) {
        lblMessage.setText(message);
    }

    public void setProgress(double progress) {
        if (progress >= 0 && progress <= 1) {
            progressBar.setProgress(progress);
            int percentage = (int) (progress * 100);
            lblPercentage.setText(percentage + "%");

            progressBar.setVisible(true);
            progressIndicator.setVisible(false);
        } else {
            // Modo indeterminado
            progressBar.setVisible(false);
            progressIndicator.setVisible(true);
            lblPercentage.setText("");
        }
    }

    public void setIndeterminate(boolean indeterminate) {
        if (indeterminate) {
            progressBar.setVisible(false);
            progressIndicator.setVisible(true);
            lblPercentage.setText("");
        } else {
            progressBar.setVisible(true);
            progressIndicator.setVisible(false);
        }
    }

    public void bindToTask(javafx.concurrent.Task<?> task) {
        // Vincular progreso
        task.progressProperty().addListener((observable, oldValue, newValue) -> {
            javafx.application.Platform.runLater(() -> {
                if (newValue != null) {
                    setProgress(newValue.doubleValue());
                }
            });
        });

        // Vincular mensaje
        task.messageProperty().addListener((observable, oldValue, newValue) -> {
            javafx.application.Platform.runLater(() -> {
                if (newValue != null) {
                    setMessage(newValue);
                }
            });
        });

        // Manejar estados de la task
        task.setOnRunning(e -> {
            javafx.application.Platform.runLater(() -> {
                setIndeterminate(false);
                show();
            });
        });

        task.setOnSucceeded(e -> {
            javafx.application.Platform.runLater(() -> {
                close();
            });
        });

        task.setOnFailed(e -> {
            javafx.application.Platform.runLater(() -> {
                close();
            });
        });

        task.setOnCancelled(e -> {
            javafx.application.Platform.runLater(() -> {
                close();
            });
        });
    }
}
