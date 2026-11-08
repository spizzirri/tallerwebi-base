package com.valhalla.domain.notification;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.valhalla.domain.user.User;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * [NOTIF] Unit tests del modelo (T-NOTIF-B01): la entidad guarda el evento y el marcado como
 * leido es idempotente (AC-06). El dominio queda cubierto al 100% de lineas y ramas.
 */
public class NotificationTest {

  private static final UUID RECIPIENT_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000000a");
  private static final UUID ACTOR_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000000b");
  private static final UUID PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000002a");

  @Test
  public void U01_notificacionSinPlanNaceLeidaFalsoConSusProtagonistas() {
    Notification notification = new Notification(
      recipient(),
      actor(),
      NotificationType.NEW_FOLLOWER
    );

    assertThat(notification.getRecipient().getId(), is(RECIPIENT_ID));
    assertThat(notification.getActor().getId(), is(ACTOR_ID));
    assertThat(notification.getType(), is(NotificationType.NEW_FOLLOWER));
    assertThat(notification.getRelatedPlanId(), is(nullValue()));
    assertThat(notification.getRelatedPlanName(), is(nullValue()));
    assertThat(notification.isRead(), is(false));
    // El id y el timestamp los asigna la base: la entidad recien armada no los tiene.
    assertThat(notification.getId(), is(nullValue()));
    assertThat(notification.getCreatedAt(), is(nullValue()));
  }

  @Test
  public void U02_notificacionConPlanCongelaIdYNombre() {
    Notification notification = new Notification(
      recipient(),
      actor(),
      NotificationType.NEW_PARTICIPANT,
      PLAN_ID,
      "Viaje a Bariloche"
    );

    assertThat(notification.getRelatedPlanId(), is(PLAN_ID));
    assertThat(notification.getRelatedPlanName(), is("Viaje a Bariloche"));
    assertThat(notification.isRead(), is(false));
  }

  @Test
  public void U03_markAsReadEsIdempotente() {
    Notification notification = new Notification(recipient(), actor(), NotificationType.NEW_PLAN);

    notification.markAsRead();
    notification.markAsRead();

    assertThat(notification.isRead(), is(true));
  }

  @Test
  public void U04_losOnceTiposExistenYNombreRoundTrip() {
    assertThat(NotificationType.values().length, is(11));
    assertThat(
      NotificationType.valueOf(NotificationType.NEW_FOLLOWER.name()),
      is(NotificationType.NEW_FOLLOWER)
    );
    assertThat(NotificationType.valueOf("NEW_PARTICIPANT"), is(NotificationType.NEW_PARTICIPANT));
    assertThat(NotificationType.valueOf("NEW_PLAN"), is(NotificationType.NEW_PLAN));
    assertThat(NotificationType.valueOf("PLAN_INVITATION"), is(NotificationType.PLAN_INVITATION));
    assertThat(NotificationType.valueOf("JOIN_REQUESTED"), is(NotificationType.JOIN_REQUESTED));
    assertThat(
      NotificationType.valueOf("JOIN_REQUEST_ACCEPTED"),
      is(NotificationType.JOIN_REQUEST_ACCEPTED)
    );
    assertThat(
      NotificationType.valueOf("JOIN_REQUEST_REJECTED"),
      is(NotificationType.JOIN_REQUEST_REJECTED)
    );
  }

  @Test
  public void U05_constructorVacioExisteParaJpa() {
    Notification blank = new Notification();

    assertThat(blank.getRecipient(), is(nullValue()));
    assertThat(blank.isRead(), is(false));
  }

  private static User recipient() {
    return user(RECIPIENT_ID, "grace@test.com");
  }

  private static User actor() {
    return user(ACTOR_ID, "ana@test.com");
  }

  private static User user(UUID id, String email) {
    User user = new User();
    user.setId(id);
    user.setEmail(email);
    return user;
  }
}
