package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valhalla.domain.place.Place;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * I-13/I-14 (AC-02): /explore es una shell server-rendered + fetch client-side de /api/places
 * (pages/places/list no tiene th:each: las cards las arma Vue con v-for). El seam que convierte
 * los datos en un &lt;img&gt; es la expresión de fallback server-renderizada en el template:
 * {@code :src="place.imageUrl || '/images/place-placeholder.svg'"}. Por eso AC-02 se prueba en
 * dos patas: la API tiene que transportar el imageUrl (o el null) del place creado por el ABM, y
 * la vista tiene que llevar la expresión que vuelve ese dato un img src con fallback al placeholder.
 */
@WebIntegrationTest
@Transactional
public class PlaceImageTest {

  private static final String IMAGE_SRC_FALLBACK =
    ":src=\"place.imageUrl || '/images/place-placeholder.svg'\"";

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
  }

  // --- I-13 ---

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldCarryTheAbmImageUrlThroughTheApiThatFeedsExplore() throws Exception {
    givenPlaceThroughTheAbm("Foto Cargada", "https://example.com/foto.png");

    Place place = placeNamed("Foto Cargada", placesFrom(get("/api/places")));
    assertThat(place.getImageUrl(), is("https://example.com/foto.png"));

    this.mockMvc.perform(get("/explore"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString(IMAGE_SRC_FALLBACK)));
  }

  // --- I-14 ---

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldFallBackToThePlaceholderWhenTheAbmStoredNoImageUrl() throws Exception {
    givenPlaceThroughTheAbm("Sin Foto", null);

    Place place = placeNamed("Sin Foto", placesFrom(get("/api/places")));
    assertThat(place.getImageUrl(), is(nullValue()));

    this.mockMvc.perform(get("/explore"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString(IMAGE_SRC_FALLBACK)))
      .andExpect(content().string(containsString("/images/place-placeholder.svg")));
  }

  // --- helpers ---

  private void givenPlaceThroughTheAbm(String name, String imageUrl) throws Exception {
    var request = post("/admin/places").with(csrf()).param("name", name).param("category", "OTHER");
    if (imageUrl != null) {
      request = request.param("imageUrl", imageUrl);
    }
    this.mockMvc.perform(request)
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/places"));
  }

  private Place placeNamed(String name, List<Place> places) {
    return places.stream().filter(place -> place.getName().equals(name)).findFirst().orElseThrow();
  }

  /** GET a la API y parsea el array de lugares con Jackson (mismo seam que PlaceRestControllerTest). */
  private List<Place> placesFrom(MockHttpServletRequestBuilder request) throws Exception {
    String body =
      this.mockMvc.perform(request)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();
    return Arrays.asList(new ObjectMapper().readValue(body, Place[].class));
  }
}
