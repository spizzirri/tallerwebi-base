package com.tallerwebi.punta_a_punta;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.tallerwebi.punta_a_punta.vistas.VistaCarrito;
import com.tallerwebi.punta_a_punta.vistas.VistaHijos;
import com.tallerwebi.punta_a_punta.vistas.VistaHome;
import com.tallerwebi.punta_a_punta.vistas.VistaLogin;
import com.tallerwebi.punta_a_punta.vistas.VistaNuevoUsuario;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.*;

public class FlujoHastaMercadoPagoE2E {

  static Playwright playwright;
  static Browser browser;
  BrowserContext context;
  VistaLogin vistaLogin;
  VistaHome vistaHome;
  VistaCarrito vistaCarrito;

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
  void deberiaRedirigirAMercadoPagoAlPagar() {
    dadoQueElUsuarioIniciaSesion();
    dadoQueElUsuarioAprietaAgregarAlCarrito();
    cuandoElUsuarioHaceClickEnVerCarrito();
    dadoQueElUsuarioConfirmaElPedido();
  }

  private void dadoQueElUsuarioConfirmaElPedido() {
    vistaCarrito = new VistaCarrito(context.pages().get(0));
    vistaCarrito.confirmarPedido();
  }

  private void cuandoElUsuarioHaceClickEnVerCarrito() {
    vistaHome.irAlCarrito();
  }

  private void dadoQueElUsuarioIniciaSesion() {
    vistaLogin.escribirEMAIL("test@unlam.edu.ar");

    vistaLogin.escribirClave("test");

    vistaLogin.darClickEnIniciarSesion();
  }

  private void dadoQueElUsuarioAprietaAgregarAlCarrito() {
    vistaHome = new VistaHome(context.pages().get(0));
    vistaHome.agregarProductoAlCarrito();
  }
}
