package com.tallerwebi.presentacion.dto;

import com.tallerwebi.dominio.entidades.*;

import java.util.Objects;

public class ComponenteDto {


    private Long id;
    private String modelo;
    private Double precio;
    private String tipoComponente;
    private String imagen;
    private Integer stock;
    private String precioFormateado;

    public ComponenteDto() {}

    public ComponenteDto(Componente entidad) {

        this.id = entidad.getId();
        this.modelo = entidad.getNombre();
        this.precio = entidad.getPrecio();
        this.tipoComponente = entidad.getClass().getSimpleName();
        this.imagen = (entidad.getImagenes() != null && entidad.getImagenes().get(0) != null )
                    ? entidad.getImagenes().get(0).getUrlImagen()
                    : "imagen-generica.jpg";
        this.stock = entidad.getStock();
        this.precioFormateado = "0.0";

    }

    public ComponenteDto(Long id, String tipoComponente, String modelo, Double precio, String imagen, Integer stock, String precioFormateado) {
        this.id = id;
        this.tipoComponente = tipoComponente;
        this.modelo = modelo;
        this.precio = precio;
        this.imagen = imagen;
        this.stock = stock;
        this.precioFormateado = precioFormateado;
    }

    public Componente obtenerEntidad() {

        Componente entidad;

        switch (this.tipoComponente) {
            case "Procesador":
                entidad = new Procesador();
                break;
            case "Motherboard":
                entidad = new Motherboard();
                break;
            case "CoolerCPU":
                entidad = new CoolerCPU();
                break;
            case "PlacaDeVideo":
                entidad = new PlacaDeVideo();
                break;
            case "FuenteDeAlimentacion":
                entidad = new FuenteDeAlimentacion();
                break;
            case "Gabinete":
                entidad = new Gabinete();
                break;
            case "MemoriaRAM":
                entidad = new MemoriaRAM();
                break;
            case "Almacenamiento":
                entidad = new Almacenamiento();
                break;
            case "Monitor":
                entidad = new Monitor();
                break;
            case "Periferico":
                entidad = new Periferico();
                break;
            default:
                return null;
        }

        entidad.setId(this.id);
        entidad.setNombre(this.modelo);
        entidad.setPrecio(this.precio);
        entidad.setStock(this.stock);
        // que hago con imagenes y tipoComponente?
        return entidad;
    }

    public Long getId() {
        return this.id;
    }

    public String getModelo() {
        return this.modelo;
    }

    public Double getPrecio() {
        return this.precio;
    }

    public String getTipoComponente() {
        return this.tipoComponente;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTipoComponente(String tipo) {
        this.tipoComponente = tipo;
    }


    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getImagen() {
        return this.imagen;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStock() {
        return this.stock;
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
        ComponenteDto that = (ComponenteDto) o;
        return Objects.equals(id, that.id) && Objects.equals(tipoComponente, that.tipoComponente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tipoComponente);
    }
}
