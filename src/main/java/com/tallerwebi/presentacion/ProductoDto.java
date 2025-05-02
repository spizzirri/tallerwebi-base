package com.tallerwebi.presentacion;

public class ProductoDto {

    private static Long contador = 1L;
    private final Long id;
    private String nombre;
    private String descripcion;
    private Double precio;

    public ProductoDto(String nombre, Double precio) {
        this.id = contador++;
        this.nombre = nombre;
        this.precio = precio;
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
}
