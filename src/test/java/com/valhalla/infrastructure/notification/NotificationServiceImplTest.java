package com.valhalla.infrastructure.notification;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.valhalla.domain.follow.FollowRepository;
import com.valhalla.domain.notification.Notification;
import com.valhalla.domain.notification.NotificationRepository;
import com.valhalla.domain.notification.NotificationType;
import com.valhalla.domain.user.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * [NOTIF] Unit tests de {@link NotificationServiceImpl} (T-NOTIF-B01): las reglas de alta —
 * nunca notificar sobre uno mismo, no repetir sin destinatarios — y las lecturas del centro.
 */
public class NotificationServiceImplTest {

  private static final UUID RECIPIENT_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000000a");
  private static final UUID ACTOR_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000000b");
  private static final UUID PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000002a");
  private static final UUID NOTIFICATION_ID = UUID.fromString(
    "0192f0a1-0000-7000-8000-000000000077"
  );

  private NotificationServiceImpl notificationService;
  private NotificationRepository repositoryMock;
  private FollowRepository followRepositoryMock;

  @BeforeEach
  public void init() {
    this.repositoryMock = mock(NotificationRepository.class);
    this.followRepositoryMock = mock(FollowRepository.class);
    this.notificationService =
      new NotificationServiceImpl(this.repositoryMock, this.followRepositoryMock);
  }

  // --- notifyNewFollower ---

  @Test
  public void U01_newFollower_guardaLaNotificacionAlSeguido() {
    this.notificationService.notifyNewFollower(
        user(ACTOR_ID, "ana@test.com"),
        user(RECIPIENT_ID, "grace@test.com")
      );

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(this.repositoryMock, times(1)).save(captor.capture());
    Notification saved = captor.getValue();
    assertThat(saved.getRecipient().getId(), is(RECIPIENT_ID));
    assertThat(saved.getActor().getId(), is(ACTOR_ID));
    assertThat(saved.getType(), is(NotificationType.NEW_FOLLOWER));
  }

  // --- notifyNewParticipant ---

  @Test
  public void U02_newParticipant_guardaAlDuenoConElPlanCongelado() {
    this.notificationService.notifyNewParticipant(
        user(ACTOR_ID, "ana@test.com"),
        user(RECIPIENT_ID, "grace@test.com"),
        PLAN_ID,
        "Viaje a Bariloche"
      );

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(this.repositoryMock, times(1)).save(captor.capture());
    Notification saved = captor.getValue();
    assertThat(saved.getType(), is(NotificationType.NEW_PARTICIPANT));
    assertThat(saved.getRelatedPlanId(), is(PLAN_ID));
    assertThat(saved.getRelatedPlanName(), is("Viaje a Bariloche"));
  }

  @Test
  public void U03_newParticipant_sinDuenoONadieSeNotificaASiMismo() {
    // given: un plan sin administrador resuelto (dato incompleto) y un dueño que se une a si mismo
    this.notificationService.notifyNewParticipant(
        user(ACTOR_ID, "ana@test.com"),
        null,
        PLAN_ID,
        "Plan"
      );
    this.notificationService.notifyNewParticipant(
        user(RECIPIENT_ID, "grace@test.com"),
        user(RECIPIENT_ID, "grace@test.com"),
        PLAN_ID,
        "Plan"
      );

    // then: ninguno de los dos casos escribe
    verifyNoInteractions(this.repositoryMock);
  }

  // --- notifyNewPlan ---

  @Test
  public void U04_newPlan_unaFilaPorCadaSeguidor() {
    // given: el autor tiene dos seguidores
    when(this.followRepositoryMock.findFollowers(RECIPIENT_ID))
      .thenReturn(List.of(user(ACTOR_ID, "ana@test.com"), user(UUID.randomUUID(), "bo@test.com")));

    this.notificationService.notifyNewPlan(
        user(RECIPIENT_ID, "grace@test.com"),
        PLAN_ID,
        "Escapada"
      );

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
    verify(this.repositoryMock, times(1)).saveAll(captor.capture());
    List<Notification> batch = captor.getValue();
    assertThat(batch, hasSize(2));
    assertThat(batch.getFirst().getType(), is(NotificationType.NEW_PLAN));
    assertThat(batch.getFirst().getRecipient().getId(), is(ACTOR_ID));
    assertThat(batch.getFirst().getActor().getId(), is(RECIPIENT_ID));
  }

  @Test
  public void U05_newPlan_sinSeguidoresNoEscribeNada() {
    when(this.followRepositoryMock.findFollowers(RECIPIENT_ID)).thenReturn(List.of());

    this.notificationService.notifyNewPlan(
        user(RECIPIENT_ID, "grace@test.com"),
        PLAN_ID,
        "Escapada"
      );

    verify(this.repositoryMock, never()).saveAll(any());
  }

  // --- notifyInvitation ---

  @Test
  public void U10_invitation_guardaAlInvitadoConElPlanCongelado() {
    this.notificationService.notifyInvitation(
        user(ACTOR_ID, "grace@test.com"),
        user(RECIPIENT_ID, "ana@test.com"),
        PLAN_ID,
        "Cena secreta"
      );

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(this.repositoryMock, times(1)).save(captor.capture());
    Notification saved = captor.getValue();
    assertThat(saved.getRecipient().getId(), is(RECIPIENT_ID));
    assertThat(saved.getActor().getId(), is(ACTOR_ID));
    assertThat(saved.getType(), is(NotificationType.PLAN_INVITATION));
    assertThat(saved.getRelatedPlanId(), is(PLAN_ID));
    assertThat(saved.getRelatedPlanName(), is("Cena secreta"));
  }

  // --- solicitudes de unión ---

  @Test
  public void U11_joinRequest_guardaAlDuenoConElPlanCongelado() {
    this.notificationService.notifyJoinRequest(
        user(ACTOR_ID, "ana@test.com"),
        user(RECIPIENT_ID, "grace@test.com"),
        PLAN_ID,
        "Cena navideña"
      );

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(this.repositoryMock, times(1)).save(captor.capture());
    Notification saved = captor.getValue();
    assertThat(saved.getRecipient().getId(), is(RECIPIENT_ID));
    assertThat(saved.getType(), is(NotificationType.JOIN_REQUESTED));
    assertThat(saved.getRelatedPlanName(), is("Cena navideña"));
  }

  @Test
  public void U12_joinRequestResponse_laAceptacionNaceEnElSolicitante() {
    this.notificationService.notifyJoinRequestResponse(
        user(ACTOR_ID, "grace@test.com"),
        user(RECIPIENT_ID, "ana@test.com"),
        PLAN_ID,
        "Cena navideña",
        true
      );

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(this.repositoryMock, times(1)).save(captor.capture());
    Notification saved = captor.getValue();
    assertThat(saved.getRecipient().getId(), is(RECIPIENT_ID));
    assertThat(saved.getType(), is(NotificationType.JOIN_REQUEST_ACCEPTED));
  }

  @Test
  public void U13_joinRequestResponse_elRechazoUsaElTipoPropio() {
    this.notificationService.notifyJoinRequestResponse(
        user(ACTOR_ID, "grace@test.com"),
        user(RECIPIENT_ID, "ana@test.com"),
        PLAN_ID,
        "Cena navideña",
        false
      );

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(this.repositoryMock, times(1)).save(captor.capture());
    assertThat(captor.getValue().getType(), is(NotificationType.JOIN_REQUEST_REJECTED));
  }

  // --- lecturas ---

  @Test
  public void U06_listCountYUnreadCountDeleganAlRepositorio() {
    when(this.repositoryMock.findByRecipientId(RECIPIENT_ID, 2, 10)).thenReturn(List.of());
    when(this.repositoryMock.countByRecipientId(RECIPIENT_ID)).thenReturn(25L);
    when(this.repositoryMock.countUnreadByRecipientId(RECIPIENT_ID)).thenReturn(7L);

    assertThat(this.notificationService.list(RECIPIENT_ID, 2, 10), is(List.of()));
    assertThat(this.notificationService.count(RECIPIENT_ID), is(25L));
    assertThat(this.notificationService.unreadCount(RECIPIENT_ID), is(7L));
    verify(this.repositoryMock, times(1)).findByRecipientId(RECIPIENT_ID, 2, 10);
  }

  @Test
  public void U07_markRead_encuentraLaFilaDelDestinatarioYLaMarca() {
    // given: la fila existe y es del destinatario
    Notification found = new Notification(
      user(RECIPIENT_ID, "grace@test.com"),
      user(ACTOR_ID, "ana@test.com"),
      NotificationType.NEW_FOLLOWER
    );
    when(this.repositoryMock.findByIdAndRecipientId(NOTIFICATION_ID, RECIPIENT_ID))
      .thenReturn(Optional.of(found));

    // when
    this.notificationService.markRead(RECIPIENT_ID, NOTIFICATION_ID);

    // then (AC-06)
    assertThat(found.isRead(), is(true));
    verify(this.repositoryMock, times(1)).save(found);
  }

  @Test
  public void U08_markRead_idAjenoOIinexistenteEsNoOpSilencioso() {
    // given: la busqueda cruzada id+destinatario no encuentra nada (AC-09)
    when(this.repositoryMock.findByIdAndRecipientId(any(UUID.class), any(UUID.class)))
      .thenReturn(Optional.empty());

    this.notificationService.markRead(RECIPIENT_ID, NOTIFICATION_ID);

    verify(this.repositoryMock, never()).save(any(Notification.class));
  }

  @Test
  public void U09_markAllReadDelegaEnUnaSolaEscritura() {
    this.notificationService.markAllRead(RECIPIENT_ID);

    verify(this.repositoryMock, times(1)).markAllRead(RECIPIENT_ID);
    verify(this.repositoryMock, never()).findByRecipientId(any(UUID.class), anyInt(), anyInt());
  }

  @Test
  public void U14_delete_tiraLaFilaPropiaYNiRopaAjenaNiInexistente() {
    // given: una fila del destinatario y una busqueda que no encuentra nada para el resto
    Notification found = new Notification(
      user(RECIPIENT_ID, "grace@test.com"),
      user(ACTOR_ID, "ana@test.com"),
      NotificationType.NEW_FOLLOWER
    );
    when(this.repositoryMock.findByIdAndRecipientId(NOTIFICATION_ID, RECIPIENT_ID))
      .thenReturn(Optional.of(found));

    this.notificationService.delete(RECIPIENT_ID, NOTIFICATION_ID);

    // then (AC-18)
    verify(this.repositoryMock, times(1)).delete(found);

    // when: id ajeno o ya borrado
    when(this.repositoryMock.findByIdAndRecipientId(any(UUID.class), any(UUID.class)))
      .thenReturn(Optional.empty());
    this.notificationService.delete(RECIPIENT_ID, UUID.randomUUID());

    // then: el mismo no-op silencioso de markRead (AC-09)
    verify(this.repositoryMock, times(1)).delete(any(Notification.class));
  }

  private static User user(UUID id, String email) {
    User user = new User();
    user.setId(id);
    user.setEmail(email);
    return user;
  }
}
