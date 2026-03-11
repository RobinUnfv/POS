package com.robin.pos.controller;

import com.robin.pos.dao.RegistroVentaDao;
import com.robin.pos.model.RegVta;
import com.robin.pos.util.GestorDescargas;
import com.robin.pos.util.Mensaje;
import com.robin.pos.util.Metodos;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para el formulario de Registro de Ventas
 *
 * @author Robin POS
 * @version 1.0
 */
public class RegistroVentaController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(RegistroVentaController.class.getName());
    private static final String NO_CIA = "01";

    // Rutas de reportes
    private static final String REPORTE_PDF_PATH = "/com/robin/pos/reportes/registroVentaPdf.jasper";
    private static final String REPORTE_XLS_PATH = "/com/robin/pos/reportes/registroVentaXls.jasper";

    // ==================== CAMPOS FXML - PERÍODO ====================

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
    private String userId;

    // ==================== INITIALIZE ====================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Inicializando RegistroVentaController");

        configurarComboBoxes();
        configurarDatePickers();
        configurarListeners();
        //configurarAtajos();
        establecerRutaPorDefecto();

        LOGGER.info("RegistroVentaController inicializado correctamente");
    }

    /**
     * Configura los ComboBoxes
     */
    private void configurarComboBoxes() {
        // Tipos de documento
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
        // Establecer primer día del mes actual
        LocalDate hoy = LocalDate.now();
        LocalDate primerDia = hoy.withDayOfMonth(1);

        dpFechaDesde.setValue(primerDia);
        dpFechaHasta.setValue(hoy);

        // Formato de fecha
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dpFechaDesde.setPromptText("dd/MM/yyyy");
        dpFechaHasta.setPromptText("dd/MM/yyyy");
    }

    /**
     * Configura los listeners
     */
    private void configurarListeners() {
        // Listener para cambio de destino
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
     * Actualiza la ruta del archivo según el destino seleccionado
     */
    private void actualizarRutaArchivo() {
        if (rbImpresora.isSelected()) {
            txtRutaArchivo.setText("Vista previa de impresión");
        } else {
            establecerRutaPorDefecto();
        }
    }

    // ==================== EXAMINAR RUTA ====================

    /**
     * Abre diálogo para seleccionar carpeta de destino
     */
    @FXML
    private void examinarRuta(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar Carpeta de Destino");

        // Establecer directorio inicial
        String rutaActual = txtRutaArchivo.getText();
        if (rutaActual != null && !rutaActual.isEmpty()) {
            File dirActual = new File(rutaActual);
            if (dirActual.exists() && dirActual.isDirectory()) {
                directoryChooser.setInitialDirectory(dirActual);
            }
        }

        // Mostrar diálogo
        File dirSeleccionado = directoryChooser.showDialog(btnExaminar.getScene().getWindow());

        if (dirSeleccionado != null) {
            txtRutaArchivo.setText(dirSeleccionado.getAbsolutePath());
            LOGGER.info("Ruta seleccionada: " + dirSeleccionado.getAbsolutePath());
        }
    }

    // ==================== PROCESAR REGISTRO ====================

    /**
     * Procesa el registro de ventas
     */
    @FXML
    private void procesarRegistro(ActionEvent event) {

        try {
            // 1. Validar datos
            if (!validarDatos()) {
                return;
            }

            // 2. Obtener parámetros
            LocalDate fechaDesde = dpFechaDesde.getValue();
            LocalDate fechaHasta = dpFechaHasta.getValue();
            String tipoDoc = obtenerCodigoTipoDocumento();
            String moneda = rbSoles.isSelected() ? "S" : "D";

            // 3. Ejecutar procedimiento almacenado
            LOGGER.info("Ejecutando procedimiento FACTU.REGISTRO_VENTA");
            userId = registroVentaDao.ejecutarRegistroVenta(
                    NO_CIA, fechaDesde, fechaHasta, tipoDoc, moneda
            );

            if (userId == null || userId.isEmpty()) {
                Mensaje.error(null, "Error de Proceso",
                        "No se pudo ejecutar el procedimiento almacenado.");
                return;
            }

            LOGGER.info("Procedimiento ejecutado. UserId: " + userId);

            // 4. Obtener datos para el reporte
            List<RegVta> registros = registroVentaDao.obtenerRegistrosVenta(
                    NO_CIA, userId, fechaDesde, fechaHasta
            );

            if (registros.isEmpty()) {
                Mensaje.alerta(null, "Sin Datos",
                        "No se encontraron registros de venta para el período seleccionado.");
                return;
            }

            LOGGER.info("Se obtuvieron " + registros.size() + " registros");

            // 5. Generar reporte según destino
            generarReporte(registros, fechaDesde, fechaHasta);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al procesar registro de ventas", e);
            Mensaje.error(null, "Error de Proceso",
                    "Ocurrió un error al procesar el registro de ventas:\n" + e.getMessage());
        }
    }

    /**
     * Valida los datos del formulario
     */
    private boolean validarDatos() {
        // Validar fecha desde
        if (dpFechaDesde.getValue() == null) {
            Mensaje.alerta(null, "Validación", "Debe seleccionar la fecha inicial.");
            dpFechaDesde.requestFocus();
            return false;
        }

        // Validar fecha hasta
        if (dpFechaHasta.getValue() == null) {
            Mensaje.alerta(null, "Validación", "Debe seleccionar la fecha final.");
            dpFechaHasta.requestFocus();
            return false;
        }

        // Validar que fecha desde sea menor o igual a fecha hasta
        if (dpFechaDesde.getValue().isAfter(dpFechaHasta.getValue())) {
            Mensaje.alerta(null, "Validación",
                    "La fecha inicial debe ser menor o igual a la fecha final.");
            dpFechaDesde.requestFocus();
            return false;
        }

        // Validar tipo de documento
        if (cbxTipoDocumento.getValue() == null) {
            Mensaje.alerta(null, "Validación", "Debe seleccionar el tipo de documento.");
            cbxTipoDocumento.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Obtiene el código del tipo de documento seleccionado
     */
    private String obtenerCodigoTipoDocumento() {
        String seleccion = cbxTipoDocumento.getValue();
        if (seleccion.equals("TODOS")) {
            return null;
        }
        return Metodos.getTipoDocumentoSunat(seleccion.substring(0, 2));
    }

    // ==================== GENERACIÓN DE REPORTES ====================

    /**
     * Genera el reporte según el destino seleccionado
     */
    private void generarReporte(List<RegVta> registros, LocalDate fechaDesde, LocalDate fechaHasta) {
        try {
            if (rbPdf.isSelected()) {
                generarReportePDF(registros, fechaDesde, fechaHasta);
            } else if (rbExcel.isSelected()) {
                generarReporteExcel(registros, fechaDesde, fechaHasta);
            } else {
                // Impresora (vista previa)
                mostrarVistaPrevia(registros, fechaDesde, fechaHasta);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar reporte", e);
            Mensaje.error(null, "Error de Reporte",
                    "No se pudo generar el reporte:\n" + e.getMessage());
        }
    }

    /**
     * Genera reporte PDF
     */
    private void generarReportePDF(List<RegVta> registros, LocalDate fechaDesde, LocalDate fechaHasta)
            throws JRException {
        LOGGER.info("Generando reporte PDF");

        // Cargar reporte
        InputStream reportStream = getClass().getResourceAsStream(REPORTE_PDF_PATH);
        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);

        // Preparar parámetros
        Map<String, Object> parametros = prepararParametros(fechaDesde, fechaHasta);

        // Crear datasource
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(registros);

        // Llenar reporte
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);

        // Exportar a PDF
        String nombreArchivo = generarNombreArchivo(fechaDesde, fechaHasta, "pdf");
        String rutaCompleta = txtRutaArchivo.getText() + File.separator + nombreArchivo;

        JasperExportManager.exportReportToPdfFile(jasperPrint, rutaCompleta);

        LOGGER.info("Reporte PDF generado: " + rutaCompleta);
        Mensaje.alerta(null, "Reporte Generado",
                "El reporte PDF se guardó correctamente en:\n" + rutaCompleta);
    }

    /**
     * Genera reporte Excel
     */
    private void generarReporteExcel(List<RegVta> registros, LocalDate fechaDesde, LocalDate fechaHasta)
            throws JRException {
        LOGGER.info("Generando reporte Excel");

        // Cargar reporte
        InputStream reportStream = getClass().getResourceAsStream(REPORTE_XLS_PATH);
        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);

        // Preparar parámetros
        Map<String, Object> parametros = prepararParametros(fechaDesde, fechaHasta);

        // Crear datasource
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(registros);

        // Llenar reporte
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);

        // Exportar a Excel
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

    /**
     * Muestra vista previa del reporte
     */
    private void mostrarVistaPrevia(List<RegVta> registros, LocalDate fechaDesde, LocalDate fechaHasta)
            throws JRException {
        LOGGER.info("Mostrando vista previa");

        // Cargar reporte
        InputStream reportStream = getClass().getResourceAsStream(REPORTE_PDF_PATH);
        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);

        // Preparar parámetros
        Map<String, Object> parametros = prepararParametros(fechaDesde, fechaHasta);

        // Crear datasource
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(registros);

        // Llenar reporte
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);

        // Mostrar visor
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
     * Prepara los parámetros para el reporte
     */
    private Map<String, Object> prepararParametros(LocalDate fechaDesde, LocalDate fechaHasta) {
        Map<String, Object> parametros = new HashMap<>();

        parametros.put("NO_CIA", NO_CIA);
        parametros.put("userid", userId);
        parametros.put("d_fecha", java.sql.Date.valueOf(fechaDesde));
        parametros.put("h_fecha", java.sql.Date.valueOf(fechaHasta));

        // Parámetros adicionales para el reporte
        parametros.put("TITULO", "REGISTRO DE VENTAS");
        parametros.put("PERIODO", "Del " +
                fechaDesde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " al " +
                fechaHasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        return parametros;
    }

    /**
     * Genera el nombre del archivo
     */
    private String generarNombreArchivo(LocalDate fechaDesde, LocalDate fechaHasta, String extension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return "REG_VENT_" +
                fechaDesde.format(formatter) + "_AL_" +
                fechaHasta.format(formatter) +
                "_" + System.currentTimeMillis() +
                "." + extension;
    }
}