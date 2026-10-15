package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Servicio;
import com.tallerwebi.dominio.ServicioCobertura;
import com.tallerwebi.dominio.ServicioVeterinaria;
import com.tallerwebi.dominio.Veterinaria;
import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.servlet.http.HttpSession;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorRegistroVeterinariaTest {

  private ServicioVeterinaria servicioVeterinaria;
  private ServicioCobertura servicioCobertura;
  private ControladorRegistroVeterinaria controlador;
  private DatosVeterinaria datos;
  private HttpSession sesion;

  @BeforeEach
  public void init() {
    servicioVeterinaria = mock(ServicioVeterinaria.class);
    servicioCobertura = mock(ServicioCobertura.class);
    controlador = new ControladorRegistroVeterinaria(servicioVeterinaria, servicioCobertura);
    when(servicioCobertura.listarServicios()).thenReturn(List.of(new Servicio()));
    datos = datosValidos();
    sesion = mock(HttpSession.class);
  }

  @Test
  public void formularioRegistroDeberiaIncluirDiasYServicios() {
    ModelAndView mav = controlador.formularioRegistro();

    assertThat(mav.getViewName(), equalTo("registro-veterinaria"));
    assertThat(mav.getModel().get("datosVeterinaria"), instanceOf(DatosVeterinaria.class));
    assertThat(mav.getModel().get("diasSemana"), is(DayOfWeek.values()));
    assertThat(mav.getModel().get("servicios"), is(servicioCobertura.listarServicios()));
    assertThat(mav.getModel().get("mostrarPassword"), is(true));
  }

  @Test
  public void registrarDeberiaCrearSesionYRedirigirAlInicio() throws Exception {
    doAnswer(invocacion -> {
        Veterinaria veterinaria = invocacion.getArgument(0);
        veterinaria.getUsuario().setId(8L);
        veterinaria.getUsuario().setRol(com.tallerwebi.dominio.Usuario.ROL_VETERINARIA);
        return null;
      })
      .when(servicioVeterinaria)
      .registrar(any(Veterinaria.class), anyList());

    ModelAndView mav = controlador.registrar(datos, sesion);

    assertThat(mav.getViewName(), equalTo("redirect:/veterinaria/inicio"));
    verify(servicioVeterinaria).registrar(any(Veterinaria.class), eq(List.of(1L)));
    verify(sesion).setAttribute(ControladorLogin.USUARIO_ID, 8L);
    verify(sesion).setAttribute(ControladorLogin.EMAIL, "vet@test.com");
    verify(sesion)
      .setAttribute(ControladorLogin.ROL, com.tallerwebi.dominio.Usuario.ROL_VETERINARIA);
  }

  @Test
  public void registrarConEmailExistenteDeberiaVolverConError() throws Exception {
    doThrow(UsuarioExistente.class)
      .when(servicioVeterinaria)
      .registrar(any(Veterinaria.class), anyList());

    ModelAndView mav = controlador.registrar(datos, sesion);

    assertThat(mav.getViewName(), equalTo("registro-veterinaria"));
    assertThat(mav.getModel().get("error"), equalTo("Ya existe una cuenta con ese email"));
    assertThat(mav.getModel().get("datosVeterinaria"), is(datos));
  }

  @Test
  public void registrarConDatosInvalidosDeberiaVolverConElMensajeDelServicio() throws Exception {
    doThrow(new DatosDeRegistroInvalidos("Elegí al menos un día de atención"))
      .when(servicioVeterinaria)
      .registrar(any(Veterinaria.class), anyList());

    ModelAndView mav = controlador.registrar(datos, sesion);

    assertThat(mav.getViewName(), equalTo("registro-veterinaria"));
    assertThat(mav.getModel().get("error"), equalTo("Elegí al menos un día de atención"));
  }

  private DatosVeterinaria datosValidos() {
    DatosVeterinaria datos = new DatosVeterinaria();
    datos.setNombre("Veterinaria Central");
    datos.setCuit("30712345671");
    datos.setDireccion("Calle 123");
    datos.setTelefono("4444-1111");
    datos.setEmail("vet@test.com");
    datos.setPassword("secreto");
    datos.setHoraApertura(LocalTime.of(9, 0));
    datos.setHoraCierre(LocalTime.of(18, 0));
    datos.setDiasAtencion(Set.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
    datos.setServiciosIds(List.of(1L));
    return datos;
  }
}
