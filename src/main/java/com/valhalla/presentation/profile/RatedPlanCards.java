package com.valhalla.presentation.profile;

import com.valhalla.domain.rating.RatedPlanView;
import com.valhalla.domain.rating.RatingSummary;
import com.valhalla.presentation.plan.PlanFeedCards;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * [MYRAT] Modelo Thymeleaf de una fila de "Mis calificaciones".
 *
 * <p>Traduce el read model del dominio a lo que la vista necesita: el puntaje propio, el agregado
 * global del plan y el copy del badge. El badge es copy de presentacion, no una regla de negocio:
 * la accesibilidad ya viene resuelta en el {@link RatedPlanView}.
 */
public final class RatedPlanCards {

  /** AC-06: el plan calificado dejo de ser accesible; la fila queda con el nombre y sin link. */
  public static final String UNAVAILABLE_BADGE = "No disponible";

  private RatedPlanCards() {}

  public static Map<String, Object> row(RatedPlanView view, RatingSummary summary) {
    Map<String, Object> model = new LinkedHashMap<>();
    model.put("planId", view.planId());
    model.put("name", view.planName());
    model.put("score", view.score());
    model.put("accessible", view.accessible());
    model.put("member", view.member());
    model.put("ratingAverage", PlanFeedCards.roundOneDecimal(summary.average()));
    model.put("ratingCount", summary.count());
    model.put("badge", view.accessible() ? null : UNAVAILABLE_BADGE);
    return model;
  }
}
