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
    plan.setId(1L);
    place = new Place();
    place.setId(2L);
    entry = new PlanPlace();
    entry.setId(3L);
    entry.setPlan(plan);
    entry.setPlace(place);
    entry.setSortOrder(7);
    when(ownership.getEditablePlan(1L, "owner")).thenReturn(plan);
    when(plans.findById(1L)).thenReturn(Optional.of(plan));
    when(places.findById(2L)).thenReturn(Optional.of(place));
    when(repository.findByPlanId(1L)).thenReturn(List.of(entry));
    when(repository.findById(3L)).thenReturn(Optional.of(entry));
    when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
  }

  @Test
  void U01_addAssignsMaxPlusOne() {
    assertEquals(8, service.addPlaceToPlan(1L, 2L, "owner").getSortOrder());
  }

  @Test
  void addToEmptyPlanStartsAtOne() {
    when(repository.findByPlanId(1L)).thenReturn(List.of());
    assertEquals(1, service.addPlaceToPlan(1L, 2L, "owner").getSortOrder());
  }

  @Test
  void U02_samePlaceCanBeAddedAgain() {
    when(repository.existsByPlanIdAndPlaceId(1L, 2L)).thenReturn(true);
    assertEquals(8, service.addPlaceToPlan(1L, 2L, "owner").getSortOrder());
    verify(repository).save(any());
  }

  @Test
  void U03_readsOrderedRepository() {
    assertEquals(List.of(entry), service.getItinerary(1L, "owner"));
    verify(repository).findByPlanId(1L);
  }

  @Test
  void U04_updatesSchedule() {
    LocalDate date = LocalDate.of(2026, 12, 1);
    LocalTime time = LocalTime.of(10, 30);
    assertSame(entry, service.updatePlanPlace(1L, 3L, date, time, "owner"));
    assertEquals(date, entry.getVisitDate());
    assertEquals(time, entry.getVisitTime());
  }

  @Test
  void U05_removesEntry() {
    service.removePlaceFromPlan(1L, 3L, "owner");
    verify(repository).deleteById(3L);
  }

  @Test
  void U06_reordersByEntryIds() {
    service.reorderPlaces(1L, List.of(3L), "owner");
    assertEquals(1, entry.getSortOrder());
    verify(repository).save(entry);
  }

  @Test
  void U07_membershipTrue() {
    when(repository.existsByPlanIdAndPlaceId(1L, 2L)).thenReturn(true);
    assertTrue(service.isPlaceInPlan(1L, 2L));
  }

  @Test
  void U08_membershipFalse() {
    assertFalse(service.isPlaceInPlan(1L, 2L));
  }

  @Test
  void publicItineraryDoesNotRequireOwner() {
    plan.setIsPublic(true);
    assertEquals(List.of(entry), service.getItinerary(1L, null));
    verifyNoInteractions(ownership);
  }

  @Test
  void privateAnonymousRejected() {
    assertThrows(PlanNotFoundException.class, () -> service.getItinerary(1L, null));
  }

  @Test
  void unknownPlanRejected() {
    assertThrows(PlanNotFoundException.class, () -> service.getItinerary(99L, null));
  }

  @Test
  void unknownPlaceRejected() {
    assertThrows(PlanNotFoundException.class, () -> service.addPlaceToPlan(1L, 99L, "owner"));
  }

  @Test
  void unknownEntryRejected() {
    assertThrows(PlanNotFoundException.class, () -> service.removePlaceFromPlan(1L, 99L, "owner"));
  }

  @Test
  void entryFromAnotherPlanRejected() {
    Plan other = new Plan();
    other.setId(9L);
    entry.setPlan(other);
    assertThrows(PlanNotFoundException.class, () -> service.removePlaceFromPlan(1L, 3L, "owner"));
    verify(repository, never()).deleteById(any());
  }

  @Test
  void invalidReorderRejectedBeforeSaving() {
    for (List<Long> ids : Arrays.asList(null, List.<Long>of(), List.of(99L), List.of(3L, 3L))) {
      assertThrows(IllegalArgumentException.class, () -> service.reorderPlaces(1L, ids, "owner"));
    }
    verify(repository, never()).save(any());
  }

  @Test
  void entityAccessorsPreserveExistingDescription() {
    entry.setDescription("note");
    assertEquals("note", entry.getDescription());
    assertEquals(3L, entry.getId());
    assertSame(plan, entry.getPlan());
    assertSame(place, entry.getPlace());
  }
}
