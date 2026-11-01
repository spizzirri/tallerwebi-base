package com.valhalla.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import com.valhalla.domain.place.*;
import com.valhalla.domain.plan.*;
import com.valhalla.domain.planplace.*;
import com.valhalla.infrastructure.planplace.JpaPlanPlaceRepository;
import com.valhalla.integration.JpaIntegrationTest;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@JpaIntegrationTest
@Transactional
class PlanPlaceRepositoryTest {

  @Autowired
  PlanPlaceRepository entries;

  @Autowired
  JpaPlanPlaceRepository jpa;

  @Autowired
  PlanRepository plans;

  @Autowired
  PlaceRepository places;

  @Autowired
  EntityManager em;

  Plan plan;
  Place first;

  @BeforeEach
  void setup() {
    plan = new Plan();
    plan.setName("Repository plan");
    plans.save(plan);
    first = places.findAll().get(0);
  }

  PlanPlace save(Place place, int order) {
    PlanPlace entry = new PlanPlace();
    entry.setPlan(plan);
    entry.setPlace(place);
    entry.setSortOrder(order);
    return entries.save(entry);
  }

  @Test
  void I07_ordersAndFetchesPlaces() {
    PlanPlace late = save(first, 8);
    PlanPlace early = save(places.findAll().get(1), 2);
    em.flush();
    em.clear();
    var result = entries.findByPlanId(plan.getId());
    assertEquals(early.getId(), result.get(0).getId());
    assertEquals(late.getId(), result.get(1).getId());
    assertTrue(
      em.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(result.get(0), "place")
    );
  }

  @Test
  void I08_existingMembership() {
    save(first, 1);
    assertTrue(entries.existsByPlanIdAndPlaceId(plan.getId(), first.getId()));
  }

  @Test
  void I09_missingMembership() {
    assertFalse(entries.existsByPlanIdAndPlaceId(plan.getId(), first.getId()));
  }

  @Test
  void I10_samePlaceCanBeSavedTwice() {
    save(first, 1);
    save(first, 2);
    em.flush();
    assertEquals(2, entries.findByPlanId(plan.getId()).size());
  }

  @Test
  void findAndDelete() {
    UUID id = save(first, 1).getId();
    assertTrue(entries.findById(id).isPresent());
    entries.deleteById(id);
    assertTrue(entries.findById(id).isEmpty());
  }

  @Test
  void planCollectionPersistsAndLoadsOrderedEntries() {
    PlanPlace late = new PlanPlace();
    late.setPlace(first);
    late.setSortOrder(8);
    PlanPlace early = new PlanPlace();
    early.setPlace(places.findAll().get(1));
    early.setSortOrder(2);
    plan.addPlanPlace(late);
    plan.addPlanPlace(early);
    plans.save(plan);
    em.flush();
    UUID planId = plan.getId();
    em.clear();
    Plan loaded = plans.findById(planId).orElseThrow();
    // The itinerary renders outside a session (controller builds the detail view model), so the
    // collection is deliberately FetchType.EAGER and is loaded with the plan.
    assertTrue(
      em.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(loaded, "planPlaces")
    );
    assertEquals(2, loaded.getPlanPlaces().size());
    assertEquals(2, loaded.getPlanPlaces().get(0).getSortOrder());
    assertEquals(8, loaded.getPlanPlaces().get(1).getSortOrder());
    assertEquals(first.getId(), loaded.getPlanPlaces().get(1).getPlace().getId());
    UUID earlyId = loaded.getPlanPlaces().get(0).getId();
    UUID lateId = loaded.getPlanPlaces().get(1).getId();
    assertEquals(planId, loaded.getPlanPlaces().get(0).getPlan().getId());
    loaded.setName("Updated plan");
    plans.save(loaded);
    em.flush();
    em.clear();
    assertEquals(2, plans.findById(planId).orElseThrow().getPlanPlaces().size());
    plans.deleteById(planId);
    em.flush();
    assertTrue(entries.findById(earlyId).isEmpty());
    assertTrue(entries.findById(lateId).isEmpty());
    assertTrue(places.findById(first.getId()).isPresent());
  }

  @Test
  void I11_legacyNullTimePinnedLoadsAsNotPinned() {
    PlanPlace entry = save(first, 1);
    em.flush();

    em
      .createNativeQuery("UPDATE plan_places SET time_pinned = NULL WHERE id = ?")
      .setParameter(1, entry.getId())
      .executeUpdate();
    em.clear();

    PlanPlace loaded = entries.findById(entry.getId()).orElseThrow();
    assertFalse(loaded.isTimePinned());
  }
}
