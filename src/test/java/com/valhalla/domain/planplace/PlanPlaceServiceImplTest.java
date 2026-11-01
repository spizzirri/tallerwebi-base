package com.valhalla.domain.planplace;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.place.*;
import com.valhalla.domain.plan.*;
import com.valhalla.infrastructure.planplace.PlanPlaceServiceImpl;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlanPlaceServiceImplTest {

  static final UUID PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000001");
  static final UUID OTHER_PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000009");
  static final UUID UNKNOWN_PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000063");
  static final UUID PLACE_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000002");
  static final UUID UNKNOWN_PLACE_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000064");
  static final UUID ENTRY_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000005");
  static final UUID UNKNOWN_ENTRY_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000065");

  PlanPlaceRepository repository;
  PlanRepository plans;
  PlanService ownership;
  PlaceRepository places;
  PlanPlaceServiceImpl service;
  Plan plan;
  Place place;
  PlanPlace entry;

  @BeforeEach
  void setup() {
    repository = mock(PlanPlaceRepository.class);
    plans = mock(PlanRepository.class);
    ownership = mock(PlanService.class);
    places = mock(PlaceRepository.class);
    service = new PlanPlaceServiceImpl(repository, plans, ownership, places);
    plan = new Plan();
    plan.setId(PLAN_ID);
    place = new Place();
    place.setId(PLACE_ID);
    entry = new PlanPlace();
    entry.setId(ENTRY_ID);
    entry.setPlan(plan);
    entry.setPlace(place);
    entry.setSortOrder(7);
    when(ownership.getEditablePlan(PLAN_ID, "owner")).thenReturn(plan);
    when(plans.findById(PLAN_ID)).thenReturn(Optional.of(plan));
    when(places.findById(PLACE_ID)).thenReturn(Optional.of(place));
    when(repository.findByPlanId(PLAN_ID)).thenReturn(List.of(entry));
    when(repository.findById(ENTRY_ID)).thenReturn(Optional.of(entry));
    when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
  }

  @Test
  void U01_addAssignsMaxPlusOne() {
    assertEquals(8, service.addPlaceToPlan(PLAN_ID, PLACE_ID, "owner").getSortOrder());
  }

  @Test
  void addToEmptyPlanStartsAtOne() {
    when(repository.findByPlanId(PLAN_ID)).thenReturn(List.of());
    assertEquals(1, service.addPlaceToPlan(PLAN_ID, PLACE_ID, "owner").getSortOrder());
  }

  @Test
  void U02_samePlaceCanBeAddedAgain() {
    when(repository.existsByPlanIdAndPlaceId(PLAN_ID, PLACE_ID)).thenReturn(true);
    assertEquals(8, service.addPlaceToPlan(PLAN_ID, PLACE_ID, "owner").getSortOrder());
    verify(repository).save(any());
  }

  @Test
  void U03_readsOrderedRepository() {
    assertEquals(List.of(entry), service.getItinerary(PLAN_ID, "owner"));
    verify(repository).findByPlanId(PLAN_ID);
  }

  @Test
  void U04_updatesSchedule() {
    LocalDate date = LocalDate.of(2026, 12, 1);
    LocalTime time = LocalTime.of(10, 30);
    PlanPlaceUpdate changes = new PlanPlaceUpdate(null, date, time, null, null);
    assertSame(entry, service.updatePlanPlace(PLAN_ID, ENTRY_ID, changes, "owner"));
    assertEquals(date, entry.getVisitDate());
    assertEquals(time, entry.getVisitTime());
    assertTrue(entry.isTimePinned());
  }

  @Test
  void updateGuardaDuracionYModoDelTramo() {
    PlanPlaceUpdate changes = new PlanPlaceUpdate(
      45,
      LocalDate.of(2026, 12, 1),
      LocalTime.of(10, 30),
      TransportMode.CAR,
      null
    );

    service.updatePlanPlace(PLAN_ID, ENTRY_ID, changes, "owner");

    assertEquals(45, entry.getDurationMinutes());
    assertEquals(TransportMode.CAR, entry.getTransportModeToNext());
  }

  @Test
  void updateConSoloUnaHoraTambienFijaLaParada() {
    service.updatePlanPlace(
      PLAN_ID,
      ENTRY_ID,
      new PlanPlaceUpdate(null, null, LocalTime.of(14, 0), null, null),
      "owner"
    );

    assertTrue(entry.isTimePinned());
    assertEquals(LocalTime.of(14, 0), entry.getVisitTime());
  }

  @Test
  void updateSinFechaNiHoraDejaQueElSchedulerDeriveElHorario() {
    plan.setEventDate(LocalDate.of(2026, 12, 1));
    plan.setEventTime(LocalTime.of(9, 0));
    plan.addPlanPlace(entry);

    service.updatePlanPlace(
      PLAN_ID,
      ENTRY_ID,
      new PlanPlaceUpdate(null, null, null, null, null),
      "owner"
    );

    assertFalse(entry.isTimePinned());
    assertEquals(LocalDate.of(2026, 12, 1), entry.getVisitDate());
    assertEquals(LocalTime.of(9, 0), entry.getVisitTime());
    assertEquals(60, entry.getDurationMinutes());
  }

  @Test
  void updateGuardaElTrasladoFijoYLoLimpiaConNulo() {
    service.updatePlanPlace(
      PLAN_ID,
      ENTRY_ID,
      new PlanPlaceUpdate(null, null, null, null, 30),
      "owner"
    );

    assertEquals(30, entry.getTravelMinutesToNext());

    // El form vacio el input: null no es "conserva", es volver al calculo por distancia y modo.
    service.updatePlanPlace(
      PLAN_ID,
      ENTRY_ID,
      new PlanPlaceUpdate(null, null, null, null, null),
      "owner"
    );

    assertNull(entry.getTravelMinutesToNext());
  }

  @Test
  void addDejaLaParadaNuevaConDuracionYHoraDerivadas() {
    plan.setEventDate(LocalDate.of(2026, 12, 1));
    plan.setEventTime(LocalTime.of(10, 0));
    place.setCategory(PlaceCategory.MUSEUM);
    place.setDefaultDurationMinutes(90);

    PlanPlace added = service.addPlaceToPlan(PLAN_ID, PLACE_ID, "owner");

    assertEquals(90, added.getDurationMinutes());
    assertEquals(LocalDate.of(2026, 12, 1), added.getVisitDate());
    assertEquals(LocalTime.of(10, 0), added.getVisitTime());
  }

  @Test
  void removeRecalculaLaParadaQueQueda() {
    plan.setEventDate(LocalDate.of(2026, 12, 1));
    plan.setEventTime(LocalTime.of(10, 0));
    PlanPlace second = new PlanPlace();
    second.setId(UUID.fromString("0192f0a1-0000-7000-8000-000000000006"));
    second.setPlan(plan);
    second.setPlace(place);
    second.setSortOrder(2);
    second.setDurationMinutes(90);
    // Hora vieja: la derivacion tiene que pisarla al cerrar el hueco de la parada que se va.
    second.setVisitTime(LocalTime.of(12, 0));
    plan.addPlanPlace(entry);
    plan.addPlanPlace(second);

    service.removePlaceFromPlan(PLAN_ID, ENTRY_ID, "owner");

    assertEquals(LocalTime.of(10, 0), second.getVisitTime());
    assertEquals(LocalDate.of(2026, 12, 1), second.getVisitDate());
    verify(repository).deleteById(ENTRY_ID);
  }

  @Test
  void reorderConAutoScheduleRecalculaElHorario() {
    plan.setEventDate(LocalDate.of(2026, 12, 1));
    plan.setEventTime(LocalTime.of(10, 0));
    place.setLatitude(-34.5895);
    place.setLongitude(-58.4095);
    PlanPlace second = new PlanPlace();
    second.setId(UUID.fromString("0192f0a1-0000-7000-8000-000000000006"));
    second.setPlan(plan);
    second.setPlace(place);
    second.setSortOrder(2);
    second.setDurationMinutes(90);
    entry.setSortOrder(1);
    entry.setDurationMinutes(90);
    plan.addPlanPlace(entry);
    plan.addPlanPlace(second);
    when(repository.findByPlanId(PLAN_ID)).thenReturn(List.of(entry, second));

    service.reorderPlaces(PLAN_ID, List.of(second.getId(), entry.getId()), "owner");

    // Mismo lugar y misma duracion: el orden nuevo arranca en la hora de inicio del plan y la
    // segunda parada paga los 90 minutos de la primera mas el piso de 5 del traslado.
    assertEquals(LocalTime.of(10, 0), second.getVisitTime());
    assertEquals(LocalTime.of(11, 35), entry.getVisitTime());
  }

  @Test
  void reorderConAutoScheduleApagadoConservaLasHorasFijadas() {
    plan.setEventDate(LocalDate.of(2026, 12, 1));
    plan.setEventTime(LocalTime.of(10, 0));
    plan.getSettings().setAutoSchedule(false);
    PlanPlace second = new PlanPlace();
    second.setId(UUID.fromString("0192f0a1-0000-7000-8000-000000000006"));
    second.setPlan(plan);
    second.setPlace(place);
    second.setSortOrder(2);
    second.setVisitTime(LocalTime.of(16, 0));
    entry.setSortOrder(1);
    entry.setVisitTime(LocalTime.of(18, 0));
    plan.addPlanPlace(entry);
    plan.addPlanPlace(second);
    when(repository.findByPlanId(PLAN_ID)).thenReturn(List.of(entry, second));

    service.reorderPlaces(PLAN_ID, List.of(second.getId(), entry.getId()), "owner");

    assertEquals(LocalTime.of(18, 0), entry.getVisitTime());
    assertEquals(LocalTime.of(16, 0), second.getVisitTime());
  }

  @Test
  void U05_removesEntry() {
    service.removePlaceFromPlan(PLAN_ID, ENTRY_ID, "owner");
    verify(repository).deleteById(ENTRY_ID);
  }

  @Test
  void U06_reordersByEntryIds() {
    service.reorderPlaces(PLAN_ID, List.of(ENTRY_ID), "owner");
    assertEquals(1, entry.getSortOrder());
    verify(repository).save(entry);
  }

  @Test
  void U07_membershipTrue() {
    when(repository.existsByPlanIdAndPlaceId(PLAN_ID, PLACE_ID)).thenReturn(true);
    assertTrue(service.isPlaceInPlan(PLAN_ID, PLACE_ID));
  }

  @Test
  void U08_membershipFalse() {
    assertFalse(service.isPlaceInPlan(PLAN_ID, PLACE_ID));
  }

  @Test
  void publicItineraryDoesNotRequireOwner() {
    plan.setIsPublic(true);
    assertEquals(List.of(entry), service.getItinerary(PLAN_ID, null));
    verifyNoInteractions(ownership);
  }

  @Test
  void privateAnonymousRejected() {
    assertThrows(PlanNotFoundException.class, () -> service.getItinerary(PLAN_ID, null));
  }

  @Test
  void unknownPlanRejected() {
    assertThrows(PlanNotFoundException.class, () -> service.getItinerary(UNKNOWN_PLAN_ID, null));
  }

  @Test
  void unknownPlaceRejected() {
    assertThrows(
      PlanNotFoundException.class,
      () -> service.addPlaceToPlan(PLAN_ID, UNKNOWN_PLACE_ID, "owner")
    );
  }

  @Test
  void unknownEntryRejected() {
    assertThrows(
      PlanNotFoundException.class,
      () -> service.removePlaceFromPlan(PLAN_ID, UNKNOWN_ENTRY_ID, "owner")
    );
  }

  @Test
  void entryFromAnotherPlanRejected() {
    Plan other = new Plan();
    other.setId(OTHER_PLAN_ID);
    entry.setPlan(other);
    assertThrows(
      PlanNotFoundException.class,
      () -> service.removePlaceFromPlan(PLAN_ID, ENTRY_ID, "owner")
    );
    verify(repository, never()).deleteById(any());
  }

  @Test
  void invalidReorderRejectedBeforeSaving() {
    for (List<UUID> ids : Arrays.asList(
      null,
      List.<UUID>of(),
      List.of(UNKNOWN_ENTRY_ID),
      List.of(ENTRY_ID, ENTRY_ID)
    )) {
      assertThrows(
        IllegalArgumentException.class,
        () -> service.reorderPlaces(PLAN_ID, ids, "owner")
      );
    }
    verify(repository, never()).save(any());
  }

  @Test
  void entityAccessorsPreserveExistingDescription() {
    entry.setDescription("note");
    assertEquals("note", entry.getDescription());
    assertEquals(ENTRY_ID, entry.getId());
    assertSame(plan, entry.getPlan());
    assertSame(place, entry.getPlace());
  }

  @Test
  void entityAccessorsPreserveVisitOverrides() {
    entry.setDurationMinutes(45);
    entry.setTransportModeToNext(TransportMode.CAR);

    assertEquals(45, entry.getDurationMinutes());
    assertEquals(TransportMode.CAR, entry.getTransportModeToNext());
  }
}
