package com.robin.pos.model;

import java.time.LocalDate;

/**
 * Modelo para Comprobante de Pago que será dado de baja
 * Usado en la Comunicación de Baja a SUNAT
 *
 * @author Robin POS
 * @version 1.0
 */
public class ComprobanteBaja {

    private String noCia;
    private String tipoDocumento;           // F=Factura, B=Boleta, N=Nota de Crédito
    private String numeroComprobante;       // Número completo: F001-00000123
    private LocalDate fechaEmision;
    private String nombreCliente;
    private String documentoCliente;
    private Double total;
    private String codigoMotivo;           // 01-08 según catálogo SUNAT
    private String descripcionMotivo;
    private LocalDate fechaBaja;
    private String estadoBaja;             // PENDIENTE, ENVIADO, ACEPTADO, RECHAZADO
    private String ticketSunat;            // Número de ticket SUNAT

    // ==================== CONSTRUCTORES ====================

    public ComprobanteBaja() {
        this.fechaBaja = LocalDate.now();
        this.estadoBaja = "PENDIENTE";
    }

    public ComprobanteBaja(String noCia, String tipoDocumento, String numeroComprobante,
                           LocalDate fechaEmision, String nombreCliente, String documentoCliente,
                           Double total, String codigoMotivo, String descripcionMotivo) {
        this.noCia = noCia;
        this.tipoDocumento = tipoDocumento;
        this.numeroComprobante = numeroComprobante;
        this.fechaEmision = fechaEmision;
        this.nombreCliente = nombreCliente;
        this.documentoCliente = documentoCliente;
        this.total = total;
        this.codigoMotivo = codigoMotivo;
        this.descripcionMotivo = descripcionMotivo;
    }

    // ==================== GETTERS Y SETTERS ====================

    public String getNoCia() {
        return noCia;
    }

    public void setNoCia(String noCia) {
        this.noCia = noCia;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getDocumentoCliente() {
        return documentoCliente;
    }

    public void setDocumentoCliente(String documentoCliente) {
        this.documentoCliente = documentoCliente;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getCodigoMotivo() {
        return codigoMotivo;
    }

    public void setCodigoMotivo(String codigoMotivo) {
        this.codigoMotivo = codigoMotivo;
    }

    public String getDescripcionMotivo() {
        return descripcionMotivo;
    }

    public void setDescripcionMotivo(String descripcionMotivo) {
        this.descripcionMotivo = descripcionMotivo;
    }

    public LocalDate getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(LocalDate fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public String getEstadoBaja() {
        return estadoBaja;
    }

    public void setEstadoBaja(String estadoBaja) {
        this.estadoBaja = estadoBaja;
    }

    public String getTicketSunat() {
        return ticketSunat;
    }

    public void setTicketSunat(String ticketSunat) {
        this.ticketSunat = ticketSunat;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Obtiene la descripción del tipo de documento
     */
    public String getTipoDocumentoDescripcion() {
        if (tipoDocumento == null) return "";

        return switch (tipoDocumento) {
            case "F" -> "FACTURA";
            case "B" -> "BOLETA";
            case "N" -> "NOTA CRÉDITO";
            default -> tipoDocumento;
        };
    }

    /**
     * Obtiene el código SUNAT del tipo de documento
     */
    public String getTipoDocumentoSunat() {
        if (tipoDocumento == null) return "01";

        return switch (tipoDocumento) {
            case "F" -> "01";
            case "B" -> "03";
            case "N" -> "07";
            default -> "01";
        };
    }

    /**
     * Obtiene la serie del comprobante
     * Ejemplo: F001-00000123 -> F001
     */
    public String getSerie() {
        if (numeroComprobante == null || numeroComprobante.length() < 4) {
            return "";
        }
        return numeroComprobante.substring(0, 4);
    }

    /**
     * Obtiene el correlativo del comprobante
     * Ejemplo: F001-00000123 -> 00000123
     */
    public String getCorrelativo() {
        if (numeroComprobante == null || numeroComprobante.length() <= 4) {
            return "";
        }
        return numeroComprobante.substring(4);
    }

    /**
     * Valida si el comprobante está dentro del plazo para baja (7 días)
     */
    public boolean estaDentroDePlazo() {
        if (fechaEmision == null) return false;

        long diasTranscurridos = java.time.temporal.ChronoUnit.DAYS
                .between(fechaEmision, LocalDate.now());

        return diasTranscurridos <= 7;
    }

    /**
     * Obtiene los días restantes para dar de baja
     */
    public long getDiasRestantes() {
        if (fechaEmision == null) return 0;

        long diasTranscurridos = java.time.temporal.ChronoUnit.DAYS
                .between(fechaEmision, LocalDate.now());

        return Math.max(0, 7 - diasTranscurridos);
    }

    // ==================== MÉTODOS OVERRIDE ====================

    @Override
    public String toString() {
        return "ComprobanteBaja{" +
                "numeroComprobante='" + numeroComprobante + '\'' +
                ", tipoDocumento='" + tipoDocumento + '\'' +
                ", fechaEmision=" + fechaEmision +
                ", total=" + total +
                ", codigoMotivo='" + codigoMotivo + '\'' +
                ", estadoBaja='" + estadoBaja + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComprobanteBaja that = (ComprobanteBaja) o;

        return numeroComprobante != null &&
                numeroComprobante.equals(that.numeroComprobante);
    }

    @Override
    public int hashCode() {
        return numeroComprobante != null ? numeroComprobante.hashCode() : 0;
    }
}
