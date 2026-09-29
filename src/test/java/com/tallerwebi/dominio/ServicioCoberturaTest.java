package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioCoberturaTest {

  private ServicioCobertura servicioCobertura;
  private Servicio consulta;
  private Servicio radiografia;
  private Mascota conPlanBasico;

  @BeforeEach
  public void init() {
    RepositorioServicio repositorioServicioMock = mock(RepositorioServicio.class);
    servicioCobertura = new ServicioCoberturaImpl(repositorioServicioMock);

    consulta = dadoUnServicio(1L, "Consulta clínica", 1);
    radiografia = dadoUnServicio(2L, "Radiografía", 2);
    when(repositorioServicioMock.listar()).thenReturn(List.of(consulta, radiografia));

    conPlanBasico = new Mascota();
    conPlanBasico.setPlan(dadoUnPlan(1));
  }

  @Test
  public void conPlanBasicoDeberiaVerTodosLosServiciosYSoloLosBasicosIncluidos() {
    List<ItemCobertura> items = servicioCobertura.coberturaDe(conPlanBasico, FiltroCobertura.TODOS);

    assertThat(items, hasSize(2));
    assertThat(items.get(0).isIncluido(), is(true));
    assertThat(items.get(1).isIncluido(), is(false));
  }

  @Test
  public void conPlanPremiumDeberiaTenerTodoIncluido() {
    Mascota conPlanPremium = new Mascota();
    conPlanPremium.setPlan(dadoUnPlan(2));

    List<ItemCobertura> items = servicioCobertura.coberturaDe(
      conPlanPremium,
      FiltroCobertura.INCLUIDOS
    );

    assertThat(items, hasSize(2));
  }

  @Test
  public void filtrarSoloIncluidosDeberiaDevolverLosCubiertos() {
    assertThat(
      servicios(servicioCobertura.coberturaDe(conPlanBasico, FiltroCobertura.INCLUIDOS)),
      contains(consulta)
    );
  }

  @Test
  public void filtrarSoloNoIncluidosDeberiaDevolverLosNoCubiertos() {
    assertThat(
      servicios(servicioCobertura.coberturaDe(conPlanBasico, FiltroCobertura.NO_INCLUIDOS)),
      contains(radiografia)
    );
  }

  @Test
  public void sinMascotaNingunServicioEstaIncluido() {
    assertThat(servicioCobertura.coberturaDe(null, FiltroCobertura.INCLUIDOS), hasSize(0));
  }

  @Test
  public void coberturaEnUnaVeterinariaDeberiaMostrarSoloSusServicios() {
    Veterinaria veterinaria = new Veterinaria();
    veterinaria.setHoraApertura(LocalTime.of(9, 0));
    veterinaria.setHoraCierre(LocalTime.of(10, 0));
    veterinaria.getServicios().add(radiografia);

    List<ItemCobertura> items = servicioCobertura.coberturaEn(veterinaria, conPlanBasico);

    assertThat(servicios(items), contains(radiografia));
    assertThat(items.get(0).isIncluido(), is(false));
  }

  private List<Servicio> servicios(List<ItemCobertura> items) {
    return items.stream().map(ItemCobertura::getServicio).collect(Collectors.toList());
  }

  private Servicio dadoUnServicio(Long id, String nombre, int nivel) {
    Servicio servicio = new Servicio();
    servicio.setId(id);
    servicio.setNombre(nombre);
    servicio.setNivelRequerido(nivel);
    return servicio;
  }

  private Plan dadoUnPlan(int nivel) {
    Plan plan = new Plan();
    plan.setNivel(nivel);
    return plan;
  }
}
