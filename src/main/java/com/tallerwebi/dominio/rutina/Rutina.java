package com.tallerwebi.dominio.rutina;

import com.tallerwebi.dominio.Ejercicio;
import com.tallerwebi.dominio.Objetivo;

import java.util.ArrayList;
import java.util.List;


public class Rutina {

    private Long idRutina;

    private String nombre;

    private Objetivo objetivo;

    private List<Ejercicio> ejercicios = new ArrayList<>();

    public Rutina(String nombre, Objetivo objetivo) {
        this.nombre = nombre;
        this.objetivo = objetivo;
        this.ejercicios = new ArrayList<>();
    }

    public Rutina() {

    }

    public Long getIdRutina() {
        return idRutina;
    }

    public void setIdRutina(Long idRutina) {
        this.idRutina = idRutina;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Objetivo getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Objetivo objetivo) {
        this.objetivo = objetivo;
    }

    public List<Ejercicio> getEjercicios() {
        return ejercicios;
    }

    public void setEjercicios(List<Ejercicio> ejercicios) {
        this.ejercicios = ejercicios;
    }


    @Override
    public String toString() {
        return "Rutina{" +
                "nombre='" + nombre + '\'' +
                ", objetivo=" + objetivo +
                ", ejercicios=" + ejercicios +
                '}';
    }


}