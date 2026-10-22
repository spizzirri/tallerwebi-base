package com.valhalla.presentation.profile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.bookmark.PlanBookmarkService;
import com.valhalla.domain.exception.UserNotFoundException;
import com.valhalla.domain.follow.FollowService;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 * [FOLLOW] Unit tests of {@link ProfileController}: la vista publica suma los conteos reales, las
 * dos listas y {@code isFollowing} (U-09), y el perfil propio muestra el editor con su seccion
 * {@code #follow} pero sin boton de seguir (U-10). Los POST resuelven al seguidor desde la sesion,
 * jamas desde el path (AC-13).
 */
public class ProfileControllerTest {

  private static final UUID VIEWER_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000001");
  private static final UUID OWNER_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000002");
  private static final UUID FAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000007");
  private static final UUID IDOL_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000008");
  private static final UUID MISSING_ID = UUID.fromString("0192f0a1-0000-7000-8000-0000000003e7");
  private static final String VIEWER_EMAIL = "viewer@test.com";
  private static final String OWNER_EMAIL = "owner@test.com";
  private static final String VIEW_PUBLIC = "pages/profile/view";
  private static final String VIEW_EDIT = "pages/profile/edit";
  private static final String VIEW_ERROR = "pages/error";
  private static final String ERROR_NOT_FOUND = "User not found";

  private ProfileController controller;
  private UserService userServiceMock;
  private PlanService planServiceMock;
  private PlanBookmarkService planBookmarkServiceMock;
  private FollowService followServiceMock;
  private Authentication viewer;

  @BeforeEach
  public void init() {
    this.userServiceMock = mock(UserService.class);
    this.planServiceMock = mock(PlanService.class);
    this.planBookmarkServiceMock = mock(PlanBookmarkService.class);
    this.followServiceMock = mock(FollowService.class);
    this.controller =
      new ProfileController(
        this.userServiceMock,
        this.planServiceMock,
        this.planBookmarkServiceMock,
        this.followServiceMock
      );
    when(this.planServiceMock.getPublicPlans()).thenReturn(List.of());
    this.viewer = authenticated(VIEWER_EMAIL);
    when(this.userServiceMock.getByEmail(VIEWER_EMAIL))
      .thenReturn(user(VIEWER_ID, VIEWER_EMAIL, "Ana", "Perez"));
  }

  // --- U-09: vista publica con conteos reales, isFollowing y las dos listas ---

  @Test
  public void U09_view_publicaExponeConteosRealesIsFollowingYLasDosListas() {
    // given: el dueno del perfil tiene 3 seguidores y sigue a 4, y el visitante ya lo sigue
    User owner = user(OWNER_ID, OWNER_EMAIL, "Grace", "Hopper");
    User follower = user(FAN_ID, "fan@test.com", "Fan", "Uno");
    User followed = user(IDOL_ID, "idol@test.com", "Idolo", "Dos");
    when(this.userServiceMock.findById(OWNER_ID)).thenReturn(owner);
    when(this.followServiceMock.countFollowers(OWNER_ID)).thenReturn(3L);
    when(this.followServiceMock.countFollowing(OWNER_ID)).thenReturn(4L);
    when(this.followServiceMock.followers(OWNER_ID)).thenReturn(List.of(follower));
    when(this.followServiceMock.following(OWNER_ID)).thenReturn(List.of(followed));
    when(this.followServiceMock.isFollowing(VIEWER_ID, OWNER_ID)).thenReturn(true);

    // when
    ModelAndView view = this.controller.view(OWNER_ID, this.viewer);

    // then (AC-04, AC-05): sale el 0L placeholder y entran el agregado y las listas reales
    assertThat(view.getViewName(), is(VIEW_PUBLIC));
    assertThat(view.getModel().get("followerCount"), is(equalTo(3L)));
    assertThat(view.getModel().get("followingCount"), is(equalTo(4L)));
    assertThat(view.getModel().get("isFollowing"), is(true));
    assertThat(view.getModel().get("followers"), is(equalTo(List.of(follower))));
    assertThat(view.getModel().get("following"), is(equalTo(List.of(followed))));
    // AC-12: el modelo de la vista publica no publica email ni rol como datos del perfil
    assertThat(view.getModel().containsKey("email"), is(false));
    assertThat(view.getModel().containsKey("role"), is(false));
    verify(this.followServiceMock, times(1)).isFollowing(VIEWER_ID, OWNER_ID);
  }

  // --- U-09 (anonimo): nunca sigue, y la tabla no se consulta ---

  @Test
  public void U09b_view_anonimaMuestraConteosYIsFollowingEnFalso() {
    // given: el anonimo no tiene fila en users, la relacion no aplica (AC-10)
    User owner = user(OWNER_ID, OWNER_EMAIL, "Grace", "Hopper");
    when(this.userServiceMock.findById(OWNER_ID)).thenReturn(owner);
    when(this.followServiceMock.countFollowers(OWNER_ID)).thenReturn(3L);
    when(this.followServiceMock.countFollowing(OWNER_ID)).thenReturn(0L);

    // when
    ModelAndView view = this.controller.view(OWNER_ID, anonymous());

    // then
    assertThat(view.getViewName(), is(VIEW_PUBLIC));
    assertThat(view.getModel().get("followerCount"), is(equalTo(3L)));
    assertThat(view.getModel().get("isFollowing"), is(false));
    verify(this.followServiceMock, never()).isFollowing(any(UUID.class), any(UUID.class));
  }

  // --- U-09 (sin Authentication): la cadena sin anonymous deja el principal en null ---
  @Test
  public void U09c_view_sinAuthenticationTambienQuedaEnIsFollowingFalso() {
    // given
    User owner = user(OWNER_ID, OWNER_EMAIL, "Grace", "Hopper");
    when(this.userServiceMock.findById(OWNER_ID)).thenReturn(owner);

    // when
    ModelAndView view = this.controller.view(OWNER_ID, null);

    // then: sin principal no hay relacion que consultar
    assertThat(view.getModel().get("isFollowing"), is(false));
    verify(this.followServiceMock, never()).isFollowing(any(UUID.class), any(UUID.class));
  }

  // --- U-09 (con sesion pero sin relacion): el boton dice "Seguir" ---

  @Test
  public void U09d_view_conSesionSinRelacionMuestraIsFollowingEnFalso() {
    // given: el visitante tiene sesion pero todavia no sigue al dueno
    User owner = user(OWNER_ID, OWNER_EMAIL, "Grace", "Hopper");
    when(this.userServiceMock.findById(OWNER_ID)).thenReturn(owner);
    when(this.followServiceMock.isFollowing(VIEWER_ID, OWNER_ID)).thenReturn(false);

    // when
    ModelAndView view = this.controller.view(OWNER_ID, this.viewer);

    // then (AC-01): el template muestra "Seguir", no "Dejar de seguir"
    assertThat(view.getModel().get("isFollowing"), is(false));
    verify(this.followServiceMock, times(1)).isFollowing(VIEWER_ID, OWNER_ID);
  }

  // --- U-10: el perfil propio no expone el boton y lleva las listas de #follow ---

  @Test
  public void U10_view_propiaMuestraElEditorSinBotonDeSeguirConLasListas() {
    // given: el dueno es el usuario de la sesion
    User me = user(VIEWER_ID, VIEWER_EMAIL, "Ana", "Perez");
    User follower = user(FAN_ID, "fan@test.com", "Fan", "Uno");
    User followed = user(IDOL_ID, "idol@test.com", "Idolo", "Dos");
    when(this.userServiceMock.findById(VIEWER_ID)).thenReturn(me);
    when(this.followServiceMock.countFollowers(VIEWER_ID)).thenReturn(2L);
    when(this.followServiceMock.countFollowing(VIEWER_ID)).thenReturn(1L);
    when(this.followServiceMock.followers(VIEWER_ID)).thenReturn(List.of(follower));
    when(this.followServiceMock.following(VIEWER_ID)).thenReturn(List.of(followed));

    // when
    ModelAndView view = this.controller.view(VIEWER_ID, this.viewer);

    // then (AC-03, AC-05): edit.html, sin isFollowing (no hay boton) y con las dos listas
    assertThat(view.getViewName(), is(VIEW_EDIT));
    assertThat(view.getModel().containsKey("isFollowing"), is(false));
    assertThat(view.getModel().get("followerCount"), is(equalTo(2L)));
    assertThat(view.getModel().get("followingCount"), is(equalTo(1L)));
    assertThat(view.getModel().get("followers"), is(equalTo(List.of(follower))));
    assertThat(view.getModel().get("following"), is(equalTo(List.of(followed))));
    verify(this.followServiceMock, never()).isFollowing(any(UUID.class), any(UUID.class));
  }

  // --- POST /profile/{id}/follow ---

  @Test
  public void U10b_follow_alPropioIdEsUnNoOpYRedirigeAlPerfil() {
    // when
    ModelAndView view = this.controller.follow(VIEWER_ID, this.viewer);

    // then (AC-03): ni excepcion ni relacion, el UI no ofrece el boton en el perfil propio
    assertThat(view.getViewName(), is(equalTo("redirect:/profile/" + VIEWER_ID)));
    verify(this.followServiceMock, never()).follow(any(UUID.class), any(UUID.class));
  }

  @Test
  public void U10c_follow_usaElIdDeLaSesionComoSeguidorYRedirigeAlPerfil() {
    // when
    ModelAndView view = this.controller.follow(OWNER_ID, this.viewer);

    // then (AC-01, AC-13): el path solo nombra al seguido
    assertThat(view.getViewName(), is(equalTo("redirect:/profile/" + OWNER_ID)));
    verify(this.followServiceMock, times(1)).follow(VIEWER_ID, OWNER_ID);
  }

  @Test
  public void U10d_follow_deUnPerfilInexistenteRespondeElMismo404QueLaVista() {
    // given
    doThrow(new UserNotFoundException()).when(this.followServiceMock).follow(VIEWER_ID, MISSING_ID);

    // when
    ModelAndView view = this.controller.follow(MISSING_ID, this.viewer);

    // then (AC-01): mismo 404 del perfil, no un 500
    assertThat(view.getViewName(), is(VIEW_ERROR));
    assertThat(view.getStatus(), is(HttpStatus.NOT_FOUND));
    assertThat(view.getModel().get("error"), is(equalTo(ERROR_NOT_FOUND)));
  }

  // --- POST /profile/{id}/unfollow ---

  @Test
  public void U10e_unfollow_usaElIdDeLaSesionYRedirigeAlPerfil() {
    // when
    ModelAndView view = this.controller.unfollow(OWNER_ID, this.viewer);

    // then (AC-02, AC-13)
    assertThat(view.getViewName(), is(equalTo("redirect:/profile/" + OWNER_ID)));
    verify(this.followServiceMock, times(1)).unfollow(VIEWER_ID, OWNER_ID);
  }

  private static User user(UUID id, String email, String firstName, String lastName) {
    User user = new User();
    user.setId(id);
    user.setEmail(email);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    return user;
  }

  /** Sesion real: el principal es un {@code UserDetails} cuyo nombre es el email. */
  private static Authentication authenticated(String email) {
    return new UsernamePasswordAuthenticationToken(
      new org.springframework.security.core.userdetails.User(email, "n/a", List.of()),
      null,
      AuthorityUtils.createAuthorityList("ROLE_USER")
    );
  }

  private static Authentication anonymous() {
    return new AnonymousAuthenticationToken(
      "key",
      "anonymousUser",
      AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
    );
  }
}
