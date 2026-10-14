package com.valhalla.presentation.plan;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.user.User;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * [PPV] Tarjetas del feed de planes publicos, compartidas entre el modelo de {@code GET /for-you} y
 * el payload JSON de {@code GET /api/plans/public} (el sidebar de /explore).
 *
 * <p>Ni shortCode, ni emails, ni participantes: solo los campos publicos de la tarjeta.
 */
public final class PlanFeedCards {

  /** Cantidad de covers genericos en /images/plans: el id del plan elige uno, deterministico. */
  private static final int COVER_COUNT = 6;

  private PlanFeedCards() {}

  /**
   * [PPV, AC-03/AC-09] Payload de la tarjeta: nombre, descripcion, fecha, cantidad de lugares del
   * itinerario y nombre del administrador. {@code cover} es la imagen generica del plan (hashCode
   * del id % COVER_COUNT); a futuro, una columna {@code imageUrl} en Plan la reemplaza.
   */
  public record Card(
    UUID id,
    String name,
    String description,
    String eventDate,
    int placeCount,
    String administratorName,
    String cover
  ) {}

  /** Construye la tarjeta del plan: unica fuente de campos para feed y API. */
  public static Card card(Plan plan) {
    return new Card(
      plan.getId(),
      plan.getName(),
      plan.getDescription(),
      plan.getEventDate() == null ? null : plan.getEventDate().toString(),
      plan.getPlanPlaces().size(),
      feedDisplayName(plan.getAdministrator()),
      "/images/plans/cover-" + (Math.floorMod(plan.getId().hashCode(), COVER_COUNT) + 1) + ".svg"
    );
  }

  /**
   * Misma tarjeta como mapa, con las claves en orden, para el modelo Thymeleaf de {@code /for-you}
   * (la vista y sus tests leen claves, no componentes de record).
   */
  static Map<String, Object> planCard(Plan plan) {
    Card card = card(plan);
    Map<String, Object> model = new LinkedHashMap<>();
    model.put("id", card.id());
    model.put("name", card.name());
    model.put("description", card.description());
    model.put("eventDate", card.eventDate());
    model.put("placeCount", card.placeCount());
    model.put("administratorName", card.administratorName());
    model.put("cover", card.cover());
    return model;
  }

  /**
   * [PPV] Nombre visible del administrador en el feed. Sin nombre registrado queda vacio y la vista
   * oculta la linea: AC-09 prohibe exponer emails en el listado, a diferencia del helper de la
   * vista publica del plan, que usa el email como ultimo recurso.
   */
  private static String feedDisplayName(User user) {
    if (user == null) {
      return "";
    }
    String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
    String last = user.getLastName() == null ? "" : user.getLastName().trim();
    return (first + " " + last).trim();
  }
}
