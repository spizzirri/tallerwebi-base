package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaCoolers;
import com.tallerwebi.punta_a_punta.vistas.VistaProductos;
import com.tallerwebi.punta_a_punta.vistas.VistaWeb;
import org.junit.jupiter.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

public class VistaProductosE2E {


    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    VistaProductos vistaProductos;
    VistaCoolers vistaCoolers;

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
        vistaProductos = new VistaProductos(page);
        vistaCoolers = new VistaCoolers(page);
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

    @Test
    public void CuandoLeDoyClickAUnMismoItemOnceVecesSeSumaDiezVecesYObtengoUnMensajeDeError(){
        vistaCoolers.darClickCoolers();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        String mensajeError = vistaCoolers.obtenerMensajeDeError();

        assertThat(mensajeError, equalTo("Stock insuficiente"));
    }

    @Test
    public void deberiaAgregarUnProductoAlCarrito() {
        vistaCoolers.darClickCoolers();

        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();

        String contador = vistaCoolers.obtenerContadorDelCarrito();

        assertThat(Integer.parseInt(contador), greaterThanOrEqualTo(1));
    }
}
