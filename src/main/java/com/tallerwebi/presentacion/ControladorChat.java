package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioAsistente;
import com.tallerwebi.dominio.ServicioSocio;
import com.tallerwebi.dominio.Socio;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorChat extends ControladorConSocio {

  private final ServicioAsistente servicioAsistente;

  @Autowired
  public ControladorChat(ServicioSocio servicioSocio, ServicioAsistente servicioAsistente) {
    super(servicioSocio);
    this.servicioAsistente = servicioAsistente;
  }

  @GetMapping("/chat")
  public ModelAndView irAlChat(HttpSession sesion) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("socio", socioLogueado(sesion));
    return new ModelAndView("chat", modelo);
  }

  @PostMapping("/chat/mensaje")
  @ResponseBody
  public Map<String, String> mensaje(@RequestParam("mensaje") String mensaje, HttpSession sesion) {
    Socio socio = socioLogueado(sesion);
    if (socio == null) {
      throw new ChatNoAutorizado();
    }
    String respuesta = servicioAsistente.enviarMensaje(sesion, mensaje);
    return Map.of("respuesta", respuesta);
  }

  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  private static class ChatNoAutorizado extends RuntimeException {

    private static final long serialVersionUID = 1L;
  }
}
