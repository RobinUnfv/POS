package com.robin.pos.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GeneradorQR {
    private static final Logger LOGGER = Logger.getLogger(GeneradorQR.class.getName());

    // Tamaño por defecto del código QR
    private static final int DEFAULT_WIDTH = 200;
    private static final int DEFAULT_HEIGHT = 200;

    public static BufferedImage generarQRImage(String contenido, int ancho, int alto) throws WriterException {
        // Configurar hints para el QR
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1); // Margen mínimo

        // Crear el escritor QR
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(contenido, BarcodeFormat.QR_CODE, ancho, alto, hints);

        // Convertir a imagen
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * Genera un código QR como BufferedImage con tamaño por defecto
     */
    public static BufferedImage generarQRImage(String contenido) throws WriterException {
        return generarQRImage(contenido, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Genera un código QR y lo devuelve como InputStream
     * (Útil para JasperReports)
     *
     * @param contenido Texto a codificar
     * @param ancho     Ancho de la imagen
     * @param alto      Alto de la imagen
     * @return InputStream con la imagen PNG del QR
     */
    public static InputStream generarQRInputStream(String contenido, int ancho, int alto) {
        try {
            BufferedImage image = generarQRImage(contenido, ancho, alto);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar QR como InputStream", e);
            return null;
        }
    }

    /**
     * Genera un código QR y lo devuelve como InputStream con tamaño por defecto
     */
    public static InputStream generarQRInputStream(String contenido) {
        return generarQRInputStream(contenido, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Genera un código QR y lo guarda como archivo
     *
     * @param contenido Texto a codificar
     * @param rutaArchivo Ruta donde guardar la imagen
     * @param ancho Ancho de la imagen
     * @param alto Alto de la imagen
     * @return true si se guardó correctamente
     */
    public static boolean generarQRArchivo(String contenido, String rutaArchivo, int ancho, int alto) {
        try {
            BufferedImage image = generarQRImage(contenido, ancho, alto);
            File outputFile = new File(rutaArchivo);

            // Determinar formato por extensión
            String extension = rutaArchivo.substring(rutaArchivo.lastIndexOf('.') + 1).toUpperCase();
            if (!extension.equals("PNG") && !extension.equals("JPG") && !extension.equals("JPEG")) {
                extension = "PNG";
            }

            ImageIO.write(image, extension, outputFile);
            LOGGER.info("Código QR guardado en: " + rutaArchivo);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al guardar QR en archivo", e);
            return false;
        }
    }

    /**
     * Genera un código QR como array de bytes
     *
     * @param contenido Texto a codificar
     * @param ancho Ancho de la imagen
     * @param alto Alto de la imagen
     * @return byte[] con la imagen PNG
     */
    public static byte[] generarQRBytes(String contenido, int ancho, int alto) {
        try {
            BufferedImage image = generarQRImage(contenido, ancho, alto);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar QR como bytes", e);
            return null;
        }
    }

    /**
     * Genera el código QR para comprobantes electrónicos según formato SUNAT
     *
     * Formato SUNAT:
     * RUC|TIPO_DOC|SERIE|NUMERO|IGV|TOTAL|FECHA|TIPO_DOC_CLIENTE|NUM_DOC_CLIENTE|HASH
     *
     * Ejemplo:
     * 20609272016|01|F001|00000499|100.83|661.00|2025-04-29|6|20127649872|
     *
     * @param rucEmisor RUC del emisor (11 dígitos)
     * @param tipoComprobante "01" para Factura, "03" para Boleta
     * @param serie Serie del comprobante (ej: F001, B001)
     * @param numero Número correlativo
     * @param igv Monto del IGV
     * @param total Importe total
     * @param fechaEmision Fecha de emisión
     * @param tipoDocCliente "6" para RUC, "1" para DNI, etc.
     * @param numDocCliente Número de documento del cliente
     * @param hashCpe Hash del comprobante (opcional, puede ser vacío)
     * @return InputStream con la imagen del QR
     */
    public static InputStream generarQRSunat(String rucEmisor,
                                             String tipoComprobante,
                                             String serie,
                                             String numero,
                                             BigDecimal igv,
                                             BigDecimal total,
                                             LocalDate fechaEmision,
                                             String tipoDocCliente,
                                             String numDocCliente,
                                             String hashCpe) {

        // Formatear fecha
        String fecha = fechaEmision.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Formatear montos con 2 decimales
        String igvStr = igv.setScale(2, RoundingMode.HALF_UP).toString();
        String totalStr = total.setScale(2, RoundingMode.HALF_UP).toString();

        // Construir cadena según formato SUNAT
        StringBuilder qrData = new StringBuilder();
        qrData.append(rucEmisor).append("|");
        qrData.append(tipoComprobante).append("|");
        qrData.append(serie).append("|");
        qrData.append(numero).append("|");
        qrData.append(igvStr).append("|");
        qrData.append(totalStr).append("|");
        qrData.append(fecha).append("|");
        qrData.append(tipoDocCliente).append("|");
        qrData.append(numDocCliente).append("|");
        qrData.append(hashCpe != null ? hashCpe : "");

        LOGGER.info("QR SUNAT generado: " + qrData.toString());

        return generarQRInputStream(qrData.toString());
    }

    /**
     * Genera QR SUNAT con parámetros simplificados
     *
     * @param rucEmisor RUC del emisor
     * @param numeroComprobante Número completo (ej: F001-00000499 o B001-00000026)
     * @param igv Monto del IGV
     * @param total Importe total
     * @param fechaEmision Fecha de emisión
     * @param tipoDocCliente Tipo de documento del cliente
     * @param numDocCliente Número de documento del cliente
     * @return InputStream con la imagen del QR
     */
    public static InputStream generarQRSunat(String rucEmisor,
                                             String numeroComprobante,
                                             BigDecimal igv,
                                             BigDecimal total,
                                             LocalDate fechaEmision,
                                             String tipoDocCliente,
                                             String numDocCliente) {

        // Extraer serie y número del comprobante
        String serie;
        String numero;
        String tipoComprobante;

        if (numeroComprobante.contains("-")) {
            String[] partes = numeroComprobante.split("-");
            serie = partes[0];
            numero = partes[1];
        } else if (numeroComprobante.length() >= 4) {
            serie = numeroComprobante.substring(0, 4);
            numero = numeroComprobante.substring(4);
        } else {
            serie = numeroComprobante;
            numero = "0";
        }

        // Determinar tipo de comprobante por la serie
        if (serie.startsWith("F")) {
            tipoComprobante = "01"; // Factura
        } else if (serie.startsWith("B")) {
            tipoComprobante = "03"; // Boleta
        } else {
            tipoComprobante = "01"; // Por defecto Factura
        }

        // Convertir tipo de documento del cliente
        String tipoDocSunat = convertirTipoDocumentoSunat(tipoDocCliente);

        return generarQRSunat(rucEmisor, tipoComprobante, serie, numero,
                igv, total, fechaEmision, tipoDocSunat, numDocCliente, "");
    }

    /**
     * Convierte el tipo de documento al código SUNAT
     */
    private static String convertirTipoDocumentoSunat(String tipoDoc) {
        if (tipoDoc == null) return "0";

        switch (tipoDoc.toUpperCase()) {
            case "DNI":
                return "1";
            case "CE":
            case "CARNET DE EXTRANJERIA":
                return "4";
            case "RUC":
                return "6";
            case "PASAPORTE":
                return "7";
            default:
                return "0"; // Otros
        }
    }

}
