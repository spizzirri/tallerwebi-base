package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;


public class VistaCarrito extends VistaWeb {

    public VistaCarrito(Page page) {
        super(page);
        page.navigate("http://localhost:8080/carritoDeCompras/index");
    }

    public void escribirCodigoDeDescuento(String codigoInput) {
        this.escribirEnElElemento("#codigoInput", codigoInput);
    }

    public void darClickEnCalcularDescuento() {
        this.darClickEnElElemento("#btnAplicarDescuento");
    }

    public void escribirCodigoPostal(String codigoPostal) {
        this.escribirEnElElemento("#codigoPostal", codigoPostal);
    }

    public void darClickEnCalcularEnvio() {
        this.darClickEnElElemento("#btnCalcular");
    }

    public void darClickEnTarjetaDeCredito() {
        this.darClickEnElElemento("#tarjetaCredito");
    }

    public void darClickEnFinalizarCompra() {
        this.darClickEnElElemento("#btnComprar");
    }

}
