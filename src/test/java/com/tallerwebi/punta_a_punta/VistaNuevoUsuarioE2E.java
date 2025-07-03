package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaNuevoUsuario;
import com.tallerwebi.punta_a_punta.vistas.VistaRegistrarme;
import org.junit.jupiter.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class VistaNuevoUsuarioE2E {

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    VistaNuevoUsuario vistaNuevoUsuario;
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
        vistaNuevoUsuario = new VistaNuevoUsuario(page);
        vistaRegistrarme = new VistaRegistrarme(page);
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

    void loginValidoDeUsuarioGustavoNarancio() {
        vistaNuevoUsuario.escribirNOMBRE("Gustavo");
        vistaNuevoUsuario.escribirAPELLIDO("Narancio");
        vistaNuevoUsuario.escribirEMAIL("Huesos@gmail.com");
        vistaNuevoUsuario.escribirClave("123");
        vistaNuevoUsuario.escribirTELEFONO("11425123112");
        vistaNuevoUsuario.escribirDNI("39.022.123");
        vistaNuevoUsuario.darClickEnRegistrarme();
    }

    @Test
    void deberiaRegistrarUnUsuarioAlPonerTodosLosDatosBienEnLosCampos() {
        loginValidoDeUsuarioGustavoNarancio();
        String url = vistaNuevoUsuario.obtenerURLActual();
        assertThat(url, containsStringIgnoringCase("/registrarme"));
    }

    @Test
    void deberiaRegistrarUnUsuarioAlPonerTodosLosDatosBienEnLosCamposYLuegoAlRedirigirmePoderLogearmeCorrectamente() {
        loginValidoDeUsuarioGustavoNarancio();
        vistaRegistrarme.escribirEMAIL("Huesos@gmail.com");
        vistaRegistrarme.escribirClave("123");
        vistaRegistrarme.darClickEnIniciarSesion();
        String url = vistaNuevoUsuario.obtenerURLActual();
        assertThat(url, containsStringIgnoringCase("/index"));
    }

    @Test
    void deberiaDarmeUnMensajeDeErrorAlCorrerEsteTestPorSegundaVezRegistrarUnUsuarioAlPonerTodosLosDatosBienEnLosCampos() {
        loginValidoDeUsuarioGustavoNarancio();
        String textoObtenido = vistaNuevoUsuario.obtenerMensajeDeError();
        String textoEsperado = "Error: El usuario ya existe";
        assertEquals(textoEsperado, textoObtenido);
    }

    @Test
    void deberiaDarmeUnMensajeDeUsuarioRepetidoAlRegistrarUnUsuarioDosVecesBien() {
        loginValidoDeUsuarioGustavoNarancio();
        vistaRegistrarme.darClickEnRegistarteVistaRegistrarme();
        loginValidoDeUsuarioGustavoNarancio();
        String textoObtenido = vistaNuevoUsuario.obtenerMensajeDeError();
        String textoEsperado = "Error: El usuario ya existe";
        assertEquals(textoEsperado, textoObtenido);
    }


}