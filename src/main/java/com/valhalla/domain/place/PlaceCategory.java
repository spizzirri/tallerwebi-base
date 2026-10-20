package com.valhalla.domain.place;

import java.util.Locale;
import java.util.Optional;

public enum PlaceCategory {
  RESTAURANT,
  BAR,
  CAFE,
  MUSEUM,
  PARK,
  SHOPPING,
  NIGHTLIFE,
  CULTURE,
  SPORT,
  OTHER;

  public static Optional<PlaceCategory> fromCode(String code) {
    if (code == null || code.isBlank()) {
      return Optional.empty();
    }
    try {
      return Optional.of(valueOf(code));
    } catch (IllegalArgumentException exception) {
      return Optional.empty();
    }
  }

  /**
   * Maps an OSM tag pair (like {@code amenity=restaurant}) to a category. Inputs are trimmed and
   * lower-cased so case or padding variants from the importer map to the same branch; anything
   * without an explicit mapping falls back to {@link #OTHER} (the admin can always change it).
   */
  public static PlaceCategory fromOsm(String key, String value) { // NOPMD — intentional: the switch is a declarative mapping table (each case is an OSM tag, not a decision point); 21 vs the methodReportLevel=10, but every branch is a one-liner covered by U-01..U-04.
    if (key == null || value == null) {
      return OTHER;
    }
    String osmKey = key.trim().toLowerCase(Locale.ROOT);
    String osmValue = value.trim().toLowerCase(Locale.ROOT);
    return switch (osmKey + "=" + osmValue) {
      case "amenity=restaurant", "amenity=fast_food" -> RESTAURANT;
      case "amenity=bar", "amenity=pub" -> BAR;
      case "amenity=cafe" -> CAFE;
      case "amenity=nightclub" -> NIGHTLIFE;
      case "amenity=museum" -> MUSEUM;
      case "amenity=theatre",
        "amenity=cinema",
        "amenity=arts_centre",
        "amenity=library",
        "amenity=planetarium" -> CULTURE;
      case "amenity=park", "amenity=garden", "amenity=playground" -> PARK;
      case "amenity=fitness_centre", "amenity=sport_centre" -> SPORT;
      default -> "shop".equals(osmKey) ? SHOPPING : OTHER;
    };
  }
}
