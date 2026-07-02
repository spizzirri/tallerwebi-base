package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Calendario.ServicioCalendario;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.presentacion.Calendario.CalendarioControlador;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.ModelAndView;

public class CalendarioControladorTest {

  private CalendarioControlador calendarioControlador;
  private ServicioCalendario servicioCalendarioMock;
  private HttpSession sessionMock;
  private Usuario usuarioMock;

  @BeforeEach
  public void init() {
    sessionMock = Mockito.mock(HttpSession.class);
    usuarioMock = Mockito.mock(Usuario.class);
    servicioCalendarioMock = Mockito.mock(ServicioCalendario.class);
    calendarioControlador = new CalendarioControlador(servicioCalendarioMock);
  }

  @Test
  public void queSiNoHaySesionRedirijaAlLogin() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(null);
    ModelAndView mav = calendarioControlador.irAMiCalendario(sessionMock);
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void siHaySesionQueVayaAlCalendario() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    ModelAndView mav = calendarioControlador.irAMiCalendario(sessionMock);
    assertThat(mav.getViewName(), equalToIgnoringCase("mi-calendario"));
  }
}
