package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaProductos extends VistaWeb {
        public VistaProductos(Page page) {
            super(page);
            page.navigate("http://localhost:8080/productos");
        }
        public void darClickEnAgregarPrimerProductoAlCarrito(){
        this.page.locator(".card-producto-agregar-carrito").nth(0).click();
    }
    }

