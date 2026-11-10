package com.valhalla.integration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@WebIntegrationTest
@Transactional
public class PlanBudgetIntegrationTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private PlanRepository plans;

  @Autowired
  private UserRepository users;

  @Autowired
  private PlanService service;

  @Autowired
  private EntityManager em;

  private MockMvc mvc;
  private UUID id;

  @BeforeEach
  public void setUp() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    Plan plan = new Plan();
    plan.setName("Budget plan");
    plan.setIsPublic(true);
    plan.setAdministrator(createUser("owner@budget.test"));
    User editor = createUser("editor@budget.test");
    plan.getParticipants().add(editor);
    plan.getEditors().add(editor);
    plan.getParticipants().add(createUser("member@budget.test"));
    id = plans.save(plan).getId();
  }

  @Test
  public void ownerAndEditorSavePersistedRangeAndClone() throws Exception {
    for (String role : new String[] { "owner", "editor" }) {
      mvc
        .perform(
          post("/plans/{id}/budget", id)
            .with(user(role + "@budget.test"))
            .with(csrf())
            .param("minimum", "1000.50")
            .param("maximum", "2000")
            .param("notes", "Food and tickets")
        )
        .andExpect(status().is3xxRedirection());
      em.flush();
      em.clear();
      assertEquals(
        0,
        plans
          .findById(id)
          .orElseThrow()
          .getBudget()
          .getMinimum()
          .compareTo(new BigDecimal("1000.50"))
      );
    }
    mvc
      .perform(get("/plans/{id}", id).with(user("member@budget.test")))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("1.000,50")));
    mvc
      .perform(get("/plans/{id}/public", id))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("Food and tickets")));
    Plan copy = service.clonePlan(id, "member@budget.test");
    assertEquals("Food and tickets", copy.getBudget().getNotes());
  }

  @Test
  public void invalidInputsRenderErrorsWithoutSaving() throws Exception {
    for (String[] values : new String[][] {
      { "20", "10" },
      { "-1", "10" },
      { "1.001", "10" },
      { "1", "" },
      { "bad", "10" },
      { "1", "10000000000" },
    }) {
      mvc
        .perform(
          post("/plans/{id}/budget", id)
            .with(user("owner@budget.test"))
            .with(csrf())
            .param("minimum", values[0])
            .param("maximum", values[1])
        )
        .andExpect(status().isOk())
        .andExpect(model().attributeHasErrors("budgetForm"));
      assertFalse(plans.findById(id).orElseThrow().getBudget().isEstimated());
    }
  }

  @Test
  public void memberCannotWriteAndCsrfIsRequired() throws Exception {
    mvc
      .perform(
        post("/plans/{id}/budget", id)
          .with(user("member@budget.test"))
          .with(csrf())
          .param("minimum", "1")
          .param("maximum", "2")
      )
      .andExpect(status().is3xxRedirection());
    mvc
      .perform(post("/plans/{id}/budget", id).with(user("owner@budget.test")))
      .andExpect(status().isForbidden());
    assertFalse(plans.findById(id).orElseThrow().getBudget().isEstimated());
  }

  @Test
  public void zeroMeansFreeAndEmptyClearsEstimate() throws Exception {
    mvc
      .perform(
        post("/plans/{id}/budget", id)
          .with(user("owner@budget.test"))
          .with(csrf())
          .param("minimum", "0")
          .param("maximum", "0")
      )
      .andExpect(status().is3xxRedirection());
    assertTrue(plans.findById(id).orElseThrow().getBudget().isEstimated());
    assertFalse(plans.findById(id).orElseThrow().getBudget().isRange());
    mvc
      .perform(
        post("/plans/{id}/budget", id)
          .with(user("editor@budget.test"))
          .with(csrf())
          .param("minimum", "")
          .param("maximum", "")
      )
      .andExpect(status().is3xxRedirection());
    assertFalse(plans.findById(id).orElseThrow().getBudget().isEstimated());
  }

  private User createUser(String email) {
    User user = new User();
    user.setEmail(email);
    user.setPassword("password123");
    user.setRole("USER");
    users.save(user);
    return user;
  }
}
