package com.valhalla.presentation.place;

import com.valhalla.domain.place.BoundingBox;
import com.valhalla.domain.place.MapPoi;
import com.valhalla.domain.place.OsmPoi;
import com.valhalla.domain.place.OverpassClient;
import com.valhalla.domain.place.PlaceService;
import com.valhalla.domain.place.ZonePois;
import com.valhalla.infrastructure.place.PoiIngestService;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Vista "Importar del mapa" y sus dos endpoints JSON (AC-04, AC-05). La consulta la hace el backend
 * a traves de {@link OverpassClient}: el navegador nunca habla con Overpass y ningun test depende de
 * la red (AC-10).
 *
 * <p>El flujo tiene tres pasos y solo uno puede costar medio minuto, y solo la primera vez que
 * se mira una zona:
 *
 * <table>
 *   <caption>Endpoints</caption>
 *   <tr><th>Paso</th><th>Endpoint</th><th>Red</th></tr>
 *   <tr><td>Entrar o mover el mapa</td><td>{@code GET /pois}</td><td>ninguna: lee de la base</td></tr>
 *   <tr><td>"Cargar puntos de esta zona"</td><td>{@code GET /refresh}</td><td>Overpass + diff, o solo la base si la zona esta fresca</td></tr>
 *   <tr><td>Elegir un punto</td><td>{@code /admin/places/new?…}</td><td>ninguna</td></tr>
 * </table>
 *
 * <p>La ventana de frescura es lo que evita pagar los 25-45 s dos veces por la misma zona: una
 * caja traida hace menos de {@code place.import.freshness-minutes} minutos se responde con lo
 * guardado. El elemento que se trae no se puede recortar para ganar tiempo: Overpass evalua la
 * consulta completa antes de aplicar el limite de {@code out}, y recortar la respuesta marcaria
 * como dados de baja puntos que siguen existiendo.
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

  /** Campos del sobre: los mismos para los dos endpoints, para que la vista no distinga. */
  private static final String FIELD_POIS = "pois";
  private static final String FIELD_FETCHED_AT = "fetchedAt";

  /** {@code true} solo si esta misma request fue a Overpass; {@code false} si salio de la base. */
  private static final String FIELD_REFRESHED = "refreshed";

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

  /** Solo las claves con este prefijo son una identidad de OSM que el alta puede reusar. */
  private static final String OSM_PREFIX = "osm:";

  private final OverpassClient overpassClient;
  private final PoiIngestService poiIngestService;
  private final PlaceService placeService;

  /** Cuanto vale lo guardado de una zona antes de volver a pedirla. */
  private final Duration freshness;

  @Autowired
  public PlaceImportController(
    OverpassClient overpassClient,
    PoiIngestService poiIngestService,
    PlaceService placeService,
    @Value("${place.import.freshness-minutes:15}") long freshnessMinutes
  ) {
    this.overpassClient = overpassClient;
    this.poiIngestService = poiIngestService;
    this.placeService = placeService;
    this.freshness = Duration.ofMinutes(freshnessMinutes);
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
   * Lo guardado de esa zona y tag. Es lo que corre al entrar y al mover el mapa: responde en
   * milisegundos y no gasta una consulta de 30 s por cada pixel.
   */
  @GetMapping("/pois")
  @ResponseBody
  public ResponseEntity<Object> listPois(@RequestParam Map<String, String> params) {
    return respond(params, false);
  }

  /**
   * Consulta Overpass, aplica el diff contra lo guardado (crear lo nuevo, actualizar lo que cambio,
   * dar de baja lo que ya no viene) y responde lo mismo que {@code /pois}.
   *
   * <p>Es el unico camino que puede tocar la red. Solo la toca si la zona+tag se trajo hace mas de
   * {@code place.import.freshness-minutes}: una zona recien cargada se responde con lo guardado y
   * el sobre lo dice en {@code refreshed}.
   */
  @GetMapping("/refresh")
  @ResponseBody
  public ResponseEntity<Object> refreshPois(@RequestParam Map<String, String> params) {
    return respond(params, true);
  }

  /**
   * Forma del pedido (un tag del allowlist, los cuatro bordes y ningun parametro de mas) y despues
   * el trabajo. Cualquier otra forma responde 400 sin llamar al cliente (I-19); una vista fuera de
   * CABA tambien, porque la consulta se recorta a la ciudad.
   *
   * @param refresh {@code true} cuando el pedido viene del boton y hay que ir a Overpass
   */
  private ResponseEntity<Object> respond(Map<String, String> params, boolean refresh) {
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
    ZonePois stored = poiIngestService.read(tag.key(), tag.value(), visible);
    boolean queried = refresh && !isFresh(stored.fetchedAt());
    if (queried && !ingest(tag, visible)) {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(ERROR_KEY, ERROR_OVERPASS));
    }
    ZonePois answer = queried ? poiIngestService.read(tag.key(), tag.value(), visible) : stored;
    return ResponseEntity.ok(envelope(answer, queried));
  }

  /**
   * Una zona traida dentro de la ventana no se vuelve a pedir: Overpass tarda 25-45 s y recargar la
   * misma caja con los mismos datos no cambia nada. {@code null} es "nunca se cargo", que nunca es
   * fresco.
   */
  private boolean isFresh(Instant fetchedAt) {
    return fetchedAt != null && fetchedAt.plus(freshness).isAfter(Instant.now());
  }

  /**
   * Trae la zona sellada a Overpass y la guarda. La llamada HTTP queda fuera de la transaccion de
   * {@code ingest}: sostener una conexion del pool durante 30 s por click deja el sitio sin pool.
   *
   * @return {@code false} si Overpass fallo; en ese caso no se toco ninguna fila
   */
  private boolean ingest(OsmTag tag, BoundingBox visible) {
    BoundingBox zone = PoiIngestService.zoneOf(visible);
    try {
      List<OsmPoi> fetched = overpassClient.findPois(tag.key(), tag.value(), zone);
      poiIngestService.ingest(tag.key(), tag.value(), zone, fetched);
      return true;
    } catch (RuntimeException exception) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.log(
          Level.WARNING,
          "Overpass failed for " + tag.key() + "=" + tag.value(),
          exception
        );
      }
      return false;
    }
  }

  /**
   * Mismo sobre para los dos endpoints: {@code fetchedAt} en {@code null} es "zona nunca cargada",
   * que es distinto de "cargada sin resultados".
   *
   * <p>{@code refreshed} dice si esta misma request pago Overpass. Es la unica forma que tiene la
   * vista de distinguir "se actualizo y no cambio nada" de "no se consulto porque lo guardado
   * todavia vale", que desde el boton se ven iguales.
   *
   * <p>Cada punto viaja como {@link PoiView}, con su categoria (el color del marcador) y el id del
   * lugar que ya lo representa. Los ids guardados se piden una sola vez para toda la pantalla: la
   * vista puede traer hasta 300 puntos y una consulta por punto no escala.
   *
   * <p>{@code fetchedAt} va como ISO-8601 ya formateado, no como {@code Instant}: asi el navegador
   * lo parsea con zona horaria en vez de depender de como serialice fechas el ObjectMapper.
   */
  private Map<String, Object> envelope(ZonePois zonePois, boolean refreshed) {
    Map<String, UUID> savedPlaceIds = placeService.savedPlaceIds(osmKeysOf(zonePois.pois()));
    Map<String, Object> body = new LinkedHashMap<>();
    body.put(FIELD_POIS, viewsOf(zonePois.pois(), savedPlaceIds));
    body.put(
      FIELD_FETCHED_AT,
      zonePois.fetchedAt() == null ? null : zonePois.fetchedAt().toString()
    );
    body.put(FIELD_REFRESHED, refreshed);
    return body;
  }

  /** Las identidades que vale la pena preguntar: el fallback no es una identidad de OSM. */
  private static List<String> osmKeysOf(List<MapPoi> pois) {
    return pois
      .stream()
      .map(MapPoi::getSourceKey)
      .filter(key -> key.startsWith(OSM_PREFIX))
      .toList();
  }

  /** Un punto por {@link PoiView}, con el lugar guardado que le corresponde (o {@code null}). */
  private static List<PoiView> viewsOf(List<MapPoi> pois, Map<String, UUID> savedPlaceIds) {
    return pois
      .stream()
      .map(poi -> PoiView.from(poi, savedPlaceIds.get(poi.getSourceKey())))
      .toList();
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
