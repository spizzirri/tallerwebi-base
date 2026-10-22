package com.valhalla.presentation.plan;

import com.valhalla.domain.bookmark.PlanBookmarkService;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.rating.PlanRatingService;
import com.valhalla.domain.rating.RatingSummary;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
public class PlanRestController {

  /** Ventana del sidebar de /explore: los próximos N planes públicos. */
  private static final int PUBLIC_PLANS_LIMIT = 5;

  private final PlanService planService;
  private final PlanRatingService planRatingService;
  private final PlanBookmarkService planBookmarkService;

  public PlanRestController(
    PlanService planService,
    PlanRatingService planRatingService,
    PlanBookmarkService planBookmarkService
  ) {
    this.planService = planService;
    this.planRatingService = planRatingService;
    this.planBookmarkService = planBookmarkService;
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
    // [CAL] El mismo agregado del feed Thymeleaf, una query para las cinco tarjetas:
    // el JSON del sidebar expone el promedio, nunca la autoria de las calificaciones.
    Map<UUID, RatingSummary> summaries = this.planRatingService.getSummariesByPlanId();
    // [MARK] El sidebar pinta el icono desde este flag: un lookup para las cinco tarjetas y
    // ninguno sin sesion (el anonimo nunca ve el toggle).
    boolean signedIn =
      principal instanceof Authentication authentication &&
      authentication.getPrincipal() instanceof UserDetails;
    Set<UUID> bookmarkedPlanIds = signedIn
      ? this.planBookmarkService.getBookmarkedPlanIds(currentEmail)
      : Set.of();
    return planService
      .getPublicPlans()
      .stream()
      .filter(plan -> Boolean.TRUE.equals(plan.getIsPublic()))
      .filter(plan -> currentEmail == null || !plan.isAdministrator(currentEmail))
      .sorted(
        Comparator.comparing(Plan::getEventDate, Comparator.nullsLast(Comparator.naturalOrder()))
      )
      .limit(PUBLIC_PLANS_LIMIT)
      .map(plan ->
        PlanFeedCards.card(
          plan,
          summaries.getOrDefault(plan.getId(), new RatingSummary(plan.getId(), null, 0L)),
          bookmarkedPlanIds.contains(plan.getId())
        )
      )
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
    UUID id,
    UUID placeId,
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
