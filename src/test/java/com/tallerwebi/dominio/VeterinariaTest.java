package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;

public class VeterinariaTest {

  @Test
  public void atiendeSoloLosDiasConfigurados() {
    Veterinaria veterinaria = new Veterinaria();
    veterinaria.setDiasAtencion(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.SATURDAY));

    assertThat(veterinaria.atiendeEl(LocalDate.of(2026, 10, 12)), is(true));
    assertThat(veterinaria.atiendeEl(LocalDate.of(2026, 10, 10)), is(true));
    assertThat(veterinaria.atiendeEl(LocalDate.of(2026, 10, 11)), is(false));
    assertThat(veterinaria.atiendeEl(null), is(false));
  }

  @Test
  public void veterinariaSinDiasGuardadosConservaElHorarioHistoricoDeLunesAViernes() {
    Veterinaria veterinaria = new Veterinaria();
    veterinaria.setDiasAtencion(EnumSet.noneOf(DayOfWeek.class));

    assertThat(veterinaria.atiendeEl(LocalDate.of(2026, 10, 12)), is(true));
    assertThat(veterinaria.atiendeEl(LocalDate.of(2026, 10, 10)), is(false));
    assertThat(
      veterinaria.getDiasAtencionTexto(),
      equalTo("Lunes, Martes, Miércoles, Jueves, Viernes")
    );
  }

  @Test
  public void horariosDeAtencionDevuelveBloquesCompletosDeMediaHora() {
    Veterinaria veterinaria = new Veterinaria();
    veterinaria.setHoraApertura(LocalTime.of(9, 0));
    veterinaria.setHoraCierre(LocalTime.of(10, 15));

    assertThat(veterinaria.horariosDeAtencion(), contains(LocalTime.of(9, 0), LocalTime.of(9, 30)));
  }

  @Test
  public void horariosDeAtencionVacioCuandoElHorarioEsInvalido() {
    Veterinaria veterinaria = new Veterinaria();
    veterinaria.setHoraApertura(LocalTime.of(18, 0));
    veterinaria.setHoraCierre(LocalTime.of(9, 0));

    assertThat(veterinaria.horariosDeAtencion().isEmpty(), is(true));
  }
}
