package com.valhalla.presentation.notification;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.notification.Notification;
import com.valhalla.domain.notification.NotificationService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.ModelAndView;

/**
 * [NOTIF] Unit tests de {@link NotificationController} (T-NOTIF-B02): paginacion, clamp de la
 * pagina pedida, marcado individual que conserva la pagina y marcado global. La identidad siempre
 * sale de la sesion, nunca de un parametro (AC-09).
 */
public class NotificationControllerTest {

  private static final UUID ME_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000000a");
  private static final String ME_EMAIL = "grace@test.com";
  private static final UUID NOTIFICATION_ID = UUID.fromString(
    "0192f0a1-0000-7000-8000-000000000077"
  );

  private NotificationController controller;
  private NotificationService notificationServiceMock;
  private UserService userServiceMock;
  private Authentication me;

  @BeforeEach
  public void init() {
    this.notificationServiceMock = mock(NotificationService.class);
    this.userServiceMock = mock(UserService.class);
    this.controller =
      new NotificationController(this.notificationServiceMock, this.userServiceMock);
    User user = new User();
    user.setId(ME_ID);
    user.setEmail(ME_EMAIL);
    when(this.userServiceMock.getByEmail(ME_EMAIL)).thenReturn(user);
    this.me = new UsernamePasswordAuthenticationToken(ME_EMAIL, null);
  }

  // --- listado ---

  @Test
  public void U01_list_primeraPaginaTieneSiguienteSinPrevio() {
    // given: 25 notificaciones, ventana de 10
    List<Notification> page = List.of(mock(Notification.class));
    when(this.notificationServiceMock.count(ME_ID)).thenReturn(25L);
    when(this.notificationServiceMock.list(ME_ID, 0, NotificationController.PAGE_SIZE))
      .thenReturn(page);

    ModelAndView view = this.controller.list(0, this.me);

    assertThat(view.getViewName(), is("pages/notifications/list"));
    assertThat(view.getModel().get("notifications"), is(page));
    assertThat(view.getModel().get("page"), is(0));
    assertThat(view.getModel().get("hasPrev"), is(false));
    assertThat(view.getModel().get("hasNext"), is(true));
    verify(this.notificationServiceMock, times(1)).list(ME_ID, 0, NotificationController.PAGE_SIZE);
  }

  @Test
  public void U02_list_paginaNegativaSeAclampaACero() {
    when(this.notificationServiceMock.count(ME_ID)).thenReturn(5L);

    this.controller.list(-4, this.me);

    verify(this.notificationServiceMock, times(1)).list(ME_ID, 0, NotificationController.PAGE_SIZE);
  }

  @Test
  public void U03_list_paginaFueraDeRangoCaeALaUltima() {
    // given: 25 notificaciones (3 paginas), piden la 9
    when(this.notificationServiceMock.count(ME_ID)).thenReturn(25L);

    ModelAndView view = this.controller.list(9, this.me);

    verify(this.notificationServiceMock, times(1)).list(ME_ID, 2, NotificationController.PAGE_SIZE);
    assertThat(view.getModel().get("page"), is(2));
    assertThat(view.getModel().get("hasPrev"), is(true));
    assertThat(view.getModel().get("hasNext"), is(false));
  }

  @Test
  public void U04_feedPartial_devuelveLaBandejaSinLayout() {
    List<Notification> page = List.of(mock(Notification.class));
    when(this.notificationServiceMock.count(ME_ID)).thenReturn(25L);
    when(this.notificationServiceMock.list(ME_ID, 1, NotificationController.PAGE_SIZE))
      .thenReturn(page);

    ModelAndView view = this.controller.feedPartial(1, this.me);

    assertThat(view.getViewName(), is("pages/notifications/list :: feed"));
    assertThat(view.getModel().get("notifications"), is(page));
    assertThat(view.getModel().get("page"), is(1));
    assertThat(view.getModel().get("hasPrev"), is(true));
    assertThat(view.getModel().get("hasNext"), is(true));
  }

  @Test
  public void U04_list_sinNotificacionesVaciaLaPaginaCero() {
    // given: cuenta 0 — ni siquiera existe una pagina -1
    when(this.notificationServiceMock.count(ME_ID)).thenReturn(0L);

    ModelAndView view = this.controller.list(0, this.me);

    assertThat(view.getModel().get("notifications"), is(List.of()));
    assertThat(view.getModel().get("hasPrev"), is(false));
    assertThat(view.getModel().get("hasNext"), is(false));
  }

  // --- marcar ---

  @Test
  public void U05_markRead_marcaAlDestinatarioDeLaSesionYConservaLaPagina() {
    ModelAndView view = this.controller.markRead(NOTIFICATION_ID, 2, this.me);

    verify(this.notificationServiceMock, times(1)).markRead(ME_ID, NOTIFICATION_ID);
    assertThat(view.getViewName(), is("redirect:/notifications?page=2"));
  }

  @Test
  public void U06_markRead_paginaNegativaRedirigeACero() {
    ModelAndView view = this.controller.markRead(NOTIFICATION_ID, -1, this.me);

    assertThat(view.getViewName(), is("redirect:/notifications?page=0"));
  }

  @Test
  public void U07_markAllRead_delegaLaEscrituraMasiva() {
    ModelAndView view = this.controller.markAllRead(this.me);

    verify(this.notificationServiceMock, times(1)).markAllRead(ME_ID);
    assertThat(view.getViewName(), is("redirect:/notifications"));
  }

  @Test
  public void U08_delete_tiraLaFilaDelDestinatarioYConservaLaPagina() {
    ModelAndView view = this.controller.delete(NOTIFICATION_ID, 3, this.me);

    verify(this.notificationServiceMock, times(1)).delete(ME_ID, NOTIFICATION_ID);
    assertThat(view.getViewName(), is("redirect:/notifications?page=3"));
  }
}
