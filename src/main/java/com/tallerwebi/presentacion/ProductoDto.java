package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.Componente;

public class ProductoDto {

    private Long id;
    private String nombre;
    private Double precio;
    private Integer cantidad;
    private String tipoComponente;
    private String imagen;

    public ProductoDto(Componente componente) {
        this.id = componente.getId();
        this.nombre = componente.getNombre();
        this.precio = componente.getPrecio();
        this.tipoComponente = componente.getClass().getSimpleName();
        this.imagen =  (componente.getImagenes() != null &&
                !componente.getImagenes().isEmpty() &&
                componente.getImagenes().get(0) != null)
                ? componente.getImagenes().get(0).getUrlImagen()
                : "imagen-generica.jpg";
    }

    public ProductoDto(Long id, String nombre, Double precio, String tipoComponente, String imagen) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = 2;
        this.tipoComponente = tipoComponente;
        this.imagen = imagen;
    }

    public Long getId() {
        return id;
    }
    public String getImagen() {
        return imagen;
    }
    public String getTipoComponente() {
        return tipoComponente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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
