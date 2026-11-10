package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.place.PlaceService;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlaceService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import com.valhalla.presentation.place.EditPlaceRequest;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.HiddenHttpMethodFilter;

/**
 * [PSHARE] La ficha publica de un lugar: T-PSHARE-001 (200 sin sesion con las etiquetas OG),
 * T-PSHARE-002 (404 para un id inexistente, sin el redirect del ABM) y PSHARE-007 (el sitemap
 * con los planes publicos junto a los lugares).
 *
 * <p>Los lugares y planes viven en DB real (HSQLDB) como en el resto de la suite: {@code
 * @Transactional} revierte cada test. Sin {@code @WithMockUser} a proposito: la ficha y el
 * sitemap tienen que responderle al anonimo, que es como llega un link compartido.
 */
@WebIntegrationTest
@Transactional
public class PlaceShareIntegrationTest {

  private static final String OWNER_EMAIL = "pshare.owner@test.com";
  private static final String PLACE_NAME = "Cafe de Prueba";
  private static final String PLAN_NAME = "Plan que incluye Cafe de Prueba";
  /** Mas de 200 caracteres: la descripcion del preview se corta en 199 + puntos. */
  private static final String LONG_DESCRIPTION = "Descripcion larga. ".repeat(20);
  private static final String MISSING_PLACE_ID = "0192f0a1-0000-7000-8000-000000000999";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private PlaceService placeService;

  @Autowired
  private PlanService planService;

  @Autowired
  private PlanPlaceService planPlaceService;

  @Autowired
  private UserRepository userRepository;

  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    this.mockMvc =
      MockMvcBuilders
        .webAppContextSetup(this.wac)
        .apply(springSecurity())
        .addFilter((Filter) new HiddenHttpMethodFilter())
        .build();
  }

  // --- T-PSHARE-001: 200 sin sesion + etiquetas OG propias del lugar ---

  @Test
  public void placeSheetIsPublicAndDeclaresItsOwnSharePreview() throws Exception {
    Place place = givenPlaceWithPhotoAndCoordinates();
    Plan plan = givenPublicPlanIncluding(place);

    String html =
      this.mockMvc.perform(get("/places/" + place.getId()))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/places/detail"))
        .andReturn()
        .getResponse()
        .getContentAsString();

    // [PSHARE-002] Vista previa propia: titulo, url absoluta y la FOTO del lugar como og:image.
    assertThat(html, containsString("property=\"og:title\" content=\"" + PLACE_NAME + "\""));
    assertThat(
      html,
      containsString(
        "property=\"og:url\" content=\"http://localhost/places/" + place.getId() + "\""
      )
    );
    assertThat(
      html,
      containsString(
        "property=\"og:image\" content=\"http://localhost/images/place-placeholder.svg\""
      )
    );
    assertThat(
      html,
      containsString(
        "property=\"og:description\" content=\"" + LONG_DESCRIPTION.substring(0, 199) + "…\""
      )
    );
    assertThat(html, containsString("name=\"twitter:card\" content=\"summary_large_image\""));

    // [PSHARE-003] El boton de compartir reusa share-plan.js con la URL de la ficha.
    assertThat(html, containsString("data-share-plan"));
    assertThat(html, containsString("data-share-url=\"/places/" + place.getId() + "\""));
    assertThat(html, containsString("/js/share-plan.js"));

    // [PSHARE-001] La ficha pinta los datos del lugar, su mapa y los planes publicos que lo
    // incluyen, linkeados a la vista publica del plan.
    assertThat(html, containsString("Av. Siempreviva 742"));
    assertThat(html, containsString("id=\"place-map\""));
    assertThat(html, containsString(PLAN_NAME));
    assertThat(html, containsString("href=\"/plans/" + plan.getId() + "/public\""));

    // [PSHARE-005] Como llegar a Google Maps con las coordenadas del lugar.
    assertThat(html, containsString("destination=-34.5895,-58.4095"));
  }

  // --- T-PSHARE-002: 404 para un lugar inexistente ---

  @Test
  public void unknownPlaceAnswers404InsteadOfARedirect() throws Exception {
    this.mockMvc.perform(get("/places/" + MISSING_PLACE_ID))
      .andExpect(status().isNotFound())
      .andExpect(view().name("pages/error"));
  }

  /**
   * El otro lado de las etiquetas OG: un lugar sin descripcion y sin foto no declara un og:image
   * roto (cae en la imagen de marca) y su descripcion sale de un texto listo, no vacio. Cubre la
   * rama en blanco del preview que T-PSHARE-001 (descripcion larga, con foto) no alcanza.
   */
  @Test
  public void blankDescriptionFallsBackToThePreviewTextAndTheBrandImage() throws Exception {
    EditPlaceRequest request = placeRequest("Lugar Sin Descripcion");
    Place place = placeService.create(request);

    String html =
      this.mockMvc.perform(get("/places/" + place.getId()))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(
      html,
      containsString(
        "property=\"og:description\" content=\"Lugar Sin Descripcion es un lugar" +
        " para descubrir en PlanIt.\""
      )
    );
    assertThat(
      html,
      containsString("property=\"og:image\" content=\"http://localhost/images/og-image.png\"")
    );
  }

  // --- PSHARE-007: el sitemap suma los lugares junto a los planes ---

  @Test
  public void sitemapListsThePublicPlansAndThePlaces() throws Exception {
    Place place = givenPlaceWithPhotoAndCoordinates();
    Plan plan = givenPublicPlanIncluding(place);

    String xml =
      this.mockMvc.perform(get("/sitemap.xml"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(xml, startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
    assertThat(
      xml,
      containsString("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">")
    );
    // La home, la ficha del lugar y el plan publico que lo incluye: URLs absolutas.
    assertThat(xml, containsString("<loc>http://localhost/</loc>"));
    assertThat(xml, containsString("<loc>http://localhost/places/" + place.getId() + "</loc>"));
    assertThat(
      xml,
      containsString("<loc>http://localhost/plans/" + plan.getId() + "/public</loc>")
    );
  }

  // --- helpers ---

  private EditPlaceRequest placeRequest(String name) {
    EditPlaceRequest request = new EditPlaceRequest();
    request.setName(name);
    request.setCategory(PlaceCategory.CAFE);
    return request;
  }

  /** Un lugar con foto y coordenadas: el caso completo de la ficha (mapa, og:image, Maps). */
  private Place givenPlaceWithPhotoAndCoordinates() {
    EditPlaceRequest request = placeRequest(PLACE_NAME);
    request.setDescription(LONG_DESCRIPTION);
    request.setAddress("Av. Siempreviva 742");
    request.setLatitude(-34.5895);
    request.setLongitude(-58.4095);
    request.setImageUrl("/images/place-placeholder.svg");
    return placeService.create(request);
  }

  /** Un plan PUBLICO del dueno que contiene el lugar, como los que lista la ficha. */
  private Plan givenPublicPlanIncluding(Place place) {
    User owner = new User();
    owner.setEmail(OWNER_EMAIL);
    owner.setPassword("password123");
    owner.setRole("USER");
    owner.setFirstName("Pshare");
    owner.setLastName("Owner");
    owner.setActive(true);
    userRepository.save(owner);

    Plan plan = new Plan();
    plan.setName(PLAN_NAME);
    plan.setIsPublic(true);
    Plan created = planService.createPlan(plan, OWNER_EMAIL);
    planPlaceService.addPlaceToPlan(created.getId(), place.getId(), OWNER_EMAIL);
    return created;
  }
}
