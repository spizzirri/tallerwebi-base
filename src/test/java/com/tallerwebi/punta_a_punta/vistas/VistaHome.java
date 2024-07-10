package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaHome extends VistaWeb{

    public VistaHome(Page page) {
        super(page);
        page.navigate("localhost:8081/spring/home");
    }

    public String obtenerTextoDeLaBarraDeNavegacion() {
        return this.obtenerTextoDelElemento("h1.logo-text");
    }

    public String obtenerTextoDelTitulo(){
        return this.obtenerTextoDelElemento("h1.main-title");
    }

    public String obtenerTextoDelBoton() {
        return this.obtenerTextoDelElemento("#btn-empezar");
    }

    public void darClickEnEmpezar() {
        this.darClickEnElElemento("#btn-empezar");
    }

    public void darClickEnComenzarEntrenamiento() {
        this.darClickEnElElemento("a.main-btn");
    }
}
