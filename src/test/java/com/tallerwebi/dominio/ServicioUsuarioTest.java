package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Carrito.RepositorioCarrito;
import com.tallerwebi.dominio.Hijos.RepositorioHijo;
import com.tallerwebi.dominio.Pedidos.RepositorioPedido;
import com.tallerwebi.dominio.SubidaDeImgs.ServicioImagenes;
import com.tallerwebi.dominio.Usuario.*;
import com.tallerwebi.dominio.excepcion.NoSePudoGuardarInformacionException;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

public class ServicioUsuarioTest {

  private ServicioImagenes servicioImagenesMock;
  private ServicioUsuario servicioUsuario;
  private RepositorioUsuario repositorioUsuarioMock;
  private RepositorioHijo repositorioHijoMock;
  private RepositorioCarrito repositorioCarritoMock;
  private RepositorioPedido repositorioPedidoMock;

  @BeforeEach
  public void init() {
    repositorioUsuarioMock = mock(RepositorioUsuario.class);
    servicioImagenesMock = mock(ServicioImagenes.class);
    repositorioHijoMock = mock(RepositorioHijo.class);
    repositorioCarritoMock = mock(RepositorioCarrito.class);
    repositorioPedidoMock = mock(RepositorioPedido.class);

    this.servicioUsuario =
      new ServicioUsuarioImpl(
        repositorioUsuarioMock,
        servicioImagenesMock,
        repositorioHijoMock,
        repositorioCarritoMock,
        repositorioPedidoMock
      );
  }

  @Test
  public void cambiarMailDebeLLamarAlRepoYCambiarlo() {
    String email = "ro@test.com";
    String nuevoMail = "nuevo@test.com";

    Usuario usuario = new Usuario();
    Long id = 1L;
    usuario.setEmail(email);

    when(repositorioUsuarioMock.buscarUsuarioPorId(id)).thenReturn(usuario);
    servicioUsuario.actualizarMail(id, nuevoMail);

    verify(repositorioUsuarioMock, times(1)).modificar(usuario);
    assertThat(usuario.getEmail(), equalTo(nuevoMail));
  }

  @Test
  public void cambiarCelularDebeLLamarAlRepoYCambiarlo() {
    Long nuevoCelu = 1122334455L;

    Usuario usuario = new Usuario();
    Long id = 1L;
    usuario.setCelular(nuevoCelu);

    when(repositorioUsuarioMock.buscarUsuarioPorId(id)).thenReturn(usuario);
    servicioUsuario.actualizarCelular(id, nuevoCelu);

    verify(repositorioUsuarioMock, times(1)).modificar(usuario);
    assertThat(usuario.getCelular(), equalTo(nuevoCelu));
  }

  @Test
  public void cambiarFotoPerfilDebeLLamarAlRepoYCambiarlo() throws Exception {
    Usuario usuario = new Usuario();
    Long id = 1L;

    MultipartFile fotoMock = mock(MultipartFile.class);

    when(fotoMock.getOriginalFilename()).thenReturn("foto.jpg");

    when(repositorioUsuarioMock.buscarUsuarioPorId(id)).thenReturn(usuario);

    when(servicioImagenesMock.subirImagen(fotoMock, "KionetTWI/img_perfiles"))
      .thenReturn("https://res.cloudinary.com/test/foto.jpg");

    servicioUsuario.actualizarFoto(id, fotoMock);

    verify(repositorioUsuarioMock, times(1)).modificar(usuario);
    assertThat(usuario.getFotoPerfil(), equalTo("https://res.cloudinary.com/test/foto.jpg"));
  }

  @Test
  public void dadoUsuarioInexistenteCuandoActualizaFotoEntoncesLanzaExcepcion() {
    MultipartFile fotoMock = mock(MultipartFile.class);

    when(repositorioUsuarioMock.buscarUsuarioPorId(99L)).thenReturn(null);

    assertThrows(
      NoSePudoGuardarInformacionException.class,
      () -> servicioUsuario.actualizarFoto(99L, fotoMock)
    );
  }

  @Test
  public void dadoUnUsuarioExistenteCuandoSeEliminaLaCuentaEntoncesSeEliminanTodosSusDatos() {
    Long idUsuario = 1L;

    Usuario usuario = new Usuario();

    when(repositorioUsuarioMock.buscarUsuarioPorId(idUsuario)).thenReturn(usuario);

    servicioUsuario.eliminarCuenta(idUsuario);

    verify(repositorioHijoMock).eliminarPorUsuario(idUsuario);
    verify(repositorioCarritoMock).eliminarPorUsuario(idUsuario);
    verify(repositorioPedidoMock).eliminarPorUsuario(idUsuario);

    verify(repositorioUsuarioMock).buscarUsuarioPorId(idUsuario);
    verify(repositorioUsuarioMock).eliminar(usuario);
  }

  @Test
  public void dadoUnUsuarioInexistenteCuandoSeEliminaLaCuentaEntoncesNoSeDebeEliminarElUsuario() {
    Long idUsuario = 1L;

    when(repositorioUsuarioMock.buscarUsuarioPorId(idUsuario)).thenReturn(null);

    servicioUsuario.eliminarCuenta(idUsuario);

    verify(repositorioHijoMock).eliminarPorUsuario(idUsuario);
    verify(repositorioCarritoMock).eliminarPorUsuario(idUsuario);
    verify(repositorioPedidoMock).eliminarPorUsuario(idUsuario);

    verify(repositorioUsuarioMock).buscarUsuarioPorId(idUsuario);

    verify(repositorioUsuarioMock, never()).eliminar(any());
  }
}
