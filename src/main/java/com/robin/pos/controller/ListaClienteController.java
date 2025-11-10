package com.robin.pos.controller;

import com.robin.pos.dao.ArccmcDao;
import com.robin.pos.model.Arccmc;
import com.robin.pos.model.Arinda1;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class ListaClienteController implements Initializable {

    @FXML
    private ImageView btnEditar;

    @FXML
    private ImageView btnNuevo;

    @FXML
    private TableColumn<Arccmc, String> colCodigo;

    @FXML
    private TableColumn<Arccmc, String> colNombre;

    @FXML
    private TableColumn<Arccmc, String> colTipCliente;

    @FXML
    private TableColumn<Arccmc, String> colTipPersona;

    @FXML
    private TableColumn<Arccmc, String> colEstado;

    @FXML
    private HBox hbxCabecera;

    @FXML
    private Label lblListaCliente;

    @FXML
    private TableView<Arccmc> tListaCliente;

    @FXML
    private TextField txtBuscarCliente;

    @FXML
    private VBox vbxListaCliente;

    @FXML
    private VBox vbxPrincipal;

    FilteredList<Arccmc> filtro;

    private ObservableList<Arccmc> listaClientes = FXCollections.observableArrayList();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("noCliente"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipCliente.setCellValueFactory(new PropertyValueFactory<>("tipoCliente"));
        colTipPersona.setCellValueFactory(new PropertyValueFactory<>("tipoPersona"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("activo"));

        filtro = new FilteredList<>(listaClientes, e -> true);
        SortedList<Arccmc> sorterData = new SortedList<>(filtro);
        sorterData.comparatorProperty().bind(this.tListaCliente.comparatorProperty());
        tListaCliente.setItems(sorterData);

        // Listener de búsqueda: una sola vez, con comprobación nula
        this.txtBuscarCliente.textProperty().addListener((obs, oldValue, newValue) -> {
            final String texto = (newValue == null) ? "" : newValue.trim().toUpperCase();
            filtro.setPredicate(param -> {
                if (texto.isEmpty()) return true;
                String nombre = param.getNombre();
                if (nombre == null) return false; // evita NPE cuando nombre es null
                return nombre.toUpperCase().contains(texto);
            });
        });
        this.cargarClientes();

    }

    @FXML
    void crearCliente(KeyEvent event) {

    }

    @FXML
    void editarCliente(KeyEvent event) {

    }

    private void cargarClientes() {
        Task<List<Arccmc>> listTask = new Task<List<Arccmc>>() {
            @Override
            protected List<Arccmc> call() throws Exception {
                // Aquí iría la lógica para cargar los clientes desde la base de datos
                ArccmcDao arccmcDao = new ArccmcDao();
                return arccmcDao.listar("01");
            }
        };

        listTask.setOnFailed(event1 -> {
            tListaCliente.setPlaceholder(null);
        });

        listTask.setOnSucceeded(event1 -> {
            tListaCliente.setPlaceholder(null);
            listaClientes.setAll(listTask.getValue());

        });

        ProgressIndicator progressIndicator = new ProgressIndicator();
        tListaCliente.setPlaceholder(progressIndicator);

        Thread hilo = new Thread(listTask);
        hilo.start();

    }

    @FXML
    void buscarCliente(KeyEvent event) {
       switch (event.getCode()) {
           case DOWN:
               tListaCliente.requestFocus();
               tListaCliente.getSelectionModel().select(0, colNombre);
               break;
           case ESCAPE:
               this.btnEditar.requestFocus();
               break;
           default:
               break;
       }
    }

}
