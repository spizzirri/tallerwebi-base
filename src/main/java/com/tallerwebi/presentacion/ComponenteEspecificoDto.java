package com.tallerwebi.presentacion;

import java.util.List;
import java.util.Map;

public class ComponenteEspecificoDto {

    private Long id;
    private String nombre;
    private Double precio;
    private List<String> imagenes;

    private Map<String, String> caracteristicasGenerales;
    private Map<String, String> especificacionesCPU;
    private Map<String, String> coolersYDisipadores;
    private Map<String, String> memoria;

    public ComponenteEspecificoDto() {}
    public ComponenteEspecificoDto(Long id, String nombre, Double precio,
                                   List<String> imagenes,
                                   Map<String, String> caracteristicasGenerales,
                                   Map<String, String> especificacionesCPU,
                                   Map<String, String> coolersYDisipadores,
                                   Map<String, String> memoria) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.imagenes = imagenes;
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

    public List<String> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<String> imagenes) {
        this.imagenes = imagenes;
    }

    public Map<String, String> getCaracteristicasGenerales() {
        return caracteristicasGenerales;
    }

    public void setCaracteristicasGenerales(Map<String, String> caracteristicasGenerales) {
        this.caracteristicasGenerales = caracteristicasGenerales;
    }

    public Map<String, String> getEspecificacionesCPU() {
        return especificacionesCPU;
    }

    public void setEspecificacionesCPU(Map<String, String> especificacionesCPU) {
        this.especificacionesCPU = especificacionesCPU;
    }

    public Map<String, String> getCoolersYDisipadores() {
        return coolersYDisipadores;
    }

    public void setCoolersYDisipadores(Map<String, String> coolersYDisipadores) {
        this.coolersYDisipadores = coolersYDisipadores;
    }

    public Map<String, String> getMemoria() {
        return memoria;
    }

    public void setMemoria(Map<String, String> memoria) {
        this.memoria = memoria;
    }

}
