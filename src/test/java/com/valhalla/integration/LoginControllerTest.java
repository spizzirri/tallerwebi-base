package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.valhalla.domain.login.LoginService;
import com.valhalla.domain.user.UserRepository;
import com.valhalla.domain.user.UserService;
import com.valhalla.presentation.shared.LoginRedirects;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebIntegrationTest
public class LoginControllerTest {

  private static final String LOGIN_EMAIL = "login@unlam.edu.ar";
  private static final String LOGIN_PASSWORD = "secure-password";
  private static final String ADMIN_EMAIL = "login.admin@unlam.edu.ar";
  private static final String ADMIN_PASSWORD = "admin-password";

  private static final String ATTR_NEW_USER_DATA = "newUserData";
  private static final String BINDING_RESULT_NEW_USER =
    "org.springframework.validation.BindingResult." + ATTR_NEW_USER_DATA;

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private LoginService loginService;

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SessionRegistry sessionRegistry;

  @Autowired
  private UserDetailsService userDetailsService;

  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
    if (userRepository.findByEmail(LOGIN_EMAIL).isEmpty()) {
      loginService.register(LOGIN_EMAIL, LOGIN_PASSWORD, "Login", "Test");
    }
    if (userRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {
      userService.create(ADMIN_EMAIL, ADMIN_PASSWORD, "ADMIN", "Admin", "Test");
    }
  }

  @Test
  public void shouldShowLandingPage() throws Exception {
    this.mockMvc.perform(get("/"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/landing"));
  }

  @Test
  public void shouldReturnLoginPage() throws Exception {
    this.mockMvc.perform(get("/admin/login"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/auth/login"));
  }

  @Test
  public void shouldShowLoginLinkInNavbarWhenAnonymous() throws Exception {
    this.mockMvc.perform(get("/admin/login"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("href=\"/auth/login\"")));
  }

  /**
   * La regresión del segundo bug reportado: con sesión activa, /admin/login renderizaba el
   * formulario de login encima de la sesión. SecurityConfig la deja en permitAll porque tiene que
   * abrirse sin sesión, así que el corte va en el controller.
   */
  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldRedirectAwayFromTheAdminLoginPageWhenAlreadySignedIn() throws Exception {
    this.mockMvc.perform(get("/admin/login"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/home"));
  }

  /** Un usuario común no aterriza en /admin/home: ahí no tiene nada que ver. */
  @Test
  @WithMockUser(username = "user@unlam.edu.ar", roles = { "USER" })
  public void shouldRedirectACommonUserToTheLandingWhenAlreadySignedIn() throws Exception {
    this.mockMvc.perform(get("/admin/login"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(LoginRedirects.USER_LANDING));
  }

  /**
   * El logout manda a /admin/login?logout=true para que la vista confirme que la sesión terminó. La
   * sesión ya está invalidada ahí, así que tiene que renderizar igual que anónimo: si el corte
   * confundiera "recién deslogueado" con "con sesión", el usuario caería en un redirect sin
   * explicación y el aviso de logout no se vería nunca.
   */
  @Test
  public void shouldStillRenderTheLoginPageAfterLogout() throws Exception {
    this.mockMvc.perform(get("/admin/login").param("logout", "true"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/auth/login"));
  }

  /** Un login fallido manda ?error=true sin sesión: el formulario tiene que seguir apareciendo. */
  @Test
  public void shouldStillRenderTheLoginPageAfterAFailedAttempt() throws Exception {
    this.mockMvc.perform(get("/admin/login").param("error", "true"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/auth/login"));
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldReturnNewUserPage() throws Exception {
    this.mockMvc.perform(get("/admin/new-user"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/auth/new-user"))
      .andExpect(model().attributeExists(ATTR_NEW_USER_DATA));
  }

  @Test
  public void shouldRedirectToAdminHomeWhenAdminCredentialsAreCorrect() throws Exception {
    this.mockMvc.perform(
        post("/admin/validate-login")
          .with(csrf())
          .param("email", ADMIN_EMAIL)
          .param("password", ADMIN_PASSWORD)
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/home"));
  }

  @Test
  public void shouldReRenderLoginWithErrorWhenPasswordIsWrong() throws Exception {
    this.mockMvc.perform(
        post("/admin/validate-login")
          .with(csrf())
          .param("email", LOGIN_EMAIL)
          .param("password", "wrong-password")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/login?error=true"));
  }

  @Test
  public void shouldRedirectToLoginWhenEmailDoesNotExist() throws Exception {
    this.mockMvc.perform(
        post("/admin/validate-login")
          .with(csrf())
          .param("email", "nobody@unlam.edu.ar")
          .param("password", LOGIN_PASSWORD)
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/login?error=true"));
  }

  @Test
  public void shouldRedirectToLoginFromHomeWhenNotAuthenticated() throws Exception {
    this.mockMvc.perform(get("/admin/home")).andExpect(status().is3xxRedirection());
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldRedirectToLoginWhenRegistering() throws Exception {
    this.mockMvc.perform(
        post("/admin/register")
          .with(csrf())
          .param("firstName", "New")
          .param("lastName", "User")
          .param("email", "new@unlam.edu.ar")
          .param("password", "new-password")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/login"));
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldReRenderNewUserWithErrorWhenEmailAlreadyExists() throws Exception {
    String duplicateEmail = "duplicate@unlam.edu.ar";
    this.mockMvc.perform(
        post("/admin/register")
          .with(csrf())
          .param("firstName", "Dup")
          .param("lastName", "User")
          .param("email", duplicateEmail)
          .param("password", LOGIN_PASSWORD)
      )
      .andExpect(status().is3xxRedirection());

    this.mockMvc.perform(
        post("/admin/register")
          .with(csrf())
          .param("firstName", "Dup")
          .param("lastName", "User")
          .param("email", duplicateEmail)
          .param("password", "another-password")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("pages/auth/new-user"))
      .andExpect(model().attribute("error", "Email is already registered"));
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldReRenderNewUserWithValidationErrors() throws Exception {
    this.mockMvc.perform(
        post("/admin/register")
          .with(csrf())
          .param("firstName", "")
          .param("lastName", "")
          .param("email", "not-an-email")
          .param("password", "123")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("pages/auth/new-user"))
      .andExpect(model().attribute("error", "Invalid registration data"))
      .andExpect(model().attributeExists(BINDING_RESULT_NEW_USER));
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldShowHomeWhenAuthenticated() throws Exception {
    this.mockMvc.perform(get("/admin/home"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/home"));
  }

  @Test
  public void shouldRejectPostWithoutCsrf() throws Exception {
    this.mockMvc.perform(
        post("/admin/validate-login").param("email", LOGIN_EMAIL).param("password", LOGIN_PASSWORD)
      )
      .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldLogoutRedirectToLoginAndInvalidateSession() throws Exception {
    MockHttpSession session = new MockHttpSession();
    this.mockMvc.perform(post("/admin/logout").session(session).with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/login?logout=true"));
    assertThat("session must be invalidated after logout", session.isInvalid(), is(true));
  }

  @Test
  public void shouldRequireCsrfForApiWrites() throws Exception {
    this.mockMvc.perform(post("/api/something")).andExpect(status().isForbidden());
  }

  @Test
  public void shouldKeepOnlyTheMostRecentSessionPerUser() throws Exception {
    UserDetails principal = userDetailsService.loadUserByUsername(ADMIN_EMAIL);

    MockHttpSession firstSession = new MockHttpSession();
    performSuccessfulLogin(firstSession);
    assertThat(
      "a single login yields one active session",
      sessionRegistry.getAllSessions(principal, false),
      hasSize(1)
    );

    MockHttpSession secondSession = new MockHttpSession();
    performSuccessfulLogin(secondSession);

    List<SessionInformation> activeSessions = sessionRegistry.getAllSessions(principal, false);
    assertThat(
      "only one session stays active after a second login (AC-13)",
      activeSessions,
      hasSize(1)
    );
    assertThat(
      "the most recent session prevails",
      activeSessions.get(0).getSessionId(),
      is(secondSession.getId())
    );

    SessionInformation firstSessionInfo = sessionRegistry
      .getAllSessions(principal, true)
      .stream()
      .filter(s -> s.getSessionId().equals(firstSession.getId()))
      .findFirst()
      .orElseThrow();
    assertThat("the displaced session must be expired", firstSessionInfo.isExpired(), is(true));
  }

  private void performSuccessfulLogin(MockHttpSession session) throws Exception {
    this.mockMvc.perform(
        post("/admin/validate-login")
          .session(session)
          .with(csrf())
          .param("email", ADMIN_EMAIL)
          .param("password", ADMIN_PASSWORD)
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/home"));
  }
}
