package com.robin.pos.model;

public class ResultadoEmision {
    // Indicador de éxito de la operación
    private boolean exito;
    private String mensaje;
    private String codigoError;
    private String xmlRespuesta;

    // Campos mapeados del XML de respuesta EXITOSA
    private String noCia;
    private String noCliente;
    private String noOrden;
    private String noGuia;
    private String noFactu;
    private String fecha;
    private String resultadoOracle;  // "OK" o "ERROR"

    // Campos derivados (extraídos de noFactu)
    private String serie;        // Ej: "B001" de "B0010000026"
    private String correlativo;  // Ej: "0000026" de "B0010000026"

    // Campos adicionales de ERROR
    private String nomProceso;   // Nombre del proceso que falló
    private String msjError;     // Mensaje de error específico
    private String fecProceso;   // Fecha del proceso

    // ========== GETTERS Y SETTERS ==========

    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getCodigoError() { return codigoError; }
    public void setCodigoError(String codigoError) { this.codigoError = codigoError; }

    public String getXmlRespuesta() { return xmlRespuesta; }
    public void setXmlRespuesta(String xmlRespuesta) { this.xmlRespuesta = xmlRespuesta; }

    public String getNoCia() { return noCia; }
    public void setNoCia(String noCia) { this.noCia = noCia; }

    public String getNoCliente() { return noCliente; }
    public void setNoCliente(String noCliente) { this.noCliente = noCliente; }

    public String getNoOrden() { return noOrden; }
    public void setNoOrden(String noOrden) { this.noOrden = noOrden; }

    public String getNoGuia() { return noGuia; }
    public void setNoGuia(String noGuia) { this.noGuia = noGuia; }

    public String getNoFactu() { return noFactu; }
    public void setNoFactu(String noFactu) { this.noFactu = noFactu; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getResultadoOracle() { return resultadoOracle; }
    public void setResultadoOracle(String resultadoOracle) { this.resultadoOracle = resultadoOracle; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public String getCorrelativo() { return correlativo; }
    public void setCorrelativo(String correlativo) { this.correlativo = correlativo; }

    public String getNomProceso() { return nomProceso; }
    public void setNomProceso(String nomProceso) { this.nomProceso = nomProceso; }

    public String getMsjError() { return msjError; }
    public void setMsjError(String msjError) { this.msjError = msjError; }

    public String getFecProceso() { return fecProceso; }
    public void setFecProceso(String fecProceso) { this.fecProceso = fecProceso; }

    /**
     * Obtiene el número de comprobante formateado (Serie-Correlativo)
     * Ej: "B001-0000026"
     */
    public String getNumeroComprobanteFormateado() {
        if (serie != null && correlativo != null) {
            return serie + "-" + correlativo;
        }
        return noFactu;
    }

    /**
     * Verifica si la respuesta contiene un error
     */
    public boolean tieneError() {
        return "ERROR".equalsIgnoreCase(resultadoOracle);
    }

    @Override
    public String toString() {
        if (exito) {
            return "ResultadoEmision{" +
                    "exito=true" +
                    ", noCia='" + noCia + '\'' +
                    ", noCliente='" + noCliente + '\'' +
                    ", noOrden='" + noOrden + '\'' +
                    ", noGuia='" + noGuia + '\'' +
                    ", noFactu='" + noFactu + '\'' +
                    ", fecha='" + fecha + '\'' +
                    ", resultado='" + resultadoOracle + '\'' +
                    '}';
        } else {
            return "ResultadoEmision{" +
                    "exito=false" +
                    ", codigoError='" + codigoError + '\'' +
                    ", mensaje='" + mensaje + '\'' +
                    ", nomProceso='" + nomProceso + '\'' +
                    ", fecha='" + fecha + '\'' +
                    '}';
        }
    }
}
