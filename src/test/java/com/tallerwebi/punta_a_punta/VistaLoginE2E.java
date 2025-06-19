package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaLogin;
import com.tallerwebi.punta_a_punta.vistas.VistaNuevoUsuario;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

public class VistaLoginE2E {

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    VistaLogin vistaLogin;

    @BeforeAll
    static void abrirNavegador() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        //browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(50));
    }

    @AfterAll
    static void cerrarNavegador() {
        playwright.close();
    }

    @BeforeEach
    void crearContextoYPagina() {
        ReiniciarDB.limpiarBaseDeDatos();

        context = browser.newContext();
        Page page = context.newPage();
        vistaLogin = new VistaLogin(page);
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

    @Test
    void deberiaDecirUNLAMEnElNavbar() {
        String texto = vistaLogin.obtenerTextoDeLaBarraDeNavegacion();
        assertThat("UNLAM", equalToIgnoringCase(texto));
    }

    @Test
    void deberiaDarUnErrorAlNoCompletarElLoginYTocarElBoton() {
        vistaLogin.escribirEMAIL("damian@unlam.edu.ar");
        vistaLogin.escribirClave("unlam");
        vistaLogin.darClickEnIniciarSesion();
        String texto = vistaLogin.obtenerMensajeDeError();
        assertThat("Error Usuario o clave incorrecta", equalToIgnoringCase(texto));
    }

    @Test
    void deberiaNavegarAlHomeSiElUsuarioExiste() throws MalformedURLException {
        vistaLogin.escribirEMAIL("test@unlam.edu.ar");
        vistaLogin.escribirClave("test");
        vistaLogin.darClickEnIniciarSesion();
        URL url = vistaLogin.obtenerURLActual();
        assertThat(url.getPath(), matchesPattern("^/spring/home(?:;jsessionid=[^/\\s]+)?$"));
    }

    @Test
    void deberiaRegistrarUnUsuarioEIniciarSesionExistosamente() throws MalformedURLException {
        vistaLogin.darClickEnRegistrarse();
        VistaNuevoUsuario vistaNuevoUsuario = new VistaNuevoUsuario(context.pages().get(0));
        vistaNuevoUsuario.escribirEMAIL("juan@unlam.edu.ar");
        vistaNuevoUsuario.escribirClave("123456");
        vistaNuevoUsuario.darClickEnRegistrarme();
        URL urlLogin = vistaLogin.obtenerURLActual();
        assertThat(urlLogin.getPath(), matchesPattern("^/spring/login(?:;jsessionid=[^/\\s]+)?$"));
        vistaLogin.escribirEMAIL("juan@unlam.edu.ar");
        vistaLogin.escribirClave("123456");
        vistaLogin.darClickEnIniciarSesion();
        URL urlHome = vistaLogin.obtenerURLActual();
        assertThat(urlHome.getPath(), matchesPattern("^/spring/home(?:;jsessionid=[^/\\s]+)?$"));
    }
}
