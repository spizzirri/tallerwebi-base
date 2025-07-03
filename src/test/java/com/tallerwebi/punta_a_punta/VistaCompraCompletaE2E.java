package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

public class VistaCompraCompletaE2E {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;

    @BeforeAll
    static void abrirNavegador() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500));

    }

    @AfterAll
    static void cerrarNavegador() {
        playwright.close();
    }

    @BeforeEach
    void crearContextoYPagina() {
        context = browser.newContext();
        Page page = context.newPage();
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

}
