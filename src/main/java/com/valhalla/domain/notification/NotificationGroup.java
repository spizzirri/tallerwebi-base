package com.valhalla.domain.notification;

import com.valhalla.domain.user.User;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * [NGRP] Una fila del centro de notificaciones: una notificacion sola o varias del mismo tipo,
 * plan y ventana de lectura.
 *
 * <p>El grupo no se persiste. Es una vista de lectura que arma
 * {@link NotificationGrouping} sobre las notificaciones que ya existen: el badge y la paginacion
 * siguen contando filas, no grupos (NGRP-004). El nombre visible de los actores se deriva de las
 * filas agrupadas, nunca de un texto guardado.
 */
public final class NotificationGroup {

  /** [NGRP-001] Dos eventos del mismo tipo y plan se leen juntos dentro de esta ventana. */
  static final Duration WINDOW = Duration.ofHours(24);

  private final NotificationType type;
  private final UUID relatedPlanId;
  private final String relatedPlanName;
  private final List<Notification> notifications;
  private final Instant newestAt;

  private NotificationGroup(List<Notification> notifications) {
    if (notifications.isEmpty()) {
      throw new IllegalArgumentException("Un grupo de notificaciones necesita al menos una fila");
    }
    this.notifications = List.copyOf(notifications);
    Notification first = this.notifications.getFirst();
    this.type = first.getType();
    this.relatedPlanId = first.getRelatedPlanId();
    this.relatedPlanName = first.getRelatedPlanName();
    this.newestAt =
      this.notifications.stream()
        .map(Notification::getCreatedAt)
        .filter(Objects::nonNull)
        .max(Comparator.naturalOrder())
        .orElse(null);
  }

  /** [NGRP] Una fila individual tambien es un grupo de uno: la vista tiene un solo tipo de item. */
  static NotificationGroup of(List<Notification> notifications) {
    return new NotificationGroup(notifications);
  }

  /** Agrega una fila que ya paso {@link #accepts(Notification)} y recalcula la ventana. */
  NotificationGroup plus(Notification notification) {
    List<Notification> merged = new ArrayList<>(this.notifications);
    merged.add(notification);
    return new NotificationGroup(merged);
  }

  /**
   * [NGRP-001] Misma clave de agrupacion y dentro de la ventana de 24 h. Sin timestamp no se
   * agrupa: preferimos una fila de mas antes que mezclar eventos que no se pueden fechar.
   */
  boolean accepts(Notification notification) {
    return (
      this.type == notification.getType() &&
      Objects.equals(this.relatedPlanId, notification.getRelatedPlanId()) &&
      withinWindow(notification.getCreatedAt())
    );
  }

  private boolean withinWindow(Instant createdAt) {
    if (this.newestAt == null || createdAt == null) {
      return false;
    }
    return Duration.between(createdAt, this.newestAt).abs().compareTo(WINDOW) <= 0;
  }

  /**
   * [NGRP-002] "Ana", "Ana y Luis", "Ana y 2 más" o "Ana, Luis y 3 más". Se listan como maximo
   * dos nombres y el resto se resume; el orden es alfabetico para que el texto no cambie segun
   * cual fue la ultima notificacion.
   */
  public String getActorSummary() {
    List<String> names =
      this.notifications.stream()
        .map(Notification::getActor)
        .map(NotificationGroup::displayName)
        .sorted(String.CASE_INSENSITIVE_ORDER)
        .toList();
    return switch (names.size()) {
      case 1 -> names.getFirst();
      case 2 -> names.get(0) + " y " + names.get(1);
      case 3 -> names.getFirst() + " y 2 más";
      default -> names.get(0) + ", " + names.get(1) + " y " + (names.size() - 2) + " más";
    };
  }

  /** Primer nombre del actor, con apellido o "Alguien" como respaldo. */
  private static String displayName(User actor) {
    if (actor == null) {
      return "Alguien";
    }
    String firstName = trimToNull(actor.getFirstName());
    if (firstName != null) {
      return firstName;
    }
    String lastName = trimToNull(actor.getLastName());
    return lastName == null ? "Alguien" : lastName;
  }

  private static String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  public NotificationType getType() {
    return type;
  }

  public UUID getRelatedPlanId() {
    return relatedPlanId;
  }

  public String getRelatedPlanName() {
    return relatedPlanName;
  }

  public List<Notification> getNotifications() {
    return notifications;
  }

  public Instant getNewestAt() {
    return newestAt;
  }

  public boolean isGrouped() {
    return this.notifications.size() > 1;
  }

  /** [NGRP-004] Filas pendientes del grupo: el badge global usa el mismo conteo por fila. */
  public int getUnreadCount() {
    return (int) this.notifications.stream().filter(notification -> !notification.isRead()).count();
  }
}
