package com.robin.pos.util;

import javafx.scene.control.Alert;

public class Mensaje {

    public static void alerta(String header, String titulo, String contenido) {
        Alert alert = new Alert( Alert.AlertType.INFORMATION);
        alert.setHeaderText(header);
        alert.setTitle(titulo);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    public static void error(String header, String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setTitle(titulo);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
