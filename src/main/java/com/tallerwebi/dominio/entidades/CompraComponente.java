package com.tallerwebi.dominio.entidades;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class CompraComponente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con Compra
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCompra", nullable = false)
    private Compra compra;

    // Relación con Componente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idComponente", nullable = false)
    private Componente componente;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Double precioUnitario;

    @Column(length = 250)
    private String urlImagen;

    @Column(nullable = false)
    private Boolean esArmado = false;

    @Column
    private Integer numeroDeArmado;

    public CompraComponente() {}

    public CompraComponente(Compra compra, Componente componente, Integer cantidad, Double precioUnitario, Boolean esArmado, Integer numeroDeArmado, String urlImagen) {
        this.compra = compra;
        this.componente = componente;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.esArmado = esArmado;
        this.numeroDeArmado = numeroDeArmado;
        this.urlImagen = urlImagen;
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

    public Compra getCompra() {
        return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
    }

    public Componente getComponente() {
        return componente;
    }

    public void setComponente(Componente componente) {
        this.componente = componente;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Boolean getEsArmado() {
        return esArmado;
    }

    public void setEsArmado(Boolean esArmado) {
        this.esArmado = esArmado;
    }

    public Integer getNumeroDeArmado() {
        return numeroDeArmado;
    }

    public void setNumeroDeArmado(Integer numeroDeArmado) {
        this.numeroDeArmado = numeroDeArmado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompraComponente)) return false;
        CompraComponente that = (CompraComponente) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}