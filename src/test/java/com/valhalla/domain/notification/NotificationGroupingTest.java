package com.valhalla.domain.notification;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.valhalla.domain.user.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * [NGRP] T-NGRP-001/002: reglas de agrupacion por tipo, plan y ventana de 24 h; los tipos que no
 * se repiten quedan sueltos y las filas leidas siguen dentro de su grupo.
 */
public class NotificationGroupingTest {

  private static final UUID PLAN_A = UUID.fromString("0192f0a1-0000-7000-8000-00000000002a");
  private static final UUID PLAN_B = UUID.fromString("0192f0a1-0000-7000-8000-00000000002b");
  private static final Instant NOW = Instant.parse("2026-01-10T12:00:00Z");

  @Test
  public void G01_listaVaciaNoTieneGrupos() {
    assertThat(NotificationGrouping.group(List.of()), is(List.of()));
  }

  @Test
  public void G02_mismoTipoPlanYVentanaSeAgrupanEnLaPosicionDeLaPrimeraFila() {
    Notification first = notification(NotificationType.NEW_FOLLOWER, null, NOW, "Ana", "A");
    Notification second = notification(
      NotificationType.NEW_FOLLOWER,
      null,
      NOW.minusSeconds(3600),
      "Luis",
      "B"
    );
    Notification third = notification(
      NotificationType.NEW_FOLLOWER,
      null,
      NOW.minusSeconds(7200),
      "Marta",
      "C"
    );

    List<NotificationGroup> groups = NotificationGrouping.group(List.of(first, second, third));

    assertThat(groups, hasSize(1));
    NotificationGroup group = groups.getFirst();
    assertThat(group.isGrouped(), is(true));
    assertThat(group.getType(), is(NotificationType.NEW_FOLLOWER));
    assertThat(group.getRelatedPlanId(), is(nullValue()));
    assertThat(group.getNotifications(), contains(first, second, third));
    assertThat(group.getNewestAt(), is(NOW));
    assertThat(group.getUnreadCount(), is(3));
    assertThat(group.getActorSummary(), is("Ana y 2 más"));
  }

  @Test
  public void G03_distintoTipoODistintoPlanNoSeMezclan() {
    List<Notification> rows = List.of(
      notification(NotificationType.NEW_FOLLOWER, null, NOW, "Ana", "A"),
      notification(NotificationType.NEW_FOLLOWER, PLAN_A, NOW, "Luis", "B"),
      notification(NotificationType.NEW_PARTICIPANT, PLAN_A, NOW, "Marta", "C")
    );

    assertThat(NotificationGrouping.group(rows), hasSize(3));
  }

  @Test
  public void G04_laVentanaEsDe24HorasYSeReiniciaTrasUnHueco() {
    Notification newest = notification(NotificationType.NEW_FOLLOWER, PLAN_A, NOW, "Ana", "A");
    Notification exact = notification(
      NotificationType.NEW_FOLLOWER,
      PLAN_A,
      NOW.minusSeconds(24 * 3600),
      "Luis",
      "B"
    );
    Notification tooOld = notification(
      NotificationType.NEW_FOLLOWER,
      PLAN_A,
      NOW.minusSeconds(24 * 3600 + 1),
      "Marta",
      "C"
    );
    Notification nextWindow = notification(
      NotificationType.NEW_FOLLOWER,
      PLAN_A,
      NOW.minusSeconds(25 * 3600),
      "Nora",
      "D"
    );

    List<NotificationGroup> groups = NotificationGrouping.group(
      List.of(newest, exact, tooOld, nextWindow)
    );

    assertThat(groups, hasSize(2));
    assertThat(groups.get(0).getNotifications(), contains(newest, exact));
    assertThat(groups.get(1).getNotifications(), contains(tooOld, nextWindow));
  }

  @Test
  public void G05_sinTimestampNoSeAgrupaAunqueCoincidaLaClave() {
    Notification withoutDate = notification(NotificationType.NEW_FOLLOWER, null, null, "Ana", "A");
    Notification withDate = notification(NotificationType.NEW_FOLLOWER, null, NOW, "Luis", "B");

    List<NotificationGroup> groups = NotificationGrouping.group(List.of(withoutDate, withDate));

    assertThat(groups, hasSize(2));
    assertThat(groups.get(0).getNewestAt(), is(nullValue()));
    assertThat(groups.get(1).getNewestAt(), is(NOW));
  }

  @Test
  public void G06_lasFilasLeidasSiguenEnElGrupoPeroNoCuentanParaElBadge() {
    Notification read = notification(NotificationType.NEW_FOLLOWER, null, NOW, "Ana", "A");
    read.markAsRead();
    Notification otherRead = notification(
      NotificationType.NEW_FOLLOWER,
      null,
      NOW.minusSeconds(60),
      "Luis",
      "B"
    );
    otherRead.markAsRead();
    Notification unread = notification(
      NotificationType.NEW_FOLLOWER,
      null,
      NOW.minusSeconds(120),
      "Marta",
      "C"
    );

    List<NotificationGroup> groups = NotificationGrouping.group(List.of(read, otherRead, unread));

    assertThat(groups, hasSize(1));
    assertThat(groups.getFirst().isGrouped(), is(true));
    assertThat(groups.getFirst().getNotifications(), contains(read, otherRead, unread));
    assertThat(groups.getFirst().getUnreadCount(), is(1));
  }

  @Test
  public void G07_soloLosTiposRepetiblesSeAgrupan() {
    List<Notification> rows = new ArrayList<>();
    for (NotificationType type : NotificationType.values()) {
      rows.add(notification(type, PLAN_A, NOW, type.name(), "Actor"));
    }

    List<NotificationGroup> groups = NotificationGrouping.group(rows);

    assertThat(groups, hasSize(NotificationType.values().length));
    assertThat(groups.stream().filter(NotificationGroup::isGrouped).toList(), is(List.of()));
  }

  @Test
  public void G08_elResumenDeActoresOrdenaYResumeLosNombres() {
    assertThat(summary("Ana", "A"), is("Ana"));
    assertThat(summary("Luis", "B", "Ana", "A"), is("Ana y Luis"));
    assertThat(summary("Marta", "C", "Luis", "B", "Ana", "A"), is("Ana y 2 más"));
    assertThat(summary("Zoe", "D", "Marta", "C", "Luis", "B", "Ana", "A"), is("Ana, Luis y 2 más"));
    assertThat(
      summary("Zoe", "D", "Marta", "C", "Luis", "B", "Ana", "A", "Nora", "E"),
      is("Ana, Luis y 3 más")
    );
  }

  @Test
  public void G09_nombresVaciosOCorruptosTienenRespaldo() {
    Notification noActor = notification(NotificationType.NEW_FOLLOWER, null, NOW, "Ana", "A");
    ReflectionTestUtils.setField(noActor, "actor", null);
    Notification blankFirst = notification(NotificationType.NEW_FOLLOWER, null, NOW, " ", "Pérez");
    Notification blankAll = notification(NotificationType.NEW_FOLLOWER, null, NOW, " ", " ");
    Notification nullNames = notification(NotificationType.NEW_FOLLOWER, null, NOW, null, null);

    assertThat(
      NotificationGrouping.group(List.of(noActor)).getFirst().getActorSummary(),
      is("Alguien")
    );
    assertThat(
      NotificationGrouping.group(List.of(blankFirst)).getFirst().getActorSummary(),
      is("Pérez")
    );
    assertThat(
      NotificationGrouping.group(List.of(blankAll)).getFirst().getActorSummary(),
      is("Alguien")
    );
    assertThat(
      NotificationGrouping.group(List.of(nullNames)).getFirst().getActorSummary(),
      is("Alguien")
    );
  }

  @Test
  public void G10_unGrupoNecesitaAlMenosUnaFila() {
    assertThrows(IllegalArgumentException.class, () -> NotificationGroup.of(List.of()));
  }

  @Test
  public void G11_losGettersDelGrupoExponenLaFila() {
    Notification notification = notification(NotificationType.NEW_PLAN, PLAN_B, NOW, "Ana", "A");
    NotificationGroup group = NotificationGroup.of(List.of(notification));

    assertThat(group.getType(), is(NotificationType.NEW_PLAN));
    assertThat(group.getRelatedPlanId(), is(PLAN_B));
    assertThat(group.getRelatedPlanName(), is("Plan de prueba"));
    assertThat(group.getNotifications(), contains(notification));
    assertThat(group.getNewestAt(), is(NOW));
    assertThat(group.isGrouped(), is(false));
    assertThat(group.getUnreadCount(), is(1));
  }

  @Test
  public void G12_acceptsComparaTipoPlanYVentana() {
    Notification base = notification(NotificationType.NEW_FOLLOWER, PLAN_A, NOW, "Ana", "A");
    NotificationGroup group = NotificationGroup.of(List.of(base));

    assertThat(
      group.accepts(
        notification(NotificationType.NEW_FOLLOWER, PLAN_A, NOW.minusSeconds(1), "L", "B")
      ),
      is(true)
    );
    assertThat(
      group.accepts(notification(NotificationType.NEW_PLAN, PLAN_A, NOW.minusSeconds(1), "L", "B")),
      is(false)
    );
    assertThat(
      group.accepts(
        notification(NotificationType.NEW_FOLLOWER, PLAN_B, NOW.minusSeconds(1), "L", "B")
      ),
      is(false)
    );
    assertThat(
      group.accepts(notification(NotificationType.NEW_FOLLOWER, PLAN_A, null, "L", "B")),
      is(false)
    );
  }

  private static String summary(String... names) {
    List<Notification> rows = new ArrayList<>();
    for (int i = 0; i < names.length; i += 2) {
      rows.add(
        notification(
          NotificationType.NEW_FOLLOWER,
          null,
          NOW.minusSeconds(i),
          names[i],
          names[i + 1]
        )
      );
    }
    return NotificationGrouping.group(rows).getFirst().getActorSummary();
  }

  private static Notification notification(
    NotificationType type,
    UUID planId,
    Instant createdAt,
    String firstName,
    String lastName
  ) {
    User recipient = user("recipient@test.com", "Grace", "Hopper");
    User actor = user("actor@test.com", firstName, lastName);
    Notification notification = new Notification(
      recipient,
      actor,
      type,
      planId,
      planId == null ? null : "Plan de prueba"
    );
    ReflectionTestUtils.setField(notification, "createdAt", createdAt);
    return notification;
  }

  private static User user(String email, String firstName, String lastName) {
    User user = new User();
    user.setId(UUID.randomUUID());
    user.setEmail(email);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    return user;
  }
}
