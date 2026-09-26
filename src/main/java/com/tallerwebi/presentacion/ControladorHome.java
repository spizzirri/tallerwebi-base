package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioSocio;
import com.tallerwebi.dominio.ServicioTurno;
import com.tallerwebi.dominio.Socio;
import com.tallerwebi.dominio.Turno;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorHome extends ControladorConSocio {

  private ServicioTurno servicioTurno;

  @Autowired
  public ControladorHome(ServicioSocio servicioSocio, ServicioTurno servicioTurno) {
    super(servicioSocio);
    this.servicioTurno = servicioTurno;
  }

  @RequestMapping(path = "/home", method = RequestMethod.GET)
  public ModelAndView irAHome(HttpSession sesion) {
    Map<String, Object> modelo = new ModelMap();
    Socio socio = socioLogueado(sesion);
    modelo.put("socio", socio);
    if (socio != null) {
      modelo.put("proximosTurnos", proximosTurnos(socio));
    }
    return new ModelAndView("home", modelo);
  }

  private List<Turno> proximosTurnos(Socio socio) {
    LocalDate hoy = LocalDate.now();
    return servicioTurno
      .turnosDe(socio)
      .stream()
      .filter(t -> t.estaConfirmado() && !t.getFecha().isBefore(hoy))
      .sorted((a, b) -> a.getFecha().atTime(a.getHora()).compareTo(b.getFecha().atTime(b.getHora()))
      )
      .collect(Collectors.toList());
  }
}
