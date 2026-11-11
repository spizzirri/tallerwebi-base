package com.valhalla.infrastructure.notification;

import com.valhalla.domain.follow.FollowRepository;
import com.valhalla.domain.notification.Notification;
import com.valhalla.domain.notification.NotificationRepository;
import com.valhalla.domain.notification.NotificationService;
import com.valhalla.domain.notification.NotificationType;
import com.valhalla.domain.user.User;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [NOTIF] Alta de eventos y lecturas del centro.
 *
 * <p>Las escrituras comparten la regla de fondo: una notificacion nunca es motivo de fallo
 * de la accion que la dispara, y no se notifica a nadie sobre si mismo. {@code notifyNewPlan} es
 * la unica que resuelve destinatarias ella sola (los seguidores del autor via
 * {@link FollowRepository}): quien publica no tiene la lista a mano.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

  /** [NOTIF] Ventana anti-spam para no repetir "empezó a seguir" del mismo actor. */
  private static final Duration FOLLOWER_NOTIFICATION_WINDOW = Duration.ofHours(24);

  private final NotificationRepository notificationRepository;
  private final FollowRepository followRepository;

  @Autowired
  public NotificationServiceImpl(
    NotificationRepository notificationRepository,
    FollowRepository followRepository
  ) {
    this.notificationRepository = notificationRepository;
    this.followRepository = followRepository;
  }

  @Override
  @Transactional
  public void notifyNewFollower(User follower, User followed) {
    Instant since = Instant.now().minus(FOLLOWER_NOTIFICATION_WINDOW);
    boolean alreadyNotified =
      notificationRepository.existsByRecipientIdAndActorIdAndTypeAndCreatedAtAfter(
        followed.getId(),
        follower.getId(),
        NotificationType.NEW_FOLLOWER,
        since
      );
    if (alreadyNotified) {
      return;
    }
    notificationRepository.save(
      new Notification(followed, follower, NotificationType.NEW_FOLLOWER)
    );
  }

  @Override
  @Transactional
  public void notifyNewParticipant(User participant, User owner, UUID planId, String planName) {
    if (owner == null || Objects.equals(owner.getId(), participant.getId())) {
      return;
    }
    notificationRepository.save(
      new Notification(owner, participant, NotificationType.NEW_PARTICIPANT, planId, planName)
    );
  }

  @Override
  @Transactional
  public void notifyNewPlan(User owner, UUID planId, String planName) {
    List<User> followers = followRepository.findFollowers(owner.getId());
    if (followers.isEmpty()) {
      return;
    }
    notificationRepository.saveAll(
      followers
        .stream()
        .map(follower ->
          new Notification(follower, owner, NotificationType.NEW_PLAN, planId, planName)
        )
        .toList()
    );
  }

  @Override
  @Transactional
  public void notifyInvitation(User inviter, User recipient, UUID planId, String planName) {
    notificationRepository.save(
      new Notification(recipient, inviter, NotificationType.PLAN_INVITATION, planId, planName)
    );
  }

  @Override
  @Transactional
  public void notifyJoinRequest(User requester, User owner, UUID planId, String planName) {
    notificationRepository.save(
      new Notification(owner, requester, NotificationType.JOIN_REQUESTED, planId, planName)
    );
  }

  @Override
  @Transactional
  public void notifyJoinRequestResponse(
    User owner,
    User requester,
    UUID planId,
    String planName,
    boolean accepted
  ) {
    notificationRepository.save(
      new Notification(
        requester,
        owner,
        accepted ? NotificationType.JOIN_REQUEST_ACCEPTED : NotificationType.JOIN_REQUEST_REJECTED,
        planId,
        planName
      )
    );
  }

  @Override
  @Transactional
  public void notifyParticipantRemoved(User actor, User participant, String planName) {
    if (!sameUser(actor, participant)) {
      notificationRepository.save(
        new Notification(participant, actor, NotificationType.PARTICIPANT_REMOVED, null, planName)
      );
    }
  }

  @Override
  @Transactional
  public void notifyParticipantBanned(User actor, User participant, String planName) {
    if (!sameUser(actor, participant)) {
      notificationRepository.save(
        new Notification(participant, actor, NotificationType.PARTICIPANT_BANNED, null, planName)
      );
    }
  }

  @Override
  @Transactional
  public void notifyEditorRoleChanged(
    User owner,
    User participant,
    UUID planId,
    String planName,
    boolean editor
  ) {
    if (sameUser(owner, participant)) {
      return;
    }
    NotificationType type = editor
      ? NotificationType.EDITOR_ROLE_GRANTED
      : NotificationType.EDITOR_ROLE_REVOKED;
    notificationRepository.save(new Notification(participant, owner, type, planId, planName));
  }

  private boolean sameUser(User first, User second) {
    return first != null && second != null && Objects.equals(first.getId(), second.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public List<Notification> list(UUID recipientId, int page, int size) {
    return notificationRepository.findByRecipientId(recipientId, page, size);
  }

  @Override
  @Transactional(readOnly = true)
  public long count(UUID recipientId) {
    return notificationRepository.countByRecipientId(recipientId);
  }

  @Override
  @Transactional(readOnly = true)
  public long unreadCount(UUID recipientId) {
    return notificationRepository.countUnreadByRecipientId(recipientId);
  }

  @Override
  @Transactional
  public void markRead(UUID recipientId, UUID notificationId) {
    notificationRepository
      .findByIdAndRecipientId(notificationId, recipientId)
      .ifPresent(found -> {
        found.markAsRead();
        notificationRepository.save(found);
      });
  }

  @Override
  @Transactional
  public void markAllRead(UUID recipientId) {
    notificationRepository.markAllRead(recipientId);
  }

  @Override
  @Transactional
  public void deleteRead(UUID recipientId) {
    notificationRepository.deleteRead(recipientId);
  }

  @Override
  @Transactional
  public void delete(UUID recipientId, UUID notificationId) {
    notificationRepository
      .findByIdAndRecipientId(notificationId, recipientId)
      .ifPresent(notificationRepository::delete);
  }
}
