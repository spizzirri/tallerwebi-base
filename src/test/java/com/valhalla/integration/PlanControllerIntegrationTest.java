package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.HiddenHttpMethodFilter;

/**
 * End-to-end HTTP coverage of the plan contract against HSQLDB: ownership by user, the generated
 * share code, the form-verb routing and the {@code PlanNotFoundException} redirect.
 *
 * <p>Every negative case asserts the effect — the repository state before and after — not only the
 * redirect, so a handler that swallowed the rejection would still fail here.
 */
@WebIntegrationTest
@Transactional
public class PlanControllerIntegrationTest {

  private static final String OWNER_EMAIL = "dueno@test.com";
  private static final String OTHER_EMAIL = "ajeno@test.com";
  private static final String PLAN_NAME = "Plan de integración";
  private static final String OTHER_PLAN_NAME = "Plan del otro usuario";
  private static final String PUBLIC_PLAN_NAME = "Plan publico compartido";

  /**
   * Where {@code GlobalExceptionHandler} sends a {@code PlanNotFoundException}: the plans listing,
   * never {@code pages/error}, and never a different plan's detail page. The message travels as a
   * query parameter because a {@code redirect:} {@code ModelAndView} carries its model into the
   * target URL — the same shape the {@code UserNotFoundException} handler already produces.
   */
  private static final String NOT_FOUND_REDIRECT = "/plans?error=Plan+not+found";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PlanRepository planRepository;

  @Autowired
  private PlaceRepository placeRepository;

  @Autowired
  private EntityManager entityManager;

  private MockMvc mockMvc;

  private Long ownerId;
  private Long otherId;

  @BeforeEach
  public void setUp() {
    this.mockMvc =
      MockMvcBuilders
        .webAppContextSetup(this.wac)
        .apply(springSecurity())
        .addFilter(new HiddenHttpMethodFilter())
        .build();
    this.ownerId = givenUser(OWNER_EMAIL);
    this.otherId = givenUser(OTHER_EMAIL);
  }

  // --- listado ---

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PLN_070_getPlans_muestraLosPlanesDelDueno() throws Exception {
    // given
    Long planId = givenPlanFor(this.ownerId, PLAN_NAME);

    // when
    MvcResult result =
      this.mockMvc.perform(get("/plans"))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/plans/list"))
        .andExpect(model().attributeExists("plans"))
        .andReturn();

    // then
    List<?> plans = plansOf(result);
    assertThat(plans, hasSize(1));
    assertThat(((Plan) plans.get(0)).getId(), is(equalTo(planId)));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PLN_071_getPlans_noMuestraLosPlanesDeOtroUsuario() throws Exception {
    // given: the plan belongs to somebody else, so it must not reach this user's listing
    givenPlanFor(this.otherId, OTHER_PLAN_NAME);

    // when
    MvcResult result = this.mockMvc.perform(get("/plans")).andExpect(status().isOk()).andReturn();

    // then
    assertThat(plansOf(result), is(empty()));
  }

  @Test
  public void T_PLN_072_getPlansSinSesionRedirige() throws Exception {
    this.mockMvc.perform(get("/plans"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrlPattern("**/auth/login"));
  }

  // --- GET /plans/public [PPV, feed solo con sesion] ---

  @Test
  public void I01_getPlansPublic_anonimoRedirigeALogin() throws Exception {
    // given: sin @WithMockUser, la request es anonima — el feed no es para visitantes

    // when/then
    this.mockMvc.perform(get("/plans/public"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrlPattern("**/auth/login"));
  }

  @Test
  public void I01b_getPlansPublic_logueadoResponde200ConElListado() throws Exception {
    // given
    Long planId = givenPublicPlanFor(this.ownerId, PLAN_NAME);

    // when
    MvcResult result =
      this.mockMvc.perform(get("/plans/public").with(user(OTHER_EMAIL)))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/plans/public-list"))
        .andExpect(model().attributeExists("publicPlans"))
        .andReturn();

    // then
    String html = result.getResponse().getContentAsString();
    assertThat(html, containsString(PLAN_NAME));
    assertThat(html, containsString("href=\"/plans/" + planId + "/public\""));
    // El boton "Usar plan" es parte de la tarjeta, uno por plan listado.
    assertThat(occurrences(html, "Usar plan"), is(1));
  }

  @Test
  public void I02_getPlansPublic_noIncluyeNingunPlanPrivado() throws Exception {
    // given: un plan publico y otro privado, ambos con nombre propio
    givenPublicPlanFor(this.ownerId, PLAN_NAME);
    givenPlanFor(this.otherId, OTHER_PLAN_NAME);

    // when
    MvcResult result =
      this.mockMvc.perform(get("/plans/public").with(user(OTHER_EMAIL)))
        .andExpect(status().isOk())
        .andReturn();

    // then: ni nombre, ni datos, ni cantidad del privado (AC-02)
    String html = result.getResponse().getContentAsString();
    assertThat(html, containsString(PLAN_NAME));
    assertThat(html, not(containsString(OTHER_PLAN_NAME)));
  }

  @Test
  public void I03_getPlansPublic_sinPlanesPublicosMuestraElEstadoVacio() throws Exception {
    // given: solo planes privados, que para este listado es lo mismo que ninguno
    givenPlanFor(this.ownerId, PLAN_NAME);

    // when
    MvcResult result =
      this.mockMvc.perform(get("/plans/public").with(user(OWNER_EMAIL)))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/plans/public-list"))
        .andReturn();

    // then: estado vacio amigable con salida a la creacion (AC-06; quien llega aca ya tiene sesion)
    String html = result.getResponse().getContentAsString();
    assertThat(html, containsString("Todavía no hay planes públicos"));
    assertThat(html, containsString("href=\"/explore\""));
    assertThat(html, not(containsString(PLAN_NAME)));
  }

  @Test
  public void I04_cadaTarjetaLinkeaALaVistaPublicaDelPlan() throws Exception {
    // given
    Long publicId = givenPublicPlanFor(this.ownerId, PLAN_NAME);
    Long anotherId = givenPublicPlanFor(this.ownerId, OTHER_PLAN_NAME);

    // when
    String html =
      this.mockMvc.perform(get("/plans/public").with(user(OTHER_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then (AC-04)
    assertThat(html, containsString("href=\"/plans/" + publicId + "/public\""));
    assertThat(html, containsString("href=\"/plans/" + anotherId + "/public\""));
  }

  @Test
  public void I05_getPlansPublic_muestraEditarSoloEnLosPlanesPropios() throws Exception {
    // given: un plan propio publico y uno ajeno publico
    givenPublicPlanFor(this.ownerId, PLAN_NAME);
    givenPublicPlanFor(this.otherId, OTHER_PLAN_NAME);

    // when: como dueno del primero
    String mio =
      this.mockMvc.perform(get("/plans/public").with(user(OWNER_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    // when: como ajeno a ambos
    String ajeno =
      this.mockMvc.perform(get("/plans/public").with(user("visita@test.com")))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then: "Editar" solo aparece al lado del plan que es tuyo (boton Usar plan esta en todos)
    assertThat(occurrences(oneLine(mio), " Editar "), is(1));
    assertThat(occurrences(oneLine(ajeno), " Editar "), is(0));
    assertThat(occurrences(ajeno, "Usar plan"), is(2));
  }

  @Test
  public void I06_cadaTarjetaMuestraUnCoverVisualConAltDelPlan() throws Exception {
    // given: dos planes publicos (covers distintos porque el id elige uno)
    Long publicId = givenPublicPlanFor(this.ownerId, PLAN_NAME);
    Long anotherId = givenPublicPlanFor(this.otherId, OTHER_PLAN_NAME);

    // when
    String html =
      this.mockMvc.perform(get("/plans/public").with(user("visita@test.com")))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then: un cover por tarjeta, con alt accesible igual al nombre del plan
    assertThat(occurrences(html, "src=\"/images/plans/cover-"), is(2));
    assertThat(occurrences(html, "alt=\"" + PLAN_NAME + "\""), is(1));
    assertThat(occurrences(html, "alt=\"" + OTHER_PLAN_NAME + "\""), is(1));
    // el id determina el cover (floorMod) y la imagen existe y se sirve
    assertThat(
      html,
      containsString("src=\"/images/plans/cover-" + (Math.floorMod(publicId, 6L) + 1) + ".svg\"")
    );
    assertThat(
      html,
      containsString("src=\"/images/plans/cover-" + (Math.floorMod(anotherId, 6L) + 1) + ".svg\"")
    );
    this.mockMvc.perform(get("/images/plans/cover-1.svg")).andExpect(status().isOk());
  }

  // --- POST /plans ---

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PLN_073_postPlans_asignaElDuenoYGeneraElShortCode() throws Exception {
    // when
    MvcResult result =
      this.mockMvc.perform(
          post("/plans")
            .with(csrf())
            .param("name", PLAN_NAME)
            .param("eventDate", "2026-12-31")
            .param("eventTime", "18:30")
        )
        .andExpect(status().is3xxRedirection())
        .andReturn();

    // then
    String location = result.getResponse().getRedirectedUrl();
    assertThat(location, matchesPattern("^/plans/\\d+$"));

    this.entityManager.flush();
    this.entityManager.clear();
    Plan saved = this.planRepository.findById(planIdOf(location)).orElseThrow();
    assertThat(saved.getName(), is(equalTo(PLAN_NAME)));
    assertThat(saved.getEventDate(), is(equalTo(LocalDate.of(2026, 12, 31))));
    assertThat(saved.getEventTime(), is(equalTo(LocalTime.of(18, 30))));
    String detail =
      this.mockMvc.perform(get(location))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    assertThat(detail, containsString("value=\"2026-12-31\""));
    assertThat(detail, containsString("value=\"18:30\""));
    assertThat(detail, containsString(">2026-12-31</span>"));
    assertThat(detail, containsString(">18:30</span>"));
    assertThat(saved.getAdministrator().getId(), is(equalTo(this.ownerId)));
    assertThat(saved.getAdministrator().getEmail(), is(equalTo(OWNER_EMAIL)));
    assertThat(saved.getShortCode(), matchesPattern("[A-Z0-9]{8}"));
    // An unchecked checkbox posts nothing, so visibility defaults to private (AC-04)
    assertThat(saved.getIsPublic(), is(false));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PLN_074_postPlans_guardaIsPublicCuandoVieneMarcado() throws Exception {
    // when
    MvcResult result =
      this.mockMvc.perform(
          post("/plans").with(csrf()).param("name", PLAN_NAME).param("isPublic", "true")
        )
        .andExpect(status().is3xxRedirection())
        .andReturn();

    // then
    Plan saved =
      this.planRepository.findById(planIdOf(result.getResponse().getRedirectedUrl())).orElseThrow();
    assertThat(saved.getIsPublic(), is(true));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PLN_074b_postPlans_guardaLosLugaresEnOrdenPermitiendoRepetirUnoYDejandoVaciosEnNull()
    throws Exception {
    // given
    Long placeId = this.placeRepository.findAll().get(0).getId();

    // when
    MvcResult result =
      this.mockMvc.perform(
          post("/plans")
            .with(csrf())
            .param("name", PLAN_NAME)
            .param("places[0].placeId", placeId.toString())
            .param("places[0].description", "desayuno")
            .param("places[0].visitDate", "2026-12-01")
            .param("places[0].visitTime", "09:30")
            .param("places[1].placeId", placeId.toString())
            .param("places[1].description", "")
            .param("places[1].visitDate", "")
            .param("places[1].visitTime", "")
        )
        .andExpect(status().is3xxRedirection())
        .andReturn();

    // then
    Plan saved =
      this.planRepository.findById(planIdOf(result.getResponse().getRedirectedUrl())).orElseThrow();
    assertThat(saved.getPlanPlaces(), hasSize(2));
    assertThat(saved.getPlanPlaces().get(0).getSortOrder(), is(1));
    assertThat(saved.getPlanPlaces().get(0).getDescription(), is(equalTo("desayuno")));
    assertThat(saved.getPlanPlaces().get(0).getVisitDate(), is(equalTo(LocalDate.of(2026, 12, 1))));
    assertThat(saved.getPlanPlaces().get(0).getVisitTime(), is(equalTo(LocalTime.of(9, 30))));
    assertThat(saved.getPlanPlaces().get(1).getSortOrder(), is(2));
    assertThat(saved.getPlanPlaces().get(1).getVisitDate(), is(nullValue()));
    assertThat(saved.getPlanPlaces().get(1).getVisitTime(), is(nullValue()));
    assertThat(saved.getPlanPlaces().get(1).getPlace().getId(), is(equalTo(placeId)));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PLN_075_postPlans_conNombreVacioRedirigeAExploreYNoCreaNada() throws Exception {
    // when: sin formulario standalone, un POST inválido vuelve a la vista de creación (/explore)
    this.mockMvc.perform(post("/plans").with(csrf()).param("name", ""))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/explore"));

    // then
    assertThat(this.planRepository.findAll(), is(empty()));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PLN_075b_getPlansNew_yaNoEsUnaRuta() throws Exception {
    // La creación de planes vive solo en /explore: GET /plans/new debe dar 404.
    this.mockMvc.perform(get("/plans/new")).andExpect(status().isNotFound());

    // then
    assertThat(this.planRepository.findAll(), is(empty()));
  }

  // --- GET /plans/{id} ---

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PLN_076_getPlansId_cargaElPlanPropio() throws Exception {
    // given
    Long planId = givenPlanFor(this.ownerId, PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setShortCode("test1234");
    plan.setEventDate(LocalDate.of(2026, 12, 1));
    PlanPlace entry = new PlanPlace();
    entry.setPlace(this.placeRepository.findAll().get(0));
    entry.setSortOrder(1);
    plan.addPlanPlace(entry);
    this.planRepository.save(plan);

    // when
    MvcResult result =
      this.mockMvc.perform(get("/plans/" + planId))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/plans/detail"))
        .andExpect(model().attributeExists("plan"))
        .andReturn();

    // then: render real properties and serialize places without the parent or its password.
    String html = result.getResponse().getContentAsString();
    assertThat(html, containsString(OWNER_EMAIL));
    assertThat(html, containsString("test1234"));
    assertThat(html, containsString("2026-12-01"));
    assertThat(html, containsString("\"id\":" + entry.getPlace().getId()));
    assertThat(html, not(containsString("password123")));
    assertThat(html, not(containsString("\"administrator\":")));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PLN_077_getPlansId_inexistenteRedirigeAlListado() throws Exception {
    this.mockMvc.perform(get("/plans/999999"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PLN_078_getPlansId_deOtroUsuarioRedirigeYNoLoBorra() throws Exception {
    // given
    Long planId = givenPlanFor(this.otherId, OTHER_PLAN_NAME);

    // when
    this.mockMvc.perform(get("/plans/" + planId))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));

    // then: the redirect must not have been a delete in disguise
    assertThat(this.planRepository.findById(planId).isPresent(), is(true));
  }

  // --- PUT /plans/{id} ---

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PLN_079_postPlansId_putConservaShortCodeYAdministrator() throws Exception {
    // given
    Long planId = givenPlanFor(this.ownerId, PLAN_NAME);
    String shortCode = this.planRepository.findById(planId).orElseThrow().getShortCode();

    // when
    this.mockMvc.perform(
        post("/plans/" + planId)
          .with(csrf())
          .param("_method", "PUT")
          .param("name", "Plan renombrado")
          .param("description", "Descripcion nueva")
          .param("eventDate", "2026-06-15")
          .param("eventTime", "09:15")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/plans/" + planId));

    // then
    Plan updated = this.planRepository.findById(planId).orElseThrow();
    assertThat(updated.getName(), is(equalTo("Plan renombrado")));
    assertThat(updated.getDescription(), is(equalTo("Descripcion nueva")));
    assertThat(updated.getEventDate(), is(equalTo(LocalDate.of(2026, 6, 15))));
    assertThat(updated.getEventTime(), is(equalTo(LocalTime.of(9, 15))));
    assertThat(updated.getShortCode(), is(equalTo(shortCode)));
    assertThat(updated.getAdministrator().getId(), is(equalTo(this.ownerId)));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PLN_080_postPlansId_putConErroresRedirigeAlDetalleYNoModifica() throws Exception {
    // given
    Long planId = givenPlanFor(this.ownerId, PLAN_NAME);

    // when: sin formulario standalone, un PUT inválido vuelve al detalle del plan
    this.mockMvc.perform(
        post("/plans/" + planId).with(csrf()).param("_method", "PUT").param("name", "")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/plans/" + planId));

    // then
    assertThat(
      this.planRepository.findById(planId).orElseThrow().getName(),
      is(equalTo(PLAN_NAME))
    );
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PLN_081_postPlansId_putDeOtroUsuarioNoModificaNada() throws Exception {
    // given
    Long planId = givenPlanFor(this.otherId, OTHER_PLAN_NAME);
    String shortCode = this.planRepository.findById(planId).orElseThrow().getShortCode();

    // when
    this.mockMvc.perform(
        post("/plans/" + planId).with(csrf()).param("_method", "PUT").param("name", "Hackeado")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));

    // then
    Plan untouched = this.planRepository.findById(planId).orElseThrow();
    assertThat(untouched.getName(), is(equalTo(OTHER_PLAN_NAME)));
    assertThat(untouched.getShortCode(), is(equalTo(shortCode)));
    assertThat(untouched.getAdministrator().getId(), is(equalTo(this.otherId)));
  }

  // --- POST /plans/{id}/delete ---

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PLN_082_postPlansIdDeleteBorraYRedirige() throws Exception {
    // given
    Long planId = givenPlanFor(this.ownerId, PLAN_NAME);
    givenPlanFor(this.ownerId, OTHER_PLAN_NAME);

    // when: a plain form POST, exactly what list.html and detail.html send. No _method, because
    // the filter that would honour it is not registered in the servlet container.
    this.mockMvc.perform(post("/plans/" + planId + "/delete").with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/plans"));

    // then
    assertThat(this.planRepository.findById(planId).isPresent(), is(false));
    assertThat(this.planRepository.findAll(), hasSize(1));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PLN_083_postPlansIdDeleteDeOtroUsuarioNoBorraNada() throws Exception {
    // given
    Long planId = givenPlanFor(this.otherId, OTHER_PLAN_NAME);

    // when
    this.mockMvc.perform(post("/plans/" + planId + "/delete").with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));

    // then
    assertThat(this.planRepository.findById(planId).isPresent(), is(true));
  }

  // --- POST /plans/{id}/clone ---

  @Test
  @WithMockUser(username = OTHER_EMAIL)
  public void T_PLN_084_postPlansIdCloneDePlanPublicoRedirigeALaCopiaQueApareceEnElListado()
    throws Exception {
    // given (I-01): a public plan of another user
    Long planId = givenPublicPlanWithPlace();

    // when
    String location = cloneLocationOf(planId);

    // then: the redirect lands on a different plan, listed as owned by the cloner
    assertThat(location, matchesPattern("^/plans/\\d+$"));
    assertThat(planIdOf(location), is(not(equalTo(planId))));
    MvcResult listing = this.mockMvc.perform(get("/plans")).andExpect(status().isOk()).andReturn();
    assertThat(((Plan) plansOf(listing).get(0)).getId(), is(equalTo(planIdOf(location))));
  }

  @Test
  @WithMockUser(username = OTHER_EMAIL)
  public void T_PLN_085_postPlansIdClone_laCopiaNacePrivadaConShortCodeNuevo() throws Exception {
    // given (I-02)
    Long planId = givenPublicPlanWithPlace();

    // when
    String location = cloneLocationOf(planId);
    this.entityManager.flush();
    this.entityManager.clear();

    // then
    Plan copy = this.planRepository.findById(planIdOf(location)).orElseThrow();
    assertThat(copy.getShortCode(), matchesPattern("[A-Z0-9]{8}"));
    assertThat(copy.getShortCode(), is(not(equalTo("ORIG1234"))));
    assertThat(copy.getIsPublic(), is(false));
    assertThat(copy.getAdministrator().getId(), is(equalTo(this.otherId)));
    assertThat(copy.getAdministrator().getEmail(), is(equalTo(OTHER_EMAIL)));
  }

  @Test
  @WithMockUser(username = OTHER_EMAIL)
  public void T_PLN_086_getPlansIdDeLaCopiaRenderizaElMismoItinerario() throws Exception {
    // given (I-03)
    Long planId = givenPublicPlanWithPlace();
    Long placeId = this.placeRepository.findAll().get(0).getId();
    String location = cloneLocationOf(planId);
    this.entityManager.flush();
    this.entityManager.clear();
    Plan copy = this.planRepository.findById(planIdOf(location)).orElseThrow();
    assertThat(copy.getPlanPlaces(), hasSize(1));
    assertThat(copy.getPlanPlaces().get(0).getPlace().getId(), is(equalTo(placeId)));
    assertThat(copy.getPlanPlaces().get(0).getDescription(), is(equalTo("desayuno")));

    // when
    String html =
      this.mockMvc.perform(get(location))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then: the clone's detail renders the same place and visit data as the original
    assertThat(html, containsString("desayuno"));
    assertThat(html, containsString("\"id\":" + placeId));
    assertThat(html, containsString("\"visitDate\":\"2026-12-01\""));
  }

  @Test
  @WithMockUser(username = OTHER_EMAIL)
  public void T_PLN_087_postPlansIdCloneDePlanPrivadoAjenoRespondeComoIdInexistente()
    throws Exception {
    // given (I-04): private plan of the owner, the cloner is neither administrator nor participant
    Long planId = givenPlanFor(this.ownerId, OTHER_PLAN_NAME);

    // when: cloning it answers exactly like cloning a missing id
    this.mockMvc.perform(post("/plans/" + planId + "/clone").with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    this.mockMvc.perform(post("/plans/999999/clone").with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));

    // then: no copy was created
    assertThat(this.planRepository.findAll(), hasSize(1));
  }

  @Test
  @WithMockUser(username = OTHER_EMAIL)
  public void T_PLN_088_postPlansIdClone_dejaElOriginalIntacto() throws Exception {
    // given (I-05): public plan with a participant and an itinerary
    Long planId = givenPublicPlanWithPlace();
    Long participantId = givenUser("participant@test.com");
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.getParticipants().add(this.userRepository.findById(participantId).orElseThrow());
    this.planRepository.save(plan);

    // when
    cloneLocationOf(planId);
    this.entityManager.flush();
    this.entityManager.clear();

    // then: code, visibility, owner, participants and itinerary all as before
    Plan original = this.planRepository.findById(planId).orElseThrow();
    assertThat(original.getShortCode(), is(equalTo("ORIG1234")));
    assertThat(original.getIsPublic(), is(true));
    assertThat(original.getAdministrator().getId(), is(equalTo(this.ownerId)));
    assertThat(original.getParticipants(), hasSize(1));
    assertThat(original.getPlanPlaces(), hasSize(1));
  }

  // --- AC-12: el CTA "Clonar plan" de la vista publica ---

  @Test
  public void T_CLN_001_getPlansIdPublic_conSesionElCtaDeClonarApuntaAlEndpointDeClonar()
    throws Exception {
    // given: un plan publico ajeno con itinerario
    Long planId = givenPublicPlanWithPlace();

    // when
    String html =
      this.mockMvc.perform(get("/plans/" + planId + "/public").with(user(OTHER_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then (AC-12): el form de clonar existe y postea al endpoint de CLO, no a otro lado
    assertThat(html, containsString("Clonar plan"));
    assertThat(html, containsString("action=\"/plans/" + planId + "/clone\""));
    assertThat(html, containsString("name=\"_csrf\""));
  }

  @Test
  public void T_CLN_002_getPlansIdPublic_sinSesionElCtaDeClonarLlevaAlLoginConservandoLaVuelta()
    throws Exception {
    // given
    Long planId = givenPublicPlanWithPlace();

    // when: anonimo abre la vista publica
    String html =
      this.mockMvc.perform(get("/plans/" + planId + "/public"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then (AC-12): el CTA no ejecuta clone, lleva al login con redirect de vuelta al plan.
    // El parametro viaja url-encoded segun Thymeleaf, por eso el ternario / %2F.
    assertThat(html, containsString("Inicia sesion para unirte o clonar"));
    assertThat(html, not(containsString("action=\"/plans/" + planId + "/clone\"")));
    assertThat(
      html,
      matchesPattern(
        "(?s).*href=\"/auth/login\\?redirect=[^\"]*" + planId + "(/|%2F)public[^\"]*\".*"
      )
    );
  }

  @Test
  @WithMockUser(username = OTHER_EMAIL)
  public void participantCanJoinReadAndLeaveWithoutDeletingPlan() throws Exception {
    Long planId = givenPlanFor(this.ownerId, PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setShortCode("JOIN1234");
    plan.setDescription("Recorrido con amigos");
    plan.setEventDate(LocalDate.of(2026, 12, 31));
    plan.setEventTime(LocalTime.of(18, 30));
    Long remainingUserId = givenUser("remaining@test.com");
    plan.getParticipants().add(this.userRepository.findById(remainingUserId).orElseThrow());
    PlanPlace entry = new PlanPlace();
    entry.setPlace(this.placeRepository.findAll().get(0));
    plan.addPlanPlace(entry);
    this.planRepository.save(plan);

    this.mockMvc.perform(post("/plans/join").with(csrf()).param("shortCode", " join1234 "))
      .andExpect(redirectedUrl("/plans/" + planId));
    this.mockMvc.perform(post("/plans/join").with(csrf()).param("shortCode", "JOIN1234"))
      .andExpect(redirectedUrl("/plans/" + planId));
    this.entityManager.flush();
    this.entityManager.clear();
    assertThat(this.planRepository.findById(planId).orElseThrow().getParticipants(), hasSize(2));

    MvcResult detail =
      this.mockMvc.perform(get("/plans/" + planId)).andExpect(status().isOk()).andReturn();
    String html = detail.getResponse().getContentAsString();
    String summary = html.substring(
      html.indexOf("id=\"plan-summary\""),
      html.indexOf("<section", html.indexOf("id=\"plan-summary\""))
    );
    assertThat(summary, containsString("Recorrido con amigos"));
    assertThat(summary, containsString(">2026-12-31</span>"));
    assertThat(summary, containsString(">18:30</span>"));
    assertThat(html, containsString("Salir del plan"));
    assertThat(html, not(containsString("Eliminar plan")));
    assertThat(html, not(containsString("Editar plan")));
    assertThat(html, not(containsString("Agregar lugar al plan")));
    assertThat(html, not(containsString("password123")));
    this.mockMvc.perform(get("/api/plans/" + planId + "/places")).andExpect(status().isOk());
    MvcResult listing = this.mockMvc.perform(get("/plans")).andExpect(status().isOk()).andReturn();
    assertThat((List<?>) listing.getModelAndView().getModel().get("participantPlans"), hasSize(1));

    this.mockMvc.perform(post("/plans/" + planId + "/leave").with(csrf()))
      .andExpect(redirectedUrl("/plans"));
    this.entityManager.flush();
    this.entityManager.clear();
    Plan remaining = this.planRepository.findById(planId).orElseThrow();
    assertThat(remaining.getParticipants(), hasSize(1));
    assertThat(remaining.isParticipant("remaining@test.com"), is(true));
    assertThat(remaining.isParticipant(OTHER_EMAIL), is(false));
    assertThat(remaining.getPlanPlaces(), hasSize(1));
    assertThat(this.userRepository.findById(this.otherId).isPresent(), is(true));
    assertThat(this.planRepository.findByParticipantsEmail(OTHER_EMAIL), is(empty()));
    this.mockMvc.perform(get("/plans/" + planId)).andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    this.mockMvc.perform(get("/api/plans/" + planId + "/places")).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = OTHER_EMAIL)
  public void participantCannotEditOrDeleteAndCsrfProtectsLeaving() throws Exception {
    Long planId = givenPlanFor(this.ownerId, PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.getParticipants().add(this.userRepository.findById(this.otherId).orElseThrow());
    this.planRepository.save(plan);
    this.mockMvc.perform(post("/plans/" + planId + "/delete").with(csrf()))
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    this.mockMvc.perform(post("/plans/" + planId).with(csrf()).param("name", "Changed"))
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    this.mockMvc.perform(post("/plans/" + planId + "/leave")).andExpect(status().isForbidden());
    this.mockMvc.perform(post("/plans/join").param("shortCode", "anything"))
      .andExpect(status().isForbidden());
    assertThat(plan.getName(), is(PLAN_NAME));
    assertThat(plan.getParticipants(), hasSize(1));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void administratorCanDeleteButCannotLeave() throws Exception {
    Long planId = givenPlanFor(this.ownerId, PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setShortCode("JOIN1234");
    plan.getParticipants().add(this.userRepository.findById(this.otherId).orElseThrow());
    this.planRepository.save(plan);
    this.mockMvc.perform(post("/plans/join").with(csrf()).param("shortCode", "JOIN1234"))
      .andExpect(redirectedUrl("/plans/" + planId));
    assertThat(plan.getParticipants(), hasSize(1));
    String html =
      this.mockMvc.perform(get("/plans/" + planId))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    assertThat(html, containsString("Eliminar plan"));
    assertThat(html, not(containsString("Salir del plan")));
    this.mockMvc.perform(post("/plans/" + planId + "/leave").with(csrf()))
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    this.mockMvc.perform(post("/plans/" + planId + "/delete").with(csrf()))
      .andExpect(redirectedUrl("/plans"));
    this.entityManager.flush();
    this.entityManager.clear();
    assertThat(this.planRepository.findById(planId).isPresent(), is(false));
    assertThat(this.userRepository.findById(this.otherId).isPresent(), is(true));
  }

  @Test
  @WithMockUser(username = OTHER_EMAIL)
  public void nonParticipantCannotLeaveAndInvalidCodeDoesNotJoin() throws Exception {
    Long planId = givenPlanFor(this.ownerId, PLAN_NAME);
    this.mockMvc.perform(post("/plans/" + planId + "/leave").with(csrf()))
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    for (String code : List.of("", "UNKNOWN")) {
      this.mockMvc.perform(post("/plans/join").with(csrf()).param("shortCode", code))
        .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    }
    this.mockMvc.perform(post("/plans/" + planId + "/leave").with(csrf()).with(user(OWNER_EMAIL)))
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    assertThat(this.planRepository.findById(planId).orElseThrow().getParticipants(), is(empty()));
  }

  @Test
  public void T_PVP_003_getPlansIdPublic_conPlanPublicoDevuelve200ConLosDatos() throws Exception {
    Long planId = givenPlanFor(this.ownerId, PUBLIC_PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setIsPublic(true);
    plan.setDescription("Descripcion publica");
    this.planRepository.save(plan);

    String html =
      this.mockMvc.perform(get("/plans/" + planId + "/public"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(html, containsString(PUBLIC_PLAN_NAME));
    assertThat(html, containsString("Descripcion publica"));
    // AC-03: quien lo creó, visible en la vista pública (el fixture nombra al owner "Test User")
    assertThat(html, containsString("Creado por"));
    assertThat(html, containsString("Test User"));
    // AC-07: el anónimo no ejecuta join/clone: el CTA va al login conservando la vuelta al plan
    assertThat(
      html,
      matchesPattern("(?s).*href=\"/auth/login\\?redirect=[^\"]*" + planId + "[^\"]*\".*")
    );
  }

  @Test
  public void T_PVP_004_getPlansIdPublic_conPlanPrivadoRespondeIgualQueUnoInexistente()
    throws Exception {
    Long planId = givenPlanFor(this.ownerId, OTHER_PLAN_NAME);

    this.mockMvc.perform(get("/plans/" + planId + "/public"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));

    this.mockMvc.perform(get("/plans/999999/public"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
  }

  @Test
  public void T_PVP_005_getPlansIdPublic_sinSesionNoRedirigeALogin() throws Exception {
    Long planId = givenPlanFor(this.ownerId, PUBLIC_PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setIsPublic(true);
    this.planRepository.save(plan);

    this.mockMvc.perform(get("/plans/" + planId + "/public")).andExpect(status().isOk());
  }

  @Test
  public void T_PVP_006_getPlansIdPublic_conSesionDeNoParticipanteMuestraUnirseYClonar()
    throws Exception {
    Long planId = givenPlanFor(this.ownerId, PUBLIC_PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setIsPublic(true);
    this.planRepository.save(plan);

    this.mockMvc.perform(get("/plans/" + planId + "/public").with(user(OTHER_EMAIL)))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("Unirme al plan")))
      .andExpect(content().string(containsString("Clonar plan")));
  }

  @Test
  public void T_PVP_006_getPlansIdPublic_conSesionDeParticipanteNoMuestraUnirse() throws Exception {
    Long planId = givenPlanFor(this.ownerId, PUBLIC_PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setIsPublic(true);
    plan.getParticipants().add(this.userRepository.findById(this.otherId).orElseThrow());
    this.planRepository.save(plan);

    this.mockMvc.perform(get("/plans/" + planId + "/public").with(user(OTHER_EMAIL)))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("Ver mi plan")))
      .andExpect(content().string(not(containsString("Unirme al plan"))));
  }

  @Test
  public void T_PVP_014_getPlansIdPublic_muestraElItinerarioConLosLugaresDelPlan()
    throws Exception {
    // given
    Long planId = givenPlanFor(this.ownerId, PUBLIC_PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setIsPublic(true);
    List<Place> catalog = this.placeRepository.findAll();
    PlanPlace first = new PlanPlace();
    first.setPlace(catalog.get(0));
    first.setSortOrder(1);
    first.setDescription("Parada uno");
    PlanPlace second = new PlanPlace();
    second.setPlace(catalog.get(1));
    second.setSortOrder(2);
    second.setDescription("Parada dos");
    plan.addPlanPlace(first);
    plan.addPlanPlace(second);
    this.planRepository.save(plan);

    // when
    String html =
      this.mockMvc.perform(get("/plans/" + planId + "/public"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then: cada lugar del plan, y en el orden de su sortOrder
    assertThat(html, containsString(catalog.get(0).getName()));
    assertThat(html, containsString(catalog.get(1).getName()));
    assertThat(html, containsString("Parada uno"));
    assertThat(html, containsString("Parada dos"));
    assertThat(html.indexOf("Parada uno") < html.indexOf("Parada dos"), is(true));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PVP_015_getPlansIdPublic_noRenderizaFormularioDeEdicionNiEliminarPlan()
    throws Exception {
    // given: hasta el administrador con sesión ve una vista read-only
    Long planId = givenPlanFor(this.ownerId, PUBLIC_PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setIsPublic(true);
    this.planRepository.save(plan);

    // when
    String html =
      this.mockMvc.perform(get("/plans/" + planId + "/public"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then
    assertThat(html, containsString("Ver mi plan"));
    assertThat(html, not(containsString("Eliminar plan")));
    assertThat(html, not(containsString("Salir del plan")));
    assertThat(html, not(containsString("Editar plan")));
    assertThat(html, not(containsString("Agregar lugar al plan")));
  }

  // --- fixtures ---

  private Long givenUser(String email) {
    User user = new User();
    user.setEmail(email);
    user.setPassword("password123");
    user.setRole("USER");
    user.setFirstName("Test");
    user.setLastName("User");
    user.activate();
    this.userRepository.save(user);
    return this.userRepository.findByEmail(email).orElseThrow().getId();
  }

  private Long givenPlanFor(Long administratorId, String name) {
    Plan plan = new Plan();
    plan.setName(name);
    plan.setAdministrator(this.userRepository.findById(administratorId).orElseThrow());
    return this.planRepository.save(plan).getId();
  }

  /** [PPV] Un plan visible para el listado publico. */
  private Long givenPublicPlanFor(Long administratorId, String name) {
    Long id = givenPlanFor(administratorId, name);
    Plan plan = this.planRepository.findById(id).orElseThrow();
    plan.setIsPublic(true);
    return this.planRepository.save(plan).getId();
  }

  private static int occurrences(String haystack, String needle) {
    int count = 0;
    for (
      int at = haystack.indexOf(needle);
      at >= 0;
      at = haystack.indexOf(needle, at + needle.length())
    ) {
      count++;
    }
    return count;
  }

  /** Colapsa el whitespace del HTML renderizado para poder contar texto con saltos de línea. */
  private static String oneLine(String html) {
    return html.replaceAll("\\s+", " ");
  }

  /** A public plan of the owner: itinerary entry included, the fixture the clone tests need. */
  private Long givenPublicPlanWithPlace() {
    Long planId = givenPlanFor(this.ownerId, PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setDescription("Plan original");
    plan.setShortCode("ORIG1234");
    plan.setEventDate(LocalDate.of(2026, 12, 31));
    plan.setEventTime(LocalTime.of(18, 30));
    plan.setIsPublic(true);
    PlanPlace entry = new PlanPlace();
    entry.setPlace(this.placeRepository.findAll().get(0));
    entry.setDescription("desayuno");
    entry.setVisitDate(LocalDate.of(2026, 12, 1));
    entry.setVisitTime(LocalTime.of(9, 30));
    entry.setSortOrder(1);
    plan.addPlanPlace(entry);
    return this.planRepository.save(plan).getId();
  }

  private String cloneLocationOf(Long planId) throws Exception {
    return this.mockMvc.perform(post("/plans/" + planId + "/clone").with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andReturn()
      .getResponse()
      .getRedirectedUrl();
  }

  private static List<?> plansOf(MvcResult result) {
    return (List<?>) result.getModelAndView().getModel().get("plans");
  }

  private static Long planIdOf(String redirectUrl) {
    return Long.valueOf(redirectUrl.substring("/plans/".length()));
  }
}
