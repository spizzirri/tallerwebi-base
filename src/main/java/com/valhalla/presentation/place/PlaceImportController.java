package com.valhalla.presentation.place;

import com.valhalla.domain.place.BoundingBox;
import com.valhalla.domain.place.OverpassClient;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * <p>La consulta llega con el area visible del mapa ({@code south}, {@code west}, {@code north},
 * {@code east}) y se recorta a {@link BoundingBox#CABA}: el importador carga la zona que el usuario
 * esta mirando, pero nunca sale de la ciudad.
 *
 * <p>Los errores se responden con {@link ResponseEntity} y no con una excepcion de status: el
 * {@code GlobalExceptionHandler} atrapa {@code Exception} y convertiria el 400/502 en el 500 de
 * {@code pages/error}.
 */
@Controller
@RequestMapping("/admin/places/import")
public class PlaceImportController {

  private static final String VIEW_IMPORT = "pages/admin/place-import";
  private static final String MODEL_CABA = "cabaBounds";
  private static final String ERROR_KEY = "error";
  private static final String ERROR_QUERY = "Unsupported query: use amenity or shop";
  private static final String ERROR_BOX = "The visible area must be inside CABA";
  private static final String ERROR_OVERPASS = "Overpass is not available right now";

  /** La vista no tiene como distinguir un timeout de un 502: el log es la unica pista. */
  private static final Logger LOGGER = Logger.getLogger(PlaceImportController.class.getName());

  /** Mismo allowlist que {@code HttpOverpassClient}: el 400 se responde sin salir a la red. */
  private static final Set<String> ALLOWED_KEYS = Set.of("amenity", "shop");

  /** Los cuatro bordes del area visible. Cualquier otro parametro es un 400. */
  private static final String PARAM_SOUTH = "south";
  private static final String PARAM_WEST = "west";
  private static final String PARAM_NORTH = "north";
  private static final String PARAM_EAST = "east";
  private static final Set<String> BOX_PARAMS = Set.of(
    PARAM_SOUTH,
    PARAM_WEST,
    PARAM_NORTH,
    PARAM_EAST
  );

  /** Un unico tag por consulta: el proxy no acepta combos de tags ni parametros de mas. */
  private static final int SINGLE_TAG = 1;

  private final OverpassClient overpassClient;

  @Autowired
  public PlaceImportController(OverpassClient overpassClient) {
    this.overpassClient = overpassClient;
  }

  /** El modelo lleva la caja de CABA: la vista decide con ella si el boton de cargar esta activo. */
  @GetMapping
  public ModelAndView showImportMap() {
    return new ModelAndView(VIEW_IMPORT)
      .addObject(
        MODEL_CABA,
        Map.of(
          PARAM_SOUTH,
          BoundingBox.CABA.south(),
          PARAM_WEST,
          BoundingBox.CABA.west(),
          PARAM_NORTH,
          BoundingBox.CABA.north(),
          PARAM_EAST,
          BoundingBox.CABA.east()
        )
      );
  }

  /** Tag OSM ya validado: key del allowlist y value no vacio. */
  private record OsmTag(String key, String value) {}

  /**
   * Proxy de una unica consulta: exactamente un tag del allowlist, los cuatro bordes del area
   * visible y ningun parametro de mas. Cualquier otra forma responde 400 sin llamar al cliente
   * (I-19); una vista fuera de CABA tambien, porque la consulta se recorta a la ciudad.
   */
  @GetMapping("/pois")
  @ResponseBody
  public ResponseEntity<Object> listPois(@RequestParam Map<String, String> params) {
    Optional<OsmTag> maybeTag = singleTagOf(params);
    if (maybeTag.isEmpty()) {
      return badRequest(ERROR_QUERY);
    }
    OsmTag tag = maybeTag.get();
    BoundingBox visible;
    try {
      visible = boxOf(params).intersection(BoundingBox.CABA);
    } catch (IllegalArgumentException exception) {
      return badRequest(ERROR_BOX);
    }
    if (visible == null) {
      return badRequest(ERROR_BOX);
    }
    try {
      return ResponseEntity.ok(overpassClient.findPois(tag.key(), tag.value(), visible));
    } catch (RuntimeException exception) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.log(
          Level.WARNING,
          "Overpass failed for " + tag.key() + "=" + tag.value(),
          exception
        );
      }
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(ERROR_KEY, ERROR_OVERPASS));
    }
  }

  /** Vacio si hay parametros desconocidos o si no vino exactamente un tag del allowlist. */
  private Optional<OsmTag> singleTagOf(Map<String, String> params) {
    boolean unknown = params
      .keySet()
      .stream()
      .anyMatch(name -> !ALLOWED_KEYS.contains(name) && !BOX_PARAMS.contains(name));
    List<Map.Entry<String, String>> tags = params
      .entrySet()
      .stream()
      .filter(entry -> ALLOWED_KEYS.contains(entry.getKey()))
      .toList();
    if (unknown || tags.size() != SINGLE_TAG) {
      return Optional.empty();
    }
    Map.Entry<String, String> tag = tags.getFirst();
    String value = tag.getValue();
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }
    return Optional.of(new OsmTag(tag.getKey(), value));
  }

  /**
   * Arma la caja del area visible. Un borde ausente o no numerico es {@link
   * IllegalArgumentException} y termina en 400: no se consulta con una caja a medias.
   */
  private BoundingBox boxOf(Map<String, String> params) {
    return new BoundingBox(
      coordinate(params, PARAM_SOUTH),
      coordinate(params, PARAM_WEST),
      coordinate(params, PARAM_NORTH),
      coordinate(params, PARAM_EAST)
    );
  }

  private double coordinate(Map<String, String> params, String name) {
    String raw = params.get(name);
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("Missing bounding box parameter: " + name);
    }
    try {
      return Double.parseDouble(raw);
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("Invalid bounding box parameter: " + name, exception);
    }
  }

  private ResponseEntity<Object> badRequest(String message) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(ERROR_KEY, message));
  }
}
