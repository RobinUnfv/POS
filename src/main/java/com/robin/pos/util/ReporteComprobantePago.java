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
    private static final String NO_CIA = "01"; // Código de compañía

    // DAOs para obtener datos dinámicamente
    private final ArfamcDao arfamcDao = new ArfamcDao();
    private final SucursalPtovtaDao sucursalDao = new SucursalPtovtaDao();

    // Cache de datos de empresa y sucursal
    private Arfamc datosEmpresa;
    private SucursalPtovta datosSucursal;

    /**
     * Constructor que carga los datos de la empresa y sucursal
     */
    public ReporteComprobantePago() {
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
                datosEmpresa = new Arfamc(); // Crear objeto vacío para evitar NPE
            }

            // Obtener la primera sucursal activa
            List<SucursalPtovta> sucursales = sucursalDao.listarSucursales(NO_CIA);
            if (!sucursales.isEmpty()) {
                // Buscar sucursal activa o tomar la primera
                datosSucursal = sucursales.stream()
                        .filter(s -> "A".equals(s.getEstadoSuc()))
                        .findFirst()
                        .orElse(sucursales.get(0));
            } else {
                LOGGER.warning("No se encontraron sucursales");
                datosSucursal = new SucursalPtovta(); // Crear objeto vacío
            }

            LOGGER.info("Datos de empresa y sucursal cargados correctamente");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar datos de empresa", e);
            // Inicializar objetos vacíos para evitar NPE
            datosEmpresa = new Arfamc();
            datosSucursal = new SucursalPtovta();
        }
    }

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

            String tipoComprobante = Metodos.getTipoComprobante(resultado.getNoFactu());
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
            LOGGER.log(Level.SEVERE, "Error al generar reporte", e);
            Mensaje.error(null, "Error de Reporte",
                    "No se pudo generar el reporte: " + e.getMessage());
        }
    }

    public void generarReportePDf(ResultadoEmision resultado,
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

            String carpetaDescargas = GestorDescargas.getCarpetaDescargas();
            String nombreArchivo = carpetaDescargas + "/" + resultado.getNoFactu()+ ".pdf";
            JasperExportManager.exportReportToPdfFile(jasperPrint, nombreArchivo);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar PDF", e);
            Mensaje.error(null, "Error de Reporte",
                    "No se pudo generar el PDF: " + e.getMessage());
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
     * Prepara todos los parámetros para el reporte usando datos de BD
     */
    private Map<String, Object> prepararParametros(ResultadoEmision resultado,
                                                   DatosCliente cliente,
                                                   DatosVenta venta,
                                                   List<DetalleVenta> detalles) {

        Map<String, Object> params = new HashMap<>();
        String tipoComprobante = Metodos.getTipoComprobante(resultado.getNoFactu());
        params.put("TIPO_COMPROBANTE", tipoComprobante);
        String tipoDocumentoCliente = Metodos.getTipoDocumentoCliente( resultado.getNoFactu(), resultado.getNoCliente() );
        params.put("TIP_DOC_CLI", tipoDocumentoCliente);

        // ==================== DATOS DE LA EMPRESA DESDE BD ====================
        if (datosEmpresa != null) {
            // Nombre comercial
            params.put("EMPRESA_NOMBRE",
                    datosEmpresa.getNombre() != null ? datosEmpresa.getNombre() : "");

            // Actividad comercial (usar descripción)
            params.put("EMPRESA_ACTIVIDAD",
                    datosEmpresa.getDescripcion() != null ? datosEmpresa.getDescripcion() : "");

            // RUC
            params.put("EMPRESA_RUC",
                    datosEmpresa.getRuc() != null ? datosEmpresa.getRuc() : "");

            // Cuentas bancarias
            params.put("BANCO_CUENTA_SOLES",
                    datosEmpresa.getCuentaSol() != null ? datosEmpresa.getCuentaSol() : "");

            params.put("BANCO_CUENTA_DOLARES",
                    datosEmpresa.getCci() != null ? datosEmpresa.getCci() : "");
        } else {
            // Valores por defecto si no hay datos
            params.put("EMPRESA_NOMBRE", "");
            params.put("EMPRESA_ACTIVIDAD", "");
            params.put("EMPRESA_RUC", "");
            params.put("BANCO_CUENTA_SOLES", "");
            params.put("BANCO_CUENTA_DOLARES", "");
        }

        // ==================== DATOS DE LA SUCURSAL DESDE BD ====================
        if (datosSucursal != null) {
            // Dirección completa de la sucursal
            params.put("EMPRESA_DIRECCION",
                    datosSucursal.getDireccion() != null ? datosSucursal.getDireccion() : "");

            // Teléfonos de la sucursal (concatenar si hay dos)
            StringBuilder telefonos = new StringBuilder();
            if (datosSucursal.getTelef1() != null && !datosSucursal.getTelef1().isEmpty()) {
                telefonos.append(datosSucursal.getTelef1());
            }
            if (datosSucursal.getTelef2() != null && !datosSucursal.getTelef2().isEmpty()) {
                if (telefonos.length() > 0) telefonos.append(" / ");
                telefonos.append(datosSucursal.getTelef2());
            }
            params.put("EMPRESA_TELEFONOS", telefonos.toString());

            // Email de la sucursal
            params.put("EMPRESA_EMAIL",
                    datosSucursal.getCorreoElectro() != null ? datosSucursal.getCorreoElectro() : "");
        } else {
            // Valores por defecto
            params.put("EMPRESA_DIRECCION", "");
            params.put("EMPRESA_TELEFONOS", "");
            params.put("EMPRESA_EMAIL", "");
        }

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
        params.put("ORDEN_COMPRA", resultado.getNoOrden() != null ? resultado.getNoOrden() : "-----");
        params.put("GUIA_REMISION", resultado.getNoGuia() != null ? resultado.getNoGuia() : "-----");

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
                    datosEmpresa != null ? datosEmpresa.getRuc() : "",
                    resultado.getNumeroComprobanteFormateado(),
                    igvTotal,
                    totalConIgv,
                    venta.getFechaEmision(),
                    cliente.getTipoDocumento(),
                    cliente.getNumeroDocumento()
            );
            params.put("QR_CODE_IMAGE", null); // Nexer no quiere el QR
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
                LOGGER.info("Sucursal establecida: " + codSucursal + "-" + codPtoVta);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo establecer la sucursal", e);
        }
    }
}