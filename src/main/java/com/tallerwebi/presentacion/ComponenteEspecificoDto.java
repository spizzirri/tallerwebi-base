package com.tallerwebi.presentacion;

import java.util.ArrayList;
import java.util.List;

public class ComponenteEspecificoDto {

    private Long id;
    private String nombre;
    private Double precio;
    private String imagen;

    private List<String> caracteristicasGenerales;
    private List<String> especificacionesCPU;
    private List<String> coolersYDisipadores;
    private List<String> memoria;

    public ComponenteEspecificoDto() {}
    public ComponenteEspecificoDto(Long id, String nombre, Double precio, String imagen,
                                   List<String> caracteristicasGenerales,
                                   List<String> especificacionesCPU,
                                   List<String> coolersYDisipadores,
                                   List<String> memoria) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.imagen = imagen;
        this.caracteristicasGenerales = caracteristicasGenerales;
        this.especificacionesCPU = especificacionesCPU;
        this.coolersYDisipadores = coolersYDisipadores;
        this.memoria = memoria;
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

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public List<String> getCaracteristicasGenerales() {
        return caracteristicasGenerales;
    }

    public void setCaracteristicasGenerales(List<String> caracteristicasGenerales) {
        this.caracteristicasGenerales = caracteristicasGenerales;
    }

    public List<String> getEspecificacionesCPU() {
        return especificacionesCPU;
    }

    public void setEspecificacionesCPU(List<String> especificacionesCPU) {
        this.especificacionesCPU = especificacionesCPU;
    }

    public List<String> getCoolersYDisipadores() {
        return coolersYDisipadores;
    }

    public void setCoolersYDisipadores(List<String> coolersYDisipadores) {
        this.coolersYDisipadores = coolersYDisipadores;
    }

    public List<String> getMemoria() {
        return memoria;
    }

    public void setMemoria(List<String> memoria) {
        this.memoria = memoria;
    }

}
