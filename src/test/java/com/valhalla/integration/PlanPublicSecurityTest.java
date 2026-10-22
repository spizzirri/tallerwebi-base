package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Dos contratos de seguridad sobre la misma FQN, distinguidos por prefijo de nombre:
 *
 * <ul>
 *   <li>[PPV] sin prefijo: el feed {@code GET /for-you} y la API que lo alimenta en /explore,
 *       {@code GET /api/plans/public}, contra la cadena real de {@code SecurityConfig} — son
 *       visibles con y sin sesion (S-01/S-02) y, ya adentro, no filtran informacion privada de
 *       los planes (S-03).
 *   <li>[PVP] con prefijo {@code PVP_}: la vista {@code GET /plans/{id}/public}, escenarios
 *       S-01..S-04 de 13-PVP.md — {@code permitAll} solo para GET y sin volverse oraculo de ids
 *       ni filtro de datos privados.
 *   <li>[CAL] con prefijo {@code CAL_}: escenarios S-01..S-03 de 16-CAL.md — el voto exige
 *       sesion y la autoria de las calificaciones nunca sale al HTML, solo el agregado.
 * </ul>
 */
@WebIntegrationTest
@Transactional
public class PlanPublicSecurityTest {

  // --- [PPV] fixtures del feed ---

  private static final String FEED_OWNER_EMAIL = "dueno.ppv@test.com";
  private static final String FEED_PARTICIPANT_EMAIL = "participante.ppv@test.com";
  private static final String VISITOR_EMAIL = "visitante.ppv@test.com";
  private static final String FEED_PUBLIC_PLAN_NAME = "Plan publico visible";
  private static final String FEED_PRIVATE_PLAN_NAME = "Plan privado oculto";

  // --- [PVP] fixtures de la vista publica ---

  private static final String OWNER_EMAIL = "dueno-pvp@test.com";
  private static final String PARTICIPANT_EMAIL = "participante-pvp@test.com";
  private static final String PUBLIC_PLAN_NAME = "Plan publico PVP";
  private static final String PRIVATE_PLAN_NAME = "Plan privado PVP";
  private static final String PUBLIC_SHORT_CODE = "PVPBLIC01";

  /** Bien formado y no creado: un id numérico ya no llega al servicio, no parsea como UUID. */
  private static final UUID MISSING_PLAN_ID = UUID.fromString(
    "00000000-0000-0000-0000-000000000000"
  );

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PlanRepository planRepository;

  @Autowired
  private PlaceRepository placeRepository;

  private MockMvc mockMvc;

  private UUID publicPlanId;
  private UUID privatePlanId;

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();

    // [PVP] la vista publica siempre tiene plan publico y privado para comparar.
    User owner = new User();
    owner.setEmail(OWNER_EMAIL);
    owner.setPassword("password123");
    owner.setRole("USER");
    owner.setFirstName("Dueno");
    owner.setLastName("PVP");
    this.userRepository.save(owner);

    User participant = new User();
    participant.setEmail(PARTICIPANT_EMAIL);
    participant.setPassword("password123");
    participant.setRole("USER");
    participant.setFirstName("Participante");
    participant.setLastName("PVP");
    this.userRepository.save(participant);

    this.publicPlanId = givenPlan(PUBLIC_PLAN_NAME, true);
    Plan publicPlan = this.planRepository.findById(this.publicPlanId).orElseThrow();
    publicPlan.setShortCode(PUBLIC_SHORT_CODE);
    publicPlan.getParticipants().add(participant);
    this.planRepository.save(publicPlan);

    this.privatePlanId = givenPlan(PRIVATE_PLAN_NAME, false);
  }

  // ============================================================
  // [PPV] Feed: S-01..S-03 de 14-PPV.md
  // ============================================================

  // S-01: el feed es visible sin sesion. El visitante entra igual que el logueado.
  @Test
  public void S01_getForYouAnonimoResponde200() throws Exception {
    givenAPublicPlan();

    this.mockMvc.perform(get("/for-you")).andExpect(status().isOk());
  }

  // S-01b: con sesion el mismo GET responde 200 y rinde el listado.
  @Test
  public void S01b_getForYouConSesionResponde200() throws Exception {
    givenAPublicPlan();

    this.mockMvc.perform(get("/for-you").with(user(VISITOR_EMAIL))).andExpect(status().isOk());
  }

  // S-01c: /explore consume el feed por API. El GET /api/plans/public es permitAll: responde 200
  // en JSON para un anónimo, y solo con planes públicos (el privado nunca aparece, ni su shortCode).
  @Test
  public void S01c_getApiPlansPublicAnonimoResponde200SoloConPlanesPublicos() throws Exception {
    givenAPublicPlan();
    givenAPrivatePlan();

    String json =
      this.mockMvc.perform(get("/api/plans/public"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(json, containsString(FEED_PUBLIC_PLAN_NAME));
    assertThat(json, not(containsString(FEED_PRIVATE_PLAN_NAME)));
    assertThat(json, not(containsString("PPVSECRE")));
    assertThat(json, not(containsString(FEED_OWNER_EMAIL)));
  }

  // S-02: el permitAll es solo GET: cualquier otro verbo sobre la ruta cae en el login. Con token
  // CSRF la request llega al filtro de autorizacion, asi que el redirect prueba la regla y no un
  // 403 de CSRF.
  @Test
  public void S02_postForYouAnonimoRedirigeALogin() throws Exception {
    this.mockMvc.perform(post("/for-you").with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrlPattern("**/auth/login"));
  }

  // S-03: ni shortCode, ni emails de participantes ni datos de planes privados.
  @Test
  public void S03_elListadoNoExponeInformacionPrivada() throws Exception {
    Plan publicPlan = givenAPublicPlan();
    givenAPrivatePlan();

    String html =
      this.mockMvc.perform(get("/for-you").with(user(VISITOR_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(html, containsString(FEED_PUBLIC_PLAN_NAME));
    assertThat(html, not(containsString(publicPlan.getShortCode())));
    assertThat(html, not(containsString(FEED_PARTICIPANT_EMAIL)));
    assertThat(html, not(containsString(FEED_OWNER_EMAIL)));
    assertThat(html, not(containsString(FEED_PRIVATE_PLAN_NAME)));
  }

  // ============================================================
  // [PVP] Vista publica: S-01..S-04 de 13-PVP.md
  // ============================================================

  /** S-01 (AC-09): the GET is permitAll — 200 without a session and without a login redirect. */
  @Test
  public void PVP_S01_getPlanPublicoAnonimoResponde200SinRedirigir() throws Exception {
    this.mockMvc.perform(get("/plans/" + this.publicPlanId + "/public")).andExpect(status().isOk());
  }

  /** Private plan links require login and never expose private content to anonymous visitors. */
  @Test
  public void PVP_S02_getPlanPrivadoAnonimoNoResponde200ConContenido() throws Exception {
    MvcResult privateResult =
      this.mockMvc.perform(get("/plans/" + this.privatePlanId + "/public"))
        .andExpect(status().is3xxRedirection())
        .andReturn();
    this.mockMvc.perform(get("/plans/" + MISSING_PLAN_ID + "/public"))
      .andExpect(status().is3xxRedirection())
      .andReturn();

    assertThat(
      privateResult.getResponse().getRedirectedUrl(),
      is("/auth/login?redirect=/plans/" + this.privatePlanId + "/public")
    );
    assertThat(
      privateResult.getResponse().getContentAsString(),
      not(containsString(PRIVATE_PLAN_NAME))
    );
  }

  /** S-03 (AC-10): permitAll covers GET only — a POST still goes through the login gate. */
  @Test
  public void PVP_S03_postPlanPublicoAnonimoRedirigeALogin() throws Exception {
    this.mockMvc.perform(post("/plans/" + this.publicPlanId + "/public").with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrlPattern("**/auth/login"));
  }

  /** S-04 (AC-04): no participant emails, no shortCode and no private-plan data in the HTML. */
  @Test
  public void PVP_S04_getPlanPublicoNoFiltraEmailsNiDatosDePlanesPrivados() throws Exception {
    String html =
      this.mockMvc.perform(get("/plans/" + this.publicPlanId + "/public"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(html, not(containsString(PARTICIPANT_EMAIL)));
    assertThat(html, not(containsString(OWNER_EMAIL)));
    assertThat(html, not(containsString(PUBLIC_SHORT_CODE)));
    assertThat(html, not(containsString(PRIVATE_PLAN_NAME)));
    assertThat(html, containsString(PUBLIC_PLAN_NAME));
  }

  // ============================================================
  // [CAL] Calificaciones: S-01..S-03 de 16-CAL.md
  // ============================================================

  /** S-01 (AC-13): regression guard — el permitAll de la vista publica sigue igual con el widget. */
  @Test
  public void CAL_S01_getPlanPublicoConWidgetSiguePermitAllParaAnonimo() throws Exception {
    this.mockMvc.perform(get("/plans/" + this.publicPlanId + "/public")).andExpect(status().isOk());
  }

  /** S-02 (AC-13): el voto exige sesion; el POST anonimo no llega a calificar. */
  @Test
  public void CAL_S02_postRatingsAnonimoRedirigeALogin() throws Exception {
    this.mockMvc.perform(
        post("/plans/" + this.publicPlanId + "/ratings").with(csrf()).param("score", "5")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrlPattern("**/auth/login"));
  }

  /** S-03 (AC-02): la autoria de las calificaciones es interna, el HTML solo muestra agregados. */
  @Test
  public void CAL_S03_lasVistasNoExponenIdentidadesDeCalificadores() throws Exception {
    String raterEmail = "votante.pvp@test.com";
    User rater = givenUser(raterEmail);
    rater.setFirstName("Votante");
    rater.setLastName("Oculto");
    this.userRepository.save(rater);
    this.mockMvc.perform(
        post("/plans/" + this.publicPlanId + "/ratings")
          .with(user(raterEmail))
          .with(csrf())
          .param("score", "5")
      )
      .andExpect(status().is3xxRedirection());

    String html =
      this.mockMvc.perform(get("/plans/" + this.publicPlanId + "/public"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    String feed =
      this.mockMvc.perform(get("/for-you").with(user(VISITOR_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // ni el email, ni el nombre del votante aparecen en ninguna de las dos paginas
    assertThat(html, not(containsString(raterEmail)));
    assertThat(html, not(containsString("Votante Oculto")));
    assertThat(feed, not(containsString(raterEmail)));
    // en cambio, el agregado publico si sale
    assertThat(html, containsString("5.0"));
  }

  // --- fixtures [PVP] ---

  private UUID givenPlan(String name, boolean isPublic) {
    Plan plan = new Plan();
    plan.setName(name);
    plan.setIsPublic(isPublic);
    plan.setAdministrator(this.userRepository.findByEmail(OWNER_EMAIL).orElseThrow());
    return this.planRepository.save(plan).getId();
  }

  // --- fixtures [PPV] ---

  private Plan givenAPublicPlan() {
    User owner = givenUser(FEED_OWNER_EMAIL);
    Plan plan = new Plan();
    plan.setName(FEED_PUBLIC_PLAN_NAME);
    plan.setAdministrator(owner);
    plan.setShortCode("PPVSECRE");
    plan.setIsPublic(true);
    User participant = givenUser(FEED_PARTICIPANT_EMAIL);
    plan.getParticipants().add(participant);
    PlanPlace entry = new PlanPlace();
    entry.setPlace(this.placeRepository.findAll().get(0));
    plan.addPlanPlace(entry);
    return this.planRepository.save(plan);
  }

  private void givenAPrivatePlan() {
    Plan plan = new Plan();
    plan.setName(FEED_PRIVATE_PLAN_NAME);
    plan.setAdministrator(this.userRepository.findByEmail(FEED_OWNER_EMAIL).orElseThrow());
    this.planRepository.save(plan);
  }

  private User givenUser(String email) {
    User user = new User();
    user.setEmail(email);
    user.setPassword("password123");
    user.setRole("USER");
    user.setFirstName("Nombre");
    user.setLastName("Apellido");
    user.activate();
    this.userRepository.save(user);
    return this.userRepository.findByEmail(email).orElseThrow();
  }
}
