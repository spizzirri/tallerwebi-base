package com.tallerwebi.dominio;

import javax.persistence.*;

@Entity
@Table(name = "ejercicios")
public class Ejercicio {

    @Id
    private Long idEjercicio;

    @Column(name = "nombre", length = 50)
    private String nombre;
    @Column(name = "descripcion")
    private String descripcion;
    @Enumerated(EnumType.STRING)
    @Column(name = "grupo_muscular")
    private GrupoMuscularObjetivo grupoMuscularObjetivo;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ejercicio")
    private TipoEjercicio tipoEjercicio;

    @Column(name = "series", nullable = false)
    private Integer series;

    @Column(name = "repeticiones", nullable = false)
    private Integer repeticiones;


    public Ejercicio(String nombre, String descripcion, GrupoMuscularObjetivo grupoMuscularObjetivo, TipoEjercicio tipoEjercicio, Integer series, Integer repeticiones) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.grupoMuscularObjetivo = grupoMuscularObjetivo;
        this.tipoEjercicio = tipoEjercicio;
        this.series = series;
        this.repeticiones = repeticiones;
    }

    public Ejercicio() {

    }

    // Getters and setters
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

    public GrupoMuscularObjetivo getGrupoMuscularObjetivo() {
        return grupoMuscularObjetivo;
    }

    public void setGrupoMuscularObjetivo(GrupoMuscularObjetivo grupoMuscularObjetivo) {
        this.grupoMuscularObjetivo = grupoMuscularObjetivo;
    }

    public TipoEjercicio getTipoEjercicio() {
        return tipoEjercicio;
    }

    public void setTipoEjercicio(TipoEjercicio tipoEjercicio) {
        this.tipoEjercicio = tipoEjercicio;
    }


    public Integer getSeries() {
        return series;
    }

    public void setSeries(Integer series) {
        this.series = series;
    }

    public Integer getRepeticiones() {
        return repeticiones;
    }

    public void setRepeticiones(Integer repeticiones) {
        this.repeticiones = repeticiones;
    }

    // Método toString para imprimir el objeto Ejercicio
    @Override
    public String toString() {
        return "Ejercicio{" +
                "nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", grupoMuscularObjetivo='" + grupoMuscularObjetivo + '\'' +
                ", tipoEjercicio='" + tipoEjercicio + '\'' +
                ", series=" + series +
                ", repeticiones=" + repeticiones +
                '}';
    }

    public void setIdEjercicio(Long idEjercicio) {
        this.idEjercicio = idEjercicio;
    }

    public Long getIdEjercicio() {
        return idEjercicio;
    }
}
