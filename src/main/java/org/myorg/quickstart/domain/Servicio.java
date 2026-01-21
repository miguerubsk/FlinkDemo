package org.myorg.quickstart.domain;

public class Servicio {
    private String proveedor;
    private String tipo;
    private double coste;

    public Servicio() {
    }

    public Servicio(String proveedor, String tipo, double coste) {
        this.proveedor = proveedor;
        this.tipo = tipo;
        this.coste = coste;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getCoste() {
        return coste;
    }

    public void setCoste(double coste) {
        this.coste = coste;
    }
}
