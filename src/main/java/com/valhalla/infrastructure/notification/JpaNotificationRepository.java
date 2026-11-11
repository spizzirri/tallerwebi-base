package com.valhalla.infrastructure.notification;

import com.valhalla.domain.notification.Notification;
import com.valhalla.domain.notification.NotificationType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * [NOTIF] Consultas de la tabla {@code notifications}. La paginacion y los conteos salen de la
 * base (AC-07); la ventana mas nueva primero la define {@code created_at}, que coincide con el
 * orden de UUIDv7 pero no depende de el.
 */
public interface JpaNotificationRepository extends JpaRepository<Notification, UUID> {
  @Query(
    "select n from Notification n where n.recipient.id = :recipientId " +
    "order by n.createdAt desc, n.id desc"
  )
  List<Notification> findRecent(@Param("recipientId") UUID recipientId, Pageable pageAndSize);

  long countByRecipientId(UUID recipientId);

  long countByRecipientIdAndReadIsFalse(UUID recipientId);

  Optional<Notification> findByIdAndRecipientId(UUID id, UUID recipientId);

  /** [NOTIF] Evita duplicados si el mismo actor ya notifico en la ventana. */
  boolean existsByRecipientIdAndActorIdAndTypeAndCreatedAtAfter(
    UUID recipientId,
    UUID actorId,
    NotificationType type,
    Instant since
  );

  /** Una sola escritura para "marcar todas como leidas": sin SELECT + N UPDATEs. */
  @Modifying
  @Query(
    "update Notification n set n.read = true where n.recipient.id = :recipientId and n.read = false"
  )
  void markAllRead(@Param("recipientId") UUID recipientId);

  /** Borrado masivo de leidas: no toca las pendientes ni las filas de otros destinatarios. */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("delete from Notification n where n.recipient.id = :recipientId and n.read = true")
  void deleteRead(@Param("recipientId") UUID recipientId);
}
