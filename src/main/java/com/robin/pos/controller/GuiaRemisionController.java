package com.robin.pos.controller;

import com.robin.pos.dao.ArccdiDao;
import com.robin.pos.dao.ArccdpDao;
import com.robin.pos.dao.ArccprDao;
import com.robin.pos.dao.ClienteDao;
import com.robin.pos.dao.GuiaRemisionDao;
import com.robin.pos.model.Arccdi;
import com.robin.pos.model.Arccdp;
import com.robin.pos.model.Arccpr;
import com.robin.pos.model.Cliente;
import com.robin.pos.model.GuiaRemision;
import com.robin.pos.model.GuiaRemisionDetalle;
import com.robin.pos.model.ItemCatalogo;
import com.robin.pos.reporte.GuiaRemisionReporte;
import com.robin.pos.util.AppConfig;
import com.robin.pos.util.Mensaje;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registro de la Guía de Remisión Electrónica Remitente (GRE 2022).
 *
 * Flujo:
 *  1. El usuario escribe el N° de factura/boleta (NO_FACTU) y pulsa "Cargar datos".
 *  2. Se leen FACTU.ARPFFE (cabecera) y FACTU.ARPFFL (detalle) de NO_CIA + NO_FACTU,
 *     y el destinatario se completa con el maestro de clientes (CXC.ARCCMC).
 *  3. Se completan/corrigen los datos exigidos por SUNAT (peso, ubigeos, transporte).
 *  4. "Registrar guía" graba FACTU.ARGUIA + FACTU.ARGUIL en una transacción, con el
 *     correlativo de FACTU.GRE_SERIE_CTRL, y la deja en estado P (pendiente de envío).
 *  5. Opcionalmente abre la representación impresa en PDF.
 *
 * Se abre como ventana modal (ver DashboardController.ingresarGuiaRemision) y admite
 * ser precargado desde otra pantalla con {@link #cargarDesdeFactura(String)}.
 *
 * @author Robin POS
 */
public class GuiaRemisionController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(GuiaRemisionController.class.getName());
    private static final String NO_CIA = AppConfig.noCia();
    private static final String ESTILO_ERROR = "validation-error";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Catálogo 06 - Tipo de documento de identidad */
    private static final List<ItemCatalogo> TIPOS_DOC_IDENTIDAD = List.of(
            new ItemCatalogo("6", "RUC"),
            new ItemCatalogo("1", "DNI"),
            new ItemCatalogo("4", "Carnet de extranjería"),
            new ItemCatalogo("7", "Pasaporte"),
            new ItemCatalogo("0", "Doc. no domiciliado"));

    /** Catálogo 03 - Unidades de medida más usadas en la GRE */
    private static final ObservableList<String> UNIDADES = FXCollections.observableArrayList(
            "NIU", "DZN", "KGM", "BX", "PK", "SET", "MTR", "LTR", "PR", "ZZ");

    // ============================ FXML ============================
    @FXML private StackPane rootStack;
    @FXML private BorderPane root;
    @FXML private StackPane overlayCarga;
    @FXML private Label lblCargando;
    @FXML private ScrollPane scrollContent;

    // Cabecera
    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblRucEmisor;
    @FXML private ComboBox<String> cbxSerie;
    @FXML private Label lblNumeroGuia;
    @FXML private Label lblNumeroHint;

    // Documento origen
    @FXML private TextField txtNoFactu;
    @FXML private Button btnCargarOrigen;
    @FXML private Label lblOrigen;
    @FXML private DatePicker dpFecEmision;
    @FXML private DatePicker dpFecInicio;

    // Destinatario
    @FXML private ComboBox<ItemCatalogo> cbxTipoDocDestin;
    @FXML private TextField txtNroDocDestin;
    @FXML private Button btnBuscarDestin;
    @FXML private TextField txtRazonDestin;

    // Traslado
    @FXML private ComboBox<ItemCatalogo> cbxMotivo;
    @FXML private VBox vbxDescMotivo;
    @FXML private TextField txtDescMotivo;
    @FXML private RadioButton rbPrivado;
    @FXML private RadioButton rbPublico;
    @FXML private TextField txtPeso;
    @FXML private ComboBox<String> cbxUndPeso;
    @FXML private TextField txtBultos;
    @FXML private CheckBox chkTransbordo;
    @FXML private CheckBox chkM1L;

    // Partida / llegada
    @FXML private ComboBox<Arccdp> cbxPartDep;
    @FXML private ComboBox<Arccpr> cbxPartProv;
    @FXML private ComboBox<Arccdi> cbxPartDist;
    @FXML private TextField txtPartDireccion;
    @FXML private TextField txtPartUbigeo;
    @FXML private VBox vbxPartEstab;
    @FXML private TextField txtPartCodEstab;
    @FXML private ComboBox<Arccdp> cbxLlegDep;
    @FXML private ComboBox<Arccpr> cbxLlegProv;
    @FXML private ComboBox<Arccdi> cbxLlegDist;
    @FXML private TextField txtLlegDireccion;
    @FXML private TextField txtLlegUbigeo;
    @FXML private VBox vbxLlegEstab;
    @FXML private TextField txtLlegCodEstab;

    // Transporte
    @FXML private Label lblModalidadInfo;
    @FXML private VBox vbxPublico;
    @FXML private TextField txtRucTransp;
    @FXML private TextField txtRazonTransp;
    @FXML private TextField txtMtc;
    @FXML private VBox vbxPrivado;
    @FXML private TextField txtPlaca;
    @FXML private TextField txtMarca;
    @FXML private TextField txtCertInscripcion;
    @FXML private ComboBox<ItemCatalogo> cbxTipoDocConduc;
    @FXML private TextField txtNroDocConduc;
    @FXML private TextField txtLicencia;
    @FXML private TextField txtNombreConduc;
    @FXML private TextField txtApellidoConduc;

    // Documento relacionado / comprador / observaciones
    @FXML private ComboBox<ItemCatalogo> cbxTipoDocRef;
    @FXML private TextField txtSerieRef;
    @FXML private TextField txtCorrRef;
    @FXML private TextField txtRucEmisorRef;
    @FXML private VBox vbxComprador;
    @FXML private ComboBox<ItemCatalogo> cbxTipoDocCompr;
    @FXML private TextField txtNroDocCompr;
    @FXML private TextField txtRazonCompr;
    @FXML private TextArea txtObservaciones;

    // Bienes
    @FXML private TableView<GuiaRemisionDetalle> tblDetalle;
    @FXML private TableColumn<GuiaRemisionDetalle, Integer> colItem;
    @FXML private TableColumn<GuiaRemisionDetalle, String> colCodigo;
    @FXML private TableColumn<GuiaRemisionDetalle, String> colDescripcion;
    @FXML private TableColumn<GuiaRemisionDetalle, BigDecimal> colCantidad;
    @FXML private TableColumn<GuiaRemisionDetalle, String> colUnidad;
    @FXML private TableColumn<GuiaRemisionDetalle, BigDecimal> colPeso;
    @FXML private Label lblResumenItems;
    @FXML private Button btnUsarPeso;
    @FXML private Button btnAgregarLinea;
    @FXML private Button btnQuitarLinea;

    // Pie
    @FXML private CheckBox chkImprimir;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCancelar;
    @FXML private Button btnRegistrar;

    @FXML private TabPane tabPane;
    @FXML private Label lblUsuario;
    @FXML private VBox guiaSubmenu;
    @FXML private Label lblGuiaArrow;

    // ============================ ESTADO ============================
    private final GuiaRemisionDao dao = new GuiaRemisionDao();
    private final ObservableList<GuiaRemisionDetalle> lineas = FXCollections.observableArrayList(
            d -> new Observable[]{d.cantidadProperty(), d.pesoTotalProperty()});
    private final ToggleGroup grupoModalidad = new ToggleGroup();

    private UbigeoSelector ubigeoPartida;
    private UbigeoSelector ubigeoLlegada;
    private GuiaRemision guiaOrigen;          // ARPFFE cargada (bodega / no_guia)
    private String usuario = System.getProperty("user.name");
    private Consumer<GuiaRemision> onGuiaRegistrada;
    private Runnable onCerrar;

    // =================================================================
    // INICIALIZACIÓN
    // =================================================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblRucEmisor.setText("R.U.C. " + AppConfig.empresaRuc());

        configurarFechas();
        configurarCombosEstaticos();
        configurarModalidad();
        configurarFormatos();
        configurarTabla();

        ubigeoPartida = new UbigeoSelector(cbxPartDep, cbxPartProv, cbxPartDist, txtPartUbigeo);
        ubigeoLlegada = new UbigeoSelector(cbxLlegDep, cbxLlegProv, cbxLlegDist, txtLlegUbigeo);

        cbxMotivo.valueProperty().addListener((o, a, n) -> actualizarPorMotivo());
        cbxSerie.valueProperty().addListener((o, a, n) -> mostrarCorrelativoReferencial(n));

        limpiarErrorAlEditar(txtNoFactu, txtNroDocDestin, txtRazonDestin, txtDescMotivo, txtPeso,
                txtPartDireccion, txtPartUbigeo, txtPartCodEstab, txtLlegDireccion, txtLlegUbigeo,
                txtLlegCodEstab, txtRucTransp, txtRazonTransp, txtPlaca, txtNroDocConduc, txtLicencia,
                txtNombreConduc, txtApellidoConduc, txtNroDocCompr, txtRazonCompr, txtCorrRef,
                dpFecEmision, dpFecInicio, cbxSerie, cbxMotivo, cbxTipoDocDestin);

        configurarAtajos();
        cargarCatalogos();
        valoresPorDefecto();
        actualizarOrigenVisual();

        Platform.runLater(() -> txtNoFactu.requestFocus());
    }

    private void configurarFechas() {
        StringConverter<LocalDate> conv = new StringConverter<>() {
            @Override
            public String toString(LocalDate d) {
                return d == null ? "" : FORMATO_FECHA.format(d);
            }

            @Override
            public LocalDate fromString(String s) {
                try {
                    return s == null || s.isBlank() ? null : LocalDate.parse(s.trim(), FORMATO_FECHA);
                } catch (Exception e) {
                    return null;
                }
            }
        };
        for (DatePicker dp : List.of(dpFecEmision, dpFecInicio)) {
            dp.setConverter(conv);
            dp.setPromptText("dd/mm/aaaa");
        }
    }

    private void configurarCombosEstaticos() {
        cbxTipoDocDestin.getItems().setAll(TIPOS_DOC_IDENTIDAD);
        cbxTipoDocCompr.getItems().setAll(TIPOS_DOC_IDENTIDAD);
        cbxTipoDocConduc.getItems().setAll(TIPOS_DOC_IDENTIDAD.subList(1, 4)); // DNI, CE, Pasaporte
        cbxUndPeso.getItems().setAll("KGM", "TNE");

        // Tipo de documento: al cambiar se adapta el largo máximo del número
        cbxTipoDocDestin.valueProperty().addListener((o, a, n) ->
                txtNroDocDestin.setPromptText(n == null ? "" : switch (n.getCodigo()) {
                    case "6" -> "11 dígitos";
                    case "1" -> "8 dígitos";
                    default -> "Hasta 15 caracteres";
                }));
    }

    private void configurarModalidad() {
        rbPrivado.setToggleGroup(grupoModalidad);
        rbPublico.setToggleGroup(grupoModalidad);
        rbPrivado.setUserData("02");
        rbPublico.setUserData("01");
        grupoModalidad.selectedToggleProperty().addListener((o, a, n) -> actualizarModalidad());
        chkM1L.selectedProperty().addListener((o, a, n) -> actualizarModalidad());
    }

    /** Filtros de entrada: solo dígitos, mayúsculas, longitudes máximas de SUNAT. */
    private void configurarFormatos() {
        txtNoFactu.setTextFormatter(formato("[A-Z0-9-]{0,14}", true));
        txtNroDocDestin.setTextFormatter(formato("[A-Z0-9]{0,15}", true));
        txtNroDocCompr.setTextFormatter(formato("[A-Z0-9]{0,15}", true));
        txtNroDocConduc.setTextFormatter(formato("[A-Z0-9]{0,15}", true));
        txtRucTransp.setTextFormatter(formato("\\d{0,11}", false));
        txtRucEmisorRef.setTextFormatter(formato("\\d{0,11}", false));
        txtPartUbigeo.setTextFormatter(formato("\\d{0,6}", false));
        txtLlegUbigeo.setTextFormatter(formato("\\d{0,6}", false));
        txtPartCodEstab.setTextFormatter(formato("\\d{0,4}", false));
        txtLlegCodEstab.setTextFormatter(formato("\\d{0,4}", false));
        txtPeso.setTextFormatter(formato("\\d{0,9}([.,]\\d{0,3})?", false));
        txtBultos.setTextFormatter(formato("\\d{0,5}", false));
        txtPlaca.setTextFormatter(formato("[A-Z0-9-]{0,10}", true));
        txtLicencia.setTextFormatter(formato("[A-Z0-9-]{0,15}", true));
        txtSerieRef.setTextFormatter(formato("[A-Z0-9]{0,4}", true));
        txtCorrRef.setTextFormatter(formato("\\d{0,8}", false));
        txtMtc.setTextFormatter(formato("[A-Z0-9-]{0,20}", true));

        for (TextInputControl t : List.of(txtRazonDestin, txtDescMotivo, txtPartDireccion, txtLlegDireccion,
                txtRazonTransp, txtMarca, txtCertInscripcion, txtNombreConduc, txtApellidoConduc, txtRazonCompr)) {
            t.setTextFormatter(formato(".{0,150}", true));
        }
        txtObservaciones.setTextFormatter(new TextFormatter<String>(c ->
                c.getControlNewText().length() <= 500 ? c : null));
    }

    private void valoresPorDefecto() {
        dpFecEmision.setValue(LocalDate.now());
        dpFecInicio.setValue(LocalDate.now());
        cbxTipoDocDestin.getSelectionModel().select(0);
        cbxTipoDocConduc.getSelectionModel().select(0);
        cbxTipoDocCompr.getSelectionModel().select(0);
        cbxUndPeso.setValue("KGM");
        rbPrivado.setSelected(true);
        txtPartDireccion.setText(AppConfig.empresaDireccion());
        txtRucEmisorRef.setText(AppConfig.empresaRuc());
        actualizarModalidad();
    }

    // =================================================================
    // TABLA DE BIENES
    // =================================================================
    private void configurarTabla() {
        tblDetalle.setItems(lineas);

        colItem.setCellValueFactory(c -> c.getValue().itemOrdenProperty().asObject());

        colCodigo.setCellValueFactory(c -> c.getValue().codProductoProperty());
        colCodigo.setCellFactory(TextFieldTableCell.forTableColumn());
        colCodigo.setOnEditCommit(e -> e.getRowValue().setCodProducto(mayus(e.getNewValue())));

        colDescripcion.setCellValueFactory(c -> c.getValue().descripcionProperty());
        colDescripcion.setCellFactory(TextFieldTableCell.forTableColumn());
        colDescripcion.setOnEditCommit(e -> {
            String v = mayus(e.getNewValue());
            e.getRowValue().setDescripcion(v.length() > 500 ? v.substring(0, 500) : v);
        });

        colCantidad.setCellValueFactory(c -> c.getValue().cantidadProperty());
        colCantidad.setCellFactory(TextFieldTableCell.forTableColumn(new DecimalConverter(4)));
        colCantidad.setOnEditCommit(e -> {
            BigDecimal v = e.getNewValue();
            if (v != null && v.signum() > 0) {
                e.getRowValue().setCantidad(v);
            }
            tblDetalle.refresh();
        });

        colUnidad.setCellValueFactory(c -> c.getValue().undMedidaProperty());
        colUnidad.setCellFactory(ComboBoxTableCell.forTableColumn(UNIDADES));
        colUnidad.setOnEditCommit(e -> e.getRowValue().setUndMedida(e.getNewValue()));

        colPeso.setCellValueFactory(c -> c.getValue().pesoTotalProperty());
        colPeso.setCellFactory(TextFieldTableCell.forTableColumn(new DecimalConverter(3)));
        colPeso.setOnEditCommit(e -> e.getRowValue().setPesoTotal(e.getNewValue()));

        // Estado vacío con ilustración
        ImageView ilustracion = new ImageView(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/com/robin/pos/imagenes/sin_guias.png"))));
        ilustracion.setFitWidth(96);
        ilustracion.setPreserveRatio(true);
        Label texto = new Label("Cargue una factura/boleta o agregue ítems manualmente");
        VBox vacio = new VBox(8, ilustracion, texto);
        vacio.setAlignment(Pos.CENTER);
        tblDetalle.setPlaceholder(vacio);

        tblDetalle.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE && tblDetalle.getEditingCell() == null) {
                quitarLinea(null);
            }
        });

        btnQuitarLinea.disableProperty().bind(tblDetalle.getSelectionModel().selectedItemProperty().isNull());
        lineas.addListener((javafx.collections.ListChangeListener<GuiaRemisionDetalle>) c -> actualizarResumen());
        actualizarResumen();
    }

    private void actualizarResumen() {
        BigDecimal cant = BigDecimal.ZERO;
        BigDecimal peso = BigDecimal.ZERO;
        for (GuiaRemisionDetalle d : lineas) {
            if (d.getCantidad() != null) {
                cant = cant.add(d.getCantidad());
            }
            if (d.getPesoTotal() != null) {
                peso = peso.add(d.getPesoTotal());
            }
        }
        String txt = lineas.size() + (lineas.size() == 1 ? " ítem" : " ítems")
                + "  ·  " + cant.stripTrailingZeros().toPlainString() + " unid.";
        if (peso.signum() > 0) {
            txt += "  ·  " + peso.setScale(3, RoundingMode.HALF_UP).toPlainString() + " kg";
        }
        lblResumenItems.setText(txt);
        btnUsarPeso.setDisable(peso.signum() <= 0);
    }

    @FXML
    void agregarLinea(ActionEvent e) {
        GuiaRemisionDetalle d = new GuiaRemisionDetalle(lineas.size() + 1, "", "", BigDecimal.ONE, "NIU");
        lineas.add(d);
        tblDetalle.getSelectionModel().select(d);
        tblDetalle.scrollTo(d);
        Platform.runLater(() -> tblDetalle.edit(lineas.indexOf(d), colDescripcion));
    }

    @FXML
    void quitarLinea(ActionEvent e) {
        GuiaRemisionDetalle sel = tblDetalle.getSelectionModel().getSelectedItem();
        if (sel != null) {
            lineas.remove(sel);
            renumerar();
        }
    }

    @FXML
    void usarPesoDeLineas(ActionEvent e) {
        BigDecimal peso = lineas.stream().map(GuiaRemisionDetalle::getPesoTotal)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (peso.signum() > 0) {
            cbxUndPeso.setValue("KGM");
            txtPeso.setText(peso.setScale(3, RoundingMode.HALF_UP).toPlainString());
        }
    }

    private void renumerar() {
        for (int i = 0; i < lineas.size(); i++) {
            lineas.get(i).setItemOrden(i + 1);
        }
        tblDetalle.refresh();
    }

    // =================================================================
    // CATÁLOGOS (en segundo plano)
    // =================================================================
    private record Catalogos(List<String> series, List<ItemCatalogo> motivos,
                             List<ItemCatalogo> docsRel, List<Arccdp> departamentos) {
    }

    private void cargarCatalogos() {
        ejecutar("Cargando catálogos...", () -> new Catalogos(
                dao.listarSeriesActivas(NO_CIA),
                dao.listarMotivosTraslado(),
                dao.listarDocumentosRelacionados(),
                new ArccdpDao().listarDepartamentos(NO_CIA)
        ), cat -> {
            cbxSerie.getItems().setAll(cat.series());
            if (!cat.series().isEmpty()) {
                cbxSerie.getSelectionModel().selectFirst();
            } else {
                lblNumeroHint.setText("Registre una serie en FACTU.GRE_SERIE_CTRL");
            }
            cbxMotivo.getItems().setAll(cat.motivos());
            seleccionarCodigo(cbxMotivo, "01");
            cbxTipoDocRef.getItems().setAll(cat.docsRel());
            seleccionarCodigo(cbxTipoDocRef, "01");

            List<Arccdp> deps = cat.departamentos() != null ? cat.departamentos() : List.of();
            ubigeoPartida.setDepartamentos(deps);
            ubigeoLlegada.setDepartamentos(deps);
            ubigeoPartida.seleccionarUbigeo(AppConfig.empresaUbigeo());
        });
    }

    private void mostrarCorrelativoReferencial(String serie) {
        if (serie == null) {
            lblNumeroGuia.setText("--------");
            return;
        }
        Task<Long> t = new Task<>() {
            @Override
            protected Long call() {
                return dao.siguienteCorrelativo(NO_CIA, serie);
            }
        };
        t.setOnSucceeded(e -> lblNumeroGuia.setText(t.getValue() > 0 ? String.format("%08d", t.getValue()) : "--------"));
        iniciar(t);
    }

    // =================================================================
    // DOCUMENTO ORIGEN: ARPFFE + ARPFFL
    // =================================================================
    private record CargaOrigen(GuiaRemision origen, List<GuiaRemisionDetalle> lineas,
                               String greExistente, Cliente cliente) {
    }

    /** Permite abrir el formulario ya cargado desde otra pantalla (p.ej. la factura). */
    public void cargarDesdeFactura(String noFactu) {
        txtNoFactu.setText(noFactu);
        Platform.runLater(() -> cargarDocumentoOrigen(null));
    }

    @FXML
    void cargarDocumentoOrigen(ActionEvent e) {
        String numero = txtNoFactu.getText() == null ? "" : txtNoFactu.getText().trim();
        if (numero.length() < 5) {
            marcarError(txtNoFactu, "Ingrese el número de la factura o boleta (ej. F001-00000066).");
            return;
        }
        ejecutar("Buscando " + numero + "...", () -> dao.buscarDocumentoOrigen(NO_CIA, numero), lista -> {
            if (lista.isEmpty()) {
                marcarError(txtNoFactu, "No se encontró una guía interna (FACTU.ARPFFE) para el documento " + numero + ".");
                return;
            }
            GuiaRemision elegido = lista.size() == 1 ? lista.get(0) : elegirGuiaInterna(lista);
            if (elegido != null) {
                cargarDetalle(elegido);
            }
        });
    }

    private GuiaRemision elegirGuiaInterna(List<GuiaRemision> lista) {
        List<String> opciones = new ArrayList<>();
        for (GuiaRemision g : lista) {
            opciones.add("Bodega " + g.getBodega() + "  ·  Guía " + g.getNoGuia()
                    + (g.getFecInicioTraslado() != null ? "  ·  " + FORMATO_FECHA.format(g.getFecInicioTraslado()) : ""));
        }
        ChoiceDialog<String> dlg = new ChoiceDialog<>(opciones.get(0), opciones);
        dlg.setTitle("Guías internas");
        dlg.setHeaderText("El documento tiene " + lista.size() + " guías internas.\nSeleccione la que desea trasladar:");
        dlg.setContentText("Guía:");
        dlg.initOwner(ventana());
        Optional<String> r = dlg.showAndWait();
        return r.map(s -> lista.get(opciones.indexOf(s))).orElse(null);
    }

    private void cargarDetalle(GuiaRemision origen) {
        ejecutar("Cargando detalle de la guía " + origen.getNoGuia() + "...", () -> new CargaOrigen(
                origen,
                dao.listarDetalleOrigen(origen.getNoCia(), origen.getBodega(), origen.getNoGuia()),
                dao.buscarGreExistente(origen.getNoCia(), origen.getBodega(), origen.getNoGuia()),
                origen.getNoCliente() != null ? new ClienteDao().buscarPorNumId(NO_CIA, origen.getNoCliente()) : null
        ), carga -> {
            if (carga.greExistente() != null) {
                boolean continuar = confirmar("Guía ya emitida",
                        "La guía interna " + origen.getNoGuia() + " ya tiene la GRE " + carga.greExistente()
                                + ".\n¿Desea emitir otra guía de remisión para el mismo documento?");
                if (!continuar) {
                    return;
                }
            }
            poblarFormulario(carga);
        });
    }

    private void poblarFormulario(CargaOrigen carga) {
        GuiaRemision o = carga.origen();
        Cliente cli = carga.cliente();
        guiaOrigen = o;
        actualizarOrigenVisual();

        // Fechas: el traslado no puede iniciar antes de la emisión
        LocalDate hoy = LocalDate.now();
        dpFecEmision.setValue(hoy);
        LocalDate inicio = o.getFecInicioTraslado();
        dpFecInicio.setValue(inicio == null || inicio.isBefore(hoy) ? hoy : inicio);

        // Destinatario (prioridad: maestro de clientes)
        if (cli != null) {
            seleccionarCodigo(cbxTipoDocDestin, GuiaRemisionDao.tipoDocSunat(cli.getTipoDocumento(), cli.getNoCliente()));
            txtNroDocDestin.setText(cli.getNoCliente());
            txtRazonDestin.setText(cli.getNombre());
        } else {
            seleccionarCodigo(cbxTipoDocDestin, o.getTipoDocDestin());
            txtNroDocDestin.setText(nvl(o.getNroDocDestin()));
            txtRazonDestin.setText(nvl(o.getRazonSocDestin()));
        }

        // Motivo y modalidad
        seleccionarCodigo(cbxMotivo, o.getMotivoTraslado());
        ("01".equals(o.getModalidadTraslado()) ? rbPublico : rbPrivado).setSelected(true);

        // Punto de partida
        if (!vacio(o.getPartDirec())) {
            txtPartDireccion.setText(o.getPartDirec());
        }

        // Punto de llegada
        String direccionLlegada = !vacio(o.getLlegdDireccion()) ? o.getLlegdDireccion()
                : cli != null ? nvl(cli.getDireccion()) : "";
        txtLlegDireccion.setText(direccionLlegada);
        if (cli != null && !vacio(cli.getCodiDepa()) && vacio(o.getLlegdDist())) {
            ubigeoLlegada.seleccionar(cli.getCodiDepa(), cli.getCodiProv(), cli.getCodiDist());
        } else if (!vacio(o.getLlegdDist())) {
            ubigeoLlegada.seleccionarPorNombres(o.getLlegdDepar(), o.getLlegdProv(), o.getLlegdDist());
        } else if (cli != null && !vacio(cli.getCodiDepa())) {
            ubigeoLlegada.seleccionar(cli.getCodiDepa(), cli.getCodiProv(), cli.getCodiDist());
        }

        // Transporte
        txtRucTransp.setText(nvl(o.getRucTransportista()));
        txtRazonTransp.setText(nvl(o.getRazonSocTransp()));
        txtPlaca.setText(nvl(o.getPlacaVehiculo()));
        txtMarca.setText(nvl(o.getMarcaVehiculo()));
        txtCertInscripcion.setText(nvl(o.getCertInscripcion()));
        txtLicencia.setText(nvl(o.getBrevete()));
        txtNombreConduc.setText(nvl(o.getNombreConduc()));

        // Documento relacionado
        seleccionarCodigo(cbxTipoDocRef, nvlDef(o.getTipoDocRef(), "01"));
        txtSerieRef.setText(nvl(o.getSerieDocRef()));
        txtCorrRef.setText(nvl(o.getCorrDocRef()));
        txtRucEmisorRef.setText(AppConfig.empresaRuc());

        txtObservaciones.setText(nvl(o.getObservaciones()));

        lineas.setAll(carga.lineas());
        renumerar();

        lblSubtitulo.setText("Datos cargados de " + txtNoFactu.getText().trim()
                + " · Revise el peso, los ubigeos y el transporte antes de registrar");
        Platform.runLater(() -> {
            scrollContent.setVvalue(0);
            txtPeso.requestFocus();
        });
    }

    private void actualizarOrigenVisual() {
        lblOrigen.getStyleClass().remove("chip-origen-vacio");
        if (guiaOrigen == null) {
            lblOrigen.setText("Sin documento cargado");
            lblOrigen.getStyleClass().add("chip-origen-vacio");
        } else {
            lblOrigen.setText("✓  Bodega " + guiaOrigen.getBodega() + "  ·  Guía " + guiaOrigen.getNoGuia());
        }
    }

    @FXML
    void buscarDestinatario(ActionEvent e) {
        String nro = txtNroDocDestin.getText() == null ? "" : txtNroDocDestin.getText().trim();
        if (nro.isEmpty()) {
            marcarError(txtNroDocDestin, "Ingrese el número de documento del destinatario.");
            return;
        }
        ejecutar("Buscando cliente...", () -> Optional.ofNullable(new ClienteDao().buscarPorNumId(NO_CIA, nro)), opt -> {
            if (opt.isEmpty()) {
                Mensaje.alerta(null, "Destinatario", "El documento " + nro
                        + " no está en el maestro de clientes.\nPuede escribir la razón social manualmente.");
                txtRazonDestin.requestFocus();
                return;
            }
            Cliente c = opt.get();
            seleccionarCodigo(cbxTipoDocDestin, GuiaRemisionDao.tipoDocSunat(c.getTipoDocumento(), nro));
            txtRazonDestin.setText(c.getNombre());
            if (vacio(txtLlegDireccion.getText())) {
                txtLlegDireccion.setText(nvl(c.getDireccion()));
            }
            if (vacio(txtLlegUbigeo.getText()) && !vacio(c.getCodiDepa())) {
                ubigeoLlegada.seleccionar(c.getCodiDepa(), c.getCodiProv(), c.getCodiDist());
            }
        });
    }

    // =================================================================
    // REGLAS DINÁMICAS (motivo / modalidad)
    // =================================================================
    private void actualizarPorMotivo() {
        String motivo = codigo(cbxMotivo);
        mostrar(vbxDescMotivo, "13".equals(motivo));
        mostrar(vbxComprador, "03".equals(motivo));
        boolean entreEstablecimientos = "04".equals(motivo);
        mostrar(vbxPartEstab, entreEstablecimientos);
        mostrar(vbxLlegEstab, entreEstablecimientos);
        if (entreEstablecimientos && vacio(txtNroDocDestin.getText())) {
            seleccionarCodigo(cbxTipoDocDestin, "6");
            txtNroDocDestin.setText(AppConfig.empresaRuc());
            txtRazonDestin.setText(AppConfig.empresaRazonSocial());
        }
    }

    private void actualizarModalidad() {
        boolean publico = rbPublico.isSelected();
        boolean m1l = chkM1L.isSelected();
        mostrar(vbxPublico, publico);
        mostrar(vbxPrivado, !publico);
        vbxPrivado.setDisable(!publico && m1l);
        lblModalidadInfo.setText(publico
                ? "Transporte público: datos de la empresa de transportes"
                : m1l ? "Vehículo M1 o L: placa y conductor no son obligatorios"
                : "Transporte privado: vehículo y conductor");
    }

    // =================================================================
    // REGISTRO
    // =================================================================
    @FXML
    void registrarGuia(ActionEvent e) {
        if (!validar()) {
            return;
        }
        GuiaRemision g = construirGuia();
        String mensaje = "Se registrará la guía de remisión serie " + g.getSerieElect()
                + " para " + g.getRazonSocDestin() + " con " + g.getLineas().size() + " ítem(s).\n\n¿Desea continuar?";
        if (!confirmar("Registrar guía", mensaje)) {
            return;
        }
        boolean imprimir = chkImprimir.isSelected();
        ejecutar("Registrando guía de remisión...", () -> dao.registrar(g), guia -> {
            if (onGuiaRegistrada != null) {
                onGuiaRegistrada.accept(guia);
            }
            Mensaje.alerta(null, "Guía registrada",
                    "Se registró la guía " + guia.getNumeroCompleto() + ".\n"
                            + "Quedó PENDIENTE de envío a SUNAT.");
            if (imprimir) {
                imprimirEnSegundoPlano(guia.getIdGuia());
            }
            cerrarSinPreguntar();
        });
    }

    private GuiaRemision construirGuia() {
        GuiaRemision g = new GuiaRemision();
        g.setNoCia(NO_CIA);
        g.setBodega(guiaOrigen.getBodega());
        g.setNoGuia(guiaOrigen.getNoGuia());
        g.setSerieElect(cbxSerie.getValue());
        g.setFecEmision(dpFecEmision.getValue());
        g.setFecInicioTraslado(dpFecInicio.getValue());
        g.setObservaciones(texto(txtObservaciones));
        g.setUsuario(usuario);

        g.setRucRemit(AppConfig.empresaRuc());
        g.setRazonSocRemit(AppConfig.empresaRazonSocial());

        g.setTipoDocDestin(codigo(cbxTipoDocDestin));
        g.setNroDocDestin(texto(txtNroDocDestin));
        g.setRazonSocDestin(texto(txtRazonDestin));

        g.setMotivoTraslado(codigo(cbxMotivo));
        g.setDescMotivo("13".equals(g.getMotivoTraslado()) ? texto(txtDescMotivo) : null);
        if ("03".equals(g.getMotivoTraslado())) {
            g.setTipoDocCompr(codigo(cbxTipoDocCompr));
            g.setNroDocCompr(texto(txtNroDocCompr));
            g.setRazonSocCompr(texto(txtRazonCompr));
        }
        g.setIndTransbordo(chkTransbordo.isSelected() ? "S" : "N");
        g.setIndVehiculoM1L(chkM1L.isSelected() ? "S" : "N");
        g.setPesoBrutoTotal(decimal(txtPeso.getText()));
        g.setUndPeso(cbxUndPeso.getValue());
        g.setNroBultos(vacio(txtBultos.getText()) ? null : Integer.valueOf(txtBultos.getText().trim()));
        g.setModalidadTraslado((String) grupoModalidad.getSelectedToggle().getUserData());

        g.setRucTransportista(texto(txtRucTransp));
        g.setRazonSocTransp(texto(txtRazonTransp));
        g.setMtcTransportista(texto(txtMtc));
        g.setPlacaVehiculo(GuiaRemisionDao.limpiarPlaca(texto(txtPlaca)));
        g.setMarcaVehiculo(texto(txtMarca));
        g.setCertInscripcion(texto(txtCertInscripcion));
        g.setTipoDocConduc(codigo(cbxTipoDocConduc));
        g.setNroDocConduc(texto(txtNroDocConduc));
        g.setNombreConduc(texto(txtNombreConduc));
        g.setApellidoConduc(texto(txtApellidoConduc));
        g.setBrevete(texto(txtLicencia));

        g.setPartUbigeo(texto(txtPartUbigeo));
        g.setPartDirec(texto(txtPartDireccion));
        g.setLlegdUbigeo(texto(txtLlegUbigeo));
        g.setLlegdDireccion(texto(txtLlegDireccion));
        g.setLlegdDist(ubigeoLlegada.nombreDistrito());
        g.setLlegdProv(ubigeoLlegada.nombreProvincia());
        g.setLlegdDepar(ubigeoLlegada.nombreDepartamento());
        if ("04".equals(g.getMotivoTraslado())) {
            g.setPartCodEstab(texto(txtPartCodEstab));
            g.setLlegdCodEstab(texto(txtLlegCodEstab));
        }

        if (!vacio(txtSerieRef.getText())) {
            g.setTipoDocRef(codigo(cbxTipoDocRef));
            g.setSerieDocRef(texto(txtSerieRef));
            g.setCorrDocRef(texto(txtCorrRef));
            g.setRucEmisorDocRef(texto(txtRucEmisorRef));
            g.setNoFactuRef(guiaOrigen.getNoFactuRef());
        }

        List<GuiaRemisionDetalle> copia = new ArrayList<>(lineas);
        g.setLineas(copia);
        return g;
    }

    /** Validaciones según la GRE 2022 (RS 123-2022/SUNAT). Marca el primer campo con error. */
    private boolean validar() {
        if (guiaOrigen == null) {
            return marcarError(txtNoFactu, "Primero cargue la factura o boleta: la guía se vincula a su guía interna (FACTU.ARPFFE).");
        }
        if (cbxSerie.getValue() == null) {
            return marcarError(cbxSerie, "Seleccione la serie de la guía de remisión.");
        }

        // Fechas
        LocalDate emision = dpFecEmision.getValue();
        LocalDate inicio = dpFecInicio.getValue();
        if (emision == null) {
            return marcarError(dpFecEmision, "Ingrese la fecha de emisión.");
        }
        if (emision.isAfter(LocalDate.now())) {
            return marcarError(dpFecEmision, "La fecha de emisión no puede ser posterior a hoy.");
        }
        if (inicio == null) {
            return marcarError(dpFecInicio, "Ingrese la fecha de inicio del traslado.");
        }
        if (inicio.isBefore(emision)) {
            return marcarError(dpFecInicio, "La fecha de inicio del traslado no puede ser anterior a la fecha de emisión.");
        }

        // Destinatario
        String tipoDest = codigo(cbxTipoDocDestin);
        String nroDest = texto(txtNroDocDestin);
        if (tipoDest == null) {
            return marcarError(cbxTipoDocDestin, "Seleccione el tipo de documento del destinatario.");
        }
        String errorDoc = validarDocumento(tipoDest, nroDest);
        if (errorDoc != null) {
            return marcarError(txtNroDocDestin, "Destinatario: " + errorDoc);
        }
        if (vacio(txtRazonDestin.getText())) {
            return marcarError(txtRazonDestin, "Ingrese el nombre o razón social del destinatario.");
        }

        // Motivo
        String motivo = codigo(cbxMotivo);
        if (motivo == null) {
            return marcarError(cbxMotivo, "Seleccione el motivo de traslado.");
        }
        if ("13".equals(motivo) && vacio(txtDescMotivo.getText())) {
            return marcarError(txtDescMotivo, "Para el motivo 13 - Otros debe describir el motivo.");
        }
        if ("04".equals(motivo)) {
            if (!AppConfig.empresaRuc().equals(nroDest)) {
                return marcarError(txtNroDocDestin, "En el traslado entre establecimientos el destinatario debe ser la misma empresa (RUC "
                        + AppConfig.empresaRuc() + ").");
            }
            if (!texto(txtPartCodEstab, "").matches("\\d{4}")) {
                return marcarError(txtPartCodEstab, "Ingrese el código de establecimiento SUNAT (4 dígitos) del punto de partida.");
            }
            if (!texto(txtLlegCodEstab, "").matches("\\d{4}")) {
                return marcarError(txtLlegCodEstab, "Ingrese el código de establecimiento SUNAT (4 dígitos) del punto de llegada.");
            }
        }
        if ("03".equals(motivo)) {
            String err = validarDocumento(codigo(cbxTipoDocCompr), texto(txtNroDocCompr));
            if (err != null) {
                return marcarError(txtNroDocCompr, "Comprador: " + err);
            }
            if (vacio(txtRazonCompr.getText())) {
                return marcarError(txtRazonCompr, "Ingrese la razón social del comprador.");
            }
        }

        // Peso
        BigDecimal peso = decimal(txtPeso.getText());
        if (peso == null || peso.signum() <= 0) {
            return marcarError(txtPeso, "Ingrese el peso bruto total de la carga (mayor a cero).");
        }

        // Partida y llegada
        if (vacio(txtPartDireccion.getText())) {
            return marcarError(txtPartDireccion, "Ingrese la dirección del punto de partida.");
        }
        if (!texto(txtPartUbigeo, "").matches("\\d{6}")) {
            return marcarError(txtPartUbigeo, "El ubigeo del punto de partida debe tener 6 dígitos.");
        }
        if (vacio(txtLlegDireccion.getText())) {
            return marcarError(txtLlegDireccion, "Ingrese la dirección del punto de llegada.");
        }
        if (!texto(txtLlegUbigeo, "").matches("\\d{6}")) {
            return marcarError(txtLlegUbigeo, "El ubigeo del punto de llegada debe tener 6 dígitos.");
        }
        if (texto(txtPartUbigeo).equals(texto(txtLlegUbigeo))
                && texto(txtPartDireccion).equalsIgnoreCase(texto(txtLlegDireccion))) {
            return marcarError(txtLlegDireccion, "El punto de llegada no puede ser igual al punto de partida.");
        }

        // Transporte
        if (rbPublico.isSelected()) {
            String ruc = texto(txtRucTransp, "");
            if (!ruc.matches("\\d{11}")) {
                return marcarError(txtRucTransp, "El RUC del transportista debe tener 11 dígitos.");
            }
            if (ruc.equals(AppConfig.empresaRuc())) {
                return marcarError(txtRucTransp, "En transporte público el transportista no puede ser el mismo remitente. Use transporte privado.");
            }
            if (vacio(txtRazonTransp.getText())) {
                return marcarError(txtRazonTransp, "Ingrese la razón social del transportista.");
            }
        } else if (!chkM1L.isSelected()) {
            String placa = GuiaRemisionDao.limpiarPlaca(texto(txtPlaca, ""));
            if (!placa.matches("[A-Z0-9]{6,8}")) {
                return marcarError(txtPlaca, "Ingrese una placa válida (6 a 8 caracteres, sin guiones).");
            }
            String errCond = validarDocumento(codigo(cbxTipoDocConduc), texto(txtNroDocConduc));
            if (errCond != null) {
                return marcarError(txtNroDocConduc, "Conductor: " + errCond);
            }
            if (vacio(txtNombreConduc.getText())) {
                return marcarError(txtNombreConduc, "Ingrese los nombres del conductor.");
            }
            if (vacio(txtApellidoConduc.getText())) {
                return marcarError(txtApellidoConduc, "Ingrese los apellidos del conductor.");
            }
            if (!texto(txtLicencia, "").matches("[A-Z0-9-]{9,10}")) {
                return marcarError(txtLicencia, "La licencia de conducir debe tener entre 9 y 10 caracteres (ej. Q12345678).");
            }
        }

        // Documento relacionado
        if (!vacio(txtSerieRef.getText()) && vacio(txtCorrRef.getText())) {
            return marcarError(txtCorrRef, "Ingrese el número del documento relacionado.");
        }

        // Bienes
        if (lineas.isEmpty()) {
            return marcarError(tblDetalle, "Agregue al menos un bien a transportar.");
        }
        for (GuiaRemisionDetalle d : lineas) {
            if (vacio(d.getDescripcion())) {
                tblDetalle.getSelectionModel().select(d);
                return marcarError(tblDetalle, "El ítem " + d.getItemOrden() + " no tiene descripción.");
            }
            if (d.getCantidad() == null || d.getCantidad().signum() <= 0) {
                tblDetalle.getSelectionModel().select(d);
                return marcarError(tblDetalle, "La cantidad del ítem " + d.getItemOrden() + " debe ser mayor a cero.");
            }
            if (vacio(d.getUndMedida())) {
                tblDetalle.getSelectionModel().select(d);
                return marcarError(tblDetalle, "Seleccione la unidad de medida del ítem " + d.getItemOrden() + ".");
            }
        }
        return true;
    }

    /** @return mensaje de error o null si el documento es válido. */
    private static String validarDocumento(String tipo, String numero) {
        if (tipo == null) {
            return "seleccione el tipo de documento.";
        }
        String n = numero == null ? "" : numero.trim();
        return switch (tipo) {
            case "6" -> n.matches("(10|15|16|17|20)\\d{9}") ? null : "el RUC debe tener 11 dígitos y empezar con 10, 15, 16, 17 o 20.";
            case "1" -> n.matches("\\d{8}") ? null : "el DNI debe tener 8 dígitos.";
            case "4" -> n.matches("[A-Z0-9]{8,12}") ? null : "el carnet de extranjería debe tener entre 8 y 12 caracteres.";
            case "7" -> n.matches("[A-Z0-9]{5,12}") ? null : "el pasaporte debe tener entre 5 y 12 caracteres.";
            default -> n.isEmpty() ? "ingrese el número de documento." : null;
        };
    }

    private void imprimirEnSegundoPlano(long idGuia) {
        Thread t = new Thread(() -> {
            try {
                new GuiaRemisionReporte().abrirPdf(idGuia);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error al imprimir la guía " + idGuia, ex);
                Platform.runLater(() -> Mensaje.error(null, "Impresión",
                        "La guía se registró, pero no se pudo generar el PDF:\n" + ex.getMessage()));
            }
        }, "imprimir-guia");
        t.setDaemon(true);
        t.start();
    }

    // =================================================================
    // LIMPIAR / CERRAR / ATAJOS
    // =================================================================
    @FXML
    public void limpiarFormulario(ActionEvent e) {
        guiaOrigen = null;
        actualizarOrigenVisual();
        for (TextInputControl t : List.of(txtNoFactu, txtNroDocDestin, txtRazonDestin, txtDescMotivo, txtPeso,
                txtBultos, txtPartCodEstab, txtLlegDireccion, txtLlegUbigeo, txtLlegCodEstab, txtRucTransp,
                txtRazonTransp, txtMtc, txtPlaca, txtMarca, txtCertInscripcion, txtNroDocConduc, txtLicencia,
                txtNombreConduc, txtApellidoConduc, txtSerieRef, txtCorrRef, txtNroDocCompr, txtRazonCompr,
                txtObservaciones)) {
            t.clear();
            t.getStyleClass().remove(ESTILO_ERROR);
        }
        chkTransbordo.setSelected(false);
        chkM1L.setSelected(false);
        lineas.clear();
        ubigeoLlegada.limpiar();
        ubigeoPartida.seleccionarUbigeo(AppConfig.empresaUbigeo());
        seleccionarCodigo(cbxMotivo, "01");
        seleccionarCodigo(cbxTipoDocRef, "01");
        valoresPorDefecto();
        lblSubtitulo.setText("Remitente · Cargue la factura o boleta para completar los datos del traslado");
        txtNoFactu.requestFocus();
    }

    @FXML
    void cerrarVentana(ActionEvent e) {
        boolean hayDatos = guiaOrigen != null || !lineas.isEmpty();
        if (hayDatos && !confirmar("Cerrar", "Se perderán los datos de la guía que no ha registrado.\n¿Desea cerrar?")) {
            return;
        }
        cerrarSinPreguntar();
    }

    private void cerrarSinPreguntar() {
        if (onCerrar != null) {
            onCerrar.run();
            return;
        }
        Window w = ventana();
        if (w instanceof Stage s) {
            s.close();
        }
    }

    private void configurarAtajos() {
        rootStack.sceneProperty().addListener((o, a, scene) -> {
            if (scene != null) {
                registrarAtajos(scene);
            }
        });
    }

    private void registrarAtajos(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> {
            if (overlayCarga.isVisible()) {
                return;
            }
            if (ev.getCode() == KeyCode.ESCAPE && tblDetalle.getEditingCell() == null) {
                cerrarVentana(null);
                ev.consume();
            } else if (ev.isControlDown() && ev.getCode() == KeyCode.S) {
                registrarGuia(null);
                ev.consume();
            } else if (ev.getCode() == KeyCode.F3) {
                cargarDocumentoOrigen(null);
                ev.consume();
            }
        });
    }

    // =================================================================
    // API PÚBLICA
    // =================================================================

    /**
     * Abre el formulario como ventana modal ajustada a la pantalla.
     * Lo usan el Dashboard y la lista de guías.
     *
     * @param owner       ventana padre (puede ser null)
     * @param usuario     usuario de la sesión (se graba en ARGUIA.USUARIO / USU_CREA)
     * @param onRegistrada acción al registrar (p.ej. refrescar la lista); puede ser null
     */
    public static GuiaRemisionController abrirVentana(Window owner, String usuario,
                                                      Consumer<GuiaRemision> onRegistrada) throws java.io.IOException {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                GuiaRemisionController.class.getResource("/com/robin/pos/fxml/GuiaRemision.fxml"));
        javafx.scene.Parent vista = loader.load();
        GuiaRemisionController ctrl = loader.getController();
        ctrl.setUsuario(usuario);
        ctrl.setOnGuiaRegistrada(onRegistrada);

        javafx.geometry.Rectangle2D pantalla = javafx.stage.Screen.getPrimary().getVisualBounds();
        Stage stage = new Stage();
        stage.setTitle("Guía de Remisión Electrónica Remitente");
        stage.setScene(new Scene(vista,
                Math.min(1180, pantalla.getWidth() - 40), Math.min(800, pantalla.getHeight() - 40)));
        stage.setMinWidth(980);
        stage.setMinHeight(600);
        if (owner != null) {
            stage.initOwner(owner);
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        }
        try (java.io.InputStream icono = GuiaRemisionController.class
                .getResourceAsStream("/com/robin/pos/imagenes/icono_guia_azul.png")) {
            if (icono != null) {
                stage.getIcons().add(new Image(icono));
            }
        }
        stage.show();
        return ctrl;
    }

    public void setUsuario(String usuario) {
        if (usuario != null && !usuario.isBlank()) {
            this.usuario = usuario;
        }
    }

    /** Se invoca con la guía recién registrada (p.ej. para refrescar la lista). */
    public void setOnGuiaRegistrada(Consumer<GuiaRemision> callback) {
        this.onGuiaRegistrada = callback;
    }

    /** Acción al cerrar si el formulario está dentro de una pestaña en lugar de una ventana. */
    public void setOnCerrar(Runnable onCerrar) {
        this.onCerrar = onCerrar;
    }

    // =================================================================
    // UTILIDADES
    // =================================================================
    /** Ejecuta un trabajo de BD en segundo plano mostrando la capa de carga. */
    private <T> void ejecutar(String mensaje, Callable<T> trabajo, Consumer<T> alTerminar) {
        lblCargando.setText(mensaje);
        overlayCarga.setVisible(true);
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return trabajo.call();
            }
        };
        task.setOnSucceeded(ev -> {
            overlayCarga.setVisible(false);
            alTerminar.accept(task.getValue());
        });
        task.setOnFailed(ev -> {
            overlayCarga.setVisible(false);
            Throwable ex = task.getException();
            LOGGER.log(Level.SEVERE, mensaje, ex);
            Mensaje.error(null, "Error", mensajeError(ex));
        });
        iniciar(task);
    }

    private static void iniciar(Task<?> task) {
        Thread t = new Thread(task, "guia-remision-bd");
        t.setDaemon(true);
        t.start();
    }

    private static String mensajeError(Throwable ex) {
        if (ex instanceof SQLException sql) {
            String m = sql.getMessage() == null ? "" : sql.getMessage();
            if (m.contains("ORA-00001")) {
                return "El número de guía ya existe. Vuelva a intentarlo para tomar el siguiente correlativo.";
            }
            if (m.contains("ORA-02290")) {
                return "Algún dato no cumple las reglas de la tabla FACTU.ARGUIA (serie T###, estado, modalidad...).\n\n" + m;
            }
            if (m.contains("ORA-00942") || m.contains("ORA-01031")) {
                return "El usuario de BD no tiene acceso a las tablas de la guía (FACTU.ARGUIA / ARPFFE).\n\n" + m;
            }
            return "Error de base de datos:\n" + m;
        }
        return ex == null ? "Error desconocido" : String.valueOf(ex.getMessage());
    }

    private boolean marcarError(Control campo, String mensaje) {
        if (campo != null && !campo.getStyleClass().contains(ESTILO_ERROR)) {
            campo.getStyleClass().add(ESTILO_ERROR);
        }
        Mensaje.alerta(null, "Validación", mensaje);
        if (campo != null) {
            Platform.runLater(() -> {
                enfocarEnScroll(campo);
                campo.requestFocus();
            });
        }
        return false;
    }

    private void enfocarEnScroll(Control campo) {
        if (!scrollContent.getContent().getLayoutBounds().isEmpty()
                && campo.getScene() != null && scrollContent.getContent().localToScene(0, 0) != null) {
            double alto = scrollContent.getContent().getBoundsInLocal().getHeight();
            double y = scrollContent.getContent().sceneToLocal(campo.localToScene(0, 0)).getY();
            double visible = scrollContent.getViewportBounds().getHeight();
            if (alto > visible) {
                scrollContent.setVvalue(Math.max(0, Math.min(1, (y - 40) / (alto - visible))));
            }
        }
    }

    private void limpiarErrorAlEditar(Control... campos) {
        for (Control c : campos) {
            c.focusedProperty().addListener((o, a, foco) -> {
                if (!foco) {
                    return;
                }
                c.getStyleClass().remove(ESTILO_ERROR);
            });
        }
        tblDetalle.focusedProperty().addListener((o, a, f) -> tblDetalle.getStyleClass().remove(ESTILO_ERROR));
    }

    private boolean confirmar(String titulo, String mensaje) {
        Optional<ButtonType> r = Mensaje.confirmacion(null, titulo, mensaje);
        return r != null && r.isPresent() && r.get() == ButtonType.OK;
    }

    private Window ventana() {
        return rootStack.getScene() != null ? rootStack.getScene().getWindow() : null;
    }

    private static void mostrar(javafx.scene.Node n, boolean visible) {
        n.setVisible(visible);
        n.setManaged(visible);
    }

    private static TextFormatter<String> formato(String regex, boolean mayusculas) {
        UnaryOperator<TextFormatter.Change> filtro = c -> {
            if (mayusculas && c.getText() != null) {
                c.setText(c.getText().toUpperCase());
            }
            return c.getControlNewText().matches(regex) ? c : null;
        };
        return new TextFormatter<>(filtro);
    }

    private static void seleccionarCodigo(ComboBox<ItemCatalogo> combo, String codigo) {
        if (codigo == null) {
            return;
        }
        combo.getItems().stream().filter(i -> i.getCodigo().equals(codigo)).findFirst()
                .ifPresent(i -> combo.getSelectionModel().select(i));
    }

    private static String codigo(ComboBox<ItemCatalogo> combo) {
        return combo.getValue() == null ? null : combo.getValue().getCodigo();
    }

    private static String texto(TextInputControl t) {
        return t.getText() == null || t.getText().isBlank() ? null : t.getText().trim();
    }

    private static String texto(TextInputControl t, String def) {
        String v = texto(t);
        return v == null ? def : v;
    }

    private static BigDecimal decimal(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(s.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String mayus(String s) {
        return s == null ? "" : s.trim().toUpperCase();
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String nvlDef(String s, String def) {
        return vacio(s) ? def : s;
    }

    /** Conversor BigDecimal para las celdas editables (acepta coma o punto). */
    private static final class DecimalConverter extends StringConverter<BigDecimal> {
        private final int decimales;

        DecimalConverter(int decimales) {
            this.decimales = decimales;
        }

        @Override
        public String toString(BigDecimal v) {
            return v == null ? "" : v.stripTrailingZeros().scale() > decimales
                    ? v.setScale(decimales, RoundingMode.HALF_UP).toPlainString()
                    : v.stripTrailingZeros().toPlainString();
        }

        @Override
        public BigDecimal fromString(String s) {
            BigDecimal v = decimal(s);
            return v == null ? null : v.setScale(Math.min(Math.max(v.scale(), 0), decimales), RoundingMode.HALF_UP);
        }
    }



    // =================================================================
    // SELECTOR DE UBIGEO (Departamento / Provincia / Distrito -> código INEI)
    // =================================================================
    private final class UbigeoSelector {
        private final ComboBox<Arccdp> dep;
        private final ComboBox<Arccpr> prov;
        private final ComboBox<Arccdi> dist;
        private final TextField codigo;
        private boolean programatico;

        UbigeoSelector(ComboBox<Arccdp> dep, ComboBox<Arccpr> prov, ComboBox<Arccdi> dist, TextField codigo) {
            this.dep = dep;
            this.prov = prov;
            this.dist = dist;
            this.codigo = codigo;
            configurar(dep, Arccdp::getDesDepa);
            configurar(prov, Arccpr::getDescProv);
            configurar(dist, Arccdi::getDescDist);

            dep.valueProperty().addListener((o, a, n) -> {
                if (!programatico) {
                    cargarProvincias(n);
                    if (!prov.getItems().isEmpty()) {
                        prov.getSelectionModel().selectFirst();
                    }
                }
            });
            prov.valueProperty().addListener((o, a, n) -> {
                if (!programatico) {
                    cargarDistritos(dep.getValue(), n);
                    if (!dist.getItems().isEmpty()) {
                        dist.getSelectionModel().selectFirst();
                    }
                }
            });
            dist.valueProperty().addListener((o, a, n) -> {
                if (!programatico) {
                    actualizarCodigo();
                }
            });
        }

        private <T> void configurar(ComboBox<T> combo, Function<T, String> texto) {
            combo.setConverter(new StringConverter<>() {
                @Override
                public String toString(T item) {
                    return item == null ? "" : texto.apply(item);
                }

                @Override
                public T fromString(String s) {
                    return null;
                }
            });
            combo.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : texto.apply(item));
                }
            });
            combo.setVisibleRowCount(12);
        }

        void setDepartamentos(List<Arccdp> lista) {
            programatico = true;
            dep.getItems().setAll(lista);
            programatico = false;
        }

        private void cargarProvincias(Arccdp d) {
            prov.getItems().clear();
            dist.getItems().clear();
            if (d != null) {
                List<Arccpr> l = new ArccprDao().listaProvincias(NO_CIA, d.getCodDepa());
                if (l != null) {
                    prov.getItems().setAll(l);
                }
            }
        }

        private void cargarDistritos(Arccdp d, Arccpr p) {
            dist.getItems().clear();
            if (d != null && p != null) {
                List<Arccdi> l = new ArccdiDao().listaDistrito(NO_CIA, d.getCodDepa(), p.getCodiProv());
                if (l != null) {
                    dist.getItems().setAll(l);
                }
            }
        }

        /** Ubigeo INEI = DD + PP + dd (se toman los 2 últimos dígitos de cada código). */
        private void actualizarCodigo() {
            if (dep.getValue() != null && prov.getValue() != null && dist.getValue() != null) {
                String d = dos(dep.getValue().getCodDepa());
                String p = dos(prov.getValue().getCodiProv());
                String di = dist.getValue().getCodiDist();
                codigo.setText(di != null && di.trim().length() == 6 ? di.trim() : d + p + dos(di));
            }
        }

        void seleccionar(String codDepa, String codProv, String codDist) {
            programatico = true;
            try {
                Arccdp d = dep.getItems().stream().filter(x -> dos(x.getCodDepa()).equals(dos(codDepa))).findFirst().orElse(null);
                dep.setValue(d);
                cargarProvincias(d);
                Arccpr p = prov.getItems().stream().filter(x -> dos(x.getCodiProv()).equals(dos(codProv))).findFirst().orElse(null);
                prov.setValue(p);
                cargarDistritos(d, p);
                Arccdi di = dist.getItems().stream().filter(x -> dos(x.getCodiDist()).equals(dos(codDist))).findFirst().orElse(null);
                dist.setValue(di);
            } finally {
                programatico = false;
            }
            actualizarCodigo();
        }

        void seleccionarUbigeo(String ubigeo) {
            if (ubigeo != null && ubigeo.trim().matches("\\d{6}")) {
                String u = ubigeo.trim();
                seleccionar(u.substring(0, 2), u.substring(2, 4), u.substring(4, 6));
                if (vacio(codigo.getText())) {
                    codigo.setText(u);
                }
            }
        }

        void seleccionarPorNombres(String nomDep, String nomProv, String nomDist) {
            programatico = true;
            try {
                Arccdp d = dep.getItems().stream().filter(x -> igual(x.getDesDepa(), nomDep)).findFirst().orElse(null);
                if (d == null) {
                    return;
                }
                dep.setValue(d);
                cargarProvincias(d);
                Arccpr p = prov.getItems().stream().filter(x -> igual(x.getDescProv(), nomProv)).findFirst().orElse(null);
                prov.setValue(p);
                cargarDistritos(d, p);
                Arccdi di = dist.getItems().stream().filter(x -> igual(x.getDescDist(), nomDist)).findFirst().orElse(null);
                dist.setValue(di);
            } finally {
                programatico = false;
            }
            actualizarCodigo();
        }

        void limpiar() {
            programatico = true;
            dep.setValue(null);
            prov.getItems().clear();
            dist.getItems().clear();
            codigo.clear();
            programatico = false;
        }

        String nombreDepartamento() {
            return dep.getValue() == null ? null : dep.getValue().getDesDepa();
        }

        String nombreProvincia() {
            return prov.getValue() == null ? null : prov.getValue().getDescProv();
        }

        String nombreDistrito() {
            return dist.getValue() == null ? null : dist.getValue().getDescDist();
        }

        private static String dos(String s) {
            if (s == null) {
                return "";
            }
            String t = s.trim();
            if (t.length() >= 2) {
                return t.substring(t.length() - 2);
            }
            return t.length() == 1 ? "0" + t : t;
        }

        private static boolean igual(String a, String b) {
            return a != null && b != null && normal(a).equals(normal(b));
        }

        private static String normal(String s) {
            return java.text.Normalizer.normalize(s.trim().toUpperCase(), java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");
        }
    }

    // Menú "Guía Remisión" > "Nueva Guía"  (ventana modal)
    @FXML
    void ingresarGuiaRemision(ActionEvent event) {
        try {
            GuiaRemisionController.abrirVentana(
                    tabPane.getScene().getWindow(),
                    lblUsuario.getText(),
                    guia -> refrescarListaGuiasSiEstaAbierta());
        } catch (Exception e) {
            e.printStackTrace();
            Mensaje.error(null, "Error", "No se pudo abrir la guía de remisión:\n" + e.getMessage());
        }
    }

    // Menú "Guía Remisión" > "Lista de Guías"  (pestaña)
    @FXML
    void ingresarListaGuias(ActionEvent event) {
        ListaGuiaRemisionController ctrl = abrirPestana("Guías de Remisión",
                "/com/robin/pos/view/ListaGuiaRemision.fxml");
        if (ctrl != null) {
            ctrl.setUsuario(lblUsuario.getText());
        }
    }

    // Si el submenú de guía aún no tiene su toggle, este es el mismo patrón que Ventas:
    @FXML
    void toggleGuiaSubmenu(ActionEvent event) {
        boolean abrir = !guiaSubmenu.isVisible();
        guiaSubmenu.setVisible(abrir);
        guiaSubmenu.setManaged(abrir);
        lblGuiaArrow.setText(abrir ? "▼" : "▶");
    }

    // ---------- utilidades ----------

    // Abre (o selecciona si ya existe) una pestaña con el FXML indicado y devuelve su controlador.
    private <T> T abrirPestana(String titulo, String rutaFxml) {
        for (Tab t : tabPane.getTabs()) {
            if (titulo.equals(t.getText())) {
                tabPane.getSelectionModel().select(t);
                return null;
            }
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            Parent vista = loader.load();
            Tab tab = new Tab(titulo, vista);
            tab.setUserData(loader.getController());
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            return loader.getController();
        } catch (Exception e) {
            e.printStackTrace();
            Mensaje.error(null, "Error", "No se pudo abrir " + titulo + ":\n" + e.getMessage());
            return null;
        }
    }

    // Refresca la pestaña "Guías de Remisión" si está abierta.
    private void refrescarListaGuiasSiEstaAbierta() {
        for (Tab t : tabPane.getTabs()) {
            if (t.getUserData() instanceof ListaGuiaRemisionController lista) {
                lista.refrescar();
            }
        }
    }


}
