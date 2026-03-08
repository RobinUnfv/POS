package com.robin.pos.controller;

import com.robin.pos.MainApp;
import com.robin.pos.dao.ArfaccDao;
import com.robin.pos.dao.ArfadocDao;
import com.robin.pos.model.Arfacc;
import com.robin.pos.model.Arfact;
import com.robin.pos.model.Arfadoc;
import com.robin.pos.util.Mensaje;
import com.robin.pos.util.Metodos;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para la gestión de series de documentos (ARFACC)
 * Permite listar, buscar, filtrar y realizar operaciones CRUD
 *
 * @author Robin POS
 * @version 1.0
 */
public class ListaSerieDocumentoController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ListaSerieDocumentoController.class.getName());
    private static final String NO_CIA = "01"; // Código de compañía
    private static final String CENTRO = "41"; // Centro por defecto

    // ==================== CAMPOS FXML ====================

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbxCentro;
    @FXML private ComboBox<Arfadoc> cbxTipoDoc;
    @FXML private ComboBox<String> cbxEstado;

    @FXML private TableView<Arfacc> tblSeries;
    @FXML private TableColumn<Arfacc, String> colCentro;
    @FXML private TableColumn<Arfacc, String> colTipoDoc;
    @FXML private TableColumn<Arfacc, String> colSerie;
    @FXML private TableColumn<Arfacc, Integer> colConsDesde;
    @FXML private TableColumn<Arfacc, Integer> colLineas;
    @FXML private TableColumn<Arfacc, String> colControlAuto;
    @FXML private TableColumn<Arfacc, String> colActivo;
    @FXML private TableColumn<Arfacc, String> colNoCaba;

    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnActualizar;

    @FXML private Label lblContador;

    // ==================== VARIABLES DE INSTANCIA ====================

    private final ArfaccDao arfaccDao = new ArfaccDao();
    private ObservableList<Arfacc> listaSeries = FXCollections.observableArrayList();
    private ObservableList<Arfacc> listaFiltrada = FXCollections.observableArrayList();

    // ==================== INITIALIZE ====================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Inicializando ListaSerieDocumentoController");

        // Configurar tabla
        configurarTabla();

        // Configurar ComboBoxes
        configurarComboBoxes();

        // Cargar datos iniciales
        cargarSeries();

        // Configurar listeners
        configurarListeners();

        LOGGER.info("ListaSerieDocumentoController inicializado correctamente");
    }

    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        colCentro.setCellValueFactory(new PropertyValueFactory<>("centro"));
        //colTipoDoc.setCellValueFactory(new PropertyValueFactory<>("tipoDoc"));
        colTipoDoc.setCellValueFactory(new PropertyValueFactory<>("descripcion")); // Mostrar descripción del tipo de documento
        colSerie.setCellValueFactory(new PropertyValueFactory<>("serie"));
        colConsDesde.setCellValueFactory(new PropertyValueFactory<>("consDesde"));
        colLineas.setCellValueFactory(new PropertyValueFactory<>("lineas"));

        // Columna control automático con descripción
        colControlAuto.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getControlAutoDescripcion()
                )
        );

        // Columna activo con descripción
        colActivo.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getActivoDescripcion()
                )
        );

        colNoCaba.setCellValueFactory(new PropertyValueFactory<>("noCaba"));

        tblSeries.setItems(listaFiltrada);

        // Placeholder cuando no hay datos
        tblSeries.setPlaceholder(new Label("No hay series registradas"));
    }

    /**
     * Configura los ComboBoxes de filtro
     */
    private void configurarComboBoxes() {
        // Centro
        cbxCentro.getItems().addAll("TODOS", "41", "42", "43");
        cbxCentro.setValue("TODOS");

        // Tipo de documento
        /*
        cbxTipoDoc.getItems().addAll("TODOS", "F", "B", "N");
        cbxTipoDoc.setValue("TODOS");
        */
        cargarTipoDocumento();;

        // Estado
        cbxEstado.getItems().addAll("TODOS", "ACTIVO", "INACTIVO");
        cbxEstado.setValue("TODOS");
    }

    private void cargarTipoDocumento() {
        ArfadocDao arfadocDao = new ArfadocDao();
        //List<Arfadoc> arfadocs = arfadocDao.listarDocumentos(NO_CIA);
        cbxTipoDoc.getItems().setAll(arfadocDao.listarDocumentos(NO_CIA));

        cbxTipoDoc.setConverter(new StringConverter<Arfadoc>() {
            @Override
            public String toString(Arfadoc arfadoc) {
                return arfadoc != null ? arfadoc.getDescripcion() : "";
            }

            @Override
            public Arfadoc fromString(String s) {
                return cbxTipoDoc.getItems().stream()
                        .filter(d -> d.getDescripcion().equals(s))
                        .findFirst().orElse(null);
            }
        });

        cbxTipoDoc.setCellFactory(lv -> new ListCell<Arfadoc>() {
            @Override
            protected void updateItem(Arfadoc item, boolean empty) {
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
        tblSeries.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean haySeleccion = newVal != null;
            btnEditar.setDisable(!haySeleccion);
            btnEliminar.setDisable(!haySeleccion);
        });
    }

    // ==================== CARGAR DATOS ====================

    /**
     * Carga todas las series desde la base de datos
     */
    private void cargarSeries() {
        LOGGER.info("Cargando series de la base de datos");

        try {
            List<Arfacc> series = arfaccDao.listarTodasSeries(NO_CIA);
            listaSeries.clear();
            listaSeries.addAll(series);

            aplicarFiltros();
            actualizarContador();

            LOGGER.info("Se cargaron " + series.size() + " series");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar series", e);
            Mensaje.error(null, "Error al Cargar",
                    "No se pudieron cargar las series.\n" + e.getMessage());
        }
    }

    // ==================== BÚSQUEDA Y FILTROS ====================

    /**
     * Busca series por texto ingresado
     */
    @FXML
    private void buscarSerie(KeyEvent event) {
        aplicarFiltros();
    }

    /**
     * Filtra por centro
     */
    @FXML
    private void filtrarPorCentro(ActionEvent event) {
        aplicarFiltros();
    }

    /**
     * Filtra por tipo de documento
     */
    @FXML
    private void filtrarPorTipoDoc(ActionEvent event) {
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
        String centroSeleccionado = cbxCentro.getValue();
        String tipoDocSeleccionado = cbxTipoDoc.getValue() != null ? cbxTipoDoc.getValue().getCodDoc() : "TODOS";
        String estadoSeleccionado = cbxEstado.getValue();

        listaFiltrada.clear();

        for (Arfacc serie : listaSeries) {
            boolean coincideTexto = true;
            boolean coincideCentro = true;
            boolean coincideTipoDoc = true;
            boolean coincideEstado = true;

            // Filtro de texto
            if (!textoBusqueda.isEmpty()) {
                coincideTexto = serie.getSerie().toLowerCase().contains(textoBusqueda) ||
                        serie.getCentro().toLowerCase().contains(textoBusqueda) ||
                        serie.getTipoDoc().toLowerCase().contains(textoBusqueda);
            }

            // Filtro de centro
            if (!"TODOS".equals(centroSeleccionado)) {
                coincideCentro = serie.getCentro().equals(centroSeleccionado);
            }

            // Filtro de tipo documento
            if (!"TODOS".equals(tipoDocSeleccionado)) {
                coincideTipoDoc = serie.getTipoDoc().equals(tipoDocSeleccionado);
            }

            // Filtro de estado
            if (!"TODOS".equals(estadoSeleccionado)) {
                String activo = serie.getActivo() != null ? serie.getActivo() : "";
                coincideEstado = ("ACTIVO".equals(estadoSeleccionado) && "S".equals(activo)) ||
                        ("INACTIVO".equals(estadoSeleccionado) && "N".equals(activo));
            }

            if (coincideTexto && coincideCentro && coincideTipoDoc && coincideEstado) {
                listaFiltrada.add(serie);
            }
        }

        actualizarContador();
    }

    /**
     * Actualiza el contador de registros
     */
    private void actualizarContador() {
        int total = listaFiltrada.size();
        lblContador.setText(total + (total == 1 ? " Serie" : " Series"));
    }

    // ==================== OPERACIONES CRUD ====================

    /**
     * Abre el formulario para crear una nueva serie
     */
    @FXML
    private void abrirFormularioNuevo(ActionEvent event) {
        LOGGER.info("Abriendo formulario para nueva serie");
        abrirFormulario(null, "Nueva Serie de Documento");
    }

    /**
     * Abre el formulario para editar la serie seleccionada
     */
    @FXML
    private void abrirFormularioEditar(ActionEvent event) {
        Arfacc seleccionado = tblSeries.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            Mensaje.alerta(null, "Sin Selección",
                    "Por favor, seleccione una serie para editar.");
            return;
        }

        LOGGER.info("Abriendo formulario para editar: " + seleccionado.getSerie());
        abrirFormulario(seleccionado, "Editar Serie de Documento");
    }

    /**
     * Elimina la serie seleccionada
     */
    @FXML
    private void eliminarSerie(ActionEvent event) {
        Arfacc seleccionado = tblSeries.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            Mensaje.alerta(null, "Sin Selección",
                    "Por favor, seleccione una serie para eliminar.");
            return;
        }

        // Confirmación
        Optional<ButtonType> resultado = Mensaje.confirmacion(null,
                "Confirmar Eliminación",
                "¿Está seguro de eliminar la serie?\n\n" +
                        "Centro: " + seleccionado.getCentro() + "\n" +
                        "Tipo Doc: " + seleccionado.getTipoDoc() + "\n" +
                        "Serie: " + seleccionado.getSerie() + "\n\n" +
                        "Esta acción no se puede deshacer.");

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            LOGGER.info("Eliminando serie: " + seleccionado.getSerie());

            boolean eliminado = arfaccDao.eliminar(
                    seleccionado.getNoCia(),
                    seleccionado.getCentro(),
                    seleccionado.getTipoDoc(),
                    seleccionado.getSerie()
            );

            if (eliminado) {
                cargarSeries();
            }
        }
    }

    /**
     * Actualiza la lista de series
     */
    @FXML
    private void actualizarLista(ActionEvent event) {
        LOGGER.info("Actualizando lista de series");
        cargarSeries();
    }

    // ==================== FORMULARIO MODAL ====================

    /**
     * Abre el formulario modal para crear o editar una serie
     *
     * @param serie Serie a editar (null para nuevo)
     * @param titulo Título de la ventana
     */
    private void abrirFormulario(Arfacc serie, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/robin/pos/fxml/formSerieDocumento.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del formulario
            FormSerieDocumentoController controller = loader.getController();
            controller.setNoCia(NO_CIA);

            // Si es edición, cargar datos
            if (serie != null) {
                controller.cargarDatos(serie);
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
                cargarSeries();
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
            Arfacc seleccionado = tblSeries.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                abrirFormulario(seleccionado, "Editar Serie de Documento");
            }
        }
    }

    /**
     * Maneja los atajos de teclado
     */
    @FXML
    private void manejarTeclas(KeyEvent event) {
        Arfacc seleccionado = tblSeries.getSelectionModel().getSelectedItem();

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
            eliminarSerie(null);
            event.consume();
        }
        // F5: Actualizar
        else if (event.getCode() == KeyCode.F5) {
            actualizarLista(null);
            event.consume();
        }
    }
}
