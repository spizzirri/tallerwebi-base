package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaHome;
import com.tallerwebi.punta_a_punta.vistas.VistaLogin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

public class VistaHomeE2E {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    VistaHome vistaHome;
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
        vistaHome = new VistaHome(page);
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

    @Test
    void deberiaDecirMagnusFitEnElNav() {
        String texto = vistaHome.obtenerTextoDeLaBarraDeNavegacion();
        assertThat("MagnusFit", equalToIgnoringCase(texto));
    }

    @Test
    void deberiaDecirTuEntrenadorPersonalDeTitulo() {
        String texto = vistaHome.obtenerTextoDelTitulo();
        assertThat("Tu Entrenador Personal", equalToIgnoringCase(texto));
    }

    @Test
    void deberiaHaberUnBotonEmpezarReto() {
        String texto = vistaHome.obtenerTextoDelBoton();
        assertThat("Empezar", equalToIgnoringCase(texto));
    }

    @Test
    void deberiaNavegarARutinasAlApretarElBotonComenzarEntrenamiento(){
        vistaHome.darClickEnComenzarEntrenamiento();
        String url = vistaHome.obtenerURLActual();
        assertThat(url, containsStringIgnoringCase("/spring/rutinas?objetivo="));
    }

    @Test
    void deberiaEmpezarRetoAlApretarElBoton(){
        vistaHome.darClickEnEmpezar();
        String url = vistaHome.obtenerURLActual();
        assertThat(url, containsStringIgnoringCase("/spring/empezar-reto"));
    }
}
