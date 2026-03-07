package com.robin.pos.controller;

import com.robin.pos.MainApp;
import com.robin.pos.dao.ArfadocDao;
import com.robin.pos.model.Arfact;
import com.robin.pos.model.Arfadoc;
import com.robin.pos.util.Mensaje;
import com.robin.pos.util.Metodos;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.util.StringConverter;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para la gestión de documentos (ARFADOC)
 * Permite listar, buscar, filtrar y realizar operaciones CRUD
 *
 * @author Robin POS
 * @version 1.0
 */
public class ListaDocumentoController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ListaDocumentoController.class.getName());
    private static final String NO_CIA = "01"; // Código de compañía

    // ==================== CAMPOS FXML ====================

    @FXML private TextField txtBuscar;
    //@FXML private ComboBox<String> cbxTipo;
    @FXML private ComboBox<Arfact> cbxTipo;
    @FXML private ComboBox<String> cbxEstado;

    @FXML private TableView<Arfadoc> tblDocumentos;
    @FXML private TableColumn<Arfadoc, String> colCodDoc;
    @FXML private TableColumn<Arfadoc, String> colDescripcion;
    @FXML private TableColumn<Arfadoc, String> colTipo;
    @FXML private TableColumn<Arfadoc, String> colCodSunat;
    @FXML private TableColumn<Arfadoc, String> colEstado;

    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnActualizar;

    @FXML private Label lblContador;

    // ==================== VARIABLES DE INSTANCIA ====================

    private final ArfadocDao arfadocDao = new ArfadocDao();
    private ObservableList<Arfadoc> listaDocumentos = FXCollections.observableArrayList();
    private ObservableList<Arfadoc> listaFiltrada = FXCollections.observableArrayList();

    // ==================== INITIALIZE ====================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Inicializando ListaDocumentoController");

        // Configurar tabla
        configurarTabla();

        // Configurar ComboBoxes
        configurarComboBoxes();

        // Cargar datos iniciales
        cargarDocumentos();

        // Configurar listeners
        configurarListeners();

        LOGGER.info("ListaDocumentoController inicializado correctamente");
    }

    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        colCodDoc.setCellValueFactory(new PropertyValueFactory<>("codDoc"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colCodSunat.setCellValueFactory(new PropertyValueFactory<>("codSunat"));

        // Columna de estado con descripción
        colEstado.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getEstadoDescripcion()
                )
        );

        tblDocumentos.setItems(listaFiltrada);

        // Placeholder cuando no hay datos
        tblDocumentos.setPlaceholder(new Label("No hay documentos registrados"));
    }

    /**
     * Configura los ComboBoxes de filtro
     */
    private void configurarComboBoxes() {
        // Tipo de documento
        cargarTipoDocumento();

        // Estado
        cbxEstado.getItems().addAll("TODOS", "ACTIVO", "INACTIVO");
        cbxEstado.setValue("TODOS");
    }

    private void cargarTipoDocumento() {
        this.cbxTipo.getItems().setAll(Metodos.getArfacts());

        cbxTipo.setConverter(new StringConverter<Arfact>() {
            @Override
            public String toString(Arfact arfact) {
                return arfact != null ? arfact.getDescripcion() : "";
            }

            @Override
            public Arfact fromString(String s) {
                return cbxTipo.getItems().stream()
                        .filter(d -> d.getDescripcion().equals(s))
                        .findFirst().orElse(null);
            }
        });

        cbxTipo.setCellFactory(lv -> new ListCell<Arfact>() {
            @Override
            protected void updateItem(Arfact item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDescripcion());
            }
        });
    }

    /**
     * Configura listeners para la tabla
     */
    private void configurarListeners() {
        // Habilitar/deshabilitar botones según selección
        tblDocumentos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean haySeleccion = newVal != null;
            btnEditar.setDisable(!haySeleccion);
            btnEliminar.setDisable(!haySeleccion);
        });
    }

    // ==================== CARGAR DATOS ====================

    /**
     * Carga todos los documentos desde la base de datos
     */
    private void cargarDocumentos() {
        LOGGER.info("Cargando documentos de la base de datos");

        try {
            List<Arfadoc> documentos = arfadocDao.listarDocumentos(NO_CIA);
            listaDocumentos.clear();
            listaDocumentos.addAll(documentos);

            aplicarFiltros();
            actualizarContador();

            LOGGER.info("Se cargaron " + documentos.size() + " documentos");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar documentos", e);
            Mensaje.error(null, "Error al Cargar",
                    "No se pudieron cargar los documentos.\n" + e.getMessage());
        }
    }

    // ==================== BÚSQUEDA Y FILTROS ====================

    /**
     * Busca documentos por texto ingresado
     */
    @FXML
    private void buscarDocumento(KeyEvent event) {
        aplicarFiltros();
    }

    /**
     * Filtra por tipo de documento
     */
    @FXML
    private void filtrarPorTipo(ActionEvent event) {
        aplicarFiltros();
    }

    /**
     * Filtra por estado
     */
    @FXML
    private void filtrarPorEstado(ActionEvent event) {
        aplicarFiltros();
    }

    /**
     * Aplica todos los filtros activos
     */
    private void aplicarFiltros() {
        String textoBusqueda = txtBuscar.getText().toLowerCase().trim();
        //String tipo = cbxTipo.getValue() != null ? cbxTipo.getValue().getTipo() : "TODOS";
        String tipoSeleccionado = cbxTipo.getValue() != null ? cbxTipo.getValue().getTipo() : "TODOS";
        String estadoSeleccionado = cbxEstado.getValue();

        listaFiltrada.clear();

        for (Arfadoc doc : listaDocumentos) {
            boolean coincideTexto = true;
            boolean coincideTipo = true;
            boolean coincideEstado = true;

            // Filtro de texto
            if (!textoBusqueda.isEmpty()) {
                coincideTexto = doc.getCodDoc().toLowerCase().contains(textoBusqueda) ||
                        doc.getDescripcion().toLowerCase().contains(textoBusqueda) ||
                        (doc.getCodSunat() != null && doc.getCodSunat().toLowerCase().contains(textoBusqueda));
            }

            // Filtro de tipo
            if (!"TODOS".equals(tipoSeleccionado)) {
                String tipoDoc = doc.getTipo() != null ? doc.getTipo().toUpperCase() : "";
                coincideTipo = tipoDoc.contains(tipoSeleccionado.substring(0, 1));
            }

            // Filtro de estado
            if (!"TODOS".equals(estadoSeleccionado)) {
                String estadoDoc = doc.getEstado() != null ? doc.getEstado() : "";
                coincideEstado = ("ACTIVO".equals(estadoSeleccionado) && "A".equals(estadoDoc)) ||
                        ("INACTIVO".equals(estadoSeleccionado) && "I".equals(estadoDoc));
            }

            if (coincideTexto && coincideTipo && coincideEstado) {
                listaFiltrada.add(doc);
            }
        }

        actualizarContador();
    }

    /**
     * Actualiza el contador de registros
     */
    private void actualizarContador() {
        int total = listaFiltrada.size();
        lblContador.setText(total + (total == 1 ? " Documento" : " Documentos"));
    }

    // ==================== OPERACIONES CRUD ====================

    /**
     * Abre el formulario para crear un nuevo documento
     */
    @FXML
    private void abrirFormularioNuevo(ActionEvent event) {
        LOGGER.info("Abriendo formulario para nuevo documento");
        abrirFormulario(null, "Nuevo Documento");
    }

    /**
     * Abre el formulario para editar el documento seleccionado
     */
    @FXML
    private void abrirFormularioEditar(ActionEvent event) {
        Arfadoc seleccionado = tblDocumentos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            Mensaje.alerta(null, "Sin Selección",
                    "Por favor, seleccione un documento para editar.");
            return;
        }

        LOGGER.info("Abriendo formulario para editar: " + seleccionado.getCodDoc());
        abrirFormulario(seleccionado, "Editar Documento");
    }

    /**
     * Elimina el documento seleccionado
     */
    @FXML
    private void eliminarDocumento(ActionEvent event) {
        Arfadoc seleccionado = tblDocumentos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            Mensaje.alerta(null, "Sin Selección",
                    "Por favor, seleccione un documento para eliminar.");
            return;
        }

        // Confirmación
        Optional<ButtonType> resultado = Mensaje.confirmacion(null,
                "Confirmar Eliminación",
                "¿Está seguro de eliminar el documento?\n\n" +
                        "Código: " + seleccionado.getCodDoc() + "\n" +
                        "Descripción: " + seleccionado.getDescripcion() + "\n\n" +
                        "Esta acción no se puede deshacer.");

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            LOGGER.info("Eliminando documento: " + seleccionado.getCodDoc());

            boolean eliminado = arfadocDao.eliminar(seleccionado.getNoCia(), seleccionado.getCodDoc());

            if (eliminado) {
                cargarDocumentos();
            }
        }
    }

    /**
     * Actualiza la lista de documentos
     */
    @FXML
    private void actualizarLista(ActionEvent event) {
        LOGGER.info("Actualizando lista de documentos");
        cargarDocumentos();
    }

    // ==================== FORMULARIO MODAL ====================

    /**
     * Abre el formulario modal para crear o editar un documento
     *
     * @param documento Documento a editar (null para nuevo)
     * @param titulo Título de la ventana
     */
    private void abrirFormulario(Arfadoc documento, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/robin/pos/fxml/formDocumento.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del formulario
            FormDocumentoController controller = loader.getController();
            controller.setNoCia(NO_CIA);

            // Si es edición, cargar datos
            if (documento != null) {
                controller.cargarDatos(documento);
            }

            // Crear y configurar la ventana modal
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setResizable(false);
            stage.setScene(new Scene(root));

            // Callback para cuando se guarde
            controller.setOnGuardado(() -> {
                cargarDocumentos();
                stage.close();
            });

            // Callback para cuando se cancele
            controller.setOnCancelado(() -> {
                stage.close();
            });

            // Centrar en la pantalla
            stage.centerOnScreen();

            // Mostrar y esperar
            stage.showAndWait();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al abrir formulario", e);
            Mensaje.error(null, "Error",
                    "No se pudo abrir el formulario.\n" + e.getMessage());
        }
    }

    // ==================== EVENTOS DE TECLADO Y MOUSE ====================

    /**
     * Maneja el doble clic en la tabla para editar
     */
    @FXML
    private void manejarDobleClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Arfadoc seleccionado = tblDocumentos.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                abrirFormulario(seleccionado, "Editar Documento");
            }
        }
    }

    /**
     * Maneja los atajos de teclado
     */
    @FXML
    private void manejarTeclas(KeyEvent event) {
        Arfadoc seleccionado = tblDocumentos.getSelectionModel().getSelectedItem();

        // Ctrl+N: Nuevo
        if (event.getCode() == KeyCode.N && event.isControlDown()) {
            abrirFormularioNuevo(null);
            event.consume();
        }
        // Ctrl+E: Editar
        else if (event.getCode() == KeyCode.E && event.isControlDown() && seleccionado != null) {
            abrirFormularioEditar(null);
            event.consume();
        }
        // Delete: Eliminar
        else if (event.getCode() == KeyCode.DELETE && seleccionado != null) {
            eliminarDocumento(null);
            event.consume();
        }
        // F5: Actualizar
        else if (event.getCode() == KeyCode.F5) {
            actualizarLista(null);
            event.consume();
        }
    }
}