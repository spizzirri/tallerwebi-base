package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.presentation.shared.LoginRedirects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.HiddenHttpMethodFilter;

/**
 * AC-03: la cadena /admin/** de SecurityConfig es la dueña del acceso a /admin/places*. Ninguna
 * de estas requests llega al PlaceAdminController, así que además del redirect se prueba que no
 * persiste nada.
 */
@WebIntegrationTest
@Transactional
public class PlaceAdminSecurityTest {

  private static final String USER_EMAIL = "user@unlam.edu.ar";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private PlaceRepository placeRepository;

  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    this.mockMvc =
      MockMvcBuilders
        .webAppContextSetup(this.wac)
        .apply(springSecurity())
        .addFilter(new HiddenHttpMethodFilter())
        .build();
  }

  // --- S-01 ---

  @Test
  @WithMockUser(username = USER_EMAIL, roles = { "USER" })
  public void shouldRedirectACommonUserAwayFromThePlacesAbm() throws Exception {
    this.mockMvc.perform(get("/admin/places"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(LoginRedirects.USER_LANDING));
  }

  // --- S-02 ---

  @Test
  public void shouldRedirectAnonymousTrafficAwayFromThePlacesAbm() throws Exception {
    this.mockMvc.perform(get("/admin/places"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrlPattern("**/admin/login"));
  }

  // --- S-03 ---

  @Test
  @WithMockUser(username = USER_EMAIL, roles = { "USER" })
  public void shouldNotPersistAnythingWhenACommonUserPostsToThePlacesAbm() throws Exception {
    this.mockMvc.perform(
        post("/admin/places").with(csrf()).param("name", "Alta Negada").param("category", "CAFE")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(LoginRedirects.USER_LANDING));

    assertThat(placeRepository.findByNameContainingIgnoreCase("Alta Negada"), hasSize(0));

    Place place = placeRepository.findAll().getFirst();
    this.mockMvc.perform(
        post("/admin/places/" + place.getId() + "/delete").with(csrf()).param("_method", "DELETE")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(LoginRedirects.USER_LANDING));

    assertThat(placeRepository.findById(place.getId()).isPresent(), is(true));
  }

  // --- S-04 ---

  @Test
  @WithMockUser(username = USER_EMAIL, roles = { "USER" })
  public void shouldNotLetACommonUserReachThePlaceAdminForms() throws Exception {
    this.mockMvc.perform(get("/admin/places/new"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(LoginRedirects.USER_LANDING));

    Place place = placeRepository.findAll().getFirst();
    this.mockMvc.perform(get("/admin/places/" + place.getId() + "/edit"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(LoginRedirects.USER_LANDING));
  }
}
