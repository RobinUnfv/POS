package com.robin.pos.controller;

import com.robin.pos.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardController {

    @FXML
    private Button btn;

    @FXML
    private Button btnCliente;

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnProductos;

    @FXML
    private Button btnReportes;

    @FXML
    private Button btnVentas;

    @FXML
    private ImageView imgLogoCia;

    @FXML
    private TextField txtBuscar;

    @FXML
    private TabPane tabPane;

    @FXML
    void ingresarDashboard(ActionEvent event) {
        this.cambiarColorBtnSeleccionado((Button) event.getSource());
    }

    @FXML
    void ingreasarVenta(ActionEvent  event) throws IOException {
        this.cambiarColorBtnSeleccionado((Button) event.getSource());
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/robin/pos/fxml/Venta.fxml"));
        BorderPane ap = loader.load();
        VentaController ventaController = loader.getController();
        Tab tabVenta = new Tab(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE dd MMM hh:mm:ss a")), ap);
        tabVenta.setClosable(true);
        this.tabPane.getTabs().add(tabVenta);
        this.tabPane.getSelectionModel().select(tabVenta);
    }

    @FXML
    void ingresarCliente(ActionEvent event) {
        this.cambiarColorBtnSeleccionado((Button) event.getSource());
    }

    @FXML
    void ingresarProducto(ActionEvent event) {
        this.cambiarColorBtnSeleccionado((Button) event.getSource());
    }

    @FXML
    void ingresarReporte(ActionEvent event) {
        this.cambiarColorBtnSeleccionado((Button) event.getSource());
    }

    private void cambiarColorBtnSeleccionado(Button btnSeleccionado) {
        String colorDefecto = "-fx-background-color:  transparent; -fx-text-fill: black; -fx-border-width: 0 ; -fx-font-size: 14px;";
        String colorSeleccionado = "-fx-background-color: #dcdcdc; -fx-text-fill: black; -fx-border-width: 0 ; -fx-font-size: 14px; -fx-font-weight: bold;";
        // Resetear el estilo de todos los botones
        this.btnDashboard.setStyle(colorDefecto);
        this.btnVentas.setStyle(colorDefecto);
        this.btnCliente.setStyle(colorDefecto);
        this.btnProductos.setStyle(colorDefecto);
        this.btnReportes.setStyle(colorDefecto);

        // Aplicar el estilo al botón seleccionado
        btnSeleccionado.setStyle(colorSeleccionado);
    }

}
