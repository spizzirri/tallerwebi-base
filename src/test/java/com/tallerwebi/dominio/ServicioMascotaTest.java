package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioMascotaTest {

  private RepositorioMascota repositorioMascotaMock;
  private RepositorioPlan repositorioPlanMock;
  private ServicioMascota servicioMascota;

  @BeforeEach
  public void init() {
    repositorioMascotaMock = mock(RepositorioMascota.class);
    repositorioPlanMock = mock(RepositorioPlan.class);
    servicioMascota = new ServicioMascotaImpl(repositorioMascotaMock, repositorioPlanMock);
  }

  @Test
  public void buscarPorIdDeberiaDevolverLaMascota() {
    Mascota mascotaEsperada = new Mascota();
    mascotaEsperada.setId(1L);
    mascotaEsperada.setNombre("Firulais");
    when(repositorioMascotaMock.buscarPorId(1L)).thenReturn(mascotaEsperada);
    Mascota mascotaObtenida = servicioMascota.buscarPorId(1L);
    assertThat(mascotaObtenida, equalTo(mascotaEsperada));
    verify(repositorioMascotaMock, times(1)).buscarPorId(1L);
  }

  @Test
  public void cambiarPlanDeberiaAsignarElPlan() {
    Socio socio = new Socio();
    Mascota mascota = new Mascota();
    mascota.setId(1L);
    socio.agregarMascota(mascota);
    Plan plan = new Plan();
    plan.setId(2L);
    when(repositorioMascotaMock.buscarPorId(1L)).thenReturn(mascota);
    when(repositorioPlanMock.buscarPorId(2L)).thenReturn(plan);

    servicioMascota.cambiarPlan(socio, 1L, 2L);

    assertThat(mascota.getPlan(), equalTo(plan));
  }

  @Test
  public void darDeBajaDeberiaQuitarElPlan() {
    Socio socio = new Socio();
    Mascota mascota = new Mascota();
    mascota.setId(1L);
    mascota.setPlan(new Plan());
    socio.agregarMascota(mascota);
    when(repositorioMascotaMock.buscarPorId(1L)).thenReturn(mascota);

    servicioMascota.darDeBaja(socio, 1L);

    assertThat(mascota.getPlan(), equalTo(null));
  }
}
