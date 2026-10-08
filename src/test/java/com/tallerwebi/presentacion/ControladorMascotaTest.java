package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.HistorialTurnos;
import com.tallerwebi.dominio.Mascota;
import com.tallerwebi.dominio.ServicioMascota;
import com.tallerwebi.dominio.ServicioPlan;
import com.tallerwebi.dominio.ServicioSocio;
import com.tallerwebi.dominio.ServicioTurno;
import com.tallerwebi.dominio.Socio;
import com.tallerwebi.dominio.excepcion.TurnoInvalido;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorMascotaTest {

  private ControladorMascota controladorMascota;
  private ServicioSocio servicioSocioMock;
  private ServicioTurno servicioTurnoMock;
  private HttpSession sesionMock;
  private Socio socio;

  @BeforeEach
  public void init() {
    servicioSocioMock = mock(ServicioSocio.class);
    servicioTurnoMock = mock(ServicioTurno.class);
    controladorMascota =
      new ControladorMascota(
        servicioSocioMock,
        mock(ServicioPlan.class),
        mock(ServicioMascota.class),
        servicioTurnoMock
      );

    sesionMock = mock(HttpSession.class);
    when(sesionMock.getAttribute(ControladorLogin.USUARIO_ID)).thenReturn(2L);
    Mascota firulais = new Mascota();
    firulais.setId(1L);
    socio = new Socio();
    socio.agregarMascota(firulais);
    when(servicioSocioMock.buscarPorUsuario(2L)).thenReturn(socio);
  }

  @Test
  public void verPerfilDeberiaMostrarElHistorialDeTurnosDeLaMascota() throws TurnoInvalido {
    HistorialTurnos historial = new HistorialTurnos(List.of(), List.of());
    when(servicioTurnoMock.historialDe(socio, 1L)).thenReturn(historial);

    ModelAndView mav = controladorMascota.verPerfil(1L, sesionMock);

    assertThat(mav.getViewName(), equalTo("mascota-perfil"));
    assertThat(mav.getModel().get("historial"), is(historial));
  }

  @Test
  public void verPerfilDeUnaMascotaAjenaDeberiaVolverAlHome() throws TurnoInvalido {
    ModelAndView mav = controladorMascota.verPerfil(999L, sesionMock);

    assertThat(mav.getViewName(), equalTo("redirect:/home"));
    verify(servicioTurnoMock, never()).historialDe(any(), any());
  }

  @Test
  public void verPerfilSinSerSocioDeberiaVolverAlHome() {
    when(servicioSocioMock.buscarPorUsuario(2L)).thenReturn(null);

    ModelAndView mav = controladorMascota.verPerfil(1L, sesionMock);

    assertThat(mav.getViewName(), equalTo("redirect:/home"));
  }
}
