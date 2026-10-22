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
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@WebIntegrationTest
@Transactional
abstract class PlanPlaceWebFixture {

  @Autowired
  WebApplicationContext context;

  @Autowired
  PlanRepository plans;

  @Autowired
  UserRepository users;

  @Autowired
  PlaceRepository places;

  @Autowired
  PlanPlaceRepository entries;

  MockMvc mvc;
  Plan plan;
  UUID first;
  UUID second;
  String url;

  @BeforeEach
  void setup() {
    mvc =
      MockMvcBuilders
        .webAppContextSetup(context)
        .defaultRequest(
          get("/")
            .with(request -> {
              try {
                context
                  .getBean(org.springframework.web.servlet.handler.HandlerMappingIntrospector.class)
                  .setCache(request);
              } catch (Exception exception) {
                throw new IllegalStateException(exception);
              }
              return request;
            })
        )
        .apply(springSecurity())
        .build();
    User owner = new User();
    owner.setEmail("apl@test.com");
    owner.setPassword("password");
    owner.setRole("USER");
    users.save(owner);
    plan = new Plan();
    plan.setName("Itinerary");
    plan.setAdministrator(owner);
    plans.save(plan);
    first = places.findAll().get(0).getId();
    second = places.findAll().get(1).getId();
    url = "/api/plans/" + plan.getId() + "/places";
  }

  UUID add(UUID placeId) throws Exception {
    mvc
      .perform(
        post(url).param("placeId", placeId.toString()).with(user("apl@test.com")).with(csrf())
      )
      .andExpect(status().isOk())
      .andExpect(jsonValue("/placeId", placeId));
    return entries
      .findByPlanId(plan.getId())
      .stream()
      .filter(e -> e.getPlace().getId().equals(placeId))
      .findFirst()
      .orElseThrow()
      .getId();
  }

  static org.springframework.test.web.servlet.ResultMatcher jsonValue(
    String pointer,
    Object expected
  ) {
    return result ->
      assertEquals(
        String.valueOf(expected),
        new com.fasterxml.jackson.databind.ObjectMapper()
          .readTree(result.getResponse().getContentAsString())
          .at(pointer)
          .asText()
      );
  }

  static org.springframework.test.web.servlet.ResultMatcher jsonMissing(String pointer) {
    return result ->
      assertTrue(
        new com.fasterxml.jackson.databind.ObjectMapper()
          .readTree(result.getResponse().getContentAsString())
          .at(pointer)
          .isMissingNode()
      );
  }
}
