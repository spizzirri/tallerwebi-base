package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Hijos.ServicioHijo;
import com.tallerwebi.dominio.Usuario.ServicioLogin;
import com.tallerwebi.dominio.Usuario.ServicioUsuario;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.NoSePudoGuardarInformacionException;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class PerfilControladorTest {

  private Usuario usuarioMock;
  private PerfilControlador perfilControlador;
  private HttpSession sessionMock;
  private ServicioUsuario servicioUsuarioMock;
  private BindingResult bindingResultMock;
  private DatosEditarPerfilDTO dto;
  private RedirectAttributes redirectAttributesMock;
  private ServicioLogin servicioLoginMock;

  @BeforeEach
  public void init() {
    servicioUsuarioMock = Mockito.mock(ServicioUsuario.class);
    perfilControlador = new PerfilControlador(servicioUsuarioMock, servicioLoginMock);
    usuarioMock = Mockito.mock(Usuario.class);
    sessionMock = Mockito.mock(HttpSession.class);
    bindingResultMock = Mockito.mock(BindingResult.class);
    dto = new DatosEditarPerfilDTO();
    redirectAttributesMock = mock(RedirectAttributes.class);
  }

  @Test
  public void siNoHayUsuarioRegistradoAlApretarPerfilIrAlLogin() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(null);
    ModelAndView modelAndView = perfilControlador.irAlPerfil(sessionMock);
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void siHayUsuarioRegistradoAlApretarPerfilIrAlPerfil() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    ModelAndView modelAndView = perfilControlador.irAlPerfil(sessionMock);
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("perfil"));
  }

  @Test
  public void elPerfilDebeMostrarNombreDeUsuario() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getNombre()).thenReturn("nombrePrueba");
    ModelAndView modelAndView = perfilControlador.mostrarDatosUsuario(sessionMock);
    assertThat(
      ((Usuario) modelAndView.getModel().get("usuario")).getNombre(),
      equalToIgnoringCase("nombrePrueba")
    );
  }

  @Test
  public void elPerfilDebeMostrarApellidoDeUsuario() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getApellido()).thenReturn("apellidoPrueba");
    ModelAndView modelAndView = perfilControlador.mostrarDatosUsuario(sessionMock);
    assertThat(
      ((Usuario) modelAndView.getModel().get("usuario")).getApellido(),
      equalToIgnoringCase("apellidoPrueba")
    );
  }

  @Test
  public void elPerfilDebeMostrarEmailDeUsuario() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getEmail()).thenReturn("email");
    ModelAndView modelAndView = perfilControlador.mostrarDatosUsuario(sessionMock);
    assertThat(
      ((Usuario) modelAndView.getModel().get("usuario")).getEmail(),
      equalToIgnoringCase("email")
    );
  }

  @Test
  public void elPerfilDebeMostrarFotoDePerfilDeUsuario() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getFotoPerfil()).thenReturn("foto.jpg");
    ModelAndView modelAndView = perfilControlador.mostrarDatosUsuario(sessionMock);
    assertThat(
      ((Usuario) modelAndView.getModel().get("usuario")).getFotoPerfil(),
      equalToIgnoringCase("foto.jpg")
    );
  }

  @Test
  public void seDebePoderCambiarElMailDelUsuario() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getId()).thenReturn(1L);

    when(bindingResultMock.hasErrors()).thenReturn(false);

    String mailNuevo = "nuevo@test.com";
    Usuario usuarioActualizado = new Usuario();
    usuarioActualizado.setEmail(mailNuevo);

    when(servicioUsuarioMock.buscarPorId(1L)).thenReturn(usuarioActualizado);

    dto.setEmail(mailNuevo);

    perfilControlador.editarPerfil(dto, bindingResultMock, sessionMock, redirectAttributesMock);
    verify(servicioUsuarioMock).actualizarMail(1L, mailNuevo);
    verify(sessionMock).setAttribute("USUARIO", usuarioActualizado);
  }

  @Test
  public void siNoSePudoGuardarMailDebeMostrarMensajeDeErrorException() {
    String msjError = "No se pudo guardar el nuevo mail ";

    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getId()).thenReturn(1L);

    when(bindingResultMock.hasErrors()).thenReturn(false);

    String mailNuevo = "nuevo@test.com";
    dto.setEmail(mailNuevo);

    Mockito
      .doThrow(new NoSePudoGuardarInformacionException(msjError))
      .when(servicioUsuarioMock)
      .actualizarMail(1L, mailNuevo);

    ModelAndView mv = perfilControlador.editarPerfil(
      dto,
      bindingResultMock,
      sessionMock,
      redirectAttributesMock
    );
    assertThat((String) mv.getModel().get("mensajeError"), equalToIgnoringCase(msjError));
  }

  @Test
  public void seDebePoderCambiarElCelularDelUsuario() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getId()).thenReturn(1L);
    when(bindingResultMock.hasErrors()).thenReturn(false);

    String nuevoCel = "1122334455";
    Usuario usuarioActualizado = new Usuario();
    usuarioActualizado.setCelular(Long.parseLong(nuevoCel));
    when(servicioUsuarioMock.buscarPorId(1L)).thenReturn(usuarioActualizado);
    dto.setCelular(nuevoCel);
    perfilControlador.editarPerfil(dto, bindingResultMock, sessionMock, redirectAttributesMock);

    verify(servicioUsuarioMock).actualizarCelular(1L, Long.parseLong(nuevoCel));

    verify(sessionMock).setAttribute("USUARIO", usuarioActualizado);
  }

  @Test
  public void siNoSePudoGuardarCelularDebeMostrarMensajeDeErrorException() {
    String msjError = "No se pudo guardar el nuevo celular ";
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getId()).thenReturn(1L);

    when(bindingResultMock.hasErrors()).thenReturn(false);

    String celNuevo = "123456789";
    dto.setCelular(celNuevo);

    Mockito
      .doThrow(new NoSePudoGuardarInformacionException(msjError))
      .when(servicioUsuarioMock)
      .actualizarCelular(1L, Long.parseLong(celNuevo));

    ModelAndView mv = perfilControlador.editarPerfil(
      dto,
      bindingResultMock,
      sessionMock,
      redirectAttributesMock
    );

    assertThat((String) mv.getModel().get("mensajeError"), equalToIgnoringCase(msjError));
  }

  @Test
  public void seDebePoderCambiarLaFotoDelUsuario() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getId()).thenReturn(1L);
    when(bindingResultMock.hasErrors()).thenReturn(false);

    MultipartFile fotoMock = Mockito.mock(MultipartFile.class);
    when(fotoMock.isEmpty()).thenReturn(false);
    dto.setFotoPerfil(fotoMock);

    perfilControlador.editarPerfil(dto, bindingResultMock, sessionMock, redirectAttributesMock);

    verify(servicioUsuarioMock).actualizarFoto(1L, fotoMock);
  }

  @Test
  public void siNoSePudoGuardarFotoDebeMostrarMensajeDeErrorException() {
    String msjError = "No se pudo guardar la nueva foto de perfil";

    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getId()).thenReturn(1L);

    when(bindingResultMock.hasErrors()).thenReturn(false);

    MultipartFile fotoMock = Mockito.mock(MultipartFile.class);

    when(fotoMock.isEmpty()).thenReturn(false);

    dto.setFotoPerfil(fotoMock);

    Mockito
      .doThrow(new NoSePudoGuardarInformacionException(msjError))
      .when(servicioUsuarioMock)
      .actualizarFoto(1L, fotoMock);

    ModelAndView mv = perfilControlador.editarPerfil(
      dto,
      bindingResultMock,
      sessionMock,
      redirectAttributesMock
    );

    assertThat((String) mv.getModel().get("mensajeError"), equalToIgnoringCase(msjError));
  }
}
