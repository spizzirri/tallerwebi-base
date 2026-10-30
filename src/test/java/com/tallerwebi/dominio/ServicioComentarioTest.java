package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.ComentarioInvalido;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ServicioComentarioTest {

  private ServicioComentario servicioComentario;
  private RepositorioComentario repositorioComentarioMock;

  @BeforeEach
  public void init() {
    this.repositorioComentarioMock = mock(RepositorioComentario.class);
    this.servicioComentario = new ServicioComentarioImpl(this.repositorioComentarioMock);
  }

  @Test
  public void publicarComentarioValidoDeberiaGuardarloEnElRepositorio() {
    // preparacion
    String contenido = "Un comentario valido";

    // ejecucion
    this.servicioComentario.publicar(contenido);

    // validacion
    ArgumentCaptor<Comentario> comentarioCaptor = ArgumentCaptor.forClass(Comentario.class);
    verify(this.repositorioComentarioMock).guardar(comentarioCaptor.capture());
    assertThat(comentarioCaptor.getValue().getContenido(), equalTo(contenido));
  }

  @Test
  public void publicarComentarioDeUnCaracterDeberiaGuardarlo() {
    // preparacion
    String contenido = "a";

    // ejecucion
    this.servicioComentario.publicar(contenido);

    // validacion
    ArgumentCaptor<Comentario> comentarioCaptor = ArgumentCaptor.forClass(Comentario.class);
    verify(this.repositorioComentarioMock).guardar(comentarioCaptor.capture());
    assertThat(comentarioCaptor.getValue().getContenido(), equalTo(contenido));
  }

  @Test
  public void publicarComentarioDe2000CaracteresDeberiaGuardarlo() {
    // preparacion
    String contenido = "a".repeat(2000);

    // ejecucion
    this.servicioComentario.publicar(contenido);

    // validacion
    ArgumentCaptor<Comentario> comentarioCaptor = ArgumentCaptor.forClass(Comentario.class);
    verify(this.repositorioComentarioMock).guardar(comentarioCaptor.capture());
    assertThat(comentarioCaptor.getValue().getContenido(), equalTo(contenido));
  }

  @Test
  public void publicarComentarioDe2000PuntosDeCodigoDeberiaGuardarlo() {
    // preparacion
    String contenido = "\uD83D\uDE00".repeat(2000);

    // ejecucion
    this.servicioComentario.publicar(contenido);

    // validacion
    ArgumentCaptor<Comentario> comentarioCaptor = ArgumentCaptor.forClass(Comentario.class);
    verify(this.repositorioComentarioMock).guardar(comentarioCaptor.capture());
    assertThat(comentarioCaptor.getValue().getContenido(), equalTo(contenido));
  }

  @Test
  public void publicarComentarioDe2001CaracteresDeberiaLanzarComentarioInvalidoSinGuardar() {
    // preparacion
    String contenido = "a".repeat(2001);

    // ejecucion
    ComentarioInvalido excepcion = assertThrows(
      ComentarioInvalido.class,
      () -> this.servicioComentario.publicar(contenido)
    );

    // validacion
    assertThat(
      excepcion.getMessage(),
      equalTo("El comentario no puede superar los 2000 caracteres")
    );
    verify(this.repositorioComentarioMock, never()).guardar(any(Comentario.class));
  }

  @Test
  public void publicarComentarioVacioDeberiaLanzarComentarioInvalidoSinGuardar() {
    // preparacion
    String contenido = "";

    // ejecucion
    ComentarioInvalido excepcion = assertThrows(
      ComentarioInvalido.class,
      () -> this.servicioComentario.publicar(contenido)
    );

    // validacion
    assertThat(excepcion.getMessage(), equalTo("El comentario no puede estar vacío"));
    verify(this.repositorioComentarioMock, never()).guardar(any(Comentario.class));
  }

  @Test
  public void publicarComentarioSoloConEspaciosDeberiaLanzarComentarioInvalidoSinGuardar() {
    // preparacion
    String contenido = "   ";

    // ejecucion
    ComentarioInvalido excepcion = assertThrows(
      ComentarioInvalido.class,
      () -> this.servicioComentario.publicar(contenido)
    );

    // validacion
    assertThat(excepcion.getMessage(), equalTo("El comentario no puede estar vacío"));
    verify(this.repositorioComentarioMock, never()).guardar(any(Comentario.class));
  }

  @Test
  public void publicarComentarioNuloDeberiaLanzarComentarioInvalido() {
    // preparacion
    String contenido = null;

    // ejecucion
    ComentarioInvalido excepcion = assertThrows(
      ComentarioInvalido.class,
      () -> this.servicioComentario.publicar(contenido)
    );

    // validacion
    assertThat(excepcion.getMessage(), equalTo("El comentario no puede estar vacío"));
    verify(this.repositorioComentarioMock, never()).guardar(any(Comentario.class));
  }

  @Test
  public void publicarComentarioValidoDeberiaPreservarEspaciosAlInicioYFinal() {
    // preparacion
    String contenido = "  comentario valido  ";

    // ejecucion
    this.servicioComentario.publicar(contenido);

    // validacion
    ArgumentCaptor<Comentario> comentarioCaptor = ArgumentCaptor.forClass(Comentario.class);
    verify(this.repositorioComentarioMock).guardar(comentarioCaptor.capture());
    assertThat(comentarioCaptor.getValue().getContenido(), equalTo(contenido));
  }

  @Test
  public void listarComentariosDeberiaDevolverLosDelRepositorio() {
    // preparacion
    List<Comentario> comentariosEsperados = List.of(new Comentario(), new Comentario());
    when(this.repositorioComentarioMock.listarTodos()).thenReturn(comentariosEsperados);

    // ejecucion
    List<Comentario> resultado = this.servicioComentario.listar();

    // validacion
    assertThat(resultado, equalTo(comentariosEsperados));
  }

  @Test
  public void listarComentariosSinDatosDeberiaDevolverListaVacia() {
    // preparacion
    when(this.repositorioComentarioMock.listarTodos()).thenReturn(List.of());

    // ejecucion
    List<Comentario> resultado = this.servicioComentario.listar();

    // validacion
    assertThat(resultado, equalTo(List.of()));
  }
}
