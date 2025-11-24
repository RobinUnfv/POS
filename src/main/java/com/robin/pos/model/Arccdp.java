package com.robin.pos.model;

public class Arccdp {

    private String codDepa;
    private String desDepa;

    public Arccdp(){}

    public Arccdp(String codDepa, String desDepa) {
        this.codDepa = codDepa;
        this.desDepa = desDepa;
    }

    public String getCodDepa() {
        return codDepa;
    }
    public void setCodDepa(String codDepa) {
        this.codDepa = codDepa;
    }

    public String getDesDepa() {
        return desDepa;
    }
    public void setDesDepa(String desDepa) {
        this.desDepa = desDepa;
    }

}
