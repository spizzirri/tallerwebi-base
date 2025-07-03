package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaTarjetaDeCredito;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.containsStringIgnoringCase;


public class VistaTarjetaDeCreditoE2E {

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    VistaTarjetaDeCredito vistaTarjetaDeCredito;

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
        vistaTarjetaDeCredito = new VistaTarjetaDeCredito(page);
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

    @Test
    public void deberiaCargarLosDatosCorrectamenteYNavegarAlMensajeDeExito(){
        vistaTarjetaDeCredito.darClickEnNumeroDeTarjeta();
        vistaTarjetaDeCredito.escribirNumeroDeTarjeta("4567867567541234");
        vistaTarjetaDeCredito.darClickEnNombreDelTitular();
        vistaTarjetaDeCredito.escribirNombreDelTitular("Maria Lopez");
        vistaTarjetaDeCredito.darClickEnVencimiento();
        vistaTarjetaDeCredito.escribirVencimiento("12/25");
        vistaTarjetaDeCredito.darClickEnCodigoDeSeguridad();
        vistaTarjetaDeCredito.escribirCodigoDeSeguridad("345");
        vistaTarjetaDeCredito.darClickEnDocumento();
        vistaTarjetaDeCredito.escribirDocumento("37658569");
        vistaTarjetaDeCredito.darClickEnContinuar();
        String url = vistaTarjetaDeCredito.obtenerURLActual();

        MatcherAssert.assertThat(url, containsStringIgnoringCase("/tarjetaDeCredito/validar"));
    }

}
