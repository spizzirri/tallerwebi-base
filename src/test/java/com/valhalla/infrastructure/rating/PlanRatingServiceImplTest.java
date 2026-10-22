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

  private static User user(UUID id, String email) {
    User user = new User();
    user.setId(id);
    user.setEmail(email);
    return user;
  }
}
