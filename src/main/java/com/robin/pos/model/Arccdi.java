package com.robin.pos.model;

public class Arccdi {

    private String codiDist;
    private String descDist;

    public Arccdi() {
    }
    public Arccdi(String codiDist, String descDist) {
        this.codiDist = codiDist;
        this.descDist = descDist;
    }
    public String getCodiDist() {
        return codiDist;
    }
    public void setCodiDist(String codiDist) {
        this.codiDist = codiDist;
    }
    public String getDescDist() {
        return descDist;
    }
    public void setDescDist(String descDist) {
        this.descDist = descDist;
    }

}
