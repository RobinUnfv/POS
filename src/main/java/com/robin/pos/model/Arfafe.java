package com.robin.pos.model;

public class Arfafe {
    private String tipoDoc;
    private String tipoCliente;
    private String tipoCambio;
    private String indDoc;
    private String mDsctoGlobal;

    public Arfafe(){}

    public Arfafe(String tipoDoc, String tipoCliente, String tipoCambio, String indDoc, String mDsctoGlobal) {
        this.tipoDoc = tipoDoc;
        this.tipoCliente = tipoCliente;
        this.tipoCambio = tipoCambio;
        this.indDoc = indDoc;
        this.mDsctoGlobal = mDsctoGlobal;
    }

    public String getTipoDoc() {
        return tipoDoc;
    }

    public void setTipoDoc(String tipoDoc) {
        this.tipoDoc = tipoDoc;
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    public String getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(String tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public String getIndDoc() {
        return indDoc;
    }

    public void setIndDoc(String indDoc) {
        this.indDoc = indDoc;
    }

    public String getmDsctoGlobal() {
        return mDsctoGlobal;
    }

    public void setmDsctoGlobal(String mDsctoGlobal) {
        this.mDsctoGlobal = mDsctoGlobal;
    }
}
