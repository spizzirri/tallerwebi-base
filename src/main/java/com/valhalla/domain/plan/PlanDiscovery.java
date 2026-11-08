package com.valhalla.domain.plan;

import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.rating.RatingSummary;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** [TOP] Ranking de popularidad del feed: funcion pura, sin Spring y sin acceso a datos. */
public final class PlanDiscovery {

  /** Votos virtuales del prior: cuanto pesa el promedio inicial frente a los votos reales. */
  static final double PRIOR_VOTES = 5.0;
  /** Promedio del prior: un plan sin votos arranca aca. */
  static final double PRIOR_MEAN = 3.7;

  /** [PABM] Email del usuario sistema dueno de los planes que se crean desde el admin. */
  public static final String PLANIT_EMAIL = "app@planit.com";

  private PlanDiscovery() {}

  /**
   * [PABM] Regla de producto: los planes de PlanIt van siempre primero, en todos los ordenes y con
   * cualquier filtro. Dentro de cada grupo se conserva el orden propio de la lista. Un plan sin
   * administrador no es de PlanIt y nunca rompe la comparacion.
   */
  public static Comparator<Plan> planItFirst() {
    return Comparator.comparingInt((Plan plan) -> isPlanIt(plan) ? 0 : 1);
  }

  /** [PABM] Plan creado por el usuario sistema del admin, reconocido por el email del dueno. */
  public static boolean isPlanIt(Plan plan) {
    return (
      plan != null &&
      plan.getAdministrator() != null &&
      PLANIT_EMAIL.equalsIgnoreCase(plan.getAdministrator().getEmail())
    );
  }

  /**
   * [PPV] Orden por fecha del sidebar publico: PlanIt primero y, dentro de cada grupo, la misma
   * fecha ascendente con nulos al final que ya usaba el endpoint.
   */
  public static Comparator<Plan> publicDateOrder() {
    return planItFirst().thenComparing(eventDateOrder());
  }

  /** [TOP] Promedio bayesiano: el prior pesa menos a medida que el plan junta votos. */
  public static double popularityScore(Double average, Long count) {
    long votes = count == null ? 0L : count;
    double sum = average == null ? 0.0 : average * votes;
    return (PRIOR_VOTES * PRIOR_MEAN + sum) / (PRIOR_VOTES + votes);
  }

  /**
   * Orden de popularidad: PlanIt primero y, dentro de cada grupo, calificados antes que sin
   * calificar, despues score descendente, participantes descendentes, fecha ascendente (nulos al
   * final), nombre y id.
   */
  public static Comparator<Plan> popularityOrder(
    Map<UUID, RatingSummary> summaries,
    Map<UUID, Long> participantCounts
  ) {
    return planItFirst()
      .thenComparingInt(plan -> hasRatings(plan, summaries) ? 0 : 1)
      .thenComparing(
        Comparator.comparingDouble((Plan plan) ->
          -popularityScore(average(plan, summaries), votes(plan, summaries))
        )
      )
      .thenComparing(
        Comparator.comparingLong((Plan plan) -> -participants(plan, participantCounts))
      )
      .thenComparing(Plan::getEventDate, Comparator.nullsLast(Comparator.naturalOrder()))
      .thenComparing(plan -> normalize(plan.getName()))
      .thenComparing(Plan::getId);
  }

  /**
   * [TOP] Copia ordenada de {@code plans}: {@code stream().sorted(...).toList()} nunca reordena la
   * lista que devuelve el repositorio.
   */
  public static List<Plan> orderByPopularity(
    List<Plan> plans,
    Map<UUID, RatingSummary> summaries,
    Map<UUID, Long> participantCounts
  ) {
    return plans.stream().sorted(popularityOrder(summaries, participantCounts)).toList();
  }

  /**
   * [SEARCH] Copia filtrada y ordenada de {@code plans}: texto, categoria y ventana de fecha se
   * combinan con AND antes de aplicar el orden. {@code today} entra por parametro para no depender
   * del reloj.
   */
  public static List<Plan> discover(
    List<Plan> plans,
    Map<UUID, RatingSummary> summaries,
    Map<UUID, Long> participantCounts,
    PlanDiscoveryQuery query,
    LocalDate today
  ) {
    return plans
      .stream()
      .filter(plan -> matchesText(plan, query.text()))
      .filter(plan -> matchesCategory(plan, query.category()))
      .filter(plan -> matchesWhen(plan, query.when(), today))
      .sorted(orderFor(query.sort(), summaries, participantCounts))
      .toList();
  }

  private static Comparator<Plan> orderFor(
    PlanDiscoveryQuery.Sort sort,
    Map<UUID, RatingSummary> summaries,
    Map<UUID, Long> participantCounts
  ) {
    return switch (sort) {
      case DATE -> planItFirst().thenComparing(eventDateOrder());
      case NAME -> planItFirst().thenComparing(nameOrder());
      case POPULAR -> popularityOrder(summaries, participantCounts);
    };
  }

  private static Comparator<Plan> eventDateOrder() {
    return Comparator
      .comparing(
        (Plan plan) -> plan.getEventDate(),
        Comparator.nullsLast(Comparator.naturalOrder())
      )
      .thenComparing(plan -> normalize(plan.getName()))
      .thenComparing(Plan::getId);
  }

  private static Comparator<Plan> nameOrder() {
    return Comparator
      .comparing((Plan plan) -> normalize(plan.getName()))
      .thenComparing(
        (Plan plan) -> plan.getEventDate(),
        Comparator.nullsLast(Comparator.naturalOrder())
      )
      .thenComparing(Plan::getId);
  }

  private static boolean matchesText(Plan plan, String text) {
    if (text.isEmpty()) {
      return true;
    }
    String needle = text.toLowerCase(Locale.ROOT);
    return (
      normalize(plan.getName()).contains(needle) ||
      normalize(plan.getDescription()).contains(needle)
    );
  }

  private static boolean matchesCategory(Plan plan, PlaceCategory category) {
    return (
      category == null ||
      plan
        .getPlanPlaces()
        .stream()
        .anyMatch(entry -> entry.getPlace() != null && entry.getPlace().getCategory() == category)
    );
  }

  private static boolean matchesWhen(Plan plan, PlanDiscoveryQuery.When when, LocalDate today) {
    return switch (when) {
      case ALL -> true;
      case UPCOMING -> plan.getEventDate() != null && !plan.getEventDate().isBefore(today);
      case PAST -> plan.getEventDate() != null && plan.getEventDate().isBefore(today);
    };
  }

  private static boolean hasRatings(Plan plan, Map<UUID, RatingSummary> summaries) {
    RatingSummary summary = summaries.get(plan.getId());
    return summary != null && summary.count() != null && summary.count() > 0;
  }

  private static Double average(Plan plan, Map<UUID, RatingSummary> summaries) {
    RatingSummary summary = summaries.get(plan.getId());
    return summary == null ? null : summary.average();
  }

  private static Long votes(Plan plan, Map<UUID, RatingSummary> summaries) {
    RatingSummary summary = summaries.get(plan.getId());
    // `count` es Long: un agregado sin votos puede traer null y el score lo descompondria.
    return summary == null || summary.count() == null ? 0L : summary.count();
  }

  private static long participants(Plan plan, Map<UUID, Long> participantCounts) {
    return participantCounts.getOrDefault(plan.getId(), 0L);
  }

  private static String normalize(String value) {
    return value == null ? "" : value.toLowerCase(Locale.ROOT);
  }
}
