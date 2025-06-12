package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.Componente;

public class ProductoDto {

    private Long id;
    private String nombre;
    private Double precio;
    private Integer stock;
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
        this.stock = componente.getStock();
    }

    public ProductoDto(Long id, String nombre,Integer stock, Double precio, String tipoComponente, String imagen) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
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

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
