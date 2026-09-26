package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioCobertura;
import com.tallerwebi.dominio.ServicioVeterinaria;
import com.tallerwebi.dominio.Veterinaria;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorVeterinaria {

  private ServicioVeterinaria servicioVeterinaria;
  private ServicioCobertura servicioCobertura;

  @Autowired
  public ControladorVeterinaria(
    ServicioVeterinaria servicioVeterinaria,
    ServicioCobertura servicioCobertura
  ) {
    this.servicioVeterinaria = servicioVeterinaria;
    this.servicioCobertura = servicioCobertura;
  }

  @RequestMapping(path = "/veterinarias", method = RequestMethod.GET)
  public ModelAndView listarVeterinarias(
    @RequestParam(name = "servicioId", required = false) Long servicioId
  ) {
    List<Veterinaria> veterinarias = servicioVeterinaria.listar(servicioId);

    Map<String, Object> modelo = new ModelMap();
    modelo.put("veterinarias", veterinarias);
    modelo.put("servicios", servicioCobertura.listarServicios());
    modelo.put("servicioId", servicioId);
    return new ModelAndView("veterinarias", modelo);
  }
}
