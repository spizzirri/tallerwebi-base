package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.CompraComponente;
import com.tallerwebi.presentacion.dto.ComponenteDto;

public class CompraComponenteDto {

    private Long id;
    private Long compra;
    private ComponenteDto componente;
    private Integer cantidad;
    private Double precioUnitario;
    private Boolean esArmado;
    private Integer numeroDeArmado;
    private String urlImagen;
    private Double precioDolar;


    public CompraComponenteDto(CompraComponente compraComponente) {
        this.id = compraComponente.getId();
        this.compra = compraComponente.getCompra().getIdCompra();
        this.componente = new ComponenteDto(compraComponente.getComponente());
        this.cantidad = compraComponente.getCantidad();
        this.precioUnitario = compraComponente.getPrecioUnitario();
        this.esArmado = compraComponente.getEsArmado();
        this.numeroDeArmado = compraComponente.getNumeroDeArmado();
        this.urlImagen = compraComponente.getUrlImagen();
        this.precioDolar = compraComponente.getPrecioDolar();
    }

    public CompraComponenteDto() {}

    public Long getId() {
        return id;
    }

    public Long getCompra() {
        return compra;
    }
    public String getUrlImagen() {return urlImagen;}
    public void setUrlImagen(String urlImagen) {this.urlImagen = urlImagen;}

    public ComponenteDto getComponente() {
        return componente;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCompra(Long compra) {
        this.compra = compra;
    }

    public void setComponente(ComponenteDto componente) {
        this.componente = componente;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Boolean getEsArmado() {
        return esArmado;
    }

    public void setEsArmado(Boolean esArmado) {
        this.esArmado = esArmado;
    }

    public Integer getNumeroDeArmado() {
        return numeroDeArmado;
    }

    public void setNumeroDeArmado(Integer numeroDeArmado) {
        this.numeroDeArmado = numeroDeArmado;
    }

    public void setPrecioDolar(Double precioDolar) {
        this.precioDolar = precioDolar;
    }

    public Double getPrecioDolar() {
        return precioDolar;
    }


}
