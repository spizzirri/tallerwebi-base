package com.tallerwebi.presentacion.dto;

import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.presentacion.ProductoCarritoDto;

import java.util.Objects;

public class ProductoCarritoArmadoDto extends ProductoCarritoDto {

    private Boolean esEscencialParaElArmado;
    private Integer numeroDeArmadoAlQuePertenece;

    public ProductoCarritoArmadoDto() {}

    public ProductoCarritoArmadoDto(Long id, String nombre, Double precio, Integer cantidad) {
    super(id, nombre, precio, cantidad);
    }

    public ProductoCarritoArmadoDto(Componente componente, Integer cantidad) {
        super(componente, cantidad);
        this.esEscencialParaElArmado = false;
        this.numeroDeArmadoAlQuePertenece = 0;
    }

    public Boolean getEsEscencialParaElArmado() {
        return esEscencialParaElArmado;
    }

    public void setEsEscencialParaElArmado(Boolean esEscencialParaElArmado) {
        this.esEscencialParaElArmado = esEscencialParaElArmado;
    }

    public Integer getNumeroDeArmadoAlQuePertenece() {
        return numeroDeArmadoAlQuePertenece;
    }

    public void setNumeroDeArmadoAlQuePertenece(Integer numeroDeArmadoAlQuePertenece) {
        this.numeroDeArmadoAlQuePertenece = numeroDeArmadoAlQuePertenece;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProductoCarritoArmadoDto that = (ProductoCarritoArmadoDto) o;
        return Objects.equals(numeroDeArmadoAlQuePertenece, that.numeroDeArmadoAlQuePertenece);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), numeroDeArmadoAlQuePertenece);
    }
}
