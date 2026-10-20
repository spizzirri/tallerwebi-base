package com.valhalla.domain.plan;

import com.valhalla.domain.place.PlaceCategory;
import java.util.Locale;

/** [SEARCH] Filtros y orden del feed, ya normalizados. Los parametros crudos no llegan al dominio. */
public record PlanDiscoveryQuery(String text, PlaceCategory category, When when, Sort sort) {
  public enum When {
    ALL,
    UPCOMING,
    PAST,
  }

  public enum Sort {
    POPULAR,
    DATE,
    NAME,
  }

  /** Parseo tolerante: parametro vacio o invalido cae al default, nunca lanza. */
  public static PlanDiscoveryQuery parse(String text, String category, String when, String sort) {
    return new PlanDiscoveryQuery(
      text == null ? "" : text.trim(),
      parseCategory(category),
      parseWhen(when),
      parseSort(sort)
    );
  }

  /** Hay filtro activo (el orden no cuenta como filtro): decide el estado vacio de AC-09. */
  public boolean hasFilters() {
    return !text.isEmpty() || category != null || when != When.ALL;
  }

  private static PlaceCategory parseCategory(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    return PlaceCategory.fromCode(raw.trim().toUpperCase(Locale.ROOT)).orElse(null);
  }

  private static When parseWhen(String raw) {
    if (raw == null || raw.isBlank()) {
      return When.ALL;
    }
    return switch (raw.trim().toLowerCase(Locale.ROOT)) {
      case "upcoming" -> When.UPCOMING;
      case "past" -> When.PAST;
      default -> When.ALL;
    };
  }

  private static Sort parseSort(String raw) {
    if (raw == null || raw.isBlank()) {
      return Sort.POPULAR;
    }
    return switch (raw.trim().toLowerCase(Locale.ROOT)) {
      case "date" -> Sort.DATE;
      case "name" -> Sort.NAME;
      default -> Sort.POPULAR;
    };
  }
}
