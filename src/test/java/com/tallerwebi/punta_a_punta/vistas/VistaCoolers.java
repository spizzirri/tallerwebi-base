package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaCoolers extends VistaWeb {
    public VistaCoolers(Page page) {
        super(page);
    }

    public void darClickEnAgregarPrimerProductoAlCarrito() {
        page.locator("[data-testid='agregar-carrito-btn']").first().click();
    }

    public void darClickCoolers() {
        page.waitForSelector("#btn-link-Coolers").click();
    }

    public void darClickEnCarritoNavbar() {
        this.darClickEnElElemento("#btn-link-Coolers");
    }

    public String obtenerContadorDelCarrito() {
        return page.locator("#contadorCarrito").innerText();
    }

        public String obtenerMensajeDeError(){
        return this.obtenerTextoDelElemento("#mensajeNotificacion-70");
    }
}
