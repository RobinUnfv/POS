package com.robin.pos.util;

import com.robin.pos.model.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
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
 * Clase para generar reportes de comprobante de pago en formato ticket (80mm)
 * Para impresoras térmicas POS
 *
 * @author Robin POS
 * @version 1.0
 */
public class ReporteComprobantePagoTicket {

    private static final Logger LOGGER = Logger.getLogger(ReporteComprobantePagoTicket.class.getName());

    // Rutas de los archivos de reporte
    private static final String JRXML_PATH = "/com/robin/pos/reportes/comprobantePagoTicket.jrxml";
    private static final String JASPER_PATH = "/com/robin/pos/reportes/comprobantePagoTicket.jasper";
    private static final String LOGO_PATH = "/com/robin/pos/imagenes/logos-nexer.png";

    // Datos de la empresa (pueden ser configurables)
    private String empresaNombre = "CORPORACION TEXTIL CELIA E.I.R.L.";
    private String empresaActividad = "VENTA DE ROPA PARA BEBES\nPRECIOS ESPECIALES PARA MAYORISTAS";
    private String empresaDireccion = "JR. GAMARRA NRO. 676 INT. 262\nLA VICTORIA - LIMA - LIMA";
    private String empresaRuc = "20609272016";
    private String empresaTelefonos = "";
    private String empresaEmail = "";
    private String bancoCuentaSoles = "191-9409603-0-93";
    private String bancoCuentaDolares = "00219100940960309350";

    /**
     * Genera y muestra el reporte de ticket en un visor
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
            JasperPrint jasperPrint = generarJasperPrint(resultado, detalles, datosCliente, datosVenta);
            String tipoComprobante = Metodos.getTipoComprobante(resultado.getNoFactu());
             /*
            // Mostrar en visor
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            String tipoComprobante = Metodos.getTipoComprobante(resultado.getNoFactu());
            //viewer.setTitle("TICKET " + tipoComprobante + " - " + resultado.getNoFactu());
            viewer.setTitle(tipoComprobante+" ELECTRONICA - " + resultado.getNoFactu());
            viewer.setVisible(true);
            */
            SwingUtilities.invokeLater(() -> {
                JasperViewer viewer = new JasperViewer(jasperPrint, false);
                viewer.setTitle(tipoComprobante + " ELECTRONICA - " + resultado.getNoFactu());

                // Configurar la ventana para que se muestre al frente
                viewer.setAlwaysOnTop(true);   // Temporalmente siempre al frente
                viewer.setVisible(true);
                viewer.toFront();              // Traer al frente
                viewer.requestFocus();         // Solicitar foco
                viewer.setAlwaysOnTop(false);  // Quitar siempre al frente después

                // Centrar en pantalla
                viewer.setLocationRelativeTo(null);

                // Estado normal
                viewer.setExtendedState(JFrame.NORMAL);
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar reporte ticket", e);
            Mensaje.error(null, "Error de Reporte",
                    "No se pudo generar el ticket: " + e.getMessage());
        }
    }

    /**
     * Genera e imprime directamente en la impresora térmica
     *
     * @param resultado Resultado de la emisión
     * @param detalles Lista de detalles
     * @param datosCliente Datos del cliente
     * @param datosVenta Datos de la venta
     * @param nombreImpresora Nombre de la impresora térmica (null para impresora por defecto)
     */
    public void imprimirDirecto(ResultadoEmision resultado,
                                List<DetalleVenta> detalles,
                                DatosCliente datosCliente,
                                DatosVenta datosVenta,
                                String nombreImpresora) {
        try {
            JasperPrint jasperPrint = generarJasperPrint(resultado, detalles, datosCliente, datosVenta);

            // Buscar la impresora
            PrintService printService = buscarImpresora(nombreImpresora);

            if (printService != null) {
                PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
                JasperPrintManager.printReport(jasperPrint, false);

                LOGGER.info("Ticket impreso correctamente en: " + printService.getName());
            } else {
                // Si no encuentra la impresora, mostrar en visor
                LOGGER.warning("Impresora no encontrada, mostrando en visor");
                generarReporte(resultado, detalles, datosCliente, datosVenta);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al imprimir ticket", e);
            Mensaje.error(null, "Error de Impresión",
                    "No se pudo imprimir el ticket: " + e.getMessage());
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
     * Prepara todos los parámetros del reporte
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

        // Datos de la empresa
        params.put("EMPRESA_NOMBRE", empresaNombre);
        params.put("EMPRESA_ACTIVIDAD", empresaActividad);
        params.put("EMPRESA_DIRECCION", empresaDireccion);
        params.put("EMPRESA_RUC", empresaRuc);
        params.put("EMPRESA_TELEFONOS", empresaTelefonos);
        params.put("EMPRESA_EMAIL", empresaEmail);
        params.put("BANCO_CUENTA_SOLES", bancoCuentaSoles);
        params.put("BANCO_CUENTA_DOLARES", bancoCuentaDolares);

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
        params.put("CLIENTE_DIRECCION", cliente.getDireccion() != null ? cliente.getDireccion() : "");

        // Fecha y hora
        params.put("FECHA_EMISION", venta.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        params.put("HORA_EMISION", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        // Condición de pago y otros
        params.put("CONDICION_PAGO", venta.getCondicionPago() != null ? venta.getCondicionPago() : "CONTADO");
        params.put("MONEDA", "S/");
        params.put("VENDEDOR", venta.getVendedor() != null ? venta.getVendedor() : "");
        params.put("ORDEN_COMPRA", resultado.getNoOrden() != null ? resultado.getNoOrden() : "");
        params.put("GUIA_REMISION", resultado.getNoGuia() != null ? resultado.getNoGuia() : "");

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

        // Código QR
        try {
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
     * Convierte DetalleVenta a DetalleComprobante
     */
    private List<DetalleComprobante> convertirDetalles(List<DetalleVenta> detalles, int porcentajeIgv) {
        List<DetalleComprobante> lista = new ArrayList<>();
        for (DetalleVenta dv : detalles) {
            lista.add(DetalleComprobante.fromDetalleVenta(dv, porcentajeIgv));
        }
        return lista;
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
     * Busca una impresora por nombre
     */
    private PrintService buscarImpresora(String nombreImpresora) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

        if (nombreImpresora == null || nombreImpresora.isEmpty()) {
            // Retornar impresora por defecto
            return PrintServiceLookup.lookupDefaultPrintService();
        }

        for (PrintService service : printServices) {
            if (service.getName().toLowerCase().contains(nombreImpresora.toLowerCase())) {
                return service;
            }
        }

        return null;
    }

    /**
     * Lista las impresoras disponibles en el sistema
     */
    public static List<String> listarImpresoras() {
        List<String> impresoras = new ArrayList<>();
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

        for (PrintService service : printServices) {
            impresoras.add(service.getName());
        }

        return impresoras;
    }

    // ==================== SETTERS PARA CONFIGURACIÓN ====================

    public void setEmpresaNombre(String empresaNombre) {
        this.empresaNombre = empresaNombre;
    }

    public void setEmpresaActividad(String empresaActividad) {
        this.empresaActividad = empresaActividad;
    }

    public void setEmpresaDireccion(String empresaDireccion) {
        this.empresaDireccion = empresaDireccion;
    }

    public void setEmpresaRuc(String empresaRuc) {
        this.empresaRuc = empresaRuc;
    }

    public void setEmpresaTelefonos(String empresaTelefonos) {
        this.empresaTelefonos = empresaTelefonos;
    }

    public void setEmpresaEmail(String empresaEmail) {
        this.empresaEmail = empresaEmail;
    }

    public void setBancoCuentaSoles(String bancoCuentaSoles) {
        this.bancoCuentaSoles = bancoCuentaSoles;
    }

    public void setBancoCuentaDolares(String bancoCuentaDolares) {
        this.bancoCuentaDolares = bancoCuentaDolares;
    }
}
