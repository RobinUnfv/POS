package com.robin.pos.controller;

import com.robin.pos.dao.Arinda1Dao;
import com.robin.pos.dao.ClienteDao;
import com.robin.pos.model.Arinda1;
import com.robin.pos.model.Cliente;
import com.robin.pos.model.DetalleVenta;
import com.robin.pos.util.AutoCompleteTextField;
import com.robin.pos.util.Mensaje;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class VentaController implements Initializable {

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
    private TableView<DetalleVenta> tProducto;

    @FXML
    private TableColumn<DetalleVenta, String> colProducto;

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
    private AutoCompleteTextField txtListaProd;
    private Task<List<Arinda1>> busquedaTask;

    private ClienteDao clienteDao;
    private Arinda1Dao arinda1Dao;

    ObservableList<DetalleVenta> listaDetalleVentas = FXCollections.observableArrayList();

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configurar el AutoCompleteTextField
        configurarAutoComplete();
    }

    private void configurarAutoComplete() {
        // Listener para buscar productos mientras escribe
        txtListaProd.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() >= 2) {
                buscarProductosAsync(newVal);
            }
        });

        txtListaProd.setOnProductoSeleccionado(producto -> cargarDatosProducto((Arinda1) producto));

    }

    private void buscarProductosAsync(String criterio) {
        // Cancelar búsqueda anterior si existe
        if (busquedaTask != null && busquedaTask.isRunning()) {
            busquedaTask.cancel();
        }

        // Crear nueva tarea de búsqueda
        busquedaTask = new Task<List<Arinda1>>() {
            @Override
            protected List<Arinda1> call() {
                arinda1Dao = new Arinda1Dao();
                return arinda1Dao.buscarProducto("01", criterio); //dao.buscarProductos(criterio);
            }
        };

        busquedaTask.setOnSucceeded(e -> {
            List<Arinda1> productos = busquedaTask.getValue();
            txtListaProd.mostrarSugerencias(productos);
        });

        busquedaTask.setOnFailed(e -> {
            System.err.println("Error en búsqueda: " + busquedaTask.getException().getMessage());
        });

        // Ejecutar en hilo separado
        Thread thread = new Thread(busquedaTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void cargarDatosProducto(Arinda1 producto) {
        if (producto != null) {
            System.out.println(producto.getCodigo() + " + " + producto.getDescripcion());
        }
    }

}
