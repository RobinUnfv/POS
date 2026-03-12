package com.robin.pos.controller;

import com.robin.pos.MainApp;
import com.robin.pos.dao.ConfigSunatDao;
import com.robin.pos.model.ConfigSunat;
import com.robin.pos.util.Mensaje;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para la lista de configuraciones SUNAT
 *
 * @author Robin POS
 * @version 1.0
 */
public class ListaConfSunatController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ListaConfSunatController.class.getName());
    private static final String NO_CIA = "01";

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbxFiltroGrupo;
    @FXML private TableView<ConfigSunat> tblConfiguraciones;
    @FXML private TableColumn<ConfigSunat, String> colCodigo;
    @FXML private TableColumn<ConfigSunat, String> colDescripcion;
    @FXML private TableColumn<ConfigSunat, String> colValor;
    @FXML private TableColumn<ConfigSunat, String> colGrupo;
    @FXML private TableColumn<ConfigSunat, String> colTipoDato;
    @FXML private TableColumn<ConfigSunat, String> colActivo;
    @FXML private Label lblTotalRegistros;
    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnRefrescar;

    private final ConfigSunatDao configSunatDao = new ConfigSunatDao();
    private ObservableList<ConfigSunat> listaConfiguraciones;
    private FilteredList<ConfigSunat> listaFiltrada;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Inicializando ListaConfSunatController");

        configurarTabla();
        configurarFiltros();
        configurarAtajos();
        cargarDatos();

        LOGGER.info("ListaConfSunatController inicializado");
    }

    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colGrupo.setCellValueFactory(new PropertyValueFactory<>("grupo"));
        colTipoDato.setCellValueFactory(new PropertyValueFactory<>("tipoDato"));

        // Columna VALOR con lógica de enmascaramiento
        colValor.setCellValueFactory(cellData -> cellData.getValue().valorProperty());
        colValor.setCellFactory(column -> new TableCell<ConfigSunat, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    ConfigSunat config = getTableRow().getItem();
                    if (config.isPassword()) {
                        setText("••••••••");
                        setStyle("-fx-text-fill: #6b7280; -fx-font-style: italic;");
                    } else {
                        setText(item);
                        setStyle("");
                    }
                }
            }
        });

        // Columna ACTIVO con badge
        colActivo.setCellValueFactory(cellData -> cellData.getValue().activoProperty());
        colActivo.setCellFactory(column -> new TableCell<ConfigSunat, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    if ("S".equals(item)) {
                        setText("● Activo");
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else {
                        setText("○ Inactivo");
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Doble clic para editar
        tblConfiguraciones.setOnMouseClicked(this::handleTableClick);
    }

    /**
     * Configura los filtros de búsqueda
     */
    private void configurarFiltros() {
        // ComboBox de grupos
        cbxFiltroGrupo.getItems().add("Todos");
        cbxFiltroGrupo.setValue("Todos");

        // Listener para búsqueda
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
        cbxFiltroGrupo.valueProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
    }

    /**
     * Configura atajos de teclado
     */
    private void configurarAtajos() {
        tblConfiguraciones.setOnKeyPressed(this::handleKeyPressed);
    }

    /**
     * Carga los datos desde la base de datos
     */
    private void cargarDatos() {
        try {
            List<ConfigSunat> datos = configSunatDao.listarPorCompania(NO_CIA);
            listaConfiguraciones = FXCollections.observableArrayList(datos);

            listaFiltrada = new FilteredList<>(listaConfiguraciones, p -> true);
            SortedList<ConfigSunat> listaOrdenada = new SortedList<>(listaFiltrada);
            listaOrdenada.comparatorProperty().bind(tblConfiguraciones.comparatorProperty());

            tblConfiguraciones.setItems(listaOrdenada);

            // Cargar grupos en ComboBox
            cargarGrupos();

            actualizarContador();

            LOGGER.info("Cargadas " + datos.size() + " configuraciones");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar configuraciones", e);
            Mensaje.error(null, "Error de Carga",
                    "No se pudieron cargar las configuraciones:\n" + e.getMessage());
        }
    }

    /**
     * Carga los grupos únicos en el ComboBox
     */
    private void cargarGrupos() {
        List<String> grupos = configSunatDao.listarGrupos(NO_CIA);
        cbxFiltroGrupo.getItems().clear();
        cbxFiltroGrupo.getItems().add("Todos");
        cbxFiltroGrupo.getItems().addAll(grupos);
        cbxFiltroGrupo.setValue("Todos");
    }

    /**
     * Aplica los filtros de búsqueda
     */
    private void aplicarFiltros() {
        listaFiltrada.setPredicate(config -> {
            String textoBusqueda = txtBuscar.getText();
            String grupoSeleccionado = cbxFiltroGrupo.getValue();

            // Filtro por texto
            boolean coincideTexto = true;
            if (textoBusqueda != null && !textoBusqueda.isEmpty()) {
                String busquedaLower = textoBusqueda.toLowerCase();
                coincideTexto = (config.getCodigo() != null && config.getCodigo().toLowerCase().contains(busquedaLower))
                        || (config.getDescripcion() != null && config.getDescripcion().toLowerCase().contains(busquedaLower))
                        || (config.getValor() != null && config.getValor().toLowerCase().contains(busquedaLower));
            }

            // Filtro por grupo
            boolean coincideGrupo = true;
            if (grupoSeleccionado != null && !"Todos".equals(grupoSeleccionado)) {
                coincideGrupo = grupoSeleccionado.equals(config.getGrupo());
            }

            return coincideTexto && coincideGrupo;
        });

        actualizarContador();
    }

    /**
     * Actualiza el contador de registros
     */
    private void actualizarContador() {
        int total = listaFiltrada != null ? listaFiltrada.size() : 0;
        lblTotalRegistros.setText("Total: " + total + " configuración" + (total != 1 ? "es" : ""));
    }

    /**
     * Maneja el clic en la tabla
     */
    private void handleTableClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            editar(null);
        }
    }

    /**
     * Maneja las teclas presionadas
     */
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            editar(null);
            event.consume();
        } else if (event.getCode() == KeyCode.DELETE) {
            eliminar(null);
            event.consume();
        } else if (event.getCode() == KeyCode.F5) {
            refrescar(null);
            event.consume();
        }
    }

    // ==================== ACCIONES ====================

    @FXML
    private void nuevo(ActionEvent event) {
        abrirFormulario(null);
    }

    @FXML
    private void editar(ActionEvent event) {
        ConfigSunat seleccionado = tblConfiguraciones.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            Mensaje.alerta(null, "Selección Requerida",
                    "Debe seleccionar una configuración para editar.");
            return;
        }

        abrirFormulario(seleccionado);
    }

    @FXML
    private void eliminar(ActionEvent event) {
        ConfigSunat seleccionado = tblConfiguraciones.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            Mensaje.alerta(null, "Selección Requerida",
                    "Debe seleccionar una configuración para eliminar.");
            return;
        }

        Optional<ButtonType> resultado = Mensaje.confirmacion(null,
                "Confirmar Eliminación",
                "¿Está seguro de eliminar la configuración:\n" +
                        seleccionado.getCodigo() + " - " + seleccionado.getDescripcion() + "?");

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (configSunatDao.eliminar(NO_CIA, seleccionado.getCodigo())) {
                cargarDatos();
            }
        }
    }

    @FXML
    private void refrescar(ActionEvent event) {
        cargarDatos();
       // Mensaje.toast("Datos actualizados");
    }

    /**
     * Abre el formulario de configuración
     */
    private void abrirFormulario(ConfigSunat config) {
        try {

            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/robin/pos/fxml/formConfSunat.fxml"));
            Parent root = loader.load();

            FormConfSunatController controller = loader.getController();

            if (config != null) {
                controller.setConfiguracion(config);
            }

            controller.setOnGuardado(() -> cargarDatos());

            Stage stage = new Stage();
            stage.setTitle(config == null ? "Nueva Configuración" : "Editar Configuración");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al abrir formulario", e);
            Mensaje.error(null, "Error", "No se pudo abrir el formulario:\n" + e.getMessage());
        }
    }
}
