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
        //browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500));
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
    void deberiaDecirUNLAMEnElNavbar() throws MalformedURLException {
        dadoQueElUsuarioEstaEnLaVistaDeLogin();
        entoncesDeberiaVerUNLAMEnElNavbar();
    }

    @Test
    void deberiaDarUnErrorAlIntentarIniciarSesionConUnUsuarioQueNoExiste() {
        dadoQueElUsuarioCargaSusDatosDeLoginCon("damian@unlam.edu.ar", "unlam");
        cuandoElUsuarioTocaElBotonDeLogin();
        entoncesDeberiaVerUnMensajeDeError();
    }

    @Test
    void deberiaNavegarAlHomeSiElUsuarioExiste() throws MalformedURLException {
        dadoQueElUsuarioCargaSusDatosDeLoginCon("test@unlam.edu.ar", "test");
        cuandoElUsuarioTocaElBotonDeLogin();
        entoncesDeberiaSerRedirigidoALaVistaDeHome();
    }

    @Test
    void deberiaRegistrarUnUsuarioEIniciarSesionExistosamente() throws MalformedURLException {
        dadoQueElUsuarioNavegaALaVistaDeRegistro();
        dadoQueElUsuarioSeRegistraCon("juan@unlam.edu.ar", "123456");
        dadoQueElUsuarioEstaEnLaVistaDeLogin();
        dadoQueElUsuarioCargaSusDatosDeLoginCon("juan@unlam.edu.ar", "123456");
        cuandoElUsuarioTocaElBotonDeLogin();    
        entoncesDeberiaSerRedirigidoALaVistaDeHome();
    }

    private void entoncesDeberiaVerUNLAMEnElNavbar() {
        String texto = vistaLogin.obtenerTextoDeLaBarraDeNavegacion();
        assertThat("UNLAM", equalToIgnoringCase(texto));
    }

    private void dadoQueElUsuarioEstaEnLaVistaDeLogin() throws MalformedURLException {
        URL urlLogin = vistaLogin.obtenerURLActual();
        assertThat(urlLogin.getPath(), matchesPattern("^/spring/login(?:;jsessionid=[^/\\s]+)?$"));
    }

    private void cuandoElUsuarioTocaElBotonDeLogin() {
        vistaLogin.darClickEnIniciarSesion();
    }

    private void entoncesDeberiaSerRedirigidoALaVistaDeHome() throws MalformedURLException {
        URL url = vistaLogin.obtenerURLActual();
        assertThat(url.getPath(), matchesPattern("^/spring/home(?:;jsessionid=[^/\\s]+)?$"));
    }

    private void entoncesDeberiaVerUnMensajeDeError() {
        String texto = vistaLogin.obtenerMensajeDeError();
        assertThat("Error Usuario o clave incorrecta", equalToIgnoringCase(texto));
    }

    private void dadoQueElUsuarioCargaSusDatosDeLoginCon(String email, String clave) {
        vistaLogin.escribirEMAIL(email);
        vistaLogin.escribirClave(clave);
    }

    private void dadoQueElUsuarioNavegaALaVistaDeRegistro() {
        vistaLogin.darClickEnRegistrarse();
    }

    private void dadoQueElUsuarioSeRegistraCon(String email, String clave) {
        VistaNuevoUsuario vistaNuevoUsuario = new VistaNuevoUsuario(context.pages().get(0));
        vistaNuevoUsuario.escribirEMAIL(email);
        vistaNuevoUsuario.escribirClave(clave);
        vistaNuevoUsuario.darClickEnRegistrarme();
    }
}
