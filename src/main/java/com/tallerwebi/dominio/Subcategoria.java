package com.tallerwebi.dominio;

import javax.persistence.*;

@Entity
@Table(name = "subcategorias")
public class Subcategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String nombreEnUrl;

    @ManyToOne
    @JoinColumn(name="categoria_id", nullable=false)
    private Categoria categoria;

    public Long getId() {return id; }
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

    public Categoria getCategoria() {
        return categoria;
    }
    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}

