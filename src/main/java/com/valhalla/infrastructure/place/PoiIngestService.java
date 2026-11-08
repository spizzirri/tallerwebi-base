package com.valhalla.infrastructure.place;

import com.valhalla.domain.place.BoundingBox;
import com.valhalla.domain.place.MapPoi;
import com.valhalla.domain.place.MapPoiRepository;
import com.valhalla.domain.place.OsmPoi;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.place.PoiStatus;
import com.valhalla.domain.place.PoiZone;
import com.valhalla.domain.place.PoiZoneRepository;
import com.valhalla.domain.place.ZonePois;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Guarda en la base lo que trae Overpass y aplica solo el diff contra lo anterior. [POI]
 *
 * <p>La consulta a Overpass tarda 25–45 s, asi que {@link #ingest} es lo unico transaccional y la
 * llamada HTTP queda afuera, en el controller: sostener una conexion del pool durante medio minuto
 * por click es la forma mas rapida de dejar el sitio sin conexiones.
 *
 * <p>El diff es semantico, campo por campo, no de filas: recargar la misma zona con los mismos datos
 * no emite un solo {@code UPDATE}. Y la baja es logica ({@link PoiStatus#REMOVED}), porque un 502 de
 * Overpass no puede vaciarle el mapa al admin.
 */
@Service
public class PoiIngestService {

  /** Grilla de la zona: 0.01° ≈ 1.1 km. Redondear hacia afuera es lo que garantiza el "contiene". */
  private static final double GRID_SCALE = 100;

  /**
   * Tolerancia al redondear. {@code -34.62 * 100} da {@code -3462.0000000000005} en binario: sin
   * esto, una vista que ya cae justo en la grilla se agrandaria una celda de mas y dos vistas
   * pegadas dejarian de compartir zona.
   */
  private static final double GRID_EPSILON = 1e-9;

  private final MapPoiRepository mapPoiRepository;
  private final PoiZoneRepository poiZoneRepository;

  @Autowired
  public PoiIngestService(MapPoiRepository mapPoiRepository, PoiZoneRepository poiZoneRepository) {
    this.mapPoiRepository = mapPoiRepository;
    this.poiZoneRepository = poiZoneRepository;
  }

  /**
   * La caja que se consulta y se guarda: la pedida, agrandada a la grilla y recortada a CABA.
   *
   * <p>Agrandar hacia afuera y no hacia adentro es a proposito: asi la zona siempre <em>contiene</em>
   * la vista, y servirla nunca devuelve menos puntos de los que el admin esta mirando.
   */
  public static BoundingBox zoneOf(BoundingBox requested) {
    BoundingBox snapped = new BoundingBox(
      Math.floor(requested.south() * GRID_SCALE + GRID_EPSILON) / GRID_SCALE,
      Math.floor(requested.west() * GRID_SCALE + GRID_EPSILON) / GRID_SCALE,
      Math.ceil(requested.north() * GRID_SCALE - GRID_EPSILON) / GRID_SCALE,
      Math.ceil(requested.east() * GRID_SCALE - GRID_EPSILON) / GRID_SCALE
    );
    BoundingBox clipped = snapped.intersection(BoundingBox.CABA);
    // Defensivo: el controller ya recorta la vista a CABA, asi que esto solo se dispara si la zona
    // quedo entera afuera. Devolver la pedida mantiene el contrato en vez de romper con null.
    return clipped == null ? requested : clipped;
  }

  /**
   * Lo guardado de la caja pedida. No toca la red: es lo que corre al entrar y al mover el mapa.
   *
   * <p>Devuelve las filas tal como estan guardadas, porque el sobre necesita su categoria y su
   * identidad; el controller las proyecta a {@code PoiView}.
   */
  public ZonePois read(String tagKey, String tagValue, BoundingBox requested) {
    List<MapPoi> pois = mapPoiRepository
      .findInBox(tagKey, tagValue, requested)
      .stream()
      .filter(poi -> poi.getStatus() == PoiStatus.ACTIVE)
      .toList();
    Instant fetchedAt = poiZoneRepository
      .find(zoneKey(zoneOf(requested)), tagKey, tagValue)
      .map(PoiZone::getFetchedAt)
      .orElse(null);
    return new ZonePois(pois, fetchedAt);
  }

  /**
   * Aplica el diff de lo traido contra lo guardado y sella la cobertura de la zona.
   *
   * @param zone la caja con la que se consulto Overpass (ya pasada por {@link #zoneOf})
   * @return cuantas filas se crearon, actualizaron, dieron de baja y reactivaron
   */
  @Transactional
  public IngestResult ingest(
    String tagKey,
    String tagValue,
    BoundingBox zone,
    List<OsmPoi> fetched
  ) {
    Map<String, MapPoi> stored = new HashMap<>();
    for (MapPoi poi : mapPoiRepository.findInBox(tagKey, tagValue, zone)) {
      stored.put(poi.getSourceKey(), poi);
    }

    List<MapPoi> toSave = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    Map<Outcome, Integer> counts = new EnumMap<>(Outcome.class);
    for (OsmPoi poi : fetched) {
      counts.merge(merge(poi, tagKey, tagValue, stored, seen, toSave), 1, Integer::sum);
    }

    int removed = 0;
    for (MapPoi existing : stored.values()) {
      if (seen.contains(existing.getSourceKey()) || existing.getStatus() == PoiStatus.REMOVED) {
        continue;
      }
      existing.setStatus(PoiStatus.REMOVED);
      toSave.add(existing);
      removed++;
    }

    mapPoiRepository.saveAll(toSave);
    persistZone(zone, tagKey, tagValue);
    return new IngestResult(
      counts.getOrDefault(Outcome.CREATED, 0),
      counts.getOrDefault(Outcome.UPDATED, 0),
      removed,
      counts.getOrDefault(Outcome.REACTIVATED, 0)
    );
  }

  /**
   * Que hacer con un POI traido: crear, actualizar, reactivar o nada. {@code toSave} recibe solo las
   * filas que hay que escribir, asi que una recarga identica no emite un solo {@code UPDATE}.
   */
  private static Outcome merge(
    OsmPoi poi,
    String tagKey,
    String tagValue,
    Map<String, MapPoi> stored,
    Set<String> seen,
    List<MapPoi> toSave
  ) {
    // Sin coordenadas no hay donde dibujarlo ni como encontrarlo de nuevo: guardarlo dejaria una
    // fila duplicada en cada carga, porque la lectura por caja nunca la vuelve a ver.
    if (poi.latitude() == null || poi.longitude() == null) {
      return Outcome.IGNORED;
    }
    String key = MapPoi.sourceKeyOf(poi);
    // Un mismo elemento repetido en la respuesta se cuenta una vez: si no, el segundo intento
    // viola la unique de source_key.
    if (!seen.add(key)) {
      return Outcome.IGNORED;
    }
    PlaceCategory category = PlaceCategory.fromOsm(poi.osmKey(), poi.osmValue());
    MapPoi existing = stored.get(key);
    if (existing == null) {
      toSave.add(MapPoi.from(poi, category, tagKey, tagValue));
      return Outcome.CREATED;
    }
    if (existing.getStatus() == PoiStatus.REMOVED) {
      existing.setStatus(PoiStatus.ACTIVE);
      existing.apply(poi, category);
      toSave.add(existing);
      return Outcome.REACTIVATED;
    }
    if (existing.apply(poi, category)) {
      toSave.add(existing);
      return Outcome.UPDATED;
    }
    return Outcome.IGNORED;
  }

  /** Sella la cobertura: crea la fila o le mueve el {@code fetched_at}, sin tocar los POIs. */
  private void persistZone(BoundingBox zone, String tagKey, String tagValue) {
    Instant now = Instant.now();
    String key = zoneKey(zone);
    PoiZone row = poiZoneRepository
      .find(key, tagKey, tagValue)
      .orElseGet(() -> new PoiZone(key, tagKey, tagValue, now));
    row.setFetchedAt(now);
    poiZoneRepository.save(row);
  }

  /**
   * Clave de la zona: los cuatro bordes a dos decimales. Se arma con {@code Locale.ROOT} porque un
   * default {@code es-AR} escribiria comas y la clave se partiria en cuatro.
   *
   * <p>Es la identidad de la cobertura ({@code poi_zone.zone_key}); publica para que los tests
   * puedan leer y envejecer una zona sin rearmar el formato a mano.
   */
  public static String zoneKey(BoundingBox zone) {
    return String.format(
      Locale.ROOT,
      "%.2f,%.2f,%.2f,%.2f",
      zone.south(),
      zone.west(),
      zone.north(),
      zone.east()
    );
  }

  /**
   * Conteos del diff. Existen para que el diff sea observable sin contar filas a mano en los tests.
   *
   * @param created filas nuevas
   * @param updated filas cuyo contenido visible cambio
   * @param removed filas que pasaron a {@code REMOVED}
   * @param reactivated filas {@code REMOVED} que volvieron a aparecer
   */
  public record IngestResult(int created, int updated, int removed, int reactivated) {}

  /** Que le paso a un POI traido, para poder contarlo sin ramas anidadas en {@link #ingest}. */
  private enum Outcome {
    CREATED,
    UPDATED,
    REACTIVATED,
    IGNORED,
  }
}
