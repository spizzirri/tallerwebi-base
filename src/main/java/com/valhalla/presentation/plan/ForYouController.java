package com.valhalla.presentation.plan;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import java.util.Map;
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

  @Autowired
  public ForYouController(PlanService planService) {
    this.planService = planService;
  }

  @GetMapping("/for-you")
  public ModelAndView forYou(Authentication authentication) {
    // Con anonymousAuthenticationFilter encendido el principal es un AnonymousAuthenticationToken
    // (nombre "anonymousUser"), no null: ningun plan tiene ese administrador, asi que el filtro de
    // propios deja pasar todo igual. El corte por null cubre el caso sin anonymous habilitado.
    String currentEmail = authentication == null ? null : authentication.getName();
    Map<String, Object> model = new ModelMap();
    model.put(
      ATTR_PUBLIC_PLANS,
      planService
        .getPublicPlans()
        .stream()
        .filter(plan -> currentEmail == null || !plan.isAdministrator(currentEmail))
        .map(PlanFeedCards::planCard)
        .toList()
    );
    return new ModelAndView(VIEW_PUBLIC_LIST, model);
  }
}
