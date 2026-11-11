package com.valhalla.domain.notification;

/**
 * [NOTIF] Tipos de evento que el centro de notificaciones sabe contar. Cada tipo define la frase
 * que ve el usuario y el destino del link, no una lógica propia: las reglas viven en
 * {@link NotificationService}.
 */
public enum NotificationType {
  /** Un usuario empezó a seguir al destinatario. */
  NEW_FOLLOWER,

  /** Un usuario se sumó como participante a un plan cuyo dueño es el destinatario. */
  NEW_PARTICIPANT,

  /** Un usuario seguido publicó un plan nuevo. */
  NEW_PLAN,

  /** Un usuario invitó al destinatario a un plan (decisión de producto, ver 25-NOTIF). */
  PLAN_INVITATION,

  /** Un usuario pidió unirse al plan cuyo dueño es el destinatario. */
  JOIN_REQUESTED,

  /** El dueño aprobó la solicitud de unión del destinatario. */
  JOIN_REQUEST_ACCEPTED,

  /** El dueño rechazó la solicitud de unión del destinatario. */
  JOIN_REQUEST_REJECTED,

  /** The recipient was removed from a Plan without being banned. */
  PARTICIPANT_REMOVED,

  /** The recipient was removed and banned from a Plan. */
  PARTICIPANT_BANNED,

  /** The recipient was granted the Editor role. */
  EDITOR_ROLE_GRANTED,

  /** The recipient's Editor role was revoked. */
  EDITOR_ROLE_REVOKED;

  /**
   * [NGRP-005] Solo los eventos que se repiten sin pedir una decision distinta se agrupan.
   * Invitaciones, solicitudes y avisos de acceso conservan una fila por evento.
   */
  public boolean isRepeatable() {
    return this == NEW_FOLLOWER || this == NEW_PARTICIPANT || this == NEW_PLAN;
  }
}
