package com.robin.pos.util;

import com.robin.pos.dao.ArfamcDao;
import com.robin.pos.dao.SucursalPtovtaDao;
import com.robin.pos.model.*;
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

/**
 * Clase para generar reportes de cotización
 * Datos dinámicos desde BD
 *
 * @author Robin POS
 * @version 2.0
 */
public class ReporteCotizacion {
    private static final Logger LOGGER = Logger.getLogger(ReporteCotizacion.class.getName());

    // Rutas de archivos
    private static final String JRXML_PATH = "/com/robin/pos/reportes/cotizacion.jrxml";
    private static final String JASPER_PATH = "/com/robin/pos/reportes/cotizacion.jasper";
    private static final String LOGO_PATH = "/com/robin/pos/imagenes/logos-nexer.png";
    private static final String NO_CIA = "01";

    // DAOs para obtener datos dinámicamente
    private final ArfamcDao arfamcDao = new ArfamcDao();
    private final SucursalPtovtaDao sucursalDao = new SucursalPtovtaDao();

    // Cache de datos
    private Arfamc datosEmpresa;
    private SucursalPtovta datosSucursal;

    public ReporteCotizacion() {
        cargarDatosEmpresa();
    }

    private void cargarDatosEmpresa() {
        try {
            datosEmpresa = arfamcDao.obtenerDatosCompania(NO_CIA);
            if (datosEmpresa == null) {
                LOGGER.warning("No se pudieron obtener los datos de la empresa");
                datosEmpresa = new Arfamc();
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

            LOGGER.info("Datos de empresa cargados para cotización");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar datos de empresa", e);
            datosEmpresa = new Arfamc();
            datosSucursal = new SucursalPtovta();
        }
    }

    public void generarReporte(String numeroCotizacion,
                               String clienteNombre,
                               List<DetalleVenta> detalles) {

        try {
            LOGGER.info("Iniciando generación de cotización: " + numeroCotizacion);

            JasperReport jasperReport = cargarReporte();
            Map<String, Object> parametros = prepararParametros(numeroCotizacion, clienteNombre, detalles);
            List<ItemCotizacion> items = convertirDetalles(detalles);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(items);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);
            mostrarVisor(jasperPrint, numeroCotizacion);

            LOGGER.info("Cotización generada exitosamente");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar cotización", e);
            Mensaje.error(null, "Error al Generar Cotización",
                    "No se pudo generar la cotización: " + e.getMessage());
        }
    }

    public void generarPDF(String numeroCotizacion,
                           String clienteNombre,
                           List<DetalleVenta> detalles) {

        try {
            LOGGER.info("Generando PDF de cotización: " + numeroCotizacion);

            JasperReport jasperReport = cargarReporte();
            Map<String, Object> parametros = prepararParametros(numeroCotizacion, clienteNombre, detalles);
            List<ItemCotizacion> items = convertirDetalles(detalles);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(items);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);

            String carpetaDescargas = GestorDescargas.getCarpetaDescargas();
            String nombreArchivo = carpetaDescargas + "/COTIZACION_" + numeroCotizacion + ".pdf";
            JasperExportManager.exportReportToPdfFile(jasperPrint, nombreArchivo);

            LOGGER.info("PDF generado: " + nombreArchivo);
            Mensaje.alerta(null, "PDF Generado",
                    "La cotización se guardó en:\n" + nombreArchivo);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar PDF", e);
            Mensaje.error(null, "Error al Generar PDF",
                    "No se pudo generar el PDF: " + e.getMessage());
        }
    }

    private JasperReport cargarReporte() throws JRException {
        InputStream jasperStream = getClass().getResourceAsStream(JASPER_PATH);
        if (jasperStream != null) {
            LOGGER.fine("Cargando reporte compilado");
            return (JasperReport) JRLoader.loadObject(jasperStream);
        }

        InputStream jrxmlStream = getClass().getResourceAsStream(JRXML_PATH);
        if (jrxmlStream != null) {
            LOGGER.fine("Compilando reporte desde JRXML");
            return JasperCompileManager.compileReport(jrxmlStream);
        }

        throw new JRException("No se encontró el archivo de reporte: " + JRXML_PATH);
    }

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

        // ==================== DATOS DE LA EMPRESA DESDE BD ====================
        if (datosEmpresa != null) {
            params.put("EMPRESA_NOMBRE",
                    datosEmpresa.getNombre() != null ? datosEmpresa.getNombre() : "");

            params.put("EMPRESA_TAGLINE",
                    datosEmpresa.getDescripcion() != null ? datosEmpresa.getDescripcion() : "");
        } else {
            params.put("EMPRESA_NOMBRE", "");
            params.put("EMPRESA_TAGLINE", "");
        }

        // ==================== DATOS DE LA SUCURSAL DESDE BD ====================
        if (datosSucursal != null) {
            params.put("EMPRESA_DIRECCION",
                    datosSucursal.getDireccion() != null ? datosSucursal.getDireccion() : "");

            StringBuilder telefonos = new StringBuilder();
            if (datosSucursal.getTelef1() != null && !datosSucursal.getTelef1().isEmpty()) {
                telefonos.append(datosSucursal.getTelef1());
            }
            if (datosSucursal.getTelef2() != null && !datosSucursal.getTelef2().isEmpty()) {
                if (telefonos.length() > 0) telefonos.append(" / ");
                telefonos.append(datosSucursal.getTelef2());
            }
            params.put("EMPRESA_TELEFONO", telefonos.toString());

            params.put("EMPRESA_EMAIL",
                    datosSucursal.getCorreoElectro() != null ? datosSucursal.getCorreoElectro() : "");
        } else {
            params.put("EMPRESA_DIRECCION", "");
            params.put("EMPRESA_TELEFONO", "");
            params.put("EMPRESA_EMAIL", "");
        }

        // Datos de la cotización
        params.put("NUMERO_COTIZACION", numeroCotizacion);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaActual = LocalDate.now().format(formatter);
        params.put("FECHA_COTIZACION", fechaActual);
        params.put("CLIENTE_NOMBRE", clienteNombre);

        // Calcular totales
        BigDecimal subtotal = calcularSubtotal(detalles);
        BigDecimal subTotal = subtotal.divide(BigDecimal.valueOf(1.18), 2, RoundingMode.HALF_UP);
        BigDecimal igvTotal = subtotal.subtract(subTotal);

        params.put("SUBTOTAL", subTotal);
        params.put("IGV_PORCENTAJE", new BigDecimal("18"));
        params.put("IGV_MONTO", igvTotal);
        params.put("TOTAL", subtotal);

        // Método de pago desde BD
        if (datosEmpresa != null) {
            params.put("METODO_PAGO",
                    datosEmpresa.getBanco() != null ? datosEmpresa.getBanco() : "");
            params.put("NUMERO_CUENTA",
                    datosEmpresa.getCuentaSol() != null ? datosEmpresa.getCuentaSol() : "");
            params.put("NUMERO_CUENTA_COMPLETO",
                    "CCI: " + (datosEmpresa.getCci() != null ? datosEmpresa.getCci() : ""));
        } else {
            params.put("METODO_PAGO", "");
            params.put("NUMERO_CUENTA", "");
            params.put("NUMERO_CUENTA_COMPLETO", "");
        }

        String montoEnLetras = NumeroALetras.convertir(subtotal.doubleValue(), "SOL");
        params.put("SON", montoEnLetras);

        return params;
    }

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

    private BigDecimal calcularSubtotal(List<DetalleVenta> detalles) {
        double total = detalles.stream()
                .mapToDouble(d -> d.getCantidad() * d.getPrecio())
                .sum();
        return BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP);
    }

    private void mostrarVisor(JasperPrint jasperPrint, String numeroCotizacion) {
        SwingUtilities.invokeLater(() -> {
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("COTIZACIÓN - " + numeroCotizacion);
            viewer.setAlwaysOnTop(true);
            viewer.setVisible(true);
            viewer.toFront();
            viewer.requestFocus();
            viewer.setAlwaysOnTop(false);
            viewer.setLocationRelativeTo(null);
            viewer.setExtendedState(JFrame.NORMAL);
        });
    }

    public void setSucursal(String codSucursal, String codPtoVta) {
        try {
            SucursalPtovta sucursal = sucursalDao.buscarSucursal(NO_CIA, codSucursal, codPtoVta);
            if (sucursal != null) {
                this.datosSucursal = sucursal;
                LOGGER.info("Sucursal establecida para cotización: " + codSucursal + "-" + codPtoVta);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo establecer la sucursal", e);
        }
    }
}