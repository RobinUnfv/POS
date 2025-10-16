package com.robin.pos.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class SunatController implements Initializable {

    @FXML
    private ComboBox<String> comboTipoDocumento;
    @FXML
    private TextField txtNumeroDocumento;
    @FXML
    private TextField txtNumeroRUC;
    @FXML
    private TextField txtEstado;
    @FXML
    private TextField txtCondicion;
    @FXML
    private TextField txtRazonSocial;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtDepartamento;
    @FXML
    private TextField txtProvincia;
    @FXML
    private TextField txtDistrito;
    @FXML
    private TextField txtUbigeo;
    @FXML
    private Button btnBuscar;
    @FXML
    private Button btnRegistrar;
    @FXML
    private Button btnSalir;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboTipoDocumento.setValue("RUC");
    }
}
