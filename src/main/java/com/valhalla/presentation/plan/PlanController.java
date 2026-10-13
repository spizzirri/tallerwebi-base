package com.valhalla.presentation.plan;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.planplace.PlanPlaceService;
import com.valhalla.domain.user.User;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/plans")
public class PlanController {

  private static final String VIEW_PLANS_LIST = "pages/plans/list";
  private static final String VIEW_PLAN_DETAIL = "pages/plans/detail";
  private static final String VIEW_PUBLIC_PLAN = "pages/plans/public";
  private static final String REDIRECT_PLANS = "redirect:/plans";
  private static final String REDIRECT_PLAN_DETAIL = "redirect:/plans/";
  private static final String REDIRECT_EXPLORE = "redirect:/explore";
  private static final String ATTR_PLANS = "plans";
  private static final String ATTR_PLAN = "plan";

  private final PlanService planService;
  private final PlanPlaceService planPlaceService;

  @Autowired
  public PlanController(PlanService planService, PlanPlaceService planPlaceService) {
    this.planService = planService;
    this.planPlaceService = planPlaceService;
  }

  @GetMapping
  public ModelAndView listPlans(Authentication authentication) {
    Map<String, Object> model = new ModelMap();
    model.put(ATTR_PLANS, planService.getPlansByUserEmail(authentication.getName()));
    model.put("participantPlans", planService.getParticipantPlans(authentication.getName()));
    return new ModelAndView(VIEW_PLANS_LIST, model);
  }

  @GetMapping("/{id}")
  public ModelAndView showPlan(@PathVariable Long id, Authentication authentication) {
    Map<String, Object> model = new ModelMap();
    Plan plan = planService.getParticipatingPlan(id, authentication.getName());
    model.put(ATTR_PLAN, plan);
    model.put("isPlanAdministrator", plan.isAdministrator(authentication.getName()));
    model.put(
      "participantEmails",
      plan
        .getParticipants()
        .stream()
        .map(User::getEmail)
        .sorted(String.CASE_INSENSITIVE_ORDER.thenComparing(Comparator.naturalOrder()))
        .toList()
    );
    model.put(
      "itineraryPlaces",
      plan.getPlanPlaces().stream().map(PlanController::placeView).toList()
    );
    return new ModelAndView(VIEW_PLAN_DETAIL, model);
  }

  /**
   * Vista publica read-only de un plan compartido. La visibilidad la decide el servicio: un plan
   * privado responde igual que un id inexistente. Los CTAs se deciden por el estado real de la
   * sesion (isLogged, isParticipant, isPlanAdministrator) y no por {@code authentication != null}:
   * con anonimo ese objeto existe como AnonymousAuthenticationToken y seria siempre true.
   */
  @GetMapping("/{id}/public")
  public ModelAndView showPublicPlan(@PathVariable Long id, Authentication authentication) {
    Plan plan = planService.getPublicPlan(id);
    Map<String, Object> model = new ModelMap();
    model.put(ATTR_PLAN, plan);
    model.put(
      "itineraryPlaces",
      plan.getPlanPlaces().stream().map(PlanController::placeView).toList()
    );
    // AC-03: "Creado por" en la vista pública; sin nombre de visualización se usa el email.
    model.put("administratorName", displayName(plan.getAdministrator()));

    String email = currentEmail(authentication);
    model.put("isLogged", email != null);
    model.put("isParticipant", plan.isParticipant(email));
    model.put("isPlanAdministrator", plan.isAdministrator(email));
    // El form de join necesita el codigo; en un plan publico compartirlo es el proposito.
    model.put("shortCode", plan.getShortCode());
    return new ModelAndView(VIEW_PUBLIC_PLAN, model);
  }

  /** En anonimo el principal es el String "anonymousUser", no un UserDetails. */
  private static String currentEmail(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
      return null;
    }
    return authentication.getName();
  }

  /**
   * Nombre con el que se firma el plan en la vista pública. Prefiere el nombre real y cae al
   * email: el "Creado por" nunca queda en blanco (13-PVP AC-03, decisión de 14-PPV).
   */
  private static String displayName(User user) {
    if (user == null) {
      return "";
    }
    String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
    String last = user.getLastName() == null ? "" : user.getLastName().trim();
    String full = (first + " " + last).trim();
    return full.isEmpty() ? user.getEmail() : full;
  }

  @PostMapping
  public ModelAndView createPlan(
    @Valid @ModelAttribute(ATTR_PLAN) PlanRequest planForm,
    BindingResult bindingResult,
    Authentication authentication
  ) {
    if (bindingResult.hasErrors()) {
      // No hay formulario standalone: la creación vive en el modal de /explore, así que
      // un POST inválido vuelve a esa vista en vez de re-renderizar pages/plans/new.
      return new ModelAndView(REDIRECT_EXPLORE);
    }
    Plan created = planService.createPlan(toPlan(planForm), authentication.getName());
    return new ModelAndView(REDIRECT_PLAN_DETAIL + created.getId());
  }

  @RequestMapping(value = "/{id}", method = { RequestMethod.POST, RequestMethod.PUT })
  public ModelAndView updatePlan(
    @PathVariable Long id,
    @Valid @ModelAttribute(ATTR_PLAN) PlanRequest planForm,
    BindingResult bindingResult,
    Authentication authentication
  ) {
    if (bindingResult.hasErrors()) {
      // El detalle edita en su propia página: un PUT inválido vuelve al detalle del plan.
      return new ModelAndView(REDIRECT_PLAN_DETAIL + id);
    }
    planService.updatePlan(id, toPlan(planForm), authentication.getName());
    return new ModelAndView(REDIRECT_PLAN_DETAIL + id);
  }

  /**
   * Delete lives on its own sub-path and accepts POST, the same way UserController's
   * /admin/users/{id}/delete does, so it never competes with updatePlan above for
   * {@code POST /plans/{id}}. Both verbs answer on the same route, which keeps the delete button
   * behaving identically whether or not {@code _method} is honoured.
   */
  @RequestMapping(value = "/{id}/delete", method = { RequestMethod.POST, RequestMethod.DELETE })
  public ModelAndView deletePlan(@PathVariable Long id, Authentication authentication) {
    planService.deleteOwnedPlan(id, authentication.getName());
    return new ModelAndView(REDIRECT_PLANS);
  }

  @PostMapping("/{id}/clone")
  public ModelAndView clonePlan(@PathVariable Long id, Authentication authentication) {
    Plan copy = planService.clonePlan(id, authentication.getName());
    return new ModelAndView(REDIRECT_PLAN_DETAIL + copy.getId());
  }

  @PostMapping("/{id}/leave")
  public ModelAndView leavePlan(@PathVariable Long id, Authentication authentication) {
    planService.leavePlan(id, authentication.getName());
    return new ModelAndView(REDIRECT_PLANS);
  }

  @PostMapping("/join")
  public ModelAndView joinPlan(@RequestParam String shortCode, Authentication authentication) {
    Plan plan = planService.joinPlan(shortCode, authentication.getName());
    return new ModelAndView(REDIRECT_PLAN_DETAIL + plan.getId());
  }

  /** The form owns these fields and its itinerary rows only: id, shortCode and administrator never come from a post. */
  private Plan toPlan(PlanRequest planForm) {
    Plan plan = new Plan();
    plan.setName(planForm.getName());
    plan.setDescription(planForm.getDescription());
    plan.setEventDate(planForm.getEventDate());
    plan.setEventTime(planForm.getEventTime());
    plan.setIsPublic(planForm.getIsPublic());
    int order = 1;
    for (PlanPlaceRequest row : planForm.getPlaces()) {
      if (row.getPlaceId() == null) {
        continue;
      }
      // Id-only stub: PlanServiceImpl swaps it for the persisted Place.
      Place place = new Place();
      place.setId(row.getPlaceId());
      PlanPlace entry = new PlanPlace();
      entry.setPlace(place);
      entry.setDescription(row.getDescription());
      entry.setVisitDate(row.getVisitDate());
      entry.setVisitTime(row.getVisitTime());
      entry.setSortOrder(order);
      order++;
      plan.addPlanPlace(entry);
    }
    return plan;
  }

  private static Map<String, Object> placeView(PlanPlace entry) {
    Place place = entry.getPlace();
    Map<String, Object> view = new LinkedHashMap<>();
    view.put("id", place.getId());
    view.put("entryId", entry.getId());
    view.put("description", entry.getDescription());
    view.put("visitDate", entry.getVisitDate() == null ? null : entry.getVisitDate().toString());
    view.put("visitTime", entry.getVisitTime() == null ? null : entry.getVisitTime().toString());
    view.put("name", place.getName());
    view.put("category", place.getCategory() == null ? null : place.getCategory().name());
    view.put("latitude", place.getLatitude());
    view.put("longitude", place.getLongitude());
    return view;
  }
}
