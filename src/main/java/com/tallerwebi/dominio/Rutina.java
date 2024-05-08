package com.tallerwebi.dominio;

import org.hibernate.annotations.Fetch;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "rutinas")
public class Rutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rutina")
    private Long idRutina;

    @Column(name = "nombre")
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "objetivo")
    private Objetivo objetivo;

    @OneToMany(mappedBy = "rutina", cascade = CascadeType.ALL, orphanRemoval = true)
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

    public void agregarEjercicio(Ejercicio ejercicio) {
    }

    // Método toString para imprimir la rutina de ejercicios
    @Override
    public String toString() {
        return "Rutina{" +
                "nombre='" + nombre + '\'' +
                ", objetivo=" + objetivo +
                ", ejercicios=" + ejercicios +
                '}';
    }


}
