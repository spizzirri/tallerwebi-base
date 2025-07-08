package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaLogin;
import com.tallerwebi.punta_a_punta.vistas.VistaRegistrarme;
import org.junit.jupiter.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;

public class VistaRegistrarmeE2E {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    VistaRegistrarme vistaRegistrarme;

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
        vistaRegistrarme = new VistaRegistrarme(page);
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }
    @Test
    void deberiaLogearmeAlInscribirBienElMailYPasswordDeUnUsuarioReal() {
        vistaRegistrarme.escribirEMAIL("Huesos@gmail.com");
        vistaRegistrarme.escribirClave("123");
        vistaRegistrarme.darClickEnIniciarSesion();
        String url = vistaRegistrarme.obtenerURLActual();
        assertThat(url, containsStringIgnoringCase("/index"));

    }
}
