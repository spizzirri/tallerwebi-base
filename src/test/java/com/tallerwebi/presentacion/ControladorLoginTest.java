package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.microsoft.playwright.Response;
import com.tallerwebi.dominio.Mail.ServicioEmail;
import com.tallerwebi.dominio.Usuario.ServicioLogin;
import com.tallerwebi.dominio.Usuario.ServicioRecuperacionContrasenia;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

public class ControladorLoginTest {

  private ControladorLogin controladorLogin;
  private Usuario usuarioMock;
  private DatosLogin datosLoginMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;
  private ServicioLogin servicioLoginMock;
  private HttpServletResponse responseMock;
  private ServicioRecuperacionContrasenia servicioRecuperacionContraseniaMock;
  private ServicioEmail servicioEmailMock;

  @BeforeEach
  public void init() {
    datosLoginMock = new DatosLogin("dami@unlam.com", "123");
    usuarioMock = mock(Usuario.class);
    when(usuarioMock.getEmail()).thenReturn("dami@unlam.com");
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    servicioLoginMock = mock(ServicioLogin.class);
    servicioRecuperacionContraseniaMock = mock(ServicioRecuperacionContrasenia.class);
    servicioEmailMock = mock(ServicioEmail.class);
    controladorLogin =
      new ControladorLogin(
        servicioLoginMock,
        servicioRecuperacionContraseniaMock,
        servicioEmailMock
      );
    responseMock = mock(HttpServletResponse.class);
  }

  @Test
  public void loginConUsuarioYPasswordIncorrectosDeberiaLlevarALoginNuevamente() {
    // preparacion
    when(servicioLoginMock.consultarUsuarioLogin(anyString(), anyString())).thenReturn(null);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.validarLogin(
      datosLoginMock,
      requestMock,
      responseMock
    );

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Usuario o clave incorrecta")
    );
    verify(sessionMock, times(0)).setAttribute("ROL", "ADMIN");
  }

  @Test
  public void loginConUsuarioYPasswordCorrectosDeberiaLLevarAHome() {
    // preparacion
    Usuario usuarioEncontradoMock = mock(Usuario.class);
    when(usuarioEncontradoMock.getRol()).thenReturn("ADMIN");

    when(requestMock.getSession()).thenReturn(sessionMock);
    when(servicioLoginMock.consultarUsuarioLogin(anyString(), anyString()))
      .thenReturn(usuarioEncontradoMock);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.validarLogin(
      datosLoginMock,
      requestMock,
      responseMock
    );

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
    verify(sessionMock, times(1)).setAttribute("ROL", usuarioEncontradoMock.getRol());
  }

  @Test
  public void registrameSiUsuarioNoExisteDeberiaCrearUsuarioYVolverAlLogin()
    throws UsuarioExistente {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.registrarme(usuarioMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
    verify(servicioLoginMock, times(1)).registrar(usuarioMock);
  }

  @Test
  public void registrarmeSiUsuarioExisteDeberiaVolverAFormularioYMostrarError()
    throws UsuarioExistente {
    // preparacion
    doThrow(UsuarioExistente.class).when(servicioLoginMock).registrar(usuarioMock);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.registrarme(usuarioMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("El usuario ya existe")
    );
  }

  @Test
  public void errorEnRegistrarmeDeberiaVolverAFormularioYMostrarError() throws UsuarioExistente {
    // preparacion
    doThrow(RuntimeException.class).when(servicioLoginMock).registrar(usuarioMock);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.registrarme(usuarioMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Error al registrar el nuevo usuario")
    );
  }

  @Test
  public void irALoginDeberiaRetornarVistaLoginConDatosLogin() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.irALogin(requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
    assertThat(modelAndView.getModel().get("datosLogin"), instanceOf(DatosLogin.class));
  }

  @Test
  public void nuevoUsuarioDeberiaRetornarVistaNuevoUsuarioConUsuarioVacio() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.nuevoUsuario();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
    assertThat(modelAndView.getModel().get("usuario"), instanceOf(Usuario.class));
  }

  @Test
  public void inicioDeberiaRedirigirALogin() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.inicio();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void queAlCerrarSesionMeRedirijaAlLogin() {
    when(requestMock.getSession()).thenReturn(sessionMock);
    ModelAndView mv = controladorLogin.logout(requestMock);

    verify(sessionMock, times(1)).invalidate();
    assertThat(mv.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  //test de la cookie del recordarme
  @Test
  public void loginConRememberMeDeberiaGuardarLaCookie() {
    datosLoginMock.setRememberMe(true); //marco el check

    Usuario usuarioEncontradoMock = mock(Usuario.class);
    when(usuarioEncontradoMock.getRol()).thenReturn("USER");
    when(requestMock.getSession()).thenReturn(sessionMock);
    when(servicioLoginMock.consultarUsuarioLogin(anyString(), anyString()))
      .thenReturn(usuarioEncontradoMock);

    controladorLogin.validarLogin(datosLoginMock, requestMock, responseMock);

    verify(responseMock, times(1)).addCookie(any(Cookie.class));
  }

  @Test
  public void loginSinRememberMeNoDeberiaGuardarLaCookie() {
    datosLoginMock.setRememberMe(false); // ← no marcó el checkbox

    Usuario usuarioEncontradoMock = mock(Usuario.class);
    when(usuarioEncontradoMock.getRol()).thenReturn("USER");
    when(requestMock.getSession()).thenReturn(sessionMock);
    when(servicioLoginMock.consultarUsuarioLogin(anyString(), anyString()))
      .thenReturn(usuarioEncontradoMock);

    controladorLogin.validarLogin(datosLoginMock, requestMock, responseMock);

    // verificamos que NO se agregó ninguna cookie
    verify(responseMock, times(0)).addCookie(any(Cookie.class));
  }

  @Test
  public void irALoginConCookieDeberiaPrecargarEmail() {
    // preparacion
    Cookie cookie = new Cookie("emailRecordado", "ro@unlam.com");
    when(requestMock.getCookies()).thenReturn(new Cookie[] { cookie });

    // ejecucion
    ModelAndView modelAndView = controladorLogin.irALogin(requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
    DatosLogin datos = (DatosLogin) modelAndView.getModel().get("datosLogin");
    assertThat(datos.getEmail(), equalToIgnoringCase("ro@unlam.com"));
  }

  @Test
  public void irALoginSinCookieDeberiaRetornarEmailVacio() {
    // preparacion
    when(requestMock.getCookies()).thenReturn(null);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.irALogin(requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
    DatosLogin datos = (DatosLogin) modelAndView.getModel().get("datosLogin");
    assertThat(datos.getEmail(), equalToIgnoringCase(""));
  }

  @Test
  public void verificarMailParaCambioDeClaveDeberiaRetornarOkSiElUsuarioExiste() {
    when(servicioLoginMock.usuarioYaExiste(any(Usuario.class))).thenReturn(true);

    ResponseEntity<String> respuesta = controladorLogin.verificarEmail("ro@unlam.com", sessionMock);

    assertThat(respuesta.getStatusCode(), equalTo(HttpStatus.OK));

    verify(sessionMock, times(1)).setAttribute(eq("codigoRecuperacion"), any());

    verify(sessionMock, times(1)).setAttribute(eq("emailRecuperacion"), eq("ro@unlam.com"));
  }

  @Test
  public void verificarMailParaCambioDeClaveDeberiaRetornarNotFoundSiElUsuarioNoExiste() {
    when(servicioLoginMock.usuarioYaExiste(any(Usuario.class))).thenReturn(false);

    ResponseEntity<String> respuesta = controladorLogin.verificarEmail("ro@unlam.com", sessionMock);

    assertThat(respuesta.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));

    verify(sessionMock, never()).setAttribute(anyString(), any());
  }

  @Test
  public void olvidasteTuContraseniaDebeRetornarVistaContraseniaOlvidada() {
    ModelAndView mv = controladorLogin.irACambiarContrasenia();
    assertThat(mv.getViewName(), equalToIgnoringCase("cambiarContrasenia"));
  }

  @Test
  public void actualizarContraseniaDebeCambiarPasswordSiCodigoVerificado() {
    when(sessionMock.getAttribute("codigoVerificado")).thenReturn(true);
    when(sessionMock.getAttribute("emailRecuperacion")).thenReturn("ro@test.com");

    ResponseEntity<Void> response = controladorLogin.actualizarContrasenia(
      "ro@test.com",
      "nuevaClave",
      sessionMock
    );

    assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

    verify(servicioLoginMock, times(1)).cambiarContrasenia("ro@test.com", "nuevaClave");

    verify(sessionMock, times(1)).removeAttribute("codigoRecuperacion");

    verify(sessionMock, times(1)).removeAttribute("emailRecuperacion");
  }

  @Test
  public void actualizarContraseniaDebeFallarSiCodigoNoVerificado() {
    when(sessionMock.getAttribute("codigoVerificado")).thenReturn(false);

    ResponseEntity<Void> response = controladorLogin.actualizarContrasenia(
      "ro@test.com",
      "nuevaClave",
      sessionMock
    );

    assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));

    verify(servicioLoginMock, never()).cambiarContrasenia(anyString(), anyString());
  }
}
