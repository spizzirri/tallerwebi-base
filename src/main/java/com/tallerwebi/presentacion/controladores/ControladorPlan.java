package com.tallerwebi.presentacion.controladores;

import com.tallerwebi.dominio.Plan;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorPlan {

  @RequestMapping(path = "/planes", method = RequestMethod.GET)
  public ModelAndView mostrarPlanes() {
    List<Plan> planes = List.of(
      new Plan(
        1L,
        "Plan Básico",
        1,
        "Prevención y atención primaria. Incluye clínica general y vacunación.",
        20000.0
      ),
      new Plan(
        2L,
        "Plan Premium",
        2,
        "Cobertura de alta complejidad. Incluye especialistas y estudios complejos.",
        50000.0
      )
    );

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("planes", planes);

    return new ModelAndView("planes", modelo);
  }
}
