package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaHamburgueseriasCercanas;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

public class HamburgueseriasCercanasE2E {

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    VistaHamburgueseriasCercanas vistaHamburgueseriasCercanas;

    @BeforeAll
    static void abrirNavegador() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void cerrarNavegador() {
        playwright.close();
    }

    @BeforeEach
    void crearContextoYPagina() {
        ReiniciarDB.limpiarBaseDeDatos();

        context = browser.newContext(new Browser.NewContextOptions()
                .setPermissions(Arrays.asList("geolocation"))
                .setGeolocation(-34.6037, -58.3816)
                .setLocale("es-AR"));

        Page page = context.newPage();

        page.onConsoleMessage(msg -> {
            System.out.println("Console [" + msg.type() + "]: " + msg.text());
        });

        // Capturar errores de red
        page.onResponse(response -> {
            System.out.println("Response: " + response.url() + " - Status: " + response.status());
        });

        page.onRequestFailed(request -> {
            System.out.println("Request failed: " + request.url() + " - " + request.failure());
        });

        page.waitForTimeout(3000);

        vistaHamburgueseriasCercanas = new VistaHamburgueseriasCercanas(page);
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

    @Test
    void deberiaDecirUNLAMEnElNavbar() throws MalformedURLException {
        dadoQueElUsuarioEstaEnLaVistaDeHamburgueseriasCercanas();
        entoncesDeberiaVerUNLAMEnElNavbar();
    }

    private void entoncesDeberiaVerUNLAMEnElNavbar() {
        String texto = vistaHamburgueseriasCercanas.obtenerTextoDeLaBarraDeNavegacion();
        assertThat("UNLAM", equalToIgnoringCase(texto));
    }

    private void dadoQueElUsuarioEstaEnLaVistaDeHamburgueseriasCercanas() throws MalformedURLException {
        URL urlLogin = vistaHamburgueseriasCercanas.obtenerURLActual();
        assertThat(urlLogin.getPath(), matchesPattern("^/spring/hamburgueserias-cercanas"));
    }

    @Test
    void deberiaCargarYMostrarHamburgueseriasCercanas() throws MalformedURLException {
        dadoQueElUsuarioEstaEnLaVistaDeHamburgueseriasCercanas();
        cuandoLaPaginaObtieneUbicacionYCargaHamburgueserias();
        entoncesDeberiaVerLaListaDeHamburgueserias();
    }

    @Test
    void deberiaMostrarMensajeCuandoNoHayHamburgueseriasCercanas() throws MalformedURLException {
        dadoQueElUsuarioEstaEnLaVistaDeHamburgueseriasCercanas();
        assert true; // TODO: Simular que no hay hamburgueserías cercanas cuando integremos la BD.
    }

    private void cuandoLaPaginaObtieneUbicacionYCargaHamburgueserias() {
        vistaHamburgueseriasCercanas.obtenerElemento("li")
                .first()
                .waitFor(new Locator.WaitForOptions().setTimeout(10000));
    }

    private void entoncesDeberiaVerLaListaDeHamburgueserias() {
        var hamburgueserias = vistaHamburgueseriasCercanas
                .obtenerElemento("#hamburgueserias-list .list-group-item");
        int cantidadHamburgueserias = hamburgueserias.count();

        assertThat("Debería haber al menos una hamburguesería en la lista", cantidadHamburgueserias, greaterThan(0));

        if (cantidadHamburgueserias > 0) {
            String primerElemento = hamburgueserias.nth(1).textContent();
            assertThat(primerElemento, containsString("Es comercio adherido: Si"));
        }
    }
}
