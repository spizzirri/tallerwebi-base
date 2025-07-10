package com.tallerwebi.presentacion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequisitosProgramas {

    private String id;
    private String nombre;
    private Map<String, Double> requisitosMinimos;
    private Map<String, Double> requisitosRecomendados;

    public RequisitosProgramas() {}

    public RequisitosProgramas(String id, String nombre, Map<String, Double> requisitosMinimos, Map<String, Double> requisitosRecomendados) {
        this.id = id;
        this.nombre = nombre;
        this.requisitosMinimos = requisitosMinimos;
        this.requisitosRecomendados = requisitosRecomendados;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Map<String, Double> getRequisitosMinimos() {
        return requisitosMinimos;
    }

    public void setRequisitosMinimos(Map<String, Double> requisitosMinimos) {
        this.requisitosMinimos = requisitosMinimos;
    }

    public Map<String, Double> getRequisitosRecomendados() {
        return requisitosRecomendados;
    }

    public void setRequisitosRecomendados(Map<String, Double> requisitosRecomendados) {
        this.requisitosRecomendados = requisitosRecomendados;
    }

    @Override
    public String toString() {
        return "RequisitosProgramas{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", requisitosMinimos=" + requisitosMinimos +
                ", requisitosRecomendados=" + requisitosRecomendados +
                '}';
    }
}
