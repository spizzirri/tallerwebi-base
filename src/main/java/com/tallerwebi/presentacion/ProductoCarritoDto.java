package com.tallerwebi.presentacion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tallerwebi.dominio.entidades.Componente;

public class ProductoCarritoDto {

    private static Long contador = 1L;
    private final Long id;
    private String nombre;
    private Double precio;
    private Integer stock;
    private Integer cantidad = 1;
    private String marca;
    private String imagen;


    public ProductoCarritoDto(Componente componente, Integer cantidad) {
        this.id = componente.getId();
        this.nombre = componente.getNombre();
        this.precio = componente.getPrecio() != null ? componente.getPrecio() : 0.0;
        this.stock = componente.getStock();
        this.cantidad = cantidad != null ? cantidad : 1;;
        this.marca = componente.getMarca();
        this.imagen =  (componente.getImagenes() != null &&
                !componente.getImagenes().isEmpty() &&
                componente.getImagenes().get(0) != null)
                ? componente.getImagenes().get(0).getUrlImagen()
                : "imagen-generica.jpg";
    }

    public ProductoCarritoDto(Long id, String nombre, Double precio, Integer cantidad, String marca) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.stock = 10;
        this.marca = marca;
        this.imagen = "imagen-generica.jpg";
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public Double getPrecio() {
        return precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}
