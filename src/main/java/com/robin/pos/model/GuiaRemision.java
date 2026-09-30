package com.robin.pos.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cabecera de la Guía de Remisión Electrónica Remitente (tipo 09 - GRE 2022).
 * Origen de datos: FACTU.ARPFFE (guía interna / despacho de la factura o boleta)
 * Destino:         FACTU.ARGUIA
 */
public class GuiaRemision {

    // ---- Identificación ----
    private Long idGuia;
    private String noCia;
    private String bodega;
    private String noGuia;             // N° guía interna ARPFFE
    private String serieElect;         // T001
    private String corrElect;          // 1..99999999 (sin ceros a la izquierda)
    private LocalDate fecEmision;
    private String horaEmision;
    private String observaciones;

    // ---- Remitente ----
    private String rucRemit;
    private String razonSocRemit;

    // ---- Destinatario ----
    private String noCliente;
    private String tipoDocDestin;      // Cat. 06: 6 RUC, 1 DNI, 4 CE, 7 Pasaporte
    private String nroDocDestin;
    private String razonSocDestin;

    // ---- Comprador (motivo 03) ----
    private String tipoDocCompr;
    private String nroDocCompr;
    private String razonSocCompr;

    // ---- Envío ----
    private String motivoTraslado;     // Cat. 20
    private String descMotivo;
    private String indTransbordo = "N";
    private String indVehiculoM1L = "N";
    private BigDecimal pesoBrutoTotal;
    private String undPeso = "KGM";
    private Integer nroBultos;
    private String modalidadTraslado = "02"; // 01 público, 02 privado
    private LocalDate fecInicioTraslado;

    // ---- Transportista (modalidad 01) ----
    private String rucTransportista;
    private String razonSocTransp;
    private String mtcTransportista;

    // ---- Vehículo y conductor (modalidad 02) ----
    private String placaVehiculo;
    private String marcaVehiculo;
    private String certInscripcion;
    private String tipoDocConduc = "1";
    private String nroDocConduc;
    private String nombreConduc;
    private String apellidoConduc;
    private String brevete;

    // ---- Punto de llegada ----
    private String llegdUbigeo;
    private String llegdDireccion;
    private String llegdCodEstab;
    private String llegdDist;
    private String llegdProv;
    private String llegdDepar;

    // ---- Punto de partida ----
    private String partUbigeo;
    private String partDirec;
    private String partCodEstab;

    // ---- Documento relacionado (Cat. 61) ----
    private String tipoDocRef;
    private String serieDocRef;
    private String corrDocRef;
    private String noFactuRef;
    private String rucEmisorDocRef;

    // ---- Estado SUNAT ----
    private String stsSunat = "P";
    private String descEstado;
    private String hashcode;           // URL del QR devuelta en el CDR
    private String usuario;

    private List<GuiaRemisionDetalle> lineas = new ArrayList<>();

    /** Número visible: T001-00000019 */
    public String getNumeroCompleto() {
        if (serieElect == null || corrElect == null) {
            return "";
        }
        try {
            return serieElect + "-" + String.format("%08d", Long.parseLong(corrElect));
        } catch (NumberFormatException e) {
            return serieElect + "-" + corrElect;
        }
    }

    public Long getIdGuia() { return idGuia; }
    public void setIdGuia(Long v) { idGuia = v; }
    public String getNoCia() { return noCia; }
    public void setNoCia(String v) { noCia = v; }
    public String getBodega() { return bodega; }
    public void setBodega(String v) { bodega = v; }
    public String getNoGuia() { return noGuia; }
    public void setNoGuia(String v) { noGuia = v; }
    public String getSerieElect() { return serieElect; }
    public void setSerieElect(String v) { serieElect = v; }
    public String getCorrElect() { return corrElect; }
    public void setCorrElect(String v) { corrElect = v; }
    public LocalDate getFecEmision() { return fecEmision; }
    public void setFecEmision(LocalDate v) { fecEmision = v; }
    public String getHoraEmision() { return horaEmision; }
    public void setHoraEmision(String v) { horaEmision = v; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String v) { observaciones = v; }

    public String getRucRemit() { return rucRemit; }
    public void setRucRemit(String v) { rucRemit = v; }
    public String getRazonSocRemit() { return razonSocRemit; }
    public void setRazonSocRemit(String v) { razonSocRemit = v; }

    public String getNoCliente() { return noCliente; }
    public void setNoCliente(String v) { noCliente = v; }
    public String getTipoDocDestin() { return tipoDocDestin; }
    public void setTipoDocDestin(String v) { tipoDocDestin = v; }
    public String getNroDocDestin() { return nroDocDestin; }
    public void setNroDocDestin(String v) { nroDocDestin = v; }
    public String getRazonSocDestin() { return razonSocDestin; }
    public void setRazonSocDestin(String v) { razonSocDestin = v; }

    public String getTipoDocCompr() { return tipoDocCompr; }
    public void setTipoDocCompr(String v) { tipoDocCompr = v; }
    public String getNroDocCompr() { return nroDocCompr; }
    public void setNroDocCompr(String v) { nroDocCompr = v; }
    public String getRazonSocCompr() { return razonSocCompr; }
    public void setRazonSocCompr(String v) { razonSocCompr = v; }

    public String getMotivoTraslado() { return motivoTraslado; }
    public void setMotivoTraslado(String v) { motivoTraslado = v; }
    public String getDescMotivo() { return descMotivo; }
    public void setDescMotivo(String v) { descMotivo = v; }
    public String getIndTransbordo() { return indTransbordo; }
    public void setIndTransbordo(String v) { indTransbordo = v; }
    public String getIndVehiculoM1L() { return indVehiculoM1L; }
    public void setIndVehiculoM1L(String v) { indVehiculoM1L = v; }
    public BigDecimal getPesoBrutoTotal() { return pesoBrutoTotal; }
    public void setPesoBrutoTotal(BigDecimal v) { pesoBrutoTotal = v; }
    public String getUndPeso() { return undPeso; }
    public void setUndPeso(String v) { undPeso = v; }
    public Integer getNroBultos() { return nroBultos; }
    public void setNroBultos(Integer v) { nroBultos = v; }
    public String getModalidadTraslado() { return modalidadTraslado; }
    public void setModalidadTraslado(String v) { modalidadTraslado = v; }
    public LocalDate getFecInicioTraslado() { return fecInicioTraslado; }
    public void setFecInicioTraslado(LocalDate v) { fecInicioTraslado = v; }

    public String getRucTransportista() { return rucTransportista; }
    public void setRucTransportista(String v) { rucTransportista = v; }
    public String getRazonSocTransp() { return razonSocTransp; }
    public void setRazonSocTransp(String v) { razonSocTransp = v; }
    public String getMtcTransportista() { return mtcTransportista; }
    public void setMtcTransportista(String v) { mtcTransportista = v; }

    public String getPlacaVehiculo() { return placaVehiculo; }
    public void setPlacaVehiculo(String v) { placaVehiculo = v; }
    public String getMarcaVehiculo() { return marcaVehiculo; }
    public void setMarcaVehiculo(String v) { marcaVehiculo = v; }
    public String getCertInscripcion() { return certInscripcion; }
    public void setCertInscripcion(String v) { certInscripcion = v; }
    public String getTipoDocConduc() { return tipoDocConduc; }
    public void setTipoDocConduc(String v) { tipoDocConduc = v; }
    public String getNroDocConduc() { return nroDocConduc; }
    public void setNroDocConduc(String v) { nroDocConduc = v; }
    public String getNombreConduc() { return nombreConduc; }
    public void setNombreConduc(String v) { nombreConduc = v; }
    public String getApellidoConduc() { return apellidoConduc; }
    public void setApellidoConduc(String v) { apellidoConduc = v; }
    public String getBrevete() { return brevete; }
    public void setBrevete(String v) { brevete = v; }

    public String getLlegdUbigeo() { return llegdUbigeo; }
    public void setLlegdUbigeo(String v) { llegdUbigeo = v; }
    public String getLlegdDireccion() { return llegdDireccion; }
    public void setLlegdDireccion(String v) { llegdDireccion = v; }
    public String getLlegdCodEstab() { return llegdCodEstab; }
    public void setLlegdCodEstab(String v) { llegdCodEstab = v; }
    public String getLlegdDist() { return llegdDist; }
    public void setLlegdDist(String v) { llegdDist = v; }
    public String getLlegdProv() { return llegdProv; }
    public void setLlegdProv(String v) { llegdProv = v; }
    public String getLlegdDepar() { return llegdDepar; }
    public void setLlegdDepar(String v) { llegdDepar = v; }

    public String getPartUbigeo() { return partUbigeo; }
    public void setPartUbigeo(String v) { partUbigeo = v; }
    public String getPartDirec() { return partDirec; }
    public void setPartDirec(String v) { partDirec = v; }
    public String getPartCodEstab() { return partCodEstab; }
    public void setPartCodEstab(String v) { partCodEstab = v; }

    public String getTipoDocRef() { return tipoDocRef; }
    public void setTipoDocRef(String v) { tipoDocRef = v; }
    public String getSerieDocRef() { return serieDocRef; }
    public void setSerieDocRef(String v) { serieDocRef = v; }
    public String getCorrDocRef() { return corrDocRef; }
    public void setCorrDocRef(String v) { corrDocRef = v; }
    public String getNoFactuRef() { return noFactuRef; }
    public void setNoFactuRef(String v) { noFactuRef = v; }
    public String getRucEmisorDocRef() { return rucEmisorDocRef; }
    public void setRucEmisorDocRef(String v) { rucEmisorDocRef = v; }

    public String getStsSunat() { return stsSunat; }
    public void setStsSunat(String v) { stsSunat = v; }
    public String getDescEstado() { return descEstado; }
    public void setDescEstado(String v) { descEstado = v; }
    public String getHashcode() { return hashcode; }
    public void setHashcode(String v) { hashcode = v; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String v) { usuario = v; }

    public List<GuiaRemisionDetalle> getLineas() { return lineas; }
    public void setLineas(List<GuiaRemisionDetalle> v) { lineas = v != null ? v : new ArrayList<>(); }
}
