package com.tallerwebi.presentacion;

import java.util.List;

public class PagoRequest {
    private List<ProductoCarritoDto> productoDtoList;
    private String metodoDePago;
    private Double costoEnvio = 0.0;

    // Constructor vacío necesario para el binding
    public PagoRequest() {}

    public List<ProductoCarritoDto> getProductos() {
        return productoDtoList;
    }

    public void setProductos(List<ProductoCarritoDto> productos) {
        this.productoDtoList = productos;
    }

    public String getMetodoDePago() {
        return metodoDePago;
    }

    public void setMetodoDePago(String metodoDePago) {
        this.metodoDePago = metodoDePago;
    }

    public Double getCostoEnvio() {
        return costoEnvio;
    }

    public void setCostoEnvio(Double costoEnvio) {
        this.costoEnvio = costoEnvio;
    }
}
