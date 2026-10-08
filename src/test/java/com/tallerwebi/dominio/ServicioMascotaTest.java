package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioMascotaTest {

  private ServicioMascota servicioMascota;
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
  public void buscarPorIdDeberiaDevolverLaMascota() {
    Mascota mascotaEsperada = new Mascota();
    mascotaEsperada.setId(1L);
    mascotaEsperada.setNombre("Firulais");
    when(repositorioMascota.buscarPorId(1L)).thenReturn(mascotaEsperada);
    Mascota mascotaObtenida = servicioMascota.buscarPorId(1L);
    assertThat(mascotaObtenida, equalTo(mascotaEsperada));
    verify(repositorioMascota, times(1)).buscarPorId(1L);
  }

  @Test
  public void cambiarPlanDeberiaAsignarElPlan() {
    Socio socioLocal = new Socio();
    Mascota mascotaLocal = new Mascota();
    mascotaLocal.setId(1L);
    socioLocal.agregarMascota(mascotaLocal);
    Plan planLocal = new Plan();
    planLocal.setId(2L);
    when(repositorioMascota.buscarPorId(1L)).thenReturn(mascotaLocal);
    when(repositorioPlan.buscarPorId(2L)).thenReturn(planLocal);

    servicioMascota.cambiarPlan(socioLocal, 1L, 2L);

    assertThat(mascotaLocal.getPlan(), equalTo(planLocal));
  }

  @Test
  public void darDeBajaDeberiaQuitarElPlan() {
    Socio socioLocal = new Socio();
    Mascota mascotaLocal = new Mascota();
    mascotaLocal.setId(1L);
    mascotaLocal.setPlan(new Plan());
    socioLocal.agregarMascota(mascotaLocal);
    when(repositorioMascota.buscarPorId(1L)).thenReturn(mascotaLocal);

    servicioMascota.darDeBaja(socioLocal, 1L);

    assertThat(mascotaLocal.getPlan(), equalTo(null));
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
