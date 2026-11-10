package com.valhalla.domain.user;

import java.text.Normalizer;
import java.util.Locale;

/**
 * [USEARCH] Normalizacion del texto de busqueda de usuarios.
 *
 * <p>El nombre y el apellido se guardan tal como los escribio cada persona, con mayusculas y
 * acentos. Para que "jose" encuentre a "José", el servicio y la query normalizan los dos lados:
 * el texto que llega se descompone, se le quitan las marcas diacriticas y se pasa a minusculas.
 * La query hace lo propio en SQL con {@code translate} + {@code lower}, porque {@code LIKE} solo
 * es case-insensitive.
 */
public final class UserSearchText {

  private static final String COMBINING_MARKS = "\\p{M}+";

  private UserSearchText() {}

  /**
   * Texto canonico de la consulta: sin espacios de los extremos, sin acentos y en minusculas. Una
   * consulta nula o en blanco queda como cadena vacia; el servicio la traduce a "sin resultados"
   * y nunca lista a todos los usuarios.
   */
  public static String normalize(String query) {
    if (query == null) {
      return "";
    }
    String trimmed = query.trim();
    if (trimmed.isEmpty()) {
      return "";
    }
    return Normalizer
      .normalize(trimmed, Normalizer.Form.NFD)
      .replaceAll(COMBINING_MARKS, "")
      .toLowerCase(Locale.ROOT);
  }

  /**
   * Escapa los comodines de {@code LIKE} para que se busquen como texto literal. Sin esto, una
   * consulta {@code %} matchearia a todos los usuarios, que es justo lo que la busqueda no debe
   * hacer.
   */
  public static String escapeLike(String normalizedQuery) {
    return normalizedQuery.replace("!", "!!").replace("%", "!%").replace("_", "!_");
  }
}
