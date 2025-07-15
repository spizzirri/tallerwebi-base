package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.tallerwebi.punta_a_punta.vistas.*;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

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
    VistaCarrito vistaCarrito;
    VistaTarjetaDeCredito vistaTarjetaDeCredito;

    @BeforeAll
    static void abrirNavegador() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(800));
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
        vistaCarrito = new VistaCarrito(page);
        vistaTarjetaDeCredito = new VistaTarjetaDeCredito(page);
        vistaIndex.ir();
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

    @Test
    public void deberiaPoderRealizarLaCompraDeUnProductoDeManeraExitosa() {
        cuandoLeDoyClickAIngresaMeLlevaALaPaginaLogin();
        deberiaNavegarAlNuevoUsuarioAlTocarElBotonRegistrarme();
        deberiaIniciarSesionCorrectamenteDespuesDeRegistrarUnUsuarioYcompletarElFormulario();
        cuandoLeDoyClickAProductosMeLlevaALaPaginaProductos();
        irAVistaCoolers();
        sumarProductoCoolerAlTocarElBoton();
        sumarProductoCoolerAlTocarElBoton();
        deberiaLlevarmeALaVistaDelResumenDelCarrito();
        deberiaNavegarALaVistaTarjetaDeCreditoSiElMetodoDePagoEsValido();
        deberiaCargarLosDatosCorrectamenteYNavegarAlMensajeDeExito();
    }

    public void cuandoLeDoyClickAIngresaMeLlevaALaPaginaLogin() {
        vistaIndex.darClickEnBotonIngresa();
    }

    public void deberiaNavegarAlNuevoUsuarioAlTocarElBotonRegistrarme() {
        vistaLogin.darClickEnRegistarte();
    }

    public void cuandoLeDoyClickAProductosMeLlevaALaPaginaProductos() {
        vistaIndex.darClickEnBotonProductos();
    }

    void sumarProductoCoolerAlTocarElBoton() {
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
    }

    void irAVistaCoolers() {
        vistaCoolers.ir();
    }

    public void deberiaIniciarSesionCorrectamenteDespuesDeRegistrarUnUsuarioYcompletarElFormulario() {
        loginValidoDeUsuarioGustavoNarancio();
        vistaRegistrarme.escribirEMAIL("Huesos123456@gmail.com");
        vistaRegistrarme.escribirClave("123");
        vistaRegistrarme.darClickEnIniciarSesion();
    }

    public void loginValidoDeUsuarioGustavoNarancio() {
        vistaNuevoUsuario.escribirNOMBRE("Gustavo");
        vistaNuevoUsuario.escribirAPELLIDO("Narancio");
        vistaNuevoUsuario.escribirEMAIL("Huesos123456@gmail.com");
        vistaNuevoUsuario.escribirClave("123");
        vistaNuevoUsuario.escribirTELEFONO("11425123112");
        vistaNuevoUsuario.escribirDNI("39.022.123");
        vistaNuevoUsuario.darClickEnRegistrarme();
    }

    public void deberiaLlevarmeALaVistaDelResumenDelCarrito() {
        vistaProductos.abrirResumenCarrito();
        vistaProductos.obtenerResumenCarrito();
        vistaProductos.darClickEnIrAlPago();
    }

    public void deberiaNavegarALaVistaTarjetaDeCreditoSiElMetodoDePagoEsValido() {
        vistaCarrito.escribirCodigoDeDescuento("compra5");
        vistaCarrito.darClickEnCalcularDescuento();
        vistaCarrito.escribirCodigoPostal("1704");
        vistaCarrito.darClickEnCalcularEnvio();
        vistaCarrito.darClickEnTarjetaDeCredito();
        vistaCarrito.darClickEnFinalizarCompra();

    }

    public void deberiaCargarLosDatosCorrectamenteYNavegarAlMensajeDeExito() {
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
    }

}
