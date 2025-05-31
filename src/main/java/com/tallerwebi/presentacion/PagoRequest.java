package com.tallerwebi.presentacion;

import java.util.List;

public class PagoRequest {
    private FormularioDePagoDTO formularioPagoDTO;
    private List<ProductoCarritoDto> productoDtoList;
    private String metodoDePago;

    // Constructor vacío necesario para el binding
    public PagoRequest() {
        this.formularioPagoDTO = new FormularioDePagoDTO();
    }


    public List<ProductoCarritoDto> getProductos() {
        return productoDtoList;
    }

    public void setProductos(List<ProductoCarritoDto> productos) {
        this.productoDtoList = productos;
    }

    public FormularioDePagoDTO getFormularioPagoDTO() {
        return formularioPagoDTO;
    }

    public String getMetodoDePago() {
        return metodoDePago;
    }

    public void setMetodoDePago(String metodoDePago) {
        this.metodoDePago = metodoDePago;
    }

    public void setFormularioPagoDTO(FormularioDePagoDTO formularioPagoDTO) {
        this.formularioPagoDTO = formularioPagoDTO;
    }
}
