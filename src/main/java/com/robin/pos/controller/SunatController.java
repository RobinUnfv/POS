package com.robin.pos.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

public class SunatController implements Initializable {

    @FXML
    private ComboBox<String> cbxTipoDocumento;
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
        configuracionNumeroDocumento(this.txtNumeroDocumento,"RUC");
    }

    void configuracionNumeroDocumento(TextField textField, String tipoDocumento) {
        UnaryOperator<TextFormatter.Change> filter;
        if (tipoDocumento == "RUC") {
             filter = change -> {
                String newText = change.getControlNewText();

                // Permite solo dígitos y máximo 11 caracteres
                if (newText.matches("\\d{0,11}")) {
//                    txtNumeroDocumento.setStyle("-fx-border-color: green; -fx-border-width: 1px; -fx-background-radius: 5; -fx-border-radius: 5;");
                    return change;
                }

                return null; // Rechaza el cambio si no cumple
            };
        } else {
            filter = change -> {
                String newText = change.getControlNewText();

                // Permite solo dígitos y máximo 8 caracteres
                if (newText.matches("\\d{0,8}")) {
                    return change;
                }
                txtNumeroDocumento.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
                return null;
            };
        }



        textField.setTextFormatter(new TextFormatter<>(filter));
    }

    @FXML
    void salir(ActionEvent event) {
        // logica para cerrar el modal
          btnSalir.getScene().getWindow().hide();
//        System.out.println("=)");
    }

    @FXML
    void buscar(ActionEvent event) {

    }

    @FXML
    private void buscarDocumento(KeyEvent evt) {
        if (evt.getCode() == KeyCode.ENTER) {

        }
    }

    @FXML
    void cambiarTipoDocumento(ActionEvent event) {
      String tipoDocumento = this.cbxTipoDocumento.getValue();
      configuracionNumeroDocumento(this.txtNumeroDocumento, tipoDocumento);
      this.txtNumeroDocumento.requestFocus();
    }

}
