package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class VistaProductos extends VistaWeb {
    public VistaProductos(Page page) {
        super(page);
        page.navigate("http://localhost:8080/productos");
    }

    public void darClickEnAgregarPrimerProductoAlCarrito() {
        page.locator("[data-testid='agregar-carrito-btn']").first().click();
    }

    public String obtenerContadorDelCarrito() {
        return page.locator("#contadorCarrito").innerText();
    }

    public void abrirResumenCarrito() {
        page.locator("#abrirResumenCarrito").click();
    }

    public Locator obtenerResumenCarrito() {
        return page.locator("#resumenCarrito");
    }

    public void darClickEnIrAlPago() {
        this.darClickEnElElemento("#resumen-carrito");
    }

}

