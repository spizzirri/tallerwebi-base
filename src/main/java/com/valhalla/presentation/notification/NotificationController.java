package com.valhalla.presentation.notification;

import com.valhalla.domain.notification.Notification;
import com.valhalla.domain.notification.NotificationGrouping;
import com.valhalla.domain.notification.NotificationService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * [NOTIF] Centro de notificaciones in-app: listado paginado, marcar una leida y marcar todas.
 *
 * <p>Mismo contrato de identidad que el perfil (AC-09): el destinatario sale siempre de la sesion,
 * el path solo nombra a la notificacion. La busqueda cruza id con destinatario en la capa de datos,
 * asi marcar una fila ajena es un no-op silencioso, no una filtracion.
 */
@Controller
@RequestMapping("/notifications")
public class NotificationController {

  /** Ventana fija: suficientemente grande para el scroll manual, acotada para la consulta. */
  static final int PAGE_SIZE = 10;

  private static final String VIEW_LIST = "pages/notifications/list";
  private static final String REDIRECT_LIST = "redirect:/notifications";
  private static final String PAGE_PARAM = "page";
  private static final String OPEN_GROUP_PARAM = "openGroup";

  private final NotificationService notificationService;
  private final UserService userService;

  @Autowired
  public NotificationController(NotificationService notificationService, UserService userService) {
    this.notificationService = notificationService;
    this.userService = userService;
  }

  @GetMapping
  public ModelAndView list(
    @RequestParam(name = PAGE_PARAM, defaultValue = "0") int page,
    @RequestParam(name = OPEN_GROUP_PARAM, required = false) UUID openGroup,
    Authentication authentication
  ) {
    return feed(new ModelAndView(VIEW_LIST), page, openGroup, authentication);
  }

  /**
   * [NOTIF] La misma bandeja sola, sin layout: el script de list la pide al tocar
   * Anteriores/Siguientes y reemplaza el fragmento ahi mismo, sin recargar la pagina entera.
   */
  @GetMapping(params = "partial")
  public ModelAndView feedPartial(
    @RequestParam(name = PAGE_PARAM, defaultValue = "0") int page,
    @RequestParam(name = OPEN_GROUP_PARAM, required = false) UUID openGroup,
    Authentication authentication
  ) {
    return feed(new ModelAndView(VIEW_LIST + " :: feed"), page, openGroup, authentication);
  }

  /** Ventana pedida fuera de rango: aclamp a la ultima, para que F5 y fragmento pinten lo mismo. */
  private ModelAndView feed(
    ModelAndView model,
    int page,
    UUID openGroup,
    Authentication authentication
  ) {
    User me = userService.getByEmail(authentication.getName());
    int currentPage = Math.max(0, page);
    long total = notificationService.count(me.getId());
    long unread = notificationService.unreadCount(me.getId());
    int totalPages = (int) ((total + PAGE_SIZE - 1) / PAGE_SIZE);
    if (currentPage >= totalPages) {
      currentPage = Math.max(0, totalPages - 1);
    }
    List<Notification> notifications = notificationService.list(me.getId(), currentPage, PAGE_SIZE);
    model.addObject("notifications", notifications);
    model.addObject("groups", NotificationGrouping.group(notifications));
    model.addObject("openGroup", openGroup);
    model.addObject(PAGE_PARAM, currentPage);
    model.addObject("readNotificationCount", Math.max(0, total - unread));
    model.addObject("totalPages", Math.max(1, totalPages));
    model.addObject("hasPrev", currentPage > 0);
    model.addObject("hasNext", currentPage + 1 < totalPages);
    model.addObject("pageTitle", "Notificaciones - PlanIt");
    return model;
  }

  /**
   * [NOTIF] AC-06: marcar una leida conserva la pagina desde la que se la acciono: el form de la
   * fila lleva el numero de pagina como hidden, porque el usuario no puede caer en otra ventana.
   */
  @PostMapping("/{id}/read")
  public ModelAndView markRead(
    @PathVariable UUID id,
    @RequestParam(name = PAGE_PARAM, defaultValue = "0") int page,
    Authentication authentication
  ) {
    User me = userService.getByEmail(authentication.getName());
    notificationService.markRead(me.getId(), id);
    return redirectToPage(page);
  }

  /** [NOTIF] AC-06: vaciar el badge de una accion. */
  @PostMapping("/read-all")
  public ModelAndView markAllRead(Authentication authentication) {
    User me = userService.getByEmail(authentication.getName());
    notificationService.markAllRead(me.getId());
    return new ModelAndView(REDIRECT_LIST);
  }

  /** Borrar en bloque las notificaciones ya leidas, sin tocar las pendientes. */
  @PostMapping("/delete-read")
  public ModelAndView deleteRead(
    @RequestParam(name = PAGE_PARAM, defaultValue = "0") int page,
    Authentication authentication
  ) {
    User me = userService.getByEmail(authentication.getName());
    notificationService.deleteRead(me.getId());
    return redirectToPage(page);
  }

  /**
   * [NGRP-006] Borrar todas las filas de un grupo. Cada borrado cruza el id con el destinatario
   * de la sesion, asi que un id ajeno es un no-op.
   */
  @PostMapping("/group/delete")
  public ModelAndView deleteGroup(
    @RequestParam(name = "ids", required = false) List<UUID> ids,
    @RequestParam(name = PAGE_PARAM, defaultValue = "0") int page,
    Authentication authentication
  ) {
    User me = userService.getByEmail(authentication.getName());
    if (ids != null) {
      ids.forEach(id -> notificationService.delete(me.getId(), id));
    }
    return redirectToPage(page);
  }

  /** [NGRP-003] Abrir el grupo marca todas sus filas propias como leidas y lo deja desplegado. */
  @PostMapping("/group/read")
  public ModelAndView markGroupRead(
    @RequestParam(name = "ids", required = false) List<UUID> ids,
    @RequestParam(name = PAGE_PARAM, defaultValue = "0") int page,
    @RequestParam(name = OPEN_GROUP_PARAM) UUID openGroup,
    Authentication authentication
  ) {
    User me = userService.getByEmail(authentication.getName());
    if (ids != null) {
      ids.forEach(id -> notificationService.markRead(me.getId(), id));
    }
    return new ModelAndView(
      REDIRECT_LIST + "?page=" + Math.max(0, page) + "&openGroup=" + openGroup
    );
  }

  /**
   * [NOTIF] AC-18: tirar una fila. La bandeja no es un archivo: lo que ya no sirve se borra y el
   * badge descuenta solo, porque el conteo se recalcula en el render siguiente.
   */
  @PostMapping("/{id}/delete")
  public ModelAndView delete(
    @PathVariable UUID id,
    @RequestParam(name = PAGE_PARAM, defaultValue = "0") int page,
    Authentication authentication
  ) {
    User me = userService.getByEmail(authentication.getName());
    notificationService.delete(me.getId(), id);
    return redirectToPage(page);
  }

  private static ModelAndView redirectToPage(int page) {
    return new ModelAndView(REDIRECT_LIST + "?page=" + Math.max(0, page));
  }
}
