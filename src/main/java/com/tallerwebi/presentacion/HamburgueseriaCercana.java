package com.tallerwebi.presentacion;

public class HamburgueseriaCercana {
    private Long id;
    private String nombre;
    private Double latitud;
    private Double longitud;
    private Double puntuacion;
    private String direccion;
    private boolean esComercioAdherido;

    public HamburgueseriaCercana() {
    }

    public HamburgueseriaCercana(Long id, String nombre, Double latitud, Double longitud, Double puntuacion,
            String direccion, boolean esComercioAdherido) {
        this.id = id;
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
        this.puntuacion = puntuacion;
        this.direccion = direccion;
        this.esComercioAdherido = esComercioAdherido;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Double getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(Double puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public boolean getEsComercioAdherido() {
        return esComercioAdherido;
    }

    public void setEsComercioAdherido(boolean esComercioAdherido) {
        this.esComercioAdherido = esComercioAdherido;
    }
}