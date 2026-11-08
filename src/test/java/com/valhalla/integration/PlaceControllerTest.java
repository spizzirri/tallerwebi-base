package com.valhalla.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebIntegrationTest
public class PlaceControllerTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    mockMvc =
      MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build();
  }

  @Test
  @WithMockUser
  public void shouldReturnPlacesList() throws Exception {
    mockMvc
      .perform(get("/explore"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/places/list"))
      .andExpect(model().attributeDoesNotExist("places", "categories"));
  }

  /**
   * El filtro por categoria de la URL lo resuelve el JS contra /api/places. El servidor solo tiene
   * que seguir sirviendo la pagina cuando la URL trae el parametro viejo.
   */
  @Test
  @WithMockUser
  public void shouldIgnoreTheLegacyCategoryParam() throws Exception {
    mockMvc
      .perform(get("/explore").param("category", "RESTAURANT"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/places/list"));
  }

  @Test
  @WithMockUser
  public void shouldIgnoreTheLegacySearchParam() throws Exception {
    mockMvc
      .perform(get("/explore").param("search", "Don"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/places/list"));
  }

  @Test
  @WithMockUser
  public void shouldReturnNotFoundForPlaceDetailPage() throws Exception {
    mockMvc.perform(get("/explore/1")).andExpect(status().isNotFound());
  }

  // [MAPS] El panel de detalle de explore trae el link de directions (lo arma el Vue con la
  // direccion del lugar que llega de /api/places). Desde la fase 5 el helper vive en
  // lib/places-map.js, compartido con el detalle del plan y la vista publica: el markup trae
  // el binding y el modulo arma la URL.
  @Test
  @WithMockUser
  public void shouldShowTheMapsDirectionsLinkOnPlaceDetails() throws Exception {
    mockMvc
      .perform(get("/explore"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString(":href=\"directionsUrl(selectedPlace)\"")));

    String module = Files.readString(
      Paths.get("src", "main", "webapp", "resources", "core", "js", "lib", "places-map.js")
    );
    assertTrue(module.contains("maps/dir/?api=1"), "el helper arma el link de directions");
  }

  /**
   * [FLASH] El template de Vue viaja crudo en el HTML: sin v-cloak el navegador pinta el banner de
   * error y los {{selectedPlace.*}} hasta que places-list.js monta. El atributo no sirve
   * sin la regla del layout, y el logo necesita sus dimensiones para no pintarse a 1366 px cuando
   * el CSS compilado todavia no llego.
   */
  @Test
  @WithMockUser
  public void shouldServeTheFirstPaintContractOnTheExplorePage() throws Exception {
    mockMvc
      .perform(get("/explore"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("id=\"explore-app\" v-cloak")))
      .andExpect(content().string(containsString("[v-cloak]")))
      .andExpect(content().string(containsString("display: none !important;")))
      .andExpect(content().string(containsString("width=\"48\" height=\"48\"")))
      // El reveal no puede volver al umbral por longitud: el bloque @theme lo satisfacia antes
      // de que Tailwind compilara.
      .andExpect(content().string(not(containsString("textContent.length > 2000"))));
  }

  // [SHARE] El sidebar de /explore lo pinta Vue despues del load: el boton viaja solo con el
  // link (share-plan.js comparte unicamente la URL; el mensaje no lleva titulo ni descripcion),
  // y el listener delegado cubre el alta tardia.
  @Test
  @WithMockUser
  public void shouldShipTheShareButtonOnTheExplorePlanCards() throws Exception {
    mockMvc
      .perform(get("/explore"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("data-share-plan")))
      .andExpect(
        content().string(containsString(":data-share-url=\"'/plans/' + plan.id + '/public'\""))
      )
      .andExpect(content().string(not(containsString(":data-share-title"))))
      .andExpect(content().string(not(containsString(":data-share-text"))))
      .andExpect(content().string(not(containsString(":data-share-cover"))))
      .andExpect(content().string(containsString("/js/share-plan.js")));
  }

  // [META] El catalogo tambien se comparte por link: mismo par OG/Twitter del layout con URL
  // absoluta y la portada de marca.
  @Test
  @WithMockUser
  public void shouldDeclareTheSharePreviewForTheExploreCatalogue() throws Exception {
    mockMvc
      .perform(get("/explore"))
      .andExpect(status().isOk())
      .andExpect(
        content()
          .string(
            containsString("property=\"og:title\" content=\"Explorar lugares en Buenos Aires\"")
          )
      )
      .andExpect(
        content()
          .string(
            containsString(
              "property=\"og:description\" content=\"Descubrí lugares y armá tu plan perfecto " +
              "con PlanIt.\""
            )
          )
      )
      .andExpect(
        content().string(containsString("property=\"og:url\" content=\"http://localhost/explore\""))
      )
      .andExpect(
        content()
          .string(
            containsString("property=\"og:image\" content=\"http://localhost/images/og-image.png\"")
          )
      );
  }
}
