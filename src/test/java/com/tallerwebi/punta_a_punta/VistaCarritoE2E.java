package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaCarrito;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.*;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;

public class VistaCarritoE2E {

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    VistaCarrito vistaCarrito;

    @BeforeAll
    static void abrirNavegador() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(100));
    }

    @AfterAll
    static void cerrarNavegador() {
        playwright.close();
    }

    @BeforeEach
    void crearContextoYPagina() {
        context = browser.newContext();
        Page page = context.newPage();
        vistaCarrito = new VistaCarrito(page);
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

    @Test
    public void deberiaDevolvermeUnMensajeConElCodigoDeDescuento() {
        vistaCarrito.ir();
        vistaCarrito.escribirCodigoDeDescuento("compra5");
        vistaCarrito.darClickEnCalcularDescuento();

        Locator descuento = vistaCarrito.page.locator("#mensajeDescuento");

        assertThat(descuento).hasText("Descuento aplicado! Nuevo total: $0,00");
    }

    @Test
    public void deberiaDevolvermeUnMensajeConCostoTiempoYZonaDeEnvio() {
        vistaCarrito.ir();
        vistaCarrito.escribirCodigoPostal("1704");
        vistaCarrito.darClickEnCalcularEnvio();

        Locator costo = vistaCarrito.page.locator("#costo");
        Locator tiempo = vistaCarrito.page.locator("#tiempo");
        Locator zona = vistaCarrito.page.locator("#zona");

        assertThat(costo).hasText("$ 1.500,00");
        assertThat(tiempo).hasText("1-2 días");
        assertThat(zona).hasText("Ramos Mejía, Buenos Aires");
    }

    @Test
    public void deberiaNavegarALaVistaTarjetaDeCreditoSiElMetodoDePagoEsValido() {
        vistaCarrito.ir();
        deberiaDevolvermeUnMensajeConElCodigoDeDescuento();
        deberiaDevolvermeUnMensajeConCostoTiempoYZonaDeEnvio();
        vistaCarrito.darClickEnTarjetaDeCredito();
        vistaCarrito.darClickEnFinalizarCompra();
        String url = vistaCarrito.obtenerURLActual();

        MatcherAssert.assertThat(url, containsStringIgnoringCase("/tarjetaDeCredito"));
    }

}
