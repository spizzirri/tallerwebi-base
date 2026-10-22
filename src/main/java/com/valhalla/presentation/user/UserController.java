package com.valhalla.presentation.user;

import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class UserController {

  private static final String VIEW_USERS = "pages/admin/users";
  private static final String VIEW_USER_FORM = "pages/admin/user-form";
  private static final String REDIRECT_USERS = "redirect:/admin/users";
  private static final String ATTR_USERS = "users";
  private static final String ATTR_USER_FORM = "userForm";
  private static final String ATTR_USER_ID = "userId";
  private static final String ATTR_IS_EDIT = "isEdit";
  private static final String ATTR_CURRENT_USER_EMAIL = "currentUserEmail";
  private static final String ATTR_GENERATED_PASSWORD = "generatedPassword";

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ModelAndView listUsers(Authentication authentication) {
    Map<String, Object> model = new ModelMap();
    model.put(ATTR_USERS, userService.findAll());
    model.put(ATTR_CURRENT_USER_EMAIL, authentication.getName());
    return new ModelAndView(VIEW_USERS, model);
  }

  @GetMapping("/new")
  public ModelAndView showNewUserForm() {
    Map<String, Object> model = new ModelMap();
    model.put(ATTR_USER_FORM, new EditUserRequest());
    model.put(ATTR_IS_EDIT, false);
    return new ModelAndView(VIEW_USER_FORM, model);
  }

  @PostMapping
  public ModelAndView createUser(
    @Valid @ModelAttribute(ATTR_USER_FORM) EditUserRequest userForm,
    BindingResult bindingResult,
    RedirectAttributes redirectAttributes
  ) {
    if (bindingResult.hasErrors()) {
      return renderFormWithError(userForm, false);
    }
    String password = userService.generatePassword();
    userService.create(
      userForm.getEmail(),
      password,
      userForm.getRole(),
      userForm.getFirstName(),
      userForm.getLastName()
    );
    redirectAttributes.addFlashAttribute(ATTR_GENERATED_PASSWORD, password);
    return new ModelAndView(REDIRECT_USERS);
  }

  @GetMapping("/{id}/edit")
  public ModelAndView showEditUserForm(@PathVariable UUID id) {
    User user = userService.findById(id);
    Map<String, Object> model = new ModelMap();
    model.put(
      ATTR_USER_FORM,
      new EditUserRequest(user.getEmail(), user.getRole(), user.getFirstName(), user.getLastName())
    );
    model.put(ATTR_USER_ID, id);
    model.put(ATTR_IS_EDIT, true);
    return new ModelAndView(VIEW_USER_FORM, model);
  }

  @RequestMapping(value = "/{id}", method = { RequestMethod.POST, RequestMethod.PUT })
  public ModelAndView updateUser(
    @PathVariable UUID id,
    @Valid @ModelAttribute(ATTR_USER_FORM) EditUserRequest userForm,
    BindingResult bindingResult
  ) {
    if (bindingResult.hasErrors()) {
      return renderFormWithError(userForm, true, id);
    }
    userService.update(
      id,
      userForm.getEmail(),
      userForm.getRole(),
      userForm.getFirstName(),
      userForm.getLastName()
    );
    return new ModelAndView(REDIRECT_USERS);
  }

  @RequestMapping(value = "/{id}/deactivate", method = { RequestMethod.POST, RequestMethod.PUT })
  public ModelAndView deactivateUser(@PathVariable UUID id) {
    userService.deactivate(id);
    return new ModelAndView(REDIRECT_USERS);
  }

  @RequestMapping(value = "/{id}/activate", method = { RequestMethod.POST, RequestMethod.PUT })
  public ModelAndView activateUser(@PathVariable UUID id) {
    userService.activate(id);
    return new ModelAndView(REDIRECT_USERS);
  }

  @RequestMapping(
    value = "/{id}/rotate-password",
    method = { RequestMethod.POST, RequestMethod.PUT }
  )
  public ModelAndView rotatePassword(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
    String newPassword = userService.rotatePassword(id);
    redirectAttributes.addFlashAttribute(ATTR_GENERATED_PASSWORD, newPassword);
    return new ModelAndView(REDIRECT_USERS);
  }

  @RequestMapping(value = "/{id}/delete", method = { RequestMethod.POST, RequestMethod.DELETE })
  public ModelAndView deleteUser(@PathVariable UUID id) {
    userService.delete(id);
    return new ModelAndView(REDIRECT_USERS);
  }

  private ModelAndView renderFormWithError(EditUserRequest form, boolean isEdit) {
    return renderFormWithError(form, isEdit, null);
  }

  private ModelAndView renderFormWithError(EditUserRequest form, boolean isEdit, UUID id) {
    Map<String, Object> model = new ModelMap();
    model.put(ATTR_USER_FORM, form);
    model.put(ATTR_IS_EDIT, isEdit);
    if (id != null) {
      model.put(ATTR_USER_ID, id);
    }
    model.put("error", "Invalid user data");
    return new ModelAndView(VIEW_USER_FORM, model);
  }
}
