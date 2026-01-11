package com.robin.pos.controller;

import com.robin.pos.dao.ComprobantePagoDao;
import com.robin.pos.model.ComprobantePago;
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
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la Lista de Comprobantes de Pago
 *
 * @author Robin POS
 * @version 1.0
 */
public class ListaComprobantePagoController implements Initializable {

    // === COMPONENTES FXML ===
    @FXML private VBox vbxPrincipal;
    @FXML private TableView<ComprobantePago> tblComprobantes;
    @FXML private TextField txtBuscar;

    // Columnas
    @FXML private TableColumn<ComprobantePago, String> colNumero;
    @FXML private TableColumn<ComprobantePago, String> colFecha;
    @FXML private TableColumn<ComprobantePago, String> colCliente;
    @FXML private TableColumn<ComprobantePago, String> colDocumento;
    @FXML private TableColumn<ComprobantePago, String> colTipoDoc;
    @FXML private TableColumn<ComprobantePago, String> colMoneda;
    @FXML private TableColumn<ComprobantePago, String> colTotal;
    // @FXML private TableColumn<ComprobantePago, String> colEstado;

    // Filtros
    @FXML private ComboBox<String> cbxEstado;
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;

    // Labels
    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblContador;
    @FXML private Label lblEstado;

    // Botones
    @FXML private Button btnCopiaA4;
    @FXML private Button btnCopiaPOS;
    @FXML private Button btnActualizar;
    @FXML private Button btnBuscarFechas;

    // === DATOS ===
    private final ObservableList<ComprobantePago> listaComprobantes = FXCollections.observableArrayList();
    private FilteredList<ComprobantePago> filteredData;
    private SortedList<ComprobantePago> sortedData;

    // === CONSTANTES ===
    private static final String NO_CIA = "01";
    private static final String TIPO_DOC_FACTURA = "F";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Estados disponibles
    private static final String ESTADO_TODOS = "Todos";
    private static final String ESTADO_DESPACHADO = "Despachado";
    private static final String ESTADO_PENDIENTE = "Pendiente";
    private static final String ESTADO_ANULADO = "Anulado";
    private static final String ESTADO_PROCESANDO = "Procesando";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarComboEstado();
        configurarColumnas();
        configurarFiltro();
        configurarTabla();
        configurarFechas();
        configurarAtajosTeclado();
        cargarComprobantes();
    }

    /**
     * Configura el ComboBox de estados
     */
    private void configurarComboEstado() {
        cbxEstado.getItems().addAll(
                ESTADO_TODOS,
                ESTADO_DESPACHADO,
                ESTADO_PENDIENTE,
                ESTADO_ANULADO,
                ESTADO_PROCESANDO
        );
        cbxEstado.setValue(ESTADO_DESPACHADO);
    }

    /**
     * Configura las columnas de la tabla
     */
    private void configurarColumnas() {
        // Número de comprobante
        colNumero.setCellValueFactory(new PropertyValueFactory<>("noFactu"));
        colNumero.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-family: 'Consolas'; -fx-text-fill: #6366f1; -fx-font-weight: bold;");
                }
            }
        });

        // Fecha
        colFecha.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFecha();
            return new SimpleStringProperty(fecha != null ? fecha.format(DATE_FORMATTER) : "");
        });
        colFecha.setStyle("-fx-alignment: CENTER;");

        // Cliente
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nbrCliente"));

        // Documento del cliente
        colDocumento.setCellValueFactory(new PropertyValueFactory<>("numDocCli"));
        colDocumento.setStyle("-fx-alignment: CENTER;");

        // Tipo documento cliente
        colTipoDoc.setCellValueFactory(cellData -> {
            String tipo = cellData.getValue().getTipoDocCliDescripcion();
            return new SimpleStringProperty(tipo);
        });
        colTipoDoc.setStyle("-fx-alignment: CENTER;");

        // Moneda
        colMoneda.setCellValueFactory(cellData -> {
            String moneda = cellData.getValue().getMoneda();
            String simbolo = cellData.getValue().getSimboloMoneda();
            return new SimpleStringProperty(simbolo);
        });
        colMoneda.setStyle("-fx-alignment: CENTER;");

        // Total
        colTotal.setCellValueFactory(cellData -> {
            Double total = cellData.getValue().getTotal();
            return new SimpleStringProperty(String.format("%,.2f", total));
        });
        colTotal.setCellFactory(col -> new TableCell<>() {
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
        /*
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
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
        */
    }

    /**
     * Crea un badge visual para el estado
     */
    private Label crearBadgeEstado(String estado) {
        Label badge = new Label();
        badge.setAlignment(Pos.CENTER);

        String texto;
        String estilo;

        switch (estado != null ? estado.toUpperCase() : "") {
            case "D" -> {
                texto = "Despachado";
                estilo = "-fx-background-color: #dcfce7; -fx-background-radius: 12; " +
                        "-fx-padding: 4 10; -fx-text-fill: #166534; -fx-font-size: 10px; -fx-font-weight: bold;";
            }
            case "P" -> {
                texto = "Pendiente";
                estilo = "-fx-background-color: #fef3c7; -fx-background-radius: 12; " +
                        "-fx-padding: 4 10; -fx-text-fill: #92400e; -fx-font-size: 10px; -fx-font-weight: bold;";
            }
            case "A" -> {
                texto = "Anulado";
                estilo = "-fx-background-color: #fee2e2; -fx-background-radius: 12; " +
                        "-fx-padding: 4 10; -fx-text-fill: #991b1b; -fx-font-size: 10px; -fx-font-weight: bold;";
            }
            case "E" -> {
                texto = "Procesando";
                estilo = "-fx-background-color: #dbeafe; -fx-background-radius: 12; " +
                        "-fx-padding: 4 10; -fx-text-fill: #1e40af; -fx-font-size: 10px; -fx-font-weight: bold;";
            }
            default -> {
                texto = estado;
                estilo = "-fx-background-color: #f1f5f9; -fx-background-radius: 12; " +
                        "-fx-padding: 4 10; -fx-text-fill: #475569; -fx-font-size: 10px;";
            }
        }

        badge.setText(texto);
        badge.setStyle(estilo);
        return badge;
    }

    /**
     * Configura el filtro de búsqueda
     */
    private void configurarFiltro() {
        filteredData = new FilteredList<>(listaComprobantes, p -> true);

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tblComprobantes.comparatorProperty());
        tblComprobantes.setItems(sortedData);
    }

    /**
     * Aplica los filtros de búsqueda
     */
    private void aplicarFiltros() {
        String textoBusqueda = txtBuscar.getText();

        filteredData.setPredicate(comprobante -> {
            if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
                return true;
            }

            String filtro = textoBusqueda.toLowerCase().trim();

            // Buscar en número de factura
            if (comprobante.getNoFactu() != null &&
                    comprobante.getNoFactu().toLowerCase().contains(filtro)) {
                return true;
            }

            // Buscar en nombre del cliente
            if (comprobante.getNbrCliente() != null &&
                    comprobante.getNbrCliente().toLowerCase().contains(filtro)) {
                return true;
            }

            // Buscar en documento del cliente
            if (comprobante.getNumDocCli() != null &&
                    comprobante.getNumDocCli().toLowerCase().contains(filtro)) {
                return true;
            }

            return false;
        });

        actualizarContador();
    }

    /**
     * Configura la tabla
     */
    private void configurarTabla() {
        // Doble clic para ver detalle
        tblComprobantes.setOnMouseClicked(this::handleTableClick);

        // Placeholder
        Label placeholder = new Label("No se encontraron comprobantes");
        placeholder.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");
        tblComprobantes.setPlaceholder(placeholder);

        // Selección simple
        tblComprobantes.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Habilitar/deshabilitar botones según selección
        tblComprobantes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean haySeleccion = newVal != null;
            btnCopiaA4.setDisable(!haySeleccion);
            btnCopiaPOS.setDisable(!haySeleccion);
        });

        // Deshabilitar botones inicialmente
        btnCopiaA4.setDisable(true);
        btnCopiaPOS.setDisable(true);
    }

    /**
     * Configura las fechas por defecto
     */
    private void configurarFechas() {
        LocalDate hoy = LocalDate.now();
        dpFechaInicio.setValue(hoy.withDayOfMonth(1)); // Primer día del mes
        dpFechaFin.setValue(hoy); // Hoy
    }

    /**
     * Configura atajos de teclado
     */
    private void configurarAtajosTeclado() {
        Platform.runLater(() -> {
            if (vbxPrincipal.getScene() != null) {
                vbxPrincipal.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    switch (event.getCode()) {
                        case F5 -> {
                            actualizarLista(null);
                            event.consume();
                        }
                        case F6 -> {
                            if (!btnCopiaA4.isDisabled()) {
                                generarCopiaA4(null);
                            }
                            event.consume();
                        }
                        case F7 -> {
                            if (!btnCopiaPOS.isDisabled()) {
                                generarCopiaPOS(null);
                            }
                            event.consume();
                        }
                        case F -> {
                            if (event.isControlDown()) {
                                txtBuscar.requestFocus();
                                txtBuscar.selectAll();
                                event.consume();
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * Carga los comprobantes desde la base de datos
     */
    private void cargarComprobantes() {
        actualizarEstado("Cargando comprobantes...");

        // Mostrar indicador de carga
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(50, 50);
        tblComprobantes.setPlaceholder(progress);

        String estadoSeleccionado = obtenerCodigoEstado(cbxEstado.getValue());

        Task<List<ComprobantePago>> task = new Task<>() {
            @Override
            protected List<ComprobantePago> call() throws Exception {
                ComprobantePagoDao dao = new ComprobantePagoDao();
                LocalDate fechaInicio = dpFechaInicio.getValue();
                LocalDate fechaFin = dpFechaFin.getValue();

                return dao.listarPorFechas(NO_CIA, TIPO_DOC_FACTURA, estadoSeleccionado,
                        fechaInicio, fechaFin);
            }
        };

        task.setOnSucceeded(event -> {
            listaComprobantes.setAll(task.getValue());
            actualizarContador();
            actualizarEstado("Listo");
            restaurarPlaceholder();
        });

        task.setOnFailed(event -> {
            actualizarEstado("Error al cargar datos");
            restaurarPlaceholder();
            Throwable ex = task.getException();
            if (ex != null) {
                Mensaje.error(null, "Error de conexión",
                        "No se pudieron cargar los comprobantes: " + ex.getMessage());
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Obtiene el código de estado para la consulta
     */
    private String obtenerCodigoEstado(String estado) {
        if (estado == null) return "D";
        return switch (estado) {
            case ESTADO_DESPACHADO -> "D";
            case ESTADO_PENDIENTE -> "P";
            case ESTADO_ANULADO -> "A";
            case ESTADO_PROCESANDO -> "E";
            default -> "D";
        };
    }

    // === ACCIONES ===

    @FXML
    void actualizarLista(ActionEvent event) {
        txtBuscar.clear();
        cargarComprobantes();
    }

    @FXML
    void filtrarPorEstado(ActionEvent event) {
        cargarComprobantes();
    }

    @FXML
    void buscarPorFechas(ActionEvent event) {
        LocalDate fechaInicio = dpFechaInicio.getValue();
        LocalDate fechaFin = dpFechaFin.getValue();

        if (fechaInicio == null || fechaFin == null) {
            Mensaje.alerta(null, "Fechas requeridas",
                    "Debe seleccionar fecha de inicio y fin.");
            return;
        }

        if (fechaInicio.isAfter(fechaFin)) {
            Mensaje.alerta(null, "Fechas inválidas",
                    "La fecha de inicio no puede ser mayor a la fecha fin.");
            return;
        }

        cargarComprobantes();
    }

    @FXML
    void buscarComprobante(KeyEvent event) {
        if (event.getCode() == KeyCode.DOWN) {
            tblComprobantes.requestFocus();
            if (!tblComprobantes.getItems().isEmpty()) {
                tblComprobantes.getSelectionModel().selectFirst();
            }
        } else if (event.getCode() == KeyCode.ESCAPE) {
            txtBuscar.clear();
        }
    }

    @FXML
    void generarCopiaA4(ActionEvent event) {
        ComprobantePago seleccionado = tblComprobantes.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            Mensaje.alerta(null, "Selección requerida",
                    "Debe seleccionar un comprobante.");
            return;
        }

        actualizarEstado("Generando copia A4...");

        // TODO: Implementar generación de reporte A4
        // Aquí iría la lógica para generar el documento con JasperReports

        Mensaje.alerta(null, "Generar Copia A4",
                "Generando copia A4 del comprobante: " + seleccionado.getNoFactu());

        actualizarEstado("Listo");
    }

    @FXML
    void generarCopiaPOS(ActionEvent event) {
        ComprobantePago seleccionado = tblComprobantes.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            Mensaje.alerta(null, "Selección requerida",
                    "Debe seleccionar un comprobante.");
            return;
        }

        actualizarEstado("Generando copia POS...");

        // TODO: Implementar generación de ticket POS
        // Aquí iría la lógica para imprimir en impresora térmica

        Mensaje.alerta(null, "Generar Copia POS",
                "Generando copia POS del comprobante: " + seleccionado.getNoFactu());

        actualizarEstado("Listo");
    }

    // === MÉTODOS AUXILIARES ===

    private void handleTableClick(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            verDetalleComprobante();
        }
    }

    private void verDetalleComprobante() {
        ComprobantePago seleccionado = tblComprobantes.getSelectionModel().getSelectedItem();

        if (seleccionado == null) return;

        // TODO: Abrir ventana de detalle
        Mensaje.alerta(null, "Detalle del Comprobante",
                "Comprobante: " + seleccionado.getNoFactu() + "\n" +
                        "Cliente: " + seleccionado.getNbrCliente() + "\n" +
                        "Total: " + seleccionado.getTotalFormateado());
    }

    private void actualizarContador() {
        int total = listaComprobantes.size();
        int filtrados = filteredData.size();

        if (total == filtrados) {
            lblContador.setText(total + " Comprobante" + (total != 1 ? "s" : ""));
        } else {
            lblContador.setText(filtrados + " de " + total + " Comprobantes");
        }
    }

    private void actualizarEstado(String mensaje) {
        if (lblEstado != null) {
            lblEstado.setText(mensaje);
        }
    }

    private void restaurarPlaceholder() {
        Label placeholder = new Label("No se encontraron comprobantes");
        placeholder.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");
        tblComprobantes.setPlaceholder(placeholder);
    }

    // === MÉTODOS PÚBLICOS ===

    /**
     * Permite refrescar desde otro controlador
     */
    public void refresh() {
        cargarComprobantes();
    }

    /**
     * Obtiene el comprobante seleccionado
     */
    public ComprobantePago getComprobanteSeleccionado() {
        return tblComprobantes.getSelectionModel().getSelectedItem();
    }
}