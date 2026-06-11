package com.tallerwebi.dominio;

import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.LoginGoogleException;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;

public class ServicioGoogleOAuth2Test {

  private ServicioLogin servicioLogin;
  private ServicioGoogleOAuth2Impl servicioGoogleOAuth2;
  private OidcUserRequest userRequest;
  private OidcUser oidcUser;
  private OidcUserService oidcUserService;

  @BeforeEach
  public void init() {
    servicioLogin = mock(ServicioLogin.class);
    userRequest = mock(OidcUserRequest.class);
    oidcUser = mock(OidcUser.class);
    oidcUserService = mock(OidcUserService.class);

    servicioGoogleOAuth2 = new ServicioGoogleOAuth2Impl();
    ReflectionTestUtils.setField(servicioGoogleOAuth2, "servicioLogin", servicioLogin);
    ReflectionTestUtils.setField(servicioGoogleOAuth2, "delegate", oidcUserService);
  }

  @Test
  public void procesarUsuarioDeberiaRegistrarSiNoExiste() throws UsuarioExistente {
    String email = "nuevo@email.com";
    dadoQueElUsuarioNoExiste(email);
    cuandoProcesoElUsuario();
    entoncesElUsuarioEsRegistrado();
  }

  @Test
  public void procesarUsuarioNoDeberiaRegistrarSiYaExiste() throws UsuarioExistente {
    String email = "existente@email.com";
    dadoQueElUsuarioYaExiste(email);
    cuandoProcesoElUsuario();
    entoncesElUsuarioNoEsRegistrado();
  }

  @Test
  public void procesarUsuarioDeberiaLanzarExcepcionSiRegistroFallaPorUsuarioExistente()
    throws UsuarioExistente {
    String email = "existe@email.com";
    dadoQueElUsuarioNoExiste(email);
    dadoQueElRegistroFallaConUsuarioExistente();
    cuandoProcesoElUsuarioYEsperoExcepcion();
  }

  @Test
  public void procesarUsuarioDeberiaLanzarExcepcionSiRegistroFallaPorErrorGeneral()
    throws UsuarioExistente {
    String email = "error@email.com";
    dadoQueElUsuarioNoExiste(email);
    dadoQueElRegistroFallaConErrorGeneral();
    cuandoProcesoElUsuarioYEsperoExcepcion();
  }

  private void dadoQueElUsuarioNoExiste(String email) {
    when(oidcUserService.loadUser(any(OidcUserRequest.class))).thenReturn(oidcUser);
    when(oidcUser.getAttribute("email")).thenReturn(email);
    when(servicioLogin.buscar(email)).thenReturn(null);
  }

  private void dadoQueElUsuarioYaExiste(String email) {
    when(oidcUserService.loadUser(any(OidcUserRequest.class))).thenReturn(oidcUser);
    when(oidcUser.getAttribute("email")).thenReturn(email);
    when(servicioLogin.buscar(email)).thenReturn(new Usuario());
  }

  private void dadoQueElRegistroFallaConUsuarioExistente() throws UsuarioExistente {
    doThrow(UsuarioExistente.class).when(servicioLogin).registrar(any(Usuario.class));
  }

  private void dadoQueElRegistroFallaConErrorGeneral() throws UsuarioExistente {
    doThrow(RuntimeException.class).when(servicioLogin).registrar(any(Usuario.class));
  }

  private void cuandoProcesoElUsuario() {
    servicioGoogleOAuth2.loadUser(userRequest);
  }

  private void cuandoProcesoElUsuarioYEsperoExcepcion() {
    Assertions.assertThrows(
      LoginGoogleException.class,
      () -> servicioGoogleOAuth2.loadUser(userRequest)
    );
  }

  private void entoncesElUsuarioEsRegistrado() throws UsuarioExistente {
    verify(servicioLogin).registrar(any(Usuario.class));
  }

  private void entoncesElUsuarioNoEsRegistrado() throws UsuarioExistente {
    verify(servicioLogin, never()).registrar(any(Usuario.class));
  }
}
