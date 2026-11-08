package com.valhalla.domain.notification;

import com.valhalla.domain.plan.UuidVersion7Generator;
import com.valhalla.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

/**
 * [NOTIF] Una actividad relevante para un usuario, persistida en el momento en que ocurre.
 *
 * <p>Tabla propia, mismo patron que {@code Follow}: {@link User} no gana la coleccion inversa y
 * el listado se lee por consultas paginadas sobre esta tabla (AC-07). El nombre del plan se
 * congela al crear la notificacion: si el plan se renombra o se borra despues, la fila conserva
 * el texto con el que nacio. Solo los eventos que mantienen acceso guardan el ID para un link;
 * las expulsiones y baneos guardan el nombre sin un destino navegable (AC-12, AC-19, AC-20).
 */
@Entity
@Table(name = "notifications")
public class Notification {

  @Id
  @UuidGenerator(algorithm = UuidVersion7Generator.class)
  private UUID id;

  /** Quien la ve: el destinatario. */
  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "recipient_id", nullable = false)
  private User recipient;

  /** Quien provoco el evento. */
  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "actor_id", nullable = false)
  private User actor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  /** Plan relacionado. Removal and ban notices intentionally keep no plan ID or navigable link. */
  @Column(name = "related_plan_id")
  private UUID relatedPlanId;

  /** Nombre del plan congelado al crear la notificacion. */
  @Column(name = "related_plan_name")
  private String relatedPlanName;

  @Column(name = "read_flag", nullable = false)
  private boolean read;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false, nullable = false)
  private Instant createdAt;

  public Notification() {}

  public Notification(User recipient, User actor, NotificationType type) {
    this(recipient, actor, type, null, null);
  }

  public Notification(
    User recipient,
    User actor,
    NotificationType type,
    UUID relatedPlanId,
    String relatedPlanName
  ) {
    this.recipient = recipient;
    this.actor = actor;
    this.type = type;
    this.relatedPlanId = relatedPlanId;
    this.relatedPlanName = relatedPlanName;
  }

  /** [NOTIF] Marcar como leida es idempotente: marcar dos veces no cambia nada (AC-06). */
  public void markAsRead() {
    this.read = true;
  }

  public UUID getId() {
    return id;
  }

  public User getRecipient() {
    return recipient;
  }

  public User getActor() {
    return actor;
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

  public boolean isRead() {
    return read;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
