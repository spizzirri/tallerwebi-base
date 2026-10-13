package com.valhalla.presentation.shared;

import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.exception.UserAlreadyExists;
import com.valhalla.domain.exception.UserNotFoundException;
import com.valhalla.presentation.user.EditUserRequest;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOGGER = Logger.getLogger(GlobalExceptionHandler.class.getName());
  private static final String ERROR_KEY = "error";

  @ExceptionHandler(UserAlreadyExists.class)
  public ModelAndView handleUserAlreadyExists() {
    Map<String, Object> model = new ModelMap();
    model.put("userForm", new EditUserRequest());
    model.put("isEdit", false);
    model.put(ERROR_KEY, "Email is already registered");
    return new ModelAndView("pages/admin/user-form", model);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ModelAndView handleUserNotFound() {
    Map<String, Object> model = new ModelMap();
    model.put(ERROR_KEY, "User not found");
    return new ModelAndView("redirect:/admin/users", model);
  }

  @ExceptionHandler(PlanNotFoundException.class)
  public ModelAndView handlePlanNotFound() {
    Map<String, Object> model = new ModelMap();
    model.put(ERROR_KEY, "Plan not found");
    return new ModelAndView("redirect:/plans", model);
  }

  /**
   * An unmapped URL. Without this the catch-all below answers 200 and renders {@code pages/error},
   * so a missing page was indistinguishable from a real one. Both exception types are listed
   * because which one fires depends on whether the request reaches the static resource handler.
   * A path variable that cannot convert (e.g. {@code GET /plans/new} matching {@code /plans/{id}})
   * is also a missing resource: {@code MethodArgumentTypeMismatchException} must answer 404, not 500.
   */
  @ExceptionHandler(
    {
      NoHandlerFoundException.class,
      NoResourceFoundException.class,
      MethodArgumentTypeMismatchException.class,
    }
  )
  public ModelAndView handleNotFound() {
    Map<String, Object> model = new ModelMap();
    model.put(ERROR_KEY, "Page not found");
    return new ModelAndView("pages/error", model, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ModelAndView handleMethodNotAllowed() {
    Map<String, Object> model = new ModelMap();
    model.put(ERROR_KEY, "Method not allowed");
    return new ModelAndView("pages/error", model, HttpStatus.METHOD_NOT_ALLOWED);
  }

  @ExceptionHandler(Exception.class)
  public ModelAndView handleUnexpectedError(Exception ex) {
    LOGGER.log(Level.SEVERE, "Unhandled error", ex);
    Map<String, Object> model = new ModelMap();
    model.put(ERROR_KEY, "An unexpected error occurred");
    return new ModelAndView("pages/error", model, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
