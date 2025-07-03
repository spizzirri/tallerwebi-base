package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaIndex;
import com.tallerwebi.punta_a_punta.vistas.VistaLogin;
import org.junit.jupiter.api.*;

public class VistaIndexE2E {

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    VistaIndex vistaIndex;

    @BeforeAll
    static void abrirNavegador() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(50));

    }

    @AfterAll
    static void cerrarNavegador() {
        playwright.close();
    }

    @BeforeEach
    void crearContextoYPagina() {
        context = browser.newContext();
        Page page = context.newPage();
        vistaIndex = new VistaIndex(page);
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

    @Test
    public void CuandoLeDoyClickAUnMismoItemCincoVecesSeSUmaCincoVeces(){
        vistaIndex.darClickEnBotonCarrito();
        vistaIndex.darClickEnBotonCarrito();
        vistaIndex.darClickEnBotonCarrito();
        vistaIndex.darClickEnBotonCarrito();
        vistaIndex.darClickEnBotonCarrito();
    }
}
