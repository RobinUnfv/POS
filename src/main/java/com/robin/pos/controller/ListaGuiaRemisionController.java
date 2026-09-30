package com.robin.pos.controller;

import com.robin.pos.dao.GuiaRemisionDao;
import com.robin.pos.model.GuiaResumen;
import com.robin.pos.model.ItemCatalogo;
import com.robin.pos.reporte.GuiaRemisionReporte;
import com.robin.pos.util.AppConfig;
import com.robin.pos.util.Mensaje;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lista de guías de remisión electrónicas (vista FACTU.V_GUIA_PENDIENTE).
 * Filtros por rango de fechas y estado SUNAT, búsqueda instantánea, indicadores
 * por estado, impresión / exportación PDF y acceso al registro de una nueva guía.
 *
 * @author Robin POS
 */
public class ListaGuiaRemisionController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ListaGuiaRemisionController.class.getName());
    private static final String NO_CIA = AppConfig.noCia();
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final List<ItemCatalogo> ESTADOS = List.of(
            new ItemCatalogo("", "Todos los estados"),
            new ItemCatalogo("P", "Pendiente"),
            new ItemCatalogo("E", "Enviada - esperando CDR"),
            new ItemCatalogo("A", "Aceptada"),
            new ItemCatalogo("O", "Aceptada con observaciones"),
            new ItemCatalogo("R", "Rechazada"),
            new ItemCatalogo("B", "Baja solicitada"),
            new ItemCatalogo("X", "Baja confirmada"),
            new ItemCatalogo("N", "No enviar"),
            new ItemCatalogo("G", "Error interno"));

    // ============================ FXML ============================
    @FXML private BorderPane root;
    @FXML private Label lblTitulo;
    @FXML private VBox kpiTodas;
    @FXML private VBox kpiPendientes;
    @FXML private VBox kpiEnviadas;
    @FXML private VBox kpiAceptadas;
    @FXML private VBox kpiRechazadas;
    @FXML private VBox kpiBajas;
    @FXML private Label lblKpiTotal;
    @FXML private Label lblKpiPendientes;
    @FXML private Label lblKpiEnviadas;
    @FXML private Label lblKpiAceptadas;
    @FXML private Label lblKpiRechazadas;
    @FXML private Label lblKpiBajas;
    @FXML private HBox hbxToolbar;
    @FXML private TextField txtBuscar;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private ComboBox<ItemCatalogo> cbxEstado;
    @FXML private Button btnFiltrar;
    @FXML private Button btnImprimir;
    @FXML private Button btnExportar;
    @FXML private Button btnNueva;
    @FXML private Button btnRefrescar;
    @FXML private TableView<GuiaResumen> tblGuias;
    @FXML private TableColumn<GuiaResumen, String> colNumero;
    @FXML private TableColumn<GuiaResumen, LocalDate> colFecha;
    @FXML private TableColumn<GuiaResumen, GuiaResumen> colDestinatario;
    @FXML private TableColumn<GuiaResumen, String> colDocRef;
    @FXML private TableColumn<GuiaResumen, String> colMotivo;
    @FXML private TableColumn<GuiaResumen, BigDecimal> colPeso;
    @FXML private TableColumn<GuiaResumen, String> colGuiaInterna;
    @FXML private TableColumn<GuiaResumen, GuiaResumen> colEstado;
    @FXML private TableColumn<GuiaResumen, String> colRespuesta;
    @FXML private ProgressIndicator piCargando;
    @FXML private Label lblEstado;

    // ============================ ESTADO ============================
    private final GuiaRemisionDao dao = new GuiaRemisionDao();
    private final ObservableList<GuiaResumen> guias = FXCollections.observableArrayList();
    private final FilteredList<GuiaResumen> filtradas = new FilteredList<>(guias, g -> true);
    private String grupoIndicador = "TODAS";
    private String usuario = System.getProperty("user.name");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarFiltros();
        configurarTabla();
        configurarAtajos();
        marcarIndicador(kpiTodas);
        refrescarLista(null);
    }

    // =================================================================
    // CONFIGURACIÓN
    // =================================================================
    private void configurarFiltros() {
        StringConverter<LocalDate> conv = new StringConverter<>() {
            @Override
            public String toString(LocalDate d) {
                return d == null ? "" : FECHA.format(d);
            }

            @Override
            public LocalDate fromString(String s) {
                try {
                    return s == null || s.isBlank() ? null : LocalDate.parse(s.trim(), FECHA);
                } catch (Exception e) {
                    return null;
                }
            }
        };
        dpDesde.setConverter(conv);
        dpHasta.setConverter(conv);
        dpDesde.setValue(LocalDate.now().withDayOfMonth(1));
        dpHasta.setValue(LocalDate.now());

        cbxEstado.getItems().setAll(ESTADOS);
        cbxEstado.setConverter(new StringConverter<>() {
            @Override
            public String toString(ItemCatalogo i) {
                return i == null ? "" : i.getDescripcion();
            }

            @Override
            public ItemCatalogo fromString(String s) {
                return null;
            }
        });
        cbxEstado.getSelectionModel().selectFirst();
        cbxEstado.valueProperty().addListener((o, a, n) -> refrescarLista(null));

        txtBuscar.textProperty().addListener((o, a, n) -> aplicarFiltro());
    }

    private void configurarTabla() {
        SortedList<GuiaResumen> ordenadas = new SortedList<>(filtradas);
        ordenadas.comparatorProperty().bind(tblGuias.comparatorProperty());
        tblGuias.setItems(ordenadas);

        // N° de guía en monoespaciado azul
        colNumero.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNumeroCompleto()));
        colNumero.setCellFactory(celdaTexto(v -> v, "numero-guia"));

        colFecha.setCellValueFactory(c -> c.getValue().fecEmisionProperty());
        colFecha.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : FECHA.format(d));
            }
        });

        // Destinatario: nombre + documento en dos líneas
        colDestinatario.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
        colDestinatario.setComparator((a, b) -> nvl(a.getDestinatario()).compareToIgnoreCase(nvl(b.getDestinatario())));
        colDestinatario.setCellFactory(col -> new TableCell<>() {
            private final Label nombre = new Label();
            private final Label doc = new Label();
            private final VBox caja = new VBox(1, nombre, doc);

            {
                nombre.getStyleClass().add("texto-principal");
                doc.getStyleClass().addAll("texto-secundario", "mono");
                caja.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(GuiaResumen g, boolean empty) {
                super.updateItem(g, empty);
                if (empty || g == null) {
                    setGraphic(null);
                } else {
                    nombre.setText(nvl(g.getDestinatario()));
                    doc.setText(nvl(g.getNroDocDestin()));
                    setGraphic(caja);
                }
            }
        });

        colDocRef.setCellValueFactory(c -> c.getValue().docReferenciaProperty());
        colDocRef.setCellFactory(celdaTexto(v -> v, "mono"));
        colMotivo.setCellValueFactory(c -> c.getValue().motivoProperty());

        colPeso.setCellValueFactory(c -> c.getValue().pesoBrutoProperty());
        colPeso.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.setScale(3, RoundingMode.HALF_UP).toPlainString());
            }
        });

        colGuiaInterna.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                nvl(c.getValue().getBodega()) + " / " + nvl(c.getValue().getNoGuia())));
        colGuiaInterna.setCellFactory(celdaTexto(v -> v, "texto-secundario"));

        // Estado SUNAT como etiqueta de color
        colEstado.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
        colEstado.setComparator((a, b) -> nvl(a.getStsSunat()).compareTo(nvl(b.getStsSunat())));
        colEstado.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(GuiaResumen g, boolean empty) {
                super.updateItem(g, empty);
                if (empty || g == null) {
                    setGraphic(null);
                    setTooltip(null);
                    return;
                }
                String sts = nvl(g.getStsSunat());
                badge.setText(GuiaRemisionDao.descripcionEstado(sts));
                badge.getStyleClass().setAll("label", "badge", "badge-" + sts);
                setGraphic(badge);
                setAlignment(Pos.CENTER);
                String detalle = nvl(g.getDescEstado())
                        + (g.getIntentosEnvio() > 0 ? "\nIntentos de envío: " + g.getIntentosEnvio() : "")
                        + (g.getFecEnvio() != null ? "\nEnviada: " + FECHA_HORA.format(g.getFecEnvio()) : "")
                        + (g.getTicketSunat() != null ? "\nTicket: " + g.getTicketSunat() : "");
                setTooltip(new Tooltip(detalle));
            }
        });

        colRespuesta.setCellValueFactory(c -> c.getValue().resulSunatProperty());
        colRespuesta.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v);
                setTooltip(empty || v == null || v.isBlank() ? null : new Tooltip(v));
            }
        });

        // Placeholder con ilustración
        ImageView img = new ImageView(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/com/robin/pos/imagenes/sin_guias.png"))));
        img.setFitWidth(110);
        img.setPreserveRatio(true);
        Label txt = new Label("No hay guías de remisión en el rango seleccionado");
        VBox vacio = new VBox(10, img, txt);
        vacio.setAlignment(Pos.CENTER);
        tblGuias.setPlaceholder(vacio);

        // Doble clic = ver PDF; menú contextual
        tblGuias.setRowFactory(tv -> {
            TableRow<GuiaResumen> fila = new TableRow<>();
            fila.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && !fila.isEmpty()) {
                    imprimirGuia(null);
                }
            });
            fila.contextMenuProperty().bind(javafx.beans.binding.Bindings
                    .when(fila.emptyProperty()).then((ContextMenu) null).otherwise(crearMenuContextual()));
            return fila;
        });

        btnImprimir.disableProperty().bind(tblGuias.getSelectionModel().selectedItemProperty().isNull());
        btnExportar.disableProperty().bind(tblGuias.getSelectionModel().selectedItemProperty().isNull());
    }

    private ContextMenu crearMenuContextual() {
        MenuItem ver = new MenuItem("Ver representación impresa (PDF)");
        ver.setOnAction(e -> imprimirGuia(null));
        MenuItem exportar = new MenuItem("Guardar como PDF...");
        exportar.setOnAction(e -> exportarPdf(null));
        MenuItem copiar = new MenuItem("Copiar N° de guía");
        copiar.setOnAction(e -> {
            GuiaResumen g = tblGuias.getSelectionModel().getSelectedItem();
            if (g != null) {
                ClipboardContent cc = new ClipboardContent();
                cc.putString(g.getNumeroCompleto());
                Clipboard.getSystemClipboard().setContent(cc);
                lblEstado.setText("Copiado: " + g.getNumeroCompleto());
            }
        });
        MenuItem respuesta = new MenuItem("Ver respuesta de SUNAT");
        respuesta.setOnAction(e -> {
            GuiaResumen g = tblGuias.getSelectionModel().getSelectedItem();
            if (g != null) {
                Mensaje.alerta(null, "Guía " + g.getNumeroCompleto(),
                        "Estado: " + nvl(g.getDescEstado())
                                + "\nTicket: " + nvl(g.getTicketSunat())
                                + "\nIntentos de envío: " + g.getIntentosEnvio()
                                + "\n\nRespuesta:\n" + (g.getResulSunat() == null ? "(sin respuesta aún)" : g.getResulSunat()));
            }
        });
        return new ContextMenu(ver, exportar, new SeparatorMenuItem(), copiar, respuesta);
    }

    private void configurarAtajos() {
        root.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F5) {
                refrescarLista(null);
                e.consume();
            } else if (e.getCode() == KeyCode.F3) {
                nuevaGuia(null);
                e.consume();
            } else if (e.isControlDown() && e.getCode() == KeyCode.F) {
                txtBuscar.requestFocus();
                txtBuscar.selectAll();
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER && tblGuias.isFocused()) {
                imprimirGuia(null);
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE && txtBuscar.isFocused() && !txtBuscar.getText().isEmpty()) {
                txtBuscar.clear();
                e.consume();
            }
        });
    }

    // =================================================================
    // CARGA Y FILTROS
    // =================================================================
    @FXML
    void refrescarLista(ActionEvent e) {
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        if (desde == null || hasta == null) {
            Mensaje.alerta(null, "Filtro", "Seleccione el rango de fechas.");
            return;
        }
        if (desde.isAfter(hasta)) {
            Mensaje.alerta(null, "Filtro", "La fecha 'Desde' no puede ser mayor que 'Hasta'.");
            return;
        }
        String estado = cbxEstado.getValue() == null ? "" : cbxEstado.getValue().getCodigo();
        ejecutar(() -> dao.listarGuias(NO_CIA, desde, hasta, estado.isEmpty() ? null : estado), lista -> {
            GuiaResumen seleccion = tblGuias.getSelectionModel().getSelectedItem();
            guias.setAll(lista);
            actualizarIndicadores();
            aplicarFiltro();
            if (seleccion != null) {
                guias.stream().filter(g -> Objects.equals(g.getIdGuia(), seleccion.getIdGuia())).findFirst()
                        .ifPresent(g -> tblGuias.getSelectionModel().select(g));
            }
            lblEstado.setText(lista.size() + " guía(s) del " + FECHA.format(desde) + " al " + FECHA.format(hasta)
                    + "  ·  actualizado a las " + LocalTime.now().withNano(0));
        });
    }

    private void aplicarFiltro() {
        String q = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toUpperCase();
        Set<String> estados = estadosDelGrupo(grupoIndicador);
        filtradas.setPredicate(g -> {
            if (estados != null && !estados.contains(nvl(g.getStsSunat()))) {
                return false;
            }
            if (q.isEmpty()) {
                return true;
            }
            return contiene(g.getNumeroCompleto(), q) || contiene(g.getDestinatario(), q)
                    || contiene(g.getNroDocDestin(), q) || contiene(g.getDocReferencia(), q)
                    || contiene(g.getNoGuia(), q) || contiene(g.getTicketSunat(), q);
        });
        if (!guias.isEmpty() && filtradas.size() != guias.size()) {
            lblEstado.setText("Mostrando " + filtradas.size() + " de " + guias.size() + " guía(s)");
        }
    }

    private void actualizarIndicadores() {
        lblKpiTotal.setText(String.valueOf(guias.size()));
        lblKpiPendientes.setText(String.valueOf(contar("PEND")));
        lblKpiEnviadas.setText(String.valueOf(contar("ENV")));
        lblKpiAceptadas.setText(String.valueOf(contar("ACEP")));
        lblKpiRechazadas.setText(String.valueOf(contar("RECH")));
        lblKpiBajas.setText(String.valueOf(contar("BAJA")));
    }

    private long contar(String grupo) {
        Set<String> estados = estadosDelGrupo(grupo);
        return guias.stream().filter(g -> estados == null || estados.contains(nvl(g.getStsSunat()))).count();
    }

    private static Set<String> estadosDelGrupo(String grupo) {
        return switch (grupo) {
            case "PEND" -> Set.of("P", "G");
            case "ENV" -> Set.of("E");
            case "ACEP" -> Set.of("A", "O");
            case "RECH" -> Set.of("R");
            case "BAJA" -> Set.of("B", "X");
            default -> null;
        };
    }

    @FXML
    void filtrarPorIndicador(MouseEvent e) {
        if (e.getSource() instanceof Node n && n.getUserData() != null) {
            grupoIndicador = n.getUserData().toString();
            marcarIndicador(n);
            aplicarFiltro();
        }
    }

    private void marcarIndicador(Node activo) {
        for (Node n : List.of(kpiTodas, kpiPendientes, kpiEnviadas, kpiAceptadas, kpiRechazadas, kpiBajas)) {
            n.getStyleClass().remove("kpi-card-activa");
        }
        activo.getStyleClass().add("kpi-card-activa");
    }

    // =================================================================
    // ACCIONES
    // =================================================================
    @FXML
    void nuevaGuia(ActionEvent e) {
        try {
            GuiaRemisionController.abrirVentana(root.getScene() != null ? root.getScene().getWindow() : null,
                    usuario, guia -> refrescarLista(null));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "No se pudo abrir el formulario de guía", ex);
            Mensaje.error(null, "Error", "No se pudo abrir el formulario de guía:\n" + ex.getMessage());
        }
    }

    @FXML
    void imprimirGuia(ActionEvent e) {
        GuiaResumen g = tblGuias.getSelectionModel().getSelectedItem();
        if (g == null) {
            Mensaje.alerta(null, "Guía", "Seleccione una guía de la lista.");
            return;
        }
        lblEstado.setText("Generando PDF de " + g.getNumeroCompleto() + "...");
        ejecutar(() -> new GuiaRemisionReporte().abrirPdf(g.getIdGuia()),
                pdf -> lblEstado.setText("PDF generado: " + g.getNumeroCompleto()));
    }

    @FXML
    void exportarPdf(ActionEvent e) {
        GuiaResumen g = tblGuias.getSelectionModel().getSelectedItem();
        if (g == null) {
            Mensaje.alerta(null, "Guía", "Seleccione una guía de la lista.");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar guía de remisión");
        fc.setInitialFileName(AppConfig.empresaRuc() + "-09-" + g.getNumeroCompleto() + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento PDF", "*.pdf"));
        File destino = fc.showSaveDialog(root.getScene().getWindow());
        if (destino == null) {
            return;
        }
        ejecutar(() -> new GuiaRemisionReporte().exportarPdf(g.getIdGuia(), destino),
                f -> lblEstado.setText("Guardado en " + f.getAbsolutePath()));
    }

    /** Recarga la lista (lo usa el Dashboard al registrar una guía desde el menú). */
    public void refrescar() {
        refrescarLista(null);
    }

    /** Usuario de la sesión (se pasa al formulario de registro). */
    public void setUsuario(String usuario) {
        if (usuario != null && !usuario.isBlank()) {
            this.usuario = usuario;
        }
    }

    // =================================================================
    // UTILIDADES
    // =================================================================
    private <T> void ejecutar(Callable<T> trabajo, Consumer<T> alTerminar) {
        piCargando.setVisible(true);
        btnRefrescar.setDisable(true);
        btnFiltrar.setDisable(true);
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return trabajo.call();
            }
        };
        task.setOnSucceeded(ev -> {
            terminarCarga();
            alTerminar.accept(task.getValue());
        });
        task.setOnFailed(ev -> {
            terminarCarga();
            Throwable ex = task.getException();
            LOGGER.log(Level.SEVERE, "Error en la lista de guías", ex);
            lblEstado.setText("Error: " + (ex != null ? ex.getMessage() : ""));
            Mensaje.error(null, "Error", ex != null ? ex.getMessage() : "Error desconocido");
        });
        Thread t = new Thread(task, "lista-guias");
        t.setDaemon(true);
        t.start();
    }

    private void terminarCarga() {
        piCargando.setVisible(false);
        btnRefrescar.setDisable(false);
        btnFiltrar.setDisable(false);
    }

    private static <T> javafx.util.Callback<TableColumn<GuiaResumen, T>, TableCell<GuiaResumen, T>> celdaTexto(
            Function<T, String> texto, String estilo) {
        return col -> new TableCell<>() {
            {
                getStyleClass().add(estilo);
            }

            @Override
            protected void updateItem(T v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : texto.apply(v));
            }
        };
    }

    private static boolean contiene(String valor, String q) {
        return valor != null && valor.toUpperCase().contains(q);
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    @SuppressWarnings("unused")
    private void enfocarBusqueda() {
        Platform.runLater(() -> txtBuscar.requestFocus());
    }
}
