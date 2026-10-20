package com.valhalla.domain.plan;

import com.valhalla.domain.rating.RatingSummary;
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

  private PlanDiscovery() {}

  /** [TOP] Promedio bayesiano: el prior pesa menos a medida que el plan junta votos. */
  public static double popularityScore(Double average, Long count) {
    long votes = count == null ? 0L : count;
    double sum = average == null ? 0.0 : average * votes;
    return (PRIOR_VOTES * PRIOR_MEAN + sum) / (PRIOR_VOTES + votes);
  }

  /**
   * Orden de popularidad: calificados antes que sin calificar, despues score descendente,
   * participantes descendentes, fecha ascendente (nulos al final), nombre y id.
   */
  public static Comparator<Plan> popularityOrder(
    Map<UUID, RatingSummary> summaries,
    Map<UUID, Long> participantCounts
  ) {
    return Comparator
      .comparingInt((Plan plan) -> hasRatings(plan, summaries) ? 0 : 1)
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
    return summary == null ? 0L : summary.count();
  }

  private static long participants(Plan plan, Map<UUID, Long> participantCounts) {
    return participantCounts.getOrDefault(plan.getId(), 0L);
  }

  private static String normalize(String value) {
    return value == null ? "" : value.toLowerCase(Locale.ROOT);
  }
}
