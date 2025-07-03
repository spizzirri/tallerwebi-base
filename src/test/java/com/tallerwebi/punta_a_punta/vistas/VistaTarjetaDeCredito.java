package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaTarjetaDeCredito extends VistaWeb{

    public VistaTarjetaDeCredito(Page page) {
        super(page);
    }

    public void ir() {
        page.navigate("http://localhost:8080/tarjetaDeCredito");
    }

    public void darClickEnNumeroDeTarjeta() {
        this.darClickEnElElemento("#numeroTarjeta");
    }

    public void escribirNumeroDeTarjeta(String numeroTarjeta) {
        this.escribirEnElElemento("#numeroTarjeta", numeroTarjeta);
    }

    public void darClickEnNombreDelTitular() {
        this.darClickEnElElemento("#nombreTitular");
    }

    public void escribirNombreDelTitular(String nombreTitular) {
        this.escribirEnElElemento("#nombreTitular", nombreTitular);
    }

    public void darClickEnVencimiento() {
        this.darClickEnElElemento("#vencimiento");
    }

    public void escribirVencimiento(String vencimiento) {
        this.escribirEnElElemento("#vencimiento", vencimiento);
    }

    public void darClickEnCodigoDeSeguridad() {
        this.darClickEnElElemento("#codigoSeguridad");
    }

    public void escribirCodigoDeSeguridad(String codigoSeguridad) {
        this.escribirEnElElemento("#codigoSeguridad", codigoSeguridad);
    }

    public void darClickEnDocumento() {
        this.darClickEnElElemento("#documento");
    }

    public void escribirDocumento(String documento) {
        this.escribirEnElElemento("#documento", documento);
    }

    public void darClickEnContinuar() {
        this.darClickEnElElemento("#continuar");
    }

}
