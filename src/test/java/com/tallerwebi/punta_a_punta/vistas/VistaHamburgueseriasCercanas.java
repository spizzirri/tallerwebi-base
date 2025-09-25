package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class VistaHamburgueseriasCercanas extends VistaWeb {
    public VistaHamburgueseriasCercanas(Page page) {
        super(page);
        page.navigate("localhost:8080/spring/hamburgueserias-cercanas");
    }

    public String obtenerTextoDeLaBarraDeNavegacion() {
        return this.obtenerTextoDelElemento("nav a.navbar-brand");
    }

    public Locator obtenerHamburgueseriasCercanas() {
        return this.obtenerElemento("#hamburgueserias-list");
    }

    public Locator obtenerListaHamburgueserias() {
        return this.obtenerElemento("#hamburgueserias-list li:not(.active)");
    }

    public void esperarCargaCompleta() {
        // Esperar que aparezca contenido dinámico
        this.obtenerElemento("#hamburgueserias-list li:not(.active)")
                .first()
                .waitFor(new Locator.WaitForOptions().setTimeout(15000));
    }
}
