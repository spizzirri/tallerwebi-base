package com.tallerwebi.presentacion;

public class EnvioDto {
    // formato interno para pasarle al controllador con datos que va a recibir de la API a traves del servicio/OpcionEnvio
    private Double costo;
    private String tiempo;
    private String destino;

    public EnvioDto(){}

    public EnvioDto(Double costo, String tiempo, String destino) {
        this.costo = costo;
        this.tiempo = tiempo;
        this.destino = destino;
    }

    public Double getCosto() {
        return costo;
    }

    public void setCosto(Double costo) {
        this.costo = costo;
    }

    public String getTiempo() {
        return tiempo;
    }

    public void setTiempo(String tiempo) {
        this.tiempo = tiempo;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    @Override
    public String toString() {
        return String.format("EnvioDto{costo=%.2f, tiempo='$s', destino='$s'}", costo, tiempo, destino);
    }
}
