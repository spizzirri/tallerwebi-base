package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioMascotaTest {

  private ServicioMascotaImpl servicioMascota;
  private RepositorioMascota repositorioMascota;
  private RepositorioPlan repositorioPlan;
  private RepositorioSocio repositorioSocio;
  private Socio socio;
  private Mascota mascota;
  private Plan plan;

  @BeforeEach
  public void init() {
    repositorioMascota = mock(RepositorioMascota.class);
    repositorioPlan = mock(RepositorioPlan.class);
    repositorioSocio = mock(RepositorioSocio.class);
    servicioMascota =
      new ServicioMascotaImpl(repositorioMascota, repositorioPlan, repositorioSocio);

    Usuario usuario = new Usuario();
    usuario.setId(1L);
    socio = new Socio();
    socio.setUsuario(usuario);
    mascota = new Mascota();
    mascota.setNombre("Luna");
    mascota.setEspecie(Especie.GATO);
    plan = new Plan();
    plan.setId(2L);

    when(repositorioSocio.buscarPorUsuario(1L)).thenReturn(socio);
    when(repositorioPlan.buscarPorId(2L)).thenReturn(plan);
  }

  @Test
  public void agregarMascotaDeberiaAsociarlaAlSocioYAsignarleElPlan() throws Exception {
    mascota.setId(99L);

    servicioMascota.agregarMascota(socio, mascota, 2L);

    assertThat(mascota.getId(), is((Long) null));
    assertThat(socio.getMascotas().contains(mascota), is(true));
    assertThat(mascota.getSocio(), is(socio));
    assertThat(mascota.getPlan(), is(plan));
  }

  @Test
  public void agregarMascotaConDatosInvalidosDeberiaRechazarla() {
    mascota.setFechaNacimiento(LocalDate.now().plusDays(1));

    assertThrows(
      DatosDeRegistroInvalidos.class,
      () -> servicioMascota.agregarMascota(socio, mascota, 2L)
    );
    verifyNoInteractions(repositorioSocio);
  }

  @Test
  public void agregarMascotaSinPlanValidoDeberiaRechazarla() {
    when(repositorioPlan.buscarPorId(3L)).thenReturn(null);

    assertThrows(
      DatosDeRegistroInvalidos.class,
      () -> servicioMascota.agregarMascota(socio, mascota, 3L)
    );
    verifyNoInteractions(repositorioSocio);
  }
}
