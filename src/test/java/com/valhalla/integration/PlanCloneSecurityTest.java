package com.valhalla.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

// Inherits the real database and filter-chain fixture; scenario ids S-01..S-03 of docs/specs/15-CLO.md.
@WebIntegrationTest
@Transactional
class PlanCloneSecurityTest {

  @Autowired
  WebApplicationContext context;

  @Autowired
  PlanRepository plans;

  @Autowired
  UserRepository users;

  MockMvc mvc;
  String url;

  @BeforeEach
  void setup() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    User owner = new User();
    owner.setEmail("apl@test.com");
    owner.setPassword("password");
    owner.setRole("USER");
    users.save(owner);
    Plan plan = new Plan();
    plan.setName("Itinerary");
    plan.setAdministrator(owner);
    url = "/plans/" + plans.save(plan).getId() + "/clone";
  }

  // With a valid CSRF token these isolate authentication.
  @Test
  void S01_anonymousCloneRedirectsWithoutCreating() throws Exception {
    mvc
      .perform(post(url).with(csrf()))
      .andExpect(status().isFound())
      .andExpect(redirectedUrlPattern("**/auth/login"));
    assertEquals(1, plans.findAll().size());
  }

  @Test
  void S02_anonymousCloneWithoutCsrfIsRejectedWithoutCreating() throws Exception {
    // Missing token is rejected with 403 before login routing; either way nothing is created.
    mvc.perform(post(url)).andExpect(status().isForbidden());
    assertEquals(1, plans.findAll().size());
  }

  @Test
  void S03_getOnCloneRouteDoesNotClone() throws Exception {
    // The mapping is POST-only; an anonymous GET never reaches the clone either.
    mvc
      .perform(get(url))
      .andExpect(status().isFound())
      .andExpect(redirectedUrlPattern("**/auth/login"));
    assertEquals(1, plans.findAll().size());
  }
}
