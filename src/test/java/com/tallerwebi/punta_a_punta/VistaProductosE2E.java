package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaIndex;
import com.tallerwebi.punta_a_punta.vistas.VistaLogin;
import com.tallerwebi.punta_a_punta.vistas.VistaProductos;
import org.junit.jupiter.api.*;

public class VistaProductosE2E {

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    VistaProductos vistaProductos;

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
        vistaProductos = new VistaProductos(page);
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

    @Test
    public void CuandoLeDoyClickAUnMismoItemCincoVecesSeSUmaCincoVeces(){
        vistaProductos.darClickEnAgregarPrimerProductoAlCarrito();
        vistaProductos.darClickEnAgregarPrimerProductoAlCarrito();
        vistaProductos.darClickEnAgregarPrimerProductoAlCarrito();
        vistaProductos.darClickEnAgregarPrimerProductoAlCarrito();
        vistaProductos.darClickEnAgregarPrimerProductoAlCarrito();
    }
}
