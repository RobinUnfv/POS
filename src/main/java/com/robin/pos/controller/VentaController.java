package com.robin.pos.controller;

import com.robin.pos.dao.ClienteDao;
import com.robin.pos.model.Cliente;
import com.robin.pos.util.ConexionBD;
import com.robin.pos.util.Mensaje;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import java.sql.SQLException;

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
    private ComboBox<String> cbxDocIdentidad;

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
    private TextField txtListaProd;

    private ClienteDao clienteDao;

    @FXML
    void buscarCliente(ActionEvent event) {

       if (this.txtNumDoc.getText().isEmpty()) {
           Mensaje.alerta(null,"Número Documento","Ingrese el Número de documento");
           return;
       }

       clienteDao = new ClienteDao();
       Cliente cliente = clienteDao.buscarPorNumId("01",txtNumDoc.getText());

       if (cliente ==  null) {
            Mensaje.error(null,"Consulta cliente","El número de documento "+this.txtNumDoc.getText()+" no valido.");
            this.limpiarCliente();
            return;
       }

       this.mostrarCliente(cliente);
    }

    @FXML
    void buscarClienteNumId(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (this.txtNumDoc.getText().isEmpty()) {
                this.limpiarCliente();
                return;
            }

            clienteDao = new ClienteDao();
            Cliente cliente = clienteDao.buscarPorNumId("01",txtNumDoc.getText());

            if (cliente ==  null) {
                Mensaje.error(null,"Consulta cliente","El número de documento "+this.txtNumDoc.getText()+" no valido.");
                this.limpiarCliente();
                return;
            }

            this.mostrarCliente(cliente);
        }
    }

    void mostrarCliente(Cliente cliente) {
        txtNumDoc.setText(cliente.getNoCliente());
        txtRazSocNom.setText(cliente.getNombre());
        txtDireccion.setText(cliente.getDireccion());
    }

    void limpiarCliente(){
        txtRazSocNom.setText("");
        txtDireccion.setText("");
    }

    @FXML
    void escogerTipoDocumento(ActionEvent event) {
        this.limpiarCliente();
        this.txtNumDoc.setText("");

        if (this.cbxDocIdentidad.getSelectionModel().getSelectedItem().equals("DNI")) {
            this.txtNumDoc.setPromptText("DNI");
            this.txtNumDoc.setTextFormatter(new TextFormatter<String>(change ->
                    change.getControlNewText().length() <= 8 ? change : null));
        } else if (this.cbxDocIdentidad.getSelectionModel().getSelectedItem().equals("RUC")) {
            this.txtNumDoc.setPromptText("RUC");
            this.txtNumDoc.setTextFormatter(new TextFormatter<String>(change ->
                    change.getControlNewText().length() <= 11 ? change : null));

        } else if (this.cbxDocIdentidad.getSelectionModel().getSelectedItem().equals("OTR")) {
            this.txtNumDoc.setPromptText("OTROS");
            this.txtNumDoc.setTextFormatter(new TextFormatter<String>(change ->
                    change.getControlNewText().length() <= 11 ? change : null));
            this.txtNumDoc.setText("99999999998");

        } else {
            this.txtNumDoc.setPromptText("Número Documento");
            this.txtNumDoc.setTextFormatter(new TextFormatter<String>(change ->
                    change.getControlNewText().length() <= 15 ? change : null));
        }

        this.txtNumDoc.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));

        this.txtNumDoc.requestFocus();
    }

}
