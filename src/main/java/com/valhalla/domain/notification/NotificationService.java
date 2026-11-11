package com.valhalla.domain.notification;

import com.valhalla.domain.user.User;
import java.util.List;
import java.util.UUID;

/**
 * [NOTIF] Escritura y lectura del centro de notificaciones in-app.
 *
 * <p>La creacion es un side-effect del evento que la genera (seguir, sumarse a un plan, publicar):
 * el que dispara ya tiene los usuarios resueltos y los pasa directo, sin requeryar. Nunca falla la
 * accion original: si el evento no corresponde a una notificacion (uno se sigue a si mismo no
 * aplica, es un no-op del follow), no se escribe nada.
 */
public interface NotificationService {
  /** [NOTIF] "X te empezó a seguir": el destinatario es el seguido. */
  void notifyNewFollower(User follower, User followed);

  /** "X se unió a tu plan": el destinatario es el dueño; sin dueño no hay a quién avisar. */
  void notifyNewParticipant(User participant, User owner, UUID planId, String planName);

  /** "X publicó un plan nuevo": una fila por cada seguidor del autor. */
  void notifyNewPlan(User owner, UUID planId, String planName);

  /**
   * "X te invitó al plan": el destinatario es el invitado. A diferencia del participante, el
   * invitado todavia no es nada del plan: la notificacion es el aviso de entrada.
   */
  void notifyInvitation(User inviter, User recipient, UUID planId, String planName);

  /** "X solicitó unirse a tu plan": el destinatario es el dueño, que tiene que responder. */
  void notifyJoinRequest(User requester, User owner, UUID planId, String planName);

  /**
   * "X aprobó/rechazó tu solicitud": el destinatario es el solicitante. La respuesta se notifica
   * en los dos sentidos: mientras la solicitud estaba abierta, el pendiente era del solicitante.
   */
  void notifyJoinRequestResponse(
    User owner,
    User requester,
    UUID planId,
    String planName,
    boolean accepted
  );

  /** Removal and ban notices retain the plan name without a linkable plan ID. */
  void notifyParticipantRemoved(User actor, User participant, String planName);

  /** Ban notice with the same no-plan-link privacy behavior as participant removal. */
  void notifyParticipantBanned(User actor, User participant, String planName);

  /** The participant keeps access to this plan after an editor-role change. */
  void notifyEditorRoleChanged(
    User owner,
    User participant,
    UUID planId,
    String planName,
    boolean editor
  );

  List<Notification> list(UUID recipientId, int page, int size);

  long count(UUID recipientId);

  long unreadCount(UUID recipientId);

  /** Marcar una leida: id inexistente o de otro destinatario es un no-op silencioso (AC-09). */
  void markRead(UUID recipientId, UUID notificationId);

  void markAllRead(UUID recipientId);

  /** Borrar todas las filas ya leidas del destinatario, sin tocar las pendientes. */
  void deleteRead(UUID recipientId);

  /** Borrar una fila propia (AC-18). Ajena o inexistente: el mismo no-op silencioso de arriba. */
  void delete(UUID recipientId, UUID notificationId);
}
