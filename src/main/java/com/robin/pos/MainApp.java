package com.robin.pos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    public static void main(String[] args) {
        launch(MainApp.class, args);
    }
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("Venta.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1270, 700);

        stage.setScene(scene);
        stage.show();
    }
}
