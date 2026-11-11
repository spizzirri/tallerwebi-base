package com.valhalla.domain.notification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * [NOTIF] Acceso a la tabla {@code notifications}. La pagina se resuelve por offset/limit en la
 * consulta, nunca en memoria: el listado puede crecer y la vista siempre pide una ventana (AC-07).
 */
public interface NotificationRepository {
  void save(Notification notification);

  void saveAll(List<Notification> notifications);

  /** Pagina mas nueva primero: la ventana la resuelve la consulta, no la memoria (AC-07). */
  List<Notification> findByRecipientId(UUID recipientId, int page, int size);

  long countByRecipientId(UUID recipientId);

  long countUnreadByRecipientId(UUID recipientId);

  /** La busqueda cruza el id con el destinatario: nadie lee ni marca filas ajenas (AC-09). */
  Optional<Notification> findByIdAndRecipientId(UUID id, UUID recipientId);

  /** [NOTIF] Anti-spam: ya existe una notificacion del mismo actor y tipo en la ventana. */
  boolean existsByRecipientIdAndActorIdAndTypeAndCreatedAtAfter(
    UUID recipientId,
    UUID actorId,
    NotificationType type,
    Instant since
  );

  /** Marca todas las leidas pendientes del destinatario en una sola escritura. */
  void markAllRead(UUID recipientId);

  /** Borra en bloque solo las filas ya leidas del destinatario. */
  void deleteRead(UUID recipientId);

  /** Tirar una fila: el borrado es fisico, la bandeja no es un archivo historico (AC-18). */
  void delete(Notification notification);
}
