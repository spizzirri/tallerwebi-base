package com.tallerwebi.presentacion.Calendario;

import com.tallerwebi.dominio.Calendario.ServicioCalendario;
import com.tallerwebi.dominio.Usuario.Usuario;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CalendarioControlador {

  private static final String USUARIO_SESSION = "USUARIO";
  private final ServicioCalendario servicioCalendario;

  public CalendarioControlador(ServicioCalendario servicioCalendario) {
    this.servicioCalendario = servicioCalendario;
  }

  @GetMapping("/mi-calendario")
  public ModelAndView irAMiCalendario(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO_SESSION);
    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }
    ModelAndView model = new ModelAndView("mi-calendario");
    model.addObject("usuario", usuario);
    return model;
  }

  @GetMapping("/api/calendario")
  @ResponseBody
  public List<EventoCalendarioDTO> obtenerEventos(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO_SESSION);

    if (usuario == null) {
      return List.of();
    }
    return servicioCalendario.obtenerPedidosParaCalendarioDelUsuario(usuario.getId());
  }
}
