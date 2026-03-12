package com.robin.pos.util;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.control.Label;

public class FormCargar {
    // Stage interno para el indicador de carga
    private static Stage loadingStage;
    /**
     * Muestra el indicador de carga modal
     */
    public static void mostrarCargando(String mensaje, VBox vbxPrincipal) {

        Platform.runLater(() -> {
            try {
                loadingStage = new Stage();
                loadingStage.initStyle(StageStyle.UNDECORATED);
                loadingStage.initModality(Modality.APPLICATION_MODAL);

                // Obtener la ventana padre
                if (vbxPrincipal.getScene() != null && vbxPrincipal.getScene().getWindow() != null) {
                    loadingStage.initOwner(vbxPrincipal.getScene().getWindow());
                }

                VBox vbox = new VBox(15);
                vbox.setAlignment(Pos.CENTER);
                vbox.setStyle("-fx-background-color: white; -fx-padding: 30; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: #e2e8f0; -fx-border-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 5);");

                ProgressIndicator progress = new ProgressIndicator();
                progress.setStyle("-fx-progress-color: #3b82f6;");
                progress.setPrefSize(50, 50);

                Label label = new Label(mensaje);
                label.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-text-fill: #475569;");

                vbox.getChildren().addAll(progress, label);

                Scene scene = new Scene(vbox);
                scene.setFill(null);
                loadingStage.setScene(scene);
                loadingStage.show();

                // Centrar en pantalla
                loadingStage.centerOnScreen();

            } catch (Exception e) {
                System.out.println("No se pudo mostrar indicador de carga: " + e.getMessage());
            }
        });

    }

    public static Label actualizarEstado(String mensaje, Label lblEstado) {
        Platform.runLater(() -> {
            if (lblEstado != null) {
                lblEstado.setText(mensaje);
            }
        });
        return lblEstado;
    }

    /**
     * Oculta el indicador de carga
     */
    public static void ocultarCargando() {
        Platform.runLater(() -> {
            try {
                if (loadingStage != null) {
                    loadingStage.close();
                    loadingStage = null;
                }
            } catch (Exception e) {
                System.out.println("Error al ocultar indicador de carga: " + e.getMessage());
            }
        });
    }
}
