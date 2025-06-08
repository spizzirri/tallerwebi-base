package com.tallerwebi.dominio.entidades;

import javax.persistence.*;

@Entity
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false, unique = true)
    private String urlImagen;

    @ManyToOne
    @JoinColumn(name = "componente_id", nullable = false)
    private Componente componente;

    public Imagen() {}

    public Imagen(Long id, String urlImagen, Componente componente) {
        this.id = id;
        this.urlImagen = urlImagen;
        this.componente = componente;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public Componente getComponente() {
        return componente;
    }

    public void setComponente(Componente componente) {
        this.componente = componente;
    }

}
