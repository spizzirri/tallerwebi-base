package com.valhalla.presentation.plan;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.rating.RatingSummary;
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
   *
   * <p>[CAL] {@code ratingAverage}/{code ratingCount} es el agregado publico de calificaciones
   * (16-CAL.md, AC-10): la autoria es interna, del feed solo sale el promedio y la cantidad.
   *
   * <p>[MARK] {@code bookmarked} es el estado del usuario de la sesion sobre este plan. No es un
   * dato del plan: viaja en la tarjeta para que el icono se pinte sin un pedido extra por card.
   */
  public record Card(
    UUID id,
    String name,
    String description,
    String eventDate,
    int placeCount,
    String administratorName,
    UUID administratorId,
    String cover,
    Double ratingAverage,
    Long ratingCount,
    boolean bookmarked
  ) {}

  /** Construye la tarjeta con el agregado de calificaciones y el estado de Guardados. */
  public static Card card(Plan plan, RatingSummary summary, boolean bookmarked) {
    return new Card(
      plan.getId(),
      plan.getName(),
      plan.getDescription(),
      plan.getEventDate() == null ? null : plan.getEventDate().toString(),
      plan.getPlanPlaces().size(),
      feedDisplayName(plan.getAdministrator()),
      plan.getAdministrator() == null ? null : plan.getAdministrator().getId(),
      coverFor(plan.getId()),
      roundOneDecimal(summary.average()),
      summary.count(),
      bookmarked
    );
  }

  /**
   * Misma tarjeta como mapa, con las claves en orden, para el modelo Thymeleaf de {@code /for-you}
   * (la vista y sus tests leen claves, no componentes de record). Los summaries de calificaciones
   * arrive solo via ports/mocks: ver 16-CAL.md.
   */
  public static Map<String, Object> planCard(
    Plan plan,
    Map<UUID, RatingSummary> summaries,
    boolean bookmarked
  ) {
    RatingSummary summary = summaries.getOrDefault(
      plan.getId(),
      new RatingSummary(plan.getId(), null, 0L)
    );
    return modelOf(card(plan, summary, bookmarked));
  }

  /**
   * Modelo Thymeleaf plano de una Card ya armada: la unica fuente de claves del feed. Los tests de
   * [PPV] fijan la lista exacta, agregar una clave aca es un cambio de contrato.
   */
  static Map<String, Object> modelOf(Card card) {
    Map<String, Object> model = new LinkedHashMap<>();
    model.put("id", card.id());
    model.put("name", card.name());
    model.put("description", card.description());
    model.put("eventDate", card.eventDate());
    model.put("placeCount", card.placeCount());
    model.put("administratorName", card.administratorName());
    model.put("administratorId", card.administratorId());
    model.put("cover", card.cover());
    model.put("ratingAverage", card.ratingAverage());
    model.put("ratingCount", card.ratingCount());
    model.put("bookmarked", card.bookmarked());
    return model;
  }

  /** Promedio con un solo decimal: el agregado se muestra redondeado, no se re-calcula. */
  public static Double roundOneDecimal(Double average) {
    if (average == null) {
      return null;
    }
    return Math.round(average * 10.0) / 10.0;
  }

  /**
   * [MARK] Cover generico del plan, deterministico por id. Lo comparten las cards del feed y las
   * de Guardados: el guardado no persiste una imagen, la elige el id del plan.
   */
  public static String coverFor(UUID planId) {
    return "/images/plans/cover-" + (Math.floorMod(planId.hashCode(), COVER_COUNT) + 1) + ".svg";
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
    String first = user.getFirstName() == null ? "" : user.getFirstName();
    String last = user.getLastName() == null ? "" : user.getLastName();
    return (first + " " + last).trim();
  }
}
