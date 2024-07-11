package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaRutina extends VistaWeb {

    public VistaRutina(Page page) {
        super(page);
        page.navigate("localhost:8081/spring/rutina");
    }

}

