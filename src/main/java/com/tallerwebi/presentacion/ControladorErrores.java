package com.tallerwebi.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

@Controller
public class ControladorErrores {

    @ExceptionHandler(NullPointerException.class)
    public ModelAndView manejarNullPointerException(NullPointerException ex) {

        return new ModelAndView("error/500");
    }
    @RequestMapping("/error500")
    public ModelAndView manejarError500(Exception ex) {
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("mensaje", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @RequestMapping("/error404")
    public ModelAndView manejarError404(NoHandlerFoundException ex) {
        return new ModelAndView("error/404");
    }
}
