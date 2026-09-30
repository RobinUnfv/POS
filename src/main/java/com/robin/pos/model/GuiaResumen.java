package com.robin.pos.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fila de la lista de guías (vista FACTU.V_GUIA_PENDIENTE).
 */
public class GuiaResumen {

    private final ObjectProperty<Long> idGuia = new SimpleObjectProperty<>();
    private final StringProperty bodega = new SimpleStringProperty();
    private final StringProperty noGuia = new SimpleStringProperty();
    private final StringProperty serieElect = new SimpleStringProperty();
    private final StringProperty corrElect = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> fecEmision = new SimpleObjectProperty<>();
    private final StringProperty tipoGuia = new SimpleStringProperty();
    private final StringProperty rucRemit = new SimpleStringProperty();
    private final StringProperty stsSunat = new SimpleStringProperty();
    private final StringProperty cdrSts = new SimpleStringProperty();
    private final IntegerProperty intentosEnvio = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDateTime> fecEnvio = new SimpleObjectProperty<>();
    private final StringProperty ticketSunat = new SimpleStringProperty();
    private final StringProperty resulSunat = new SimpleStringProperty();
    private final StringProperty descEstado = new SimpleStringProperty();
    // Datos complementarios (de FACTU.ARGUIA)
    private final StringProperty destinatario = new SimpleStringProperty();
    private final StringProperty nroDocDestin = new SimpleStringProperty();
    private final StringProperty docReferencia = new SimpleStringProperty();
    private final StringProperty motivo = new SimpleStringProperty();
    private final ObjectProperty<BigDecimal> pesoBruto = new SimpleObjectProperty<>();

    /** T001-00000019 */
    public String getNumeroCompleto() {
        String s = getSerieElect();
        String c = getCorrElect();
        if (s == null || c == null) {
            return "";
        }
        try {
            return s + "-" + String.format("%08d", Long.parseLong(c));
        } catch (NumberFormatException e) {
            return s + "-" + c;
        }
    }

    public Long getIdGuia() { return idGuia.get(); }
    public void setIdGuia(Long v) { idGuia.set(v); }
    public ObjectProperty<Long> idGuiaProperty() { return idGuia; }

    public String getBodega() { return bodega.get(); }
    public void setBodega(String v) { bodega.set(v); }
    public StringProperty bodegaProperty() { return bodega; }

    public String getNoGuia() { return noGuia.get(); }
    public void setNoGuia(String v) { noGuia.set(v); }
    public StringProperty noGuiaProperty() { return noGuia; }

    public String getSerieElect() { return serieElect.get(); }
    public void setSerieElect(String v) { serieElect.set(v); }
    public StringProperty serieElectProperty() { return serieElect; }

    public String getCorrElect() { return corrElect.get(); }
    public void setCorrElect(String v) { corrElect.set(v); }
    public StringProperty corrElectProperty() { return corrElect; }

    public LocalDate getFecEmision() { return fecEmision.get(); }
    public void setFecEmision(LocalDate v) { fecEmision.set(v); }
    public ObjectProperty<LocalDate> fecEmisionProperty() { return fecEmision; }

    public String getTipoGuia() { return tipoGuia.get(); }
    public void setTipoGuia(String v) { tipoGuia.set(v); }
    public StringProperty tipoGuiaProperty() { return tipoGuia; }

    public String getRucRemit() { return rucRemit.get(); }
    public void setRucRemit(String v) { rucRemit.set(v); }
    public StringProperty rucRemitProperty() { return rucRemit; }

    public String getStsSunat() { return stsSunat.get(); }
    public void setStsSunat(String v) { stsSunat.set(v); }
    public StringProperty stsSunatProperty() { return stsSunat; }

    public String getCdrSts() { return cdrSts.get(); }
    public void setCdrSts(String v) { cdrSts.set(v); }
    public StringProperty cdrStsProperty() { return cdrSts; }

    public int getIntentosEnvio() { return intentosEnvio.get(); }
    public void setIntentosEnvio(int v) { intentosEnvio.set(v); }
    public IntegerProperty intentosEnvioProperty() { return intentosEnvio; }

    public LocalDateTime getFecEnvio() { return fecEnvio.get(); }
    public void setFecEnvio(LocalDateTime v) { fecEnvio.set(v); }
    public ObjectProperty<LocalDateTime> fecEnvioProperty() { return fecEnvio; }

    public String getTicketSunat() { return ticketSunat.get(); }
    public void setTicketSunat(String v) { ticketSunat.set(v); }
    public StringProperty ticketSunatProperty() { return ticketSunat; }

    public String getResulSunat() { return resulSunat.get(); }
    public void setResulSunat(String v) { resulSunat.set(v); }
    public StringProperty resulSunatProperty() { return resulSunat; }

    public String getDescEstado() { return descEstado.get(); }
    public void setDescEstado(String v) { descEstado.set(v); }
    public StringProperty descEstadoProperty() { return descEstado; }

    public String getDestinatario() { return destinatario.get(); }
    public void setDestinatario(String v) { destinatario.set(v); }
    public StringProperty destinatarioProperty() { return destinatario; }

    public String getNroDocDestin() { return nroDocDestin.get(); }
    public void setNroDocDestin(String v) { nroDocDestin.set(v); }
    public StringProperty nroDocDestinProperty() { return nroDocDestin; }

    public String getDocReferencia() { return docReferencia.get(); }
    public void setDocReferencia(String v) { docReferencia.set(v); }
    public StringProperty docReferenciaProperty() { return docReferencia; }

    public String getMotivo() { return motivo.get(); }
    public void setMotivo(String v) { motivo.set(v); }
    public StringProperty motivoProperty() { return motivo; }

    public BigDecimal getPesoBruto() { return pesoBruto.get(); }
    public void setPesoBruto(BigDecimal v) { pesoBruto.set(v); }
    public ObjectProperty<BigDecimal> pesoBrutoProperty() { return pesoBruto; }
}
