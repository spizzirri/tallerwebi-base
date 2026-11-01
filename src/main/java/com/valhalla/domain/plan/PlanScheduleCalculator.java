package com.valhalla.domain.plan;

import com.valhalla.domain.place.PlaceCategory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * [SCH] Planificador del itinerario: reparte las paradas de un plan en una o mas jornadas.
 *
 * <p>Es una funcion pura: recibe la fecha, la ventana horaria, el modo por defecto y la lista de
 * paradas, y devuelve un horario por parada. Sin Spring y sin acceso a datos, para que las reglas
 * de horario se puedan probar sin levantar contexto.
 *
 * <p>[SCH] El cursor es un {@link LocalDateTime} y no un par fecha/hora: una parada que cruza la
 * medianoche tiene que avanzar al dia siguiente, no dar la vuelta al reloj. Una parada que no entra
 * en la ventana rueda una sola vez; si su duracion sola ya excede la ventana, desborda en vez de
 * iterar.
 */
public final class PlanScheduleCalculator {

  /** Traslado por defecto cuando falta la ubicacion de alguna de las dos paradas. */
  public static final int DEFAULT_GAP_MINUTES = 15;

  /** Piso del traslado: dos paradas pegadas igual pagan el tiempo de salir y volver a entrar. */
  public static final int MIN_GAP_MINUTES = 5;

  /** Techo del traslado: mas de hora y media entre dos paradas sugiere que el orden no sirve. */
  public static final int MAX_GAP_MINUTES = 90;

  /** Factor de rodeo: la distancia en linea recta no es la que se recorre por la ciudad. */
  public static final double ROAD_FACTOR = 1.3;

  /** Radio terrestre medio en kilometros, el que usa la formula de haversine. */
  public static final double EARTH_RADIUS_KM = 6371.0088;

  private static final LocalTime DEFAULT_DAY_END = LocalTime.of(23, 0);
  private static final int DEFAULT_DURATION_MINUTES = 60;
  private static final int GAP_ROUNDING_MINUTES = 5;
  private static final int MINUTES_PER_HOUR = 60;

  /**
   * [SCH] Duracion tipica por categoria, para una parada que no trae duracion propia ni la del
   * catalogo de lugares. Los valores son deliberadamente generosos: es mas facil recortar una
   * visita que llegar tarde a la siguiente.
   */
  private static final Map<PlaceCategory, Integer> DEFAULT_DURATIONS = Map.ofEntries(
    Map.entry(PlaceCategory.RESTAURANT, 90),
    Map.entry(PlaceCategory.BAR, 60),
    Map.entry(PlaceCategory.CAFE, 45),
    Map.entry(PlaceCategory.MUSEUM, 90),
    Map.entry(PlaceCategory.PARK, 60),
    Map.entry(PlaceCategory.SHOPPING, 60),
    Map.entry(PlaceCategory.NIGHTLIFE, 120),
    Map.entry(PlaceCategory.CULTURE, 90),
    Map.entry(PlaceCategory.SPORT, 90),
    Map.entry(PlaceCategory.OTHER, 60)
  );

  private PlanScheduleCalculator() {}

  /**
   * Una parada tal como entra al planificador: los pines que fijo el usuario (fecha y/u hora), las
   * duraciones candidatas, los datos con los que se estima el traslado y el traslado fijado a mano
   * hacia la parada siguiente.
   *
   * @param pinnedDate fecha fijada a mano para la parada, o {@code null} si sigue la del plan
   * @param pinnedTime hora fijada a mano para la parada, o {@code null} si sigue el calculo
   * @param durationMinutes duracion de la visita usada para calcular (fijada o por defecto)
   * @param placeDurationMinutes duracion propia del lugar, sin fijar a mano
   * @param category categoria del lugar (define la duracion por defecto)
   * @param latitude latitud del lugar, para estimar el traslado siguiente
   * @param longitude longitud del lugar, para estimar el traslado siguiente
   * @param transportModeToNext modo de traslado fijado a mano hacia la parada siguiente
   * @param travelMinutesToNext minutos de traslado fijados a mano hacia la siguiente
   */
  public record Stop(
    LocalDate pinnedDate,
    LocalTime pinnedTime,
    Integer durationMinutes,
    Integer placeDurationMinutes,
    PlaceCategory category,
    Double latitude,
    Double longitude,
    TransportMode transportModeToNext,
    Integer travelMinutesToNext
  ) {}

  /**
   * Horario calculado para una parada: dia, hora de inicio y hora de fin.
   *
   * @param date dia en el que cae la parada
   * @param start hora de inicio de la visita
   * @param end hora de fin de la visita
   */
  public record Slot(LocalDate date, LocalTime start, LocalTime end) {}

  /**
   * Calcula el horario de {@code stops} en orden, arrancando en {@code dayStart}.
   *
   * @param planDate fecha base del plan; sin ella no hay jornada donde ubicar las paradas
   * @param dayStart hora en la que abre cada jornada
   * @param dayEnd hora en la que cierra cada jornada; {@code null} usa las 23:00
   * @param defaultMode modo de traslado del plan; {@code null} usa caminar
   * @param stops paradas en el orden del itinerario
   * @return un horario por parada, en el mismo orden; lista vacia si falta la fecha, la hora de
   *     inicio o las paradas
   */
  public static List<Slot> schedule(
    LocalDate planDate,
    LocalTime dayStart,
    LocalTime dayEnd,
    TransportMode defaultMode,
    List<Stop> stops
  ) {
    if (planDate == null || dayStart == null || stops == null || stops.isEmpty()) {
      return List.of();
    }
    LocalTime end = dayEnd == null ? DEFAULT_DAY_END : dayEnd;
    TransportMode mode = defaultMode == null ? TransportMode.WALK : defaultMode;

    List<Slot> slots = new ArrayList<>(stops.size());
    LocalDateTime cursor = LocalDateTime.of(planDate, dayStart);
    for (int index = 0; index < stops.size(); index++) {
      Stop stop = stops.get(index);
      int duration = effectiveDuration(stop);
      LocalDateTime start = resolveStart(stop, cursor, dayStart, end, duration);
      LocalDateTime finish = start.plusMinutes(duration);
      slots.add(new Slot(start.toLocalDate(), start.toLocalTime(), finish.toLocalTime()));
      boolean hasNext = index + 1 < stops.size();
      cursor = finish.plusMinutes(hasNext ? gap(stop, stops.get(index + 1), mode) : 0);
    }
    return slots;
  }

  /** Duracion tipica de la visita para {@code category}; {@code null} o desconocida vale 60. */
  public static int defaultDurationMinutes(PlaceCategory category) {
    return category == null
      ? DEFAULT_DURATION_MINUTES
      : DEFAULT_DURATIONS.getOrDefault(category, DEFAULT_DURATION_MINUTES);
  }

  /**
   * [SCH] Resuelve donde arranca la parada. Un pin de hora manda sobre la cadena y conserva el dia
   * que esta ya tenia; un pin de dia sin hora reinicia esa jornada en {@code dayStart}; sin pines,
   * la parada rueda al dia siguiente solo si no entra en la ventana.
   */
  private static LocalDateTime resolveStart(
    Stop stop,
    LocalDateTime cursor,
    LocalTime dayStart,
    LocalTime dayEnd,
    int duration
  ) {
    if (stop.pinnedTime() != null) {
      LocalDate day = stop.pinnedDate() != null ? stop.pinnedDate() : cursor.toLocalDate();
      return LocalDateTime.of(day, stop.pinnedTime());
    }
    if (stop.pinnedDate() != null) {
      return LocalDateTime.of(stop.pinnedDate(), dayStart);
    }
    if (minuteOfDay(cursor) + duration > minuteOfDay(dayEnd)) {
      return LocalDateTime.of(cursor.toLocalDate().plusDays(1), dayStart);
    }
    return cursor;
  }

  /**
   * [SCH] Cascada de duraciones: override de la visita, duracion del lugar y default de la
   * categoria. Es publica porque los seeders y el backfill completan duraciones sin armar una
   * parada; la calculadora y ellos tienen que resolver el mismo nivel.
   *
   * @param durationMinutes override de la visita; {@code null} o no positivo lo descarta
   * @param placeDurationMinutes duracion tipica del lugar; {@code null} o no positiva la descarta
   * @param category categoria del lugar, la ultima fuente de la cascada
   * @return la primera duracion positiva de la cascada, o el default de la categoria
   */
  public static int effectiveDuration(
    Integer durationMinutes,
    Integer placeDurationMinutes,
    PlaceCategory category
  ) {
    if (durationMinutes != null && durationMinutes > 0) {
      return durationMinutes;
    }
    if (placeDurationMinutes != null && placeDurationMinutes > 0) {
      return placeDurationMinutes;
    }
    return defaultDurationMinutes(category);
  }

  /** Cascada de duraciones de una parada, delegada en la version publica. */
  private static int effectiveDuration(Stop stop) {
    return effectiveDuration(stop.durationMinutes(), stop.placeDurationMinutes(), stop.category());
  }

  /**
   * Traslado hacia la parada siguiente. Con minutos fijados a mano manda el override (piso 1, sin
   * techo: el usuario conocio el trayecto). Sin override se estima por modo del tramo (o el del
   * plan), distancia con rodeo y redondeo a cinco minutos; sin coordenadas cae al valor por
   * defecto, y el resultado calculado queda entre {@link #MIN_GAP_MINUTES} y
   * {@link #MAX_GAP_MINUTES}.
   */
  private static int gap(Stop from, Stop to, TransportMode defaultMode) {
    if (from.travelMinutesToNext() != null) {
      return Math.max(1, from.travelMinutesToNext());
    }
    if (!hasCoordinates(from) || !hasCoordinates(to)) {
      return DEFAULT_GAP_MINUTES;
    }
    TransportMode mode = from.transportModeToNext() == null
      ? defaultMode
      : from.transportModeToNext();
    double distanceKm = haversineKm(from, to) * ROAD_FACTOR;
    long minutes =
      Math.round(((distanceKm / mode.speedKmh()) * MINUTES_PER_HOUR) / GAP_ROUNDING_MINUTES) *
      GAP_ROUNDING_MINUTES;
    return (int) Math.max(MIN_GAP_MINUTES, Math.min(MAX_GAP_MINUTES, minutes));
  }

  private static boolean hasCoordinates(Stop stop) {
    return stop.latitude() != null && stop.longitude() != null;
  }

  /** Distancia en linea recta entre dos paradas, en kilometros. */
  private static double haversineKm(Stop from, Stop to) {
    double fromLatitude = Math.toRadians(from.latitude());
    double toLatitude = Math.toRadians(to.latitude());
    double deltaLatitude = toLatitude - fromLatitude;
    double deltaLongitude = Math.toRadians(to.longitude() - from.longitude());
    double sinLatitude = Math.sin(deltaLatitude / 2);
    double sinLongitude = Math.sin(deltaLongitude / 2);
    double haversine =
      sinLatitude * sinLatitude +
      Math.cos(fromLatitude) * Math.cos(toLatitude) * sinLongitude * sinLongitude;
    return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(haversine));
  }

  private static int minuteOfDay(LocalDateTime moment) {
    return minuteOfDay(moment.toLocalTime());
  }

  private static int minuteOfDay(LocalTime time) {
    return time.toSecondOfDay() / 60;
  }
}
