package com.valhalla.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

// Inherits the real database and filter-chain fixture, as well as the endpoint contract tests.
class PlanPlaceSecurityTest extends PlanPlaceWebFixture {

  // With a valid CSRF token these isolate authentication. Missing tokens are rejected with 403 before login routing.
  @Test
  void S01_anonymousPostRedirectsWithoutWriting() throws Exception {
    mvc
      .perform(post(url).with(csrf()).param("placeId", first.toString()))
      .andExpect(status().isFound())
      .andExpect(redirectedUrlPattern("**/auth/login"));
    assertTrue(entries.findByPlanId(plan.getId()).isEmpty());
  }

  @Test
  void S02_anonymousPutRedirectsWithoutWriting() throws Exception {
    UUID id = add(first);
    mvc
      .perform(
        put(url + "/" + id)
          .with(csrf())
          .contentType("application/json")
          .content("{\"visitDate\":\"2026-12-01\"}")
      )
      .andExpect(status().isFound());
    assertNull(entries.findById(id).orElseThrow().getVisitDate());
  }

  @Test
  void S03_anonymousDeleteRedirectsWithoutDeleting() throws Exception {
    UUID id = add(first);
    mvc.perform(delete(url + "/" + id).with(csrf())).andExpect(status().isFound());
    assertTrue(entries.findById(id).isPresent());
  }

  @Test
  void S04_catalogIsPublic() throws Exception {
    mvc
      .perform(get("/api/places"))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith("application/json"));
  }

  @Test
  void S05_S06_otherOwnerGets404AndCannotWrite() throws Exception {
    mvc
      .perform(
        post(url).param("placeId", first.toString()).with(user("other@test.com")).with(csrf())
      )
      .andExpect(status().isNotFound());
    assertTrue(entries.findByPlanId(plan.getId()).isEmpty());
  }

  @Test
  void S07_sessionWithoutCsrfRejected() throws Exception {
    mvc
      .perform(post(url).param("placeId", first.toString()).with(user("apl@test.com")))
      .andExpect(status().isForbidden());
    assertTrue(entries.findByPlanId(plan.getId()).isEmpty());
  }

  @Test
  void S08_onlyPublicItineraryIsAnonymous() throws Exception {
    add(first);
    mvc.perform(get(url)).andExpect(status().isNotFound());
    mvc.perform(get(url).with(user("other@test.com"))).andExpect(status().isNotFound());
    plan.setIsPublic(true);
    plans.save(plan);
    mvc.perform(get(url)).andExpect(status().isOk()).andExpect(jsonValue("/0/placeId", first));
  }

  @Test
  void otherOwnerCannotUpdateDeleteOrReorder() throws Exception {
    UUID id = add(first);
    mvc
      .perform(
        put(url + "/" + id)
          .with(user("other@test.com"))
          .with(csrf())
          .contentType("application/json")
          .content("{}")
      )
      .andExpect(status().isNotFound());
    mvc
      .perform(delete(url + "/" + id).with(user("other@test.com")).with(csrf()))
      .andExpect(status().isNotFound());
    mvc
      .perform(
        post(url + "/reorder")
          .with(user("other@test.com"))
          .with(csrf())
          .contentType("application/json")
          .content("[\"" + id + "\"]")
      )
      .andExpect(status().isNotFound());
    assertTrue(entries.findById(id).isPresent());
  }

  @Test
  void entryFromAnotherPlanIsNotFound() throws Exception {
    UUID id = add(first);
    com.valhalla.domain.plan.Plan other = new com.valhalla.domain.plan.Plan();
    other.setName("Other");
    other.setAdministrator(plan.getAdministrator());
    plans.save(other);
    mvc
      .perform(
        delete("/api/plans/" + other.getId() + "/places/" + id)
          .with(user("apl@test.com"))
          .with(csrf())
      )
      .andExpect(status().isNotFound());
    assertTrue(entries.findById(id).isPresent());
  }
}
