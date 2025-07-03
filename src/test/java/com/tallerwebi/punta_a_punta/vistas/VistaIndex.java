package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaIndex  extends VistaWeb{

    public VistaIndex(Page page) {
        super(page);
        page.navigate("http://localhost:8080/index");
    }
    public void darClickEnBotonCarrito(){
        this.darClickEnElElemento(".card-producto-agregar-carrito");
    }
}
