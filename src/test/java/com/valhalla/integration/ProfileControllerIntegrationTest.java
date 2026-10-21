package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import com.valhalla.domain.user.UserService;
import jakarta.servlet.Filter;
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
 * [PROF] Superficie de perfil: atajo /profile, doble vista por id, edicion self-service, cambio
 * de contrasena, avatar y sus reglas. Escenarios docs/specs/11-PROF.md (los de tema quedan en
 * PROF-010/011, gated al merge de [THEME], y no estan aca).
 *
 * <p>Los usuarios viven en DB real (HSQLDB): {@code @WithMockUser} solo trae el email, el
 * controller lo cruza por {@code UserService.getByEmail}, asi que el usuario de sesion se crea
 * en el {@code @BeforeEach}.
 */
@WebIntegrationTest
@Transactional
public class ProfileControllerIntegrationTest {

  private static final String SELF_EMAIL = "profile.owner@test.com";
  private static final String OTHER_EMAIL = "profile.visitor@test.com";
  private static final String PASSWORD = "la-password-de-siempre";
  private static final String NEW_PASSWORD_8 = "la-nueva-de-minimo-ocho";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PlanService planService;

  private MockMvc mockMvc;

  private Long selfId;
  private Long otherId;

  @BeforeEach
  public void setUp() {
    this.mockMvc =
      MockMvcBuilders
        .webAppContextSetup(this.wac)
        .apply(springSecurity())
        .addFilter((Filter) new HiddenHttpMethodFilter())
        .build();

    User self = new User();
    self.setEmail(SELF_EMAIL);
    self.setPassword(enc(PASSWORD));
    self.setRole("USER");
    self.setFirstName("Ada");
    self.setLastName("Lovelace");
    self.setActive(true);
    userRepository.save(self);
    selfId = self.getId();

    User other = new User();
    other.setEmail(OTHER_EMAIL);
    other.setPassword(enc(PASSWORD));
    other.setRole("USER");
    other.setFirstName("Grace");
    other.setLastName("Hopper");
    other.setActive(true);
    userRepository.save(other);
    otherId = other.getId();
  }

  private String enc(String raw) {
    return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(raw);
  }

  // --- I-01 ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void shortcutRedirectsToTheOwnProfileUrl() throws Exception {
    this.mockMvc.perform(get("/profile"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/profile/" + selfId));
  }

  // --- I-02: sin sesion, el atajo cae en la default chain (login) ---

  @Test
  public void profileShortcutRequiresSession() throws Exception {
    this.mockMvc.perform(get("/profile"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrlPattern("**/auth/login*"));
  }

  // --- I-03 ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void ownProfileShowsTheEditor() throws Exception {
    this.mockMvc.perform(get("/profile/" + selfId))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/profile/edit"))
      .andExpect(model().attributeExists("profileForm", "owner"))
      // Regresion de charset: el view resolver de Thymeleaf responde con
      // iso-8859-1 si nadie lo fija; con ese header el browser construye los
      // bodies de los forms en Latin-1 y Jetty los rechaza con 400 "Invalid
      // UTF-8" al primer acento (fix de BaseWebConfig).
      .andExpect(content().contentType("text/html;charset=UTF-8"))
      .andExpect(content().string(containsString("Mi perfil")));
  }

  // --- I-04 ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void someoneElseProfileShowsThePublicView() throws Exception {
    this.mockMvc.perform(get("/profile/" + otherId))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/profile/view"))
      .andExpect(content().string(containsString("Grace Hopper")));
  }

  // --- I-28 / S-06 (v2.3): la vista publica es abierta para anonimos ---

  @Test
  public void anonymousVisitorsSeeThePublicProfile() throws Exception {
    this.mockMvc.perform(get("/profile/" + selfId))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/profile/view"))
      .andExpect(content().string(containsString("Ada Lovelace")));
  }

  // --- EXPOSICION (S-04): la vista publica no sale con el email ---

  @Test
  public void publicViewDoesNotExposeTheOwnerEmail() throws Exception {
    MvcResult result = this.mockMvc.perform(get("/profile/" + selfId)).andReturn();
    assertThat(result.getResponse().getContentAsString(), not(containsString(SELF_EMAIL)));
  }

  // --- PLANES (AC-15): solo los públicos del dueño, en ningún otro caso ---

  @Test
  public void publicViewListsOnlyTheOwnersPublicPlansAndHidesPrivateOnes() throws Exception {
    Plan publicPlan = new Plan();
    publicPlan.setName("Plan publico de Ada");
    publicPlan.setIsPublic(true);
    publicPlan.setAdministrator(userRepository.findById(selfId).orElseThrow());
    planService.createPlan(publicPlan, SELF_EMAIL);

    Plan privatePlan = new Plan();
    privatePlan.setName("Plan privado de Ada");
    privatePlan.setAdministrator(userRepository.findById(selfId).orElseThrow());
    planService.createPlan(privatePlan, SELF_EMAIL);

    String html =
      this.mockMvc.perform(get("/profile/" + selfId))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(html, containsString("Plan publico de Ada"));
    assertThat(html, not(containsString("Plan privado de Ada")));
  }

  // --- PLANES: la vista de Grace muestra los planes de Grace, y los de Ada no aparecen ---

  @Test
  public void publicViewShowsTheOwnersPublicPlansOnly() throws Exception {
    Plan adaPlan = new Plan();
    adaPlan.setName("Plan publico de Ada");
    adaPlan.setIsPublic(true);
    adaPlan.setAdministrator(userRepository.findById(selfId).orElseThrow());
    planService.createPlan(adaPlan, SELF_EMAIL);

    Plan gracePlan = new Plan();
    gracePlan.setName("Plan publico de Grace");
    gracePlan.setIsPublic(true);
    gracePlan.setAdministrator(userRepository.findById(otherId).orElseThrow());
    planService.createPlan(gracePlan, OTHER_EMAIL);

    String html =
      this.mockMvc.perform(get("/profile/" + otherId))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(html, containsString("Grace Hopper"));
    assertThat(html, containsString("Plan publico de Grace"));
    assertThat(html, not(containsString("Plan publico de Ada")));
  }

  // --- I-07: el POST valida y persiste el perfil ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void profileUpdatePersistsNamesAndAvatarAndRedirects() throws Exception {
    String stack = "/images/avatars/avatar-2.svg";
    this.mockMvc.perform(
        post("/profile")
          .with(csrf())
          .param("firstName", "Ada")
          .param("lastName", "Byron")
          .param("avatarUrl", stack)
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/profile/" + selfId + "?updated=true"));

    User reloaded = userRepository.findById(selfId).orElseThrow();
    assertThat(reloaded.getLastName(), is(equalTo("Byron")));
    assertThat(reloaded.getAvatarUrl(), is(equalTo(stack)));
  }

  // --- I-08: un nombre vacio rechaza la edicion sin writer nada ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void profileUpdateRejectsBlankNamesAndRerendersTheEditor() throws Exception {
    this.mockMvc.perform(
        post("/profile").with(csrf()).param("firstName", "  ").param("lastName", "Lovelace")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("pages/profile/edit"))
      .andExpect(model().attributeHasFieldErrors("profileForm", "firstName"));

    assertThat(
      userRepository.findById(selfId).orElseThrow().getLastName(),
      is(equalTo("Lovelace"))
    );
  }

  // --- I-07 (avatar): la galeria se acepta y la URL https tambien (AC-25) ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void profileUpdateAcceptsGalleryPathsAndHttpsAvatars() throws Exception {
    this.mockMvc.perform(
        post("/profile")
          .with(csrf())
          .param("firstName", "Ada")
          .param("lastName", "Lovelace")
          .param("avatarUrl", "https://example.com/me.png")
      )
      .andExpect(status().is3xxRedirection());

    this.mockMvc.perform(
        post("/profile")
          .with(csrf())
          .param("firstName", "Ada")
          .param("lastName", "Lovelace")
          .param("avatarUrl", "/images/avatars/avatar-1.svg")
      )
      .andExpect(status().is3xxRedirection());
  }

  // --- I-29 (navbar): el avatar recien actualizado sale en la proxima render del navbar ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void theNavbarReflectsTheAvatarJustUpdated() throws Exception {
    this.mockMvc.perform(
        post("/profile")
          .with(csrf())
          .param("firstName", "Ada")
          .param("lastName", "Lovelace")
          .param("avatarUrl", "/images/avatars/avatar-2.svg")
      )
      .andExpect(status().is3xxRedirection());

    // La pagina del editor se vuelve a pedir como cualquier otra: el navbar server-rendered
    // (CurrentUserControllerAdvice.userAvatarUrl) ya lleva el <img> del avatar sin cache ni JS.
    this.mockMvc.perform(get("/profile/" + selfId))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("id=\"navbar-avatar\"")))
      .andExpect(content().string(containsString("src=\"/images/avatars/avatar-2.svg\"")));
  }

  // --- I-08 (avatar): javascript:, data: y http:// rechazan (AC-25) ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void profileUpdateRejectsInsecureAvatarUrls() throws Exception {
    for (String bad : new String[] {
      "javascript:alert(1)",
      "data:image/svg+xml;base64,AAAA",
      "http://example.com/me.png",
      "/etc/passwd",
    }) {
      MvcResult result =
        this.mockMvc.perform(
            post("/profile")
              .with(csrf())
              .param("firstName", "Ada")
              .param("lastName", "Lovelace")
              .param("avatarUrl", bad)
          )
          .andExpect(status().isOk())
          .andExpect(view().name("pages/profile/edit"))
          .andReturn();

      assertThat(
        "rejecting " + bad,
        result
          .getModelAndView()
          .getModel()
          .get("org.springframework.validation.BindingResult.profileForm")
          .toString(),
        containsString("El avatar debe ser una URL segura")
      );
    }

    // Lo que llega al repositorio no trae los intentos peligrosos y no cambió ada el nombre.
    User reloaded = userRepository.findById(selfId).orElseThrow();
    assertThat(reloaded.getAvatarUrl(), is(not(containsString("javascript:"))));
    assertThat(reloaded.getFirstName(), is(equalTo("Ada")));
  }

  // --- I-13 / AC-20: un perfil inexistente responde como recurso no disponible, no como redirect ---

  @Test
  public void unknownProfileReturnsNotFoundJsonSchema() throws Exception {
    this.mockMvc.perform(get("/profile/424242")).andExpect(status().isNotFound());
  }

  // --- S-06: no hay POST sobre /profile/{id}: las escrituras viven en la sesion (AC-04) ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void thereIsNoWriteEndpointWithAForeignId() throws Exception {
    this.mockMvc.perform(post("/profile/" + otherId).with(csrf()).param("firstName", "Hacked"))
      .andExpect(status().is4xxClientError());

    User untouched = userRepository.findById(otherId).orElseThrow();
    assertThat(untouched.getFirstName(), is(equalTo("Grace")));
  }

  // --- S-01/S-02: los POST del perfil exigen CSRF ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void profileWritesWithoutCsrfAreRejected() throws Exception {
    this.mockMvc.perform(post("/profile").param("firstName", "Ada").param("lastName", "Lovelace"))
      .andExpect(status().isForbidden());

    this.mockMvc.perform(
        post("/profile/change-password")
          .param("currentPassword", PASSWORD)
          .param("newPassword", NEW_PASSWORD_8)
          .param("confirmPassword", NEW_PASSWORD_8)
      )
      .andExpect(status().isForbidden());

    // Nada se escribio (S-01/S-02: rechazo antes de llegar al servicio).
    assertThat(
      new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
        .matches(PASSWORD, userRepository.findById(selfId).orElseThrow().getPassword()),
      is(true)
    );
  }

  // --- S-03: el ADMIN tambien ve su propio editor por la misma superficie ---

  @Test
  @WithMockUser(username = SELF_EMAIL, roles = { "ADMIN" })
  public void adminSeesTheOwnEditorThroughTheUserSurface() throws Exception {
    this.mockMvc.perform(get("/profile/" + selfId))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/profile/edit"))
      .andExpect(model().attributeExists("profileForm"));
  }

  // --- I-22 apuntado: las cards del feed linkean al perfil del autor ---

  @Test
  public void feedCardsLinkToTheAuthorsProfile() throws Exception {
    Plan publicPlan = new Plan();
    publicPlan.setName("Plan con link de autor");
    publicPlan.setIsPublic(true);
    publicPlan.setAdministrator(userRepository.findById(selfId).orElseThrow());
    planService.createPlan(publicPlan, SELF_EMAIL);

    String html =
      this.mockMvc.perform(get("/for-you"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // AC-22: "Por {autor}" linkea a /profile/{id} del dueno de la card.
    assertThat(html, containsString("href=\"/profile/" + selfId + "\""));
    assertThat(html, containsString("Ada Lovelace"));
  }

  // --- I-26/I-27: el registro self-service acepta galeria y rechaza URL inseguras ---

  @Test
  public void registerAcceptsAGalleryAvatarAndRejectsInsecureUrls() throws Exception {
    this.mockMvc.perform(
        post("/auth/register")
          .with(csrf())
          .param("firstName", "Linas")
          .param("lastName", "Torvalds")
          .param("email", "gallery.avatar@test.com")
          .param("password", "contrasena")
          .param("confirmPassword", "contrasena")
          .param("avatarUrl", "/images/avatars/avatar-3.svg")
      )
      .andExpect(status().is3xxRedirection());

    User persisted = userRepository.findByEmail("gallery.avatar@test.com").orElseThrow();
    assertThat(persisted.getAvatarUrl(), is(equalTo("/images/avatars/avatar-3.svg")));

    this.mockMvc.perform(
        post("/auth/register")
          .with(csrf())
          .param("firstName", "Trudy")
          .param("lastName", "Insecure")
          .param("email", "bad.avatar@test.com")
          .param("password", "contrasena")
          .param("confirmPassword", "contrasena")
          .param("avatarUrl", "javascript:alert(1)")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("pages/auth/user/register"))
      .andExpect(model().attributeHasFieldErrors("registerRequest", "avatarUrl"));

    assertThat(userRepository.existsByEmail("bad.avatar@test.com"), is(false));
  }

  // --- I-09: el cambio de contraseÑa exige la actual ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void passwordChangeRequiresTheCurrentOne() throws Exception {
    this.mockMvc.perform(
        post("/profile/change-password")
          .with(csrf())
          .param("currentPassword", "contrasena-equivocada")
          .param("newPassword", NEW_PASSWORD_8)
          .param("confirmPassword", NEW_PASSWORD_8)
      )
      .andExpect(status().isOk())
      .andExpect(view().name("pages/profile/change-password"));

    User reloaded = userRepository.findById(selfId).orElseThrow();
    assertThat(
      new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
        .matches(PASSWORD, reloaded.getPassword()),
      is(true)
    );
  }

  // --- AC-08: minimo 8 caracteres ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void passwordChangeRejectsTooShortNewPasswords() throws Exception {
    this.mockMvc.perform(
        post("/profile/change-password")
          .with(csrf())
          .param("currentPassword", PASSWORD)
          .param("newPassword", "12345")
          .param("confirmPassword", "12345")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("pages/profile/change-password"))
      .andExpect(model().attributeHasFieldErrors("changePasswordForm", "newPassword"));

    assertThat(
      new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
        .matches(PASSWORD, userRepository.findById(selfId).orElseThrow().getPassword()),
      is(true)
    );
  }

  // --- I-10: la nueva contraseÑa se guarda codificado y deja de operar la vieja ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void passwordChangeStoresTheNewHashAndStopsTheOldPassword() throws Exception {
    this.mockMvc.perform(
        post("/profile/change-password")
          .with(csrf())
          .param("currentPassword", PASSWORD)
          .param("newPassword", NEW_PASSWORD_8)
          .param("confirmPassword", NEW_PASSWORD_8)
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/profile/" + selfId + "?passwordChanged=true"));

    User reloaded = userRepository.findById(selfId).orElseThrow();
    assertThat(reloaded.getPassword(), is(not(equalTo(PASSWORD))));
    assertThat(reloaded.getPassword(), not(containsString(NEW_PASSWORD_8)));
    assertThat(
      new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
        .matches(NEW_PASSWORD_8, reloaded.getPassword()),
      is(true)
    );
  }

  // --- AC-07: la confirmacion distinta rechaza ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void passwordChangeRejectsMismatchedConfirmation() throws Exception {
    this.mockMvc.perform(
        post("/profile/change-password")
          .with(csrf())
          .param("currentPassword", PASSWORD)
          .param("newPassword", NEW_PASSWORD_8)
          .param("confirmPassword", "otra-distinta")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("pages/profile/change-password"))
      .andExpect(model().attributeHasFieldErrors("changePasswordForm", "passwordsMatch"));
  }

  // --- El form de cambio de contraseÑa: sin id, solo sesion propia (AC-06) ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void passwordChangeFormRendersForTheSessionOwner() throws Exception {
    this.mockMvc.perform(get("/profile/change-password"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/profile/change-password"))
      .andExpect(model().attributeExists("changePasswordForm"))
      .andExpect(content().string(containsString("Cambiar contrasena")));
  }
}
