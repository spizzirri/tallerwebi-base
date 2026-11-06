package com.valhalla.presentation.shared;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

public final class SuccessRedirects {

  private SuccessRedirects() {}

  public static ModelAndView confirmed(String destination, String message) {
    var attributes = RequestContextHolder.getRequestAttributes();
    if (attributes instanceof ServletRequestAttributes servlet) {
      var flash = RequestContextUtils.getOutputFlashMap(servlet.getRequest());
      if (flash != null) {
        flash.put("successToast", message);
      }
    }
    return new ModelAndView(destination);
  }
}
