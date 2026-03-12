package com.robin.pos.controller;

import com.robin.pos.dao.ArfamcDao;
import com.robin.pos.dao.RegistroVentaDao;
import com.robin.pos.dao.SucursalPtovtaDao;
import com.robin.pos.model.*;
import com.robin.pos.util.FormCargar;
import com.robin.pos.util.GestorDescargas;
import com.robin.pos.util.Mensaje;
import com.robin.pos.util.Metodos;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.*;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para el formulario de Registro de Ventas
 * Con integración de datos de empresa desde BD
 *
 * @author Robin POS
 * @version 2.0
 */
public class RegistroVentaController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(RegistroVentaController.class.getName());
    private static final String NO_CIA = "01";
    private static final String LOGO_PATH = "/com/robin/pos/imagenes/logos-nexer.png";

    // Rutas de reportes
    private static final String REPORTE_PDF_PATH = "/com/robin/pos/reportes/registroVentaPdf.jasper";
    private static final String REPORTE_XLS_PATH = "/com/robin/pos/reportes/registroVentaXls.jasper";

    // ==================== CAMPOS FXML - PERÍODO ====================
    @FXML private VBox vbxPrincipal;
    @FXML private Label lblEstado;

    @FXML private DatePicker dpFechaDesde;
    @FXML private DatePicker dpFechaHasta;
    @FXML private ComboBox<String> cbxTipoDocumento;

    // ==================== CAMPOS FXML - OPCIONES ====================

    @FXML private RadioButton rbImpresora;
    @FXML private RadioButton rbPdf;
    @FXML private RadioButton rbExcel;
    @FXML private ToggleGroup grupoDestino;

    @FXML private RadioButton rbSoles;
    @FXML private RadioButton rbDolares;
    @FXML private ToggleGroup grupoMoneda;

    @FXML private RadioButton rbFormatoA4;
    @FXML private TextField txtRutaArchivo;
    @FXML private Button btnExaminar;
    @FXML private Button btnProcesar;

    // ==================== VARIABLES DE INSTANCIA ====================

    private final RegistroVentaDao registroVentaDao = new RegistroVentaDao();
    private final ArfamcDao arfamcDao = new ArfamcDao();
    private final SucursalPtovtaDao sucursalDao = new SucursalPtovtaDao();

    private String userId;
    private Arfamc datosEmpresa;
    private SucursalPtovta datosSucursal;

    // ==================== INITIALIZE ====================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Inicializando RegistroVentaController");

        cargarDatosEmpresa();
        configurarComboBoxes();
        configurarDatePickers();
        configurarListeners();
        //configurarAtajos();
        establecerRutaPorDefecto();

        LOGGER.info("RegistroVentaController inicializado correctamente");
    }

    /**
     * Carga los datos de la empresa desde la BD
     */
    private void cargarDatosEmpresa() {
        try {
            datosEmpresa = arfamcDao.obtenerDatosCompania(NO_CIA);
            if (datosEmpresa == null) {
                LOGGER.warning("No se encontraron datos de la empresa");
                datosEmpresa = new Arfamc(); // Objeto vacío para evitar NPE
            }

            List<SucursalPtovta> sucursales = sucursalDao.listarSucursales(NO_CIA);
            if (!sucursales.isEmpty()) {
                datosSucursal = sucursales.stream()
                        .filter(s -> "A".equals(s.getEstadoSuc()))
                        .findFirst()
                        .orElse(sucursales.get(0));
            } else {
                datosSucursal = new SucursalPtovta();
            }

            LOGGER.info("Datos de empresa cargados para registro de ventas");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar datos de empresa", e);
            datosEmpresa = new Arfamc();
            datosSucursal = new SucursalPtovta();
        }
    }

    /**
     * Configura los ComboBoxes
     */
    private void configurarComboBoxes() {
        cbxTipoDocumento.getItems().addAll(
                "TODOS",
                "01 - Factura",
                "03 - Boleta de Venta",
                "07 - Nota de Crédito",
                "08 - Nota de Débito"
        );
        cbxTipoDocumento.getSelectionModel().selectFirst();
    }

    /**
     * Configura los DatePickers
     */
    private void configurarDatePickers() {
        LocalDate hoy = LocalDate.now();
        LocalDate primerDia = hoy.withDayOfMonth(1);

        dpFechaDesde.setValue(primerDia);
        dpFechaHasta.setValue(hoy);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dpFechaDesde.setPromptText("dd/MM/yyyy");
        dpFechaHasta.setPromptText("dd/MM/yyyy");
    }

    /**
     * Configura los listeners
     */
    private void configurarListeners() {
        grupoDestino.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            actualizarRutaArchivo();
        });
    }

    /**
     * Configura atajos de teclado
     */
    /*
    private void configurarAtajos() {
        btnProcesar.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isControlDown()) {
                procesarRegistro(null);
                event.consume();
            }
        });
    }
    */

    /**
     * Establece la ruta por defecto
     */
    private void establecerRutaPorDefecto() {
        String carpetaDescargas = GestorDescargas.getCarpetaDescargas();
        txtRutaArchivo.setText(carpetaDescargas);
    }

    /**
     * Actualiza la ruta del archivo
     */
    private void actualizarRutaArchivo() {
        if (rbImpresora.isSelected()) {
            txtRutaArchivo.setText("Vista previa de impresión");
        } else {
            establecerRutaPorDefecto();
        }
    }

    // ==================== EXAMINAR RUTA ====================

    @FXML
    private void examinarRuta(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar Carpeta de Destino");

        String rutaActual = txtRutaArchivo.getText();
        if (rutaActual != null && !rutaActual.isEmpty()) {
            File dirActual = new File(rutaActual);
            if (dirActual.exists() && dirActual.isDirectory()) {
                directoryChooser.setInitialDirectory(dirActual);
            }
        }

        File dirSeleccionado = directoryChooser.showDialog(btnExaminar.getScene().getWindow());

        if (dirSeleccionado != null) {
            txtRutaArchivo.setText(dirSeleccionado.getAbsolutePath());
            LOGGER.info("Ruta seleccionada: " + dirSeleccionado.getAbsolutePath());
        }
    }

    // ==================== PROCESAR REGISTRO ====================

    @FXML
    private void procesarRegistro(ActionEvent event) {
        try {
            if (!validarDatos()) {
                return;
            }

            LocalDate fechaDesde = dpFechaDesde.getValue();
            LocalDate fechaHasta = dpFechaHasta.getValue();
            String tipoDoc = obtenerCodigoTipoDocumento();
            String moneda = rbSoles.isSelected() ? "S" : "D";

            userId = registroVentaDao.ejecutarRegistroVenta(
                    NO_CIA, fechaDesde, fechaHasta, tipoDoc, moneda
            );

            if (userId == null || userId.isEmpty()) {
                Mensaje.error(null, "Error de Proceso",
                        "No se pudo ejecutar el procedimiento almacenado.");
                return;
            }

            LOGGER.info("Procedimiento ejecutado. UserId: " + userId);

            List<RegVta> registros = registroVentaDao.obtenerRegistrosVenta(
                    NO_CIA, userId, fechaDesde, fechaHasta
            );

            if (registros.isEmpty()) {
                Mensaje.alerta(null, "Sin Datos",
                        "No se encontraron registros de venta para el período seleccionado.");
                return;
            }

            LOGGER.info("Se obtuvieron " + registros.size() + " registros");

            generarReporte(registros, fechaDesde, fechaHasta);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al procesar registro de ventas", e);
            Mensaje.error(null, "Error de Proceso",
                    "Ocurrió un error al procesar el registro de ventas:\n" + e.getMessage());
        }
    }

    private void mostrarMensajeEspera() {
        FormCargar.mostrarCargando("Procesando reporte de ventas...", vbxPrincipal);

        // Crear tarea en segundo plano
        Task<List<RegVta>> task = new Task<List<RegVta>>() {
            @Override
            protected List<RegVta> call() throws Exception {
                LocalDate fechaDesde = dpFechaDesde.getValue();
                LocalDate fechaHasta = dpFechaHasta.getValue();
                String tipoDoc = obtenerCodigoTipoDocumento();
                String moneda = rbSoles.isSelected() ? "S" : "D";

                userId = registroVentaDao.ejecutarRegistroVenta(
                        NO_CIA, fechaDesde, fechaHasta, tipoDoc, moneda
                );

                return registroVentaDao.obtenerRegistrosVenta(NO_CIA, userId, fechaDesde, fechaHasta);
            }
        };

        task.setOnSucceeded(event -> {
            List<RegVta> registros = task.getValue();
            FormCargar.ocultarCargando();
            generarReporte(registros, dpFechaDesde.getValue(), dpFechaHasta.getValue());
        });

        // Manejar error en la tarea
        task.setOnFailed(event -> {
            FormCargar.ocultarCargando();
            Throwable exception = task.getException();
            Mensaje.error(null, "Error",
                    "Ocurrió un error al procesar el reporte de ventas: " +
                            (exception != null ? exception.getMessage() : "Error desconocido"));
            if (exception != null) {
                exception.printStackTrace();
            }
        });

        new Thread(task).start();

    }

    /**
     * Valida los datos del formulario
     */
    private boolean validarDatos() {
        if (dpFechaDesde.getValue() == null) {
            Mensaje.alerta(null, "Validación", "Debe seleccionar la fecha inicial.");
            dpFechaDesde.requestFocus();
            return false;
        }

        if (dpFechaHasta.getValue() == null) {
            Mensaje.alerta(null, "Validación", "Debe seleccionar la fecha final.");
            dpFechaHasta.requestFocus();
            return false;
        }

        if (dpFechaDesde.getValue().isAfter(dpFechaHasta.getValue())) {
            Mensaje.alerta(null, "Validación",
                    "La fecha inicial debe ser menor o igual a la fecha final.");
            dpFechaDesde.requestFocus();
            return false;
        }

        if (cbxTipoDocumento.getValue() == null) {
            Mensaje.alerta(null, "Validación", "Debe seleccionar el tipo de documento.");
            cbxTipoDocumento.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Obtiene el código del tipo de documento
     */
    private String obtenerCodigoTipoDocumento() {
        String seleccion = cbxTipoDocumento.getValue();
        if (seleccion.equals("TODOS")) {
            return null;
        }
        return Metodos.getTipoDocumentoSunat(seleccion.substring(0, 2));
    }

    // ==================== GENERACIÓN DE REPORTES ====================

    private void generarReporte(List<RegVta> registros, LocalDate fechaDesde, LocalDate fechaHasta) {
        try {
            if (rbPdf.isSelected()) {
                generarReportePDF(registros, fechaDesde, fechaHasta);
            } else if (rbExcel.isSelected()) {
                generarReporteExcel(registros, fechaDesde, fechaHasta);
            } else {
                mostrarVistaPrevia(registros, fechaDesde, fechaHasta);
            }

            RegistroVentaDao registroVentaDao = new RegistroVentaDao();
            registroVentaDao.ejecutarLimpiarRegVta(NO_CIA);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar reporte", e);
            Mensaje.error(null, "Error de Reporte",
                    "No se pudo generar el reporte:\n" + e.getMessage());
        }
    }

    private void generarReportePDF(List<RegVta> registros, LocalDate fechaDesde, LocalDate fechaHasta)
            throws JRException {
        LOGGER.info("Generando reporte PDF");

        InputStream reportStream = getClass().getResourceAsStream(REPORTE_PDF_PATH);
        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);

        Map<String, Object> parametros = prepararParametros(fechaDesde, fechaHasta);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(registros);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);

        String nombreArchivo = generarNombreArchivo(fechaDesde, fechaHasta, "pdf");
        String rutaCompleta = txtRutaArchivo.getText() + File.separator + nombreArchivo;

        JasperExportManager.exportReportToPdfFile(jasperPrint, rutaCompleta);

        LOGGER.info("Reporte PDF generado: " + rutaCompleta);
        Mensaje.alerta(null, "Reporte Generado",
                "El reporte PDF se guardó correctamente en:\n" + rutaCompleta);
    }

    private void generarReporteExcel(List<RegVta> registros, LocalDate fechaDesde, LocalDate fechaHasta)
            throws JRException {
        LOGGER.info("Generando reporte Excel");

        InputStream reportStream = getClass().getResourceAsStream(REPORTE_XLS_PATH);
        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);

        Map<String, Object> parametros = prepararParametros(fechaDesde, fechaHasta);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(registros);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);

        String nombreArchivo = generarNombreArchivo(fechaDesde, fechaHasta, "xlsx");
        String rutaCompleta = txtRutaArchivo.getText() + File.separator + nombreArchivo;

        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(rutaCompleta));

        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setOnePagePerSheet(false);
        configuration.setDetectCellType(true);
        configuration.setCollapseRowSpan(false);
        exporter.setConfiguration(configuration);

        exporter.exportReport();

        LOGGER.info("Reporte Excel generado: " + rutaCompleta);
        Mensaje.alerta(null, "Reporte Generado",
                "El reporte Excel se guardó correctamente en:\n" + rutaCompleta);
    }

    private void mostrarVistaPrevia(List<RegVta> registros, LocalDate fechaDesde, LocalDate fechaHasta)
            throws JRException {
        LOGGER.info("Mostrando vista previa");

        InputStream reportStream = getClass().getResourceAsStream(REPORTE_PDF_PATH);
        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);

        Map<String, Object> parametros = prepararParametros(fechaDesde, fechaHasta);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(registros);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);

        SwingUtilities.invokeLater(() -> {
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("REGISTRO DE VENTAS - " +
                    fechaDesde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " al " +
                    fechaHasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            viewer.setAlwaysOnTop(true);
            viewer.setVisible(true);
            viewer.toFront();
            viewer.requestFocus();
            viewer.setAlwaysOnTop(false);
            viewer.setLocationRelativeTo(null);
            viewer.setExtendedState(JFrame.NORMAL);
        });
    }

    /**
     * Prepara los parámetros del reporte incluyendo datos de empresa
     */
    private Map<String, Object> prepararParametros(LocalDate fechaDesde, LocalDate fechaHasta) {
        Map<String, Object> parametros = new HashMap<>();

        // Parámetros básicos
        parametros.put("NO_CIA", NO_CIA);
        parametros.put("userid", userId);
        parametros.put("d_fecha", java.sql.Date.valueOf(fechaDesde));
        parametros.put("h_fecha", java.sql.Date.valueOf(fechaHasta));
        parametros.put("TITULO", "REGISTRO DE VENTAS");
        parametros.put("PERIODO", "Del " +
                fechaDesde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " al " +
                fechaHasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Logo
        parametros.put("LOGO_PATH", getClass().getResource(LOGO_PATH).toString());

        // Datos de la empresa
        if (datosEmpresa != null) {
            parametros.put("EMPRESA_NOMBRE",
                    datosEmpresa.getNombre() != null ? datosEmpresa.getNombre() : "");
            parametros.put("EMPRESA_RUC",
                    datosEmpresa.getRuc() != null ? datosEmpresa.getRuc() : "");
            parametros.put("EMPRESA_DESCRIPCION",
                    datosEmpresa.getDescripcion() != null ? datosEmpresa.getDescripcion() : "");
        } else {
            parametros.put("EMPRESA_NOMBRE", "");
            parametros.put("EMPRESA_RUC", "");
            parametros.put("EMPRESA_DESCRIPCION", "");
        }

        // Datos de la sucursal
        if (datosSucursal != null) {
            parametros.put("EMPRESA_DIRECCION",
                    datosSucursal.getDireccion() != null ? datosSucursal.getDireccion() : "");
            parametros.put("EMPRESA_EMAIL",
                    datosSucursal.getCorreoElectro() != null ? datosSucursal.getCorreoElectro() : "");

            StringBuilder telefonos = new StringBuilder();
            if (datosSucursal.getTelef1() != null && !datosSucursal.getTelef1().isEmpty()) {
                telefonos.append(datosSucursal.getTelef1());
            }
            if (datosSucursal.getTelef2() != null && !datosSucursal.getTelef2().isEmpty()) {
                if (telefonos.length() > 0) telefonos.append(" / ");
                telefonos.append(datosSucursal.getTelef2());
            }
            parametros.put("EMPRESA_TELEFONOS", telefonos.toString());
        } else {
            parametros.put("EMPRESA_DIRECCION", "");
            parametros.put("EMPRESA_EMAIL", "");
            parametros.put("EMPRESA_TELEFONOS", "");
        }

        // Información de generación
        LocalDateTime ahora = LocalDateTime.now();
        parametros.put("FECHA_GENERACION", ahora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        parametros.put("HORA_GENERACION", ahora.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        parametros.put("USUARIO_GENERACION", userId != null ? userId : "SYSTEM");

        return parametros;
    }

    private String generarNombreArchivo(LocalDate fechaDesde, LocalDate fechaHasta, String extension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return "REG_VENT_" +
                fechaDesde.format(formatter) + "_AL_" +
                fechaHasta.format(formatter) +
                "_" + System.currentTimeMillis() +
                "." + extension;
    }
}