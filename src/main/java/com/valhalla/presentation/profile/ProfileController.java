package com.valhalla.presentation.profile;

import com.valhalla.domain.exception.UserNotFoundException;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserService;
import com.valhalla.presentation.plan.PlanFeedCards;
import com.valhalla.presentation.shared.AvatarUrlPolicy;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.servlet.ModelAndView;

/**
 * [PROF] Perfil de usuario: superficie propia (edicion) y vista publica read-only (11-PROF v2).
 *
 * <p>La regla de seguridad del modulo: el usuario de la ESCRITURA sale SIEMPRE de la sesion
 * (AC-04); el path {@code /profile/{id}} solo alimenta la LECTURA. Con sesion propia muestra el
 * editor; con otro id o anonimo muestra la vista publica — abierta, el link compartido funciona
 * sin sesion (v2.3, SecurityConfig tiene el GET permitAll para el id numerico).
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

  private static final String VIEW_EDIT = "pages/profile/edit";
  private static final String VIEW_PUBLIC = "pages/profile/view";
  private static final String VIEW_CHANGE_PASSWORD = "pages/profile/change-password";

  private static final String ERROR_NOT_FOUND = "User not found";

  private final UserService userService;
  private final PlanService planService;

  @Autowired
  public ProfileController(UserService userService, PlanService planService) {
    this.userService = userService;
    this.planService = planService;
  }

  /**
   * [PROF] Atajo "mi perfil": la URL canonica siempre lleva el id (AC-12), asi el perfil se
   * comparte sin boton extra y la metadata de link-sharing ([META]) tiene un id que levantar.
   * Sin sesion la default chain ya redirige a /auth/login; este handler solo corre autenticado.
   */
  @GetMapping
  public String own(Authentication authentication) {
    return "redirect:/profile/" + sessionUser(authentication).getId();
  }

  /**
   * Doble superficie (AC-13/AC-14): con sesion propia renderiza el editor, con id ajeno (o
   * anonimo) la vista publica. La comparacion es por email, que es lo que el principal lleva:
   * {@code AppUserDetails} no expone el id y extenderlo tocaria el contrato de igualdad que
   * usa el {@code SessionRegistry}, no vale la pena para esto.
   */
  @GetMapping("/{id:[\\d]+}")
  public ModelAndView view(@PathVariable Long id, Authentication authentication) {
    User owner;
    try {
      owner = userService.findById(id);
    } catch (UserNotFoundException e) {
      // AC-20: un perfil inexistente responde 404 real, no el redirect /admin/users del
      // GlobalExceptionHandler (esa pagina es del ABM admin, no del area de perfil).
      return new ModelAndView(
        "pages/error",
        Map.of("error", ERROR_NOT_FOUND),
        HttpStatus.NOT_FOUND
      );
    }

    if (isSelf(authentication, owner)) {
      Map<String, Object> model = editModel(
        owner,
        new ProfileForm(owner.getFirstName(), owner.getLastName(), owner.getAvatarUrl())
      );
      return new ModelAndView(VIEW_EDIT, model);
    }
    Map<String, Object> model = publicModel(owner);
    return new ModelAndView(VIEW_PUBLIC, model);
  }

  /**
   * [PROF] El POST no lleva id: el usuario es siempre el de la sesion (AC-04). Ver AC-25 la
   * validacion de formatos del avatar. Un error de validacion re-rende el editor con los
   * errores de campo; el exito vuelve al perfil con el aviso {@code ?updated=true}.
   */
  @PostMapping
  public ModelAndView updateProfile(
    @Valid @ModelAttribute("profileForm") ProfileForm form,
    BindingResult bindingResult,
    Authentication authentication
  ) {
    User me = sessionUser(authentication);
    if (bindingResult.hasErrors()) {
      // Re-rende el editor con los errores de campo (AC-03): el form ya trae lo tipeado,
      // el owner viaja para que el header del edit.html siga completo.
      return new ModelAndView(VIEW_EDIT, editModel(me, form));
    }
    if (!AvatarUrlPolicy.isValid(form.getAvatarUrl())) {
      // AC-25: solo https:// o la galeria /images/avatars — corta javascript:/data:/http.
      bindingResult.rejectValue("avatarUrl", null, AvatarUrlPolicy.REJECT_MESSAGE);
      return new ModelAndView(VIEW_EDIT, editModel(me, form));
    }
    userService.updateProfile(
      me.getId(),
      form.getFirstName(),
      form.getLastName(),
      form.getAvatarUrl()
    );
    return new ModelAndView("redirect:/profile/" + me.getId() + "?updated=true");
  }

  /** [PROF] Form de cambio de contrasena (AC-06). Sin id en el path, por las mismas razones. */
  @GetMapping("/change-password")
  public ModelAndView changePasswordForm() {
    Map<String, Object> model = new LinkedHashMap<>();
    model.put("changePasswordForm", new ChangePasswordForm());
    return new ModelAndView(VIEW_CHANGE_PASSWORD, model);
  }

  /**
   * [PROF] Exige la contrasena actual correcta (AC-07): un mismatch llega como
   * {@code rejectValue("currentPassword")} y re-rende el form sin cambiar nada; el exito
   * redirige con el aviso. {@code maximumSessions(1)} intacto: cambiar la propia contrasena NO
   * invalida la sesion actual, el flujo observable lo prueba E-02.
   */
  @PostMapping("/change-password")
  public ModelAndView changePassword(
    @Valid @ModelAttribute("changePasswordForm") ChangePasswordForm form,
    BindingResult bindingResult,
    Authentication authentication
  ) {
    User me = sessionUser(authentication);
    if (bindingResult.hasErrors()) {
      return new ModelAndView(VIEW_CHANGE_PASSWORD);
    }
    boolean changed = userService.changePassword(
      me.getId(),
      form.getCurrentPassword(),
      form.getNewPassword()
    );
    if (!changed) {
      bindingResult.rejectValue("currentPassword", null, "La contrasena actual no es correcta");
      return new ModelAndView(VIEW_CHANGE_PASSWORD);
    }
    return new ModelAndView("redirect:/profile/" + me.getId() + "?passwordChanged=true");
  }

  /** Usuario en sesion. En autenticado real el principal es {@code UserDetails} y su nombre es
   * el email: se cruza por servicio para obtener el agregado con el id. */
  private User sessionUser(Authentication authentication) {
    return userService.getByEmail(authentication.getName());
  }

  private boolean isSelf(Authentication authentication, User owner) {
    Object principal = authentication == null ? null : authentication.getPrincipal();
    if (!(principal instanceof UserDetails)) {
      return false; // anonimo: principal "anonymousUser", nunca self
    }
    return owner.getEmail().equals(authentication.getName());
  }

  /**
   * Modelo del editor (AC-01): datos del dueño + form pre-cargado. El email y el rol viajan
   * read-only: nadie los edita por este lado.
   */
  private Map<String, Object> editModel(User owner, ProfileForm form) {
    Map<String, Object> model = new LinkedHashMap<>();
    model.put("profileForm", form);
    model.put("owner", owner);
    model.put("ownerInitial", initialOf(owner));
    return model;
  }

  /**
   * Modelo de la vista publica (AC-15): cards del feed para los plans publicos del dueno,
   * re-usando exactamente el mismo payload de /for-you (sin email ni participantes). El
   * contador de seguidores es placeholder (0) hasta que [FOLLOW] aterrice.
   */
  private Map<String, Object> publicModel(User owner) {
    Map<String, Object> model = new LinkedHashMap<>();
    model.put("owner", owner);
    model.put("ownerInitial", initialOf(owner));
    model.put("profileName", displayName(owner));
    model.put("profileAvatarUrl", owner.getAvatarUrl());
    model.put("followerCount", 0L);
    model.put(
      "profilePlans",
      publicPlansOf(owner).stream().map(plan -> PlanFeedCards.planCard(plan, Map.of())).toList()
    );
    return model;
  }

  /** Solo planes PUBLICOS del dueño (AC-15): los privados no aparecen en ninguna vista. */
  private List<Plan> publicPlansOf(User owner) {
    return planService
      .getPublicPlans()
      .stream()
      .filter(plan -> owner.getId().equals(plan.getAdministrator().getId()))
      .toList();
  }

  private static String initialOf(User user) {
    String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
    return first.isEmpty() ? "U" : first.substring(0, 1).toUpperCase(java.util.Locale.ROOT);
  }

  /**
   * Nombre visible del dueño del perfil. El email como fallback NO aplica aca: la vista publica
   * no expone emails (S-04), de modo que sin nombre queda la inicial.
   */
  private static String displayName(User user) {
    String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
    String last = user.getLastName() == null ? "" : user.getLastName().trim();
    return (first + " " + last).trim();
  }
}
