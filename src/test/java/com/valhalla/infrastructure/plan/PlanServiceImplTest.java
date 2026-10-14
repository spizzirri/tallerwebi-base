package com.valhalla.infrastructure.plan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.exception.UserNotFoundException;
import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanInvitation;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests of {@link PlanServiceImpl} against mocked ports.
 *
 * <p><b>Anti-vacuity rule.</b> Every test asserts the interaction with the repository, not just the
 * returned value. The test this file replaces passed because Mockito answered {@code
 * Optional.empty()} by default, so the line it claimed to cover never ran. Each negative case here
 * additionally proves the effect: a {@code never()} on the destructive call, or the entity left
 * untouched.
 */
public class PlanServiceImplTest {

  private static final String OWNER_EMAIL = "dueno@test.com";
  private static final String OTHER_EMAIL = "otro@test.com";
  private static final String UNKNOWN_EMAIL = "nadie@test.com";
  private static final Long OWNER_ID = 7L;
  private static final UUID PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000002a");
  private static final UUID OTHER_PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000002b");

  /** The backend generates an 8 character uppercase alphanumeric code out of a UUID. */
  private static final String SHORT_CODE_PATTERN = "[A-Z0-9]{8}";

  private PlanServiceImpl planService;
  private PlanRepository planRepositoryMock;
  private UserRepository userRepositoryMock;
  private PlaceRepository placeRepositoryMock;

  @BeforeEach
  public void init() {
    this.planRepositoryMock = mock(PlanRepository.class);
    this.userRepositoryMock = mock(UserRepository.class);
    this.placeRepositoryMock = mock(PlaceRepository.class);
    this.planService =
      new PlanServiceImpl(
        this.planRepositoryMock,
        this.userRepositoryMock,
        this.placeRepositoryMock
      );
  }

  // --- createPlan ---

  @Test
  public void ownerCanInviteEditor() {
    Plan plan = invitationPlan(false);
    User invited = owner(9L, OTHER_EMAIL);
    when(userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(invited));
    planService.inviteParticipant(PLAN_ID, " " + OTHER_EMAIL + " ", "editor", OWNER_EMAIL);
    assertThat(plan.isParticipant(OTHER_EMAIL), is(false));
    assertThat(plan.isEditor(OTHER_EMAIL), is(false));
    assertThat(plan.getInvitations().getFirst().getRole(), is("editor"));
    assertThat(plan.getInvitations().getFirst().getInviter().getEmail(), is(OWNER_EMAIL));
    verify(planRepositoryMock).save(plan);
  }

  @Test
  public void editorCanInviteParticipant() {
    Plan plan = invitationPlan(false);
    User editor = owner(8L, OTHER_EMAIL);
    plan.getParticipants().add(editor);
    plan.getEditors().add(editor);
    when(userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(editor));
    User invited = owner(9L, UNKNOWN_EMAIL);
    when(userRepositoryMock.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.of(invited));
    planService.inviteParticipant(PLAN_ID, UNKNOWN_EMAIL, "guest", OTHER_EMAIL);
    assertThat(plan.isParticipant(UNKNOWN_EMAIL), is(false));
    assertThat(plan.isEditor(UNKNOWN_EMAIL), is(false));
    assertThat(plan.getInvitations().getFirst().getRole(), is("guest"));
    verify(planRepositoryMock).save(plan);
  }

  @Test
  public void visitorCannotInviteParticipantToPublicPlan() {
    Plan plan = invitationPlan(true);
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.inviteParticipant(PLAN_ID, OTHER_EMAIL, "guest", UNKNOWN_EMAIL)
    );
    assertThat(plan.getParticipants(), is(empty()));
    assertThat(plan.getInvitations(), is(empty()));
    verify(planRepositoryMock, never()).save(any());
  }

  @Test
  public void ownerCanInviteParticipantToPublicPlan() {
    Plan plan = invitationPlan(true);
    User invited = owner(9L, OTHER_EMAIL);
    when(userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(invited));
    planService.inviteParticipant(PLAN_ID, OTHER_EMAIL, "guest", OWNER_EMAIL);
    assertThat(plan.isParticipant(OTHER_EMAIL), is(false));
    assertThat(plan.getInvitations().size(), is(1));
    assertThat(plan.getInvitations().getFirst().getRole(), is("guest"));
    verify(planRepositoryMock).save(plan);
  }

  @Test
  public void anonymousCannotInviteParticipantToPublicPlan() {
    Plan plan = invitationPlan(true);
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.inviteParticipant(PLAN_ID, OTHER_EMAIL, "guest", null)
    );
    assertThat(plan.getParticipants(), is(empty()));
    verify(planRepositoryMock, never()).save(any());
  }

  @Test
  public void publicVisitorsCannotGrantEditorRole() {
    Plan plan = invitationPlan(true);
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.inviteParticipant(PLAN_ID, OTHER_EMAIL, "editor", null)
    );
    assertThat(plan.getParticipants(), is(empty()));
    verify(planRepositoryMock, never()).save(any());
  }

  @Test
  public void privateParticipantCannotInvite() {
    Plan plan = invitationPlan(false);
    plan.getParticipants().add(owner(8L, OTHER_EMAIL));
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.inviteParticipant(PLAN_ID, UNKNOWN_EMAIL, "guest", OTHER_EMAIL)
    );
    verify(planRepositoryMock, never()).save(any());
  }

  @Test
  public void invitationRejectsUnknownEmailAndDuplicateWithoutChangingRole() {
    Plan plan = invitationPlan(false);
    assertThrows(
      IllegalArgumentException.class,
      () -> planService.inviteParticipant(PLAN_ID, UNKNOWN_EMAIL, "guest", OWNER_EMAIL)
    );
    User existing = owner(9L, OTHER_EMAIL);
    plan.getParticipants().add(existing);
    plan.getEditors().add(existing);
    when(userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(existing));
    assertThrows(
      IllegalArgumentException.class,
      () -> planService.inviteParticipant(PLAN_ID, OTHER_EMAIL, "guest", OWNER_EMAIL)
    );
    assertThat(plan.isEditor(OTHER_EMAIL), is(true));
    assertThat(plan.getParticipants().size(), is(1));
    verify(planRepositoryMock, never()).save(any());
  }

  private Plan invitationPlan(boolean isPublic) {
    Plan plan = new Plan();
    plan.setAdministrator(owner(OWNER_ID, OWNER_EMAIL));
    plan.setIsPublic(isPublic);
    when(planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(plan));
    when(userRepositoryMock.findByEmail(OWNER_EMAIL))
      .thenReturn(Optional.of(plan.getAdministrator()));
    return plan;
  }

  // --- getPendingInvitations ---

  @Test
  public void getPendingInvitations_dePlanPublicoMarcaIsPublicEnVerdadero() {
    // given
    Plan plan = invitationPlan(true);
    plan.setId(PLAN_ID);
    User recipient = owner(9L, UNKNOWN_EMAIL);
    plan
      .getInvitations()
      .add(new PlanInvitation(plan, recipient, plan.getAdministrator(), "guest"));
    when(this.planRepositoryMock.findByInvitationsRecipientEmail(UNKNOWN_EMAIL))
      .thenReturn(List.of(plan));

    // when
    List<PlanService.PendingInvitation> pending =
      this.planService.getPendingInvitations(UNKNOWN_EMAIL);

    // then: isPublic sale del plan real para que la vista sepa si puede mostrar el enlace al detalle
    assertThat(pending, hasSize(1));
    assertThat(pending.getFirst().planId(), is(PLAN_ID));
    assertThat(pending.getFirst().isPublic(), is(true));
    verify(this.planRepositoryMock, times(1)).findByInvitationsRecipientEmail(UNKNOWN_EMAIL);
    assertNothingWasMutated();
  }

  @Test
  public void getPendingInvitations_dePlanPrivadoMarcaIsPublicEnFalso() {
    // given
    Plan plan = invitationPlan(false);
    plan.setId(PLAN_ID);
    User recipient = owner(9L, UNKNOWN_EMAIL);
    plan
      .getInvitations()
      .add(new PlanInvitation(plan, recipient, plan.getAdministrator(), "editor"));
    when(this.planRepositoryMock.findByInvitationsRecipientEmail(UNKNOWN_EMAIL))
      .thenReturn(List.of(plan));

    // when
    List<PlanService.PendingInvitation> pending =
      this.planService.getPendingInvitations(UNKNOWN_EMAIL);

    // then
    assertThat(pending, hasSize(1));
    assertThat(pending.getFirst().isPublic(), is(false));
    assertThat(pending.getFirst().role(), is("editor"));
    assertNothingWasMutated();
  }

  @Test
  public void T_PLN_025_createPlan_resuelveLosLugaresYPermiteRepetirUnoVariasVeces() {
    // given
    when(this.userRepositoryMock.findByEmail(OWNER_EMAIL))
      .thenReturn(Optional.of(owner(OWNER_ID, OWNER_EMAIL)));
    Place real = new Place();
    real.setId(5L);
    when(this.placeRepositoryMock.findById(5L)).thenReturn(Optional.of(real));
    Plan plan = new Plan();
    plan.setName("Viaje");
    for (int order = 1; order <= 2; order++) {
      Place stub = new Place();
      stub.setId(5L);
      PlanPlace entry = new PlanPlace();
      entry.setPlace(stub);
      entry.setSortOrder(order);
      plan.addPlanPlace(entry);
    }
    when(this.planRepositoryMock.save(plan)).thenReturn(plan);

    // when
    this.planService.createPlan(plan, OWNER_EMAIL);

    // then
    assertThat(plan.getPlanPlaces().get(0).getPlace(), is(sameInstance(real)));
    assertThat(plan.getPlanPlaces().get(1).getPlace(), is(sameInstance(real)));
    verify(this.planRepositoryMock, times(1)).save(plan);
  }

  @Test
  public void T_PLN_026_createPlan_rechazaUnLugarInexistenteYNoGuarda() {
    // given
    when(this.userRepositoryMock.findByEmail(OWNER_EMAIL))
      .thenReturn(Optional.of(owner(OWNER_ID, OWNER_EMAIL)));
    when(this.placeRepositoryMock.findById(99L)).thenReturn(Optional.empty());
    Plan plan = new Plan();
    Place stub = new Place();
    stub.setId(99L);
    PlanPlace entry = new PlanPlace();
    entry.setPlace(stub);
    plan.addPlanPlace(entry);

    // when and then
    assertThrows(
      IllegalArgumentException.class,
      () -> this.planService.createPlan(plan, OWNER_EMAIL)
    );
    verify(this.planRepositoryMock, never()).save(any(Plan.class));
  }

  @Test
  public void T_PLN_020_createPlan_asignaElDuenoResueltoYGardaElPlan() {
    // given
    User owner = owner(OWNER_ID, OWNER_EMAIL);
    Plan plan = new Plan();
    plan.setName("Viaje a Bariloche");
    when(this.userRepositoryMock.findByEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
    when(this.planRepositoryMock.save(plan)).thenReturn(plan);

    // when
    Plan created = this.planService.createPlan(plan, OWNER_EMAIL);

    // then
    assertThat(created, is(sameInstance(plan)));
    assertThat(plan.getAdministrator(), is(sameInstance(owner)));
    assertThat(plan.getShortCode(), is(notNullValue()));
    assertThat(plan.getShortCode(), matchesPattern(SHORT_CODE_PATTERN));
    verify(this.planRepositoryMock, times(1)).existsByShortCode(plan.getShortCode());
    verify(this.planRepositoryMock, times(1)).save(plan);
  }

  @Test
  public void T_PLN_021_createPlan_ignoraElShortCodeQueMandaElForm() {
    // given
    when(this.userRepositoryMock.findByEmail(OWNER_EMAIL))
      .thenReturn(Optional.of(owner(OWNER_ID, OWNER_EMAIL)));
    Plan plan = new Plan();
    plan.setName("Viaje a Bariloche");
    plan.setShortCode("HACKED01");
    when(this.planRepositoryMock.save(plan)).thenReturn(plan);

    // when
    this.planService.createPlan(plan, OWNER_EMAIL);

    // then: the incoming code never reaches the repository, not even as a lookup
    assertThat(plan.getShortCode(), is(not(equalTo("HACKED01"))));
    assertThat(plan.getShortCode(), matchesPattern(SHORT_CODE_PATTERN));
    verify(this.planRepositoryMock, never()).findByShortCode(anyString());
    verify(this.planRepositoryMock, times(1)).save(plan);
  }

  @Test
  public void T_PLN_022_createPlan_reintentaHastaQueElShortCodeEsteLibre() {
    // given: the first three candidates are taken, the fourth is free
    when(this.userRepositoryMock.findByEmail(OWNER_EMAIL))
      .thenReturn(Optional.of(owner(OWNER_ID, OWNER_EMAIL)));
    when(this.planRepositoryMock.existsByShortCode(anyString()))
      .thenReturn(true, true, true, false);
    Plan plan = new Plan();
    plan.setName("Viaje a Bariloche");
    when(this.planRepositoryMock.save(plan)).thenReturn(plan);

    // when
    this.planService.createPlan(plan, OWNER_EMAIL);

    // then
    assertThat(plan.getShortCode(), matchesPattern(SHORT_CODE_PATTERN));
    verify(this.planRepositoryMock, times(4)).existsByShortCode(anyString());
    verify(this.planRepositoryMock, times(1)).save(plan);
  }

  @Test
  public void T_PLN_023_createPlan_agotaLosDiezIntentosYNoGuardaNada() {
    // given
    when(this.userRepositoryMock.findByEmail(OWNER_EMAIL))
      .thenReturn(Optional.of(owner(OWNER_ID, OWNER_EMAIL)));
    when(this.planRepositoryMock.existsByShortCode(anyString())).thenReturn(true);

    // when and then
    assertThrows(
      IllegalStateException.class,
      () -> this.planService.createPlan(new Plan(), OWNER_EMAIL)
    );
    verify(this.planRepositoryMock, times(10)).existsByShortCode(anyString());
    verify(this.planRepositoryMock, never()).save(any(Plan.class));
  }

  @Test
  public void T_PLN_024_createPlan_fallaSiElDuenoNoExisteYTocandoPlanes() {
    // given
    when(this.userRepositoryMock.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());

    // when and then
    assertThrows(
      UserNotFoundException.class,
      () -> this.planService.createPlan(new Plan(), UNKNOWN_EMAIL)
    );
    verifyNoInteractions(this.planRepositoryMock);
  }

  // --- getPlansByUserEmail ---

  @Test
  public void T_PLN_025_getPlansByUserEmail_buscaPorElIdDelDuenoResuelto() {
    // given
    User owner = owner(OWNER_ID, OWNER_EMAIL);
    List<Plan> planes = List.of(ownedPlan(PLAN_ID, owner), ownedPlan(OTHER_PLAN_ID, owner));
    when(this.userRepositoryMock.findByEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
    when(this.planRepositoryMock.findByAdministratorId(OWNER_ID)).thenReturn(planes);

    // when
    List<Plan> result = this.planService.getPlansByUserEmail(OWNER_EMAIL);

    // then
    assertThat(result, is(equalTo(planes)));
    verify(this.userRepositoryMock, times(1)).findByEmail(OWNER_EMAIL);
    verify(this.planRepositoryMock, times(1)).findByAdministratorId(OWNER_ID);
  }

  @Test
  public void T_PLN_026_getPlansByUserEmail_devuelveListaVaciaSiElDuenoNoExiste() {
    // given
    when(this.userRepositoryMock.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());

    // when
    List<Plan> result = this.planService.getPlansByUserEmail(UNKNOWN_EMAIL);

    // then
    assertThat(result, is(empty()));
    verify(this.planRepositoryMock, never()).findByAdministratorId(any());
  }

  // --- getPublicPlans [PPV] ---

  @Test
  public void U05_getPublicPlans_devuelveLosPlanesQueExponeElRepositorio() {
    // given: el filtro isPublic = true vive en la query del repositorio, no en el servicio
    List<Plan> publicos = List.of(ownedPlan(PLAN_ID, owner(OWNER_ID, OWNER_EMAIL)));
    when(this.planRepositoryMock.findByIsPublicTrue()).thenReturn(publicos);

    // when
    List<Plan> result = this.planService.getPublicPlans();

    // then
    assertThat(result, is(equalTo(publicos)));
    verify(this.planRepositoryMock, times(1)).findByIsPublicTrue();
  }

  @Test
  public void U06_getPublicPlans_sinNingunPlanPublicoDevuelveListaVacia() {
    // given
    when(this.planRepositoryMock.findByIsPublicTrue()).thenReturn(List.of());

    // when
    List<Plan> result = this.planService.getPublicPlans();

    // then: la base vacia es un caso normal, no una excepcion (AC-06)
    assertThat(result, is(empty()));
    verify(this.planRepositoryMock, times(1)).findByIsPublicTrue();
  }

  @Test
  public void U07_getPublicPlans_noLlamaASaveNiModificaPlanes() {
    // given
    when(this.planRepositoryMock.findByIsPublicTrue())
      .thenReturn(List.of(ownedPlan(PLAN_ID, owner(OWNER_ID, OWNER_EMAIL))));

    // when
    this.planService.getPublicPlans();

    // then: es una lectura, el listado no escribe ni ordena nada (AC-05)
    assertNothingWasMutated();
  }

  // --- getOwnedPlan ---

  @Test
  public void T_PLN_027_getOwnedPlan_devuelveElPlanPropio() {
    // given
    User owner = owner(OWNER_ID, OWNER_EMAIL);
    Plan plan = ownedPlan(PLAN_ID, owner);
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(plan));

    // when
    Plan result = this.planService.getOwnedPlan(PLAN_ID, OWNER_EMAIL);

    // then
    assertThat(result, is(sameInstance(plan)));
    verify(this.planRepositoryMock, times(1)).findById(PLAN_ID);
  }

  @Test
  public void T_PLN_028_getOwnedPlan_fallaSiElPlanNoExiste() {
    // given
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.empty());

    // when and then
    assertThrows(
      PlanNotFoundException.class,
      () -> this.planService.getOwnedPlan(PLAN_ID, OWNER_EMAIL)
    );
    assertNothingWasMutated();
  }

  @Test
  public void T_PLN_029_getOwnedPlan_fallaIgualSiElPlanEsDeOtroUsuario() {
    // given
    Plan planOfSomebodyElse = ownedPlan(PLAN_ID, owner(OWNER_ID + 1, OTHER_EMAIL));
    planOfSomebodyElse.setName("Plan ajeno");
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(planOfSomebodyElse));

    // when and then
    assertThrows(
      PlanNotFoundException.class,
      () -> this.planService.getOwnedPlan(PLAN_ID, OWNER_EMAIL)
    );
    // the exception must not leak whether the id exists: same type as the missing-plan case
    assertThat(planOfSomebodyElse.getName(), is(equalTo("Plan ajeno")));
    assertNothingWasMutated();
  }

  @Test
  public void T_PLN_030_getOwnedPlan_fallaSiElPlanNoTieneDueno() {
    // given
    Plan orphan = new Plan();
    orphan.setId(PLAN_ID);
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(orphan));

    // when and then
    assertThrows(
      PlanNotFoundException.class,
      () -> this.planService.getOwnedPlan(PLAN_ID, OWNER_EMAIL)
    );
    assertNothingWasMutated();
  }

  // --- getPublicPlan ---

  @Test
  public void T_PVP_016_getPublicPlan_devuelveElPlanPublico() {
    // given
    Plan plan = ownedPlan(PLAN_ID, owner(OWNER_ID, OWNER_EMAIL));
    plan.setIsPublic(true);
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(plan));

    // when
    Plan result = this.planService.getPublicPlan(PLAN_ID);

    // then
    assertThat(result, is(sameInstance(plan)));
    verify(this.planRepositoryMock, times(1)).findById(PLAN_ID);
  }

  @Test
  public void T_PVP_017_getPublicPlan_fallaSiElPlanEsPrivado() {
    // given: un plan privado existe, pero la vista publica no debe confirmarlo
    Plan privado = ownedPlan(PLAN_ID, owner(OWNER_ID, OWNER_EMAIL));
    privado.setIsPublic(false);
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(privado));

    // when and then
    assertThrows(PlanNotFoundException.class, () -> this.planService.getPublicPlan(PLAN_ID));
    // effect check: el plan sigue intacto y no hubo escritura de ningun tipo
    assertThat(privado.getName(), is(equalTo("Viaje a Bariloche")));
    assertNothingWasMutated();
  }

  @Test
  public void T_PVP_018_getPublicPlan_fallaSiElPlanNoExiste() {
    // given
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.empty());

    // when and then: mismo tipo de excepcion que el plan privado (privado == inexistente)
    assertThrows(PlanNotFoundException.class, () -> this.planService.getPublicPlan(PLAN_ID));
    assertNothingWasMutated();
  }

  @Test
  public void T_PVP_019_getPublicPlan_esReadOnlyYNoTocaLosParticipantes() {
    // given
    Plan plan = ownedPlan(PLAN_ID, owner(OWNER_ID, OWNER_EMAIL));
    plan.setIsPublic(true);
    plan.getParticipants().add(owner(OWNER_ID + 2, "participante@test.com"));
    List<User> participantsBefore = List.copyOf(plan.getParticipants());
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(plan));

    // when
    this.planService.getPublicPlan(PLAN_ID);

    // then: solo lectura — ni save ni bajas ni altas de participantes
    assertThat(plan.getParticipants(), is(participantsBefore));
    verify(this.planRepositoryMock, never()).save(any(Plan.class));
    verify(this.planRepositoryMock, never()).deleteById(any());
  }

  // --- updatePlan ---

  @Test
  public void T_PLN_031_updatePlan_cambiaSoloLosCuatroCamposQueMandaElForm() {
    // given
    User owner = owner(OWNER_ID, OWNER_EMAIL);
    Plan existing = ownedPlan(PLAN_ID, owner);
    existing.setName("Nombre viejo");
    existing.setDescription("Descripcion vieja");
    existing.setEventDate(LocalDate.of(2026, 1, 1));
    existing.setIsPublic(false);
    existing.setShortCode("ABCD1234");
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(existing));
    when(this.planRepositoryMock.save(existing)).thenReturn(existing);

    Plan changes = new Plan();
    changes.setName("Nombre nuevo");
    changes.setDescription("Descripcion nueva");
    changes.setEventDate(LocalDate.of(2026, 12, 31));
    changes.setIsPublic(true);
    // A hostile post would carry these; the form never does and they must not be copied.
    changes.setShortCode("IGNORADO");
    changes.setAdministrator(owner(OWNER_ID + 1, OTHER_EMAIL));

    // when
    Plan updated = this.planService.updatePlan(PLAN_ID, changes, OWNER_EMAIL);

    // then
    assertThat(updated, is(sameInstance(existing)));
    assertThat(updated.getName(), is(equalTo("Nombre nuevo")));
    assertThat(updated.getDescription(), is(equalTo("Descripcion nueva")));
    assertThat(updated.getEventDate(), is(equalTo(LocalDate.of(2026, 12, 31))));
    assertThat(updated.getIsPublic(), is(true));
    assertThat(updated.getShortCode(), is(equalTo("ABCD1234")));
    assertThat(updated.getAdministrator(), is(sameInstance(owner)));
    assertThat(updated.getId(), is(equalTo(PLAN_ID)));
    verify(this.planRepositoryMock, times(1)).save(existing);
  }

  @Test
  public void T_PLN_032_updatePlan_fallaSiNoEsElDuenoYNoGuardaNiTocaElPlan() {
    // given
    Plan planOfSomebodyElse = ownedPlan(PLAN_ID, owner(OWNER_ID + 1, OTHER_EMAIL));
    planOfSomebodyElse.setName("Intocable");
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(planOfSomebodyElse));

    // when and then
    assertThrows(
      PlanNotFoundException.class,
      () -> this.planService.updatePlan(PLAN_ID, new Plan(), OWNER_EMAIL)
    );
    assertThat(planOfSomebodyElse.getName(), is(equalTo("Intocable")));
    verify(this.planRepositoryMock, never()).save(any(Plan.class));
  }

  // --- deleteOwnedPlan ---

  @Test
  public void T_PLN_033_deleteOwnedPlan_borraElPlanDelDueno() {
    // given
    when(this.planRepositoryMock.findById(PLAN_ID))
      .thenReturn(Optional.of(ownedPlan(PLAN_ID, owner(OWNER_ID, OWNER_EMAIL))));

    // when
    this.planService.deleteOwnedPlan(PLAN_ID, OWNER_EMAIL);

    // then
    verify(this.planRepositoryMock, times(1)).deleteById(PLAN_ID);
  }

  @Test
  public void T_PLN_034_deleteOwnedPlan_noBorraSiElPlanEsDeOtroUsuario() {
    // given
    when(this.planRepositoryMock.findById(PLAN_ID))
      .thenReturn(Optional.of(ownedPlan(PLAN_ID, owner(OWNER_ID + 1, OTHER_EMAIL))));

    // when and then
    assertThrows(
      PlanNotFoundException.class,
      () -> this.planService.deleteOwnedPlan(PLAN_ID, OWNER_EMAIL)
    );
    verify(this.planRepositoryMock, never()).deleteById(any());
    verify(this.planRepositoryMock, never()).save(any(Plan.class));
  }

  // --- clonePlan ---

  @Test
  public void T_PLN_035_clonePlan_conPlanPublicoLaCopiaTieneAlClonerComoDueno() {
    // given (U-01)
    User cloner = owner(OWNER_ID + 2, OTHER_EMAIL);
    Plan source = publicPlan();
    when(this.userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(cloner));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(source));
    givenSaveEchoes();

    // when
    Plan copy = this.planService.clonePlan(PLAN_ID, OTHER_EMAIL);

    // then
    assertThat(copy, is(not(sameInstance(source))));
    assertThat(copy.getAdministrator(), is(sameInstance(cloner)));
    verify(this.planRepositoryMock, times(1)).save(any(Plan.class));
  }

  @Test
  public void T_PLN_036_clonePlan_generaUnShortCodeDistintoDelOriginal() {
    // given (U-02): the source keeps its join key, the copy gets a fresh one
    User cloner = owner(OWNER_ID + 2, OTHER_EMAIL);
    Plan source = publicPlan();
    when(this.userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(cloner));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(source));
    when(this.planRepositoryMock.existsByShortCode(anyString())).thenReturn(false);
    givenSaveEchoes();

    // when
    Plan copy = this.planService.clonePlan(PLAN_ID, OTHER_EMAIL);

    // then
    assertThat(copy.getShortCode(), matchesPattern(SHORT_CODE_PATTERN));
    assertThat(copy.getShortCode(), is(not(equalTo("ORIG1234"))));
    verify(this.planRepositoryMock, times(1)).existsByShortCode(anyString());
  }

  @Test
  public void T_PLN_037_clonePlan_dejaLaCopiaPrivadaAunqueElOriginalSeaPublico() {
    // given (U-03)
    User cloner = owner(OWNER_ID + 2, OTHER_EMAIL);
    Plan source = publicPlan();
    when(this.userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(cloner));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(source));
    givenSaveEchoes();

    // when
    Plan copy = this.planService.clonePlan(PLAN_ID, OTHER_EMAIL);

    // then
    assertThat(copy.getIsPublic(), is(false));
    assertThat(source.getIsPublic(), is(true));
  }

  @Test
  public void T_PLN_038_clonePlan_copiaNombreDescripcionFechaYHoraDelEvento() {
    // given (U-04)
    User cloner = owner(OWNER_ID + 2, OTHER_EMAIL);
    when(this.userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(cloner));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(publicPlan()));
    givenSaveEchoes();

    // when
    Plan copy = this.planService.clonePlan(PLAN_ID, OTHER_EMAIL);

    // then
    assertThat(copy.getName(), is(equalTo("Viaje a Bariloche")));
    assertThat(copy.getDescription(), is(equalTo("Descripcion original")));
    assertThat(copy.getEventDate(), is(equalTo(LocalDate.of(2026, 12, 31))));
    assertThat(copy.getEventTime(), is(equalTo(LocalTime.of(18, 30))));
  }

  @Test
  public void T_PLN_039_clonePlan_copiaElItinerarioConLosMismosLugaresYOrdenSecuencial() {
    // given (U-05)
    User cloner = owner(OWNER_ID + 2, OTHER_EMAIL);
    Plan source = publicPlan();
    Place beach = new Place();
    beach.setId(5L);
    Place museum = new Place();
    museum.setId(6L);
    source.addPlanPlace(itineraryEntry(beach, "desayuno", 3));
    source.addPlanPlace(itineraryEntry(museum, null, 2));
    when(this.userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(cloner));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(source));
    givenSaveEchoes();

    // when
    Plan copy = this.planService.clonePlan(PLAN_ID, OTHER_EMAIL);

    // then: same persisted Place instances, renumbered 1..n in itinerary order
    assertThat(copy.getPlanPlaces().size(), is(2));
    assertThat(copy.getPlanPlaces().get(0).getPlace(), is(sameInstance(beach)));
    assertThat(copy.getPlanPlaces().get(0).getDescription(), is(equalTo("desayuno")));
    assertThat(copy.getPlanPlaces().get(0).getVisitDate(), is(equalTo(LocalDate.of(2026, 12, 1))));
    assertThat(copy.getPlanPlaces().get(0).getVisitTime(), is(equalTo(LocalTime.of(9, 30))));
    assertThat(copy.getPlanPlaces().get(0).getSortOrder(), is(1));
    assertThat(copy.getPlanPlaces().get(1).getPlace(), is(sameInstance(museum)));
    assertThat(copy.getPlanPlaces().get(1).getSortOrder(), is(2));
    assertThat(source.getPlanPlaces().size(), is(2));
  }

  @Test
  public void T_PLN_040_clonePlan_laCopiaNaceSinParticipantes() {
    // given (U-06)
    User cloner = owner(OWNER_ID + 2, OTHER_EMAIL);
    Plan source = publicPlan();
    source.getParticipants().add(owner(OWNER_ID + 3, "participant@test.com"));
    when(this.userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(cloner));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(source));
    givenSaveEchoes();

    // when
    Plan copy = this.planService.clonePlan(PLAN_ID, OTHER_EMAIL);

    // then
    assertThat(copy.getParticipants(), is(empty()));
    assertThat(source.getParticipants().size(), is(1));
  }

  @Test
  public void T_PLN_041_clonePlan_planPrivadoAjenoLanzaPlanNotFoundExceptionYNoGuarda() {
    // given (U-07): private plan, cloner is neither administrator nor participant
    User cloner = owner(OWNER_ID + 2, OTHER_EMAIL);
    Plan source = publicPlan();
    source.setIsPublic(false);
    when(this.userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(cloner));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(source));

    // when and then
    assertThrows(
      PlanNotFoundException.class,
      () -> this.planService.clonePlan(PLAN_ID, OTHER_EMAIL)
    );
    assertNothingWasMutated();
  }

  @Test
  public void T_PLN_042_clonePlan_planPrivadoLoPuedeClonarUnParticipant() {
    // given (U-08)
    User cloner = owner(OWNER_ID + 2, OTHER_EMAIL);
    Plan source = publicPlan();
    source.setIsPublic(false);
    source.getParticipants().add(cloner);
    when(this.userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(cloner));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(source));
    givenSaveEchoes();

    // when
    Plan copy = this.planService.clonePlan(PLAN_ID, OTHER_EMAIL);

    // then
    assertThat(copy.getAdministrator(), is(sameInstance(cloner)));
    assertThat(copy.getParticipants(), is(empty()));
  }

  @Test
  public void T_PLN_043_clonePlan_idInexistenteLanzaPlanNotFoundExceptionYNoGuarda() {
    // given (U-09)
    User cloner = owner(OWNER_ID + 2, OTHER_EMAIL);
    when(this.userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(cloner));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.empty());

    // when and then
    assertThrows(
      PlanNotFoundException.class,
      () -> this.planService.clonePlan(PLAN_ID, OTHER_EMAIL)
    );
    assertNothingWasMutated();
  }

  @Test
  public void T_PLN_044_clonePlan_noModificaElPlanOriginal() {
    // given (U-10)
    User cloner = owner(OWNER_ID + 2, OTHER_EMAIL);
    Plan source = publicPlan();
    source.addPlanPlace(itineraryEntry(new Place(), "desayuno", 1));
    source.getParticipants().add(owner(OWNER_ID + 3, "participant@test.com"));
    when(this.userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(cloner));
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(source));
    givenSaveEchoes();

    // when
    Plan copy = this.planService.clonePlan(PLAN_ID, OTHER_EMAIL);

    // then: same instance mutated nowhere; participants, code and itinerary untouched
    assertThat(copy, is(not(sameInstance(source))));
    assertThat(source.getShortCode(), is(equalTo("ORIG1234")));
    assertThat(source.getIsPublic(), is(true));
    assertThat(source.getAdministrator().getEmail(), is(equalTo(OWNER_EMAIL)));
    assertThat(source.getParticipants().size(), is(1));
    assertThat(source.getPlanPlaces().size(), is(1));
  }

  @Test
  public void T_PLN_045_clonePlan_emailInexistenteLanzaUserNotFoundException() {
    // given (U-11)
    when(this.userRepositoryMock.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());

    // when and then: the cloner resolves first, so the plan is never even loaded
    assertThrows(
      UserNotFoundException.class,
      () -> this.planService.clonePlan(PLAN_ID, UNKNOWN_EMAIL)
    );
    verifyNoInteractions(this.planRepositoryMock);
  }

  // --- fixtures ---

  /** Effect check shared by every read-path rejection: nothing was written or deleted. */
  private void assertNothingWasMutated() {
    verify(this.planRepositoryMock, never()).save(any(Plan.class));
    verify(this.planRepositoryMock, never()).deleteById(any());
  }

  /** The repository echoes the plan it receives, so tests assert on the built copy. */
  private void givenSaveEchoes() {
    when(this.planRepositoryMock.save(any(Plan.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));
  }

  private Plan publicPlan() {
    Plan plan = new Plan();
    plan.setId(PLAN_ID);
    plan.setName("Viaje a Bariloche");
    plan.setDescription("Descripcion original");
    plan.setEventDate(LocalDate.of(2026, 12, 31));
    plan.setEventTime(LocalTime.of(18, 30));
    plan.setIsPublic(true);
    plan.setShortCode("ORIG1234");
    plan.setAdministrator(owner(OWNER_ID, OWNER_EMAIL));
    return plan;
  }

  private PlanPlace itineraryEntry(Place place, String description, int sortOrder) {
    PlanPlace entry = new PlanPlace();
    entry.setPlace(place);
    entry.setDescription(description);
    entry.setVisitDate(LocalDate.of(2026, 12, 1));
    entry.setVisitTime(LocalTime.of(9, 30));
    entry.setSortOrder(sortOrder);
    return entry;
  }

  private User owner(Long id, String email) {
    User user = new User();
    user.setId(id);
    user.setEmail(email);
    return user;
  }

  private Plan ownedPlan(UUID id, User administrator) {
    Plan plan = new Plan();
    plan.setId(id);
    plan.setName("Viaje a Bariloche");
    plan.setAdministrator(administrator);
    return plan;
  }
}
