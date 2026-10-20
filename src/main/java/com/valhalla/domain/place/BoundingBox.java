package com.valhalla.domain.place;

import java.util.Locale;

/**
 * Caja geografica que acota el importador: el area visible del mapa. Los bordes llevan el mismo
 * nombre que usa el proxy ({@code south}, {@code west}, {@code north}, {@code east}) para que
 * vista, controller y cliente hablen el mismo idioma.
 *
 * <p>Valida en el constructor porque el bbox llega por query string: una caja invertida o fuera de
 * rango tiene que morir en un 400 del proxy y no en una consulta rara a Overpass.
 */
public record BoundingBox(double south, double west, double north, double east) {
  /**
   * CABA segun el bounding box de la relation 3082668 en Nominatim. El importador no sale de la
   * ciudad: la vista se recorta contra esta caja antes de consultar.
   */
  public static final BoundingBox CABA = new BoundingBox(-34.7058, -58.5315, -34.5266, -58.3351);

  private static final double MIN_LATITUDE = -90;
  private static final double MAX_LATITUDE = 90;
  private static final double MIN_LONGITUDE = -180;
  private static final double MAX_LONGITUDE = 180;

  /** Decimales del cache: ~100 m, para que dos vistas casi iguales compartan la consulta. */
  private static final int CACHE_SCALE = 1_000;

  public BoundingBox {
    requireRange(south, MIN_LATITUDE, MAX_LATITUDE);
    requireRange(north, MIN_LATITUDE, MAX_LATITUDE);
    requireRange(west, MIN_LONGITUDE, MAX_LONGITUDE);
    requireRange(east, MIN_LONGITUDE, MAX_LONGITUDE);
    if (south >= north || west >= east) {
      throw new IllegalArgumentException(
        "Inverted bounding box: " + south + "," + west + "," + north + "," + east
      );
    }
  }

  /**
   * Recorte contra otra caja, para quedarse dentro de CABA.
   *
   * @return la interseccion, o {@code null} si no se tocan (la vista quedo fuera de la ciudad)
   */
  public BoundingBox intersection(BoundingBox other) {
    double clippedSouth = Math.max(south, other.south);
    double clippedWest = Math.max(west, other.west);
    double clippedNorth = Math.min(north, other.north);
    double clippedEast = Math.min(east, other.east);
    if (clippedSouth >= clippedNorth) {
      return null;
    }
    if (clippedWest >= clippedEast) {
      return null;
    }
    return new BoundingBox(clippedSouth, clippedWest, clippedNorth, clippedEast);
  }

  /** Orden que pide Overpass QL: {@code (sur,oeste,norte,este)}. Sin locale entre medio. */
  public String toOverpass() {
    return String.format(Locale.ROOT, "%.6f,%.6f,%.6f,%.6f", south, west, north, east);
  }

  /** Clave estable para el cache: redondea cada borde para no guardar una entrada por pixel. */
  public String cacheKey() {
    return (
      Math.round(south * CACHE_SCALE) +
      "," +
      Math.round(west * CACHE_SCALE) +
      "," +
      Math.round(north * CACHE_SCALE) +
      "," +
      Math.round(east * CACHE_SCALE)
    );
  }

  /** {@code !(a >= min && a <= max)} rechaza tambien NaN e infinito, sin ramas extra. */
  private static void requireRange(double value, double min, double max) {
    if (!(value >= min && value <= max)) {
      throw new IllegalArgumentException("Coordinate out of range: " + value);
    }
  }
}
