package com.tallerwebi.dominio.reto;

import com.tallerwebi.dominio.calendario.ItemRendimiento;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "seleccionado", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean seleccionado;

    @Column(name = "en_proceso", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean enProceso = false;

    public Reto() {
    }

    // Constructor con parámetros
    public Reto(String nombre, String descripcion, String imagenUrl, LocalDate fechaInicio) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
        this.fechaInicio = fechaInicio;
        this.seleccionado = false;
        this.enProceso = false;
    }

    public Reto(String nombre, String descripción) {
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

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public boolean getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Boolean seleccionado) {
        this.seleccionado = seleccionado;
    }

    public Boolean getEnProceso() {
        return enProceso;
    }

    public void setEnProceso(Boolean enProceso) {
        this.enProceso = enProceso;
    }

    @Override
    public String toString() {
        return "Reto{" +
                "nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
