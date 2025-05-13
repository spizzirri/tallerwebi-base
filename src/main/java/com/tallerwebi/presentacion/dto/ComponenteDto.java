package com.tallerwebi.presentacion.dto;

import java.util.Objects;

public class ComponenteDto {

    private Long id;
    private String modelo;
    private Double precio;
    private String tipoComponente;
    private String imagen;

    public ComponenteDto() {}
    public ComponenteDto(Long id, String tipoComponente, String modelo, Double precio) {
        this.id = id;
        this.tipoComponente = tipoComponente;
        this.modelo = modelo;
        this.precio = precio;
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
