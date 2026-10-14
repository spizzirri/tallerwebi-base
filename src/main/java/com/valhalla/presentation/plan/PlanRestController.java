package com.valhalla.presentation.plan;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
public class PlanRestController {

  /** Ventana del sidebar de /explore: los próximos N planes públicos. */
  private static final int PUBLIC_PLANS_LIMIT = 5;

  private final PlanService planService;

  public PlanRestController(PlanService planService) {
    this.planService = planService;
  }

  @GetMapping
  public List<PlanSummary> getUserPlans(Principal principal) {
    if (principal == null) {
      return List.of();
    }
    return planService
      .getPlansByUserEmail(principal.getName())
      .stream()
      .map(PlanSummary::from)
      .toList();
  }

  /**
   * Feed del sidebar de /explore: planes públicos próximos, visibles con o sin sesión. Sin fecha
   * van al final y el llamador no ve los propios (misma regla que {@link ForYouController}); el
   * principal null o anónimo no filtra nada.
   */
  @GetMapping("/public")
  public List<PlanFeedCards.Card> getPublicPlans(Principal principal) {
    String currentEmail = principal == null ? null : principal.getName();
    return planService
      .getPublicPlans()
      .stream()
      .filter(plan -> Boolean.TRUE.equals(plan.getIsPublic()))
      .filter(plan -> currentEmail == null || !plan.isAdministrator(currentEmail))
      .sorted(
        Comparator.comparing(Plan::getEventDate, Comparator.nullsLast(Comparator.naturalOrder()))
      )
      .limit(PUBLIC_PLANS_LIMIT)
      .map(PlanFeedCards::card)
      .toList();
  }

  public record PlanSummary(
    UUID id,
    String name,
    String eventDate,
    Boolean isPublic,
    List<PlanPlaceSummary> planPlaces
  ) {
    static PlanSummary from(Plan plan) {
      return new PlanSummary(
        plan.getId(),
        plan.getName(),
        plan.getEventDate() == null ? null : plan.getEventDate().toString(),
        plan.getIsPublic(),
        plan.getPlanPlaces().stream().map(PlanPlaceSummary::from).toList()
      );
    }
  }

  public record PlanPlaceSummary(
    Long id,
    Long placeId,
    String name,
    String category,
    Double latitude,
    Double longitude
  ) {
    static PlanPlaceSummary from(PlanPlace pp) {
      return new PlanPlaceSummary(
        pp.getId(),
        pp.getPlace().getId(),
        pp.getPlace().getName(),
        pp.getPlace().getCategory() == null ? null : pp.getPlace().getCategory().name(),
        pp.getPlace().getLatitude(),
        pp.getPlace().getLongitude()
      );
    }
  }
}
