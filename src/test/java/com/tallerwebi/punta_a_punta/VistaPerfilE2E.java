package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaPerfil;
import com.tallerwebi.punta_a_punta.vistas.VistaLogin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

public class VistaPerfilE2E {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    VistaPerfil vistaPerfil;
    VistaLogin vistaLogin;

    @BeforeAll
    static void abrirNavegador() {
        playwright = Playwright.create();
//        browser = playwright.chromium().launch();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(300));

    }

    @AfterAll
    static void cerrarNavegador() {
        playwright.close();
    }

    @BeforeEach
    void crearContextoYPagina() {
        context = browser.newContext();
        Page page = context.newPage();
        vistaLogin = new VistaLogin(page);
        vistaLogin.escribirEMAIL("test@unlam.edu.ar");
        vistaLogin.escribirClave("test");
        vistaLogin.darClickEnIniciarSesion();
        vistaPerfil = new VistaPerfil(page);
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

    @Test
    void deberiaDecirMagnusFitEnElNav() {
        String texto = vistaPerfil.obtenerTextoDeLaBarraDeNavegacion();
        assertThat("MagnusFit", equalToIgnoringCase(texto));
    }

    @Test
    void deberiaDecirInicioEnElNav() {
        String texto = vistaPerfil.obtenerTextoDeInicioEnElNav();
        assertThat("Inicio", equalToIgnoringCase(texto));
    }

    @Test
    void inicioDeberiaLlevarAlHome() {
        vistaPerfil.darClickEnHome();
        String url = vistaPerfil.obtenerURLActual();
        assertThat(url, containsStringIgnoringCase("/spring/home"));
    }


    @Test
    void deberiaDecirMisCaracteristicas() {
        String texto = vistaPerfil.obtenerTextoDelForm();
        assertThat("Mis Características", equalToIgnoringCase(texto));
    }

    @Test
    void deberiaHaberUnBotonCerrarSesion() {
        String texto = vistaPerfil.obtenerTextoDelBoton();
        assertThat("Cerrar Sesión", equalToIgnoringCase(texto));
    }

    @Test
    void cerrarSesionDeberiaLlevarAlLogin() {
        vistaPerfil.darClickEnCerrarSesion();
        String url = vistaPerfil.obtenerURLActual();
        assertThat(url, containsStringIgnoringCase("/spring/login"));
    }

}
