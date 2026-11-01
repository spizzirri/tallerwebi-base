package com.valhalla.domain.plan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.valhalla.domain.place.PlaceCategory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * [SCH] Tests del planificador de itinerario. Fijan las reglas de borde del slice 1 (pin de hora,
 * pin de dia, ventana, desborde y traslado); los slices siguientes solo conectan esto con el form y
 * la persistencia.
 *
 * <p>Las paradas con coordenadas iguales ejercitan el piso del traslado (5 minutos), las muy
 * alejadas el techo (90) y las del ecuador el calculo por modo, sin depender de una ciudad concreta.
 */
public class PlanScheduleCalculatorTest {

  private static final LocalDate PLAN_DATE = LocalDate.of(2026, 5, 1);
  private static final LocalTime DAY_START = LocalTime.of(9, 0);
  private static final LocalTime DAY_END = LocalTime.of(18, 0);
  private static final Double LATITUDE = -34.6;
  private static final Double LONGITUDE = -58.4;

  @Test
  public void U01_tresParadasSeEncadenanConDuracionYTraslado() {
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(
        stop(null, null, PlaceCategory.RESTAURANT, LATITUDE, LONGITUDE, null),
        stop(null, null, PlaceCategory.CAFE, LATITUDE, LONGITUDE, null),
        stop(null, null, PlaceCategory.PARK, LATITUDE, LONGITUDE, null)
      )
    );

    assertEquals(3, slots.size());
    assertSlot(slots.get(0), PLAN_DATE, "09:00", "10:30");
    assertSlot(slots.get(1), PLAN_DATE, "10:35", "11:20");
    assertSlot(slots.get(2), PLAN_DATE, "11:25", "12:25");
  }

  @Test
  public void U02_laDuracionDelLugarGanaSobreElDefaultDeCategoria() {
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(stop(null, 120, PlaceCategory.RESTAURANT, null, null, null))
    );

    assertSlot(slots.get(0), PLAN_DATE, "09:00", "11:00");
  }

  @Test
  public void U03_elOverrideDeLaVisitaGanaSobreLaDuracionDelLugar() {
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(stop(30, 120, PlaceCategory.RESTAURANT, null, null, null))
    );

    assertSlot(slots.get(0), PLAN_DATE, "09:00", "09:30");
  }

  @Test
  public void U04_unPinDeHoraRecalculaSoloLasParadasSiguientes() {
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(
        stop(60, null, PlaceCategory.OTHER, null, null, null),
        pinned(null, LocalTime.of(14, 0), 60),
        stop(60, null, PlaceCategory.OTHER, null, null, null)
      )
    );

    assertSlot(slots.get(0), PLAN_DATE, "09:00", "10:00");
    assertSlot(slots.get(1), PLAN_DATE, "14:00", "15:00");
    assertSlot(slots.get(2), PLAN_DATE, "15:15", "16:15");
  }

  @Test
  public void U05_unPinDeDiaSinHoraReiniciaEseDiaEnLaHoraDeInicio() {
    LocalDate secondDay = PLAN_DATE.plusDays(1);
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(stop(60, null, PlaceCategory.OTHER, null, null, null), pinned(secondDay, null, 60))
    );

    assertSlot(slots.get(0), PLAN_DATE, "09:00", "10:00");
    assertSlot(slots.get(1), secondDay, "09:00", "10:00");
  }

  @Test
  public void U06_unaParadaQueNoEntraEnLaVentanaRuedaAlDiaSiguiente() {
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      LocalTime.of(10, 0),
      TransportMode.WALK,
      List.of(
        stop(60, null, PlaceCategory.OTHER, null, null, null),
        stop(60, null, PlaceCategory.OTHER, null, null, null)
      )
    );

    assertSlot(slots.get(0), PLAN_DATE, "09:00", "10:00");
    assertSlot(slots.get(1), PLAN_DATE.plusDays(1), "09:00", "10:00");
  }

  @Test
  public void U07_unaParadaDeDuracionMayorQueLaVentanaDesbordaSinIterar() {
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      LocalTime.of(10, 0),
      TransportMode.WALK,
      List.of(stop(180, null, PlaceCategory.OTHER, null, null, null))
    );

    assertSlot(slots.get(0), PLAN_DATE.plusDays(1), "09:00", "12:00");
  }

  @Test
  public void U08_elGapSeCalculaPorModoYQuedaEntreCincoYNoventaMinutos() {
    List<PlanScheduleCalculator.Slot> samePlace = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(
        stop(60, null, PlaceCategory.OTHER, LATITUDE, LONGITUDE, null),
        stop(60, null, PlaceCategory.OTHER, LATITUDE, LONGITUDE, null)
      )
    );
    assertEquals(LocalTime.of(10, 5), samePlace.get(1).start());

    List<PlanScheduleCalculator.Slot> farApart = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(
        stop(60, null, PlaceCategory.OTHER, LATITUDE, LONGITUDE, null),
        stop(60, null, PlaceCategory.OTHER, 40.4, -3.7, null)
      )
    );
    assertEquals(LocalTime.of(11, 30), farApart.get(1).start());

    List<PlanScheduleCalculator.Slot> walking = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(
        stop(60, null, PlaceCategory.OTHER, 0.0, 0.0, TransportMode.WALK),
        stop(60, null, PlaceCategory.OTHER, 0.027, 0.0, null)
      )
    );
    List<PlanScheduleCalculator.Slot> driving = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(
        stop(60, null, PlaceCategory.OTHER, 0.0, 0.0, TransportMode.CAR),
        stop(60, null, PlaceCategory.OTHER, 0.027, 0.0, null)
      )
    );
    assertEquals(LocalTime.of(10, 50), walking.get(1).start());
    assertEquals(LocalTime.of(10, 10), driving.get(1).start());
  }

  @Test
  public void U09_sinCoordenadasSeUsaElGapDeFallbackDeQuinceMinutos() {
    List<PlanScheduleCalculator.Slot> withoutCoordinates = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(
        stop(60, null, PlaceCategory.OTHER, null, null, null),
        stop(60, null, PlaceCategory.OTHER, null, null, null)
      )
    );
    assertEquals(LocalTime.of(10, 15), withoutCoordinates.get(1).start());

    List<PlanScheduleCalculator.Slot> fromMissingLatitude = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(
        stop(60, null, PlaceCategory.OTHER, null, LONGITUDE, null),
        stop(60, null, PlaceCategory.OTHER, LATITUDE, LONGITUDE, null)
      )
    );
    assertEquals(LocalTime.of(10, 15), fromMissingLatitude.get(1).start());

    List<PlanScheduleCalculator.Slot> toMissingLongitude = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(
        stop(60, null, PlaceCategory.OTHER, LATITUDE, LONGITUDE, null),
        stop(60, null, PlaceCategory.OTHER, LATITUDE, null, null)
      )
    );
    assertEquals(LocalTime.of(10, 15), toMissingLongitude.get(1).start());
  }

  @Test
  public void U10_laUltimaParadaNoSumaTraslado() {
    List<PlanScheduleCalculator.Slot> single = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(stop(60, null, PlaceCategory.OTHER, null, null, null))
    );
    assertEquals(1, single.size());
    assertSlot(single.get(0), PLAN_DATE, "09:00", "10:00");

    List<PlanScheduleCalculator.Slot> chained = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(
        stop(60, null, PlaceCategory.OTHER, LATITUDE, LONGITUDE, null),
        stop(60, null, PlaceCategory.OTHER, 40.4, -3.7, null)
      )
    );
    assertEquals(2, chained.size());
    assertSlot(chained.get(1), PLAN_DATE, "11:30", "12:30");
  }

  @Test
  public void U11_unPlanDeUnSoloDiaNoGeneraCambioDeDia() {
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(
        stop(60, null, PlaceCategory.OTHER, null, null, null),
        stop(60, null, PlaceCategory.OTHER, null, null, null)
      )
    );

    assertEquals(PLAN_DATE, slots.get(0).date());
    assertEquals(PLAN_DATE, slots.get(1).date());
  }

  @Test
  public void U12_unPinDeHoraConDiaExplicitoUsaEseDia() {
    LocalDate pinnedDay = PLAN_DATE.plusDays(2);
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(pinned(pinnedDay, LocalTime.of(10, 0), 30))
    );

    assertSlot(slots.get(0), pinnedDay, "10:00", "10:30");
  }

  @Test
  public void U13_unaDuracionNoPositivaCaeAlSiguienteNivelDeLaCascada() {
    List<PlanScheduleCalculator.Slot> withPlaceDuration = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(stop(0, 120, PlaceCategory.RESTAURANT, null, null, null))
    );
    assertSlot(withPlaceDuration.get(0), PLAN_DATE, "09:00", "11:00");

    List<PlanScheduleCalculator.Slot> withCategoryDefault = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(stop(0, 0, PlaceCategory.PARK, null, null, null))
    );
    assertSlot(withCategoryDefault.get(0), PLAN_DATE, "09:00", "10:00");
  }

  @Test
  public void U14_cadaCategoriaTieneSuDuracionPorDefecto() {
    assertEquals(90, PlanScheduleCalculator.defaultDurationMinutes(PlaceCategory.RESTAURANT));
    assertEquals(60, PlanScheduleCalculator.defaultDurationMinutes(PlaceCategory.BAR));
    assertEquals(45, PlanScheduleCalculator.defaultDurationMinutes(PlaceCategory.CAFE));
    assertEquals(90, PlanScheduleCalculator.defaultDurationMinutes(PlaceCategory.MUSEUM));
    assertEquals(60, PlanScheduleCalculator.defaultDurationMinutes(PlaceCategory.PARK));
    assertEquals(60, PlanScheduleCalculator.defaultDurationMinutes(PlaceCategory.SHOPPING));
    assertEquals(120, PlanScheduleCalculator.defaultDurationMinutes(PlaceCategory.NIGHTLIFE));
    assertEquals(90, PlanScheduleCalculator.defaultDurationMinutes(PlaceCategory.CULTURE));
    assertEquals(90, PlanScheduleCalculator.defaultDurationMinutes(PlaceCategory.SPORT));
    assertEquals(60, PlanScheduleCalculator.defaultDurationMinutes(PlaceCategory.OTHER));
    assertEquals(60, PlanScheduleCalculator.defaultDurationMinutes(null));
  }

  @Test
  public void U15_planDateNuloDevuelveListaVacia() {
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      null,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(stop(60, null, PlaceCategory.OTHER, null, null, null))
    );

    assertEquals(List.of(), slots);
  }

  @Test
  public void U16_dayStartNuloDevuelveListaVacia() {
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      null,
      DAY_END,
      TransportMode.WALK,
      List.of(stop(60, null, PlaceCategory.OTHER, null, null, null))
    );

    assertEquals(List.of(), slots);
  }

  @Test
  public void U17_stopsNuloDevuelveListaVacia() {
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      null
    );

    assertEquals(List.of(), slots);
  }

  @Test
  public void U18_stopsVaciaDevuelveListaVacia() {
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of()
    );

    assertEquals(List.of(), slots);
  }

  @Test
  public void U19_dayEndNuloUsaVeintitres() {
    // given: la primera parada queda pinneada a las 22:30 y termina 23:30; la siguiente ya no
    // entra en la ventana, que sin dayEnd explicito cierra a las 23:00
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      null,
      TransportMode.WALK,
      List.of(
        pinned(null, LocalTime.of(22, 30), 60),
        stop(60, null, PlaceCategory.OTHER, null, null, null)
      )
    );

    assertSlot(slots.get(0), PLAN_DATE, "22:30", "23:30");
    assertSlot(slots.get(1), PLAN_DATE.plusDays(1), "09:00", "10:00");
  }

  @Test
  public void U20_defaultModeNuloUsaCaminando() {
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      null,
      List.of(
        stop(60, null, PlaceCategory.OTHER, 0.0, 0.0, null),
        stop(60, null, PlaceCategory.OTHER, 0.027, 0.0, null)
      )
    );

    assertEquals(LocalTime.of(10, 50), slots.get(1).start());
  }

  @Test
  public void U21_categoriaNulaUsaSesentaMinutos() {
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(stop(null, null, null, null, null, null))
    );

    assertSlot(slots.get(0), PLAN_DATE, "09:00", "10:00");
  }

  @Test
  public void U22_laCascadaDeDuracionesEsPublicaYRespetaSuOrden() {
    // override de la visita
    assertEquals(30, PlanScheduleCalculator.effectiveDuration(30, 120, PlaceCategory.RESTAURANT));
    // override no positivo: manda la duracion del lugar
    assertEquals(120, PlanScheduleCalculator.effectiveDuration(0, 120, PlaceCategory.RESTAURANT));
    // las dos primeras nulas: manda el default de la categoria
    assertEquals(90, PlanScheduleCalculator.effectiveDuration(null, null, PlaceCategory.MUSEUM));
    // sin categoria: el default generico
    assertEquals(60, PlanScheduleCalculator.effectiveDuration(null, null, null));
  }

  @Test
  public void U23_elTrasladoFijoGanaSobreLaDistanciaYNoSeConeaEnNoventaMinutos() {
    // given: dos paradas tan lejos que el calculado quedaria en el techo de 90 minutos, pero el
    // tramo de la primera esta fijado a mano en 200
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(
        stop(60, null, PlaceCategory.OTHER, LATITUDE, LONGITUDE, null, 200),
        stop(60, null, PlaceCategory.OTHER, 40.4, -3.7, null)
      )
    );

    // then: manda el valor manual sin el techo del calculado
    assertEquals(LocalTime.of(13, 20), slots.get(1).start());
  }

  @Test
  public void U24_elTrasladoFijoTienePisoDeUnMinutoYNoNecesitaCoordenadas() {
    // given/then: un override no positivo cae al piso de 1 minuto
    List<PlanScheduleCalculator.Slot> clamped = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(
        stop(60, null, PlaceCategory.OTHER, null, null, null, 0),
        stop(60, null, PlaceCategory.OTHER, null, null, null)
      )
    );
    assertEquals(LocalTime.of(10, 1), clamped.get(1).start());

    // given/then: sin coordenadas el override manda sobre el fallback de 15 minutos
    List<PlanScheduleCalculator.Slot> withoutCoordinates = PlanScheduleCalculator.schedule(
      PLAN_DATE,
      DAY_START,
      DAY_END,
      TransportMode.WALK,
      List.of(
        stop(60, null, PlaceCategory.OTHER, null, null, null, 20),
        stop(60, null, PlaceCategory.OTHER, null, null, null)
      )
    );
    assertEquals(LocalTime.of(10, 20), withoutCoordinates.get(1).start());
  }

  private static PlanScheduleCalculator.Stop stop(
    Integer duration,
    Integer placeDuration,
    PlaceCategory category,
    Double latitude,
    Double longitude,
    TransportMode transportModeToNext
  ) {
    return stop(duration, placeDuration, category, latitude, longitude, transportModeToNext, null);
  }

  private static PlanScheduleCalculator.Stop stop(
    Integer duration,
    Integer placeDuration,
    PlaceCategory category,
    Double latitude,
    Double longitude,
    TransportMode transportModeToNext,
    Integer travelMinutesToNext
  ) {
    return new PlanScheduleCalculator.Stop(
      null,
      null,
      duration,
      placeDuration,
      category,
      latitude,
      longitude,
      transportModeToNext,
      travelMinutesToNext
    );
  }

  private static PlanScheduleCalculator.Stop pinned(
    LocalDate date,
    LocalTime time,
    Integer duration
  ) {
    return new PlanScheduleCalculator.Stop(
      date,
      time,
      duration,
      null,
      PlaceCategory.OTHER,
      null,
      null,
      null,
      null
    );
  }

  private static void assertSlot(
    PlanScheduleCalculator.Slot slot,
    LocalDate date,
    String start,
    String end
  ) {
    assertEquals(date, slot.date(), "dia");
    assertEquals(LocalTime.parse(start), slot.start(), "inicio");
    assertEquals(LocalTime.parse(end), slot.end(), "fin");
  }
}
