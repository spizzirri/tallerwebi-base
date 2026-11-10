package com.valhalla.presentation.user;

import com.valhalla.domain.follow.FollowService;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * [USEARCH] Buscador de personas para seguir en {@code GET /users?q=}.
 *
 * <p>La ruta exige sesion por la cadena default de seguridad; sin sesion redirige al login. Una
 * consulta vacia no consulta la base ni lista a todos: la vista muestra el estado inicial. El
 * seguimiento desde una fila es un POST y vuelve a la misma busqueda, asi el boton se
 * re-renderiza como "Siguiendo" sin perder el listado.
 */
@Controller
@RequestMapping("/users")
public class UserSearchController {

  private static final String VIEW_SEARCH = "pages/users/search";
  private static final String REDIRECT_SEARCH = "redirect:/users";

  private final UserService userService;
  private final PlanService planService;
  private final FollowService followService;

  @Autowired
  public UserSearchController(
    UserService userService,
    PlanService planService,
    FollowService followService
  ) {
    this.userService = userService;
    this.planService = planService;
    this.followService = followService;
  }

  @GetMapping
  public ModelAndView search(
    @RequestParam(value = "q", defaultValue = "") String rawQuery,
    Authentication authentication
  ) {
    User viewer = userService.getByEmail(authentication.getName());
    String query = rawQuery.trim();
    List<UserSearchResult> results = query.isEmpty() ? List.of() : resultsFor(viewer, query);
    Map<String, Object> model = new LinkedHashMap<>();
    model.put("q", query);
    model.put("results", results);
    return new ModelAndView(VIEW_SEARCH, model);
  }

  @PostMapping("/{id}/follow")
  public String follow(
    @PathVariable UUID id,
    @RequestParam(value = "q", defaultValue = "") String query,
    Authentication authentication,
    RedirectAttributes redirectAttributes
  ) {
    followService.follow(sessionUserId(authentication), id);
    return redirectToSearch(query, redirectAttributes);
  }

  @PostMapping("/{id}/unfollow")
  public String unfollow(
    @PathVariable UUID id,
    @RequestParam(value = "q", defaultValue = "") String query,
    Authentication authentication,
    RedirectAttributes redirectAttributes
  ) {
    followService.unfollow(sessionUserId(authentication), id);
    return redirectToSearch(query, redirectAttributes);
  }

  /**
   * Arma las filas con los datos que la vista necesita. La cantidad de planes publicos sale de una
   * sola query para todos los autores y el estado del grafo de una sola lectura de seguidos, asi
   * que una busqueda de N personas no dispara N consultas.
   */
  private List<UserSearchResult> resultsFor(User viewer, String query) {
    List<User> matches = userService.searchByName(query, viewer.getId());
    if (matches.isEmpty()) {
      return List.of();
    }
    List<UUID> ids = matches.stream().map(User::getId).toList();
    Map<UUID, Long> publicPlanCounts = planService
      .getPublicPlansByAdministrators(ids)
      .stream()
      .collect(
        Collectors.groupingBy(plan -> plan.getAdministrator().getId(), Collectors.counting())
      );
    Set<UUID> followed = Set.copyOf(followService.followedIds(viewer.getId()));
    return matches
      .stream()
      .map(user ->
        UserSearchResult.of(
          user,
          publicPlanCounts.getOrDefault(user.getId(), 0L),
          followed.contains(user.getId())
        )
      )
      .toList();
  }

  private UUID sessionUserId(Authentication authentication) {
    return userService.getByEmail(authentication.getName()).getId();
  }

  private static String redirectToSearch(String query, RedirectAttributes redirectAttributes) {
    redirectAttributes.addAttribute("q", query.trim());
    return REDIRECT_SEARCH;
  }
}
