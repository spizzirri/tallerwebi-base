package com.tallerwebi.dominio;

import net.bytebuddy.dynamic.loading.InjectionClassLoader;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String codigo;
    private String comision;

    @ManyToMany(mappedBy = "materias",   fetch = FetchType.LAZY )
    private List<Carrera> carreras = new ArrayList<>();

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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getComision() {
        return comision;
    }

    public void setComision(String comision) {
        this.comision = comision;
    }

    public List<Carrera> getCarreras() {
        return carreras;
    }

    public void setCarreras(List<Carrera> carreras) {
        this.carreras = carreras;
    }
}



