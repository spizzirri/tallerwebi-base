package com.valhalla.integration;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.valhalla.presentation.shared.LoginRedirects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebIntegrationTest
public class SecurityConfigTest {

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
  }

  @Test
  public void shouldAllowPlacesWithoutSession() throws Exception {
    this.mockMvc.perform(get("/explore"))
      .andExpect(status().isOk())
      .andExpect(model().attributeDoesNotExist("userPlans"));
  }

  @Test
  public void shouldRedirectToLoginWhenAccessingPlansWithoutSession() throws Exception {
    this.mockMvc.perform(get("/plans"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrlPattern("**/auth/login"));
  }

  // --- S-03: a permitted path that no controller maps answers 404, not 403 and not a 200 page ---

  @Test
  public void shouldReturnNotFoundForUnmappedPublicPath() throws Exception {
    this.mockMvc.perform(get("/share/abc123"))
      .andExpect(status().isNotFound())
      // Proves the 404 came from GlobalExceptionHandler and not from the servlet container's own
      // error page, which would answer 404 with an empty body.
      .andExpect(model().attribute("error", "Page not found"));
  }

  @Test
  public void shouldAllowPublicAccessToLandingPage() throws Exception {
    this.mockMvc.perform(get("/")).andExpect(status().isOk());
  }

  @Test
  public void shouldAllowPublicAccessToReloadEndpoint() throws Exception {
    this.mockMvc.perform(get("/reload/version")).andExpect(status().isOk());
  }

  // --- Las dos superficies son disjuntas: /admin/** es de ADMIN, /auth/** es de USER ---
  //
  // Estas reemplazan a dos tests que afirmaban lo contrario: shouldAllowUserRoleAccessToHome daba
  // 200 a un USER en /admin/home, y shouldDenyUserRoleAccessToAdminUsers esperaba un 403 que ahora
  // es un redirect al home propio.

  /** El agujero: /admin/home estaba en authenticated(), así que un USER común entraba al home del admin. */
  @Test
  @WithMockUser(roles = "USER")
  public void shouldRedirectACommonUserAwayFromTheAdminHome() throws Exception {
    this.mockMvc.perform(get("/admin/home"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(LoginRedirects.USER_LANDING));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  public void shouldAllowAdminRoleAccessToTheAdminHome() throws Exception {
    this.mockMvc.perform(get("/admin/home")).andExpect(status().isOk());
  }

  /** Un USER que cruza a /admin/** no ve un 403 genérico: vuelve a su propio home. */
  @Test
  @WithMockUser(roles = "USER")
  public void shouldRedirectACommonUserAwayFromAdminUsers() throws Exception {
    this.mockMvc.perform(get("/admin/users"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(LoginRedirects.USER_LANDING));
  }

  /** Y al revés: un ADMIN que cruza a /auth/** vuelve a /admin/home, no a la landing. */
  @Test
  @WithMockUser(roles = "ADMIN")
  public void shouldRedirectAnAdminAwayFromTheUserAuthForms() throws Exception {
    this.mockMvc.perform(get("/auth/register"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/home"));

    this.mockMvc.perform(get("/auth/forgot-password"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/home"));
  }

  /** Un USER sí usa toda la superficie de /auth/**, incluido el logout propio. */
  @Test
  @WithMockUser(roles = "USER")
  public void shouldAllowUserRoleAccessToTheAuthForms() throws Exception {
    this.mockMvc.perform(get("/auth/register")).andExpect(status().isOk());
    this.mockMvc.perform(get("/auth/forgot-password")).andExpect(status().isOk());
  }

  // --- S-01: anónimo puede llegar a los formularios, si no nadie podría registrarse ---
  //
  // "Público" es "abierto a quien no sea admin", no "para cualquiera": un anónimo tiene que poder
  // registrarse sin sesión previa, y un ADMIN ya está donde tiene que estar.

  @Test
  public void shouldAllowPublicAccessToTheAuthForms() throws Exception {
    this.mockMvc.perform(get("/auth/login")).andExpect(status().isOk());
    this.mockMvc.perform(get("/auth/register")).andExpect(status().isOk());
    this.mockMvc.perform(get("/auth/forgot-password")).andExpect(status().isOk());
  }

  // --- S-05: a register post without a token is refused ---
  //
  // Sigue siendo 403 y no un redirect: CsrfFilter corre antes que los filtros de autorización, así
  // que un POST sin token va directo al AccessDeniedHandler sin pasar por ExceptionTranslationFilter.
  // Si ese handler redirigiera, el login sin token contestaría 302 y el AC-06 de 08-AUT mudaría de
  // signo sin que nadie lo decidiera.

  @Test
  public void shouldRejectRegisterPostWithoutCsrf() throws Exception {
    this.mockMvc.perform(
        post("/auth/register").param("email", "s01@unlam.edu.ar").param("password", "secret123")
      )
      .andExpect(status().isForbidden());
  }

  /** Lo mismo en la cadena del admin: el handler es compartido, y el 403 también. */
  @Test
  public void shouldRejectAdminLoginPostWithoutCsrf() throws Exception {
    this.mockMvc.perform(
        post("/admin/validate-login").param("email", "admin@unlam.edu.ar").param("password", "x")
      )
      .andExpect(status().isForbidden());
  }
}
