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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase para generar reportes de nota de venta
 * Datos dinámicos desde BD
 *
 * @author Robin POS
 * @version 2.0
 */
public class ReporteNotaVenta {
    private static final Logger LOGGER = Logger.getLogger(ReporteNotaVenta.class.getName());

    // Rutas de los archivos de reporte
    private static final String JRXML_PATH = "/com/robin/pos/reportes/notaVentaTicket.jrxml";
    private static final String JASPER_PATH = "/com/robin/pos/reportes/notaVentaTicket.jasper";
    private static final String LOGO_PATH = "/com/robin/pos/imagenes/logos-nexer.png";
    private static final String NO_CIA = "01";

    // DAOs para obtener datos dinámicamente
    private final ArfamcDao arfamcDao = new ArfamcDao();
    private final SucursalPtovtaDao sucursalDao = new SucursalPtovtaDao();

    // Cache de datos de empresa y sucursal
    private Arfamc datosEmpresa;
    private SucursalPtovta datosSucursal;

    /**
     * Constructor que carga los datos de la empresa y sucursal
     */
    public ReporteNotaVenta() {
        cargarDatosEmpresa();
    }

    /**
     * Carga los datos de la empresa y sucursal desde la base de datos
     */
    private void cargarDatosEmpresa() {
        try {
            // Obtener datos de la compañía
            datosEmpresa = arfamcDao.obtenerDatosCompania(NO_CIA);
            if (datosEmpresa == null) {
                LOGGER.warning("No se pudieron obtener los datos de la empresa");
                datosEmpresa = new Arfamc();
            }

            // Obtener la primera sucursal activa
            List<SucursalPtovta> sucursales = sucursalDao.listarSucursales(NO_CIA);
            if (!sucursales.isEmpty()) {
                datosSucursal = sucursales.stream()
                        .filter(s -> "A".equals(s.getEstadoSuc()))
                        .findFirst()
                        .orElse(sucursales.get(0));
            } else {
                LOGGER.warning("No se encontraron sucursales");
                datosSucursal = new SucursalPtovta();
            }

            LOGGER.info("Datos de empresa y sucursal cargados para nota de venta");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar datos de empresa", e);
            datosEmpresa = new Arfamc();
            datosSucursal = new SucursalPtovta();
        }
    }

    /**
     * Genera y muestra el reporte de nota de venta
     *
     * @param resultado Resultado de la emisión
     * @param detalles Lista de detalles de venta
     * @param datosCliente Datos del cliente
     * @param datosVenta Datos adicionales de la venta
     */
    public void generarReporte(ResultadoEmision resultado,
                               List<DetalleVenta> detalles,
                               DatosCliente datosCliente,
                               DatosVenta datosVenta) {
        try {
            JasperPrint jasperPrint = generarJasperPrint(resultado, detalles, datosCliente, datosVenta);
            String tipoComprobante = Metodos.getTipoComprobante(resultado.getNoFactu());

            SwingUtilities.invokeLater(() -> {
                JasperViewer viewer = new JasperViewer(jasperPrint, false);
                viewer.setTitle(tipoComprobante + " " + resultado.getNoFactu());

                // Configurar la ventana para que se muestre al frente
                viewer.setAlwaysOnTop(true);
                viewer.setVisible(true);
                viewer.toFront();
                viewer.requestFocus();
                viewer.setAlwaysOnTop(false);

                // Centrar en pantalla
                viewer.setLocationRelativeTo(null);
                viewer.setExtendedState(JFrame.NORMAL);
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar reporte de nota de venta", e);
            Mensaje.error(null, "Error de Reporte",
                    "No se pudo generar la nota de venta: " + e.getMessage());
        }
    }

    /**
     * Genera el JasperPrint con todos los datos
     */
    private JasperPrint generarJasperPrint(ResultadoEmision resultado,
                                           List<DetalleVenta> detalles,
                                           DatosCliente datosCliente,
                                           DatosVenta datosVenta) throws JRException {

        // Cargar el reporte
        JasperReport jasperReport = cargarReporte();

        // Preparar parámetros
        Map<String, Object> parametros = prepararParametros(resultado, datosCliente, datosVenta, detalles);

        // Convertir detalles
        List<DetalleComprobante> listaDetalles = convertirDetalles(detalles, datosVenta.getPorcentajeIgv());
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(listaDetalles);

        // Llenar y retornar
        return JasperFillManager.fillReport(jasperReport, parametros, dataSource);
    }

    /**
     * Convierte DetalleVenta a DetalleComprobante
     */
    private List<DetalleComprobante> convertirDetalles(List<DetalleVenta> detalles, int porcentajeIgv) {
        List<DetalleComprobante> lista = new ArrayList<>();
        for (DetalleVenta dv : detalles) {
            lista.add(DetalleComprobante.convertirNotaVenta(dv, porcentajeIgv));
        }
        return lista;
    }

    /**
     * Carga el reporte desde .jasper o compila desde .jrxml
     */
    private JasperReport cargarReporte() throws JRException {
        // Intentar cargar compilado
        InputStream jasperStream = getClass().getResourceAsStream(JASPER_PATH);
        if (jasperStream != null) {
            return (JasperReport) JRLoader.loadObject(jasperStream);
        }

        // Compilar desde JRXML
        InputStream jrxmlStream = getClass().getResourceAsStream(JRXML_PATH);
        if (jrxmlStream != null) {
            return JasperCompileManager.compileReport(jrxmlStream);
        }

        throw new JRException("No se encontró el archivo de reporte: " + JRXML_PATH);
    }

    /**
     * Prepara todos los parámetros del reporte usando datos de BD
     */
    private Map<String, Object> prepararParametros(ResultadoEmision resultado,
                                                   DatosCliente cliente,
                                                   DatosVenta venta,
                                                   List<DetalleVenta> detalles) {
        Map<String, Object> params = new HashMap<>();

        // Tipo de comprobante
        String tipoComprobante = Metodos.getTipoComprobante(resultado.getNoFactu());
        params.put("TIPO_COMPROBANTE", tipoComprobante);

        // Tipo documento cliente
        String tipoDocumentoCliente = Metodos.getTipoDocumentoCliente(resultado.getNoFactu(), resultado.getNoCliente());
        params.put("TIP_DOC_CLI", tipoDocumentoCliente);

        // ==================== DATOS DE LA EMPRESA DESDE BD ====================
        if (datosEmpresa != null) {
            params.put("EMPRESA_NOMBRE",
                    datosEmpresa.getNombre() != null ? datosEmpresa.getNombre() : "");

            params.put("EMPRESA_ACTIVIDAD",
                    datosEmpresa.getDescripcion() != null ? datosEmpresa.getDescripcion() : "");

            params.put("EMPRESA_RUC",
                    datosEmpresa.getRuc() != null ? datosEmpresa.getRuc() : "");

            params.put("BANCO_CUENTA_SOLES",
                    datosEmpresa.getCuentaSol() != null ? datosEmpresa.getCuentaSol() : "");

            params.put("BANCO_CUENTA_DOLARES",
                    datosEmpresa.getCci() != null ? datosEmpresa.getCci() : "");
        } else {
            params.put("EMPRESA_NOMBRE", "");
            params.put("EMPRESA_ACTIVIDAD", "");
            params.put("EMPRESA_RUC", "");
            params.put("BANCO_CUENTA_SOLES", "");
            params.put("BANCO_CUENTA_DOLARES", "");
        }

        // ==================== DATOS DE LA SUCURSAL DESDE BD ====================
        if (datosSucursal != null) {
            // Dirección compacta para ticket
            String direccionTicket = datosSucursal.getDireccion() != null
                    ? datosSucursal.getDireccion().replace("\n", " ")
                    : "";
            params.put("EMPRESA_DIRECCION", direccionTicket);

            // Teléfonos concatenados
            StringBuilder telefonos = new StringBuilder();
            if (datosSucursal.getTelef1() != null && !datosSucursal.getTelef1().isEmpty()) {
                telefonos.append(datosSucursal.getTelef1());
            }
            if (datosSucursal.getTelef2() != null && !datosSucursal.getTelef2().isEmpty()) {
                if (telefonos.length() > 0) telefonos.append(" / ");
                telefonos.append(datosSucursal.getTelef2());
            }
            params.put("EMPRESA_TELEFONOS", telefonos.toString());

            params.put("EMPRESA_EMAIL",
                    datosSucursal.getCorreoElectro() != null ? datosSucursal.getCorreoElectro() : "");
        } else {
            params.put("EMPRESA_DIRECCION", "");
            params.put("EMPRESA_TELEFONOS", "");
            params.put("EMPRESA_EMAIL", "");
        }

        // Logo
        try {
            params.put("LOGO_PATH", getClass().getResource(LOGO_PATH).toString());
        } catch (Exception e) {
            LOGGER.warning("No se pudo cargar el logo: " + e.getMessage());
        }

        // Datos del comprobante
        params.put("FACTURA_NUMERO", resultado.getNumeroComprobanteFormateado());

        // Datos del cliente
        params.put("CLIENTE_NOMBRE", cliente.getNombre() != null ? cliente.getNombre() : "CLIENTE GENERAL");
        params.put("CLIENTE_RUC", cliente.getNumeroDocumento() != null ? cliente.getNumeroDocumento() : "00000000");

        // Fecha y hora
        params.put("FECHA_EMISION", venta.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        params.put("HORA_EMISION", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        // Condición de pago y otros
        params.put("CONDICION_PAGO", venta.getCondicionPago() != null ? venta.getCondicionPago() : "CONTADO");
        params.put("MONEDA", "S/");
        params.put("VENDEDOR", venta.getVendedor() != null ? venta.getVendedor() : "--");
        params.put("ORDEN_COMPRA", resultado.getNoOrden() != null ? resultado.getNoOrden() : "-----");

        // Calcular totales
        BigDecimal totalConIgv = calcularTotalConIgv(detalles);
        BigDecimal subTotal = totalConIgv.divide(BigDecimal.valueOf(1.18), 2, RoundingMode.HALF_UP);
        BigDecimal igvTotal = totalConIgv.subtract(subTotal);

        params.put("TOTAL_GRAVADAS", subTotal);
        params.put("IGV_TOTAL", igvTotal);
        params.put("IMPORTE_TOTAL", totalConIgv);
        params.put("DESCUENTO_GLOBAL", BigDecimal.ZERO);
        params.put("REDONDEO", BigDecimal.ZERO);
        params.put("DESCUENTOS_TOTALES", BigDecimal.ZERO);

        // Monto en letras
        String montoEnLetras = NumeroALetras.convertir(totalConIgv.doubleValue(), venta.getMoneda());
        params.put("SON", montoEnLetras);

        return params;
    }

    /**
     * Calcula el total con IGV
     */
    private BigDecimal calcularTotalConIgv(List<DetalleVenta> detalles) {
        double total = detalles.stream()
                .mapToDouble(dv -> dv.getCantidad() * dv.getPrecio())
                .sum();
        return BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Permite establecer una sucursal específica para el reporte
     * @param codSucursal Código de sucursal
     * @param codPtoVta Código de punto de venta
     */
    public void setSucursal(String codSucursal, String codPtoVta) {
        try {
            SucursalPtovta sucursal = sucursalDao.buscarSucursal(NO_CIA, codSucursal, codPtoVta);
            if (sucursal != null) {
                this.datosSucursal = sucursal;
                LOGGER.info("Sucursal establecida para nota de venta: " + codSucursal + "-" + codPtoVta);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo establecer la sucursal", e);
        }
    }
}