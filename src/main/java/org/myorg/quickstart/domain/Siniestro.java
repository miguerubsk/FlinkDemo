package org.myorg.quickstart.domain;

public class Siniestro {
    private int id;
    private String descripcion;
    private String estado;
    private String ultimaActualizacion;
    private int clienteId;
    private int expedienteId;

    public Siniestro() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(String ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public int getExpedienteId() {
        return expedienteId;
    }

    public void setExpedienteId(int expedienteId) {
        this.expedienteId = expedienteId;
    }
}
