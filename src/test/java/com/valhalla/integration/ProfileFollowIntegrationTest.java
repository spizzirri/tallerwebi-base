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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.valhalla.domain.follow.FollowService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * [FOLLOW] Seguir y dejar de seguir desde el perfil publico, contra la cadena real de seguridad y
 * HSQLDB: escenarios I-01..I-06 de docs/specs/19-FOLLOW.md.
 *
 * <p>El seguidor sale siempre de la sesion ({@code .with(user(email))}), nunca del path: el path
 * solo nombra al seguido. Los usuarios se crean aca mismo porque el controller los cruza por email
 * con {@code UserService} y cada contexto de {@code JpaTestConfig} arranca con la base vacia.
 */
@WebIntegrationTest
@Transactional
public class ProfileFollowIntegrationTest {

  private static final String FOLLOWER_EMAIL = "follow.follower@test.com";
  private static final String FOLLOWED_EMAIL = "follow.followed@test.com";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private FollowService followService;

  private MockMvc mockMvc;

  private UUID followerId;
  private UUID followedId;

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
    this.followerId = givenUser(FOLLOWER_EMAIL, "Ada", "Lovelace");
    this.followedId = givenUser(FOLLOWED_EMAIL, "Grace", "Hopper");
  }

  // --- I-01 ---

  @Test
  public void I01_followConSesionCreaLaRelacionYRedirigeAlPerfil() throws Exception {
    // given: dos usuarios sin relacion previa

    // when
    this.mockMvc.perform(
        post("/profile/" + this.followedId + "/follow").with(user(FOLLOWER_EMAIL)).with(csrf())
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/profile/" + this.followedId));

    // then: una sola arista, con el seguidor de la sesion
    assertThat(this.followService.isFollowing(this.followerId, this.followedId), is(true));
    assertThat(this.followService.countFollowers(this.followedId), is(1L));
    assertThat(this.followService.countFollowing(this.followerId), is(1L));
  }

  // --- I-02 ---

  @Test
  public void I02_followSinTokenCsrfResponde403YNoEscribeNada() throws Exception {
    // when: el mismo POST, sin el token que el form del perfil siempre incluye
    this.mockMvc.perform(post("/profile/" + this.followedId + "/follow").with(user(FOLLOWER_EMAIL)))
      .andExpect(status().isForbidden());

    // then: el rechazo ocurre antes del servicio
    assertThat(this.followService.isFollowing(this.followerId, this.followedId), is(false));
    assertThat(this.followService.countFollowers(this.followedId), is(0L));
  }

  // --- I-03 ---

  @Test
  public void I03_unfollowRevierteLaRelacionYBajaElContador() throws Exception {
    // given: el seguidor ya sigue al autor por el endpoint real
    this.mockMvc.perform(
        post("/profile/" + this.followedId + "/follow").with(user(FOLLOWER_EMAIL)).with(csrf())
      )
      .andExpect(status().is3xxRedirection());
    assertThat(this.followService.countFollowers(this.followedId), is(1L));

    // when
    this.mockMvc.perform(
        post("/profile/" + this.followedId + "/unfollow").with(user(FOLLOWER_EMAIL)).with(csrf())
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/profile/" + this.followedId));

    // then
    assertThat(this.followService.isFollowing(this.followerId, this.followedId), is(false));
    assertThat(this.followService.countFollowers(this.followedId), is(0L));
    assertThat(this.followService.countFollowing(this.followerId), is(0L));
  }

  // --- I-04 ---

  @Test
  public void I04_followSinSesionRedirigeAlLogin() throws Exception {
    // El token viaja para que el corte lo haga la cadena de seguridad y no el filtro de CSRF.
    this.mockMvc.perform(post("/profile/" + this.followedId + "/follow").with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrlPattern("**/auth/login*"));

    assertThat(this.followService.countFollowers(this.followedId), is(0L));
  }

  // --- I-05 ---

  @Test
  public void I05_followAlPropioIdNoCreaRelacionNiResponde500() throws Exception {
    // when: el perfil propio no ofrece el boton, pero el POST directo existe igual
    this.mockMvc.perform(
        post("/profile/" + this.followerId + "/follow").with(user(FOLLOWER_EMAIL)).with(csrf())
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/profile/" + this.followerId));

    // then: no-op silencioso, sin arista y sin 500
    assertThat(this.followService.isFollowing(this.followerId, this.followerId), is(false));
    assertThat(this.followService.countFollowing(this.followerId), is(0L));
  }

  // --- I-06 ---

  @Test
  public void I06_elPerfilMuestraAlSeguidorEnFollowYElContadorActualizado() throws Exception {
    // given: el seguidor sigue al autor
    this.mockMvc.perform(
        post("/profile/" + this.followedId + "/follow").with(user(FOLLOWER_EMAIL)).with(csrf())
      )
      .andExpect(status().is3xxRedirection());

    // when: el seguidor abre el perfil del autor
    String html =
      this.mockMvc.perform(get("/profile/" + this.followedId).with(user(FOLLOWER_EMAIL)))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/profile/view"))
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then: la seccion #follow lista al nuevo seguidor con link a su perfil y el contador real
    String followSection = followSectionOf(html);
    assertThat(followSection, containsString("data-testid=\"followers-list\""));
    assertThat(followSection, containsString("href=\"/profile/" + this.followerId + "\""));
    assertThat(followSection, containsString("Ada Lovelace"));
    assertThat(followSection, containsString(">1</span> seguidores"));
    assertThat(followSection, not(containsString("Todavía no tiene seguidores")));
    // El boton del autor ya ofrece la accion inversa (AC-01)
    assertThat(html, containsString("id=\"btn-follow\""));
    assertThat(html, containsString("Dejar de seguir"));
  }

  /** [FOLLOW] La seccion {@code #follow} del perfil: aislarla evita leer el nombre del navbar. */
  private static String followSectionOf(String html) {
    int start = html.indexOf("id=\"follow\"");
    assertThat("la vista del perfil tiene la seccion #follow", start >= 0, is(true));
    return html.substring(start);
  }

  private UUID givenUser(String email, String firstName, String lastName) {
    User user = new User();
    user.setEmail(email);
    user.setPassword("password123");
    user.setRole("USER");
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.activate();
    this.userRepository.save(user);
    return this.userRepository.findByEmail(email).orElseThrow().getId();
  }
}
