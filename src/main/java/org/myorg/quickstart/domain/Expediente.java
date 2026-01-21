package org.myorg.quickstart.domain;

public class Expediente {
    private int id;
    private String codigo;
    private String fechaApertura;
    private String sucursal;

    public Expediente() {
    }

    public Expediente(int id, String codigo, String fechaApertura, String sucursal) {
        this.id = id;
        this.codigo = codigo;
        this.fechaApertura = fechaApertura;
        this.sucursal = sucursal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(String fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }
}
