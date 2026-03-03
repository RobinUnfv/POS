package com.robin.pos.util;

import com.robin.pos.model.DetalleVenta;
import com.robin.pos.model.ItemCotizacion;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.format.DateTimeFormatter;

public class ReporteCotizacion {
    private static final Logger LOGGER = Logger.getLogger(ReporteCotizacion.class.getName());

    // Rutas de archivos
    private static final String JRXML_PATH = "/com/robin/pos/reportes/cotizacion.jrxml";
    private static final String JASPER_PATH = "/com/robin/pos/reportes/cotizacion.jasper";
    private static final String LOGO_PATH = "/com/robin/pos/imagenes/logos-nexer.png";

    // Datos de la empresa (configurables)
    private String empresaNombre = "CORPORACION TEXTIL CELIA E.I.R.L.";
    private String empresaTagline = "EN DISEÑO Y MODELOS EXCLUSIVOS EN PRODUCTOS TEXTILES - \nPRENDAS DE VESTIR - CON PRECIOS ESPECIALES PARA PROVINCIA - VENTAS POR MAYOR Y MENOR";
    private String empresaTelefono = "";
    private String empresaDireccion = "JR. MARISCAL AGUSTIN GAMARRA NRO. 676 INT. 262 URB. EL PORVENIR - LA VICTORIA - LIMA - LIMA";
    private String empresaEmail = "";

    // Método de pago predeterminado
    private String metodoPago = "Banco de Crédito del Perú (BCP)";
    private String numeroCuenta = "191-9409603-0-93";
    private String numeroCuentaCompleto = "CCI: 00219100940960309350";

    /**
     * Genera y muestra el reporte de cotización
     *
     * @param numeroCotizacion Número de la cotización
     * @param clienteNombre Nombre del cliente
     * @param detalles Lista de productos/servicios cotizados
     */
    public void generarReporte(String numeroCotizacion,
                               String clienteNombre,
                               List<DetalleVenta> detalles) {

        try {
            LOGGER.info("Iniciando generación de cotización: " + numeroCotizacion);

            // Cargar el reporte
            JasperReport jasperReport = cargarReporte();

            // Preparar parámetros
            Map<String, Object> parametros = prepararParametros(
                    numeroCotizacion,
                    clienteNombre,
                    detalles
            );

            // Convertir detalles a items de cotización
            List<ItemCotizacion> items = convertirDetalles(detalles);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(items);

            // Generar el reporte
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport,
                    parametros,
                    dataSource
            );

            // Mostrar en visor
            mostrarVisor(jasperPrint, numeroCotizacion);

            LOGGER.info("Cotización generada exitosamente");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar cotización", e);
            Mensaje.error(null, "Error al Generar Cotización",
                    "No se pudo generar la cotización: " + e.getMessage());
        }
    }

    /**
     * Genera el reporte y lo exporta a PDF
     *
     * @param numeroCotizacion Número de la cotización
     * @param clienteNombre Nombre del cliente
     * @param detalles Lista de productos/servicios
     */
    public void generarPDF(String numeroCotizacion,
                           String clienteNombre,
                           List<DetalleVenta> detalles) {

        try {
            LOGGER.info("Generando PDF de cotización: " + numeroCotizacion);

            // Cargar reporte
            JasperReport jasperReport = cargarReporte();

            // Preparar parámetros
            Map<String, Object> parametros = prepararParametros(
                    numeroCotizacion,
                    clienteNombre,
                    detalles
            );

            // Convertir detalles
            List<ItemCotizacion> items = convertirDetalles(detalles);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(items);

            // Generar reporte
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport,
                    parametros,
                    dataSource
            );

            // Exportar a PDF
            String carpetaDescargas = GestorDescargas.getCarpetaDescargas();
            String nombreArchivo = carpetaDescargas + "/COTIZACION_" + numeroCotizacion + ".pdf";
            JasperExportManager.exportReportToPdfFile(jasperPrint, nombreArchivo);

            LOGGER.info("PDF generado: " + nombreArchivo);

            Mensaje.alerta (null, "PDF Generado",
                    "La cotización se guardó en:\n" + nombreArchivo);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar PDF", e);
            Mensaje.error(null, "Error al Generar PDF",
                    "No se pudo generar el PDF: " + e.getMessage());
        }
    }

    /**
     * Carga el reporte compilado o lo compila desde JRXML
     */
    private JasperReport cargarReporte() throws JRException {

        // Intentar cargar el reporte compilado (.jasper)
        InputStream jasperStream = getClass().getResourceAsStream(JASPER_PATH);
        if (jasperStream != null) {
            LOGGER.fine("Cargando reporte compilado");
            return (JasperReport) JRLoader.loadObject(jasperStream);
        }

        // Si no existe, compilar desde JRXML
        InputStream jrxmlStream = getClass().getResourceAsStream(JRXML_PATH);
        if (jrxmlStream != null) {
            LOGGER.fine("Compilando reporte desde JRXML");
            return JasperCompileManager.compileReport(jrxmlStream);
        }

        throw new JRException("No se encontró el archivo de reporte: " + JRXML_PATH);
    }

    /**
     * Prepara todos los parámetros para el reporte
     */
    private Map<String, Object> prepararParametros(String numeroCotizacion,
                                                   String clienteNombre,
                                                   List<DetalleVenta> detalles) {

        Map<String, Object> params = new HashMap<>();

        // Logo
        try {
            params.put("LOGO_PATH", getClass().getResource(LOGO_PATH).toString());
        } catch (Exception e) {
            LOGGER.warning("No se pudo cargar el logo: " + e.getMessage());
            params.put("LOGO_PATH", null);
        }

        // Datos de la empresa
        params.put("EMPRESA_NOMBRE", empresaNombre);
        params.put("EMPRESA_TAGLINE", empresaTagline);
        params.put("EMPRESA_TELEFONO", empresaTelefono);
        params.put("EMPRESA_DIRECCION", empresaDireccion);
        params.put("EMPRESA_EMAIL", empresaEmail);

        // Datos de la cotización
        params.put("NUMERO_COTIZACION", numeroCotizacion);

        // Fecha actual
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaActual = LocalDate.now().format(formatter);
        params.put("FECHA_COTIZACION", fechaActual);

        // Cliente
        params.put("CLIENTE_NOMBRE", clienteNombre);

        // Calcular totales
        BigDecimal subtotal = calcularSubtotal(detalles);
        //BigDecimal igvMonto = calcularIGV(subtotal);
        //BigDecimal total = subtotal.add(igvMonto);

        BigDecimal subTotal = subtotal.divide(BigDecimal.valueOf(1.18), 2, RoundingMode.HALF_UP);
        BigDecimal igvTotal = subtotal.subtract(subTotal);

        params.put("SUBTOTAL", subTotal);
        params.put("IGV_PORCENTAJE", new BigDecimal("18"));
        params.put("IGV_MONTO", igvTotal);
        params.put("TOTAL", subtotal);

        // Método de pago
        params.put("METODO_PAGO", metodoPago);
        params.put("NUMERO_CUENTA", numeroCuenta);
        params.put("NUMERO_CUENTA_COMPLETO", numeroCuentaCompleto);
        // Monto en letras
        String montoEnLetras = NumeroALetras.convertir(subtotal.doubleValue(), "SOL");
        params.put("SON", montoEnLetras);

        return params;
    }

    /**
     * Convierte DetalleVenta a ItemCotizacion
     */
    private List<ItemCotizacion> convertirDetalles(List<DetalleVenta> detalles) {

        List<ItemCotizacion> items = new ArrayList<>();
        int itemNumber = 1;

        for (DetalleVenta detalle : detalles) {
            ItemCotizacion item = new ItemCotizacion();
            item.setItem(itemNumber++);
            item.setDescripcion(detalle.getArinda1().getDescripcion());
            item.setCantidad(BigDecimal.valueOf(detalle.getCantidad()));
            item.setPrecio(BigDecimal.valueOf(detalle.getPrecio()));

            BigDecimal totalItem = item.getCantidad().multiply(item.getPrecio())
                    .setScale(2, RoundingMode.HALF_UP);
            item.setTotal(totalItem);

            items.add(item);
        }

        return items;
    }

    /**
     * Calcula el subtotal (sin IGV)
     */
    private BigDecimal calcularSubtotal(List<DetalleVenta> detalles) {
        double total = detalles.stream()
                .mapToDouble(d -> d.getCantidad() * d.getPrecio())
                .sum();
        return BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el IGV (18%)
     */
    private BigDecimal calcularIGV(BigDecimal subtotal) {
        return subtotal.multiply(new BigDecimal("0.18"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Muestra el visor de JasperReports
     */
    private void mostrarVisor(JasperPrint jasperPrint, String numeroCotizacion) {

        SwingUtilities.invokeLater(() -> {
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("COTIZACIÓN - " + numeroCotizacion);

            // Configurar ventana
            viewer.setAlwaysOnTop(true);
            viewer.setVisible(true);
            viewer.toFront();
            viewer.requestFocus();
            viewer.setAlwaysOnTop(false);

            // Centrar en pantalla
            viewer.setLocationRelativeTo(null);
            viewer.setExtendedState(JFrame.NORMAL);
        });
    }

    // ==================== GETTERS Y SETTERS ====================

    public void setEmpresaNombre(String empresaNombre) {
        this.empresaNombre = empresaNombre;
    }

    public void setEmpresaTagline(String empresaTagline) {
        this.empresaTagline = empresaTagline;
    }

    public void setEmpresaTelefono(String empresaTelefono) {
        this.empresaTelefono = empresaTelefono;
    }

    public void setEmpresaDireccion(String empresaDireccion) {
        this.empresaDireccion = empresaDireccion;
    }

    public void setEmpresaEmail(String empresaEmail) {
        this.empresaEmail = empresaEmail;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public void setNumeroCuentaCompleto(String numeroCuentaCompleto) {
        this.numeroCuentaCompleto = numeroCuentaCompleto;
    }

}
