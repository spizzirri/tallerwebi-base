//package com.tallerwebi.punta_a_punta;
//
//import com.microsoft.playwright.*;
//import com.microsoft.playwright.assertions.PlaywrightAssertions;
//import com.tallerwebi.punta_a_punta.vistas.*;
//import org.hamcrest.MatcherAssert;
//import org.junit.jupiter.api.*;
//
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.containsStringIgnoringCase;
//import static org.hamcrest.Matchers.greaterThanOrEqualTo;
//
//public class VistaCompraCompletaE2E {
//    static Playwright playwright;
//    static Browser browser;
//    BrowserContext context;
//    VistaLogin vistaLogin;
//    VistaRegistrarme vistaRegistrarme;
//    VistaNuevoUsuario vistaNuevoUsuario;
//    VistaIndex vistaIndex;
//    VistaProductos vistaProductos;
//    VistaCoolers vistaCoolers;
//    VistaCarrito vistaCarrito;
//    VistaTarjetaDeCredito vistaTarjetaDeCredito;
//
//    @BeforeAll
//    static void abrirNavegador() {
//        playwright = Playwright.create();
//        browser = playwright.chromium().launch();
////        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(1000));
//
//    }
//
//    @AfterAll
//    static void cerrarNavegador() {
//        playwright.close();
//    }
//
//    @BeforeEach
//    void crearContextoYPagina() {
//        context = browser.newContext();
//        Page page = context.newPage();
//        vistaLogin = new VistaLogin(page);
//        vistaRegistrarme = new VistaRegistrarme(page);
//        vistaNuevoUsuario = new VistaNuevoUsuario(page);
//        vistaIndex = new VistaIndex(page);
//        vistaProductos = new VistaProductos(page);
//        vistaCoolers = new VistaCoolers(page);
//        vistaCarrito = new VistaCarrito(page);
//        vistaTarjetaDeCredito = new VistaTarjetaDeCredito(page);
//    }
//
//    @AfterEach
//    void cerrarContexto() {
//        context.close();
//    }
//
//    @Test
//    public void deberiaPoderRealizarLaCompraDeUnProductoDeManeraExitosa() {
//        deberiaAgregarUnProductoAlCarrito();
//        vistaProductos.abrirResumenCarrito();
//        vistaProductos.obtenerResumenCarrito();
//        vistaProductos.darClickEnIrAlPago();
//        deberiaNavegarALaVistaTarjetaDeCreditoSiElMetodoDePagoEsValido();
//        deberiaCargarLosDatosCorrectamenteYNavegarAlMensajeDeExito();
//
//    }
//
//    @Test
//    public void deberiaAgregarUnProductoAlCarrito() {
//        vistaCoolers.darClickCoolers();
//
//        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
//
//        String contador = vistaCoolers.obtenerContadorDelCarrito();
//
//        assertThat(Integer.parseInt(contador), greaterThanOrEqualTo(1));
//    }
//
//    @Test
//    void deberiaDevolvermeUnMensajeConElCodigoDeDescuento() {
//        vistaCarrito.escribirCodigoDeDescuento("compra5");
//        vistaCarrito.darClickEnCalcularDescuento();
//
//        Locator descuento = vistaCarrito.page.locator("#mensajeDescuento");
//
//        PlaywrightAssertions.assertThat(descuento).hasText("Descuento aplicado! Nuevo total: $0,00");
//    }
//
//    @Test
//    void deberiaDevolvermeUnMensajeConCostoTiempoYZonaDeEnvio() {
//        vistaCarrito.escribirCodigoPostal("1704");
//        vistaCarrito.darClickEnCalcularEnvio();
//
//        Locator costo = vistaCarrito.page.locator("#costo");
//        Locator tiempo = vistaCarrito.page.locator("#tiempo");
//        Locator zona = vistaCarrito.page.locator("#zona");
//
//        PlaywrightAssertions.assertThat(costo).hasText("$ 1.500,00");
//        PlaywrightAssertions.assertThat(tiempo).hasText("1-2 días");
//        PlaywrightAssertions.assertThat(zona).hasText("Ramos Mejía, Buenos Aires");
//    }
//
//    @Test
//    public void deberiaNavegarALaVistaTarjetaDeCreditoSiElMetodoDePagoEsValido() {
//        deberiaDevolvermeUnMensajeConElCodigoDeDescuento();
//        deberiaDevolvermeUnMensajeConCostoTiempoYZonaDeEnvio();
//        vistaCarrito.darClickEnTarjetaDeCredito();
//        vistaCarrito.darClickEnFinalizarCompra();
//        String url = vistaCarrito.obtenerURLActual();
//
//        MatcherAssert.assertThat(url, containsStringIgnoringCase("/tarjetaDeCredito"));
//    }
//
//    @Test
//    public void deberiaCargarLosDatosCorrectamenteYNavegarAlMensajeDeExito(){
//        vistaTarjetaDeCredito.darClickEnNumeroDeTarjeta();
//        vistaTarjetaDeCredito.escribirNumeroDeTarjeta("4567867567541234");
//        vistaTarjetaDeCredito.darClickEnNombreDelTitular();
//        vistaTarjetaDeCredito.escribirNombreDelTitular("Maria Lopez");
//        vistaTarjetaDeCredito.darClickEnVencimiento();
//        vistaTarjetaDeCredito.escribirVencimiento("12/25");
//        vistaTarjetaDeCredito.darClickEnCodigoDeSeguridad();
//        vistaTarjetaDeCredito.escribirCodigoDeSeguridad("345");
//        vistaTarjetaDeCredito.darClickEnDocumento();
//        vistaTarjetaDeCredito.escribirDocumento("37658569");
//        vistaTarjetaDeCredito.darClickEnContinuar();
//        String url = vistaTarjetaDeCredito.obtenerURLActual();
//
//        MatcherAssert.assertThat(url, containsStringIgnoringCase("/tarjetaDeCredito/validar"));
//    }
//
//}
