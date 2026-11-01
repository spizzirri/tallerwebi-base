package com.valhalla.presentation.place;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * La vista de /explore. El listado lo arma el JS contra /api/places, asi que aca solo se sirve el
 * template: ni el catalogo ni los filtros viajan en el modelo.
 */
@Controller
@RequestMapping("/explore")
public class PlaceController {

  private static final String VIEW_LIST = "pages/places/list";

  @GetMapping
  public ModelAndView listPlaces(Principal principal) {
    Map<String, Object> model = new ModelMap();
    model.put("defaultPlanSettings", new com.valhalla.domain.plan.PlanSettings());
    // [WEA] El selector de fecha del plan no acepta días ya pasados.
    model.put("today", LocalDate.now().toString());
    // [APL] Sesión para el picker del panel de detalle: /api/plans devuelve [] tanto en
    // anónimo como en un usuario sin planes, así que por el fetch solo el frontend no sabe
    // si debe ocultar el bloque o ofrecer crear un primer plan. Defensivo como el
    // CurrentUserControllerAdvice: token anónimo cuenta como sin sesión.
    model.put(
      "isLogged",
      principal != null && !(principal instanceof AnonymousAuthenticationToken)
    );
    return new ModelAndView(VIEW_LIST, model);
  }
}
