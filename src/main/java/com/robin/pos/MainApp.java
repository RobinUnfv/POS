package com.robin.pos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import javafx.scene.image.Image;

import java.io.IOException;

public class MainApp extends Application {
    public static void main(String[] args) {
        launch(MainApp.class, args);
    }
    @Override
    public void start(Stage stage) throws IOException {
        //FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/com/robin/pos/fxml/Venta.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/com/robin/pos/fxml/Dashboard.fxml"));
        // ========== AGREGAR ÍCONO ==========
        // Opción 1: Desde recursos del proyecto
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/robin/pos/imagenes/logos-nexer.png")));

        Scene scene = new Scene(fxmlLoader.load(), 1270, 700);
        stage.setMaximized(true);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        stage.setTitle("VENTA");
        stage.setScene(scene);
        stage.show();
    }
}
