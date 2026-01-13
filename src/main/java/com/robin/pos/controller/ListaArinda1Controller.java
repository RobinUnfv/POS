package com.robin.pos.controller;

import com.robin.pos.dao.Arinda1Dao;
import com.robin.pos.model.Arinda1;
import com.robin.pos.util.Mensaje;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para la Lista de Artículos
 * Vista: ListaArinda1.fxml
 *
 * @author Robin POS
 * @version 1.0
 */
public class ListaArinda1Controller implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ListaArinda1Controller.class.getName());

    // === COMPONENTES FXML ===
    @FXML private VBox vbxPrincipal;
    @FXML private TableView<Arinda1> tblArticulos;
    @FXML private TextField txtBuscar;

    // Columnas
    @FXML private TableColumn<Arinda1, String> colCodigo;
    @FXML private TableColumn<Arinda1, String> colDescripcion;
    //@FXML private TableColumn<Arinda1, String> colTipo;
    @FXML private TableColumn<Arinda1, String> colMedida;
    @FXML private TableColumn<Arinda1, String> colMoneda;
    @FXML private TableColumn<Arinda1, String> colCosto;
    @FXML private TableColumn<Arinda1, String> colEstado;
    //@FXML private TableColumn<Arinda1, Void> colAcciones;

    // Filtros
    @FXML private ComboBox<String> cbxTipoArticulo;
    @FXML private ComboBox<String> cbxEstado;

    // Labels
    @FXML private Label lblSubtitulo;
    @FXML private Label lblTotalArticulos;
    @FXML private Label lblArticulosVigentes;
    @FXML private Label lblContador;
    @FXML private Label lblEstado;

    // Botones
    @FXML private Button btnNuevo;
    @FXML private Button btnActualizar;

    // === DATOS ===
    private final ObservableList<Arinda1> listaArticulos = FXCollections.observableArrayList();
    private FilteredList<Arinda1> filteredData;
    private SortedList<Arinda1> sortedData;

    // === CONSTANTES ===
    private static final String NO_CIA = "01";
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");

    // Tipos de artículo
    private static final String TIPO_TODOS = "Todos";
    private static final String TIPO_VENTAS = "VENTAS";

    // Estados
    private static final String ESTADO_TODOS = "Todos";
    private static final String ESTADO_VIGENTE = "Vigente";
    private static final String ESTADO_NO_VIGENTE = "No Vigente";

    // Stage para indicador de carga
    private Stage loadingStage;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarComboTipos();
        configurarComboEstados();
        configurarColumnas();
        configurarFiltro();
        configurarTabla();
        configurarAtajosTeclado();
        cargarArticulos();
    }

    /**
     * Configura el ComboBox de tipos de artículo
     */
    private void configurarComboTipos() {
        cbxTipoArticulo.getItems().addAll(
                TIPO_TODOS,
                TIPO_VENTAS
        );
        cbxTipoArticulo.setValue(TIPO_TODOS);
    }

    /**
     * Configura el ComboBox de estados
     */
    private void configurarComboEstados() {
        cbxEstado.getItems().addAll(
                ESTADO_TODOS,
                ESTADO_VIGENTE,
                ESTADO_NO_VIGENTE
        );
        cbxEstado.setValue(ESTADO_TODOS);
    }

    /**
     * Configura las columnas de la tabla
     */
    private void configurarColumnas() {
        // Código
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colCodigo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-family: 'Consolas'; -fx-text-fill: #000005; -fx-font-weight: bold;");
                }
            }
        });

        // Descripción
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));


        // Medida
        colMedida.setCellValueFactory(new PropertyValueFactory<>("medida"));
        colMedida.setStyle("-fx-alignment: CENTER;");

        // Moneda
        colMoneda.setCellValueFactory(cellData -> {
            String moneda = cellData.getValue().getMoneda();
            String simbolo = "SOL".equals(moneda) ? "S/" : "$";
            return new SimpleStringProperty(simbolo);
        });
        colMoneda.setStyle("-fx-alignment: CENTER;");

        // Costo
        colCosto.setCellValueFactory(cellData -> {
            BigDecimal costo = cellData.getValue().getCostoUni();
            String valor = costo != null ? DECIMAL_FORMAT.format(costo) : "0.00";
            return new SimpleStringProperty(valor);
        });
        colCosto.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-family: 'Consolas'; -fx-font-weight: bold;");
                }
            }
        });

        // Estado
        colEstado.setCellValueFactory(new PropertyValueFactory<>("vigente"));
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = crearBadgeEstado(item);
                    HBox container = new HBox(badge);
                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                    setText(null);
                }
            }
        });


    }

    /**
     * Crea un badge visual para el estado
     */
    private Label crearBadgeEstado(String vigente) {
        Label badge = new Label();
        badge.setAlignment(Pos.CENTER);

        if ("S".equals(vigente)) {
            badge.setText("Vigente");
            badge.setStyle("-fx-background-color: #dcfce7; -fx-background-radius: 12; " +
                    "-fx-padding: 4 10; -fx-text-fill: #166534; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else {
            badge.setText("Inactivo");
            badge.setStyle("-fx-background-color: #fee2e2; -fx-background-radius: 12; " +
                    "-fx-padding: 4 10; -fx-text-fill: #991b1b; -fx-font-size: 11px; -fx-font-weight: bold;");
        }

        return badge;
    }

    /**
     * Obtiene la descripción del tipo de artículo
     */
    private String obtenerDescripcionTipo(String tipo) {
        if (tipo == null) return "";
        return switch (tipo) {
            case "PT" -> "Producto";
            case "MP" -> "Mat. Prima";
            case "SU" -> "Suministro";
            case "EN" -> "Envase";
            case "SE" -> "Servicio";
            default -> tipo;
        };
    }

    /**
     * Configura el filtro de búsqueda
     */
    private void configurarFiltro() {
        filteredData = new FilteredList<>(listaArticulos, p -> true);

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            aplicarFiltros();
        });

        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tblArticulos.comparatorProperty());
        tblArticulos.setItems(sortedData);
    }

    /**
     * Aplica todos los filtros (búsqueda, tipo, estado)
     */
    private void aplicarFiltros() {
        String busqueda = txtBuscar.getText();
        String tipoSeleccionado = cbxTipoArticulo.getValue();
        String estadoSeleccionado = cbxEstado.getValue();

        filteredData.setPredicate(articulo -> {
            // Filtro de búsqueda
            boolean coincideBusqueda = true;
            if (busqueda != null && !busqueda.isEmpty()) {
                String lowerCaseFilter = busqueda.toLowerCase();

                if (articulo.getCodigo() != null &&
                        articulo.getCodigo().toLowerCase().contains(lowerCaseFilter)) {
                    coincideBusqueda = true;
                } else if (articulo.getDescripcion() != null &&
                        articulo.getDescripcion().toLowerCase().contains(lowerCaseFilter)) {
                    coincideBusqueda = true;
                } else {
                    coincideBusqueda = false;
                }
            }

            // Filtro de tipo
            boolean coincideTipo = true;
            if (tipoSeleccionado != null && !TIPO_TODOS.equals(tipoSeleccionado)) {
                String codigoTipo = tipoSeleccionado.split(" - ")[0];
                coincideTipo = codigoTipo.equals(articulo.getTipoArti());
            }

            // Filtro de estado
            boolean coincideEstado = true;
            if (estadoSeleccionado != null && !ESTADO_TODOS.equals(estadoSeleccionado)) {
                if (ESTADO_VIGENTE.equals(estadoSeleccionado)) {
                    coincideEstado = "S".equals(articulo.getVigente());
                } else if (ESTADO_NO_VIGENTE.equals(estadoSeleccionado)) {
                    coincideEstado = !"S".equals(articulo.getVigente());
                }
            }

            return coincideBusqueda && coincideTipo && coincideEstado;
        });

        actualizarContador();
    }

    /**
     * Configura la tabla
     */
    private void configurarTabla() {
        tblArticulos.setOnMouseClicked(this::handleTableClick);

        // Placeholder cuando no hay datos
        Label placeholder = new Label("No se encontraron artículos");
        placeholder.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");
        tblArticulos.setPlaceholder(placeholder);
    }

    /**
     * Configura los atajos de teclado
     */
    private void configurarAtajosTeclado() {
        Platform.runLater(() -> {
            if (vbxPrincipal.getScene() != null) {
                vbxPrincipal.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.F5) {
                        refrescarArticulo(null);
                        event.consume();
                    } else if (event.getCode() == KeyCode.F2) {
                        btnEditarArticulo(null);
                        event.consume();
                    } else if (event.isControlDown() && event.getCode() == KeyCode.F) {
                        txtBuscar.requestFocus();
                        event.consume();
                    }
                });
            }
        });
    }

    /**
     * Carga los artículos desde la base de datos
     */
    private void cargarArticulos() {
        actualizarEstado("Cargando artículos...");

        // Mostrar indicador de carga en la tabla
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(50, 50);
        VBox placeholderBox = new VBox(10);
        placeholderBox.setAlignment(Pos.CENTER);
        placeholderBox.getChildren().addAll(progress, new Label("Cargando artículos..."));
        tblArticulos.setPlaceholder(placeholderBox);

        Task<List<Arinda1>> task = new Task<>() {
            @Override
            protected List<Arinda1> call() throws Exception {
                Arinda1Dao dao = new Arinda1Dao();
                return dao.listarTodos(NO_CIA);
            }
        };

        task.setOnSucceeded(event -> {
            listaArticulos.setAll(task.getValue());
            actualizarContador();
            actualizarEstadisticas();
            actualizarEstado("Listo");
            restaurarPlaceholder();
        });

        task.setOnFailed(event -> {
            actualizarEstado("Error al cargar datos");
            restaurarPlaceholder();
            Throwable ex = task.getException();
            if (ex != null) {
                LOGGER.log(Level.SEVERE, "Error al cargar artículos", ex);
                Mensaje.error(null, "Error de conexión",
                        "No se pudieron cargar los artículos: " + ex.getMessage());
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // === ACCIONES ===

    @FXML
    void refrescarArticulo(ActionEvent event) {

        txtBuscar.clear();
        cbxTipoArticulo.setValue(TIPO_TODOS);
        cbxEstado.setValue(ESTADO_TODOS);
        cargarArticulos();

    }

    @FXML
    void filtrarPorTipo(ActionEvent event) {
        aplicarFiltros();
    }

    @FXML
    void filtrarPorEstado(ActionEvent event) {
        aplicarFiltros();
    }

    @FXML
    void buscarArticulo(KeyEvent event) {
        if (event.getCode() == KeyCode.DOWN) {
            tblArticulos.requestFocus();
            if (!tblArticulos.getItems().isEmpty()) {
                tblArticulos.getSelectionModel().selectFirst();
            }
        } else if (event.getCode() == KeyCode.ESCAPE) {
            txtBuscar.clear();
        }
    }

    @FXML
    void btnEditarArticulo(ActionEvent event) {
        Arinda1 seleccionado = tblArticulos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            editarArticulo(seleccionado);
        } else {
            Mensaje.alerta(null, "Editar Artículo", "Seleccione un artículo para editar.");
        }
    }

    /**
     * Edita un artículo existente
     */
    private void editarArticulo(Arinda1 articulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/robin/pos/fxml/Arinda1.fxml"));
            Parent root = loader.load();

            // Obtener el controlador y cargar el artículo
            Arinda1Controller controller = loader.getController();
            controller.cargarArticulo(articulo);
            controller.setModoEdicion(true);

            Stage stage = new Stage();
            stage.setTitle("Editar Artículo - " + articulo.getCodigo());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(vbxPrincipal.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            stage.setOnHidden(e -> cargarArticulos());

            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al abrir formulario de edición", e);
            Mensaje.error(null, "Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    /**
     * Muestra el detalle de un artículo
     */
    private void verDetalleArticulo(Arinda1 articulo) {
        StringBuilder detalle = new StringBuilder();
        detalle.append("CÓDIGO: ").append(articulo.getCodigo()).append("\n\n");
        detalle.append("DESCRIPCIÓN:\n").append(articulo.getDescripcion()).append("\n\n");
        detalle.append("TIPO: ").append(obtenerDescripcionTipo(articulo.getTipoArti())).append("\n");
        detalle.append("UNIDAD DE MEDIDA: ").append(articulo.getMedida()).append("\n");
        detalle.append("MONEDA: ").append("SOL".equals(articulo.getMoneda()) ? "SOLES" : "DÓLARES").append("\n");
        detalle.append("COSTO UNITARIO: ").append(DECIMAL_FORMAT.format(articulo.getCostoUni())).append("\n\n");
        detalle.append("STOCK MÍNIMO: ").append(articulo.getStkMinimo()).append("\n");
        detalle.append("STOCK MÁXIMO: ").append(articulo.getStkMaximo()).append("\n\n");
        detalle.append("ESTADO: ").append("S".equals(articulo.getVigente()) ? "Vigente" : "Inactivo");

        Mensaje.alerta (null, "Detalle del Artículo", detalle.toString());
    }

    // === MÉTODOS AUXILIARES ===

    private void handleTableClick(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            Arinda1 seleccionado = tblArticulos.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                editarArticulo(seleccionado);
            }
        }
    }

    private void actualizarContador() {
        int total = listaArticulos.size();
        int filtrados = filteredData.size();

        if (total == filtrados) {
            lblContador.setText(total + " artículo" + (total != 1 ? "s" : ""));
        } else {
            lblContador.setText(filtrados + " de " + total + " artículos");
        }
    }

    private void actualizarEstadisticas() {
        int total = listaArticulos.size();
        long vigentes = listaArticulos.stream()
                .filter(a -> "S".equals(a.getVigente()))
                .count();

        lblTotalArticulos.setText(String.valueOf(total));
        lblArticulosVigentes.setText(String.valueOf(vigentes));
    }

    private void actualizarEstado(String mensaje) {
        Platform.runLater(() -> {
            if (lblEstado != null) {
                lblEstado.setText(mensaje);
            }
        });
    }

    private void restaurarPlaceholder() {
        Label placeholder = new Label("No se encontraron artículos");
        placeholder.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");
        tblArticulos.setPlaceholder(placeholder);
    }

    // === MÉTODOS PÚBLICOS ===

    /**
     * Permite refrescar desde otro controlador
     */
    public void refresh() {
        cargarArticulos();
    }

    /**
     * Obtiene el artículo seleccionado
     */
    public Arinda1 getArticuloSeleccionado() {
        return tblArticulos.getSelectionModel().getSelectedItem();
    }
}
