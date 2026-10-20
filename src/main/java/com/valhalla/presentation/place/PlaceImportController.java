package com.valhalla.presentation.place;

import com.valhalla.domain.place.OverpassClient;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Vista "Importar del mapa" y su proxy JSON sobre Overpass (AC-04). La consulta la hace el
 * backend a traves de {@link OverpassClient}: el navegador nunca habla con Overpass y ningun test
 * depende de la red (AC-10).
 *
 * <p>Los errores se responden con {@link ResponseEntity} y no con una excepcion de status: el
 * {@code GlobalExceptionHandler} atrapa {@code Exception} y convertiria el 400/502 en el 500 de
 * {@code pages/error}.
 */
@Controller
@RequestMapping("/admin/places/import")
public class PlaceImportController {

  private static final String VIEW_IMPORT = "pages/admin/place-import";
  private static final String ERROR_KEY = "error";
  private static final String ERROR_QUERY = "Unsupported query: use amenity or shop";
  private static final String ERROR_OVERPASS = "Overpass is not available right now";

  /** Mismo allowlist que {@code HttpOverpassClient}: el 400 se responde sin salir a la red. */
  private static final Set<String> ALLOWED_KEYS = Set.of("amenity", "shop");

  /** Un unico tag por consulta: el proxy no acepta combos ni parametros de mas. */
  private static final int SINGLE_QUERY_PARAM = 1;

  private final OverpassClient overpassClient;

  @Autowired
  public PlaceImportController(OverpassClient overpassClient) {
    this.overpassClient = overpassClient;
  }

  @GetMapping
  public ModelAndView showImportMap() {
    return new ModelAndView(VIEW_IMPORT);
  }

  /**
   * Proxy de una unica consulta: exactamente un parametro, con key del allowlist y value no vacio.
   * Cualquier otra forma responde 400 sin llamar al cliente (I-19).
   */
  @GetMapping("/pois")
  @ResponseBody
  public ResponseEntity<Object> listPois(@RequestParam Map<String, String> params) {
    if (params.size() != SINGLE_QUERY_PARAM) {
      return badQuery();
    }
    Map.Entry<String, String> query = params.entrySet().iterator().next();
    String key = query.getKey();
    String value = query.getValue();
    if (!ALLOWED_KEYS.contains(key) || value == null || value.isBlank()) {
      return badQuery();
    }
    try {
      return ResponseEntity.ok(overpassClient.findCabaPois(key, value));
    } catch (RuntimeException exception) {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(ERROR_KEY, ERROR_OVERPASS));
    }
  }

  private ResponseEntity<Object> badQuery() {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(ERROR_KEY, ERROR_QUERY));
  }
}
