package com.tallerwebi.presentacion;

public class ProductoCarritoDto {

    private static Long contador = 1L;
    private final Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer cantidad;

    public ProductoCarritoDto(String nombre, Double precio) {
        this.id = contador++;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = 2;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPrecio() {
        return precio;
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
}
