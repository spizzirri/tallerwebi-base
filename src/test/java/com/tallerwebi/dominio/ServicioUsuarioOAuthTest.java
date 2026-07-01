package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Usuario.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario.ServicioUsuarioOAuth;
import com.tallerwebi.dominio.Usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class ServicioUsuarioOAuthTest {

  private ServicioUsuarioOAuth servicioUsuarioOAuth;
  private RepositorioUsuario repositorioUsuarioMock;

  @BeforeEach
  public void init() {
    repositorioUsuarioMock = mock(RepositorioUsuario.class);

    servicioUsuarioOAuth = new ServicioUsuarioOAuth();

    ReflectionTestUtils.setField(
      servicioUsuarioOAuth,
      "repositorioUsuario",
      repositorioUsuarioMock
    );
  }

  @Test
  public void dadoUnUsuarioQueNoExisteCuandoSeLogueaConGoogleDebeCrearse() {
    when(repositorioUsuarioMock.buscarUsuarioPorEmail("test@gmail.com")).thenReturn(null);

    Usuario usuario = servicioUsuarioOAuth.buscarOCrearUsuario(
      "test@gmail.com",
      "Juan",
      "Perez",
      "foto.jpg"
    );

    assertNotNull(usuario);
    assertEquals("test@gmail.com", usuario.getEmail());
    assertEquals("Juan", usuario.getNombre());
    assertEquals("Perez", usuario.getApellido());
    assertEquals("CLIENTE", usuario.getRol());
    assertTrue(usuario.getActivo());
    assertEquals("foto.jpg", usuario.getFotoPerfil());

    verify(repositorioUsuarioMock).guardar(usuario);
  }

  @Test
  public void dadoUnUsuarioExistenteConFotoNoDebeActualizarse() {
    Usuario usuario = new Usuario();
    usuario.setFotoPerfil("fotoVieja.jpg");

    when(repositorioUsuarioMock.buscarUsuarioPorEmail(anyString())).thenReturn(usuario);

    Usuario obtenido = servicioUsuarioOAuth.buscarOCrearUsuario(
      "test@gmail.com",
      "Juan",
      "Perez",
      "fotoNueva.jpg"
    );

    assertSame(usuario, obtenido);

    verify(repositorioUsuarioMock, never()).guardar(any());
  }

  @Test
  public void dadoUnUsuarioExistenteSinFotoDebeActualizarseLaFoto() {
    Usuario usuario = new Usuario();
    usuario.setFotoPerfil(null);

    when(repositorioUsuarioMock.buscarUsuarioPorEmail(anyString())).thenReturn(usuario);

    servicioUsuarioOAuth.buscarOCrearUsuario("test@gmail.com", "Juan", "Perez", "fotoNueva.jpg");

    assertEquals("fotoNueva.jpg", usuario.getFotoPerfil());

    verify(repositorioUsuarioMock).guardar(usuario);
  }
}
