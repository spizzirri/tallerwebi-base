package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaCoolers extends VistaWeb {
    public VistaCoolers(Page page) {
        super(page);
        page.navigate("http://localhost:8080/productos/search?cat=CoolerCPU");
    }

    public void darClickEnAgregarPrimerProductoAlCarrito() {
        page.locator("[data-testid='agregar-carrito-btn']").first().click();
    }

    public void darClickCoolers() {
        this.darClickEnElElemento("#btn-link-Coolers");
    }

    public String obtenerContadorDelCarrito() {
        return page.locator("#contadorCarrito").innerText();
    }

        public String obtenerMensajeDeError(){
        return this.obtenerTextoDelElemento("#mensajeNotificacion-70");
    }
}
