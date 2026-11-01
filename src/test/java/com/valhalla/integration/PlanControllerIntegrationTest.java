package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.TransportMode;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
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

  /** Forma canónica de UUID: el path variable exige parseo UUID, un id numérico ya no llega. */
  private static final String UUID_PATTERN = "^/plans/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-.*$";

  /** Un id bien formado que nadie creó: mismo camino que cualquier plan inexistente. */
  private static final UUID MISSING_PLAN_ID = UUID.fromString(
    "00000000-0000-0000-0000-000000000000"
  );

  /** [SCH] Configuracion del itinerario que usan los tests de derivacion. */
  private static final LocalDate EVENT_DATE = LocalDate.of(2026, 6, 13);
  private static final LocalTime EVENT_TIME = LocalTime.of(10, 0);
  private static final LocalTime DAY_END = LocalTime.of(23, 0);

  /** 90 minutos de visita + 5 de traslado entre dos paradas pegadas. */
  private static final LocalTime SECOND_STOP_START = LocalTime.of(11, 35);
  private static final int MUSEUM_DURATION_MINUTES = 90;

  /** Coordenadas de los museos de prueba: iguales entre si, para fijar el piso del traslado. */
  private static final double LATITUDE = -34.6;
  private static final double LONGITUDE = -58.4;

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

  private UUID ownerId;
  private UUID otherId;

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

  @Test
  public void permissionsPersistOnCreationAndSettingsAreOwnerOnly() throws Exception {
    MvcResult result = mockMvc
      .perform(
        post("/plans")
          .with(user(OWNER_EMAIL))
          .with(csrf())
          .param("name", "Plan con permisos")
          .param("eventDate", "2026-12-31")
          .param("settings.inviteInternal", "OWNER")
          .param("settings.inviteByEmail", "EDITORS")
          .param("settings.handleRequests", "EDITORS")
      )
      .andExpect(status().is3xxRedirection())
      .andReturn();
    UUID id = UUID.fromString(
      result.getResponse().getRedirectedUrl().substring("/plans/".length())
    );
    entityManager.flush();
    entityManager.clear();
    Plan plan = planRepository.findById(id).orElseThrow();
    assertThat(plan.getSettings().getInviteByEmail().name(), is("EDITORS"));
    assertThat(plan.getSettings().getInviteInternal().name(), is("OWNER"));
    assertThat(plan.getSettings().getHandleRequests().name(), is("EDITORS"));
    mockMvc
      .perform(get("/plans/" + id + "/settings").with(user(OWNER_EMAIL)))
      .andExpect(redirectedUrl("/plans/" + id + "#config-roles"));
    mockMvc
      .perform(get("/plans/" + id).with(user(OWNER_EMAIL)))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("id=\"configuracion\"")))
      .andExpect(content().string(containsString("id=\"permission-inviteInternal\"")))
      .andExpect(content().string(containsString("id=\"permission-inviteByEmail\"")))
      .andExpect(content().string(containsString("id=\"permission-handleRequests\"")))
      .andExpect(content().string(not(containsString("name=\"shareLink\""))));
    mockMvc
      .perform(
        post("/plans/" + id + "/settings")
          .with(user(OTHER_EMAIL))
          .with(csrf())
          .param("handleRequests", "OWNER")
          .param("inviteInternal", "PARTICIPANTS")
          .param("inviteByEmail", "PARTICIPANTS")
      )
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    assertThat(plan.getSettings().getInviteInternal().name(), is("OWNER"));
  }

  @Test
  public void invitationLinkRequiresRecipientAndCsrfAndGetDoesNotAccept() throws Exception {
    UUID id = givenPlanFor(ownerId, PLAN_NAME);
    mockMvc
      .perform(
        post("/plans/" + id + "/participants/invite")
          .with(user(OWNER_EMAIL))
          .with(csrf())
          .param("participantEmail", OTHER_EMAIL)
          .param("channel", "email")
      )
      .andExpect(redirectedUrl("/plans/" + id));
    Plan plan = planRepository.findById(id).orElseThrow();
    String token = plan.getInvitations().getFirst().getToken();
    String url = "/plans/" + id + "/invitations/" + token + "/accept";
    mockMvc.perform(get(url)).andExpect(redirectedUrl("/auth/login?redirect=" + url));
    mockMvc
      .perform(get(url).with(user(OTHER_EMAIL)))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/plans/accept-invitation"));
    assertThat(plan.isParticipant(OTHER_EMAIL), is(false));
    mockMvc.perform(post(url).with(user(OTHER_EMAIL))).andExpect(status().isForbidden());
    mockMvc
      .perform(post(url).with(user(OWNER_EMAIL)).with(csrf()))
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    assertThat(plan.isParticipant(OTHER_EMAIL), is(false));
    mockMvc
      .perform(post(url).with(user(OTHER_EMAIL)).with(csrf()))
      .andExpect(redirectedUrl("/plans/" + id));
    assertThat(plan.isParticipant(OTHER_EMAIL), is(true));
    mockMvc
      .perform(post(url).with(user(OTHER_EMAIL)).with(csrf()))
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
  }

  @Test
  public void exploreRendersPermissionFieldsAndNoShareLink() throws Exception {
    mockMvc
      .perform(get("/explore").with(user(OWNER_EMAIL)))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("settings.inviteInternal")))
      .andExpect(content().string(containsString("settings.inviteByEmail")))
      .andExpect(content().string(containsString("settings.handleRequests")))
      .andExpect(content().string(not(containsString("settings.shareLink"))))
      .andExpect(content().string(not(containsString("Compartir link"))));
  }

  @Test
  public void T_PLN_B05_detailSettingsPersistAndAreOwnerOnly() throws Exception {
    UUID id = givenPlanFor(ownerId, PLAN_NAME);
    Plan plan = planRepository.findById(id).orElseThrow();
    User editor = userRepository.findById(otherId).orElseThrow();
    plan.getParticipants().add(editor);
    plan.getEditors().add(editor);
    planRepository.save(plan);

    mockMvc
      .perform(
        post("/plans/" + id + "/settings")
          .with(user(OWNER_EMAIL))
          .with(csrf())
          .param("handleRequests", "EDITORS")
          .param("inviteInternal", "OWNER")
          .param("inviteByEmail", "EDITORS")
      )
      .andExpect(redirectedUrl("/plans/" + id + "#config-roles"))
      .andExpect(flash().attribute("settingsSaved", "Configuración guardada"));

    entityManager.flush();
    entityManager.clear();
    Plan saved = planRepository.findById(id).orElseThrow();
    assertThat(saved.getSettings().getHandleRequests().name(), is("EDITORS"));
    assertThat(saved.getSettings().getInviteInternal().name(), is("OWNER"));
    assertThat(saved.getSettings().getInviteByEmail().name(), is("EDITORS"));

    String ownerHtml = mockMvc
      .perform(get("/plans/" + id).with(user(OWNER_EMAIL)))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
    assertThat(ownerHtml, containsString("id=\"configuracion\""));
    assertThat(ownerHtml, containsString("id=\"config-datos\""));
    assertThat(ownerHtml, containsString("id=\"config-solicitudes\""));
    assertThat(ownerHtml, containsString("id=\"config-roles\""));
    assertThat(ownerHtml, not(containsString("id=\"plan-share-link\"")));
    assertThat(ownerHtml, not(containsString("Compartir link")));
    assertThat(
      ownerHtml,
      not(containsString("Datos, solicitudes, roles y baneos en un solo lugar"))
    );
    assertThat(ownerHtml, not(containsString("Secciones de configuración")));
    assertThat(ownerHtml, not(containsString("href=\"#config-datos\"")));
    assertThat(ownerHtml, not(containsString("href=\"#config-solicitudes\"")));
    assertThat(ownerHtml, not(containsString("href=\"#config-roles\"")));
    assertThat(
      ownerHtml.indexOf("id=\"plan-summary\""),
      lessThan(ownerHtml.indexOf("id=\"config-solicitudes\""))
    );
    assertThat(
      ownerHtml.indexOf("id=\"config-solicitudes\""),
      lessThan(ownerHtml.indexOf("Invitar a alguien"))
    );
    assertThat(
      ownerHtml.indexOf("id=\"config-solicitudes\""),
      lessThan(ownerHtml.indexOf("id=\"configuracion\""))
    );
    assertThat(ownerHtml, containsString("id=\"join-requests-heading\""));
    assertThat(ownerHtml, containsString("id=\"banned-participants\""));
    assertThat(ownerHtml, containsString("value=\"EDITORS\" selected=\"selected\""));
    assertThat(ownerHtml, containsString("value=\"OWNER\" selected=\"selected\""));

    String editorHtml = mockMvc
      .perform(get("/plans/" + id).with(user(OTHER_EMAIL)))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
    assertThat(editorHtml, containsString("id=\"configuracion\""));
    assertThat(
      editorHtml,
      containsString("Solo el dueño del plan puede cambiar estas capacidades.")
    );
    assertThat(editorHtml, not(containsString("id=\"permission-handleRequests\"")));
    assertThat(editorHtml, not(containsString("id=\"permission-inviteInternal\"")));
    assertThat(editorHtml, not(containsString("id=\"permission-inviteByEmail\"")));
    assertThat(editorHtml, not(containsString("id=\"plan-share-link\"")));

    mockMvc
      .perform(
        post("/plans/" + id + "/settings")
          .with(user(OTHER_EMAIL))
          .with(csrf())
          .param("handleRequests", "OWNER")
          .param("inviteInternal", "PARTICIPANTS")
          .param("inviteByEmail", "PARTICIPANTS")
      )
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));

    entityManager.flush();
    entityManager.clear();
    Plan unchanged = planRepository.findById(id).orElseThrow();
    assertThat(unchanged.getSettings().getHandleRequests().name(), is("EDITORS"));
    assertThat(unchanged.getSettings().getInviteInternal().name(), is("OWNER"));
    assertThat(unchanged.getSettings().getInviteByEmail().name(), is("EDITORS"));
  }

  @Test
  public void ownerAppearsInParticipantListAndCannotBeDemoted() throws Exception {
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
    mockMvc
      .perform(get("/plans/" + planId).with(user(OWNER_EMAIL)))
      .andExpect(model().attribute("participantEmails", List.of(OWNER_EMAIL)));
    for (String role : List.of("guest", "editor")) {
      mockMvc
        .perform(
          post("/plans/" + planId + "/participants/role")
            .with(user(OWNER_EMAIL))
            .with(csrf())
            .param("participantEmail", OWNER_EMAIL)
            .param("role", role)
        )
        .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
      assertThat(
        planRepository.findById(planId).orElseThrow().isAdministrator(OWNER_EMAIL),
        is(true)
      );
    }
  }

  @Test
  public void editorCanEditButCannotDeleteAndLosesAccessWhenDemoted() throws Exception {
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
    Plan plan = planRepository.findById(planId).orElseThrow();
    plan.getParticipants().add(userRepository.findById(otherId).orElseThrow());
    planRepository.save(plan);
    mockMvc
      .perform(
        post("/plans/" + planId + "/participants/role")
          .with(user(OTHER_EMAIL))
          .with(csrf())
          .param("participantEmail", OTHER_EMAIL)
          .param("role", "editor")
      )
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    assertThat(plan.isEditor(OTHER_EMAIL), is(false));
    mockMvc
      .perform(
        post("/plans/" + planId + "/participants/role")
          .with(user(OWNER_EMAIL))
          .with(csrf())
          .param("participantEmail", OTHER_EMAIL)
          .param("role", "editor")
      )
      .andExpect(redirectedUrl("/plans/" + planId));
    entityManager.flush();
    entityManager.clear();
    assertThat(planRepository.findById(planId).orElseThrow().isEditor(OTHER_EMAIL), is(true));
    mockMvc
      .perform(get("/plans/" + planId).with(user(OTHER_EMAIL)))
      .andExpect(model().attribute("canEditPlan", true))
      .andExpect(model().attribute("isPlanAdministrator", false));
    mockMvc
      .perform(
        post("/plans/" + planId)
          .with(user(OTHER_EMAIL))
          .with(csrf())
          .param("name", "Edited by editor")
          .param("eventDate", "2026-12-31")
      )
      .andExpect(redirectedUrl("/plans/" + planId));
    assertThat(planRepository.findById(planId).orElseThrow().getName(), is("Edited by editor"));
    mockMvc
      .perform(post("/plans/" + planId + "/delete").with(user(OTHER_EMAIL)).with(csrf()))
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    assertThat(planRepository.findById(planId).isPresent(), is(true));
    mockMvc
      .perform(
        post("/plans/" + planId + "/participants/role")
          .with(user(OWNER_EMAIL))
          .with(csrf())
          .param("participantEmail", OTHER_EMAIL)
          .param("role", "guest")
      )
      .andExpect(redirectedUrl("/plans/" + planId));
    mockMvc
      .perform(
        post("/plans/" + planId)
          .with(user(OTHER_EMAIL))
          .with(csrf())
          .param("name", "Forbidden")
          .param("eventDate", "2026-12-31")
      )
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    assertThat(planRepository.findById(planId).orElseThrow().getName(), is("Edited by editor"));
  }

  @Test
  public void ownerSeesRoleFormAndEditorOnlySeesBadge() throws Exception {
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    User editor = this.userRepository.findById(this.otherId).orElseThrow();
    plan.getParticipants().add(editor);
    plan.getEditors().add(editor);
    this.planRepository.save(plan);

    String ownerSection = participantsSection(
      this.mockMvc.perform(get("/plans/" + planId).with(user(OWNER_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString()
    );

    // AC-14/AC-15: el dueño ve el formulario de rol y los emails solo en inputs hidden.
    assertThat(ownerSection, containsString("Guardar rol"));
    assertThat(
      ownerSection,
      containsString("name=\"participantEmail\" value=\"" + OTHER_EMAIL + "\"")
    );
    assertThat(ownerSection, not(containsString(">" + OTHER_EMAIL + "<")));
    assertThat(ownerSection, not(containsString(">" + OWNER_EMAIL + "<")));
    assertThat(ownerSection, containsString("Test User"));

    String editorSection = participantsSection(
      this.mockMvc.perform(get("/plans/" + planId).with(user(OTHER_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString()
    );

    // El editor conserva la información del rol, pero no el control para cambiarlo.
    assertThat(editorSection, not(containsString("Guardar rol")));
    assertThat(editorSection, containsString(">Editor<"));
    assertThat(editorSection, not(containsString(">" + OTHER_EMAIL + "<")));
    assertThat(editorSection, not(containsString(">" + OWNER_EMAIL + "<")));
  }

  @Test
  public void editorCannotChangeAnotherParticipantsRole() throws Exception {
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
    String guestEmail = "guest@test.com";
    User guest = this.userRepository.findById(givenUser(guestEmail)).orElseThrow();
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    User editor = this.userRepository.findById(this.otherId).orElseThrow();
    plan.getParticipants().addAll(List.of(editor, guest));
    plan.getEditors().add(editor);
    this.planRepository.save(plan);

    this.mockMvc.perform(
        post("/plans/" + planId + "/participants/role")
          .with(user(OTHER_EMAIL))
          .with(csrf())
          .param("participantEmail", guestEmail)
          .param("role", "editor")
      )
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));

    this.entityManager.flush();
    this.entityManager.clear();
    assertThat(this.planRepository.findById(planId).orElseThrow().isEditor(guestEmail), is(false));
  }

  // --- listado ---

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PLN_070_getPlans_muestraLosPlanesDelDueno() throws Exception {
    // given
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);

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

  // --- GET /for-you [PPV, feed visible con y sin sesion] ---

  @Test
  public void I01_getForYou_anonimoResponde200ConElListado() throws Exception {
    // given: sin @WithMockUser, la request es anonima — el visitante tambien ve el feed
    givenPublicPlanFor(this.ownerId, PLAN_NAME);

    // when/then
    MvcResult result =
      this.mockMvc.perform(get("/for-you"))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/plans/public-list"))
        .andExpect(model().attributeExists("publicPlans"))
        .andReturn();

    // then: el CTA de la card sin sesion es un link al login con la vuelta, no un form POST
    String html = result.getResponse().getContentAsString();
    assertThat(html, containsString("href=\"/auth/login?redirect=/for-you\""));
  }

  @Test
  public void I01c_getPlansPublic_laRutaViejaYaNoExiste() throws Exception {
    // given: /plans/public se reemplazo por /for-you. Sin sesion la cadena de seguridad manda a
    // login antes de llegar al mapping (GET /plans/** sigue autenticado); con sesion el literal
    // cae en /{id}, no parsea UUID y GlobalExceptionHandler responde 404.

    // when/then (anonimo: rebota en el login, la ruta vieja no es publica)
    this.mockMvc.perform(get("/plans/public"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrlPattern("**/auth/login"));

    // when/then (con sesion: no hay mapping, la ruta ya no existe)
    this.mockMvc.perform(get("/plans/public").with(user(OTHER_EMAIL)))
      .andExpect(status().isNotFound());
  }

  @Test
  public void I01b_getForYou_logueadoResponde200ConElListado() throws Exception {
    // given
    UUID planId = givenPublicPlanFor(this.ownerId, PLAN_NAME);

    // when
    MvcResult result =
      this.mockMvc.perform(get("/for-you").with(user(OTHER_EMAIL)))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/plans/public-list"))
        .andExpect(model().attributeExists("publicPlans"))
        .andReturn();

    // then
    String html = result.getResponse().getContentAsString();
    assertThat(html, containsString(PLAN_NAME));
    assertThat(html, containsString("href=\"/plans/" + planId + "/public\""));
    // El boton "Clonar Plan" es parte de la tarjeta, uno por plan listado.
    assertThat(occurrences(html, "Clonar Plan"), is(1));
  }

  @Test
  public void I02_getForYou_noIncluyeNingunPlanPrivado() throws Exception {
    // given: un plan publico y otro privado, ambos con nombre propio
    givenPublicPlanFor(this.ownerId, PLAN_NAME);
    givenPlanFor(this.otherId, OTHER_PLAN_NAME);

    // when
    MvcResult result =
      this.mockMvc.perform(get("/for-you").with(user(OTHER_EMAIL)))
        .andExpect(status().isOk())
        .andReturn();

    // then: ni nombre, ni datos, ni cantidad del privado (AC-02)
    String html = result.getResponse().getContentAsString();
    assertThat(html, containsString(PLAN_NAME));
    assertThat(html, not(containsString(OTHER_PLAN_NAME)));
  }

  @Test
  public void I03_getForYou_sinPlanesPublicosMuestraElEstadoVacio() throws Exception {
    // given: solo planes privados, que para este listado es lo mismo que ninguno
    givenPlanFor(this.ownerId, PLAN_NAME);

    // when
    MvcResult result =
      this.mockMvc.perform(get("/for-you").with(user(OWNER_EMAIL)))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/plans/public-list"))
        .andReturn();

    // then: estado vacio amigable con salida a la creacion (AC-06)
    String html = result.getResponse().getContentAsString();
    assertThat(html, containsString("Todavía no hay planes públicos"));
    assertThat(html, containsString("href=\"/explore\""));
    assertThat(html, not(containsString(PLAN_NAME)));
  }

  @Test
  public void I04_cadaTarjetaLinkeaALaVistaPublicaDelPlan() throws Exception {
    // given
    UUID publicId = givenPublicPlanFor(this.ownerId, PLAN_NAME);
    UUID anotherId = givenPublicPlanFor(this.ownerId, OTHER_PLAN_NAME);

    // when
    String html =
      this.mockMvc.perform(get("/for-you").with(user(OTHER_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then (AC-04)
    assertThat(html, containsString("href=\"/plans/" + publicId + "/public\""));
    assertThat(html, containsString("href=\"/plans/" + anotherId + "/public\""));
  }

  @Test
  public void I05_getForYou_ocultaLosPlanesPropiosYSinEnlaceDeEditar() throws Exception {
    // given: un plan propio publico y uno ajeno publico
    givenPublicPlanFor(this.ownerId, PLAN_NAME);
    givenPublicPlanFor(this.otherId, OTHER_PLAN_NAME);

    // when: como dueno del primero
    String mio =
      this.mockMvc.perform(get("/for-you").with(user(OWNER_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    // when: como ajeno a ambos
    String ajeno =
      this.mockMvc.perform(get("/for-you").with(user("visita@test.com")))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then: el feed es para descubrir planes de otros: el propio queda fuera
    assertThat(mio, not(containsString(PLAN_NAME)));
    assertThat(mio, containsString(OTHER_PLAN_NAME));
    // then: ningun CTA de edicion en el feed y "Clonar Plan" una vez por plan ajeno listado
    assertThat(oneLine(ajeno), not(containsString("Editar")));
    assertThat(occurrences(ajeno, "Clonar Plan"), is(2));
  }

  @Test
  public void I06_cadaTarjetaMuestraUnCoverVisualConAltDelPlan() throws Exception {
    // given: dos planes publicos (covers distintos porque el id elige uno)
    UUID publicId = givenPublicPlanFor(this.ownerId, PLAN_NAME);
    UUID anotherId = givenPublicPlanFor(this.otherId, OTHER_PLAN_NAME);

    // when
    String html =
      this.mockMvc.perform(get("/for-you").with(user("visita@test.com")))
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
      containsString(
        "src=\"/images/plans/cover-" + (Math.floorMod(publicId.hashCode(), 6) + 1) + ".svg\""
      )
    );
    assertThat(
      html,
      containsString(
        "src=\"/images/plans/cover-" + (Math.floorMod(anotherId.hashCode(), 6) + 1) + ".svg\""
      )
    );
    this.mockMvc.perform(get("/images/plans/cover-1.svg")).andExpect(status().isOk());
  }

  // --- GET /for-you con busqueda, filtros y orden [SEARCH] ---

  @Test
  public void I01_search_porTextoMuestraSoloLosPlanesQueMatchean() throws Exception {
    // given: dos planes publicos, solo uno menciona el texto buscado
    givenSearchablePublicPlan(this.ownerId, "Salida al museo", null, null);
    givenSearchablePublicPlan(this.otherId, "Cena en el barrio", null, null);

    // when
    String html = forYouHtml("/for-you?q=museo");

    // then (AC-01)
    assertThat(html, containsString("Salida al museo"));
    assertThat(html, not(containsString("Cena en el barrio")));
  }

  @Test
  public void I02_search_porCategoriaMuestraSoloPlanesConUnLugarDeEsaCategoria() throws Exception {
    // given: un plan con un lugar BAR y otro con un lugar MUSEUM
    givenSearchablePublicPlan(this.ownerId, "Noche de bares", null, PlaceCategory.BAR);
    givenSearchablePublicPlan(this.otherId, "Tarde de museo", null, PlaceCategory.MUSEUM);

    // when
    String html = forYouHtml("/for-you?category=BAR");

    // then (AC-03)
    assertThat(html, containsString("Noche de bares"));
    assertThat(html, not(containsString("Tarde de museo")));
  }

  @Test
  public void I03_search_porFechaPasadaMuestraSoloLosPlanesConFechaAnteriorAHoy() throws Exception {
    // given: uno pasado, uno futuro y uno sin fecha (fuera de las dos ventanas)
    LocalDate today = LocalDate.now();
    givenSearchablePublicPlan(this.ownerId, "Plan pasado", today.minusDays(2), null);
    givenSearchablePublicPlan(this.otherId, "Plan futuro", today.plusDays(2), null);
    givenSearchablePublicPlan(this.ownerId, "Plan sin fecha", null, null);

    // when
    String html = forYouHtml("/for-you?when=past");

    // then (AC-04)
    assertThat(html, containsString("Plan pasado"));
    assertThat(html, not(containsString("Plan futuro")));
    assertThat(html, not(containsString("Plan sin fecha")));
  }

  @Test
  public void I04_search_ordenarPorFechaRespetaElOrdenAscendente() throws Exception {
    // given: el mas nuevo primero en la lista de entrada
    givenSearchablePublicPlan(this.ownerId, "Plan de marzo", LocalDate.of(2026, 3, 1), null);
    givenSearchablePublicPlan(this.otherId, "Plan de enero", LocalDate.of(2026, 1, 1), null);

    // when
    String html = forYouHtml("/for-you?sort=date");

    // then (AC-05)
    assertThat(html.indexOf("Plan de enero") < html.indexOf("Plan de marzo"), is(true));
  }

  @Test
  public void I05_search_sinResultadosMuestraElEstadoVacioConLaSalidaParaLimpiar()
    throws Exception {
    // given
    givenSearchablePublicPlan(this.ownerId, "Salida al museo", null, null);

    // when
    String html = forYouHtml("/for-you?q=zzz");

    // then (AC-09): estado propio de los filtros, con el link que vuelve al feed completo
    assertThat(html, containsString("No encontramos planes con esos filtros"));
    assertThat(html, containsString("Limpiar filtros"));
    assertThat(html, containsString("href=\"/for-you\""));
    assertThat(html, not(containsString("Todavía no hay planes públicos")));
  }

  @Test
  public void I06_search_parametrosInvalidosResponden200SinError() throws Exception {
    // given
    givenSearchablePublicPlan(this.ownerId, "Salida al museo", null, null);

    // when/then (AC-12): categoria y orden desconocidos se ignoran, nunca un 400
    this.mockMvc.perform(get("/for-you?category=NOPE&sort=nope"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/plans/public-list"))
      .andExpect(model().attribute("filtersApplied", false));
  }

  @Test
  public void I07_search_elListadoFiltradoNoExponeShortCodeNiEmailsNiParticipantes()
    throws Exception {
    // given: un plan publico con shortCode y un participante que no debe salir al HTML
    UUID planId = givenSearchablePublicPlan(this.ownerId, "Salida al museo", null, null);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setShortCode("SECRET01");
    plan.getParticipants().add(this.userRepository.findById(this.otherId).orElseThrow());
    this.planRepository.save(plan);

    // when
    String html = forYouHtml("/for-you?q=museo");

    // then (AC-10)
    assertThat(html, containsString("Salida al museo"));
    assertThat(html, not(containsString("SECRET01")));
    assertThat(html, not(containsString(OWNER_EMAIL)));
    assertThat(html, not(containsString(OTHER_EMAIL)));
  }

  @Test
  public void I08_search_elFormularioOfreceLimpiarSoloCuandoHayAlgoAplicado() throws Exception {
    // given: un plan publico para que el feed no caiga en el estado vacio
    givenSearchablePublicPlan(this.ownerId, "Salida al museo", null, null);

    // when
    String conTexto = forYouHtml("/for-you?q=museo");
    String conOrden = forYouHtml("/for-you?sort=name");
    String sinNada = forYouHtml("/for-you");

    // then (AC-13): la salida aparece con cualquier parametro no default y desaparece limpia
    assertThat(conTexto, containsString("data-testid=\"clear-filters\""));
    assertThat(conOrden, containsString("data-testid=\"clear-filters\""));
    assertThat(sinNada, not(containsString("data-testid=\"clear-filters\"")));
  }

  @Test
  public void T_CAL_020_feedElIndicadorDeCalificacionesReflejaElAgregado() throws Exception {
    // given: un plan calificado con 4 y 5 (promedio 4.5) y otro sin calificaciones
    givenPublicPlanFor(this.ownerId, PLAN_NAME);
    UUID ratedId = givenPublicPlanFor(this.otherId, OTHER_PLAN_NAME);
    givenUser("votante@test.com");
    this.mockMvc.perform(
        post("/plans/" + ratedId + "/ratings")
          .with(user(OWNER_EMAIL))
          .with(csrf())
          .param("score", "4")
      );
    this.mockMvc.perform(
        post("/plans/" + ratedId + "/ratings")
          .with(user("votante@test.com"))
          .with(csrf())
          .param("score", "5")
      );

    // when
    String html =
      this.mockMvc.perform(get("/for-you").with(user("visita@test.com")))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then (AC-10/AC-11): el agregado sale en la tarjeta del plan calificado; el feed no ordena
    assertThat(html, containsString("4.5"));
    assertThat(html, containsString(".rating-stars-fill {")); // la regla CSS vive en base.html, una sola fuente
    assertThat(occurrences(html, "class=\"rating-stars-fill"), is(1)); // el span del plan calificado
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
    assertThat(location, matchesPattern(UUID_PATTERN));

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
    // [SCH] El tile FECHA del resumen se unificó a dd/mm/yyyy; el input del form sigue ISO.
    assertThat(detail, containsString(">31/12/2026</span>"));
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
          post("/plans")
            .with(csrf())
            .param("name", PLAN_NAME)
            .param("isPublic", "true")
            .param("eventDate", "2026-12-31")
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
  public void T_PLN_074b_postPlans_permiteRepetirElMismoLugarYDerivaElHorarioDeLasDosFilas()
    throws Exception {
    // given: el mismo lugar dos veces; la repeticion sigue siendo valida
    UUID placeId = givenMuseum();

    // when
    MvcResult result = createPlanWith(placeId, placeId);

    // then: las dos filas se guardan y el servidor les deriva fecha y hora en orden
    this.entityManager.flush();
    this.entityManager.clear();
    Plan saved =
      this.planRepository.findById(planIdOf(result.getResponse().getRedirectedUrl())).orElseThrow();
    assertThat(saved.getPlanPlaces(), hasSize(2));
    assertThat(saved.getPlanPlaces().get(0).getSortOrder(), is(1));
    assertThat(saved.getPlanPlaces().get(0).getPlace().getId(), is(equalTo(placeId)));
    assertThat(saved.getPlanPlaces().get(0).getVisitDate(), is(equalTo(EVENT_DATE)));
    assertThat(saved.getPlanPlaces().get(0).getVisitTime(), is(equalTo(EVENT_TIME)));
    assertThat(saved.getPlanPlaces().get(1).getSortOrder(), is(2));
    assertThat(saved.getPlanPlaces().get(1).getPlace().getId(), is(equalTo(placeId)));
    assertThat(saved.getPlanPlaces().get(1).getVisitDate(), is(equalTo(EVENT_DATE)));
    // 90 minutos de visita + 5 de traslado entre dos paradas pegadas
    assertThat(saved.getPlanPlaces().get(1).getVisitTime(), is(equalTo(SECOND_STOP_START)));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void I03_postPlans_respetaElOrdenDelFormularioYNoLoReordenaPorHora() throws Exception {
    // given: se envian los lugares al reves del orden en que se crearon. El reordenamiento viejo
    // por hora de visita daba vuelta esta lista; ahora el orden del form es el del itinerario.
    UUID secondCreated = givenMuseum();
    UUID firstCreated = givenMuseum();

    // when
    MvcResult result = createPlanWith(secondCreated, firstCreated);

    // then: el itinerario persistido conserva el orden enviado
    this.entityManager.flush();
    this.entityManager.clear();
    Plan saved =
      this.planRepository.findById(planIdOf(result.getResponse().getRedirectedUrl())).orElseThrow();
    assertThat(saved.getPlanPlaces(), hasSize(2));
    assertThat(saved.getPlanPlaces().get(0).getPlace().getId(), is(equalTo(secondCreated)));
    assertThat(saved.getPlanPlaces().get(0).getSortOrder(), is(1));
    assertThat(saved.getPlanPlaces().get(0).getVisitTime(), is(equalTo(EVENT_TIME)));
    assertThat(saved.getPlanPlaces().get(1).getPlace().getId(), is(equalTo(firstCreated)));
    assertThat(saved.getPlanPlaces().get(1).getSortOrder(), is(2));
    assertThat(saved.getPlanPlaces().get(1).getVisitTime(), is(equalTo(SECOND_STOP_START)));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void I01_postPlans_derivaElItinerarioDesdeLaHoraDeInicio() throws Exception {
    // given: dos museos con las mismas coordenadas, para que el traslado caiga en el piso de 5
    UUID first = givenMuseum();
    UUID second = givenMuseum();

    // when
    MvcResult result = createPlanWith(first, second);

    // then: fecha, hora de inicio y ventana viajan al plan, y cada parada queda con su horario
    this.entityManager.flush();
    this.entityManager.clear();
    Plan saved =
      this.planRepository.findById(planIdOf(result.getResponse().getRedirectedUrl())).orElseThrow();
    assertThat(saved.getEventDate(), is(equalTo(EVENT_DATE)));
    assertThat(saved.getEventTime(), is(equalTo(EVENT_TIME)));
    assertThat(saved.getDayEndTime(), is(equalTo(DAY_END)));
    assertThat(saved.getTransportMode(), is(TransportMode.WALK));
    assertThat(saved.getPlanPlaces(), hasSize(2));
    for (PlanPlace stop : saved.getPlanPlaces()) {
      assertThat(stop.getVisitDate(), is(equalTo(EVENT_DATE)));
      assertThat(stop.getDurationMinutes(), is(equalTo(MUSEUM_DURATION_MINUTES)));
    }
    assertThat(saved.getPlanPlaces().get(0).getVisitTime(), is(equalTo(EVENT_TIME)));
    assertThat(saved.getPlanPlaces().get(1).getVisitTime(), is(equalTo(SECOND_STOP_START)));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void I02_postPlans_sinFechaNoCreaNadaYVuelveAExplore() throws Exception {
    // when: la fecha es la base del itinerario, asi que el alta sin ella no pasa la validacion
    this.mockMvc.perform(
        post("/plans").with(csrf()).param("name", PLAN_NAME).param("eventTime", "10:00")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/explore"));

    // then
    assertThat(this.planRepository.findAll(), is(empty()));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void I04_putPlans_cambiarLaFechaCorreLasParadasYConservaLaHoraRelativa() throws Exception {
    // given: un plan con el itinerario ya derivado
    UUID planId = planIdOf(
      createPlanWith(givenMuseum(), givenMuseum()).getResponse().getRedirectedUrl()
    );

    // when: se cambia solo el dia; la hora de inicio y el modo siguen igual
    this.mockMvc.perform(
        post("/plans/" + planId)
          .with(csrf())
          .param("_method", "PUT")
          .param("name", PLAN_NAME)
          .param("eventDate", "2026-07-01")
          .param("eventTime", "10:00")
          .param("dayEndTime", "23:00")
          .param("transportMode", "WALK")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/plans/" + planId));

    // then: todas las paradas se corren al dia nuevo y mantienen su lugar en la cadena
    this.entityManager.flush();
    this.entityManager.clear();
    Plan updated = this.planRepository.findById(planId).orElseThrow();
    assertThat(updated.getEventDate(), is(equalTo(LocalDate.of(2026, 7, 1))));
    assertThat(updated.getPlanPlaces(), hasSize(2));
    for (PlanPlace stop : updated.getPlanPlaces()) {
      assertThat(stop.getVisitDate(), is(equalTo(LocalDate.of(2026, 7, 1))));
    }
    assertThat(updated.getPlanPlaces().get(0).getVisitTime(), is(equalTo(EVENT_TIME)));
    assertThat(updated.getPlanPlaces().get(1).getVisitTime(), is(equalTo(SECOND_STOP_START)));
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
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
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
    // El email del dueño ya no es texto visible en la lista: se muestra el nombre. El navbar sí
    // lleva el email de la sesión, así que la aserción se acota a la sección de participantes.
    String html = result.getResponse().getContentAsString();
    String participantsSection = html.substring(
      html.indexOf("participants-heading"),
      html.indexOf("id=\"configuracion\"")
    );
    assertThat(participantsSection, containsString("Test User"));
    assertThat(participantsSection, not(containsString(OWNER_EMAIL)));
    assertThat(html, containsString("test1234"));
    assertThat(html, containsString("2026-12-01"));
    assertThat(html, containsString("\"id\":\"" + entry.getPlace().getId() + "\""));
    assertThat(html, not(containsString("password123")));
    assertThat(html, not(containsString("\"administrator\":")));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PLN_077_getPlansId_inexistenteRedirigeAlListado() throws Exception {
    this.mockMvc.perform(get("/plans/" + MISSING_PLAN_ID))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void T_PLN_078_getPlansId_deOtroUsuarioRedirigeYNoLoBorra() throws Exception {
    // given
    UUID planId = givenPlanFor(this.otherId, OTHER_PLAN_NAME);

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
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
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
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);

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
    UUID planId = givenPlanFor(this.otherId, OTHER_PLAN_NAME);
    String shortCode = this.planRepository.findById(planId).orElseThrow().getShortCode();

    // when
    this.mockMvc.perform(
        post("/plans/" + planId)
          .with(csrf())
          .param("_method", "PUT")
          .param("name", "Hackeado")
          .param("eventDate", "2026-12-31")
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
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
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
    UUID planId = givenPlanFor(this.otherId, OTHER_PLAN_NAME);

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
    UUID planId = givenPublicPlanWithPlace();

    // when
    String location = cloneLocationOf(planId);

    // then: the redirect lands on a different plan, listed as owned by the cloner
    assertThat(location, matchesPattern(UUID_PATTERN));
    assertThat(planIdOf(location), is(not(equalTo(planId))));
    MvcResult listing = this.mockMvc.perform(get("/plans")).andExpect(status().isOk()).andReturn();
    assertThat(((Plan) plansOf(listing).get(0)).getId(), is(equalTo(planIdOf(location))));
  }

  @Test
  @WithMockUser(username = OTHER_EMAIL)
  public void T_PLN_085_postPlansIdClone_laCopiaNacePrivadaConShortCodeNuevo() throws Exception {
    // given (I-02)
    UUID planId = givenPublicPlanWithPlace();

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
    UUID planId = givenPublicPlanWithPlace();
    UUID placeId = this.placeRepository.findAll().get(0).getId();
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
    assertThat(html, containsString("\"id\":\"" + placeId + "\""));
    assertThat(html, containsString("\"visitDate\":\"2026-12-01\""));
  }

  @Test
  @WithMockUser(username = OTHER_EMAIL)
  public void T_PLN_087_postPlansIdCloneDePlanPrivadoAjenoRespondeComoIdInexistente()
    throws Exception {
    // given (I-04): private plan of the owner, the cloner is neither administrator nor participant
    UUID planId = givenPlanFor(this.ownerId, OTHER_PLAN_NAME);

    // when: cloning it answers exactly like cloning a missing id
    this.mockMvc.perform(post("/plans/" + planId + "/clone").with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    this.mockMvc.perform(post("/plans/" + MISSING_PLAN_ID + "/clone").with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));

    // then: no copy was created
    assertThat(this.planRepository.findAll(), hasSize(1));
  }

  @Test
  @WithMockUser(username = OTHER_EMAIL)
  public void T_PLN_088_postPlansIdClone_dejaElOriginalIntacto() throws Exception {
    // given (I-05): public plan with a participant and an itinerary
    UUID planId = givenPublicPlanWithPlace();
    UUID participantId = givenUser("participant@test.com");
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
    UUID planId = givenPublicPlanWithPlace();

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
  public void T_CLN_002_getPlansIdPublic_sinSesionLosCtasLlevanAlLoginConservandoLaVuelta()
    throws Exception {
    // given
    UUID planId = givenPublicPlanWithPlace();

    // when: anonimo abre la vista publica
    String html =
      this.mockMvc.perform(get("/plans/" + planId + "/public"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then (AC-12): el anonimo ve los MISMOS CTAs que el logueado ("Unirme al plan" y
    // "Clonar plan"), pero como links al login con redirect de vuelta al plan: no ejecutan
    // join/clone. El parametro viaja url-encoded segun Thymeleaf, por eso el ternario / %2F.
    assertThat(html, containsString("Unirme al plan"));
    assertThat(html, containsString("Clonar plan"));
    assertThat(html, not(containsString("action=\"/plans/" + planId + "/clone\"")));
    assertThat(html, not(containsString("name=\"_csrf\"")));
    assertThat(
      html,
      matchesPattern(
        "(?s).*href=\"/auth/login\\?redirect=[^\"]*" + planId + "(/|%2F)public[^\"]*\".*"
      )
    );
  }

  // --- [MAPS] Link "Cómo llegar" con la direccion del lugar ---

  @Test
  public void T_MAPS_001_getPlansIdPublic_muestraElLinkDeMapsConLaDireccionDelLugar()
    throws Exception {
    // given: un plan publico con una parada que tiene direccion
    UUID planId = givenPublicPlanWithPlace();
    Place place =
      this.planRepository.findById(planId).orElseThrow().getPlanPlaces().get(0).getPlace();

    // when: cualquiera abre la vista publica
    String html =
      this.mockMvc.perform(get("/plans/" + planId + "/public"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then: el link apunta a las directions de Google Maps con la DIRECCION del lugar (el
    // dataset trae direccion y coordenada desfasadas, asi que el link sigue el texto). El
    // destino va url-encoded; se decodifica para comparar sin atarse a %20 vs +.
    assertThat(html, containsString("maps/dir/?api=1"));
    java.util.regex.Matcher destino = java.util.regex.Pattern
      .compile("destination=([^&\"']+)")
      .matcher(html);
    assertThat(destino.find(), is(true));
    String esperado = (place.getName() + " " + place.getAddress()).trim();
    assertThat(
      java.net.URLDecoder.decode(destino.group(1), java.nio.charset.StandardCharsets.UTF_8),
      is(esperado)
    );
  }

  @Test
  public void T_MAPS_002_getPlansIdPublic_sinDireccionNoMuestraElLinkDeMaps() throws Exception {
    // given: un plan publico cuya unica parada no tiene direccion
    UUID planId = givenPlanFor(this.ownerId, PUBLIC_PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setIsPublic(true);
    Place sinDireccion = new Place();
    sinDireccion.setName("Lugar sin direccion");
    sinDireccion.setCategory(PlaceCategory.RESTAURANT);
    this.placeRepository.save(sinDireccion);
    PlanPlace entry = new PlanPlace();
    entry.setPlace(sinDireccion);
    plan.addPlanPlace(entry);
    this.planRepository.save(plan);

    // when
    String html =
      this.mockMvc.perform(get("/plans/" + planId + "/public"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then: sin direccion la vista no ofrece el link. Se busca el href ya renderizado (el
    // ancla), no el texto suelto: el popup del mapa arma su link en el script, asi que
    // "maps/dir/" aparece como literal de JS aunque no haya parada con direccion.
    assertThat(html, not(containsString("href=\"https://www.google.com/maps/dir")));
  }

  @Test
  @WithMockUser(username = OTHER_EMAIL)
  public void participantCanJoinReadAndLeaveWithoutDeletingPlan() throws Exception {
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setShortCode("JOIN1234");
    plan.setDescription("Recorrido con amigos");
    plan.setEventDate(LocalDate.of(2026, 12, 31));
    plan.setEventTime(LocalTime.of(18, 30));
    UUID remainingUserId = givenUser("remaining@test.com");
    plan.getParticipants().add(this.userRepository.findById(remainingUserId).orElseThrow());
    PlanPlace entry = new PlanPlace();
    entry.setPlace(this.placeRepository.findAll().get(0));
    plan.addPlanPlace(entry);
    this.planRepository.save(plan);

    this.mockMvc.perform(post("/plans/join").with(csrf()).param("shortCode", " join1234 "))
      .andExpect(redirectedUrl("/plans/" + planId + "/public"));
    this.mockMvc.perform(post("/plans/join").with(csrf()).param("shortCode", "JOIN1234"))
      .andExpect(redirectedUrl("/plans/" + planId + "/public"));
    assertThat(plan.getJoinRequests(), hasSize(1));
    assertThat(plan.isParticipant(OTHER_EMAIL), is(false));
    this.mockMvc.perform(
        post(
          "/plans/" +
          planId +
          "/join-requests/" +
          plan.getJoinRequests().getFirst().getId() +
          "/respond"
        )
          .with(user(OWNER_EMAIL))
          .with(csrf())
          .param("action", "accept")
      )
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
    // [SCH] El tile FECHA muestra dd/mm/yyyy con el resto de la feature.
    assertThat(summary, containsString(">31/12/2026</span>"));
    assertThat(summary, containsString(">18:30</span>"));
    assertThat(html, containsString("Salir del plan"));
    assertThat(html, not(containsString("Eliminar plan")));
    assertThat(html, not(containsString("Datos básicos")));
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
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.getParticipants().add(this.userRepository.findById(this.otherId).orElseThrow());
    this.planRepository.save(plan);
    this.mockMvc.perform(post("/plans/" + planId + "/delete").with(csrf()))
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    this.mockMvc.perform(
        post("/plans/" + planId)
          .with(csrf())
          .param("name", "Changed")
          .param("eventDate", "2026-12-31")
      )
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
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
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
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
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
    UUID planId = givenPlanFor(this.ownerId, PUBLIC_PLAN_NAME);
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
  public void T_PVP_004_getPlansIdPublic_privadoRedirigeALoginSinExponerContenido()
    throws Exception {
    UUID planId = givenPlanFor(this.ownerId, OTHER_PLAN_NAME);

    this.mockMvc.perform(get("/plans/" + planId + "/public"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/auth/login?redirect=/plans/" + planId + "/public"));

    this.mockMvc.perform(get("/plans/" + MISSING_PLAN_ID + "/public"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
  }

  @Test
  public void T_PVP_005_getPlansIdPublic_sinSesionNoRedirigeALogin() throws Exception {
    UUID planId = givenPlanFor(this.ownerId, PUBLIC_PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setIsPublic(true);
    this.planRepository.save(plan);

    this.mockMvc.perform(get("/plans/" + planId + "/public")).andExpect(status().isOk());
  }

  @Test
  public void T_PVP_006_getPlansIdPublic_conSesionDeNoParticipanteMuestraUnirseYClonar()
    throws Exception {
    UUID planId = givenPlanFor(this.ownerId, PUBLIC_PLAN_NAME);
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
    UUID planId = givenPlanFor(this.ownerId, PUBLIC_PLAN_NAME);
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
    UUID planId = givenPlanFor(this.ownerId, PUBLIC_PLAN_NAME);
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
    UUID planId = givenPlanFor(this.ownerId, PUBLIC_PLAN_NAME);
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
    assertThat(html, not(containsString("Datos básicos")));
    assertThat(html, not(containsString("Agregar lugar al plan")));
  }

  @Test
  public void T_PVP_016_getPlansIdPublic_muestraElContadorDeParticipantesSinExponerEmails()
    throws Exception {
    // given: el plan publico tiene dos participantes ademas de su administrador
    UUID planId = givenPublicPlanWithPlace();
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    UUID otroId = givenUser("otro@test.com");
    plan.getParticipants().add(this.userRepository.findById(this.otherId).orElseThrow());
    plan.getParticipants().add(this.userRepository.findById(otroId).orElseThrow());
    this.planRepository.save(plan);

    // when
    String html =
      this.mockMvc.perform(get("/plans/" + planId + "/public"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then: la cantidad se muestra, pero ni la lista ni los emails salen al HTML
    assertThat(oneLine(html), containsString("2 participantes"));
    assertThat(html, not(containsString("otro@test.com")));
    assertThat(html, not(containsString(OTHER_EMAIL)));
  }

  @Test
  public void T_PVP_017_getPlansIdPublic_muestraElMapaConLosLugaresDelItinerario()
    throws Exception {
    // given: un plan publico con dos paradas, cada una en un lugar del catalogo con lat/lng
    UUID planId = givenPlanFor(this.ownerId, PUBLIC_PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setIsPublic(true);
    List<Place> catalog = this.placeRepository.findAll();
    PlanPlace first = new PlanPlace();
    first.setPlace(catalog.get(0));
    first.setSortOrder(1);
    PlanPlace second = new PlanPlace();
    second.setPlace(catalog.get(1));
    second.setSortOrder(2);
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

    // then: el contenedor del mapa y el JSON de paradas que Leaflet consume, con las
    // coordenadas de cada lugar del catalogo (no se renderizan como texto, van al script)
    assertThat(html, containsString("id=\"plan-map\""));
    assertThat(html, containsString("leaflet"));
    assertThat(html, containsString(String.valueOf(catalog.get(0).getLatitude())));
    assertThat(html, containsString(String.valueOf(catalog.get(1).getLongitude())));
  }

  // --- POST /plans/{id}/ratings [CAL] ---

  @Test
  public void T_CAL_012_postRatings_calificaConSesionYLaVistaMuestraElAgregado() throws Exception {
    UUID planId = givenPublicPlanFor(this.ownerId, PUBLIC_PLAN_NAME);
    givenUser("votante2@test.com");

    // Dos usuarios logueados califican: 4 y 5 → promedio 4.5 con 2 calificaciones
    this.mockMvc.perform(
        post("/plans/" + planId + "/ratings")
          .with(user(OTHER_EMAIL))
          .with(csrf())
          .param("score", "4")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/plans/" + planId + "/public"));
    this.mockMvc.perform(
        post("/plans/" + planId + "/ratings")
          .with(user("votante2@test.com"))
          .with(csrf())
          .param("score", "5")
      )
      .andExpect(status().is3xxRedirection());

    String html =
      this.mockMvc.perform(get("/plans/" + planId + "/public"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // AC-07: el agregado publico sale al HTML; AC-02: la autoria nunca (ni emails de votantes)
    assertThat(html, containsString("4.5"));
    assertThat(html, not(containsString(OTHER_EMAIL)));
    assertThat(html, not(containsString("votante2@test.com")));
  }

  @Test
  public void T_CAL_013_postRatings_recalificarReemplazaNuncaSumaOtraFila() throws Exception {
    UUID planId = givenPublicPlanFor(this.ownerId, PUBLIC_PLAN_NAME);

    this.mockMvc.perform(
        post("/plans/" + planId + "/ratings")
          .with(user(OTHER_EMAIL))
          .with(csrf())
          .param("score", "4")
      );
    this.mockMvc.perform(
        post("/plans/" + planId + "/ratings")
          .with(user(OTHER_EMAIL))
          .with(csrf())
          .param("score", "5")
      )
      .andExpect(status().is3xxRedirection());

    // AC-04: misma cantidad de votos, promedio reemplazado por el nuevo puntaje
    this.mockMvc.perform(get("/plans/" + planId + "/public").with(user(OTHER_EMAIL)))
      .andExpect(status().isOk())
      .andExpect(model().attribute("ratingCount", is(1L)))
      .andExpect(model().attribute("ratingAverage", is(5.0)))
      .andExpect(model().attribute("myRating", is(5)));
  }

  @Test
  public void T_CAL_014_postRatings_anonimoNoCalifica() throws Exception {
    UUID planId = givenPublicPlanFor(this.ownerId, PUBLIC_PLAN_NAME);

    // Sin sesion el POST no califica: el filtro CSRF lo rebota y la cadena manda a login (AC-13)
    this.mockMvc.perform(post("/plans/" + planId + "/ratings").param("score", "4"))
      .andExpect(status().isForbidden());
    this.mockMvc.perform(post("/plans/" + planId + "/ratings").with(csrf()).param("score", "4"))
      .andExpect(redirectedUrlPattern("**/auth/login"));
  }

  @Test
  public void T_CAL_015_postRatings_planPrivadoOnexistenteRespondeComoInexistente()
    throws Exception {
    UUID planId = givenPlanFor(this.ownerId, OTHER_PLAN_NAME);

    // AC-06: calificar un plan privado es igual que calificar un id que no existe
    this.mockMvc.perform(
        post("/plans/" + planId + "/ratings")
          .with(user(OWNER_EMAIL))
          .with(csrf())
          .param("score", "5")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));

    this.mockMvc.perform(
        post("/plans/" + MISSING_PLAN_ID + "/ratings")
          .with(user(OWNER_EMAIL))
          .with(csrf())
          .param("score", "5")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
  }

  @Test
  public void T_CAL_016_widget_publicoSinRatingsMuestraElEstadoVacio() throws Exception {
    UUID planId = givenPublicPlanFor(this.ownerId, PUBLIC_PLAN_NAME);

    String html = oneLine(
      this.mockMvc.perform(get("/plans/" + planId + "/public"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString()
    );

    // AC-07: sin calificaciones hay estado vacio explicito y sin capa de relleno
    assertThat(html, containsString("Todavía sin calificaciones"));
    assertThat(html, not(containsString("class=\"rating-stars-fill\"")));
  }

  @Test
  public void T_CAL_017_widget_elRellenoAmarilloReflejaElPromedioEnProporcion() throws Exception {
    UUID planId = givenPublicPlanFor(this.ownerId, PUBLIC_PLAN_NAME);
    givenUser("votante@test.com");
    // 4 + 5 = promedio 4.5 → la capa amarilla cubre el 90% de la fila (AC-08)
    this.mockMvc.perform(
        post("/plans/" + planId + "/ratings")
          .with(user(OTHER_EMAIL))
          .with(csrf())
          .param("score", "4")
      );
    this.mockMvc.perform(
        post("/plans/" + planId + "/ratings")
          .with(user("votante@test.com"))
          .with(csrf())
          .param("score", "5")
      );

    String html =
      this.mockMvc.perform(get("/plans/" + planId + "/public"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(html, containsString("width:90.00%"));
    assertThat(html, containsString("4.5"));
  }

  @Test
  public void T_CAL_018_widget_anonimoVeElIndicadorPeroNuncaUnFormDeVoto() throws Exception {
    UUID planId = givenPublicPlanFor(this.ownerId, PUBLIC_PLAN_NAME);
    givenUser("votante@test.com");
    this.mockMvc.perform(
        post("/plans/" + planId + "/ratings")
          .with(user("votante@test.com"))
          .with(csrf())
          .param("score", "4")
      );

    String html =
      this.mockMvc.perform(get("/plans/" + planId + "/public"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // AC-05: el anonimo ve la misma fila (la capa del promedio) como soft gate: el widget
    // es un link al login con la vuelta, y el form de voto no existe
    assertThat(html, not(containsString("action=\"/plans/" + planId + "/ratings\"")));
    assertThat(html, not(containsString("name=\"score\"")));
    assertThat(html, containsString("rating-stars-fill"));
    assertThat(html, containsString("rating-widget-anon"));
    assertThat(html, containsString("/auth/login?redirect=/plans/" + planId + "/public"));
  }

  @Test
  public void T_CAL_019_widget_conSesionEsUnFormDeRadiosConElPuntajePropioPreseleccionado()
    throws Exception {
    UUID planId = givenPublicPlanFor(this.ownerId, PUBLIC_PLAN_NAME);
    this.mockMvc.perform(
        post("/plans/" + planId + "/ratings")
          .with(user(OTHER_EMAIL))
          .with(csrf())
          .param("score", "4")
      );

    String html =
      this.mockMvc.perform(get("/plans/" + planId + "/public").with(user(OTHER_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // AC-01/AC-03/AC-09: 5 radios de score, el propio preseleccionado, el POST de verdad
    assertThat(html, containsString("action=\"/plans/" + planId + "/ratings\""));
    assertThat(occurrences(html, "name=\"score\""), is(5));
    assertThat(html, containsString("checked=\"checked\""));
  }

  // --- fixtures ---

  @Test
  public void anonymousPublicPlanIsReadOnlyAndRendersFullName() throws Exception {
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setIsPublic(true);
    this.planRepository.save(plan);
    String html =
      this.mockMvc.perform(get("/plans/" + planId))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    assertThat(html, not(containsString("Invitar a alguien")));
    assertThat(html, containsString("Test User"));
    // El email del participante no se muestra como texto: solo nombre, rol y link al perfil.
    assertThat(html, not(containsString(OWNER_EMAIL)));
    assertThat(html, not(containsString("Salir del plan")));
    assertThat(html, not(containsString("Datos básicos")));
    assertThat(html, not(containsString("Eliminar plan")));
    assertThat(html, not(containsString("Guardar rol")));
    this.mockMvc.perform(
        post("/plans/" + planId + "/participants/invite").param("participantEmail", OTHER_EMAIL)
      )
      .andExpect(status().isForbidden());
    this.mockMvc.perform(
        post("/plans/" + planId + "/participants/invite")
          .with(csrf())
          .param("participantEmail", OTHER_EMAIL)
          .param("role", "editor")
      )
      .andExpect(redirectedUrlPattern("**/auth/login"));
    this.mockMvc.perform(
        post("/plans/" + planId + "/participants/invite")
          .with(csrf())
          .param("participantEmail", OTHER_EMAIL)
          .param("role", "guest")
      )
      .andExpect(redirectedUrlPattern("**/auth/login"));
    for (String path : List.of("", "/delete", "/leave", "/participants/role")) {
      this.mockMvc.perform(post("/plans/" + planId + path).with(csrf()))
        .andExpect(redirectedUrlPattern("**/auth/login"));
    }
    this.entityManager.flush();
    this.entityManager.clear();
    Plan saved = this.planRepository.findById(planId).orElseThrow();
    assertThat(saved.isParticipant(OTHER_EMAIL), is(false));
    assertThat(saved.isEditor(OTHER_EMAIL), is(false));
  }

  @Test
  public void anonymousCannotReadPrivatePlan() throws Exception {
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
    this.mockMvc.perform(get("/plans/" + planId)).andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
  }

  @Test
  @WithMockUser(username = OTHER_EMAIL)
  public void visitorCannotInviteToPublicPlan() throws Exception {
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setIsPublic(true);
    this.planRepository.save(plan);
    this.mockMvc.perform(get("/plans/" + planId))
      .andExpect(status().isOk())
      .andExpect(model().attribute("canInvite", false));
    this.mockMvc.perform(
        post("/plans/" + planId + "/participants/invite")
          .with(csrf())
          .param("participantEmail", "guest@test.com")
          .param("role", "guest")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    this.entityManager.flush();
    this.entityManager.clear();
    Plan saved = this.planRepository.findById(planId).orElseThrow();
    assertThat(saved.isParticipant(OTHER_EMAIL), is(false));
    assertThat(saved.getInvitations(), is(empty()));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void ownerCanInviteToOwnPublicPlan() throws Exception {
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setIsPublic(true);
    this.planRepository.save(plan);
    this.mockMvc.perform(get("/plans/" + planId))
      .andExpect(status().isOk())
      .andExpect(model().attribute("canInvite", true));
    this.mockMvc.perform(
        post("/plans/" + planId + "/participants/invite")
          .with(csrf())
          .param("participantEmail", OTHER_EMAIL)
          .param("role", "guest")
      )
      .andExpect(redirectedUrl("/plans/" + planId));
    this.entityManager.flush();
    this.entityManager.clear();
    Plan saved = this.planRepository.findById(planId).orElseThrow();
    assertThat(saved.isParticipant(OTHER_EMAIL), is(false));
    assertThat(saved.getInvitations().size(), is(1));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void ownerInvitationRemainsPendingUntilRecipientAccepts() throws Exception {
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
    this.mockMvc.perform(
        post("/plans/" + planId + "/participants/invite")
          .with(csrf())
          .param("participantEmail", "unknown@test.com")
      )
      .andExpect(redirectedUrl("/plans/" + planId))
      .andExpect(
        org.springframework.test.web.servlet.result.MockMvcResultMatchers
          .flash()
          .attributeExists("invitationError")
      );
    this.mockMvc.perform(
        post("/plans/" + planId + "/participants/invite")
          .with(csrf())
          .param("participantEmail", OTHER_EMAIL)
          .param("role", "editor")
      )
      .andExpect(redirectedUrl("/plans/" + planId));
    this.entityManager.flush();
    this.entityManager.clear();
    Plan saved = this.planRepository.findById(planId).orElseThrow();
    assertThat(saved.isParticipant(OTHER_EMAIL), is(false));
    assertThat(saved.isEditor(OTHER_EMAIL), is(false));
    UUID invitationId = saved.getInvitations().getFirst().getId();
    this.mockMvc.perform(get("/plans").with(user(OTHER_EMAIL)))
      .andExpect(status().isOk())
      .andExpect(model().attribute("pendingInvitations", hasSize(1)))
      .andExpect(
        org.springframework.test.web.servlet.result.MockMvcResultMatchers
          .content()
          .string(containsString("Invitaciones pendientes"))
      )
      .andExpect(
        org.springframework.test.web.servlet.result.MockMvcResultMatchers
          .content()
          .string(containsString(OWNER_EMAIL))
      )
      .andExpect(
        org.springframework.test.web.servlet.result.MockMvcResultMatchers
          .content()
          .string(containsString("Test User"))
      );
    this.mockMvc.perform(get("/plans/" + planId).with(user(OTHER_EMAIL)))
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    this.mockMvc.perform(
        post("/plans/" + planId + "/invitations/" + invitationId + "/respond")
          .with(user(OTHER_EMAIL))
          .with(csrf())
          .param("accept", "true")
      )
      .andExpect(redirectedUrl("/plans"));
    this.entityManager.flush();
    this.entityManager.clear();
    saved = this.planRepository.findById(planId).orElseThrow();
    assertThat(saved.isParticipant(OTHER_EMAIL), is(true));
    assertThat(saved.isEditor(OTHER_EMAIL), is(true));
    assertThat(saved.getInvitations(), is(empty()));
    this.mockMvc.perform(get("/plans").with(user(OTHER_EMAIL)))
      .andExpect(model().attribute("pendingInvitations", hasSize(0)))
      .andExpect(model().attribute("participantPlans", hasSize(1)));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void onlyRecipientCanRejectAndRepeatedResponsesCannotJoin() throws Exception {
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
    String invitePath = "/plans/" + planId + "/participants/invite";
    this.mockMvc.perform(post(invitePath).with(csrf()).param("participantEmail", OTHER_EMAIL))
      .andExpect(redirectedUrl("/plans/" + planId));
    this.entityManager.flush();
    this.entityManager.clear();
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    UUID invitationId = plan.getInvitations().getFirst().getId();
    this.mockMvc.perform(post(invitePath).with(csrf()).param("participantEmail", OTHER_EMAIL))
      .andExpect(
        org.springframework.test.web.servlet.result.MockMvcResultMatchers
          .flash()
          .attributeExists("invitationError")
      );
    String respondPath = "/plans/" + planId + "/invitations/" + invitationId + "/respond";
    this.mockMvc.perform(post(respondPath).with(csrf()).param("accept", "true"))
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    this.mockMvc.perform(get("/plans"))
      .andExpect(model().attribute("pendingInvitations", hasSize(0)));
    this.mockMvc.perform(post(respondPath).with(user(OTHER_EMAIL)).param("accept", "false"))
      .andExpect(status().isForbidden());
    this.mockMvc.perform(
        post(respondPath).with(user(OTHER_EMAIL)).with(csrf()).param("accept", "false")
      )
      .andExpect(redirectedUrl("/plans"));
    this.entityManager.flush();
    this.entityManager.clear();
    plan = this.planRepository.findById(planId).orElseThrow();
    assertThat(plan.isParticipant(OTHER_EMAIL), is(false));
    assertThat(plan.getInvitations(), is(empty()));
    this.mockMvc.perform(
        post(respondPath).with(user(OTHER_EMAIL)).with(csrf()).param("accept", "true")
      )
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    this.mockMvc.perform(post(invitePath).with(csrf()).param("participantEmail", OTHER_EMAIL))
      .andExpect(redirectedUrl("/plans/" + planId));
    this.entityManager.flush();
    this.entityManager.clear();
    plan = this.planRepository.findById(planId).orElseThrow();
    UUID nextInvitationId = plan.getInvitations().getFirst().getId();
    this.mockMvc.perform(
        post("/plans/" + planId + "/invitations/" + nextInvitationId + "/respond")
          .with(user(OTHER_EMAIL))
          .with(csrf())
          .param("accept", "true")
      )
      .andExpect(redirectedUrl("/plans"));
    this.entityManager.flush();
    this.entityManager.clear();
    plan = this.planRepository.findById(planId).orElseThrow();
    assertThat(plan.isParticipant(OTHER_EMAIL), is(true));
    assertThat(plan.isEditor(OTHER_EMAIL), is(false));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void deletingPlanRemovesPendingInvitations() throws Exception {
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
    this.mockMvc.perform(
        post("/plans/" + planId + "/participants/invite")
          .with(csrf())
          .param("participantEmail", OTHER_EMAIL)
      )
      .andExpect(redirectedUrl("/plans/" + planId));
    this.entityManager.flush();
    this.mockMvc.perform(post("/plans/" + planId + "/delete").with(csrf()))
      .andExpect(redirectedUrl("/plans"));
    this.entityManager.flush();
    this.entityManager.clear();
    this.mockMvc.perform(get("/plans").with(user(OTHER_EMAIL)))
      .andExpect(model().attribute("pendingInvitations", hasSize(0)));
  }

  @Test
  @WithMockUser(username = OWNER_EMAIL)
  public void ownerCanRemoveEditorAndOwnerCannotBeRemoved() throws Exception {
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    User editor = this.userRepository.findById(this.otherId).orElseThrow();
    plan.getParticipants().add(editor);
    plan.getEditors().add(editor);
    this.planRepository.save(plan);
    String removePath = "/plans/" + planId + "/participants/remove";
    // El nombre accesible del botón usa el nombre visible, nunca el email.
    this.mockMvc.perform(get("/plans/" + planId))
      .andExpect(
        org.springframework.test.web.servlet.result.MockMvcResultMatchers
          .content()
          .string(containsString("Eliminar del plan a Test User"))
      );
    this.mockMvc.perform(post(removePath).param("participantEmail", OTHER_EMAIL))
      .andExpect(status().isForbidden());
    this.mockMvc.perform(post(removePath).with(csrf()).param("participantEmail", OWNER_EMAIL))
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    assertThat(plan.isEditor(OTHER_EMAIL), is(true));
    this.mockMvc.perform(post(removePath).with(csrf()).param("participantEmail", OTHER_EMAIL))
      .andExpect(redirectedUrl("/plans/" + planId))
      .andExpect(
        org.springframework.test.web.servlet.result.MockMvcResultMatchers
          .flash()
          .attributeExists("participantSuccess")
      );
    this.entityManager.flush();
    this.entityManager.clear();
    plan = this.planRepository.findById(planId).orElseThrow();
    assertThat(plan.isParticipant(OTHER_EMAIL), is(false));
    assertThat(plan.getEditors(), is(empty()));
    assertThat(this.userRepository.findById(this.otherId).isPresent(), is(true));
    this.mockMvc.perform(get("/plans/" + planId).with(user(OTHER_EMAIL)))
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    this.mockMvc.perform(get("/plans").with(user(OTHER_EMAIL)))
      .andExpect(model().attribute("participantPlans", hasSize(0)));
  }

  @Test
  @WithMockUser(username = OTHER_EMAIL)
  public void editorCanRemoveParticipantButCannotRemoveEditorsOrOwner() throws Exception {
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    User editor = this.userRepository.findById(this.otherId).orElseThrow();
    User guest = this.userRepository.findById(givenUser("guest@test.com")).orElseThrow();
    User secondEditor = this.userRepository.findById(givenUser("editor@test.com")).orElseThrow();
    plan.getParticipants().addAll(List.of(editor, guest, secondEditor));
    plan.getEditors().addAll(List.of(editor, secondEditor));
    this.planRepository.save(plan);
    String html =
      this.mockMvc.perform(get("/plans/" + planId))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    // El editor solo puede eliminar al participante común: ni al dueño, ni a sí mismo, ni a otro
    // editor. El aria-label ya no distingue por email, así que el conteo es la señal.
    assertThat(occurrences(html, "Eliminar del plan a Test User"), is(1));
    assertThat(occurrences(html, "Banear del plan a Test User"), is(1));
    assertThat(
      html,
      containsString("name=\"participantEmail\" value=\"" + guest.getEmail() + "\"")
    );
    String removePath = "/plans/" + planId + "/participants/remove";
    for (String email : List.of(
      OWNER_EMAIL,
      OTHER_EMAIL,
      secondEditor.getEmail(),
      "unknown@test.com"
    )) {
      this.mockMvc.perform(post(removePath).with(csrf()).param("participantEmail", email))
        .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    }
    this.mockMvc.perform(post(removePath).with(csrf()).param("participantEmail", guest.getEmail()))
      .andExpect(redirectedUrl("/plans/" + planId));
    this.entityManager.flush();
    this.entityManager.clear();
    plan = this.planRepository.findById(planId).orElseThrow();
    assertThat(plan.isParticipant(guest.getEmail()), is(false));
    assertThat(plan.isEditor(OTHER_EMAIL), is(true));
    assertThat(plan.isEditor(secondEditor.getEmail()), is(true));
  }

  @Test
  public void publicReadersCannotRemoveParticipants() throws Exception {
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
    Plan plan = this.planRepository.findById(planId).orElseThrow();
    plan.setIsPublic(true);
    plan.getParticipants().add(this.userRepository.findById(this.otherId).orElseThrow());
    this.planRepository.save(plan);
    String removePath = "/plans/" + planId + "/participants/remove";
    this.mockMvc.perform(post(removePath).with(csrf()).param("participantEmail", OTHER_EMAIL))
      .andExpect(redirectedUrlPattern("**/auth/login"));
    this.mockMvc.perform(
        post(removePath).with(user(OTHER_EMAIL)).with(csrf()).param("participantEmail", OTHER_EMAIL)
      )
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    this.mockMvc.perform(
        post(removePath)
          .with(user("visitor@test.com"))
          .with(csrf())
          .param("participantEmail", OTHER_EMAIL)
      )
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));
    this.mockMvc.perform(get("/plans/" + planId).with(user(OTHER_EMAIL)))
      .andExpect(
        org.springframework.test.web.servlet.result.MockMvcResultMatchers
          .content()
          .string(not(containsString("Eliminar del plan")))
      );
    assertThat(plan.isParticipant(OTHER_EMAIL), is(true));
  }

  /**
   * [SCH] Un museo con categoria y coordenadas conocidas: el horario derivado no depende del
   * catalogo sembrado, que cambia con el dataset.
   */
  private UUID givenMuseum() {
    Place place = new Place();
    place.setName("Museo de integracion");
    place.setCategory(PlaceCategory.MUSEUM);
    place.setLatitude(LATITUDE);
    place.setLongitude(LONGITUDE);
    this.placeRepository.save(place);
    return place.getId();
  }

  /** [SCH] Crea un plan por el endpoint con las paradas dadas, en ese orden. */
  private MvcResult createPlanWith(UUID... placeIds) throws Exception {
    MockHttpServletRequestBuilder request = post("/plans")
      .with(csrf())
      .param("name", PLAN_NAME)
      .param("eventDate", EVENT_DATE.toString())
      .param("eventTime", "10:00")
      .param("dayEndTime", "23:00")
      .param("transportMode", "WALK");
    for (int index = 0; index < placeIds.length; index++) {
      request.param("places[" + index + "].placeId", placeIds[index].toString());
    }
    return this.mockMvc.perform(request).andExpect(status().is3xxRedirection()).andReturn();
  }

  private UUID givenUser(String email) {
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

  private UUID givenPlanFor(UUID administratorId, String name) {
    Plan plan = new Plan();
    plan.setName(name);
    plan.setAdministrator(this.userRepository.findById(administratorId).orElseThrow());
    return this.planRepository.save(plan).getId();
  }

  /** [PPV] Un plan visible para el listado publico. */
  private UUID givenPublicPlanFor(UUID administratorId, String name) {
    UUID id = givenPlanFor(administratorId, name);
    Plan plan = this.planRepository.findById(id).orElseThrow();
    plan.setIsPublic(true);
    return this.planRepository.save(plan).getId();
  }

  /**
   * [SEARCH] Plan publico con la fecha y el lugar que necesitan los filtros del feed: el plan no
   * tiene categoria propia, matchea por la categoria del lugar de su itinerario.
   */
  private UUID givenSearchablePublicPlan(
    UUID administratorId,
    String name,
    LocalDate eventDate,
    PlaceCategory category
  ) {
    UUID id = givenPublicPlanFor(administratorId, name);
    Plan plan = this.planRepository.findById(id).orElseThrow();
    plan.setEventDate(eventDate);
    if (category != null) {
      Place place = new Place();
      place.setName("Lugar " + category + " de " + name);
      place.setCategory(category);
      this.placeRepository.save(place);
      PlanPlace entry = new PlanPlace();
      entry.setPlace(place);
      entry.setSortOrder(1);
      plan.addPlanPlace(entry);
    }
    return this.planRepository.save(plan).getId();
  }

  /** [SEARCH] HTML del feed pedido como anonimo: el visitante ve todos los planes publicos. */
  private String forYouHtml(String url) throws Exception {
    return this.mockMvc.perform(get(url))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/plans/public-list"))
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  /**
   * Recorta la seccion de participantes del detalle: el navbar lleva el email de la sesion, asi
   * que las aserciones de privacidad no pueden mirar el HTML entero.
   */
  private static String participantsSection(String html) {
    return html.substring(
      html.indexOf("participants-heading"),
      html.indexOf("id=\"configuracion\"")
    );
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
  private UUID givenPublicPlanWithPlace() {
    UUID planId = givenPlanFor(this.ownerId, PLAN_NAME);
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

  private String cloneLocationOf(UUID planId) throws Exception {
    return this.mockMvc.perform(post("/plans/" + planId + "/clone").with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andReturn()
      .getResponse()
      .getRedirectedUrl();
  }

  private static List<?> plansOf(MvcResult result) {
    return (List<?>) result.getModelAndView().getModel().get("plans");
  }

  private static UUID planIdOf(String redirectUrl) {
    return UUID.fromString(redirectUrl.substring("/plans/".length()));
  }
}
