package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Especie;
import com.tallerwebi.dominio.Mascota;
import com.tallerwebi.dominio.Plan;
import com.tallerwebi.dominio.ServicioMascota;
import com.tallerwebi.dominio.ServicioPlan;
import com.tallerwebi.dominio.ServicioSocio;
import com.tallerwebi.dominio.Socio;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

public class ControladorMascotaTest {

  private ControladorMascota controladorMascota;
  private ServicioSocio servicioSocio;
  private ServicioPlan servicioPlan;
  private ServicioMascota servicioMascota;
  private HttpSession sesion;
  private Socio socio;
  private Mascota mascota;

  @BeforeEach
  public void init() {
    servicioSocio = mock(ServicioSocio.class);
    servicioPlan = mock(ServicioPlan.class);
    servicioMascota = mock(ServicioMascota.class);
    controladorMascota = new ControladorMascota(servicioSocio, servicioPlan, servicioMascota);
    sesion = mock(HttpSession.class);
    when(sesion.getAttribute(ControladorLogin.USUARIO_ID)).thenReturn(1L);
    socio = new Socio();
    socio.setUsuario(new Usuario());
    when(servicioSocio.buscarPorUsuario(1L)).thenReturn(socio);
    when(servicioPlan.listar()).thenReturn(List.of(new Plan()));
    mascota = new Mascota();
    mascota.setNombre("Luna");
    mascota.setEspecie(Especie.GATO);
  }

  @Test
  public void nuevaMascotaDeberiaMostrarFormularioConEspeciesYPlanes() {
    ModelAndView mav = controladorMascota.nuevaMascota(sesion);

    assertThat(mav.getViewName(), equalTo("nueva-mascota"));
    assertThat(mav.getModel().get("mascota"), instanceOf(Mascota.class));
    assertThat(mav.getModel().get("especies"), is(Especie.values()));
    assertThat(mav.getModel().get("planes"), is(servicioPlan.listar()));
  }

  @Test
  public void agregarMascotaDeberiaGuardarYVolverAlInicio() throws Exception {
    RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

    ModelAndView mav = controladorMascota.agregarMascota(mascota, 2L, sesion, redirectAttributes);

    verify(servicioMascota).agregarMascota(socio, mascota, 2L);
    assertThat(mav.getViewName(), equalTo("redirect:/home"));
    assertThat(
      redirectAttributes.getFlashAttributes().get("mensaje"),
      equalTo("Agregaste una mascota.")
    );
  }

  @Test
  public void agregarMascotaConErrorDeValidacionDeberiaConservarDatos() throws Exception {
    doThrow(new DatosDeRegistroInvalidos("Elegí un plan para tu mascota"))
      .when(servicioMascota)
      .agregarMascota(socio, mascota, 2L);

    ModelAndView mav = controladorMascota.agregarMascota(
      mascota,
      2L,
      sesion,
      new RedirectAttributesModelMap()
    );

    assertThat(mav.getViewName(), equalTo("nueva-mascota"));
    assertThat(mav.getModel().get("mascota"), is(mascota));
    assertThat(mav.getModel().get("planId"), is(2L));
    assertThat(mav.getModel().get("error"), equalTo("Elegí un plan para tu mascota"));
  }
}
