package com.robin.pos.controller;

import com.robin.pos.dao.ArccmcDao;
import com.robin.pos.model.Arccmc;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ListaClienteController implements Initializable {

    @FXML
    private ImageView btnEditar;

    @FXML
    private ImageView btnNuevo;

    @FXML
    private TableColumn<?, ?> colCodigo;

    @FXML
    private TableColumn<?, ?> colNombre;

    @FXML
    private TableColumn<?, ?> colTipCliente;

    @FXML
    private TableColumn<?, ?> colTipPersona;

    @FXML
    private TableColumn<?, ?> colEstado;

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

    private ObservableList<Arccmc> listaClientes = FXCollections.observableArrayList();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.cargarClientes();
        tListaCliente.setItems(listaClientes);
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

}
