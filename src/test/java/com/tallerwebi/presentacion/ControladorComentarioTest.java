package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.ServicioComentario;
import com.tallerwebi.dominio.excepcion.ComentarioInvalido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorComentarioTest {

  private ControladorComentario controladorComentario;
  private ServicioComentario servicioComentarioMock;

  @BeforeEach
  public void init() {
    this.servicioComentarioMock = mock(ServicioComentario.class);
    this.controladorComentario = new ControladorComentario(this.servicioComentarioMock);
  }

  @Test
  public void irAComentariosDeberiaDevolverLaVistaComentarios() {
    // ejecucion
    ModelAndView modelAndView = this.controladorComentario.irAComentarios();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("comentarios"));
  }

  @Test
  public void publicarComentarioValidoDeberiaMostrarMensajeDeExito() {
    // preparacion
    String contenido = "Comentario valido";

    // ejecucion
    ModelAndView modelAndView = this.controladorComentario.publicarComentario(contenido);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("comentarios"));
    assertThat(modelAndView.getModel().get("mensaje"), equalTo("Comentario publicado"));
    verify(this.servicioComentarioMock).publicar(contenido);
  }

  @Test
  public void publicarComentarioInvalidoDeberiaMostrarElMensajeDeLaExcepcion() {
    // preparacion
    String contenido = "";
    doThrow(new ComentarioInvalido("El comentario no puede estar vacío"))
      .when(this.servicioComentarioMock)
      .publicar(contenido);

    // ejecucion
    ModelAndView modelAndView = this.controladorComentario.publicarComentario(contenido);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("comentarios"));
    assertThat(modelAndView.getModel().get("error"), equalTo("El comentario no puede estar vacío"));
  }
}
