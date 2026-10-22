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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.valhalla.domain.follow.FollowService;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * [FOLLOW] Reglas de seguridad de seguir/dejar de seguir y del scope Siguiendo: escenarios
 * S-01..S-03 de docs/specs/19-FOLLOW.md.
 *
 * <p>El eje es la identidad: la sesion es la unica fuente del seguidor (AC-13). Los negativos
 * afirman el efecto sobre la tabla, no solo el status, para que un handler que trague el rechazo
 * tambien falle aca.
 */
@WebIntegrationTest
@Transactional
public class FollowSecurityTest {

  private static final String VIEWER_EMAIL = "sec.follow.viewer@test.com";
  private static final String OTHER_EMAIL = "sec.follow.other@test.com";
  private static final String AUTHOR_EMAIL = "sec.follow.author@test.com";
  private static final String PUBLIC_PLAN_NAME = "Plan publico de la autora";
  private static final String PRIVATE_PLAN_NAME = "Plan privado de la autora";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PlanRepository planRepository;

  @Autowired
  private FollowService followService;

  private MockMvc mockMvc;

  private UUID viewerId;
  private UUID otherId;
  private UUID authorId;

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
    this.viewerId = givenUser(VIEWER_EMAIL, "Ana", "Seguidora");
    this.otherId = givenUser(OTHER_EMAIL, "Bruno", "Tercero");
    this.authorId = givenUser(AUTHOR_EMAIL, "Grace", "Autora");
  }

  // --- S-01 ---

  @Test
  public void S01_postAnonimoTerminaEnElLoginYNoCreaRelacion() throws Exception {
    // El token viaja para que el corte lo haga la cadena de seguridad, no el filtro de CSRF.
    this.mockMvc.perform(post("/profile/" + this.authorId + "/follow").with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrlPattern("**/auth/login*"));

    // then (AC-10, AC-13): no existe follow anonimo
    assertThat(this.followService.countFollowers(this.authorId), is(0L));
    assertThat(this.followService.followers(this.authorId).isEmpty(), is(true));
  }

  // --- S-02 ---

  @Test
  public void S02_laSesionMandaYUnFollowerIdInventadoNoCambiaAlSeguidor() throws Exception {
    // when: el form real no tiene parametro de usuario, pero el atacante lo agrega igual
    this.mockMvc.perform(
        post("/profile/" + this.authorId + "/follow")
          .with(user(VIEWER_EMAIL))
          .with(csrf())
          .param("followerId", String.valueOf(this.otherId))
      )
      .andExpect(status().is3xxRedirection());

    // then (AC-13): la arista es del usuario de la sesion, y solo de el
    assertThat(this.followService.isFollowing(this.viewerId, this.authorId), is(true));
    assertThat(this.followService.isFollowing(this.otherId, this.authorId), is(false));
    assertThat(
      this.followService.followers(this.authorId).stream().map(User::getId).toList(),
      is(List.of(this.viewerId))
    );
  }

  // --- S-03 ---

  @Test
  public void S03_elScopeSiguiendoNoFiltraPlanesPrivadosNiEmailsDeLosAutores() throws Exception {
    // given: el viewer sigue a la autora, que tiene un plan publico y uno privado
    this.followService.follow(this.viewerId, this.authorId);
    givenPlan(this.authorId, PUBLIC_PLAN_NAME, true);
    givenPlan(this.authorId, PRIVATE_PLAN_NAME, false);

    // when
    String html =
      this.mockMvc.perform(get("/for-you?scope=following").with(user(VIEWER_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then (AC-11, AC-12): la card muestra el nombre del autor, nunca su email ni lo privado
    assertThat(html, containsString(PUBLIC_PLAN_NAME));
    assertThat(html, containsString("Grace Autora"));
    assertThat(html, containsString(">Grace Autora</a>"));
    assertThat(html, not(containsString(PRIVATE_PLAN_NAME)));
    assertThat(html, not(containsString(AUTHOR_EMAIL)));
    // El email del propio visitante si sale en el aria-label del avatar del navbar ([PROF],
    // preexistente): el eje de esta asercion son las identidades de los autores del feed.
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

  private void givenPlan(UUID administratorId, String name, boolean isPublic) {
    Plan plan = new Plan();
    plan.setName(name);
    plan.setIsPublic(isPublic);
    plan.setAdministrator(this.userRepository.findById(administratorId).orElseThrow());
    this.planRepository.save(plan);
  }
}
