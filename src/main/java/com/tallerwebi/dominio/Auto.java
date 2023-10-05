package com.tallerwebi.dominio;

import javax.persistence.*;

@Entity
public class Auto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Marca marca;
    @Column(unique = true)
    private String patente;
    @ManyToOne(cascade = CascadeType.ALL)
    private Usuario usuario;

    public Auto() {}

    public Auto(Marca marca, String patente, Usuario usuario) {
        this.marca = marca;
        this.patente = patente;
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
