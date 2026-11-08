package com.valhalla.presentation.profile;

import com.valhalla.domain.bookmark.PlanBookmarkService;
import com.valhalla.domain.exception.UserNotFoundException;
import com.valhalla.domain.follow.FollowService;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserService;
import com.valhalla.presentation.bookmark.BookmarkCards;
import com.valhalla.presentation.plan.PlanFeedCards;
import com.valhalla.presentation.shared.AvatarUrlPolicy;
import com.valhalla.presentation.shared.LoginRedirects;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

/**
 * [PROF] Perfil de usuario: superficie propia (edicion) y vista publica read-only (11-PROF v2).
 *
 * <p>La regla de seguridad del modulo: el usuario de la ESCRITURA sale SIEMPRE de la sesion
 * (AC-04); el path {@code /profile/{id}} solo alimenta la LECTURA. Con sesion propia muestra el
 * editor; con otro id o anonimo muestra la vista publica — abierta, el link compartido funciona
 * sin sesion (v2.3, SecurityConfig tiene el GET permitAll para el id UUID).
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

  private static final String VIEW_EDIT = "pages/profile/edit";
  private static final String VIEW_PUBLIC = "pages/profile/view";
  private static final String VIEW_CHANGE_PASSWORD = "pages/profile/change-password";
  private static final String VIEW_ERROR = "pages/error";
  private static final String REDIRECT_PROFILE = "redirect:/profile/";

  private static final String ERROR_NOT_FOUND = "User not found";

  private final UserService userService;
  private final PlanService planService;
  private final PlanBookmarkService planBookmarkService;
  private final FollowService followService;

  @Autowired
  public ProfileController(
    UserService userService,
    PlanService planService,
    PlanBookmarkService planBookmarkService,
    FollowService followService
  ) {
    this.userService = userService;
    this.planService = planService;
    this.planBookmarkService = planBookmarkService;
    this.followService = followService;
  }

  /**
   * [PROF] Atajo "mi perfil": la URL canonica siempre lleva el id (AC-12), asi el perfil se
   * comparte sin boton extra y la metadata de link-sharing ([META]) tiene un id que levantar.
   * Sin sesion la default chain ya redirige a /auth/login; este handler solo corre autenticado.
   */
  @GetMapping
  public String own(Authentication authentication) {
    return REDIRECT_PROFILE + sessionUser(authentication).getId();
  }

  /**
   * Doble superficie (AC-13/AC-14): con sesion propia renderiza el editor, con id ajeno (o
   * anonimo) la vista publica. La comparacion es por email, que es lo que el principal lleva:
   * {@code AppUserDetails} no expone el id y extenderlo tocaria el contrato de igualdad que
   * usa el {@code SessionRegistry}, no vale la pena para esto.
   */
  @GetMapping("/{id}")
  public ModelAndView view(@PathVariable UUID id, Authentication authentication) {
    User owner;
    try {
      owner = userService.findById(id);
    } catch (UserNotFoundException e) {
      // AC-20: un perfil inexistente responde 404 real, no el redirect /admin/users del
      // GlobalExceptionHandler (esa pagina es del ABM admin, no del area de perfil).
      return notFound();
    }

    if (isSelf(authentication, owner)) {
      Map<String, Object> model = editModel(
        owner,
        new ProfileForm(owner.getFirstName(), owner.getLastName(), owner.getAvatarUrl())
      );
      return new ModelAndView(VIEW_EDIT, model);
    }
    return new ModelAndView(VIEW_PUBLIC, publicModel(owner, authentication));
  }

  /**
   * [FOLLOW] Seguir al dueno del perfil: el path solo nombra al SEGUIDO, la identidad del
   * seguidor sale de la sesion (AC-13). Seguirse a si mismo es un no-op (AC-03): el perfil propio
   * no ofrece el boton y un POST directo no crea nada. Un id inexistente responde el mismo 404
   * que la vista (AC-01).
   */
  @PostMapping("/{id}/follow")
  public ModelAndView follow(@PathVariable UUID id, Authentication authentication) {
    UUID myId = sessionUser(authentication).getId();
    if (!myId.equals(id)) {
      try {
        followService.follow(myId, id);
      } catch (UserNotFoundException e) {
        return notFound();
      }
    }
    return new ModelAndView(REDIRECT_PROFILE + id);
  }

  /**
   * [FOLLOW] Dejar de seguir: misma regla de identidad que el follow (AC-13) y no-op si la
   * relacion no existia (AC-02).
   */
  @PostMapping("/{id}/unfollow")
  public ModelAndView unfollow(@PathVariable UUID id, Authentication authentication) {
    followService.unfollow(sessionUser(authentication).getId(), id);
    return new ModelAndView(REDIRECT_PROFILE + id);
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
    return new ModelAndView(REDIRECT_PROFILE + me.getId() + "?updated=true");
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
    return new ModelAndView(REDIRECT_PROFILE + me.getId() + "?passwordChanged=true");
  }

  /**
   * [PROF+THEME] POST del item "Tema" del modal (I-21). El usuario de la escritura sale de la
   * sesion, el valor se valida antes de tocar la DB (400 sin escribir) y el redirect vuelve a la
   * pagina de origen solo si es una ruta interna segura (open redirect cerrado). En {@code
   * /admin/**} el advice no expone {@code currentUrl}, asi que el fallback es la landing del rol:
   * un admin no pierde el area al cambiar de tema.
   */
  @PostMapping("/theme")
  public String updateTheme(
    @RequestParam("theme") String theme,
    @RequestParam(value = "redirect", required = false) String redirect,
    Authentication authentication
  ) {
    User me = sessionUser(authentication);
    if (!User.isValidTheme(theme)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tema invalido");
    }
    userService.updateThemePreference(me.getId(), theme);
    String target = LoginRedirects.isSafeInternalPath(redirect)
      ? redirect
      : LoginRedirects.landingFor(authentication);
    return "redirect:" + target;
  }

  /** Usuario en sesion. En autenticado real el principal es {@code UserDetails} y su nombre es
   * el email: se cruza por servicio para obtener el agregado con el id. */
  private User sessionUser(Authentication authentication) {
    return userService.getByEmail(authentication.getName());
  }

  private boolean isSelf(Authentication authentication, User owner) {
    String viewer = viewerEmail(authentication);
    return viewer != null && viewer.equals(owner.getEmail());
  }

  /**
   * Email del visitante con sesion, o null para un anonimo. El principal anonimo es el String
   * "anonymousUser", no un {@link UserDetails}: mismo criterio que {@link #isSelf} para no tratar
   * un nombre sintetico como identidad real.
   */
  private static String viewerEmail(Authentication authentication) {
    Object principal = authentication == null ? null : authentication.getPrincipal();
    return principal instanceof UserDetails ? authentication.getName() : null;
  }

  /**
   * Modelo del editor (AC-01): datos del dueno + form pre-cargado. El email y el rol viajan
   * read-only: nadie los edita por este lado. [FOLLOW] suma los conteos y las listas del usuario
   * en sesion para su seccion {@code #follow} (AC-05).
   */
  private Map<String, Object> editModel(User owner, ProfileForm form) {
    Map<String, Object> model = new LinkedHashMap<>();
    model.put("profileForm", form);
    model.put("owner", owner);
    model.put("ownerInitial", initialOf(owner));
    // [MARK] Guardados es privado del dueno: esta seccion solo existe en el perfil propio
    // (AC-09/AC-10). El servicio resuelve el estado de cada card por request.
    model.put(
      "bookmarks",
      this.planBookmarkService.getBookmarks(owner.getEmail())
        .stream()
        .map(BookmarkCards::card)
        .toList()
    );
    putFollowData(model, owner);
    return model;
  }

  /**
   * Modelo de la vista publica (AC-15): cards del feed para los planes publicos del dueno,
   * re-usando exactamente el mismo payload de /for-you (sin email ni participantes). [FOLLOW] los
   * conteos son los reales y el visitante con sesion sabe si ya lo sigue (AC-04).
   *
   * <p>[MARK] El visitante con sesion ve el toggle de Guardados en cada card; el anonimo no. Un
   * solo lookup alimenta todas las cards de la pagina.
   */
  private Map<String, Object> publicModel(User owner, Authentication authentication) {
    String viewer = viewerEmail(authentication);
    Set<UUID> bookmarkedPlanIds = viewer == null
      ? Set.of()
      : this.planBookmarkService.getBookmarkedPlanIds(viewer);
    Map<String, Object> model = new LinkedHashMap<>();
    model.put("owner", owner);
    model.put("ownerInitial", initialOf(owner));
    model.put("profileName", displayName(owner));
    model.put("profileAvatarUrl", owner.getAvatarUrl());
    model.put("canBookmark", viewer != null);
    putFollowData(model, owner);
    UUID viewerId = sessionUserId(authentication);
    // El anonimo nunca sigue a nadie: false sin tocar la tabla (AC-10).
    model.put(
      "isFollowing",
      viewerId != null && followService.isFollowing(viewerId, owner.getId())
    );
    model.put(
      "profilePlans",
      publicPlansOf(owner)
        .stream()
        .map(plan ->
          PlanFeedCards.planCard(plan, Map.of(), bookmarkedPlanIds.contains(plan.getId()))
        )
        .toList()
    );
    String profileName = displayName(owner);
    String description = "Perfil de " + profileName + " en PlanIt. Descubrí sus planes públicos.";
    model.put("pageTitle", profileName);
    model.put("ogTitle", profileName);
    model.put("ogType", "profile");
    model.put("pageDescription", description);
    model.put("ogDescription", description);
    model.put("ogUrl", "/profile/" + owner.getId());
    if (owner.getAvatarUrl() != null && !owner.getAvatarUrl().isBlank()) {
      model.put("ogImage", owner.getAvatarUrl());
    } else {
      model.put("ogImage", "/images/og-image.png");
    }
    return model;
  }

  /**
   * [FOLLOW] Conteos agregados y listas ordenadas del perfil (AC-04, AC-05, AC-15). Al template
   * solo llegan nombre, avatar y link al perfil: ni email ni rol (AC-12).
   */
  private void putFollowData(Map<String, Object> model, User owner) {
    model.put("followerCount", followService.countFollowers(owner.getId()));
    model.put("followingCount", followService.countFollowing(owner.getId()));
    model.put("followers", followService.followers(owner.getId()));
    model.put("following", followService.following(owner.getId()));
  }

  /** Id del visitante con sesion; {@code null} para el anonimo, que no tiene fila en users. */
  private UUID sessionUserId(Authentication authentication) {
    Object principal = authentication == null ? null : authentication.getPrincipal();
    if (!(principal instanceof UserDetails)) {
      return null;
    }
    return userService.getByEmail(authentication.getName()).getId();
  }

  /** Mismo 404 que la vista del perfil: un id inexistente no es un error del servidor. */
  private static ModelAndView notFound() {
    return new ModelAndView(VIEW_ERROR, Map.of("error", ERROR_NOT_FOUND), HttpStatus.NOT_FOUND);
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
