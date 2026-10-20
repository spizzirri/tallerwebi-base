package com.valhalla.presentation.plan;

import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanDiscovery;
import com.valhalla.domain.plan.PlanDiscoveryQuery;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.rating.PlanRatingService;
import com.valhalla.domain.rating.RatingSummary;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * [PPV] Feed "Para vos" en {@code GET /for-you}: planes publicos para descubrir, con o sin sesion.
 *
 * <p>Esta ruta vive fuera de {@code PlanController} porque ese controller esta amarrado a
 * {@code @RequestMapping("/plans")} y el feed ya no es una subruta de /plans. Sin sesion se muestran
 * todos los planes publicos (el filtro de propios no aplica: el anonimo no tiene planes propios);
 * con sesion los propios quedan fuera, porque el feed es para descubrir planes de otros.
 */
@Controller
public class ForYouController {

  private static final String VIEW_PUBLIC_LIST = "pages/plans/public-list";
  private static final String ATTR_PUBLIC_PLANS = "publicPlans";

  private final PlanService planService;
  private final PlanRatingService planRatingService;

  @Autowired
  public ForYouController(PlanService planService, PlanRatingService planRatingService) {
    this.planService = planService;
    this.planRatingService = planRatingService;
  }

  /**
   * [SEARCH] Feed filtrado y ordenado: {@code q}, {@code category}, {@code when} y {@code sort}
   * viajan en el query string y se parsean de forma tolerante, asi un parametro invalido nunca
   * rompe la pagina. Los valores aplicados vuelven en el modelo para que el formulario los refleje.
   */
  @GetMapping("/for-you")
  public ModelAndView forYou(
    Authentication authentication,
    @RequestParam(name = "q", required = false) String queryText,
    @RequestParam(required = false) String category,
    @RequestParam(required = false) String when,
    @RequestParam(required = false) String sort
  ) {
    // Con anonymousAuthenticationFilter encendido el principal es un AnonymousAuthenticationToken
    // (nombre "anonymousUser"), no null: ningun plan tiene ese administrador, asi que el filtro de
    // propios deja pasar todo igual. El corte por null cubre el caso sin anonymous habilitado.
    String currentEmail = authentication == null ? null : authentication.getName();
    PlanDiscoveryQuery query = PlanDiscoveryQuery.parse(queryText, category, when, sort);
    List<Plan> publicPlans = planService
      .getPublicPlans()
      .stream()
      .filter(plan -> currentEmail == null || !plan.isAdministrator(currentEmail))
      .toList();
    // [CAL] Un solo agregado agrupado alimenta el indicador de todas las tarjetas (sin N+1).
    Map<UUID, RatingSummary> summaries = this.planRatingService.getSummariesByPlanId();
    // [TOP] Participantes solo de los planes que llegan al descubrimiento: el conteo es LAZY y por
    // eso va por query, mientras los lugares ya vienen con el plan.
    Map<UUID, Long> participantCounts =
      this.planService.getParticipantCounts(publicPlans.stream().map(Plan::getId).toList());
    Map<String, Object> model = new ModelMap();
    model.put(
      ATTR_PUBLIC_PLANS,
      PlanDiscovery
        .discover(publicPlans, summaries, participantCounts, query, LocalDate.now())
        .stream()
        .map(plan -> PlanFeedCards.planCard(plan, summaries))
        .toList()
    );
    model.put("q", query.text());
    model.put("selectedCategory", query.category() == null ? "" : query.category().name());
    model.put("selectedWhen", query.when().name().toLowerCase(Locale.ROOT));
    model.put("selectedSort", query.sort().name().toLowerCase(Locale.ROOT));
    model.put("filtersApplied", query.hasFilters());
    model.put("categories", PlaceCategory.values());
    return new ModelAndView(VIEW_PUBLIC_LIST, model);
  }
}
