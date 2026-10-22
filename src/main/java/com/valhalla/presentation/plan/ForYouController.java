package com.valhalla.presentation.plan;

import com.valhalla.domain.bookmark.PlanBookmarkService;
import com.valhalla.domain.follow.FollowService;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanDiscovery;
import com.valhalla.domain.plan.PlanDiscoveryQuery;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.rating.PlanRatingService;
import com.valhalla.domain.rating.RatingSummary;
import com.valhalla.domain.user.UserService;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
 *
 * <p>[FOLLOW] El scope {@code following} es el mismo feed restringido a los autores que el usuario
 * sigue (AC-07, AC-08): mismo conjunto publico, mismos filtros y mismo orden, y solo con sesion
 * (AC-10). Los dos estados vacios propios viajan al modelo como {@code followingScope} y
 * {@code followsNobody} (AC-09).
 */
@Controller
public class ForYouController {

  private static final String VIEW_PUBLIC_LIST = "pages/plans/public-list";
  private static final String ATTR_PUBLIC_PLANS = "publicPlans";
  private static final String SCOPE_FOLLOWING = "following";
  private static final String REDIRECT_LOGIN_FOLLOWING =
    "redirect:/auth/login?redirect=/for-you%3Fscope%3Dfollowing";

  private final PlanService planService;
  private final PlanRatingService planRatingService;
  private final PlanBookmarkService planBookmarkService;
  private final FollowService followService;
  private final UserService userService;

  @Autowired
  public ForYouController(
    PlanService planService,
    PlanRatingService planRatingService,
    PlanBookmarkService planBookmarkService,
    FollowService followService,
    UserService userService
  ) {
    this.planService = planService;
    this.planRatingService = planRatingService;
    this.planBookmarkService = planBookmarkService;
    this.followService = followService;
    this.userService = userService;
  }

  /**
   * [SEARCH] Feed filtrado y ordenado: {@code q}, {@code category}, {@code when} y {@code sort}
   * viajan en el query string y se parsean de forma tolerante, asi un parametro invalido nunca
   * rompe la pagina. Los valores aplicados vuelven en el modelo para que el formulario los refleje.
   *
   * <p>[FOLLOW] {@code scope=following} (sin distinguir mayusculas) elige los planes publicos de
   * los autores seguidos; cualquier otro valor cae al feed completo. Sin sesion, el scope
   * Siguiendo rebota al login con vuelta a la misma URL (AC-10).
   */
  @GetMapping("/for-you")
  public ModelAndView forYou(
    Authentication authentication,
    @RequestParam(name = "q", required = false) String queryText,
    @RequestParam(required = false) String category,
    @RequestParam(required = false) String when,
    @RequestParam(required = false) String sort,
    @RequestParam(required = false) String scope
  ) {
    boolean followingScope = SCOPE_FOLLOWING.equalsIgnoreCase(scope == null ? "" : scope.trim());
    UUID viewerId = followingScope ? viewerId(authentication) : null;
    if (followingScope && viewerId == null) {
      return new ModelAndView(REDIRECT_LOGIN_FOLLOWING);
    }
    // Con anonymousAuthenticationFilter encendido el principal es un AnonymousAuthenticationToken
    // (nombre "anonymousUser"), no null: ningun plan tiene ese administrador, asi que el filtro de
    // propios deja pasar todo igual. El corte por null cubre el caso sin anonymous habilitado.
    String currentEmail = authentication == null ? null : authentication.getName();
    PlanDiscoveryQuery query = PlanDiscoveryQuery.parse(queryText, category, when, sort);
    List<UUID> followedIds = followingScope ? followService.followedIds(viewerId) : List.of();
    List<Plan> source = followingScope
      ? planService.getPublicPlansByAdministrators(followedIds)
      : planService.getPublicPlans();
    List<Plan> publicPlans = source
      .stream()
      .filter(plan -> currentEmail == null || !plan.isAdministrator(currentEmail))
      .toList();
    // [CAL] Un solo agregado agrupado alimenta el indicador de todas las tarjetas (sin N+1).
    Map<UUID, RatingSummary> summaries = this.planRatingService.getSummariesByPlanId();
    // [TOP] Participantes solo de los planes que llegan al descubrimiento: el conteo es LAZY y por
    // eso va por query, mientras los lugares ya vienen con el plan.
    Map<UUID, Long> participantCounts =
      this.planService.getParticipantCounts(publicPlans.stream().map(Plan::getId).toList());
    // [MARK] Un solo lookup alimenta el icono de todas las cards. Sin sesion no hay nada que
    // preguntar y el toggle no se renderiza (AC-05): no es un soft gate, es ausencia de control.
    boolean signedIn = signedIn(authentication);
    Set<UUID> bookmarkedPlanIds = bookmarkedPlanIds(authentication);
    Map<String, Object> model = new ModelMap();
    model.put(
      ATTR_PUBLIC_PLANS,
      PlanDiscovery
        .discover(publicPlans, summaries, participantCounts, query, LocalDate.now())
        .stream()
        .map(plan ->
          PlanFeedCards.planCard(plan, summaries, bookmarkedPlanIds.contains(plan.getId()))
        )
        .toList()
    );
    model.put("canBookmark", signedIn);
    model.put("q", query.text());
    model.put("selectedCategory", query.category() == null ? "" : query.category().name());
    model.put("selectedWhen", query.when().name().toLowerCase(Locale.ROOT));
    model.put("selectedSort", query.sort().name().toLowerCase(Locale.ROOT));
    model.put("filtersApplied", query.hasFilters());
    model.put("categories", PlaceCategory.values());
    // [FOLLOW] El template elige pestana y estado vacio con estas dos banderas (AC-09).
    model.put("followingScope", followingScope);
    model.put("followsNobody", followingScope && followedIds.isEmpty());
    return new ModelAndView(VIEW_PUBLIC_LIST, model);
  }

  /** Id del visitante con sesion; en anonimo el principal es el String "anonymousUser". */
  private UUID viewerId(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
      return null;
    }
    return userService.getByEmail(authentication.getName()).getId();
  }

  /** Visitante autenticado real: el principal anonimo es un String, no un {@link UserDetails}. */
  private static boolean signedIn(Authentication authentication) {
    return authentication != null && authentication.getPrincipal() instanceof UserDetails;
  }

  /**
   * [MARK] Planes guardados del visitante. Sin sesion no hay nada que preguntar y el toggle no se
   * renderiza (AC-05): el conjunto vacio no es un soft gate, es ausencia de control.
   */
  private Set<UUID> bookmarkedPlanIds(Authentication authentication) {
    return signedIn(authentication)
      ? this.planBookmarkService.getBookmarkedPlanIds(authentication.getName())
      : Set.of();
  }
}
