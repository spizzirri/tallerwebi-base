package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaPerfil extends VistaWeb {

    public VistaPerfil(Page page) {
        super(page);
        page.navigate("localhost:8081/spring/perfil");
    }

    public String obtenerTextoDeLaBarraDeNavegacion() {
        return this.obtenerTextoDelElemento("h1.logo-text");
    }

    public String obtenerTextoDeInicioEnElNav() {
        return this.obtenerTextoDelElemento("#inicio-link");
    }

    public void darClickEnHome() {
        this.darClickEnElElemento("#inicio-link");
    }

    public String obtenerTextoDelForm() {
        return this.obtenerTextoDelElemento("#caracteristicas");
    }

    public String obtenerTextoDelBoton() {
        return this.obtenerTextoDelElemento("button.bg-red-500");
    }

    public void darClickEnCerrarSesion() {
        this.darClickEnElElemento("button.bg-red-500");
    }

}
