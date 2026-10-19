package com.valhalla.presentation.plan;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.rating.PlanRatingService;
import com.valhalla.domain.rating.RatingSummary;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
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

  @GetMapping("/for-you")
  public ModelAndView forYou(Authentication authentication) {
    // Con anonymousAuthenticationFilter encendido el principal es un AnonymousAuthenticationToken
    // (nombre "anonymousUser"), no null: ningun plan tiene ese administrador, asi que el filtro de
    // propios deja pasar todo igual. El corte por null cubre el caso sin anonymous habilitado.
    String currentEmail = authentication == null ? null : authentication.getName();
    // [CAL] Un solo agregado agrupado alimenta el indicador de todas las tarjetas (sin N+1).
    Map<UUID, RatingSummary> summaries = this.planRatingService.getSummariesByPlanId();
    Map<String, Object> model = new ModelMap();
    model.put(
      ATTR_PUBLIC_PLANS,
      planService
        .getPublicPlans()
        .stream()
        .filter(plan -> currentEmail == null || !plan.isAdministrator(currentEmail))
        .map(plan -> PlanFeedCards.planCard(plan, summaries))
        .toList()
    );
    return new ModelAndView(VIEW_PUBLIC_LIST, model);
  }
}
