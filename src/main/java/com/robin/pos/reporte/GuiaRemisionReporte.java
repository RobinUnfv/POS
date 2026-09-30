package com.robin.pos.reporte;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.robin.pos.dao.GuiaRemisionDao;
import com.robin.pos.model.GuiaRemision;
import com.robin.pos.util.AppConfig;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.imageio.ImageIO;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Genera la representación impresa de la Guía de Remisión Electrónica Remitente.
 *
 * Plantilla : /com/robin/pos/reportes/guiaRemision.jrxml (se compila una sola vez)
 * Logo      : /com/robin/pos/imagenes/logo_empresa.png   (reemplácelo por el logo real)
 * QR        : URL devuelta por SUNAT en el CDR (ARGUIA.HASHCODE). Mientras la guía
 *             no tenga CDR se imprime un QR con los datos básicos del comprobante.
 *
 * Dependencias Maven: jasperreports, jasperreports-pdf, jasperreports-jdt (7.x) y com.google.zxing:core.
 *
 * Uso (siempre fuera del hilo de JavaFX, p.ej. dentro de un Task):
 * <pre>
 *   GuiaRemisionReporte rep = new GuiaRemisionReporte();
 *   rep.abrirPdf(idGuia);                         // genera un PDF temporal y lo abre
 *   rep.exportarPdf(idGuia, new File("T001-20.pdf"));
 * </pre>
 */
public class GuiaRemisionReporte {

    private static final Logger LOGGER = Logger.getLogger(GuiaRemisionReporte.class.getName());
    private static final String PLANTILLA = "/com/robin/pos/reportes/guiaRemision.jrxml";
    private static final String LOGO = "/com/robin/pos/imagenes/logo_empresa.png";
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static JasperReport reporteCompilado;

    private final GuiaRemisionDao dao = new GuiaRemisionDao();

    /** Compila la plantilla la primera vez y la reutiliza (compilar tarda 1-2 segundos). */
    private static synchronized JasperReport plantilla() throws JRException {
        if (reporteCompilado == null) {
            try (InputStream in = GuiaRemisionReporte.class.getResourceAsStream(PLANTILLA)) {
                if (in == null) {
                    throw new JRException("No se encontró la plantilla " + PLANTILLA);
                }
                reporteCompilado = JasperCompileManager.compileReport(in);
            } catch (IOException e) {
                throw new JRException("No se pudo leer la plantilla " + PLANTILLA, e);
            }
        }
        return reporteCompilado;
    }

    /** Llena el reporte a partir del ID de la guía (lee ARGUIA / ARGUIL). */
    public JasperPrint generar(long idGuia) throws JRException, SQLException {
        GuiaRemision guia = dao.obtenerPorId(idGuia);
        if (guia == null) {
            throw new JRException("No existe la guía con ID " + idGuia);
        }
        return generar(guia);
    }

    /** Llena el reporte con una guía ya cargada en memoria. */
    public JasperPrint generar(GuiaRemision g) throws JRException {
        Map<String, Object> p = new HashMap<>();

        p.put("EMPRESA_RUC", nvl(g.getRucRemit(), AppConfig.empresaRuc()));
        p.put("EMPRESA_NOMBRE", nvl(g.getRazonSocRemit(), AppConfig.empresaRazonSocial()));
        p.put("EMPRESA_DESCRIPCION", AppConfig.empresaDescripcion());
        p.put("EMPRESA_DIRECCION", AppConfig.empresaDireccion());
        p.put("EMPRESA_CONTACTO", contactoEmpresa());
        p.put("LOGO", cargarLogo());
        p.put("QR", generarQr(contenidoQr(g), 300));

        p.put("NUMERO_GUIA", g.getNumeroCompleto());
        p.put("FECHA_EMISION", g.getFecEmision() != null ? g.getFecEmision().format(FECHA) : "");
        p.put("HORA_EMISION", g.getHoraEmision());
        p.put("FECHA_INICIO", g.getFecInicioTraslado() != null ? g.getFecInicioTraslado().format(FECHA) : "");
        p.put("ESTADO", GuiaRemisionDao.descripcionEstado(g.getStsSunat()));

        p.put("DESTINATARIO", g.getRazonSocDestin());
        p.put("DESTINATARIO_DOC", nombreTipoDoc(g.getTipoDocDestin()) + " " + nvl(g.getNroDocDestin(), ""));
        p.put("PUNTO_PARTIDA", unir(g.getPartUbigeo(), g.getPartDirec()));
        p.put("PUNTO_LLEGADA", unir(g.getLlegdUbigeo(), g.getLlegdDireccion()
                + distritoProvincia(g.getLlegdDist(), g.getLlegdProv(), g.getLlegdDepar())));

        p.put("MOTIVO", g.getMotivoTraslado());
        p.put("MOTIVO_DESC", g.getDescMotivo());

        boolean publico = "01".equals(g.getModalidadTraslado());
        p.put("MODALIDAD", publico ? "TRANSPORTE PÚBLICO" : "TRANSPORTE PRIVADO");
        p.put("TRANSPORTISTA", publico
                ? "RUC " + nvl(g.getRucTransportista(), "") + " - " + nvl(g.getRazonSocTransp(), "")
                  + (vacio(g.getMtcTransportista()) ? "" : "   MTC: " + g.getMtcTransportista())
                : "-");
        p.put("VEHICULO", publico ? "-" : textoVehiculo(g));
        p.put("CONDUCTOR", publico ? "-" : textoConductor(g));

        BigDecimal peso = g.getPesoBrutoTotal();
        p.put("PESO", peso != null
                ? peso.setScale(3, RoundingMode.HALF_UP).toPlainString() + " " + nvl(g.getUndPeso(), "KGM")
                : "");
        p.put("BULTOS", g.getNroBultos() != null ? String.valueOf(g.getNroBultos()) : "-");
        p.put("DOC_REFERENCIA", docReferencia(g));
        p.put("OBSERVACIONES", g.getObservaciones());

        return JasperFillManager.fillReport(plantilla(), p, new JRBeanCollectionDataSource(g.getLineas()));
    }

    /** Genera el PDF en la carpeta temporal y lo abre con el visor predeterminado. */
    public File abrirPdf(long idGuia) throws JRException, SQLException, IOException {
        JasperPrint print = generar(idGuia);
        File pdf = File.createTempFile("GRE-" + idGuia + "-", ".pdf");
        pdf.deleteOnExit();
        JasperExportManager.exportReportToPdfFile(print, pdf.getAbsolutePath());
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            Desktop.getDesktop().open(pdf);
        } else {
            LOGGER.warning("El sistema no permite abrir archivos automáticamente: " + pdf);
        }
        return pdf;
    }

    /** Exporta la guía a un archivo PDF elegido por el usuario. */
    public File exportarPdf(long idGuia, File destino) throws JRException, SQLException {
        JasperPrint print = generar(idGuia);
        JasperExportManager.exportReportToPdfFile(print, destino.getAbsolutePath());
        return destino;
    }

    // ---------------------------------------------------------------- QR

    /**
     * Contenido del QR: la URL que devuelve SUNAT en el CDR (obligatoria en la
     * representación impresa). Antes de tener CDR se usa RUC|09|SERIE|NUMERO|FECHA|DOC DESTINATARIO.
     */
    private String contenidoQr(GuiaRemision g) {
        if (!vacio(g.getHashcode())) {
            return g.getHashcode();
        }
        return String.join("|",
                nvl(g.getRucRemit(), AppConfig.empresaRuc()), "09",
                nvl(g.getSerieElect(), ""), nvl(g.getCorrElect(), ""),
                g.getFecEmision() != null ? g.getFecEmision().format(FECHA) : "",
                nvl(g.getTipoDocDestin(), ""), nvl(g.getNroDocDestin(), ""));
    }

    public static BufferedImage generarQr(String contenido, int tamano) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix m = new QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, tamano, tamano, hints);
            BufferedImage img = new BufferedImage(m.getWidth(), m.getHeight(), BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < m.getWidth(); x++) {
                for (int y = 0; y < m.getHeight(); y++) {
                    img.setRGB(x, y, m.get(x, y) ? 0x000000 : 0xFFFFFF);
                }
            }
            return img;
        } catch (WriterException e) {
            LOGGER.log(Level.WARNING, "No se pudo generar el QR", e);
            return null;
        }
    }

    // ---------------------------------------------------------------- utilidades

    private static Image cargarLogo() {
        try (InputStream in = GuiaRemisionReporte.class.getResourceAsStream(LOGO)) {
            return in != null ? ImageIO.read(in) : null;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No se pudo cargar el logo " + LOGO, e);
            return null;
        }
    }

    private static String contactoEmpresa() {
        StringBuilder sb = new StringBuilder();
        if (!vacio(AppConfig.empresaTelefonos())) {
            sb.append("Telf.: ").append(AppConfig.empresaTelefonos());
        }
        if (!vacio(AppConfig.empresaEmail())) {
            if (!sb.isEmpty()) {
                sb.append("   ");
            }
            sb.append("Email: ").append(AppConfig.empresaEmail());
        }
        return sb.toString();
    }

    private static String textoVehiculo(GuiaRemision g) {
        if ("S".equals(g.getIndVehiculoM1L())) {
            return "Vehículo categoría M1 o L";
        }
        StringBuilder sb = new StringBuilder("Placa: ").append(nvl(g.getPlacaVehiculo(), "-"));
        if (!vacio(g.getMarcaVehiculo())) {
            sb.append("   Marca: ").append(g.getMarcaVehiculo());
        }
        if (!vacio(g.getCertInscripcion())) {
            sb.append("   Cert. Insc.: ").append(g.getCertInscripcion());
        }
        return sb.toString();
    }

    private static String textoConductor(GuiaRemision g) {
        if ("S".equals(g.getIndVehiculoM1L()) && vacio(g.getNroDocConduc())) {
            return "-";
        }
        String nombre = (nvl(g.getNombreConduc(), "") + " " + nvl(g.getApellidoConduc(), "")).trim();
        return nombre + "   " + nombreTipoDoc(g.getTipoDocConduc()) + ": " + nvl(g.getNroDocConduc(), "-")
                + "   Licencia: " + nvl(g.getBrevete(), "-");
    }

    private static String docReferencia(GuiaRemision g) {
        if (vacio(g.getSerieDocRef())) {
            return "-";
        }
        String tipo = switch (nvl(g.getTipoDocRef(), "")) {
            case "01" -> "Factura";
            case "03" -> "Boleta";
            case "09" -> "GRE Remitente";
            case "31" -> "GRE Transportista";
            default -> "Doc.";
        };
        String corr = nvl(g.getCorrDocRef(), "");
        try {
            corr = String.format("%08d", Long.parseLong(corr));
        } catch (NumberFormatException ignored) {
            // se deja tal cual
        }
        return tipo + " " + g.getSerieDocRef() + "-" + corr;
    }

    private static String nombreTipoDoc(String cod) {
        return switch (nvl(cod, "")) {
            case "6" -> "RUC";
            case "1" -> "DNI";
            case "4" -> "C.E.";
            case "7" -> "PAS.";
            case "0" -> "DOC.";
            default -> "DOC.";
        };
    }

    private static String distritoProvincia(String dist, String prov, String dep) {
        StringBuilder sb = new StringBuilder();
        for (String s : new String[]{dist, prov, dep}) {
            if (!vacio(s)) {
                sb.append(sb.isEmpty() ? " - " : " / ").append(s.trim());
            }
        }
        return sb.toString();
    }

    private static String unir(String ubigeo, String direccion) {
        return vacio(ubigeo) ? nvl(direccion, "") : ubigeo + " - " + nvl(direccion, "");
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    private static String nvl(String s, String def) {
        return vacio(s) ? def : s;
    }
}
