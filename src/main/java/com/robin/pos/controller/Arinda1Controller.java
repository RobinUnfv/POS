package com.robin.pos.controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Arinda1Controller {

    @FXML
    private Button btnLimpiar;

    @FXML
    private Button btnRegistrar;

    @FXML
    private Button btnSalir;

    @FXML
    private ComboBox<?> cbxMedida;

    @FXML
    private ComboBox<?> cbxMoneda;

    @FXML
    private ComboBox<?> cbxTipoArticulo;

    @FXML
    private CheckBox chkVigente;

    @FXML
    private HBox hbxPie;

    @FXML
    private HBox hbxStockIndicator;

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtCodigoBarra;

    @FXML
    private TextField txtCostoUnitario;

    @FXML
    private TextField txtDescripcion;

    @FXML
    private TextField txtNoCia;

    @FXML
    private TextArea txtObservaciones;

    @FXML
    private TextField txtStockActual;

    @FXML
    private TextField txtStockMaximo;

    @FXML
    private TextField txtStockMinimo;

    @FXML
    private VBox vbxCuerpo;

    @FXML
    private VBox vbxPrincipal;

    @FXML
    void cerrarModal(ActionEvent event) {

    }

    @FXML
    void guardarArticulo(ActionEvent event) {

    }

    @FXML
    void limpiarFormulario(ActionEvent event) {

    }

}
