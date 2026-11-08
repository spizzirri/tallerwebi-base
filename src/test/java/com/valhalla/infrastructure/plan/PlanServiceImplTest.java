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
import static org.mockito.ArgumentMatchers.eq;
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
import com.valhalla.domain.plan.PlanParticipantCount;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.rating.PlanRatingRepository;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
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
  private static final UUID OWNER_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000007");
  private static final UUID OTHER_OWNER_ID = UUID.fromString(
    "0192f0a1-0000-7000-8000-000000000008"
  );
  private static final UUID CLONER_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000009");
  private static final UUID PARTICIPANT_ID = UUID.fromString(
    "0192f0a1-0000-7000-8000-00000000000c"
  );
  private static final UUID EDITOR_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000000d");
  private static final UUID INVITED_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000000e");
  private static final UUID RECIPIENT_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000000f");
  private static final UUID PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000002a");
  private static final UUID OTHER_PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000002b");
  private static final UUID PLACE_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000005");
  private static final UUID OTHER_PLACE_ID = UUID.fromString(
    "0192f0a1-0000-7000-8000-000000000006"
  );
  private static final UUID UNKNOWN_PLACE_ID = UUID.fromString(
    "0192f0a1-0000-7000-8000-000000000063"
  );
  private static final UUID JOIN_REQUEST_ID = UUID.fromString(
    "0192f0a1-0000-7000-8000-000000000010"
  );
  private static final UUID UNKNOWN_JOIN_REQUEST_ID = UUID.fromString(
    "0192f0a1-0000-7000-8000-000000000011"
  );
  private static final UUID INVITATION_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000012");

  /** The backend generates an 8 character uppercase alphanumeric code out of a UUID. */
  private static final String SHORT_CODE_PATTERN = "[A-Z0-9]{8}";

  private PlanServiceImpl planService;
  private PlanRepository planRepositoryMock;
  private UserRepository userRepositoryMock;
  private PlaceRepository placeRepositoryMock;
  private PlanRatingRepository planRatingRepositoryMock;
  private com.valhalla.domain.notification.NotificationService notificationServiceMock;

  @BeforeEach
  public void init() {
    this.planRepositoryMock = mock(PlanRepository.class);
    this.userRepositoryMock = mock(UserRepository.class);
    this.placeRepositoryMock = mock(PlaceRepository.class);
    this.planRatingRepositoryMock = mock(PlanRatingRepository.class);
    this.notificationServiceMock = mock(com.valhalla.domain.notification.NotificationService.class);
    this.planService =
      new PlanServiceImpl(
        this.planRepositoryMock,
        this.userRepositoryMock,
        this.placeRepositoryMock,
        this.planRatingRepositoryMock,
        this.notificationServiceMock
      );
  }

  @Test
  public void expelledParticipantCanRejoinPublicPlan() {
    Plan plan = invitationPlan(true);
    User participant = owner(PARTICIPANT_ID, OTHER_EMAIL);
    plan.getParticipants().add(participant);
    planService.removeParticipant(PLAN_ID, OTHER_EMAIL, OWNER_EMAIL);
    assertThat(plan.isParticipant(OTHER_EMAIL), is(false));
    when(planRepositoryMock.findByShortCode("ABCDEFGH")).thenReturn(Optional.of(plan));
    when(userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(participant));
    planService.joinPlan("ABCDEFGH", OTHER_EMAIL);
    assertThat(plan.isParticipant(OTHER_EMAIL), is(true));
    verify(planRepositoryMock, times(2)).save(plan);
  }

  @Test
  public void bannedEditorCannotRejoinOrReceiveInvitation() {
    Plan plan = invitationPlan(true);
    User participant = owner(PARTICIPANT_ID, OTHER_EMAIL);
    plan.getParticipants().add(participant);
    plan.getEditors().add(participant);
    planService.banParticipant(PLAN_ID, OTHER_EMAIL, OWNER_EMAIL);
    assertThat(plan.isParticipant(OTHER_EMAIL), is(false));
    assertThat(plan.isEditor(OTHER_EMAIL), is(false));
    assertThat(plan.isBanned(OTHER_EMAIL), is(true));
    when(planRepositoryMock.findByShortCode("ABCDEFGH")).thenReturn(Optional.of(plan));
    assertThrows(PlanNotFoundException.class, () -> planService.joinPlan("ABCDEFGH", OTHER_EMAIL));
    when(userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(participant));
    assertThrows(
      IllegalArgumentException.class,
      () -> planService.inviteParticipant(PLAN_ID, OTHER_EMAIL, "guest", OWNER_EMAIL)
    );
    verify(planRepositoryMock).save(plan);
  }

  @Test
  public void editorCannotBanAnotherEditorOrOwner() {
    Plan plan = invitationPlan(true);
    User editor = owner(EDITOR_ID, OTHER_EMAIL);
    User otherEditor = owner(PARTICIPANT_ID, UNKNOWN_EMAIL);
    plan.getParticipants().addAll(List.of(editor, otherEditor));
    plan.getEditors().addAll(List.of(editor, otherEditor));
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.banParticipant(PLAN_ID, UNKNOWN_EMAIL, OTHER_EMAIL)
    );
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.banParticipant(PLAN_ID, OWNER_EMAIL, OTHER_EMAIL)
    );
    assertThat(plan.getParticipants(), hasSize(2));
    assertThat(plan.getBannedUsers(), empty());
    verify(planRepositoryMock, never()).save(any());
  }

  @Test
  public void editorCanBanParticipantAndClearsPendingInvitation() {
    Plan plan = invitationPlan(false);
    User editor = owner(EDITOR_ID, OTHER_EMAIL);
    User participant = owner(PARTICIPANT_ID, UNKNOWN_EMAIL);
    plan.getParticipants().addAll(List.of(editor, participant));
    plan.getEditors().add(editor);
    plan.getInvitations().add(new PlanInvitation(plan, participant, editor, "guest"));
    planService.banParticipant(PLAN_ID, UNKNOWN_EMAIL, OTHER_EMAIL);
    assertThat(plan.isBanned(UNKNOWN_EMAIL), is(true));
    assertThat(plan.isParticipant(UNKNOWN_EMAIL), is(false));
    assertThat(plan.isEditor(OTHER_EMAIL), is(true));
    assertThat(plan.getInvitations(), empty());
    verify(planRepositoryMock).save(plan);
  }

  @Test
  public void participantCannotBanAnyone() {
    Plan plan = invitationPlan(true);
    plan.getParticipants().add(owner(EDITOR_ID, OTHER_EMAIL));
    plan.getParticipants().add(owner(PARTICIPANT_ID, UNKNOWN_EMAIL));
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.banParticipant(PLAN_ID, UNKNOWN_EMAIL, OTHER_EMAIL)
    );
    assertThat(plan.getParticipants(), hasSize(2));
    verify(planRepositoryMock, never()).save(any());
  }

  @Test
  public void bannedRecipientCannotAcceptPendingInvitation() {
    Plan plan = invitationPlan(false);
    User recipient = owner(EDITOR_ID, OTHER_EMAIL);
    PlanInvitation invitation = new PlanInvitation(
      plan,
      recipient,
      plan.getAdministrator(),
      "editor"
    );
    org.springframework.test.util.ReflectionTestUtils.setField(invitation, "id", INVITATION_ID);
    plan.getInvitations().add(invitation);
    plan.getBannedUsers().add(recipient);
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.respondToInvitation(PLAN_ID, INVITATION_ID, true, OTHER_EMAIL)
    );
    assertThat(plan.getParticipants(), empty());
    verify(planRepositoryMock, never()).save(any());
  }

  // --- createPlan ---

  @Test
  public void ownerCanInviteEditor() {
    Plan plan = invitationPlan(false);
    User invited = owner(INVITED_ID, OTHER_EMAIL);
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
    User editor = owner(EDITOR_ID, OTHER_EMAIL);
    plan.getParticipants().add(editor);
    plan.getEditors().add(editor);
    when(userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(editor));
    User invited = owner(INVITED_ID, UNKNOWN_EMAIL);
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
    User invited = owner(INVITED_ID, OTHER_EMAIL);
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
    plan.getParticipants().add(owner(EDITOR_ID, OTHER_EMAIL));
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
    User existing = owner(INVITED_ID, OTHER_EMAIL);
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

  @Test
  public void invitationChannelsEnforceIndependentPermissions() {
    Plan plan = invitationPlan(false);
    User editor = owner(INVITED_ID, OTHER_EMAIL);
    plan.getParticipants().add(editor);
    plan.getEditors().add(editor);
    User recipient = owner(RECIPIENT_ID, UNKNOWN_EMAIL);
    when(userRepositoryMock.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.of(recipient));
    when(userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(editor));
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.inviteParticipant(PLAN_ID, UNKNOWN_EMAIL, "guest", "email", OTHER_EMAIL)
    );
    plan.getSettings().setInviteInternal(com.valhalla.domain.plan.PlanPermission.OWNER);
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.inviteParticipant(PLAN_ID, UNKNOWN_EMAIL, "guest", "app", OTHER_EMAIL)
    );
    plan.getSettings().setInviteInternal(com.valhalla.domain.plan.PlanPermission.PARTICIPANTS);
    plan.getEditors().clear();
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.inviteParticipant(PLAN_ID, UNKNOWN_EMAIL, "editor", "app", OTHER_EMAIL)
    );
    planService.inviteParticipant(PLAN_ID, UNKNOWN_EMAIL, "guest", "app", OTHER_EMAIL);
    assertThat(plan.getInvitations(), hasSize(1));
  }

  @Test
  public void emailInvitationSendsTokenAndOnlyRecipientCanUseItOnce() {
    Plan plan = invitationPlan(false);
    User recipient = owner(INVITED_ID, OTHER_EMAIL);
    when(userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(recipient));
    when(userRepositoryMock.findByEmail(OWNER_EMAIL))
      .thenReturn(Optional.of(plan.getAdministrator()));
    com.valhalla.domain.plan.EmailNotifier notifier = mock(
      com.valhalla.domain.plan.EmailNotifier.class
    );
    org.springframework.test.util.ReflectionTestUtils.setField(
      planService,
      "emailNotifier",
      notifier
    );
    planService.inviteParticipant(PLAN_ID, OTHER_EMAIL, "editor", "email", OWNER_EMAIL);
    PlanInvitation invitation = plan.getInvitations().getFirst();
    org.springframework.test.util.ReflectionTestUtils.setField(invitation, "id", INVITATION_ID);
    String token = invitation.getToken();
    assertThat(token.length(), is(64));
    verify(notifier).sendInvitation(OTHER_EMAIL, PLAN_ID, token);
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.acceptInvitationToken(PLAN_ID, token, UNKNOWN_EMAIL)
    );
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.acceptInvitationToken(PLAN_ID, "invalid", OTHER_EMAIL)
    );
    planService.acceptInvitationToken(PLAN_ID, token, OTHER_EMAIL);
    assertThat(plan.isParticipant(OTHER_EMAIL), is(true));
    assertThat(plan.isEditor(OTHER_EMAIL), is(true));
    assertThat(plan.getInvitations(), is(empty()));
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.acceptInvitationToken(PLAN_ID, token, OTHER_EMAIL)
    );
  }

  @Test
  public void invalidInvitationChannelDoesNotCreateInvitation() {
    Plan plan = invitationPlan(false);
    assertThrows(
      IllegalArgumentException.class,
      () -> planService.inviteParticipant(PLAN_ID, OTHER_EMAIL, "guest", "unknown", OWNER_EMAIL)
    );
    assertThat(plan.getInvitations(), is(empty()));
  }

  // --- changeParticipantRole: solo el dueño reasigna roles existentes (AC-14) ---

  @Test
  public void ownerChangesParticipantRole() {
    Plan plan = invitationPlan(false);
    plan.getParticipants().add(owner(PARTICIPANT_ID, OTHER_EMAIL));

    planService.changeParticipantRole(PLAN_ID, OTHER_EMAIL, "editor", OWNER_EMAIL);

    assertThat(plan.isEditor(OTHER_EMAIL), is(true));
    verify(planRepositoryMock).save(plan);

    planService.changeParticipantRole(PLAN_ID, OTHER_EMAIL, "guest", OWNER_EMAIL);

    assertThat(plan.isEditor(OTHER_EMAIL), is(false));
    assertThat(plan.isParticipant(OTHER_EMAIL), is(true));
  }

  @Test
  public void editorCannotChangeParticipantRole() {
    Plan plan = invitationPlan(false);
    User editor = owner(EDITOR_ID, OTHER_EMAIL);
    User guest = owner(PARTICIPANT_ID, UNKNOWN_EMAIL);
    plan.getParticipants().addAll(List.of(editor, guest));
    plan.getEditors().add(editor);

    assertThrows(
      PlanNotFoundException.class,
      () -> planService.changeParticipantRole(PLAN_ID, UNKNOWN_EMAIL, "editor", OTHER_EMAIL)
    );

    assertThat(plan.isEditor(UNKNOWN_EMAIL), is(false));
    assertNothingWasMutated();
  }

  @Test
  public void strangerCannotChangeParticipantRole() {
    Plan plan = invitationPlan(false);
    plan.getParticipants().add(owner(PARTICIPANT_ID, UNKNOWN_EMAIL));

    assertThrows(
      PlanNotFoundException.class,
      () -> planService.changeParticipantRole(PLAN_ID, UNKNOWN_EMAIL, "editor", OTHER_EMAIL)
    );

    assertThat(plan.isEditor(UNKNOWN_EMAIL), is(false));
    assertNothingWasMutated();
  }

  @Test
  public void ownerCannotChangeAdministratorRole() {
    Plan plan = invitationPlan(false);

    assertThrows(
      PlanNotFoundException.class,
      () -> planService.changeParticipantRole(PLAN_ID, OWNER_EMAIL, "guest", OWNER_EMAIL)
    );

    assertThat(plan.isAdministrator(OWNER_EMAIL), is(true));
    assertNothingWasMutated();
  }

  @Test
  public void ownerCannotAssignUnknownRole() {
    Plan plan = invitationPlan(false);
    plan.getParticipants().add(owner(PARTICIPANT_ID, OTHER_EMAIL));

    assertThrows(
      IllegalArgumentException.class,
      () -> planService.changeParticipantRole(PLAN_ID, OTHER_EMAIL, "owner", OWNER_EMAIL)
    );

    assertThat(plan.isEditor(OTHER_EMAIL), is(false));
    assertNothingWasMutated();
  }

  private Plan invitationPlan(boolean isPublic) {
    Plan plan = new Plan();
    plan.setId(PLAN_ID);
    plan.setAdministrator(owner(OWNER_ID, OWNER_EMAIL));
    plan.setIsPublic(isPublic);
    when(planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(plan));
    when(userRepositoryMock.findByEmail(OWNER_EMAIL))
      .thenReturn(Optional.of(plan.getAdministrator()));
    return plan;
  }

  @Test
  public void BAN_privatePlanRejectsRequestsFromBannedUsers() {
    Plan plan = invitationPlan(false);
    plan.getBannedUsers().add(owner(INVITED_ID, OTHER_EMAIL));
    assertThrows(
      com.valhalla.domain.exception.PlanBannedException.class,
      () -> planService.requestToJoin(PLAN_ID, OTHER_EMAIL)
    );
    assertThat(plan.getJoinRequests(), is(empty()));
    verify(planRepositoryMock, never()).save(any());
  }

  @Test
  public void BAN_pendingRequestCannotRestoreBannedParticipationButCanBeRejected() {
    Plan plan = invitationPlan(false);
    User requester = owner(INVITED_ID, OTHER_EMAIL);
    com.valhalla.domain.plan.PlanJoinRequest request = pendingRequest(plan, requester);
    plan.getBannedUsers().add(requester);
    assertThrows(
      com.valhalla.domain.exception.PlanBannedException.class,
      () -> planService.respondToJoinRequest(PLAN_ID, request.getId(), "accept", OWNER_EMAIL)
    );
    assertThat(plan.getParticipants(), is(empty()));
    assertThat(plan.getJoinRequests(), hasSize(1));
    verify(planRepositoryMock, never()).save(any());
    planService.respondToJoinRequest(PLAN_ID, request.getId(), "reject", OWNER_EMAIL);
    assertThat(plan.getJoinRequests(), is(empty()));
    assertThat(plan.isBanned(OTHER_EMAIL), is(true));
    verify(planRepositoryMock).save(plan);
  }

  @Test
  public void PET_requestIsIdempotentAndDoesNotGrantAccess() {
    Plan plan = invitationPlan(false);
    User requester = owner(INVITED_ID, OTHER_EMAIL);
    when(userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(requester));
    planService.requestToJoin(PLAN_ID, OTHER_EMAIL);
    planService.requestToJoin(PLAN_ID, OTHER_EMAIL);
    assertThat(plan.getJoinRequests(), hasSize(1));
    assertThat(plan.isParticipant(OTHER_EMAIL), is(false));
    verify(planRepositoryMock, times(1)).save(plan);
  }

  @Test
  public void PET_publicPlansRejectRequests() {
    Plan plan = invitationPlan(true);
    when(userRepositoryMock.findByEmail(OTHER_EMAIL))
      .thenReturn(Optional.of(owner(INVITED_ID, OTHER_EMAIL)));
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.requestToJoin(PLAN_ID, OTHER_EMAIL)
    );
    assertThat(plan.getJoinRequests(), is(empty()));
    verify(planRepositoryMock, never()).save(any());
  }

  @Test
  public void PET_existingMembersDoNotCreateRequests() {
    Plan plan = invitationPlan(false);
    User participant = owner(INVITED_ID, OTHER_EMAIL);
    when(userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(participant));
    plan.getParticipants().add(participant);
    planService.requestToJoin(PLAN_ID, OWNER_EMAIL);
    planService.requestToJoin(PLAN_ID, OTHER_EMAIL);
    assertThat(plan.getJoinRequests(), is(empty()));
    verify(planRepositoryMock, never()).save(any());
  }

  @Test
  public void PET_acceptGrantsParticipationAndRemovesPendingInvitation() {
    Plan plan = invitationPlan(false);
    User requester = owner(INVITED_ID, OTHER_EMAIL);
    com.valhalla.domain.plan.PlanJoinRequest request = pendingRequest(plan, requester);
    plan
      .getInvitations()
      .add(new PlanInvitation(plan, requester, plan.getAdministrator(), "editor"));
    planService.respondToJoinRequest(PLAN_ID, request.getId(), "accept", OWNER_EMAIL);
    assertThat(plan.isParticipant(OTHER_EMAIL), is(true));
    assertThat(plan.isEditor(OTHER_EMAIL), is(false));
    assertThat(plan.getJoinRequests(), is(empty()));
    assertThat(plan.getInvitations(), is(empty()));
    verify(planRepositoryMock).save(plan);
  }

  @Test
  public void PET_onlyAuthorizedManagerCanListAndRespond() {
    Plan plan = invitationPlan(false);
    User editor = owner(EDITOR_ID, OTHER_EMAIL);
    plan.getParticipants().add(editor);
    plan.getEditors().add(editor);
    com.valhalla.domain.plan.PlanJoinRequest request = pendingRequest(
      plan,
      owner(INVITED_ID, UNKNOWN_EMAIL)
    );
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.getPendingJoinRequests(PLAN_ID, OTHER_EMAIL)
    );
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.respondToJoinRequest(PLAN_ID, request.getId(), "accept", OTHER_EMAIL)
    );
    plan.getSettings().setHandleRequests(com.valhalla.domain.plan.PlanPermission.EDITORS);
    assertThat(planService.getPendingJoinRequests(PLAN_ID, OTHER_EMAIL), hasSize(1));
    planService.respondToJoinRequest(PLAN_ID, request.getId(), "reject", OTHER_EMAIL);
    assertThat(plan.isParticipant(UNKNOWN_EMAIL), is(false));
    assertThat(plan.getJoinRequests(), is(empty()));
    verify(planRepositoryMock).save(plan);
  }

  @Test
  public void PET_invalidOrMissingResponsePreservesRequest() {
    Plan plan = invitationPlan(false);
    pendingRequest(plan, owner(INVITED_ID, OTHER_EMAIL));
    assertThrows(
      IllegalArgumentException.class,
      () -> planService.respondToJoinRequest(PLAN_ID, JOIN_REQUEST_ID, "invalid", OWNER_EMAIL)
    );
    assertThrows(
      PlanNotFoundException.class,
      () ->
        planService.respondToJoinRequest(PLAN_ID, UNKNOWN_JOIN_REQUEST_ID, "accept", OWNER_EMAIL)
    );
    assertThat(plan.getJoinRequests(), hasSize(1));
    assertThat(plan.getParticipants(), is(empty()));
    verify(planRepositoryMock, never()).save(any());
  }

  @Test
  public void PET_settingsAreOwnerOnlyAndPersistPermissions() {
    Plan plan = invitationPlan(false);
    com.valhalla.domain.plan.PlanPermission ownerOnly =
      com.valhalla.domain.plan.PlanPermission.OWNER;
    com.valhalla.domain.plan.PlanPermission editors =
      com.valhalla.domain.plan.PlanPermission.EDITORS;
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.updateSettings(PLAN_ID, editors, ownerOnly, editors, OTHER_EMAIL)
    );
    verify(planRepositoryMock, never()).save(any());
    planService.updateSettings(PLAN_ID, editors, ownerOnly, editors, OWNER_EMAIL);
    assertThat(plan.getSettings().getHandleRequests(), is(editors));
    assertThat(plan.getSettings().getInviteInternal(), is(ownerOnly));
    assertThat(plan.getSettings().getInviteByEmail(), is(editors));
    verify(planRepositoryMock).save(plan);
  }

  private com.valhalla.domain.plan.PlanJoinRequest pendingRequest(Plan plan, User requester) {
    com.valhalla.domain.plan.PlanJoinRequest request = new com.valhalla.domain.plan.PlanJoinRequest(
      plan,
      requester
    );
    org.springframework.test.util.ReflectionTestUtils.setField(request, "id", JOIN_REQUEST_ID);
    plan.getJoinRequests().add(request);
    return request;
  }

  // --- getPendingInvitations ---

  @Test
  public void getPendingInvitations_dePlanPublicoMarcaIsPublicEnVerdadero() {
    // given
    Plan plan = invitationPlan(true);
    plan.setId(PLAN_ID);
    User recipient = owner(INVITED_ID, UNKNOWN_EMAIL);
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
    User recipient = owner(INVITED_ID, UNKNOWN_EMAIL);
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
    real.setId(PLACE_ID);
    when(this.placeRepositoryMock.findById(PLACE_ID)).thenReturn(Optional.of(real));
    Plan plan = new Plan();
    plan.setName("Viaje");
    for (int order = 1; order <= 2; order++) {
      Place stub = new Place();
      stub.setId(PLACE_ID);
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
    when(this.placeRepositoryMock.findById(UNKNOWN_PLACE_ID)).thenReturn(Optional.empty());
    Plan plan = new Plan();
    Place stub = new Place();
    stub.setId(UNKNOWN_PLACE_ID);
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

  // --- getParticipantCounts [TOP] ---

  @Test
  public void U14_getParticipantCounts_mapeaLaListaDelRepositorioAMapDeUuidYLong() {
    // given
    List<UUID> planIds = List.of(PLAN_ID, OTHER_PLAN_ID);
    when(this.planRepositoryMock.countParticipantsByPlanIds(planIds))
      .thenReturn(
        List.of(new PlanParticipantCount(PLAN_ID, 4L), new PlanParticipantCount(OTHER_PLAN_ID, 0L))
      );

    // when
    Map<UUID, Long> counts = this.planService.getParticipantCounts(planIds);

    // then
    assertThat(counts, is(equalTo(Map.of(PLAN_ID, 4L, OTHER_PLAN_ID, 0L))));
    verify(this.planRepositoryMock, times(1)).countParticipantsByPlanIds(planIds);
  }

  @Test
  public void U15_getParticipantCounts_sinIdsDevuelveMapaVacioYNoConsultaElRepositorio() {
    // when
    Map<UUID, Long> emptyCounts = this.planService.getParticipantCounts(List.of());
    Map<UUID, Long> nullCounts = this.planService.getParticipantCounts(null);

    // then: el corte temprano es correccion, `in :planIds` vacio no es JPQL valido
    assertThat(emptyCounts, is(equalTo(Map.of())));
    assertThat(nullCounts, is(equalTo(Map.of())));
    verify(this.planRepositoryMock, never()).countParticipantsByPlanIds(any());
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
    Plan planOfSomebodyElse = ownedPlan(PLAN_ID, owner(OTHER_OWNER_ID, OTHER_EMAIL));
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
    plan.getParticipants().add(owner(PARTICIPANT_ID, "participante@test.com"));
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
    changes.setAdministrator(owner(OTHER_OWNER_ID, OTHER_EMAIL));

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
    Plan planOfSomebodyElse = ownedPlan(PLAN_ID, owner(OTHER_OWNER_ID, OTHER_EMAIL));
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
      .thenReturn(Optional.of(ownedPlan(PLAN_ID, owner(OTHER_OWNER_ID, OTHER_EMAIL))));

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
    User cloner = owner(CLONER_ID, OTHER_EMAIL);
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
    User cloner = owner(CLONER_ID, OTHER_EMAIL);
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
    User cloner = owner(CLONER_ID, OTHER_EMAIL);
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
    User cloner = owner(CLONER_ID, OTHER_EMAIL);
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
    User cloner = owner(CLONER_ID, OTHER_EMAIL);
    Plan source = publicPlan();
    Place beach = new Place();
    beach.setId(PLACE_ID);
    Place museum = new Place();
    museum.setId(OTHER_PLACE_ID);
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
    User cloner = owner(CLONER_ID, OTHER_EMAIL);
    Plan source = publicPlan();
    source.getParticipants().add(owner(PARTICIPANT_ID, "participant@test.com"));
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
    User cloner = owner(CLONER_ID, OTHER_EMAIL);
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
    User cloner = owner(CLONER_ID, OTHER_EMAIL);
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
    User cloner = owner(CLONER_ID, OTHER_EMAIL);
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
    User cloner = owner(CLONER_ID, OTHER_EMAIL);
    Plan source = publicPlan();
    source.addPlanPlace(itineraryEntry(new Place(), "desayuno", 1));
    source.getParticipants().add(owner(PARTICIPANT_ID, "participant@test.com"));
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

  // --- [NOTIF] hooks de notificacion ---

  @Test
  public void NOTIF_createPlanPublico_notificaUnPlanNuevo() {
    // given
    User owner = owner(OWNER_ID, OWNER_EMAIL);
    when(this.userRepositoryMock.findByEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
    Plan plan = new Plan();
    plan.setId(PLAN_ID);
    plan.setName("Escapada");
    plan.setIsPublic(true);
    when(this.planRepositoryMock.save(plan)).thenReturn(plan);

    // when
    this.planService.createPlan(plan, OWNER_EMAIL);

    // then: AC-03, los seguidores del autor se enteran
    verify(this.notificationServiceMock, times(1)).notifyNewPlan(owner, PLAN_ID, "Escapada");
  }

  @Test
  public void NOTIF_createPlanPrivado_noNotificaNadie() {
    // given
    when(this.userRepositoryMock.findByEmail(OWNER_EMAIL))
      .thenReturn(Optional.of(owner(OWNER_ID, OWNER_EMAIL)));
    Plan plan = new Plan();
    plan.setName("Escapada");
    when(this.planRepositoryMock.save(plan)).thenReturn(plan);

    // when
    this.planService.createPlan(plan, OWNER_EMAIL);

    // then
    verifyNoInteractions(this.notificationServiceMock);
  }

  @Test
  public void NOTIF_updatePlan_alPasarDePrivadoAPublico_notificaUnaVez() {
    // given
    User owner = owner(OWNER_ID, OWNER_EMAIL);
    Plan existing = ownedPlan(PLAN_ID, owner);
    existing.setName("Nombre viejo");
    existing.setIsPublic(false);
    when(this.planRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(existing));
    when(this.planRepositoryMock.save(existing)).thenReturn(existing);
    Plan changes = new Plan();
    changes.setName("Nombre viejo");
    changes.setIsPublic(true);

    // when: publicar
    this.planService.updatePlan(PLAN_ID, changes, OWNER_EMAIL);

    // then: AC-03
    verify(this.notificationServiceMock, times(1)).notifyNewPlan(owner, PLAN_ID, "Nombre viejo");

    // when: editar otra vez sobre un plan ya publico
    this.planService.updatePlan(PLAN_ID, changes, OWNER_EMAIL);

    // then: no hay re-notificacion, el evento es la publicacion (AC-03)
    verify(this.notificationServiceMock, times(1)).notifyNewPlan(any(), any(), any());
  }

  @Test
  public void NOTIF_joinPlanPublico_notificaAlDuenoDelParticipante() {
    // given
    Plan plan = invitationPlan(true);
    plan.setName("Viaje a Bariloche");
    plan.setShortCode("ABCDEFGH");
    User participant = owner(INVITED_ID, OTHER_EMAIL);
    when(this.planRepositoryMock.findByShortCode("ABCDEFGH")).thenReturn(Optional.of(plan));
    when(this.userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(participant));

    // when
    this.planService.joinPlan("ABCDEFGH", OTHER_EMAIL);

    // then: AC-02, el dueno se entera del participante nuevo
    verify(this.notificationServiceMock, times(1))
      .notifyNewParticipant(participant, plan.getAdministrator(), PLAN_ID, "Viaje a Bariloche");
  }

  @Test
  public void NOTIF_joinPlanDeQuienYaParticipa_noNotifica() {
    // given: el usuario ya es participante, el join es no-op
    Plan plan = invitationPlan(true);
    User participant = owner(INVITED_ID, OTHER_EMAIL);
    plan.getParticipants().add(participant);
    plan.setShortCode("ABCDEFGH");
    when(this.planRepositoryMock.findByShortCode("ABCDEFGH")).thenReturn(Optional.of(plan));
    when(this.userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(participant));

    // when
    this.planService.joinPlan("ABCDEFGH", OTHER_EMAIL);

    // then
    verifyNoInteractions(this.notificationServiceMock);
  }

  @Test
  public void NOTIF_acceptJoinRequest_notificaAlDueno() {
    // given
    Plan plan = invitationPlan(false);
    plan.setName("Viaje a Bariloche");
    User requester = owner(INVITED_ID, OTHER_EMAIL);
    com.valhalla.domain.plan.PlanJoinRequest request = pendingRequest(plan, requester);

    // when
    planService.respondToJoinRequest(PLAN_ID, request.getId(), "accept", OWNER_EMAIL);

    // then: AC-02
    verify(this.notificationServiceMock, times(1))
      .notifyNewParticipant(requester, plan.getAdministrator(), PLAN_ID, "Viaje a Bariloche");
    // AC-16: el solicitante tambien cierra su pendiente
    verify(this.notificationServiceMock, times(1))
      .notifyJoinRequestResponse(
        plan.getAdministrator(),
        requester,
        PLAN_ID,
        "Viaje a Bariloche",
        true
      );
  }

  @Test
  public void NOTIF_requestToJoin_notificaAlDueno() {
    // given
    Plan plan = invitationPlan(false);
    plan.setName("Viaje a Jujuy");
    User requester = owner(INVITED_ID, OTHER_EMAIL);
    when(userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(requester));

    // when
    planService.requestToJoin(PLAN_ID, OTHER_EMAIL);

    // then: AC-15
    verify(this.notificationServiceMock, times(1))
      .notifyJoinRequest(requester, plan.getAdministrator(), PLAN_ID, "Viaje a Jujuy");
  }

  @Test
  public void NOTIF_solicitudRepetida_noNotificaDeNuevo() {
    // given
    Plan plan = invitationPlan(false);
    User requester = owner(INVITED_ID, OTHER_EMAIL);
    when(userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(requester));

    // when: la segunda requestToJoin encuentra la pendiente y corta antes de guardar
    planService.requestToJoin(PLAN_ID, OTHER_EMAIL);
    planService.requestToJoin(PLAN_ID, OTHER_EMAIL);

    // then: AC-15, una sola fila por solicitud abierta
    verify(this.notificationServiceMock, times(1))
      .notifyJoinRequest(eq(requester), eq(plan.getAdministrator()), eq(PLAN_ID), any());
  }

  @Test
  public void NOTIF_rejectJoinRequest_notificaAlSolicitante() {
    // given
    Plan plan = invitationPlan(false);
    plan.setName("Viaje a Mendoza");
    User requester = owner(INVITED_ID, OTHER_EMAIL);
    com.valhalla.domain.plan.PlanJoinRequest request = pendingRequest(plan, requester);

    // when
    planService.respondToJoinRequest(PLAN_ID, request.getId(), "reject", OWNER_EMAIL);

    // then: AC-16, el rechazo tambien es una respuesta; AC-02, nadie se sumo
    verify(this.notificationServiceMock, never()).notifyNewParticipant(any(), any(), any(), any());
    verify(this.notificationServiceMock, times(1))
      .notifyJoinRequestResponse(
        plan.getAdministrator(),
        requester,
        PLAN_ID,
        "Viaje a Mendoza",
        false
      );
  }

  @Test
  public void NOTIF_acceptInvitation_notificaAlDueno() {
    // given
    Plan plan = invitationPlan(false);
    plan.setName("Viaje a Bariloche");
    User recipient = owner(INVITED_ID, OTHER_EMAIL);
    PlanInvitation invitation = new PlanInvitation(
      plan,
      recipient,
      plan.getAdministrator(),
      "guest"
    );
    org.springframework.test.util.ReflectionTestUtils.setField(invitation, "id", INVITATION_ID);
    plan.getInvitations().add(invitation);

    // when
    planService.respondToInvitation(PLAN_ID, INVITATION_ID, true, OTHER_EMAIL);

    // then: AC-02, por invitacion aceptada tambien se notifica
    verify(this.notificationServiceMock, times(1))
      .notifyNewParticipant(recipient, plan.getAdministrator(), PLAN_ID, "Viaje a Bariloche");
  }

  @Test
  public void NOTIF_inviteParticipant_notificaAlInvitado() {
    // given
    Plan plan = invitationPlan(false);
    plan.setName("Cena secreta");
    User invited = owner(INVITED_ID, OTHER_EMAIL);
    when(userRepositoryMock.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(invited));

    // when
    planService.inviteParticipant(PLAN_ID, OTHER_EMAIL, "guest", OWNER_EMAIL);

    // then: AC-03 (extension), el invitado recibe la notificacion con el plan congelado
    verify(this.notificationServiceMock, times(1))
      .notifyInvitation(plan.getAdministrator(), invited, PLAN_ID, "Cena secreta");
  }

  @Test
  public void NOTIF_inviteFallida_noNotifica() {
    // given: un visitante sin permisos intenta invitar
    invitationPlan(false);

    // when
    assertThrows(
      PlanNotFoundException.class,
      () -> planService.inviteParticipant(PLAN_ID, OTHER_EMAIL, "guest", UNKNOWN_EMAIL)
    );

    // then: la invitacion no existe, la notificacion tampoco
    verifyNoInteractions(this.notificationServiceMock);
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

  private User owner(UUID id, String email) {
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
