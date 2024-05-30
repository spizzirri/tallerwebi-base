package com.tallerwebi.dominio.reto;

import com.tallerwebi.dominio.calendario.ItemRendimiento;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "reto")
public class Reto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre" ,nullable = false)
    private String nombre;

    @Column(name = "descripcion" ,nullable = false)
    private String descripcion;

    @Column(name = "imagen_url", nullable = false) // URL de la imagen
    private String imagenUrl;

    @Column(name = "seleccionado", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean seleccionado;

    public Reto() {
    }

    // Constructor con parámetros
    public Reto(String nombre, String descripcion, String imagenUrl) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
        this.seleccionado = false;
    }

    // Getters y setters
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public boolean isSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
    }

    @Override
    public String toString() {
        return "Reto{" +
                "nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
