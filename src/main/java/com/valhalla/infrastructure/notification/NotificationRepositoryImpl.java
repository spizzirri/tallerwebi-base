package com.valhalla.infrastructure.notification;

import com.valhalla.domain.notification.Notification;
import com.valhalla.domain.notification.NotificationRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

/** [NOTIF] Adaptador del puerto de dominio sobre Spring Data. */
@Repository
public class NotificationRepositoryImpl implements NotificationRepository {

  private final JpaNotificationRepository jpaNotificationRepository;

  @Autowired
  public NotificationRepositoryImpl(JpaNotificationRepository jpaNotificationRepository) {
    this.jpaNotificationRepository = jpaNotificationRepository;
  }

  @Override
  public void save(Notification notification) {
    jpaNotificationRepository.save(notification);
  }

  @Override
  public void saveAll(List<Notification> notifications) {
    jpaNotificationRepository.saveAll(notifications);
  }

  @Override
  public List<Notification> findByRecipientId(UUID recipientId, int page, int size) {
    return jpaNotificationRepository.findRecent(recipientId, PageRequest.of(page, size));
  }

  @Override
  public long countByRecipientId(UUID recipientId) {
    return jpaNotificationRepository.countByRecipientId(recipientId);
  }

  @Override
  public long countUnreadByRecipientId(UUID recipientId) {
    return jpaNotificationRepository.countByRecipientIdAndReadIsFalse(recipientId);
  }

  @Override
  public Optional<Notification> findByIdAndRecipientId(UUID id, UUID recipientId) {
    return jpaNotificationRepository.findByIdAndRecipientId(id, recipientId);
  }

  @Override
  public void markAllRead(UUID recipientId) {
    jpaNotificationRepository.markAllRead(recipientId);
  }

  @Override
  public void delete(Notification notification) {
    jpaNotificationRepository.delete(notification);
  }
}
