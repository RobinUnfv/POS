package com.robin.pos.controller;

import com.robin.pos.dao.ArfafeDao;
import com.robin.pos.dao.ArfaflDao;
import com.robin.pos.dao.ComprobantePagoDao;
import com.robin.pos.model.*;
import com.robin.pos.util.*;
import com.robin.pos.util.ConversorComprobante;
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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para la Lista de Comprobantes de Pago
 *
 * @author Robin POS
 * @version 1.1
 */
public class ListaComprobantePagoController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ListaComprobantePagoController.class.getName());

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

    // Filtros
    @FXML private ComboBox<String> cbxTipoDocu;
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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Estados disponibles
    private static final String ESTADO_TODOS = "Todos";
    private static final String ESTADO_DESPACHADO = "Despachado";
    private static final String ESTADO_PENDIENTE = "Pendiente";
    private static final String ESTADO_ANULADO = "Anulado";
    private static final String ESTADO_PROCESANDO = "Procesando";

    // Tipos de documento
    private static final String FACTURA = "Factura";
    private static final String BOLETA = "Boleta";

    // Stage para indicador de carga
    private Stage loadingStage;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarComboEstado();
        configurarComboTipoDocumento();
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
     * Configura el ComboBox de tipo de documento
     */
    private void configurarComboTipoDocumento() {
        this.cbxTipoDocu.getItems().addAll(
                BOLETA,
                FACTURA
        );
        this.cbxTipoDocu.setValue(FACTURA);
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
                        "-fx-padding: 4 10; -fx-text-fill: #166534; -fx-font-size: 11px; -fx-font-weight: bold;";
            }
            case "P" -> {
                texto = "Pendiente";
                estilo = "-fx-background-color: #fef3c7; -fx-background-radius: 12; " +
                        "-fx-padding: 4 10; -fx-text-fill: #92400e; -fx-font-size: 11px; -fx-font-weight: bold;";
            }
            case "A" -> {
                texto = "Anulado";
                estilo = "-fx-background-color: #fee2e2; -fx-background-radius: 12; " +
                        "-fx-padding: 4 10; -fx-text-fill: #991b1b; -fx-font-size: 11px; -fx-font-weight: bold;";
            }
            case "E" -> {
                texto = "Procesando";
                estilo = "-fx-background-color: #dbeafe; -fx-background-radius: 12; " +
                        "-fx-padding: 4 10; -fx-text-fill: #1e40af; -fx-font-size: 11px; -fx-font-weight: bold;";
            }
            default -> {
                texto = estado;
                estilo = "-fx-background-color: #f1f5f9; -fx-background-radius: 12; " +
                        "-fx-padding: 4 10; -fx-text-fill: #475569; -fx-font-size: 11px;";
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

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(comprobante -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // Buscar por número de comprobante
                if (comprobante.getNoFactu() != null &&
                        comprobante.getNoFactu().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                // Buscar por cliente
                if (comprobante.getNbrCliente() != null &&
                        comprobante.getNbrCliente().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                // Buscar por documento del cliente
                if (comprobante.getNumDocCli() != null &&
                        comprobante.getNumDocCli().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                return false;
            });

            actualizarContador();
        });

        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tblComprobantes.comparatorProperty());
        tblComprobantes.setItems(sortedData);
    }

    /**
     * Configura la tabla
     */
    private void configurarTabla() {
        tblComprobantes.setOnMouseClicked(this::handleTableClick);

        // Habilitar/deshabilitar botones según selección
        tblComprobantes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean haySeleccion = newVal != null;
            btnCopiaA4.setDisable(!haySeleccion);
            btnCopiaPOS.setDisable(!haySeleccion);
        });

        // Inicialmente deshabilitados
        btnCopiaA4.setDisable(true);
        btnCopiaPOS.setDisable(true);
    }

    /**
     * Configura las fechas por defecto
     */
    private void configurarFechas() {
        LocalDate hoy = LocalDate.now();
        dpFechaInicio.setValue(hoy.minusDays(30));
        dpFechaFin.setValue(hoy);
    }

    /**
     * Configura los atajos de teclado
     */
    private void configurarAtajosTeclado() {
        Platform.runLater(() -> {
            if (vbxPrincipal.getScene() != null) {
                vbxPrincipal.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.F5) {
                        actualizarLista(null);
                        event.consume();
                    } else if (event.getCode() == KeyCode.F6) {
                        if (!btnCopiaA4.isDisabled()) {
                            generarCopiaA4(null);
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.F7) {
                        if (!btnCopiaPOS.isDisabled()) {
                            generarCopiaPOS(null);
                            event.consume();
                        }
                    } else if (event.isControlDown() && event.getCode() == KeyCode.F) {
                        txtBuscar.requestFocus();
                        event.consume();
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
        String tipoDocSeleccionado = obtenerCodigoTipoComprobante(cbxTipoDocu.getValue());

        Task<List<ComprobantePago>> task = new Task<>() {
            @Override
            protected List<ComprobantePago> call() throws Exception {
                ComprobantePagoDao dao = new ComprobantePagoDao();
                LocalDate fechaInicio = dpFechaInicio.getValue();
                LocalDate fechaFin = dpFechaFin.getValue();

                return dao.listarPorFechas(NO_CIA, tipoDocSeleccionado, estadoSeleccionado,
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

    /**
     * Obtiene el código de tipo comprobante para la consulta
     */
    private String obtenerCodigoTipoComprobante(String tipoComprobante) {
        if (tipoComprobante == null) return "F";
        return switch (tipoComprobante) {
            case FACTURA -> "F";
            case BOLETA -> "B";
            default -> "F";
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
    void filtrarPorTipoDocu(ActionEvent event) {
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

    /**
     * Genera copia A4 del comprobante seleccionado
     * Utiliza ReporteComprobantePago para generar el reporte
     */
    @FXML
    void generarCopiaA4(ActionEvent event) {
        ComprobantePago seleccionado = tblComprobantes.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            Mensaje.alerta(null, "Selección requerida",
                    "Debe seleccionar un comprobante.");
            return;
        }

        String noFactu = seleccionado.getNoFactu();
        String tipoDoc = obtenerCodigoTipoComprobante(cbxTipoDocu.getValue());

        // Mostrar indicador de carga
        mostrarCargando("Generando copia A4...");
        actualizarEstado("Generando copia A4...");

        Task<DatosComprobanteCompleto> task = new Task<>() {
            @Override
            protected DatosComprobanteCompleto call() throws Exception {
                // 1. Obtener datos de la base de datos
                ArfafeDao arfafeDao = new ArfafeDao();
                ArfaflDao arfaflDao = new ArfaflDao();

                Arfafe cabecera = arfafeDao.buscarPorNumero(NO_CIA, tipoDoc, noFactu);
                if (cabecera == null) {
                    throw new Exception("No se encontró el comprobante: " + noFactu);
                }

                List<Arfafl> detalle = arfaflDao.listarDetallePorFactura(NO_CIA, tipoDoc, noFactu);
                /*
                if (detalle == null || detalle.isEmpty()) {

                    detalle = arfaflDao.listarDetalleConArticulo(NO_CIA, tipoDoc, noFactu);
                }
                */
                if (detalle == null || detalle.isEmpty()) {
                    throw new Exception("No se encontró el detalle del comprobante: " + noFactu);
                }

                // 2. Convertir datos usando ConversorComprobante
                return ConversorComprobante.convertirComprobanteCompleto(cabecera, detalle);
            }
        };

        task.setOnSucceeded(e -> {
            ocultarCargando();
            actualizarEstado("Listo");

            try {
                DatosComprobanteCompleto datos = task.getValue();

                // 3. Generar reporte A4 usando ReporteComprobantePago
                ReporteComprobantePago reporte = new ReporteComprobantePago();
                reporte.generarReporte(
                        datos.getResultadoEmision(),
                        datos.getDetalles(),
                        datos.getDatosCliente(),
                        datos.getDatosVenta()
                );

                LOGGER.info("Copia A4 generada exitosamente: " + noFactu);

            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error al mostrar reporte A4", ex);
                Mensaje.error(null, "Error", "No se pudo mostrar el reporte: " + ex.getMessage());
            }
        });

        task.setOnFailed(e -> {
            ocultarCargando();
            actualizarEstado("Error al generar copia");

            Throwable ex = task.getException();
            LOGGER.log(Level.SEVERE, "Error al generar copia A4", ex);
            Mensaje.error(null, "Error al generar copia A4",
                    ex != null ? ex.getMessage() : "Error desconocido");
        });

        // Ejecutar en hilo separado
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Genera copia POS (ticket) del comprobante seleccionado
     * Utiliza ReporteComprobantePagoTicket para generar el reporte
     */
    @FXML
    void generarCopiaPOS(ActionEvent event) {
        ComprobantePago seleccionado = tblComprobantes.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            Mensaje.alerta(null, "Selección requerida",
                    "Debe seleccionar un comprobante.");
            return;
        }

        String noFactu = seleccionado.getNoFactu();
        String tipoDoc = obtenerCodigoTipoComprobante(cbxTipoDocu.getValue());

        // Mostrar indicador de carga
        mostrarCargando("Generando ticket");
        actualizarEstado("Generando ticket");

        Task<DatosComprobanteCompleto> task = new Task<>() {
            @Override
            protected DatosComprobanteCompleto call() throws Exception {
                // 1. Obtener datos de la base de datos
                ArfafeDao arfafeDao = new ArfafeDao();
                ArfaflDao arfaflDao = new ArfaflDao();

                Arfafe cabecera = arfafeDao.buscarPorNumero(NO_CIA, tipoDoc, noFactu);
                if (cabecera == null) {
                    throw new Exception("No se encontró el comprobante: " + noFactu);
                }

                List<Arfafl> detalle = arfaflDao.listarDetallePorFactura(NO_CIA, tipoDoc, noFactu);
                if (detalle == null || detalle.isEmpty()) {
                    // Intentar con método alternativo
                    detalle = arfaflDao.listarDetalleConArticulo(NO_CIA, tipoDoc, noFactu);
                }

                if (detalle == null || detalle.isEmpty()) {
                    throw new Exception("No se encontró el detalle del comprobante: " + noFactu);
                }

                // 2. Convertir datos usando ConversorComprobante
                return ConversorComprobante.convertirComprobanteCompleto(cabecera, detalle);
            }
        };

        task.setOnSucceeded(e -> {
            ocultarCargando();
            actualizarEstado("Listo");

            try {
                DatosComprobanteCompleto datos = task.getValue();

                // 3. Generar reporte Ticket usando ReporteComprobantePagoTicket
                ReporteComprobantePagoTicket reporte = new ReporteComprobantePagoTicket();
                reporte.generarReporte(
                        datos.getResultadoEmision(),
                        datos.getDetalles(),
                        datos.getDatosCliente(),
                        datos.getDatosVenta()
                );

                LOGGER.info("Ticket generado exitosamente: " + noFactu);

            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error al mostrar ticket POS", ex);
                Mensaje.error(null, "Error", "No se pudo mostrar el ticket: " + ex.getMessage());
            }
        });

        task.setOnFailed(e -> {
            ocultarCargando();
            actualizarEstado("Error al generar ticket");

            Throwable ex = task.getException();
            LOGGER.log(Level.SEVERE, "Error al generar ticket POS", ex);
            Mensaje.error(null, "Error al generar ticket POS",
                    ex != null ? ex.getMessage() : "Error desconocido");
        });

        // Ejecutar en hilo separado
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // === INDICADOR DE CARGA ===

    /**
     * Muestra el indicador de carga modal
     */
    private void mostrarCargando(String mensaje) {
        Platform.runLater(() -> {
            try {
                loadingStage = new Stage();
                loadingStage.initStyle(StageStyle.UNDECORATED);
                loadingStage.initModality(Modality.APPLICATION_MODAL);

                // Obtener la ventana padre
                if (vbxPrincipal.getScene() != null && vbxPrincipal.getScene().getWindow() != null) {
                    loadingStage.initOwner(vbxPrincipal.getScene().getWindow());
                }

                VBox vbox = new VBox(15);
                vbox.setAlignment(Pos.CENTER);
                vbox.setStyle("-fx-background-color: white; -fx-padding: 30; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: #e2e8f0; -fx-border-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 5);");

                ProgressIndicator progress = new ProgressIndicator();
                progress.setStyle("-fx-progress-color: #3b82f6;");
                progress.setPrefSize(50, 50);

                Label label = new Label(mensaje);
                label.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-text-fill: #475569;");

                vbox.getChildren().addAll(progress, label);

                Scene scene = new Scene(vbox);
                scene.setFill(null);
                loadingStage.setScene(scene);
                loadingStage.show();

                // Centrar en pantalla
                loadingStage.centerOnScreen();

            } catch (Exception e) {
                LOGGER.warning("No se pudo mostrar indicador de carga: " + e.getMessage());
            }
        });
    }

    /**
     * Oculta el indicador de carga
     */
    private void ocultarCargando() {
        Platform.runLater(() -> {
            try {
                if (loadingStage != null) {
                    loadingStage.close();
                    loadingStage = null;
                }
            } catch (Exception e) {
                LOGGER.warning("Error al ocultar indicador de carga: " + e.getMessage());
            }
        });
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
        Platform.runLater(() -> {
            if (lblEstado != null) {
                lblEstado.setText(mensaje);
            }
        });
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