package com.valhalla.infrastructure.rating;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.rating.PlanRating;
import com.valhalla.domain.rating.PlanRatingRepository;
import com.valhalla.domain.rating.RatedPlanView;
import com.valhalla.domain.rating.RatingSummary;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** [CAL] Unit tests of {@link PlanRatingServiceImpl} against mocked ports (16-CAL.md). */
public class PlanRatingServiceImplTest {

  private static final String EMAIL = "votante@test.com";
  private static final UUID USER_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000000b");
  private static final UUID PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-0000000000ca");

  private PlanRatingServiceImpl ratingService;
  private PlanRatingRepository planRatingRepositoryMock;
  private PlanRepository planRepositoryMock;
  private UserRepository userRepositoryMock;

  @BeforeEach
  public void init() {
    this.planRatingRepositoryMock = mock(PlanRatingRepository.class);
    this.planRepositoryMock = mock(PlanRepository.class);
    this.userRepositoryMock = mock(UserRepository.class);
    this.ratingService =
      new PlanRatingServiceImpl(
        this.planRatingRepositoryMock,
        this.planRepositoryMock,
        this.userRepositoryMock
      );
  }

  // --- rate: camino feliz ---

  @Test
  public void T_CAL_001_rate_creaLaCalificacionDeUnPlanPublico() {
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(publicPlan()));
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));
    when(this.planRatingRepositoryMock.findByPlanIdAndUserEmail(PLAN_ID, EMAIL))
      .thenReturn(Optional.empty());

    this.ratingService.rate(PLAN_ID, EMAIL, 4);

    verify(this.planRatingRepositoryMock).save(any(PlanRating.class));
  }

  // --- rate: upsert ---

  @Test
  public void T_CAL_002_rate_reemplazaLaCalificacionExistenteDelUsuario() {
    Plan plan = publicPlan();
    User user = user(USER_ID, EMAIL);
    PlanRating existing = new PlanRating(plan, user, 4);
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(plan));
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    when(this.planRatingRepositoryMock.findByPlanIdAndUserEmail(PLAN_ID, EMAIL))
      .thenReturn(Optional.of(existing));
    when(this.planRatingRepositoryMock.save(any(PlanRating.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    PlanRating updated = this.ratingService.rate(PLAN_ID, EMAIL, 5);

    assertThat(updated.getScore(), is(5));
    // Reemplazo: un save sobre la misma fila, no hay creacion de una segunda (AC-04).
    verify(this.planRatingRepositoryMock, times(1)).save(existing);
  }

  public void T_CAL_002b_rate_nuncaGrabaDosFilasDelMismoUsuario() {
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(publicPlan()));
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));
    when(this.planRatingRepositoryMock.findByPlanIdAndUserEmail(PLAN_ID, EMAIL))
      .thenReturn(Optional.of(new PlanRating(publicPlan(), user(USER_ID, EMAIL), 1)));

    this.ratingService.rate(PLAN_ID, EMAIL, 2);
    this.ratingService.rate(PLAN_ID, EMAIL, 3);

    verify(this.planRatingRepositoryMock, times(2)).save(any(PlanRating.class));
  }

  // --- rate: validacion de rango ---

  @Test
  public void T_CAL_003_rate_rechazaScoresFueraDeRango() {
    assertThrows(IllegalArgumentException.class, () -> this.ratingService.rate(PLAN_ID, EMAIL, 0));
    assertThrows(IllegalArgumentException.class, () -> this.ratingService.rate(PLAN_ID, EMAIL, 6));
    verify(this.planRatingRepositoryMock, never()).save(any(PlanRating.class));
  }

  // --- rate: solo planes publicos ---

  @Test
  public void T_CAL_004_rate_rechazaUnPlanPrivadoComoInexistente() {
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(privatePlan()));

    assertThrows(PlanNotFoundException.class, () -> this.ratingService.rate(PLAN_ID, EMAIL, 3));
    verify(this.planRatingRepositoryMock, never()).save(any(PlanRating.class));
  }

  @Test
  public void T_CAL_005_rate_rechazaUnIdInexistente() {
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.empty());

    assertThrows(PlanNotFoundException.class, () -> this.ratingService.rate(PLAN_ID, EMAIL, 3));
    verify(this.planRatingRepositoryMock, never()).save(any(PlanRating.class));
  }

  // --- rate: el administrador puede calificar su propio plan ---

  @Test
  public void T_CAL_006_rate_elAdministradorPuedeCalificarSuPropioPlan() {
    Plan plan = publicPlan();
    plan.setAdministrator(user(USER_ID, EMAIL));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(plan));
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL)));
    when(this.planRatingRepositoryMock.findByPlanIdAndUserEmail(PLAN_ID, EMAIL))
      .thenReturn(Optional.empty());

    this.ratingService.rate(PLAN_ID, EMAIL, 5);

    verify(this.planRatingRepositoryMock).save(any(PlanRating.class));
  }

  // --- getSummary ---

  @Test
  public void T_CAL_007_getSummary_devuelvePromedioYCantidad() {
    when(this.planRatingRepositoryMock.findSummaries())
      .thenReturn(List.of(new RatingSummary(PLAN_ID, 4.5, 12L)));

    RatingSummary summary = this.ratingService.getSummary(PLAN_ID);

    assertThat(summary.average(), is(4.5));
    assertThat(summary.count(), is(12L));
  }

  @Test
  public void T_CAL_008_getSummary_sinCalificaciones_daCountCeroYPromedioNull() {
    when(this.planRatingRepositoryMock.findSummaries()).thenReturn(List.of());

    RatingSummary summary = this.ratingService.getSummary(PLAN_ID);

    assertThat(summary.average(), is(nullValue()));
    assertThat(summary.count(), is(0L));
  }

  // --- getMyRating ---

  @Test
  public void T_CAL_009a_getMyRating_devuelveElPuntajEPropio() {
    when(this.planRatingRepositoryMock.findByPlanIdAndUserEmail(PLAN_ID, EMAIL))
      .thenReturn(Optional.of(new PlanRating(publicPlan(), user(USER_ID, EMAIL), 4)));

    assertThat(this.ratingService.getMyRating(PLAN_ID, EMAIL), is(4));
  }

  @Test
  public void T_CAL_009b_getMyRating_sinCalificacion_devuelveNull() {
    when(this.planRatingRepositoryMock.findByPlanIdAndUserEmail(PLAN_ID, EMAIL))
      .thenReturn(Optional.empty());

    assertThat(this.ratingService.getMyRating(PLAN_ID, EMAIL), is(nullValue()));
  }

  // --- [MYRAT] getRatingsByUser: filas del perfil ---

  @Test
  public void T_MYRAT_001_getRatingsByUser_devuelveSoloLasDelUsuarioYEnOrdenEstable() {
    Plan first = plan(PLAN_ID, "Primero", true);
    Plan second = plan(UUID.fromString("0192f0a1-0000-7000-8000-0000000000cb"), "Segundo", true);
    when(this.planRatingRepositoryMock.findByUserEmail(EMAIL))
      .thenReturn(
        List.of(
          new PlanRating(first, user(USER_ID, EMAIL), 5),
          new PlanRating(second, user(USER_ID, EMAIL), 2)
        )
      );

    List<RatedPlanView> ratings = this.ratingService.getRatingsByUser(EMAIL);

    assertThat(ratings.size(), is(2));
    assertThat(ratings.get(0).planName(), is("Primero"));
    assertThat(ratings.get(0).score(), is(5));
    assertThat(ratings.get(1).planName(), is("Segundo"));
    assertThat(ratings.get(1).score(), is(2));
    verify(this.planRatingRepositoryMock, times(1)).findByUserEmail(EMAIL);
  }

  @Test
  public void T_MYRAT_002_getRatingsByUser_marcaNoAccesibleUnPlanPrivadoAjeno() {
    Plan plan = plan(PLAN_ID, "Privado ajeno", false);
    when(this.planRatingRepositoryMock.findByUserEmail(EMAIL))
      .thenReturn(List.of(new PlanRating(plan, user(USER_ID, EMAIL), 3)));

    RatedPlanView rating = this.ratingService.getRatingsByUser(EMAIL).get(0);

    assertThat(rating.accessible(), is(false));
    assertThat(rating.member(), is(false));
  }

  @Test
  public void T_MYRAT_003_getRatingsByUser_unParticipanteConservaElAccesoAlPlanPrivado() {
    Plan plan = plan(PLAN_ID, "Privado propio", false);
    plan.getParticipants().add(user(USER_ID, EMAIL));
    when(this.planRatingRepositoryMock.findByUserEmail(EMAIL))
      .thenReturn(List.of(new PlanRating(plan, user(USER_ID, EMAIL), 3)));

    RatedPlanView rating = this.ratingService.getRatingsByUser(EMAIL).get(0);

    assertThat(rating.accessible(), is(true));
    assertThat(rating.member(), is(true));
  }

  @Test
  public void T_MYRAT_004_getRatingsByUser_elBanYElPlanDesactivadoQuitanElAcceso() {
    Plan banned = plan(PLAN_ID, "Publico con ban", true);
    banned.getBannedUsers().add(user(USER_ID, EMAIL));
    Plan inactive = plan(UUID.fromString("0192f0a1-0000-7000-8000-0000000000cc"), "Inactivo", true);
    inactive.setActive(false);
    when(this.planRatingRepositoryMock.findByUserEmail(EMAIL))
      .thenReturn(
        List.of(
          new PlanRating(banned, user(USER_ID, EMAIL), 1),
          new PlanRating(inactive, user(USER_ID, EMAIL), 2)
        )
      );

    List<RatedPlanView> ratings = this.ratingService.getRatingsByUser(EMAIL);

    assertThat(ratings.get(0).accessible(), is(false));
    assertThat(ratings.get(1).accessible(), is(false));
  }

  @Test
  public void T_MYRAT_005_getRatingsByUser_sinEmailNoConsultaLaBase() {
    assertThat(this.ratingService.getRatingsByUser(null).isEmpty(), is(true));
    assertThat(this.ratingService.getRatingsByUser(" ").isEmpty(), is(true));
    verify(this.planRatingRepositoryMock, never()).findByUserEmail(any());
  }

  private static Plan publicPlan() {
    Plan plan = new Plan();
    plan.setName("Plan publico");
    plan.setIsPublic(true);
    return plan;
  }

  private static Plan privatePlan() {
    Plan plan = new Plan();
    plan.setName("Plan privado");
    plan.setIsPublic(false);
    return plan;
  }

  private static Plan plan(UUID id, String name, boolean isPublic) {
    Plan plan = new Plan();
    plan.setId(id);
    plan.setName(name);
    plan.setIsPublic(isPublic);
    return plan;
  }

  private static User user(UUID id, String email) {
    User user = new User();
    user.setId(id);
    user.setEmail(email);
    return user;
  }
}
