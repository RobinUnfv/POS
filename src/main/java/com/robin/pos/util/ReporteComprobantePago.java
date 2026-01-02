package com.robin.pos.util;

import com.robin.pos.model.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReporteComprobantePago {
    private static final Logger LOGGER = Logger.getLogger(ReporteComprobantePago.class.getName());

    // Ruta del archivo JRXML (ajustar según tu proyecto)
    private static final String JRXML_PATH = "/com/robin/pos/reportes/comprobantePago.jrxml";
    private static final String JASPER_PATH = "/com/robin/pos/reportes/comprobantePago.jasper";
    private static final String LOGO_PATH = "/com/robin/pos/imagenes/logos-nexer.png";

    private String empresaNombre = "CORPORACION TEXTIL CELIA E.I.R.L.";
    private String empresaActividad = "EN DISEÑO Y MODELOS EXCLUSIVOS\nROPA PARA BEBES - CAMPAÑA ESCOLAR\nPRECIOS ESPECIALES PARA PROVINCIAS - VENTAS POR MAYOR Y MENOR";
    private String empresaDireccion = "JR. MARISCAL AGUSTIN GAMARRA NRO. 676 INT. 262 URB. EL PORVENIR - LA VICTORIA - LIMA - LIMA";
    private String empresaRuc = "20609272016";
    private String empresaTelefonos = "/";
    private String empresaWeb = "";
    private String empresaEmail = "";
    private String bancoCuentaSoles = "191-9409603-0-93";
    private String bancoCuentaDolares = "00219100940960309350";

    /**
     * Genera y muestra el reporte de comprobante de pago
     *
     * @param resultado Resultado de la emisión del comprobante
     * @param detalles Lista de detalles de venta
     * @param datosCliente Datos del cliente
     * @param datosVenta Datos adicionales de la venta
     */
    public void generarReporte(ResultadoEmision resultado,
                               List<DetalleVenta> detalles,
                               DatosCliente datosCliente,
                               DatosVenta datosVenta) {
        try {
            // Cargar el reporte compilado o compilarlo desde JRXML
            JasperReport jasperReport = cargarReporte();

            // Preparar los parámetros
            Map<String, Object> parametros = prepararParametros(resultado, datosCliente, datosVenta, detalles);

            // Convertir detalles a DetalleComprobante para el datasource
            List<DetalleComprobante> listaDetalles = convertirDetalles(detalles, datosVenta.getPorcentajeIgv());
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(listaDetalles);

            // Llenar el reporte
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);

            // Mostrar el reporte en un visor
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("Comprobante de Pago - " + resultado.getNoFactu());
            viewer.setVisible(true);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar reporte", e);
            Mensaje.error(null, "Error de Reporte",
                    "No se pudo generar el reporte: " + e.getMessage());
        }
    }

    /**
     * Convierte la lista de DetalleVenta a DetalleComprobante
     */
    private List<DetalleComprobante> convertirDetalles(List<DetalleVenta> detalles, int porcentajeIgv) {
        List<DetalleComprobante> lista = new ArrayList<>();
        for (DetalleVenta dv : detalles) {
            lista.add(DetalleComprobante.fromDetalleVenta(dv, porcentajeIgv));
        }
        return lista;
    }

    private JasperReport cargarReporte() throws JRException {
        // Intentar cargar el reporte compilado (.jasper)
        InputStream jasperStream = getClass().getResourceAsStream(JASPER_PATH);
        if (jasperStream != null) {
            return (JasperReport) JRLoader.loadObject(jasperStream);
        }

        // Si no existe, compilar desde JRXML
        InputStream jrxmlStream = getClass().getResourceAsStream(JRXML_PATH);
        if (jrxmlStream != null) {
            return JasperCompileManager.compileReport(jrxmlStream);
        }

        throw new JRException("No se encontró el archivo de reporte: " + JRXML_PATH);
    }

    /**
     * Prepara todos los parámetros para el reporte
     */
    private Map<String, Object> prepararParametros(ResultadoEmision resultado,
                                                   DatosCliente cliente,
                                                   DatosVenta venta,
                                                   List<DetalleVenta> detalles) {
        Map<String, Object> params = new HashMap<>();

        // Datos de la empresa
        params.put("EMPRESA_NOMBRE", empresaNombre);
        params.put("EMPRESA_ACTIVIDAD", empresaActividad);
        params.put("EMPRESA_DIRECCION", empresaDireccion);
        params.put("EMPRESA_RUC", empresaRuc);
        params.put("EMPRESA_TELEFONOS", empresaTelefonos);
        params.put("EMPRESA_WEB", empresaWeb);
        params.put("EMPRESA_EMAIL", empresaEmail);
        params.put("BANCO_CUENTA_SOLES", bancoCuentaSoles);
        params.put("BANCO_CUENTA_DOLARES", bancoCuentaDolares);

        // Logo
        params.put("LOGO_PATH", getClass().getResource(LOGO_PATH).toString());

        // Datos del comprobante
        params.put("FACTURA_NUMERO", resultado.getNumeroComprobanteFormateado());

        // Datos del cliente
        params.put("CLIENTE_NOMBRE", cliente.getNombre());
        params.put("CLIENTE_RUC", cliente.getNumeroDocumento());
        params.put("CLIENTE_DIRECCION", cliente.getDireccion());

        // Datos de la venta
        params.put("FECHA_EMISION", venta.getFechaEmision().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        params.put("CONDICION_PAGO", venta.getCondicionPago());
        params.put("MONEDA", venta.getMoneda());
        params.put("VENDEDOR", venta.getVendedor());
        params.put("ORDEN_COMPRA", venta.getOrdenCompra() != null ? venta.getOrdenCompra() : "");
        params.put("GUIA_REMISION", resultado.getNoGuia() != null ? resultado.getNoGuia() : "----------");
        params.put("ENTREGA_DIRECCION", cliente.getDireccion());

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

        // Código QR (generar imagen)
        try {
            // InputStream qrImage = generarCodigoQR(resultado, totalConIgv);
            InputStream qrImage = GeneradorQR.generarQRSunat(
                    empresaRuc,
                    resultado.getNumeroComprobanteFormateado(),
                    igvTotal,
                    totalConIgv,
                    venta.getFechaEmision(),
                    cliente.getTipoDocumento(),
                    cliente.getNumeroDocumento()
            );
            params.put("QR_CODE_IMAGE", qrImage);
        } catch (Exception e) {
            LOGGER.warning("No se pudo generar código QR: " + e.getMessage());
        }

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

}
