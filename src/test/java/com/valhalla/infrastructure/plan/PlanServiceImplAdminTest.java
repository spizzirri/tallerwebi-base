package com.valhalla.infrastructure.plan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.notification.NotificationService;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanPermission;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.rating.PlanRatingRepository;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * [PABM] Unit tests of the admin operations of {@link PlanServiceImpl} (U-11 a U-20 de
 * 25-PABM.md) against mocked ports. Cada negativo prueba el efecto: un {@code never()} sobre la
 * escritura destructiva, o la entidad sin tocar.
 */
public class PlanServiceImplAdminTest {

  private static final String APP_EMAIL = "app@planit.com";
  private static final String PLANIT_NAME = "PlanIt";
  private static final UUID PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000003a");
  private static final UUID OWNER_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000041");
  private static final String OWNER_EMAIL = "dueno@test.com";

  private PlanServiceImpl planService;
  private PlanRepository planRepositoryMock;
  private UserRepository userRepositoryMock;
  private PlaceRepository placeRepositoryMock;
  private PlanRatingRepository planRatingRepositoryMock;
  private NotificationService notificationServiceMock;

  @BeforeEach
  public void init() {
    this.planRepositoryMock = mock(PlanRepository.class);
    this.userRepositoryMock = mock(UserRepository.class);
    this.placeRepositoryMock = mock(PlaceRepository.class);
    this.planRatingRepositoryMock = mock(PlanRatingRepository.class);
    this.notificationServiceMock = mock(NotificationService.class);
    this.planService =
      new PlanServiceImpl(
        this.planRepositoryMock,
        this.userRepositoryMock,
        this.placeRepositoryMock,
        this.planRatingRepositoryMock,
        this.notificationServiceMock
      );
  }

  // --- U-11 ---

  @Test
  public void getAllPlansReturnsEveryPlanInTheRepositoryWithoutOwnershipFilter() {
    Plan owned = ownedPlan(PLAN_ID, "Del usuario", OWNER_EMAIL);
    Plan planIt = ownedPlan(UUID.randomUUID(), "De PlanIt", APP_EMAIL);
    when(planRepositoryMock.findAll()).thenReturn(List.of(owned, planIt));

    List<Plan> plans = planService.getAllPlans();

    assertThat(plans, hasSize(2));
    assertThat(plans, contains(owned, planIt));
    verify(planRepositoryMock).findAll();
  }

  // --- U-12 ---

  @Test
  public void getAnyPlanReturnsAPlanOwnedByAnotherUserWithoutCheckingOwnership() {
    Plan plan = ownedPlan(PLAN_ID, "Ajeno", OWNER_EMAIL);
    when(planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(plan));

    Plan found = planService.getAnyPlan(PLAN_ID);

    assertThat(found, is(equalTo(plan)));
    verify(planRepositoryMock).findById(PLAN_ID);
  }

  // --- U-13 ---

  @Test
  public void getAnyPlanThrowsPlanNotFoundForAnUnknownId() {
    when(planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.empty());

    assertThrows(PlanNotFoundException.class, () -> planService.getAnyPlan(PLAN_ID));
  }

  // --- U-14 ---

  @Test
  public void createAdminPlanAssignsThePlanItSystemUserAsAdministrator() {
    User appUser = appUser();
    when(userRepositoryMock.findByEmail(APP_EMAIL)).thenReturn(Optional.of(appUser));
    when(planRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Plan created = planService.createAdminPlan(newPlan("Promo"));

    assertThat(created.getAdministrator(), is(equalTo(appUser)));
    assertThat(created.getAdministrator().getEmail(), is(equalTo(APP_EMAIL)));
    assertThat(created.getActive(), is(true));
    verify(planRepositoryMock).save(created);
  }

  // --- U-15 ---

  @Test
  public void createAdminPlanGeneratesAUniqueShortCode() {
    when(userRepositoryMock.findByEmail(APP_EMAIL)).thenReturn(Optional.of(appUser()));
    when(planRepositoryMock.existsByShortCode(any())).thenReturn(false);
    when(planRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Plan created = planService.createAdminPlan(newPlan("Promo"));

    assertThat(created.getShortCode(), matchesPattern("[A-Z0-9]{8}"));
    verify(planRepositoryMock).existsByShortCode(created.getShortCode());
  }

  // --- U-16 ---

  @Test
  public void createAdminPlanCreatesThePlanItUserWithAdminRoleWhenMissing() {
    when(userRepositoryMock.findByEmail(APP_EMAIL)).thenReturn(Optional.empty());
    when(planRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Plan created = planService.createAdminPlan(newPlan("Promo"));

    ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
    verify(userRepositoryMock).save(saved.capture());
    User appUser = saved.getValue();
    assertThat(appUser.getEmail(), is(equalTo(APP_EMAIL)));
    assertThat(appUser.getFirstName(), is(equalTo(PLANIT_NAME)));
    assertThat(appUser.getRole(), is(equalTo("ADMIN")));
    assertThat(appUser.getPassword(), is(equalTo("N/A")));
    assertThat(created.getAdministrator(), is(equalTo(appUser)));
  }

  // --- U-17 ---

  @Test
  public void updateAnyPlanCopiesTheFormFieldsAndKeepsTheStoredDayEnd() {
    Plan stored = ownedPlan(PLAN_ID, "Original", OWNER_EMAIL);
    stored.setDayEndTime(LocalTime.of(22, 0));
    when(planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(stored));
    when(planRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Plan changes = newPlan("Editado");
    changes.setDescription("Nueva descripcion");
    changes.setDayEndTime(LocalTime.of(10, 0));

    Plan updated = planService.updateAnyPlan(PLAN_ID, changes);

    assertThat(updated.getName(), is(equalTo("Editado")));
    assertThat(updated.getDescription(), is(equalTo("Nueva descripcion")));
    // Un update del admin no pisa la ventana del dia guardada (AC-21).
    assertThat(updated.getDayEndTime(), is(equalTo(LocalTime.of(22, 0))));
    verify(planRepositoryMock).save(stored);
  }

  @Test
  public void updateAnyPlanKeepsTheOwnerAndTheShortCode() {
    Plan stored = ownedPlan(PLAN_ID, "Original", OWNER_EMAIL);
    stored.setShortCode("ABCD1234");
    when(planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(stored));
    when(planRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Plan updated = planService.updateAnyPlan(PLAN_ID, newPlan("Editado"));

    assertThat(updated.getAdministrator().getEmail(), is(equalTo(OWNER_EMAIL)));
    assertThat(updated.getShortCode(), is(equalTo("ABCD1234")));
  }

  // --- U-18 ---

  @Test
  public void deleteAnyPlanRemovesRatingsFirstAndThenThePlan() {
    Plan plan = ownedPlan(PLAN_ID, "Para borrar", OWNER_EMAIL);
    when(planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(plan));

    planService.deleteAnyPlan(PLAN_ID);

    verify(planRatingRepositoryMock).deleteAllByPlanId(PLAN_ID);
    verify(planRepositoryMock).deleteById(PLAN_ID);
  }

  @Test
  public void deleteAnyPlanForAnUnknownIdDeletesNothing() {
    when(planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.empty());

    assertThrows(PlanNotFoundException.class, () -> planService.deleteAnyPlan(PLAN_ID));

    verify(planRepositoryMock, never()).deleteById(any());
    verify(planRatingRepositoryMock, never()).deleteAllByPlanId(any());
  }

  // --- U-19 ---

  @Test
  public void togglePlanActiveDeactivatesAndPersistsTheFlag() {
    Plan plan = ownedPlan(PLAN_ID, "Activo", OWNER_EMAIL);
    plan.setActive(true);
    when(planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(plan));

    planService.togglePlanActive(PLAN_ID, false);

    assertThat(plan.getActive(), is(false));
    verify(planRepositoryMock).save(plan);
  }

  @Test
  public void togglePlanActiveReactivatesAPlan() {
    Plan plan = ownedPlan(PLAN_ID, "Inactivo", OWNER_EMAIL);
    plan.setActive(false);
    when(planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(plan));

    planService.togglePlanActive(PLAN_ID, true);

    assertThat(plan.getActive(), is(true));
    verify(planRepositoryMock).save(plan);
  }

  // --- U-20 ---

  @Test
  public void togglePlanActiveWithAnUnknownPlanThrowsAndSavesNothing() {
    when(planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.empty());

    assertThrows(PlanNotFoundException.class, () -> planService.togglePlanActive(PLAN_ID, false));

    verify(planRepositoryMock, never()).save(any());
  }

  // --- AC-11 / AC-20 extras ---

  @Test
  public void updateAnySettingsWritesAllFourSettingsIncludingAutoSchedule() {
    Plan plan = ownedPlan(PLAN_ID, "Settings", OWNER_EMAIL);
    when(planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(plan));

    planService.updateAnySettings(
      PLAN_ID,
      PlanPermission.EDITORS,
      PlanPermission.PARTICIPANTS,
      PlanPermission.OWNER,
      true
    );

    assertThat(plan.getSettings().getHandleRequests(), is(equalTo(PlanPermission.EDITORS)));
    assertThat(plan.getSettings().getInviteInternal(), is(equalTo(PlanPermission.PARTICIPANTS)));
    assertThat(plan.getSettings().getInviteByEmail(), is(equalTo(PlanPermission.OWNER)));
    assertThat(plan.getSettings().getAutoSchedule(), is(true));
    verify(planRepositoryMock).save(plan);
  }

  @Test
  public void inviteParticipantAnyWithoutAdministratorThrowsPlanNotFound() {
    Plan plan = newPlan("Huerfano");
    plan.setId(PLAN_ID);
    when(planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(plan));

    assertThrows(
      PlanNotFoundException.class,
      () -> planService.inviteParticipantAny(PLAN_ID, "invitado@test.com", "guest")
    );

    verify(userRepositoryMock, never()).findByEmail(any());
  }

  // --- helpers ---

  private static Plan newPlan(String name) {
    Plan plan = new Plan();
    plan.setName(name);
    plan.setEventDate(LocalDate.of(2026, 12, 1));
    return plan;
  }

  private static Plan ownedPlan(UUID id, String name, String ownerEmail) {
    Plan plan = newPlan(name);
    plan.setId(id);
    plan.setShortCode("ZZZZ0000");
    User owner = new User();
    owner.setId(OWNER_ID);
    owner.setEmail(ownerEmail);
    owner.setFirstName("Dueño");
    owner.setLastName("Test");
    plan.setAdministrator(owner);
    return plan;
  }

  private static User appUser() {
    User appUser = new User();
    appUser.setId(UUID.fromString("0192f0a1-0000-7000-8000-000000000042"));
    appUser.setEmail(APP_EMAIL);
    appUser.setFirstName(PLANIT_NAME);
    appUser.setRole("ADMIN");
    return appUser;
  }
}
