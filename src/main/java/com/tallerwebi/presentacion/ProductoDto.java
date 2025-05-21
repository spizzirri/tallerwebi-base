package com.tallerwebi.presentacion;

public class ProductoDto {

    private static Long contador = 1L;
    private final Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer cantidad;
    private Integer categoria;
    private String imagen;

    public ProductoDto(String nombre, Double precio, Integer categoria, String imagen) {
        this.id = contador++;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = 2;
        this.categoria = categoria;
        this.imagen = imagen;
    }

    public Long getId() {
        return id;
    }
    public String getImagen() {
        return imagen;
    }
    public Integer getCategoria() {
        return categoria;
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
