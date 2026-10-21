package com.valhalla.presentation.bookmark;

import com.valhalla.domain.bookmark.PlanBookmarkService;
import com.valhalla.domain.plan.Plan;
import com.valhalla.presentation.shared.LoginRedirects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * [MARK] Guardar, quitar y clonar un plan guardado desde las vistas server-rendered (detalle, vista
 * publica, feed y perfil). El sidebar de {@code /explore} usa {@link PlanBookmarkRestController}
 * porque se renderiza con Vue.
 *
 * <p>El email sale siempre de la sesion, nunca del form. Todas las rutas son POST y caen en
 * {@code anyRequest().authenticated()}: sin sesion, Spring Security redirige al login antes de
 * entrar aca (AC-05).
 */
@Controller
@RequestMapping("/bookmarks")
public class BookmarkController {

  private static final String REDIRECT_PREFIX = "redirect:";
  private static final String PUBLIC_PLAN_PATH = "/plans/";

  private final PlanBookmarkService planBookmarkService;

  @Autowired
  public BookmarkController(PlanBookmarkService planBookmarkService) {
    this.planBookmarkService = planBookmarkService;
  }

  /** Guardar: idempotente. {@code returnTo} es la pantalla desde la que se apreto el icono. */
  @PostMapping("/{planId}")
  public ModelAndView save(
    @PathVariable UUID planId,
    @RequestParam(required = false) String returnTo,
    Authentication authentication
  ) {
    this.planBookmarkService.save(planId, authentication.getName());
    return new ModelAndView(REDIRECT_PREFIX + destination(returnTo, planId));
  }

  /** Quitar: idempotente. La confirmacion la pide la vista, no el controller. */
  @PostMapping("/{planId}/remove")
  public ModelAndView remove(
    @PathVariable UUID planId,
    @RequestParam(required = false) String returnTo,
    Authentication authentication
  ) {
    this.planBookmarkService.remove(planId, authentication.getName());
    return new ModelAndView(REDIRECT_PREFIX + destination(returnTo, planId));
  }

  /**
   * Clonar desde Guardados. Sirve para los tres estados del guardado: con el plan vivo delega en
   * [CLO]; si ya no esta disponible reconstruye la copia desde el snapshot.
   */
  @PostMapping("/{planId}/clone")
  public ModelAndView clone(@PathVariable UUID planId, Authentication authentication) {
    Plan copy = this.planBookmarkService.cloneBookmark(planId, authentication.getName());
    return new ModelAndView(REDIRECT_PREFIX + PUBLIC_PLAN_PATH + copy.getId());
  }

  /**
   * {@code returnTo} viaja en el form y lo controla el navegador: si no es una ruta interna segura
   * se ignora y se vuelve a la vista publica del plan (misma regla que el login, AC-18).
   */
  private static String destination(String returnTo, UUID planId) {
    if (LoginRedirects.isSafeInternalPath(returnTo)) {
      return returnTo;
    }
    return PUBLIC_PLAN_PATH + planId + "/public";
  }
}
