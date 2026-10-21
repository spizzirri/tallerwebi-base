package com.valhalla.presentation.auth;

import com.valhalla.domain.exception.UserAlreadyExists;
import com.valhalla.domain.login.LoginService;
import com.valhalla.infrastructure.security.ProgrammaticSignIn;
import com.valhalla.infrastructure.user.RecoverPasswordService;
import com.valhalla.presentation.shared.AvatarUrlPolicy;
import com.valhalla.presentation.shared.LoginRedirects;
import com.valhalla.presentation.shared.RecoverPasswordRequest;
import com.valhalla.presentation.shared.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class AuthController {

  private static final String REGISTER_VIEW = "pages/auth/user/register";

  private final LoginService loginService;
  private final RecoverPasswordService recoverPasswordService;
  private final ProgrammaticSignIn programmaticSignIn;

  @Autowired
  public AuthController(
    LoginService loginService,
    RecoverPasswordService recoverPasswordService,
    ProgrammaticSignIn programmaticSignIn
  ) {
    this.loginService = loginService;
    this.recoverPasswordService = recoverPasswordService;
    this.programmaticSignIn = programmaticSignIn;
  }

  // --- LOGIN ---
  @GetMapping("/login")
  public String showLoginForm(
    @RequestParam(value = "error", required = false) String error,
    Model model,
    Authentication authentication
  ) {
    // Mismo corte que /admin/login: con sesión activa el formulario no ofrece nada. La regla y el
    // destino son compartidos con el handler de login exitoso, así que las dos páginas de login
    // llevan a donde llevaría un login.
    if (LoginRedirects.isSignedIn(authentication)) {
      return "redirect:" + LoginRedirects.landingFor(authentication);
    }
    model.addAttribute("error", "true".equals(error));
    return "pages/auth/user/login";
  }

  // --- REGISTRO ---
  @GetMapping("/register")
  public String showRegisterForm(Model model) {
    model.addAttribute("registerRequest", new RegisterRequest());
    return REGISTER_VIEW;
  }

  @PostMapping("/register")
  public String handleRegister(
    @Valid @ModelAttribute("registerRequest") RegisterRequest request,
    BindingResult bindingResult,
    Model model,
    HttpServletRequest httpRequest,
    HttpServletResponse httpResponse
  ) {
    if (bindingResult.hasErrors()) {
      return REGISTER_VIEW;
    }

    if (!request.getPassword().equals(request.getConfirmPassword())) {
      bindingResult.rejectValue("confirmPassword", null, "Las contraseñas no coinciden");
      return REGISTER_VIEW;
    }

    // [PROF] AC-25: formato del avatar, la misma regla única del perfil (AvatarUrlPolicy).
    if (!AvatarUrlPolicy.isValid(request.getAvatarUrl())) {
      bindingResult.rejectValue("avatarUrl", null, AvatarUrlPolicy.REJECT_MESSAGE);
      return REGISTER_VIEW;
    }

    try {
      loginService.register(
        request.getEmail(),
        request.getPassword(),
        request.getFirstName(),
        request.getLastName(),
        request.getAvatarUrl()
      );
      // Crear la cuenta no es sólo crearla: quien se registra queda adentro en el mismo paso, y
      // el destino lo decide la misma regla que a un login normal (LoginRedirects), no uno nuevo.
      Authentication authentication = programmaticSignIn.signIn(
        request.getEmail(),
        httpRequest,
        httpResponse
      );
      return "redirect:" + LoginRedirects.landingFor(authentication);
    } catch (UserAlreadyExists e) {
      model.addAttribute("errorMessage", "Ese email ya está registrado");
      return REGISTER_VIEW;
    }
  }

  // --- RECUPERACIÓN DE CONTRASEÑA ---
  @GetMapping("/forgot-password")
  public String showForgotPasswordForm(Model model) {
    model.addAttribute("recoverPasswordRequest", new RecoverPasswordRequest());
    return "pages/auth/user/forgot-password";
  }

  @PostMapping("/recover")
  public String handleRecoverPassword(
    @Valid @ModelAttribute("recoverPasswordRequest") RecoverPasswordRequest request,
    BindingResult bindingResult,
    Model model
  ) {
    if (bindingResult.hasErrors()) {
      return "pages/auth/user/forgot-password";
    }

    try {
      String tempPassword = recoverPasswordService.recoverPassword(request.getEmail());
      model.addAttribute("tempPassword", tempPassword);
      return "pages/auth/user/recovered";
    } catch (IllegalArgumentException e) {
      model.addAttribute("errorMessage", e.getMessage());
      return "pages/auth/user/forgot-password";
    }
  }
}
