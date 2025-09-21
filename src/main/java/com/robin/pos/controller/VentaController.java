package com.robin.pos.controller;

import com.robin.pos.util.Mensaje;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class VentaController {

    @FXML
    private Button agregarProductoButton;

    @FXML
    private Button btnBoleta;

    @FXML
    private Button btnBuscarCliente;

    @FXML
    private Button btnFactura;

    @FXML
    private Button btnNuevoCliente;

    @FXML
    private ComboBox<?> cbxDocIdentidad;

    @FXML
    private ComboBox<?> cbxFormaPago;

    @FXML
    private ComboBox<?> cbxMoneda;

    @FXML
    private ComboBox<?> cbxMotConting;

    @FXML
    private TextField lblIgv;

    @FXML
    private TextField lblSubTotal;

    @FXML
    private TextField lblTotGravada;

    @FXML
    private Label lblTotal;

    @FXML
    private TableView<?> tProducto;

    @FXML
    private TextArea txtDireccion;

    @FXML
    private DatePicker txtFechaVenta;

    @FXML
    private TextField txtNumDoc;

    @FXML
    private TextField txtPago;

    @FXML
    private TextField txtRazSocNom;

    @FXML
    private TextField txtVuelto;

    @FXML
    void buscarCliente(ActionEvent event) {
        Mensaje.alerta(null, "Cliente","Cliente no encontrado.");
    }

}
