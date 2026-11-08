package com.valhalla.infrastructure.notification;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.notification.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/** [NOTIF] Unit tests del adaptador sobre Spring Data: cada metodo delega una sola vez. */
public class NotificationRepositoryImplTest {

  private static final UUID RECIPIENT_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000000a");
  private static final UUID NOTIFICATION_ID = UUID.fromString(
    "0192f0a1-0000-7000-8000-000000000077"
  );

  private NotificationRepositoryImpl repository;
  private JpaNotificationRepository jpaMock;

  @BeforeEach
  public void init() {
    this.jpaMock = mock(JpaNotificationRepository.class);
    this.repository = new NotificationRepositoryImpl(this.jpaMock);
  }

  @Test
  public void saveDelegatesOnce() {
    Notification notification = mock(Notification.class);

    this.repository.save(notification);

    verify(this.jpaMock, times(1)).save(notification);
  }

  @Test
  public void saveAllDelegatesTheSameBatch() {
    List<Notification> batch = List.of(mock(Notification.class));

    this.repository.saveAll(batch);

    verify(this.jpaMock, times(1)).saveAll(batch);
  }

  @Test
  public void findByRecipientIdTranslatesPageAndSizeIntoARequest() {
    Pageable expected = PageRequest.of(1, 10);
    when(this.jpaMock.findRecent(RECIPIENT_ID, expected)).thenReturn(List.of());

    assertThat(this.repository.findByRecipientId(RECIPIENT_ID, 1, 10), is(List.of()));
    verify(this.jpaMock, times(1)).findRecent(RECIPIENT_ID, expected);
  }

  @Test
  public void countsDelegateToTheirQueries() {
    when(this.jpaMock.countByRecipientId(RECIPIENT_ID)).thenReturn(25L);
    when(this.jpaMock.countByRecipientIdAndReadIsFalse(RECIPIENT_ID)).thenReturn(7L);

    assertThat(this.repository.countByRecipientId(RECIPIENT_ID), is(25L));
    assertThat(this.repository.countUnreadByRecipientId(RECIPIENT_ID), is(7L));
  }

  @Test
  public void findByIdAndRecipientIdDelegatesTheCrossCheck() {
    Notification found = mock(Notification.class);
    when(this.jpaMock.findByIdAndRecipientId(NOTIFICATION_ID, RECIPIENT_ID))
      .thenReturn(Optional.of(found));

    assertThat(
      this.repository.findByIdAndRecipientId(NOTIFICATION_ID, RECIPIENT_ID),
      is(Optional.of(found))
    );
  }

  @Test
  public void markAllReadDelegatesTheBulkUpdate() {
    this.repository.markAllRead(RECIPIENT_ID);

    verify(this.jpaMock, times(1)).markAllRead(RECIPIENT_ID);
  }

  @Test
  public void deleteDelegatesTheRemoval() {
    Notification found = mock(Notification.class);

    this.repository.delete(found);

    verify(this.jpaMock, times(1)).delete(found);
  }
}
