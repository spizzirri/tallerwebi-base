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
    assertSame(entry, service.updatePlanPlace(PLAN_ID, ENTRY_ID, date, time, "owner"));
    assertEquals(date, entry.getVisitDate());
    assertEquals(time, entry.getVisitTime());
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
}
