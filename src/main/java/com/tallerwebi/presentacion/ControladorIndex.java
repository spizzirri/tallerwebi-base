package com.tallerwebi.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
public class ControladorIndex {

    @GetMapping("/index")
    public ModelAndView irAHome() {
        return new ModelAndView("index");
    }
}
