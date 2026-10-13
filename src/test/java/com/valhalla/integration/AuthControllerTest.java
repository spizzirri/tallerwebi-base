package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.valhalla.domain.login.LoginService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import com.valhalla.domain.user.UserService;
import com.valhalla.presentation.shared.LoginRedirects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * End-to-end-ish coverage of the three security chains behind the public auth flow.
 * Scenario ids I-01..I-13 of docs/specs/08-AUT.md.
 *
 * <p>I-11 ({@code GET /explore} anonymous -> {@code /auth/login}) and I-12 ({@code GET /plans}
 * anonymous -> {@code /auth/login}) are already covered by
 * {@code SecurityConfigTest#shouldRedirectToLoginWhenAccessingPlacesWithoutSession} and its
 * sibling, so they are not duplicated here.
 *
 * <p>The {@code SecurityConfig} permission checks for these same routes live in
 * {@code SecurityConfigTest} as S-01 and S-05, where the spec files them; this class owns what
 * the routes render.
 */
@WebIntegrationTest
public class AuthControllerTest {

  private static final String USER_EMAIL = "aut.user@unlam.edu.ar";
  private static final String USER_PASSWORD = "user-password";
  private static final String PASSWORD = "secret123";
  private static final String FIRST_NAME = "Ana";
  private static final String LAST_NAME = "Perez";
  private static final String VALIDATE_LOGIN = "/auth/validate-login";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private LoginService loginService;

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
    if (userRepository.findByEmail(USER_EMAIL).isEmpty()) {
      loginService.register(USER_EMAIL, USER_PASSWORD);
    }
  }

  // --- I-01 ---

  @Test
  public void shouldRenderTheRegisterPage() throws Exception {
    this.mockMvc.perform(get("/auth/register"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/auth/user/register"))
      .andExpect(content().string(containsString("id=\"firstName\"")))
      .andExpect(content().string(containsString("id=\"lastName\"")))
      .andExpect(content().string(containsString("id=\"confirmPassword\"")));
  }

  // --- I-17 ---

  @Test
  public void shouldRenderTheForgotPasswordPage() throws Exception {
    this.mockMvc.perform(get("/auth/forgot-password"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/auth/user/forgot-password"))
      // The submit button only renders if Thymeleaf bound the recoverPasswordRequest form object.
      .andExpect(content().string(containsString("id=\"btn-recover\"")));
  }

  // --- I-02 ---

  /**
   * Registrarse deja la sesión iniciada: el redirect al landing es solo la primera prueba, la real
   * es que esa misma sesión alcanza una página que exige estar autenticado sin volver a loguearse.
   */
  @Test
  public void shouldRegisterAndSignInTheUserRightAway() throws Exception {
    String email = uniqueEmail();
    MockHttpSession session = new MockHttpSession();

    this.mockMvc.perform(
        post("/auth/register")
          .session(session)
          .with(csrf())
          .param("firstName", FIRST_NAME)
          .param("lastName", LAST_NAME)
          .param("email", email)
          .param("password", PASSWORD)
          .param("confirmPassword", PASSWORD)
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(LoginRedirects.USER_LANDING));

    this.mockMvc.perform(get("/plans").session(session))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("action=\"/auth/logout\"")));
  }

  // --- I-03 ---

  @Test
  public void shouldReRenderRegisterWithAnErrorOnDuplicateEmail() throws Exception {
    String email = uniqueEmail();
    register(email, PASSWORD);

    this.mockMvc.perform(
        post("/auth/register")
          .with(csrf())
          .param("firstName", FIRST_NAME)
          .param("lastName", LAST_NAME)
          .param("email", email)
          .param("password", PASSWORD)
          .param("confirmPassword", PASSWORD)
      )
      .andExpect(status().isOk())
      .andExpect(view().name("pages/auth/user/register"))
      .andExpect(content().string(containsString("Ese email ya está registrado")));
  }

  @Test
  public void shouldReRenderRegisterWithValidationErrors() throws Exception {
    this.mockMvc.perform(
        post("/auth/register")
          .with(csrf())
          .param("firstName", FIRST_NAME)
          .param("lastName", LAST_NAME)
          .param("email", "not-an-email")
          .param("password", "123")
          .param("confirmPassword", "123")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("pages/auth/user/register"))
      .andExpect(content().string(containsString("Email is not valid")))
      .andExpect(content().string(containsString("Password must be at least 6 characters")));
  }

  @Test
  public void shouldReRenderRegisterWhenPasswordsDoNotMatch() throws Exception {
    String email = uniqueEmail();

    this.mockMvc.perform(
        post("/auth/register")
          .with(csrf())
          .param("firstName", FIRST_NAME)
          .param("lastName", LAST_NAME)
          .param("email", email)
          .param("password", PASSWORD)
          .param("confirmPassword", "otra-clave")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("pages/auth/user/register"))
      .andExpect(content().string(containsString("Las contraseñas no coinciden")));

    assertThat(
      "a mismatched confirmation must not create the user",
      userRepository.findByEmail(email).isPresent(),
      is(false)
    );
  }

  // --- I-04 ---

  @Test
  public void shouldLandOnPlansWhenAUserLogsIn() throws Exception {
    this.mockMvc.perform(
        post(VALIDATE_LOGIN)
          .with(csrf())
          .param("username", USER_EMAIL)
          .param("password", USER_PASSWORD)
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(LoginRedirects.USER_LANDING));
  }

  @Test
  public void shouldLandOnAdminHomeWhenAnAdminLogsInThroughTheUserForm() throws Exception {
    String email = uniqueEmail();
    userService.create(email, PASSWORD, "ADMIN", "Admin", "AUT");

    this.mockMvc.perform(
        post(VALIDATE_LOGIN).with(csrf()).param("username", email).param("password", PASSWORD)
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/home"));
  }

  // --- I-05 ---

  @Test
  public void shouldRedirectToLoginWithErrorOnWrongPassword() throws Exception {
    this.mockMvc.perform(
        post(VALIDATE_LOGIN)
          .with(csrf())
          .param("username", USER_EMAIL)
          .param("password", "wrong-password")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/auth/login?error=true"));
  }

  // --- I-06 ---

  @Test
  public void shouldRedirectToLoginWithErrorForADeactivatedUser() throws Exception {
    String email = uniqueEmail();
    userService.create(email, PASSWORD, "USER", "Deactivated", "AUT");
    User user = userRepository.findByEmail(email).orElseThrow();
    userService.deactivate(user.getId());

    this.mockMvc.perform(
        post(VALIDATE_LOGIN).with(csrf()).param("username", email).param("password", PASSWORD)
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/auth/login?error=true"));
  }

  // --- I-07 ---

  @Test
  public void shouldRejectLoginPostWithoutCsrf() throws Exception {
    this.mockMvc.perform(
        post(VALIDATE_LOGIN).param("username", USER_EMAIL).param("password", USER_PASSWORD)
      )
      .andExpect(status().isForbidden());
  }

  // --- I-08 ---

  @Test
  public void shouldRotateThePasswordAndInvalidateTheOldOne() throws Exception {
    String email = uniqueEmail();
    register(email, PASSWORD);

    String tempPassword = extractTempPassword(
      this.mockMvc.perform(post("/auth/recover").with(csrf()).param("email", email))
    );

    logIn(email, PASSWORD)
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/auth/login?error=true"));

    logIn(email, tempPassword)
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(LoginRedirects.USER_LANDING));
  }

  // --- I-09 ---

  @Test
  public void shouldTellTheUserTheEmailIsUnknownOnRecovery() throws Exception {
    this.mockMvc.perform(post("/auth/recover").with(csrf()).param("email", "nadie@unlam.edu.ar"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/auth/user/forgot-password"))
      .andExpect(content().string(containsString("Email no encontrado")))
      .andExpect(content().string(not(containsString("temp-password"))));
  }

  // --- I-10 ---

  @Test
  public void shouldRedirectToLoginAndInvalidateTheSessionOnLogout() throws Exception {
    MockHttpSession session = new MockHttpSession();
    this.mockMvc.perform(
        post(VALIDATE_LOGIN)
          .session(session)
          .with(csrf())
          .param("username", USER_EMAIL)
          .param("password", USER_PASSWORD)
      )
      .andExpect(redirectedUrl(LoginRedirects.USER_LANDING));

    this.mockMvc.perform(post("/auth/logout").session(session).with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/auth/login?logout=true"));

    assertThat("session must be invalidated after logout", session.isInvalid(), is(true));
  }

  // --- Regresión: /auth/login con sesión activa ---

  /**
   * El mismo bug que se reportó en /admin/login, en la otra página de login: el navbar oculta el
   * link pero el formulario se renderiza igual. La regla y el destino son los mismos para las dos.
   */
  @Test
  @WithMockUser(username = "user@unlam.edu.ar", roles = { "USER" })
  public void shouldRedirectAwayFromTheUserLoginPageWhenAlreadySignedIn() throws Exception {
    this.mockMvc.perform(get("/auth/login"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(LoginRedirects.USER_LANDING));
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldSendAnAdminToTheAdminHomeFromTheUserLoginPage() throws Exception {
    this.mockMvc.perform(get("/auth/login"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/home"));
  }

  /** El logout de /auth manda acá con ?logout=true y la sesión ya invalidada: tiene que renderizar. */
  @Test
  public void shouldStillRenderTheUserLoginPageAfterLogout() throws Exception {
    this.mockMvc.perform(get("/auth/login").param("logout", "true"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/auth/user/login"));
  }

  // --- I-13 ---

  @Test
  public void shouldKeepAdminUsersBehindTheAdminLogin() throws Exception {
    this.mockMvc.perform(get("/admin/users"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrlPattern("**/admin/login"));
  }

  // --- AC-13 ---

  @Test
  public void shouldShowRegisterLinkInNavbarWhenAnonymous() throws Exception {
    this.mockMvc.perform(get("/auth/login"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("href=\"/auth/register\"")));
  }

  @Test
  @WithMockUser(username = "user@unlam.edu.ar", roles = { "USER" })
  public void shouldShowLogoutFormInNavbarOnceSignedIn() throws Exception {
    this.mockMvc.perform(get("/"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("action=\"/auth/logout\"")));
  }

  /** La variante admin del navbar vive solo en /admin/**: fuera de ahí un ADMIN navega como usuario. */
  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldShowTheUserNavbarWhenAnAdminBrowsesTheUserArea() throws Exception {
    this.mockMvc.perform(get("/explore"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("href=\"/explore\"")))
      .andExpect(content().string(not(containsString("href=\"/admin/users\""))));
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldShowTheAdminNavbarInsideTheAdminArea() throws Exception {
    this.mockMvc.perform(get("/admin/home"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("href=\"/admin/users\"")))
      .andExpect(content().string(not(containsString("href=\"/explore\""))));
  }

  // --- AC-04, AC-14 ---

  @Test
  public void shouldKeepAdminPermissionsAfterLoggingInThroughTheUserForm() throws Exception {
    String email = uniqueEmail();
    userService.create(email, PASSWORD, "ADMIN", "Admin", "AUT");
    MockHttpSession session = logInAndKeepSession(email, PASSWORD);

    this.mockMvc.perform(get("/admin/users").session(session)).andExpect(status().isOk());
  }

  /**
   * El POST del form de usuario sigue autenticando a un ADMIN y lo deja en su home con los
   * permisos intactos, aunque la página {@code /auth/login} ya no le sea alcanzable (S-07): la
   * página se niega en el filtro de autorización, que corre después de que el login ya autenticó y
   * cortó la cadena. Sin este assert, el test anterior probaría lo mismo con un 3xx cualquiera.
   */
  @Test
  public void shouldLandAnAdminOnTheAdminHomeWhenTheySubmitTheUserForm() throws Exception {
    String email = uniqueEmail();
    userService.create(email, PASSWORD, "ADMIN", "Admin", "AUT");

    this.mockMvc.perform(
        post(VALIDATE_LOGIN).with(csrf()).param("username", email).param("password", PASSWORD)
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/home"));
  }

  // --- helpers ---

  private static String uniqueEmail() {
    return "aut-" + java.util.UUID.randomUUID().toString().substring(0, 8) + "@unlam.edu.ar";
  }

  private void register(String email, String password) throws Exception {
    this.mockMvc.perform(
        post("/auth/register")
          .with(csrf())
          .param("firstName", FIRST_NAME)
          .param("lastName", LAST_NAME)
          .param("email", email)
          .param("password", password)
          .param("confirmPassword", password)
      )
      .andExpect(status().is3xxRedirection());
  }

  private ResultActions logIn(String email, String password) throws Exception {
    MockHttpServletRequestBuilder request = post(VALIDATE_LOGIN)
      .with(csrf())
      .param("username", email)
      .param("password", password);
    return this.mockMvc.perform(request);
  }

  /**
   * Logs in through the user form and hands the session back, so a later request can prove the
   * session it produced is the one being authorized.
   */
  private MockHttpSession logInAndKeepSession(String email, String password) throws Exception {
    MockHttpSession session = new MockHttpSession();
    this.mockMvc.perform(
        post(VALIDATE_LOGIN)
          .session(session)
          .with(csrf())
          .param("username", email)
          .param("password", password)
      )
      .andExpect(status().is3xxRedirection());
    return session;
  }

  private static String extractTempPassword(ResultActions actions) throws Exception {
    String body = actions.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    java.util.regex.Matcher matcher = java.util.regex.Pattern
      .compile("id=\"temp-password\"[^>]*>([^<]+)<")
      .matcher(body);
    assertThat("the recovery view must show a temporary password", matcher.find(), is(true));
    assertThat(matcher.group(1).trim().isEmpty(), is(false));
    return matcher.group(1).trim();
  }
}
