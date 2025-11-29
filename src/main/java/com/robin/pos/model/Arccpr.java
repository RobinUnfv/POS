package com.robin.pos.model;

public class Arccpr {

    private String codiProv;
    private String descProv;

    public Arccpr(String codiProv, String descProv) {
        this.codiProv = codiProv;
        this.descProv = descProv;
    }

    public Arccpr() {}

    public String getCodiProv() {
        return codiProv;
    }

    public void setCodiProv(String codiProv) {
        this.codiProv = codiProv;
    }

    public String getDescProv() {
        return descProv;
    }

    public void setDescProv(String descProv) {
        this.descProv = descProv;
    }

}
