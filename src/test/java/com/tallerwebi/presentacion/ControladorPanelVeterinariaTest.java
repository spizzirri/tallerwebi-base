package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.ServicioCobertura;
import com.tallerwebi.dominio.ServicioTurno;
import com.tallerwebi.dominio.ServicioVeterinaria;
import com.tallerwebi.dominio.Turno;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.Veterinaria;
import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;
import com.tallerwebi.dominio.excepcion.TurnoInvalido;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

public class ControladorPanelVeterinariaTest {

  private static final Long USUARIO_ID = 55L;
  private static final Long TURNO_ID = 9L;

  private ServicioVeterinaria servicioVeterinaria;
  private ServicioCobertura servicioCobertura;
  private ServicioTurno servicioTurno;
  private ControladorPanelVeterinaria controlador;
  private HttpSession sesion;
  private Veterinaria veterinaria;
  private DatosVeterinaria datos;

  @BeforeEach
  public void init() {
    servicioVeterinaria = mock(ServicioVeterinaria.class);
    servicioCobertura = mock(ServicioCobertura.class);
    servicioTurno = mock(ServicioTurno.class);
    controlador =
      new ControladorPanelVeterinaria(servicioVeterinaria, servicioCobertura, servicioTurno);
    sesion = mock(HttpSession.class);
    when(sesion.getAttribute(ControladorLogin.USUARIO_ID)).thenReturn(USUARIO_ID);
    when(sesion.getAttribute(ControladorLogin.ROL)).thenReturn(Usuario.ROL_VETERINARIA);
    Usuario usuario = new Usuario();
    usuario.setId(USUARIO_ID);
    usuario.setEmail("vet@test.com");
    veterinaria = new Veterinaria();
    veterinaria.setId(7L);
    veterinaria.setUsuario(usuario);
    when(servicioVeterinaria.buscarPorUsuario(USUARIO_ID)).thenReturn(veterinaria);
    when(servicioCobertura.listarServicios()).thenReturn(List.of());
    datos = new DatosVeterinaria();
    datos.setNombre("Veterinaria Central");
    datos.setCuit("30712345671");
    datos.setDireccion("Calle 123");
    datos.setTelefono("4444-1111");
    datos.setEmail("vet@test.com");
  }

  @Test
  public void panelDeberiaMostrarElPerfilEditable() {
    ModelAndView mav = controlador.panel(sesion);

    assertThat(mav.getViewName(), equalTo("panel-veterinaria"));
    assertThat(mav.getModel().get("datosVeterinaria"), instanceOf(DatosVeterinaria.class));
    assertThat(mav.getModel().get("veterinaria"), is(veterinaria));
  }

  @Test
  public void inicioDeberiaMostrarLaVeterinariaLogueada() {
    List<Turno> turnos = List.of(new Turno());
    when(servicioTurno.turnosDe(veterinaria)).thenReturn(turnos);

    ModelAndView mav = controlador.inicio(sesion);

    assertThat(mav.getViewName(), equalTo("inicio-veterinaria"));
    assertThat(mav.getModel().get("veterinaria"), is(veterinaria));
    assertThat(mav.getModel().get("turnos"), is(turnos));
  }

  @Test
  public void inicioDeberiaRechazarCuentasQueNoSonVeterinaria() {
    when(sesion.getAttribute(ControladorLogin.ROL)).thenReturn("SOCIO");

    assertThat(controlador.inicio(sesion).getViewName(), equalTo("redirect:/home"));
    verifyNoInteractions(servicioVeterinaria);
  }

  @Test
  public void panelDeberiaRechazarCuentasQueNoSonVeterinaria() {
    when(sesion.getAttribute(ControladorLogin.ROL)).thenReturn("SOCIO");

    assertThat(controlador.panel(sesion).getViewName(), equalTo("redirect:/home"));
    verifyNoInteractions(servicioVeterinaria);
  }

  @Test
  public void actualizarPerfilDeberiaGuardarCambiosYRedirigir() throws Exception {
    RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

    ModelAndView mav = controlador.actualizarPerfil(datos, sesion, attrs);

    verify(servicioVeterinaria)
      .actualizarPerfil(eq(USUARIO_ID), any(Veterinaria.class), eq("vet@test.com"), anyList());
    assertThat(mav.getViewName(), equalTo("redirect:/veterinaria/panel"));
    assertThat(
      attrs.getFlashAttributes().get("mensaje"),
      equalTo("Actualizaste los datos de tu veterinaria.")
    );
  }

  @Test
  public void actualizarPerfilConEmailExistenteDeberiaMostrarElError() throws Exception {
    doThrow(UsuarioExistente.class)
      .when(servicioVeterinaria)
      .actualizarPerfil(anyLong(), any(), anyString(), anyList());

    ModelAndView mav = controlador.actualizarPerfil(
      datos,
      sesion,
      new RedirectAttributesModelMap()
    );

    assertThat(mav.getViewName(), equalTo("panel-veterinaria"));
    assertThat(mav.getModel().get("error"), equalTo("Ya existe una cuenta con ese email"));
  }

  @Test
  public void actualizarPerfilConDatosInvalidosDeberiaMostrarElError() throws Exception {
    doThrow(new DatosDeRegistroInvalidos("Elegí al menos un servicio"))
      .when(servicioVeterinaria)
      .actualizarPerfil(anyLong(), any(), anyString(), anyList());

    ModelAndView mav = controlador.actualizarPerfil(
      datos,
      sesion,
      new RedirectAttributesModelMap()
    );

    assertThat(mav.getModel().get("error"), equalTo("Elegí al menos un servicio"));
  }

  @Test
  public void turnosDeberiaListarLosTurnosDeLaVeterinaria() {
    when(servicioTurno.turnosDe(veterinaria)).thenReturn(List.of(new Turno()));

    ModelAndView mav = controlador.turnos(sesion);

    assertThat(mav.getViewName(), equalTo("turnos-veterinaria"));
    assertThat(mav.getModel().get("turnos"), is(servicioTurno.turnosDe(veterinaria)));
  }

  @Test
  public void cancelarTurnoDeberiaRedirigirConMensaje() throws Exception {
    RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

    ModelAndView mav = controlador.cancelar(TURNO_ID, sesion, attrs);

    verify(servicioTurno).cancelarPorVeterinaria(veterinaria, TURNO_ID);
    assertThat(mav.getViewName(), equalTo("redirect:/veterinaria/turnos"));
    assertThat(attrs.getFlashAttributes().get("mensaje"), equalTo("Cancelaste el turno."));
  }

  @Test
  public void cancelarTurnoInvalidoDeberiaMostrarElError() throws Exception {
    doThrow(new TurnoInvalido("No encontramos ese turno"))
      .when(servicioTurno)
      .cancelarPorVeterinaria(veterinaria, TURNO_ID);
    RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

    controlador.cancelar(TURNO_ID, sesion, attrs);

    assertThat(attrs.getFlashAttributes().get("error"), equalTo("No encontramos ese turno"));
  }

  @Test
  public void formularioReprogramarDeberiaCargarElTurnoYHorarios() throws Exception {
    Turno turno = new Turno();
    when(servicioTurno.buscarTurnoDeVeterinaria(veterinaria, TURNO_ID)).thenReturn(turno);
    when(servicioTurno.horariosDisponibles(veterinaria, LocalDate.now()))
      .thenReturn(List.of(LocalTime.NOON));

    ModelAndView mav = controlador.formularioReprogramar(
      TURNO_ID,
      LocalDate.now(),
      sesion,
      new RedirectAttributesModelMap()
    );

    assertThat(mav.getViewName(), equalTo("reprogramar-turno"));
    assertThat(mav.getModel().get("turno"), is(turno));
    assertThat(mav.getModel().get("esVeterinaria"), is(true));
    assertThat(mav.getModel().get("urlVolver"), equalTo("/veterinaria/turnos"));
  }

  @Test
  public void formularioReprogramarTurnoAjenoDeberiaVolverALista() throws Exception {
    doThrow(new TurnoInvalido("No encontramos ese turno"))
      .when(servicioTurno)
      .buscarTurnoDeVeterinaria(veterinaria, TURNO_ID);
    RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

    ModelAndView mav = controlador.formularioReprogramar(TURNO_ID, null, sesion, attrs);

    assertThat(mav.getViewName(), equalTo("redirect:/veterinaria/turnos"));
    assertThat(attrs.getFlashAttributes().get("error"), equalTo("No encontramos ese turno"));
  }

  @Test
  public void reprogramarDeberiaGuardarLaNuevaFechaYHora() throws Exception {
    RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();
    LocalDate fecha = LocalDate.of(2026, 11, 2);
    LocalTime hora = LocalTime.of(10, 0);

    ModelAndView mav = controlador.reprogramar(TURNO_ID, fecha, hora, sesion, attrs);

    verify(servicioTurno).reprogramarPorVeterinaria(veterinaria, TURNO_ID, fecha, hora);
    assertThat(mav.getViewName(), equalTo("redirect:/veterinaria/turnos"));
    assertThat(attrs.getFlashAttributes().get("mensaje"), equalTo("Reprogramaste el turno."));
  }

  @Test
  public void reprogramarTurnoInvalidoDeberiaVolverAlFormulario() throws Exception {
    doThrow(new TurnoInvalido("Horario ocupado"))
      .when(servicioTurno)
      .reprogramarPorVeterinaria(any(), anyLong(), any(), any());

    ModelAndView mav = controlador.reprogramar(
      TURNO_ID,
      LocalDate.of(2026, 11, 2),
      LocalTime.of(10, 0),
      sesion,
      new RedirectAttributesModelMap()
    );

    assertThat(
      mav.getViewName(),
      equalTo("redirect:/veterinaria/turnos/" + TURNO_ID + "/reprogramar")
    );
  }
}
