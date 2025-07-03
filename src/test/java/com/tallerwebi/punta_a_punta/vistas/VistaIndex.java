package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaIndex  extends VistaWeb{

    public VistaIndex(Page page) {
        super(page);

    }

    public void ir() {
        page.navigate("http://localhost:8080/index");
    }
    public void darClickEnBotonProductos(){
        this.darClickEnElElemento("#nav-productos");
    }
    public void darClickEnBotonIngresa(){
        this.darClickEnElElemento("#btn-ingresa");
    }
}
