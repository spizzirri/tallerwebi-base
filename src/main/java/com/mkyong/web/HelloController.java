package com.mkyong.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HelloController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String welcome(ModelMap model) {

        model.addAttribute("message", "Spring MVC Hello World");

        // view name, map to welcome.html later
        return "welcome";

    }

    @GetMapping("/hello/{name:.+}")
    public String hello(Model model, @PathVariable("name") String name) {

        model.addAttribute("message", name);

        // view name, map to welcome.html later
        return "welcome";
    }


}
