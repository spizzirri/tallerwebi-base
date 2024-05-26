package com.tallerwebi.dominio.reto;

import com.tallerwebi.dominio.calendario.ItemRendimiento;

import javax.persistence.*;
import java.util.List;

@Entity
public class Reto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "nombre" ,nullable = false)
    private String nombre;

    @Column(name = "descripcion" ,nullable = false)
    private String descripcion;

    public Reto() {
    }

    // Constructor con parámetros
    public Reto(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}
