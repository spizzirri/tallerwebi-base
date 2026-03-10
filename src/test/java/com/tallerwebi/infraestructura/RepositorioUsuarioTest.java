package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RepositorioUsuarioTest {

  private RepositorioUsuario repositorioUsuario;
  private SessionFactory sessionFactoryMock;
  private Session sessionMock;
  private Criteria criteriaMock;

  @BeforeEach
  public void init() {
    sessionFactoryMock = mock(SessionFactory.class);
    sessionMock = mock(Session.class);
    criteriaMock = mock(Criteria.class);

    when(sessionFactoryMock.getCurrentSession()).thenReturn(sessionMock);
    when(sessionMock.createCriteria(Usuario.class)).thenReturn(criteriaMock);
    when(criteriaMock.add(any(Criterion.class))).thenReturn(criteriaMock);

    repositorioUsuario = new RepositorioUsuarioImpl(sessionFactoryMock);
  }

  @Test
  public void buscarUsuarioDeberiaRetornarUsuarioSiExiste() {
    // preparacion
    Usuario usuarioEsperado = new Usuario();
    when(criteriaMock.uniqueResult()).thenReturn(usuarioEsperado);

    // ejecucion
    Usuario usuarioObtenido = repositorioUsuario.buscarUsuario("test@test.com", "123");

    // validacion
    assertThat(usuarioObtenido, equalTo(usuarioEsperado));
  }

  @Test
  public void guardarDeberiaLlamarAlMetodoSaveDeLaSesion() {
    // preparacion
    Usuario usuario = new Usuario();

    // ejecucion
    repositorioUsuario.guardar(usuario);

    // validacion
    verify(sessionMock, times(1)).save(usuario);
  }

  @Test
  public void buscarPorEmailDeberiaRetornarUsuarioSiExiste() {
    // preparacion
    Usuario usuarioEsperado = new Usuario();
    when(criteriaMock.uniqueResult()).thenReturn(usuarioEsperado);

    // ejecucion
    Usuario usuarioObtenido = repositorioUsuario.buscar("test@test.com");

    // validacion
    assertThat(usuarioObtenido, equalTo(usuarioEsperado));
  }

  @Test
  public void modificarDeberiaLlamarAlMetodoUpdateDeLaSesion() {
    // preparacion
    Usuario usuario = new Usuario();

    // ejecucion
    repositorioUsuario.modificar(usuario);

    // validacion
    verify(sessionMock, times(1)).update(usuario);
  }
}
