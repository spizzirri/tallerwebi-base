package com.valhalla.presentation.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FlashMap;

class SuccessRedirectsTest {

  @Test
  void carriesConfirmationInFlashWithoutAddingItToTheUrl() {
    var request = new MockHttpServletRequest();
    var flash = new FlashMap();
    request.setAttribute(DispatcherServlet.OUTPUT_FLASH_MAP_ATTRIBUTE, flash);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    try {
      var result = SuccessRedirects.confirmed("redirect:/plans", "Plan eliminado.");
      assertEquals("redirect:/plans", result.getViewName());
      assertEquals("Plan eliminado.", flash.get("successToast"));
      assertEquals(0, result.getModel().size());
    } finally {
      RequestContextHolder.resetRequestAttributes();
    }
  }
}
