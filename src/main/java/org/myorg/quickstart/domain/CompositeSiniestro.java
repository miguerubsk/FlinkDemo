package org.myorg.quickstart.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CompositeSiniestro {
    @JsonProperty("siniestro_id")
    private int siniestroId;

    @JsonProperty("ultima_actualizacion")
    private String ultimaActualizacion;

    private String descripcion;
    private String estado;

    private Cliente cliente;
    private List<Expediente> expedientes;
    private List<Servicio> servicios;

    public CompositeSiniestro() {
    }

    // Getters and Setters
    public int getSiniestroId() {
        return siniestroId;
    }

    public void setSiniestroId(int siniestroId) {
        this.siniestroId = siniestroId;
    }

    public String getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(String ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<Expediente> getExpedientes() {
        return expedientes;
    }

    public void setExpedientes(List<Expediente> expedientes) {
        this.expedientes = expedientes;
    }

    public List<Servicio> getServicios() {
        return servicios;
    }

    public void setServicios(List<Servicio> servicios) {
        this.servicios = servicios;
    }
}
