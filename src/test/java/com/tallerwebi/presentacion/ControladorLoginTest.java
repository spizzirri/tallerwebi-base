package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.ServicioLogin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorLoginTest {

  private ControladorLogin controladorLogin;
  private ServicioLogin servicioLoginMock;

  @BeforeEach
  public void init() {
    servicioLoginMock = mock(ServicioLogin.class);
    controladorLogin = new ControladorLogin(servicioLoginMock);
  }

  @Test
  public void irALoginDeberiaLlevarALogin() {
    ModelAndView modelAndView = controladorLogin.irALogin();
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
  }

  @Test
  public void irAHomeDeberiaLlevarAHome() {
    ModelAndView modelAndView = controladorLogin.irAHome();
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("home"));
  }

  @Test
  public void inicioDeberiaRedirigirALogin() {
    ModelAndView modelAndView = controladorLogin.inicio();
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
  }
}
