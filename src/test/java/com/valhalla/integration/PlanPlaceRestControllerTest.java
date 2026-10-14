package com.valhalla.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.valhalla.domain.place.*;
import com.valhalla.domain.plan.*;
import com.valhalla.domain.planplace.*;
import com.valhalla.domain.user.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@WebIntegrationTest
@Transactional
class PlanPlaceRestControllerTest extends PlanPlaceWebFixture {

  @Autowired
  private PlanPlaceService itineraryService;

  @Test
  void editorCanManageEveryItineraryOperation() {
    User editor = new User();
    editor.setEmail("editor@test.com");
    editor.setPassword("password");
    editor.setRole("USER");
    users.save(editor);
    plan.getParticipants().add(editor);
    plan.getEditors().add(editor);
    plans.save(plan);
    String email = editor.getEmail();
    PlanPlace firstEntry = itineraryService.addPlaceToPlan(plan.getId(), first, email);
    PlanPlace secondEntry = itineraryService.addPlaceToPlan(plan.getId(), second, email);
    itineraryService.updatePlanPlace(
      plan.getId(),
      firstEntry.getId(),
      java.time.LocalDate.of(2026, 12, 1),
      java.time.LocalTime.of(10, 30),
      email
    );
    itineraryService.reorderPlaces(
      plan.getId(),
      java.util.List.of(secondEntry.getId(), firstEntry.getId()),
      email
    );
    assertEquals(2, firstEntry.getSortOrder());
    assertEquals(java.time.LocalTime.of(10, 30), firstEntry.getVisitTime());
    itineraryService.removePlaceFromPlan(plan.getId(), firstEntry.getId(), email);
    assertEquals(1, entries.findByPlanId(plan.getId()).size());
  }

  @Test
  void detailIncludesPersistedVisitAndPlaceDataForParticipants() throws Exception {
    Long id = add(first);
    PlanPlace entry = entries.findById(id).orElseThrow();
    entry.setDescription("Visita guiada");
    entry.setVisitDate(java.time.LocalDate.of(2026, 12, 1));
    entry.setVisitTime(java.time.LocalTime.of(10, 30));
    entries.save(entry);
    User participant = new User();
    participant.setEmail("participant@test.com");
    participant.setPassword("password");
    participant.setRole("USER");
    users.save(participant);
    plan.getParticipants().add(participant);
    plans.save(plan);
    mvc
      .perform(get(url).with(user(participant.getEmail())))
      .andExpect(status().isOk())
      .andExpect(jsonValue("/0/id", id))
      .andExpect(jsonValue("/0/description", "Visita guiada"))
      .andExpect(jsonValue("/0/visitDate", "2026-12-01"))
      .andExpect(jsonValue("/0/visitTime", "10:30"))
      .andExpect(jsonValue("/0/address", entry.getPlace().getAddress()))
      .andExpect(jsonValue("/0/category", entry.getPlace().getCategory().name()))
      .andExpect(jsonMissing("/0/password"))
      .andExpect(jsonMissing("/0/plan"));
  }

  @Test
  void repeatedPlacesKeepSeparateVisitDetails() throws Exception {
    add(first);
    add(first);
    var visits = entries.findByPlanId(plan.getId());
    visits.get(0).setDescription("Primera visita");
    visits.get(1).setDescription("Segunda visita");
    entries.save(visits.get(0));
    entries.save(visits.get(1));
    mvc
      .perform(get(url).with(user("apl@test.com")))
      .andExpect(status().isOk())
      .andExpect(jsonValue("/0/id", visits.get(0).getId()))
      .andExpect(jsonValue("/1/id", visits.get(1).getId()))
      .andExpect(jsonValue("/0/description", "Primera visita"))
      .andExpect(jsonValue("/1/description", "Segunda visita"));
  }

  @Test
  void I01_addPlace() throws Exception {
    add(first);
    assertEquals(1, entries.findByPlanId(plan.getId()).get(0).getSortOrder());
  }

  @Test
  void I02_samePlaceCanBeAddedTwice() throws Exception {
    add(first);
    add(first);
    var result = entries.findByPlanId(plan.getId());
    assertEquals(2, result.size());
    assertEquals(2, result.get(1).getSortOrder());
  }

  @Test
  void I03_orderedItineraryDoesNotExposeOwner() throws Exception {
    add(first);
    add(second);
    mvc
      .perform(get(url).with(user("apl@test.com")))
      .andExpect(status().isOk())
      .andExpect(jsonValue("/0/placeId", first))
      .andExpect(jsonValue("/1/sortOrder", 2))
      .andExpect(jsonMissing("/0/plan"));
  }

  @Test
  void I04_updatesScheduleAndClearsIt() throws Exception {
    Long id = add(first);
    mvc
      .perform(
        put(url + "/" + id)
          .with(user("apl@test.com"))
          .with(csrf())
          .contentType("application/json")
          .content("{\"visitDate\":\"2026-12-01\",\"visitTime\":\"10:30\"}")
      )
      .andExpect(status().isOk())
      .andExpect(jsonValue("/visitDate", "2026-12-01"))
      .andExpect(jsonValue("/visitTime", "10:30"));
    mvc
      .perform(
        put(url + "/" + id)
          .with(user("apl@test.com"))
          .with(csrf())
          .contentType("application/json")
          .content("{}")
      )
      .andExpect(status().isOk());
    assertNull(entries.findById(id).orElseThrow().getVisitDate());
  }

  @Test
  void I05_deletesEntry() throws Exception {
    Long id = add(first);
    mvc
      .perform(delete(url + "/" + id).with(user("apl@test.com")).with(csrf()))
      .andExpect(status().isOk());
    assertTrue(entries.findById(id).isEmpty());
  }

  @Test
  void I06_reorders() throws Exception {
    Long firstEntry = add(first);
    Long secondEntry = add(second);
    mvc
      .perform(
        post(url + "/reorder")
          .with(user("apl@test.com"))
          .with(csrf())
          .contentType("application/json")
          .content("[" + secondEntry + "," + firstEntry + "]")
      )
      .andExpect(status().isOk());
    assertEquals(second, entries.findByPlanId(plan.getId()).get(0).getPlace().getId());
  }

  @Test
  void invalidDateReturns400AndDoesNotModify() throws Exception {
    Long id = add(first);
    mvc
      .perform(
        put(url + "/" + id)
          .with(user("apl@test.com"))
          .with(csrf())
          .contentType("application/json")
          .content("{\"visitDate\":\"bad\"}")
      )
      .andExpect(status().isBadRequest());
    assertNull(entries.findById(id).orElseThrow().getVisitDate());
  }

  @Test
  void invalidReorderReturns400() throws Exception {
    add(first);
    mvc
      .perform(
        post(url + "/reorder")
          .with(user("apl@test.com"))
          .with(csrf())
          .contentType("application/json")
          .content("[]")
      )
      .andExpect(status().isBadRequest());
    assertEquals(1, entries.findByPlanId(plan.getId()).get(0).getSortOrder());
  }

  @Test
  void I11_placesIncludesOnlyOwnerPlans() throws Exception {
    mvc
      .perform(get("/explore").with(user("apl@test.com")))
      .andExpect(status().isOk())
      .andExpect(model().attribute("userPlans", org.hamcrest.Matchers.hasSize(1)));
    mvc
      .perform(get("/explore").with(user("other@test.com")))
      .andExpect(model().attribute("userPlans", org.hamcrest.Matchers.empty()));
  }

  @Test
  void I12_anonymousPlacesHasNoUserPlans() throws Exception {
    mvc
      .perform(get("/explore"))
      .andExpect(status().isOk())
      .andExpect(model().attributeDoesNotExist("userPlans"));
  }
}
