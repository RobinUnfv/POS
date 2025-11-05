package com.robin.pos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ListaClienteController {

    @FXML
    private ImageView btnEditar;

    @FXML
    private ImageView btnNuevo;

    @FXML
    private TableColumn<?, ?> colCodigo;

    @FXML
    private TableColumn<?, ?> colEstado;

    @FXML
    private TableColumn<?, ?> colNombre;

    @FXML
    private HBox hbxCabecera;

    @FXML
    private Label lblListaCliente;

    @FXML
    private TableView<?> tListaCliente;

    @FXML
    private TextField txtBuscarCliente;

    @FXML
    private VBox vbxListaCliente;

    @FXML
    private VBox vbxPrincipal;

    @FXML
    void crearCliente(KeyEvent event) {

    }

    @FXML
    void editarCliente(KeyEvent event) {

    }


}
