package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.ServicioCobertura;
import com.tallerwebi.dominio.ServicioSocio;
import com.tallerwebi.dominio.ServicioTurno;
import com.tallerwebi.dominio.ServicioVeterinaria;
import com.tallerwebi.dominio.Socio;
import com.tallerwebi.dominio.SolicitudTurno;
import com.tallerwebi.dominio.excepcion.TurnoInvalido;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

public class ControladorTurnoTest {

  private ControladorTurno controladorTurno;
  private ServicioSocio servicioSocioMock;
  private ServicioTurno servicioTurnoMock;
  private HttpSession sesionMock;
  private Socio socio;
  private SolicitudTurno solicitud;
  private RedirectAttributesModelMap redirectAttributes;

  @BeforeEach
  public void init() {
    servicioSocioMock = mock(ServicioSocio.class);
    servicioTurnoMock = mock(ServicioTurno.class);
    controladorTurno =
      new ControladorTurno(
        servicioSocioMock,
        servicioTurnoMock,
        mock(ServicioVeterinaria.class),
        mock(ServicioCobertura.class)
      );

    sesionMock = mock(HttpSession.class);
    when(sesionMock.getAttribute(ControladorLogin.USUARIO_ID)).thenReturn(2L);
    socio = new Socio();
    when(servicioSocioMock.buscarPorUsuario(2L)).thenReturn(socio);
    solicitud = new SolicitudTurno();
    redirectAttributes = new RedirectAttributesModelMap();
  }

  @Test
  public void nuevoTurnoDeberiaMostrarElFormulario() {
    ModelAndView mav = controladorTurno.nuevoTurno(solicitud, sesionMock);

    assertThat(mav.getViewName(), equalTo("nuevo-turno"));
    assertThat(mav.getModel().get("solicitud"), is(solicitud));
  }

  @Test
  public void nuevoTurnoSinSerSocioDeberiaVolverAlHome() {
    when(servicioSocioMock.buscarPorUsuario(2L)).thenReturn(null);

    ModelAndView mav = controladorTurno.nuevoTurno(solicitud, sesionMock);

    assertThat(mav.getViewName(), equalTo("redirect:/home"));
  }

  @Test
  public void reservarConExitoDeberiaIrAMisTurnosConMensaje() throws TurnoInvalido {
    ModelAndView mav = controladorTurno.reservar(solicitud, sesionMock, redirectAttributes);

    verify(servicioTurnoMock, times(1)).reservar(socio, solicitud);
    assertThat(mav.getViewName(), equalTo("redirect:/turnos"));
    assertThat(
      redirectAttributes.getFlashAttributes().get("mensaje"),
      equalTo("¡Listo! Tu turno quedó confirmado.")
    );
  }

  @Test
  public void reservarConErrorDeberiaVolverAlFormularioMostrandoElMotivo() throws TurnoInvalido {
    when(servicioTurnoMock.reservar(socio, solicitud))
      .thenThrow(new TurnoInvalido("El horario elegido ya no está disponible"));

    ModelAndView mav = controladorTurno.reservar(solicitud, sesionMock, redirectAttributes);

    assertThat(mav.getViewName(), equalTo("nuevo-turno"));
    assertThat(mav.getModel().get("error"), equalTo("El horario elegido ya no está disponible"));
  }

  @Test
  public void misTurnosDeberiaMostrarLosTurnosDelSocio() {
    ModelAndView mav = controladorTurno.misTurnos(sesionMock);

    assertThat(mav.getViewName(), equalTo("mis-turnos"));
    verify(servicioTurnoMock, times(1)).turnosDe(socio);
  }

  @Test
  public void cancelarDeberiaCancelarElTurnoYVolverAMisTurnos() throws TurnoInvalido {
    ModelAndView mav = controladorTurno.cancelar(5L, sesionMock, redirectAttributes);

    verify(servicioTurnoMock, times(1)).cancelar(socio, 5L);
    assertThat(mav.getViewName(), equalTo("redirect:/turnos"));
  }

  @Test
  public void cancelarUnTurnoInvalidoDeberiaMostrarElError() throws TurnoInvalido {
    doThrow(new TurnoInvalido("No encontramos ese turno"))
      .when(servicioTurnoMock)
      .cancelar(socio, 5L);

    controladorTurno.cancelar(5L, sesionMock, redirectAttributes);

    assertThat(
      redirectAttributes.getFlashAttributes().get("error"),
      equalTo("No encontramos ese turno")
    );
  }
}
