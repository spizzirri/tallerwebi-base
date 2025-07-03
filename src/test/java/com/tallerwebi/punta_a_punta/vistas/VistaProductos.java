package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class VistaProductos extends VistaWeb {
    public VistaProductos(Page page) {
        super(page);

    }

    public void ir() {
        page.navigate("http://localhost:8080/productos");
    }

//    public void darClickEnAgregarPrimerProductoAlCarrito() {
//        Locator contador = page.locator("#contadorCarrito");
//        String valorInicial = contador.innerText();
//
//        // Hacemos click en el botón para agregar el producto
//        page.locator("[data-testid='agregar-carrito-btn']").first().click();
//
//        // Esperamos a que el contador cambie su valor (espera activa)
//        page.waitForFunction(
//                "([contador, valorInicial]) => contador.innerText !== valorInicial",
//                new Object[]{contador, valorInicial}
//        );
//    }

    public void darClickEnBotonIngresa(){
        this.darClickEnElElemento("#btn-ingresa");
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

