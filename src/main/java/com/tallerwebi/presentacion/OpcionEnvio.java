package com.tallerwebi.presentacion;

public class OpcionEnvio {

    private String id;
    private String codigoPostal;
    private String localidad;
    private String provincia;
    private Double costoEnvio;
    private String tiempoEstimado;
    private String empresa;

    public OpcionEnvio() {}

    public OpcionEnvio(String id, String codigoPostal, String localidad, String provincia, Double costoEnvio, String tiempoEstimado, String empresa) {
        this.id = id;
        this.codigoPostal = codigoPostal;
        this.localidad = localidad;
        this.provincia = provincia;
        this.costoEnvio = costoEnvio;
        this.tiempoEstimado = tiempoEstimado;
        this.empresa = empresa;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public Double getCostoEnvio() {
        return costoEnvio;
    }

    public void setCostoEnvio(Double costoEnvio) {
        this.costoEnvio = costoEnvio;
    }

    public String getTiempoEstimado() {
        return tiempoEstimado;
    }

    public void setTiempoEstimado(String tiempoEstimado) {
        this.tiempoEstimado = tiempoEstimado;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String toString() {
        return String.format("OpcionEnvio{empresa='%s', costo=%.2f, tiempo='%s'}",
                empresa, costoEnvio, tiempoEstimado);
    }
}
