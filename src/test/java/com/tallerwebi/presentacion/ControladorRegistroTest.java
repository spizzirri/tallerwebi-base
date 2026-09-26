package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Especie;
import com.tallerwebi.dominio.Mascota;
import com.tallerwebi.dominio.ServicioRegistro;
import com.tallerwebi.dominio.Socio;
import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;
import com.tallerwebi.dominio.excepcion.SocioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorRegistroTest {

  private ControladorRegistro controladorRegistro;
  private ServicioRegistro servicioRegistroMock;
  private DatosRegistro datosRegistro;

  @BeforeEach
  public void init() {
    servicioRegistroMock = mock(ServicioRegistro.class);
    controladorRegistro = new ControladorRegistro(servicioRegistroMock);

    datosRegistro = new DatosRegistro();
    datosRegistro.setNombre("Juan");
    datosRegistro.setApellido("Pérez");
    datosRegistro.setDni("30123456");
    datosRegistro.setEmail("juan@test.com");
    datosRegistro.setPassword("123456");
    datosRegistro.getMascota().setNombre("Firulais");
    datosRegistro.getMascota().setEspecie(Especie.PERRO);
  }

  @Test
  public void irARegistroDeberiaMostrarElFormularioVacioConLasEspecies() {
    ModelAndView modelAndView = controladorRegistro.irARegistro();

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("registro"));
    assertThat(modelAndView.getModel().get("datosRegistro"), instanceOf(DatosRegistro.class));
    assertThat(modelAndView.getModel().get("especies"), is(Especie.values()));
  }

  @Test
  public void registrarmeConDatosValidosDeberiaRegistrarAlSocioYRedirigirAlLogin()
    throws Exception {
    ModelAndView modelAndView = controladorRegistro.registrarme(datosRegistro);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
    verify(servicioRegistroMock, times(1)).registrarSocio(any(Socio.class), any(Mascota.class));
  }

  @Test
  public void registrarmeConEmailExistenteDeberiaVolverAlFormularioConError() throws Exception {
    doThrow(UsuarioExistente.class).when(servicioRegistroMock).registrarSocio(any(), any());

    ModelAndView modelAndView = controladorRegistro.registrarme(datosRegistro);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("registro"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Ya existe una cuenta con ese email")
    );
  }

  @Test
  public void registrarmeConDniExistenteDeberiaVolverAlFormularioConError() throws Exception {
    doThrow(SocioExistente.class).when(servicioRegistroMock).registrarSocio(any(), any());

    ModelAndView modelAndView = controladorRegistro.registrarme(datosRegistro);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("registro"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Ya existe un socio con ese DNI")
    );
  }

  @Test
  public void registrarmeConDatosInvalidosDeberiaMostrarElMensajeYConservarLosDatos()
    throws Exception {
    doThrow(new DatosDeRegistroInvalidos("Tenés que registrar al menos una mascota"))
      .when(servicioRegistroMock)
      .registrarSocio(any(), any());

    ModelAndView modelAndView = controladorRegistro.registrarme(datosRegistro);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("registro"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalTo("Tenés que registrar al menos una mascota")
    );
    assertThat(modelAndView.getModel().get("datosRegistro"), is(datosRegistro));
  }

  @Test
  public void datosRegistroDeberiaArmarElSocioConSuUsuarioYLaMascota() {
    Socio socio = datosRegistro.crearSocio();
    Mascota mascota = datosRegistro.crearMascota();

    assertThat(socio.getDni(), equalTo("30123456"));
    assertThat(socio.getUsuario().getEmail(), equalTo("juan@test.com"));
    assertThat(mascota.getNombre(), equalTo("Firulais"));
    assertThat(mascota.getEspecie(), is(Especie.PERRO));
  }
}
