package org.myorg.quickstart.domain;

public class Cliente {
    private int id;
    private String nombre;
    private String dniCif;
    private String tipoCliente;

    public Cliente() {
    }

    public Cliente(int id, String nombre, String dniCif, String tipoCliente) {
        this.id = id;
        this.nombre = nombre;
        this.dniCif = dniCif;
        this.tipoCliente = tipoCliente;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDniCif() {
        return dniCif;
    }

    public void setDniCif(String dniCif) {
        this.dniCif = dniCif;
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
    }
}
