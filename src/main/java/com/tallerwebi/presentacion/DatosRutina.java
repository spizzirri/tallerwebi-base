package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.rutina.Ejercicio;
import com.tallerwebi.dominio.rutina.Rutina;

import java.util.ArrayList;
import java.util.List;

public class DatosRutina {

    private String nombre;

    private List<Ejercicio> ejercicios;

    private Objetivo objetivo;

    private String descripcion;

    public DatosRutina(){

    }

    public DatosRutina(String nombre, Objetivo objetivo){
        this.nombre = nombre;
        this.objetivo = objetivo;
        this.descripcion = "Rutina para lograr " + this.objetivo;
        this.ejercicios = new ArrayList<>();
    }

    public DatosRutina(String nombre,List<Ejercicio> ejercicios, Objetivo objetivo){
        this.nombre = nombre;
        this.ejercicios = ejercicios;
        this.objetivo = objetivo;
        this.descripcion = "Rutina para lograr " + this.objetivo;
    }

    public DatosRutina(Rutina rutina){
        this.nombre = rutina.getNombre();
        this.ejercicios = rutina.getEjercicios();
        this.objetivo = rutina.getObjetivo();
        this.descripcion = "Rutina para lograr " + this.objetivo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Ejercicio> getEjercicios() {
        return ejercicios;
    }

    public void setEjercicios(List<Ejercicio> ejercicios) {
        this.ejercicios = ejercicios;
    }

    public Objetivo getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Objetivo objetivo) {
        this.objetivo = objetivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
