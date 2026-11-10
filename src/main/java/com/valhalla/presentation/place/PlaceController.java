package com.valhalla.presentation.place;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceService;
import com.valhalla.domain.plan.PlanService;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

/**
 * Las dos vistas de lugares: la lista de /explore y la ficha publica de /places/{id} [PSHARE].
 *
 * <p>El listado lo arma el JS contra /api/places, asi que aca solo se sirve el template; la ficha
 * si se renderiza del modelo (nombre, categoria, direccion, mapa, planes publicos que incluyen el
 * lugar) y es abierta sin sesion: es el destino del link compartido.
 */
@Controller
public class PlaceController {

  private static final String VIEW_LIST = "pages/places/list";
  private static final String VIEW_DETAIL = "pages/places/detail";
  private static final String VIEW_ERROR = "pages/error";
  private static final String ERROR_NOT_FOUND = "Place not found";

  private final PlaceService placeService;
  private final PlanService planService;

  @Autowired
  public PlaceController(PlaceService placeService, PlanService planService) {
    this.placeService = placeService;
    this.planService = planService;
  }

  @GetMapping("/explore")
  public ModelAndView listPlaces(Principal principal) {
    Map<String, Object> model = new ModelMap();
    model.put("defaultPlanSettings", new com.valhalla.domain.plan.PlanSettings());
    // [WEA] El selector de fecha del plan no acepta días ya pasados.
    model.put("today", LocalDate.now().toString());
    model.put("ogTitle", "Explorar lugares en Buenos Aires");
    model.put("ogDescription", "Descubrí lugares y armá tu plan perfecto con PlanIt.");
    model.put("ogUrl", "/explore");
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

  /**
   * [PSHARE-001/006] Ficha publica del lugar: la pagina que se comparte por URL. Un id que no
   * existe responde 404 con la vista de error, sin caer en el redirect del ABM de /admin/places
   * (el handler de PlaceNotFoundException vive en PlaceAdminController y sigue siendo 500 alla).
   */
  @GetMapping("/places/{id}")
  public ModelAndView showPlace(@PathVariable UUID id) {
    Optional<Place> found = placeService.getPlaceById(id);
    if (found.isEmpty()) {
      return notFound();
    }
    Place place = found.get();
    Map<String, Object> model = new ModelMap();
    model.put("place", place);
    // [PSHARE-001] Los planes PUBLICOS que incluyen el lugar, listados en la ficha.
    model.put("publicPlans", planService.getPublicPlansByPlaceId(place.getId()));
    addShareMeta(model, place);
    return new ModelAndView(VIEW_DETAIL, model);
  }

  /**
   * [PSHARE-002] Metas de la vista previa al compartir. La descripcion entra truncada a 200
   * caracteres como en la vista publica de un plan: los scrapers cortan igual, y lo que se
   * recorta aca es el texto que se envia. Sin imagen propia, la ficha no declara og:image y el
   * preview de base.html cae en la imagen de marca.
   */
  private static void addShareMeta(Map<String, Object> model, Place place) {
    model.put("pageTitle", place.getName());
    model.put("ogType", "website");
    model.put("ogTitle", place.getName());
    model.put("ogUrl", "/places/" + place.getId());
    String description = Optional.ofNullable(place.getDescription()).orElse("").trim();
    if (description.isBlank()) {
      description = place.getName() + " es un lugar para descubrir en PlanIt.";
    }
    String ogDescription = description.length() <= 200
      ? description
      : description.substring(0, 199).stripTrailing() + "…";
    model.put("pageDescription", ogDescription);
    model.put("ogDescription", ogDescription);
    model.put("ogImage", imageOrNull(place.getImageUrl()));
  }

  /** La foto del lugar como og:image; vacia o en blanco no pinta un meta roto. */
  private static String imageOrNull(String imageUrl) {
    return Optional
      .ofNullable(imageUrl)
      .map(String::trim)
      .filter(image -> !image.isEmpty())
      .orElse(null);
  }

  /** El 404 de la ficha: un id inexistente no es un error del servidor. */
  private static ModelAndView notFound() {
    return new ModelAndView(VIEW_ERROR, Map.of("error", ERROR_NOT_FOUND), HttpStatus.NOT_FOUND);
  }
}
