package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Hijos.Curso;
import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Hijos.ServicioHijo;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.AliasExistenteException;
import com.tallerwebi.dominio.excepcion.AliasVacioException;
import com.tallerwebi.dominio.excepcion.HijoExistenteException;
import com.tallerwebi.dominio.excepcion.HijoNoEncontradoException;
import com.tallerwebi.presentacion.HijosControlador;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class HijosControladorTest {

  private Usuario usuarioMock;
  private HijosControlador hijosControlador;
  private ServicioHijo servicioHijoMock;
  private HttpSession sessionMock;
  private DatosEditarHijoDTO datosHijoMock;
  private BindingResult bindingResultMock;
  private RedirectAttributes redirectAttributesMock;
  private MultipartFile fotoMock;

  @BeforeEach
  public void init() {
    servicioHijoMock = mock(ServicioHijo.class);
    hijosControlador = new HijosControlador(servicioHijoMock);
    sessionMock = mock(HttpSession.class);
    usuarioMock = mock(Usuario.class);
    datosHijoMock = mock(DatosEditarHijoDTO.class);
    bindingResultMock = mock(BindingResult.class);
    redirectAttributesMock = mock(RedirectAttributes.class);
    fotoMock = mock(MultipartFile.class);
  }

  @Test
  public void vistaHijosSinSesionDeberiaRedirigirAlLogin() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(null);
    ModelAndView mv = hijosControlador.irAvistaHijos(sessionMock);
    assertThat(mv.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void misHijosDebeMostrarLosHijosDelUsuario() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    when(usuarioMock.getId()).thenReturn(1L);
    //simulo un hijo
    List<Hijo> hijoMock = List.of(Mockito.mock(Hijo.class));
    when(servicioHijoMock.obtenerHijosPorUsuario(1L)).thenReturn(hijoMock);

    ModelAndView modelAndView = hijosControlador.irAvistaHijos(sessionMock);

    List<Hijo> hijosObtenidos = (List<Hijo>) modelAndView.getModel().get("hijos");

    assertThat(hijosObtenidos.size(), equalTo(1));
  }

  @Test
  public void siNoTieneHijosDebeMostrarUnMensaje() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    when(usuarioMock.getHijos()).thenReturn(null); //cuando llamo a getHijos le pido que retorne null

    ModelAndView modelAndView = hijosControlador.irAvistaHijos(sessionMock);

    assertThat(
      (String) modelAndView.getModel().get("mensajeError"),
      equalToIgnoringCase("Aún no tenés hijos registrados")
    );
  }

  @Test
  public void vistaHijosDebeMostrarLaInfoDeLosHijos() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    Hijo hijoMock1 = Mockito.mock(Hijo.class);
    when(hijoMock1.getNombre()).thenReturn("Santiago");

    Hijo hijoMock2 = Mockito.mock(Hijo.class);
    when(hijoMock2.getNombre()).thenReturn("Romina");

    List<Hijo> hijosSimulados = List.of(hijoMock1, hijoMock2);

    when(servicioHijoMock.obtenerHijosPorUsuario(usuarioMock.getId())).thenReturn(hijosSimulados);

    ModelAndView modelAndView = hijosControlador.irAvistaHijos(sessionMock);

    List<Hijo> hijosObtenidos = (List<Hijo>) modelAndView.getModel().get("hijos");

    assertThat(hijosObtenidos.get(0).getNombre(), equalToIgnoringCase("Santiago"));
    assertThat(hijosObtenidos.get(1).getNombre(), equalToIgnoringCase("Romina"));
  }

  @Test
  public void guardarHijoDeberiaLlamarAlServicioYRecargarVistaHijos() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    Hijo hijoMock = mock(Hijo.class);
    ModelAndView modelAndView = hijosControlador.guardarHijos(
      hijoMock,
      "CUARTO",
      "D",
      fotoMock,
      sessionMock,
      redirectAttributesMock
    );
    verify(servicioHijoMock, times(1)).guardarHijo(hijoMock, fotoMock, usuarioMock);
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/vistaHijos"));
  }

  @Test
  public void guardarHijosSinSesionDeberiaRedirigirAlLogin() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(null);
    Hijo hijoMock = mock(Hijo.class);

    ModelAndView mv = hijosControlador.guardarHijos(
      hijoMock,
      "CUARTO",
      "D",
      fotoMock,
      sessionMock,
      redirectAttributesMock
    );
    assertThat(mv.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void guardarHijoQueYaExisteDeberiaRedirigirAVistaHijosConError() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    Hijo hijoMock = mock(Hijo.class);

    doThrow(HijoExistenteException.class)
      .when(servicioHijoMock)
      .guardarHijo(hijoMock, fotoMock, usuarioMock);

    ModelAndView modelAndView = hijosControlador.guardarHijos(
      hijoMock,
      "CUARTO",
      "D",
      fotoMock,
      sessionMock,
      redirectAttributesMock
    );
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("vistaHijos"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("El hijo ya se encuentra registrado")
    );
  }

  @Test
  public void guardarHijoDeberiaGuardarElCursoCorrectamente() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    Hijo hijoMock = mock(Hijo.class);
    when(hijoMock.getCurso()).thenReturn(Curso.CUARTO_D);

    ModelAndView mv = hijosControlador.guardarHijos(
      hijoMock,
      "CUARTO",
      "D",
      fotoMock,
      sessionMock,
      redirectAttributesMock
    );
    verify(servicioHijoMock, times(1)).guardarHijo(hijoMock, fotoMock, usuarioMock);
    assertThat(mv.getViewName(), equalToIgnoringCase("redirect:/vistaHijos"));
  }

  @Test
  public void guardarHijoDeberiaSetearElCursoAntesDeGuardarlo() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    Hijo hijoReal = new Hijo();
    hijosControlador.guardarHijos(
      hijoReal,
      "TERCERO",
      "D",
      fotoMock,
      sessionMock,
      redirectAttributesMock
    );
    assertThat(hijoReal.getCurso(), equalTo(Curso.TERCERO_D));
  }

  @Test
  public void guardarHijoConCursoInvalidoDeberiaRetornarVistaConError() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getId()).thenReturn(1L);

    // Simulamos que al pedir los hijos para reconstruir la vista, retorne una lista vacía o con datos
    when(servicioHijoMock.obtenerHijosPorUsuario(1L)).thenReturn(List.of());

    // Ejecutamos pasando "ANIO_TRUCHO" para forzar el IllegalArgumentException en Curso.valueOf()
    ModelAndView modelAndView = hijosControlador.guardarHijos(
      mock(Hijo.class),
      "ANIO_TRUCHO",
      "X",
      fotoMock,
      sessionMock,
      redirectAttributesMock
    );
    // Verificaciones de lo que hace 'devolverVistaConError'
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("vistaHijos"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("El año o división seleccionados no son válidos")
    );
    // Verificamos que se volvió a cargar el comando "hijo" vacío para el modal
    //devolverVistaConError esté cumpliendo con su trabajo de
    // dejar un objeto de tipo Hijo guardado bajo la clave "hijo"
    assertThat(modelAndView.getModel().get("hijo"), instanceOf(Hijo.class));
  }

  @Test
  public void editarHijoDeberiaLlamarAlServicioYRecargarLaVistaHijos() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(bindingResultMock.hasErrors()).thenReturn(false); // Simulamos validación exitosa

    when(datosHijoMock.getIdHijo()).thenReturn(1L);
    when(datosHijoMock.getAnio()).thenReturn("CUARTO");
    when(datosHijoMock.getDivision()).thenReturn("D");
    MultipartFile fotoMock = mock(MultipartFile.class);
    when(datosHijoMock.getFotoPerfilH()).thenReturn(fotoMock);

    ModelAndView mav = hijosControlador.editarHijo(
      datosHijoMock,
      bindingResultMock,
      sessionMock,
      redirectAttributesMock
    );
    verify(servicioHijoMock, times(1))
      .editarHijo(eq(1L), any(Hijo.class), eq(fotoMock), eq(usuarioMock));

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/vistaHijos"));
  }

  @Test
  public void editarHijoQueNoExisteDeberiaRedirigirAVistaHijosConError() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(bindingResultMock.hasErrors()).thenReturn(false); // Simulamos validación exitosa

    when(datosHijoMock.getIdHijo()).thenReturn(1L);
    when(datosHijoMock.getAnio()).thenReturn("CUARTO");
    when(datosHijoMock.getDivision()).thenReturn("D");
    MultipartFile fotoMock = mock(MultipartFile.class);
    when(datosHijoMock.getFotoPerfilH()).thenReturn(fotoMock);

    doThrow(HijoNoEncontradoException.class)
      .when(servicioHijoMock)
      .editarHijo(eq(1L), any(Hijo.class), eq(fotoMock), eq(usuarioMock));

    ModelAndView mav = hijosControlador.editarHijo(
      datosHijoMock,
      bindingResultMock,
      sessionMock,
      redirectAttributesMock
    );
    assertThat(mav.getViewName(), equalToIgnoringCase("vistaHijos"));
    assertThat(
      mav.getModel().get("error").toString(),
      equalToIgnoringCase("El hijo no existe o no pertenece al usuario")
    );
  }

  @Test
  public void editarHijoConCamposInvalidosDeberiaRetornarVistaConError() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getId()).thenReturn(1L);

    // 1. FORZAMOS el error de validación del DTO
    when(bindingResultMock.hasErrors()).thenReturn(true);

    // Simulamos la recarga de la lista de hijos del método auxiliar
    when(servicioHijoMock.obtenerHijosPorUsuario(1L)).thenReturn(List.of());

    ModelAndView modelAndView = hijosControlador.editarHijo(
      datosHijoMock,
      bindingResultMock,
      sessionMock,
      redirectAttributesMock
    );

    // Verificaciones del retorno de 'devolverVistaConError'
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("vistaHijos"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Hay campos inválidos en el formulario de edición.")
    );
    // Verificamos que no intentó llamar al servicio de edición porque frenó antes
    verify(servicioHijoMock, never()).editarHijo(anyLong(), any(Hijo.class), any(), any());
  }

  @Test
  public void seDebePoderCambiarLaFotoDelHijo() {
    // 1. Preparación del entorno (Sesión y Mock del archivo de imagen)
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(bindingResultMock.hasErrors()).thenReturn(false); // Simulamos validación exitosa
    MultipartFile fotoMock = Mockito.mock(MultipartFile.class);
    when(fotoMock.isEmpty()).thenReturn(false); // Simulamos que el usuario SÍ subió un archivo

    // 2. Configuración de los comportamientos del DTO (Stubs con 'when')
    when(datosHijoMock.getIdHijo()).thenReturn(1L);
    when(datosHijoMock.getAnio()).thenReturn("CUARTO");
    when(datosHijoMock.getDivision()).thenReturn("D");
    when(datosHijoMock.getFotoPerfilH()).thenReturn(fotoMock);

    // 3. Ejecución del método del controlador
    // Pasamos el bindingResultMock inmediatamente después del DTO para cumplir la firma
    ModelAndView mav = hijosControlador.editarHijo(
      datosHijoMock,
      bindingResultMock,
      sessionMock,
      redirectAttributesMock
    );
    // 4. Verificaciones (Asserts y Verifies)
    // Verificamos que el servicio recibió exactamente el mock de la foto para procesarlo
    verify(servicioHijoMock, times(1))
      .editarHijo(eq(1L), any(Hijo.class), eq(fotoMock), eq(usuarioMock));

    // Verificamos que al salir todo bien, nos redirija correctamente
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/vistaHijos"));
  }

  @Test
  public void seDebePoderEliminarHijo() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(datosHijoMock.getIdHijo()).thenReturn(1L);

    ModelAndView mav = hijosControlador.darDeBajaUnHijo(
      datosHijoMock.getIdHijo(),
      sessionMock,
      redirectAttributesMock
    );

    verify(servicioHijoMock, times(1)).eliminarHijo(datosHijoMock.getIdHijo(), usuarioMock);
    verify(redirectAttributesMock, times(1))
      .addFlashAttribute("exito", "El hijo fue dado de baja correctamente.");
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/vistaHijos"));
  }

  @Test
  public void eliminarHijoInexistenteDebeLanzarExcepcion() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    doThrow(HijoNoEncontradoException.class).when(servicioHijoMock).eliminarHijo(1L, usuarioMock);

    ModelAndView mv = hijosControlador.darDeBajaUnHijo(1L, sessionMock, redirectAttributesMock);
    // Verificamos que se capturó la excepción y se asignó el mensaje flash de error
    verify(redirectAttributesMock, times(1))
      .addFlashAttribute("error", "No se pudo eliminar: el hijo no existe o no te pertenece.");
    assertThat(mv.getViewName(), equalToIgnoringCase("redirect:/vistaHijos"));
  }

  @Test
  public void irACredencialesDebeMostrarVistaCredencialesConInfoDeLosHijosDelUsuario() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getId()).thenReturn(1L); // ← FIJAMOS el ID del usuario primero
    Hijo hijoMock1 = Mockito.mock(Hijo.class);
    when(hijoMock1.getNombre()).thenReturn("Santiago");

    Hijo hijoMock2 = Mockito.mock(Hijo.class);
    when(hijoMock2.getNombre()).thenReturn("Romina");

    List<Hijo> hijosSimulados = List.of(hijoMock1, hijoMock2);

    when(servicioHijoMock.obtenerHijosPorUsuario(1L)).thenReturn(hijosSimulados);

    ModelAndView mav = hijosControlador.irACredenciales(sessionMock);
    assertThat(mav.getViewName(), equalToIgnoringCase("credenciales"));

    List<Hijo> hijosObtenidos = (List<Hijo>) mav.getModel().get("hijos");

    assertThat(hijosObtenidos.get(0).getNombre(), equalToIgnoringCase("Santiago"));
    assertThat(hijosObtenidos.get(1).getNombre(), equalToIgnoringCase("Romina"));
    assertThat(mav.getModel().get("usuario"), equalTo(usuarioMock));
  }

  @Test
  public void cuandoSeEditaUnHijoConAliasVacioDebeDevolverVistaConError() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(bindingResultMock.hasErrors()).thenReturn(false);

    // IMPORTANTE: usar valores válidos para que no falle antes en Curso.valueOf(...)
    when(datosHijoMock.getAnio()).thenReturn("PRIMERO");
    when(datosHijoMock.getDivision()).thenReturn("A");
    when(datosHijoMock.getFotoPerfilH()).thenReturn(fotoMock);

    doThrow(new AliasVacioException("El alias no puede estar vacío"))
      .when(servicioHijoMock)
      .editarHijo(anyLong(), any(Hijo.class), any(MultipartFile.class), eq(usuarioMock));

    ModelAndView mav = hijosControlador.editarHijo(
      datosHijoMock,
      bindingResultMock,
      sessionMock,
      redirectAttributesMock
    );

    verify(servicioHijoMock)
      .editarHijo(anyLong(), any(Hijo.class), any(MultipartFile.class), eq(usuarioMock));

    assertThat(mav.getViewName(), not(equalToIgnoringCase("redirect:/vistaHijos")));
  }

  @Test
  public void cuandoGuardoUnAliasValidoEntoncesRedirigeAVistaHijos() {
    Usuario usuario = new Usuario();

    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuario);

    ModelAndView mv = hijosControlador.guardarAlias(1L, "ROJO.GATO.TREN", sessionMock);

    verify(servicioHijoMock).actualizarAlias(1L, "ROJO.GATO.TREN", usuario);

    assertEquals("redirect:/vistaHijos", mv.getViewName());
  }

  @Test
  public void cuandoElAliasYaExisteEntoncesDevuelveVistaConError() {
    Usuario usuario = new Usuario();
    usuario.setId(1L);

    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuario);

    doThrow(new AliasExistenteException("Alias existente"))
      .when(servicioHijoMock)
      .actualizarAlias(1L, "ROJO.GATO.TREN", usuario);

    ModelAndView mv = hijosControlador.guardarAlias(1L, "ROJO.GATO.TREN", sessionMock);

    assertEquals("vistaHijos", mv.getViewName());

    assertEquals("Ese alias ya está en uso.", mv.getModel().get("error"));
  }
}
