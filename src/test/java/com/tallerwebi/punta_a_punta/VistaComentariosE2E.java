package com.tallerwebi.punta_a_punta;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaComentarios;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VistaComentariosE2E {

  static Playwright playwright;
  static Browser browser;
  BrowserContext context;
  VistaComentarios vistaComentarios;

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
    this.context = browser.newContext();
    Page page = this.context.newPage();
    this.vistaComentarios = new VistaComentarios(page);
  }

  @AfterEach
  void cerrarContexto() {
    this.context.close();
  }

  @Test
  void deberiaPublicarUnComentarioYVerElMensajeDeExito() {
    // preparacion
    this.dadoQueElUsuarioEscribeUnComentarioValido();

    // ejecucion
    this.cuandoPublicaElComentario();

    // validacion
    this.entoncesVeElMensajeDeExito();
  }

  @Test
  void deberiaVerUnErrorAlPublicarUnComentarioVacio() {
    // ejecucion
    this.cuandoPublicaElComentario();

    // validacion
    this.entoncesVeElMensajeDeComentarioVacio();
  }

  @Test
  void deberiaAceptar2000PuntosDeCodigoEnElFormulario() {
    // preparacion
    this.vistaComentarios.escribirComentario("\uD83D\uDE00".repeat(2000));

    // ejecucion
    this.cuandoPublicaElComentario();

    // validacion
    this.entoncesVeElMensajeDeExito();
  }

  @Test
  void deberiaPublicarUnComentarioYVerloPrimeroEnElListado() {
    // preparacion
    this.dadoQueExisteUnComentarioPublicado("Comentario inicial");
    this.dadoQueElUsuarioEscribeUnComentario("Comentario mas reciente");

    // ejecucion
    this.cuandoPublicaElComentario();

    // validacion
    this.entoncesElPrimerComentarioEnElListadoEs("Comentario mas reciente");
  }

  private void dadoQueExisteUnComentarioPublicado(String contenido) {
    this.vistaComentarios.escribirComentario(contenido);
    this.vistaComentarios.publicarComentario();
  }

  private void dadoQueElUsuarioEscribeUnComentario(String contenido) {
    this.vistaComentarios.escribirComentario(contenido);
  }

  private void entoncesElPrimerComentarioEnElListadoEs(String contenidoEsperado) {
    assertThat(
      this.vistaComentarios.obtenerPrimerComentarioDelListado(),
      equalToIgnoringCase(contenidoEsperado)
    );
  }

  private void dadoQueElUsuarioEscribeUnComentarioValido() {
    this.vistaComentarios.escribirComentario("Comentario publicado desde el navegador");
  }

  private void cuandoPublicaElComentario() {
    this.vistaComentarios.publicarComentario();
  }

  private void entoncesVeElMensajeDeExito() {
    assertThat(
      this.vistaComentarios.obtenerMensajeDeExito(),
      equalToIgnoringCase("Comentario publicado")
    );
  }

  private void entoncesVeElMensajeDeComentarioVacio() {
    assertThat(
      this.vistaComentarios.obtenerMensajeDeError(),
      equalToIgnoringCase("El comentario no puede estar vacío")
    );
  }
}
