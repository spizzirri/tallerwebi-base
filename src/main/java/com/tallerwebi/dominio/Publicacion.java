package com.tallerwebi.dominio;


import javax.persistence.*;

@Entity
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Publicacion() {}

    public Publicacion(String descripcion) {
        this.descripcion = descripcion;
    }

    // getters y setters


    public Usuario getUsuario() {
        return usuario;
    }

    public String getDescripcion() {
        return descripcion;
    }
}