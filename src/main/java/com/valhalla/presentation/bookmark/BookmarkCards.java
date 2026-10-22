package com.valhalla.presentation.bookmark;

import com.valhalla.domain.bookmark.BookmarkEntry;
import com.valhalla.domain.bookmark.BookmarkStatus;
import com.valhalla.presentation.plan.PlanFeedCards;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * [MARK] Modelo Thymeleaf de una card de Guardados.
 *
 * <p>Traduce el read model del dominio a lo unico que la vista necesita: los textos del badge y
 * del tooltip son copy de presentacion, no reglas de negocio. El estado se muestra con la misma
 * card en todos los casos; la vista decide si abrir es un link o no con {@code openable}.
 */
public final class BookmarkCards {

  /** AC-11/AC-12: mismo aviso para "Eliminado" y "Privado", la salida es clonar. */
  public static final String UNAVAILABLE_TOOLTIP =
    "El plan original ya no esta disponible. Podes clonarlo para seguir usandolo.";

  private BookmarkCards() {}

  public static Map<String, Object> card(BookmarkEntry bookmark) {
    Map<String, Object> model = new LinkedHashMap<>();
    model.put("planId", bookmark.planId());
    model.put("bookmarkId", bookmark.bookmarkId());
    model.put("name", bookmark.name());
    model.put("description", bookmark.description());
    model.put("eventDate", bookmark.eventDate() == null ? null : bookmark.eventDate().toString());
    model.put("placeCount", bookmark.placeCount());
    model.put("administratorId", bookmark.administratorId());
    model.put("administratorName", bookmark.administratorName());
    model.put("cover", PlanFeedCards.coverFor(bookmark.planId()));
    model.put("openable", bookmark.openable());
    model.put("publicPlan", bookmark.publicPlan());
    model.put("badge", badgeOf(bookmark.status()));
    model.put("unavailableTooltip", bookmark.openable() ? null : UNAVAILABLE_TOOLTIP);
    return model;
  }

  /** Etiqueta visible del estado; un plan accesible no lleva badge. */
  private static String badgeOf(BookmarkStatus status) {
    return switch (status) {
      case DELETED -> "Eliminado";
      case PRIVATE -> "Privado";
      case AVAILABLE -> null;
    };
  }
}
