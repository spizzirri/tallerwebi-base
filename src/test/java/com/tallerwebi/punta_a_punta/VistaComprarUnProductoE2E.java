package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.*;
import org.junit.jupiter.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;

public class VistaComprarUnProductoE2E {

        static Playwright playwright;
        static Browser browser;
        BrowserContext context;
        VistaNuevoUsuario vistaNuevoUsuario;
        VistaRegistrarme vistaRegistrarme;
        VistaIndex vistaIndex;
        VistaLogin vistaLogin;
        VistaProductos vistaProductos;
        VistaCoolers vistaCoolers;

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
            vistaIndex = new VistaIndex(page);
            vistaLogin = new VistaLogin(page);
            vistaProductos = new VistaProductos(page);
            vistaCoolers = new VistaCoolers(page);

            vistaIndex.ir();
        }

        @AfterEach
        void cerrarContexto() {
            context.close();
        }

        @Test
        public void pruebaFinal(){
        cuandoLeDoyClickAIngresaMeLlevaALaPaginaLogin();
        deberiaNavegarAlNuevoUsuarioAlTocarElBotonRegistrarme();
        deberiaIniciarSesionCorrectamenteDespuesDeRegistrarUnUsuarioYcompletarElFormulario();
        cuandoLeDoyClickAProductosMeLlevaALaPaginaProductos();
        irAVistaCoolers();
        sumarProductoCoolerAlTocarElBoton();
        sumarProductoCoolerAlTocarElBoton();

        }

        public void cuandoLeDoyClickAIngresaMeLlevaALaPaginaLogin(){
        vistaIndex.darClickEnBotonIngresa();
        }
        public void deberiaNavegarAlNuevoUsuarioAlTocarElBotonRegistrarme() {
        vistaLogin.darClickEnRegistarte();
        }
        public void cuandoLeDoyClickAProductosMeLlevaALaPaginaProductos(){
        vistaIndex.darClickEnBotonProductos();
        }
        void sumarProductoCoolerAlTocarElBoton(){
            vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        }
        void irAVistaCoolers() {
            vistaCoolers.ir();
        }
//        public void deberiaRegistrarUnUsuarioAlPonerTodosLosDatosBienEnLosCampos() {
//        loginValidoDeUsuarioGustavoNarancio();
//         }
         public void deberiaIniciarSesionCorrectamenteDespuesDeRegistrarUnUsuarioYcompletarElFormulario(){
             loginValidoDeUsuarioGustavoNarancio();
             vistaRegistrarme.escribirEMAIL("Huesos123@gmail.com");
             vistaRegistrarme.escribirClave("123");
             vistaRegistrarme.darClickEnIniciarSesion();
         }
        public void loginValidoDeUsuarioGustavoNarancio(){
        vistaNuevoUsuario.escribirNOMBRE("Gustavo");
        vistaNuevoUsuario.escribirAPELLIDO("Narancio");
        vistaNuevoUsuario.escribirEMAIL("Huesos123@gmail.com");
        vistaNuevoUsuario.escribirClave("123");
        vistaNuevoUsuario.escribirTELEFONO("11425123112");
        vistaNuevoUsuario.escribirDNI("39.022.123");
        vistaNuevoUsuario.darClickEnRegistrarme();
    }

}
