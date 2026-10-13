package com.valhalla.presentation.auth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.exception.UserAlreadyExists;
import com.valhalla.domain.login.LoginService;
import com.valhalla.infrastructure.security.ProgrammaticSignIn;
import com.valhalla.infrastructure.user.RecoverPasswordService;
import com.valhalla.presentation.shared.LoginRedirects;
import com.valhalla.presentation.shared.RecoverPasswordRequest;
import com.valhalla.presentation.shared.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/** Unit tests for {@link AuthController}. Scenario ids U-01..U-10 of docs/specs/08-AUT.md. */
public class AuthControllerTest {

  private static final String EMAIL = "nuevo@unlam.edu.ar";
  private static final String PASSWORD = "secret123";
  private static final String FIRST_NAME = "Ana";
  private static final String LAST_NAME = "Perez";
  private static final String LOGIN_VIEW = "pages/auth/user/login";
  private static final String REGISTER_VIEW = "pages/auth/user/register";
  private static final String FORGOT_VIEW = "pages/auth/user/forgot-password";
  private static final String RECOVERED_VIEW = "pages/auth/user/recovered";

  private static final org.springframework.validation.Validator VALIDATOR = localValidator();

  private static LocalValidatorFactoryBean localValidator() {
    LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
    factoryBean.afterPropertiesSet();
    return factoryBean;
  }

  private LoginService loginService;
  private RecoverPasswordService recoverPasswordService;
  private ProgrammaticSignIn programmaticSignIn;
  private HttpServletRequest httpRequest;
  private HttpServletResponse httpResponse;
  private AuthController controller;

  @BeforeEach
  public void init() {
    loginService = mock(LoginService.class);
    recoverPasswordService = mock(RecoverPasswordService.class);
    programmaticSignIn = mock(ProgrammaticSignIn.class);
    httpRequest = new MockHttpServletRequest();
    httpResponse = new MockHttpServletResponse();
    controller = new AuthController(loginService, recoverPasswordService, programmaticSignIn);
  }

  // --- U-01, U-02 ---

  @Test
  public void shouldReturnTheLoginView() {
    assertThat(controller.showLoginForm(null, new ExtendedModelMap(), null), is(LOGIN_VIEW));
  }

  @Test
  public void shouldAddErrorToModelWhenLoginFails() {
    ExtendedModelMap model = new ExtendedModelMap();

    controller.showLoginForm("true", model, null);

    assertThat(model.get("error"), is(true));
  }

  @Test
  public void shouldNotAddErrorToModelWhenErrorParamIsNotTrue() {
    ExtendedModelMap falseModel = new ExtendedModelMap();
    ExtendedModelMap absentModel = new ExtendedModelMap();

    controller.showLoginForm("false", falseModel, null);
    controller.showLoginForm(null, absentModel, null);

    assertThat(falseModel.get("error"), is(false));
    assertThat(absentModel.get("error"), is(false));
  }

  /**
   * Regresión del bug reportado: con sesión activa, /auth/login renderizaba el formulario encima de
   * la sesión. El par de tests de abajo cubre las dos ramas del redirect; la integración cubre que
   * el destino sea el mismo que el de un login exitoso.
   */
  @Test
  public void shouldRedirectAwayFromTheLoginViewWhenAlreadySignedIn() {
    String view = controller.showLoginForm(
      null,
      new ExtendedModelMap(),
      new TestingAuthenticationToken(
        "admin@unlam.edu.ar",
        "n/a",
        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
      )
    );

    assertThat(view, is("redirect:/admin/home"));
  }

  @Test
  public void shouldRedirectACommonUserToTheLandingFromTheLoginView() {
    String view = controller.showLoginForm(
      null,
      new ExtendedModelMap(),
      new TestingAuthenticationToken(
        "user@unlam.edu.ar",
        "n/a",
        List.of(new SimpleGrantedAuthority("ROLE_USER"))
      )
    );

    assertThat(view, is("redirect:" + LoginRedirects.USER_LANDING));
  }

  // --- U-03, U-04 ---

  @Test
  public void shouldReturnRegisterViewWithAnEmptyForm() {
    ExtendedModelMap model = new ExtendedModelMap();

    String view = controller.showRegisterForm(model);

    assertThat(view, is(REGISTER_VIEW));
    assertThat(
      model.get("registerRequest"),
      org.hamcrest.Matchers.instanceOf(RegisterRequest.class)
    );
    assertThat(((RegisterRequest) model.get("registerRequest")).getEmail(), is(nullValue()));
  }

  /**
   * U-04: registrar ya no termina en el form de login. El mismo paso que crea la cuenta la abre
   * (ProgrammaticSignIn) y el destino sale de la misma regla que un login normal, que para un
   * rol USER es {@link LoginRedirects#USER_LANDING}.
   */
  @Test
  public void shouldSignInAndRedirectToTheLandingAfterSuccessfulRegistration() {
    RegisterRequest request = wellFormedRequest();
    when(programmaticSignIn.signIn(eq(EMAIL), same(httpRequest), same(httpResponse)))
      .thenReturn(
        new TestingAuthenticationToken(
          EMAIL,
          "n/a",
          List.of(new SimpleGrantedAuthority("ROLE_USER"))
        )
      );

    String view = controller.handleRegister(
      request,
      noErrors(request),
      new ExtendedModelMap(),
      httpRequest,
      httpResponse
    );

    assertThat(view, is("redirect:" + LoginRedirects.USER_LANDING));
    verify(loginService).register(EMAIL, PASSWORD, FIRST_NAME, LAST_NAME);
    verify(programmaticSignIn).signIn(EMAIL, httpRequest, httpResponse);
  }

  // --- U-05, U-06 ---

  @Test
  public void shouldRejectAnInvalidEmail() {
    RegisterRequest request = wellFormedRequest();
    request.setEmail("not-an-email");
    BeanPropertyBindingResult bindingResult = errorsOf(request);

    String view = controller.handleRegister(
      request,
      bindingResult,
      new ExtendedModelMap(),
      httpRequest,
      httpResponse
    );

    assertThat(view, is(REGISTER_VIEW));
    assertThat(fieldNames(bindingResult), hasItem("email"));
    verify(loginService, never()).register(EMAIL, PASSWORD, FIRST_NAME, LAST_NAME);
  }

  @Test
  public void shouldRejectAShortPassword() {
    RegisterRequest request = wellFormedRequest();
    request.setPassword("123");
    request.setConfirmPassword("123");
    BeanPropertyBindingResult bindingResult = errorsOf(request);

    String view = controller.handleRegister(
      request,
      bindingResult,
      new ExtendedModelMap(),
      httpRequest,
      httpResponse
    );

    assertThat(view, is(REGISTER_VIEW));
    assertThat(fieldNames(bindingResult), hasItem("password"));
    verify(loginService, never()).register(EMAIL, PASSWORD, FIRST_NAME, LAST_NAME);
  }

  @Test
  public void shouldRejectABlankFirstNameOrLastName() {
    RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD);
    request.setConfirmPassword(PASSWORD);
    BeanPropertyBindingResult bindingResult = errorsOf(request);

    String view = controller.handleRegister(
      request,
      bindingResult,
      new ExtendedModelMap(),
      httpRequest,
      httpResponse
    );

    assertThat(view, is(REGISTER_VIEW));
    assertThat(fieldNames(bindingResult), hasItem("firstName"));
    assertThat(fieldNames(bindingResult), hasItem("lastName"));
    verify(loginService, never()).register(EMAIL, PASSWORD, FIRST_NAME, LAST_NAME);
  }

  @Test
  public void shouldRejectAConfirmPasswordThatDoesNotMatch() {
    RegisterRequest request = wellFormedRequest();
    request.setConfirmPassword("otra-clave");
    BeanPropertyBindingResult bindingResult = errorsOf(request);

    String view = controller.handleRegister(
      request,
      bindingResult,
      new ExtendedModelMap(),
      httpRequest,
      httpResponse
    );

    assertThat(view, is(REGISTER_VIEW));
    assertThat(fieldNames(bindingResult), hasItem("confirmPassword"));
    assertThat(
      bindingResult.getFieldError("confirmPassword").getDefaultMessage(),
      is("Las contraseñas no coinciden")
    );
    verify(loginService, never()).register(EMAIL, PASSWORD, FIRST_NAME, LAST_NAME);
  }

  // --- U-07 ---

  @Test
  public void shouldReRenderRegisterViewWhenEmailIsAlreadyRegistered() {
    RegisterRequest request = wellFormedRequest();
    doThrow(new UserAlreadyExists())
      .when(loginService)
      .register(EMAIL, PASSWORD, FIRST_NAME, LAST_NAME);
    ExtendedModelMap model = new ExtendedModelMap();

    String view = controller.handleRegister(
      request,
      noErrors(request),
      model,
      httpRequest,
      httpResponse
    );

    assertThat(view, is(REGISTER_VIEW));
    assertThat((String) model.get("errorMessage"), is("Ese email ya está registrado"));
    // Un registro rechazado no abre sesión: el signIn vive después del register en el try.
    verify(programmaticSignIn, never()).signIn(anyString(), any(), any());
  }

  // --- U-08, U-09, U-10 ---

  @Test
  public void shouldReturnForgotPasswordViewWithAnEmptyForm() {
    ExtendedModelMap model = new ExtendedModelMap();

    String view = controller.showForgotPasswordForm(model);

    assertThat(view, is(FORGOT_VIEW));
    assertThat(
      model.get("recoverPasswordRequest"),
      org.hamcrest.Matchers.instanceOf(RecoverPasswordRequest.class)
    );
  }

  @Test
  public void shouldReturnTheTemporaryPasswordAfterRecovery() {
    RecoverPasswordRequest request = new RecoverPasswordRequest(EMAIL);
    when(recoverPasswordService.recoverPassword(EMAIL)).thenReturn("tmp-9876");
    ExtendedModelMap model = new ExtendedModelMap();

    String view = controller.handleRecoverPassword(request, noErrors(request), model);

    assertThat(view, is(RECOVERED_VIEW));
    assertThat((String) model.get("tempPassword"), is("tmp-9876"));
  }

  @Test
  public void shouldRejectAnInvalidEmailOnRecovery() {
    RecoverPasswordRequest request = new RecoverPasswordRequest("not-an-email");
    BeanPropertyBindingResult bindingResult = errorsOf(request);

    String view = controller.handleRecoverPassword(request, bindingResult, new ExtendedModelMap());

    assertThat(view, is(FORGOT_VIEW));
    assertThat(fieldNames(bindingResult), hasItem("email"));
  }

  @Test
  public void shouldTellTheUserWhenTheEmailIsUnknown() {
    RecoverPasswordRequest request = new RecoverPasswordRequest(EMAIL);
    when(recoverPasswordService.recoverPassword(EMAIL))
      .thenThrow(new IllegalArgumentException("Email no encontrado"));
    ExtendedModelMap model = new ExtendedModelMap();

    String view = controller.handleRecoverPassword(request, noErrors(request), model);

    assertThat(view, is(FORGOT_VIEW));
    assertThat((String) model.get("errorMessage"), is("Email no encontrado"));
    assertThat(model.get("tempPassword"), is(nullValue()));
  }

  // --- helpers ---

  private static BeanPropertyBindingResult noErrors(Object target) {
    return new BeanPropertyBindingResult(target, "target");
  }

  /** Runs the real bean validation so U-05/U-06 assert the DTO constraints, not a hand-made error. */
  private static BeanPropertyBindingResult errorsOf(Object target) {
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "target");
    VALIDATOR.validate(target, bindingResult);
    return bindingResult;
  }

  private static Set<String> fieldNames(BeanPropertyBindingResult bindingResult) {
    return bindingResult
      .getFieldErrors()
      .stream()
      .map(FieldError::getField)
      .collect(java.util.stream.Collectors.toSet());
  }

  /** A request that passes every constraint, so each test can break only its own field. */
  private static RegisterRequest wellFormedRequest() {
    RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD);
    request.setFirstName(FIRST_NAME);
    request.setLastName(LAST_NAME);
    request.setConfirmPassword(PASSWORD);
    return request;
  }

  /** Guard: the happy paths must not accidentally satisfy the "no errors" case. */
  @Test
  public void shouldValidateAWellFormedRegistration() {
    assertThat(errorsOf(wellFormedRequest()).hasErrors(), is(false));
    assertThat(errorsOf(new RecoverPasswordRequest(EMAIL)).hasErrors(), is(false));
    assertThat(fieldNames(errorsOf(new RegisterRequest(EMAIL, "123"))), not(hasItem("email")));
  }
}
