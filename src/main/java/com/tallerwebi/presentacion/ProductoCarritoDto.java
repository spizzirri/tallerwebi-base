package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.Componente;

import java.util.Objects;

public class ProductoCarritoDto {

    private Long id;
    private String nombre;
    private Double precio;
    private Integer stock;
    private String tipoComponente;
    private Integer cantidad = 1;
    private String imagen;
    private String precioFormateado;

    // private Boolean armado = false;

    public ProductoCarritoDto() {}

    public ProductoCarritoDto(Componente componente, Integer cantidad) {
        this.id = componente.getId();
        this.nombre = componente.getNombre();
        this.precio = componente.getPrecio();
        this.cantidad = cantidad != null ? cantidad : 1;;
        this.tipoComponente = componente.getClass().getSimpleName();
        this.imagen =  (componente.getImagenes() != null &&
                !componente.getImagenes().isEmpty() &&
                componente.getImagenes().get(0) != null)
                ? componente.getImagenes().get(0).getUrlImagen()
                : "imagen-generica.jpg";
        this.stock = componente.getStock();
        this.precioFormateado = componente.getPrecio().toString();
    }

    public ProductoCarritoDto(Long id, String nombre, Double precio, Integer cantidad) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.stock = 10;
        this.imagen = "imagen-generica.jpg";
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

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad != null ? cantidad : 1;
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

    public String getPrecioFormateado() {
        return precioFormateado;
    }

    public void setPrecioFormateado(String precioFormateado) {
        this.precioFormateado = precioFormateado;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProductoCarritoDto that = (ProductoCarritoDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
