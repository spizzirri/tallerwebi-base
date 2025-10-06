package com.tallerwebi.dominio;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String nombreEnUrl;
    @OneToMany(cascade = CascadeType.ALL, mappedBy="categoria", fetch = FetchType.LAZY)
    private List<Subcategoria> subcategorias = new ArrayList<>();

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

    public String getNombreEnUrl() {
        return nombreEnUrl;
    }
    public void setNombreEnUrl(String nombreEnUrl) {
        this.nombreEnUrl = nombreEnUrl;
    }

    public List<Subcategoria> getSubcategorias() {
        return subcategorias;
    }
    public void setSubcategorias(List<Subcategoria> subcategorias) {
        this.subcategorias = subcategorias;
    }
}
