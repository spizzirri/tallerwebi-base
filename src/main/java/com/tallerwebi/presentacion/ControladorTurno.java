package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioCobertura;
import com.tallerwebi.dominio.ServicioSocio;
import com.tallerwebi.dominio.ServicioTurno;
import com.tallerwebi.dominio.ServicioVeterinaria;
import com.tallerwebi.dominio.Socio;
import com.tallerwebi.dominio.SolicitudTurno;
import com.tallerwebi.dominio.Veterinaria;
import com.tallerwebi.dominio.excepcion.TurnoInvalido;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorTurno extends ControladorConSocio {

  private static final String REDIRECT_HOME = "redirect:/home";
  private static final String REDIRECT_TURNOS = "redirect:/turnos";
  private static final String MENSAJE = "mensaje";
  private static final String ERROR = "error";

  private ServicioTurno servicioTurno;
  private ServicioVeterinaria servicioVeterinaria;
  private ServicioCobertura servicioCobertura;

  @Autowired
  public ControladorTurno(
    ServicioSocio servicioSocio,
    ServicioTurno servicioTurno,
    ServicioVeterinaria servicioVeterinaria,
    ServicioCobertura servicioCobertura
  ) {
    super(servicioSocio);
    this.servicioTurno = servicioTurno;
    this.servicioVeterinaria = servicioVeterinaria;
    this.servicioCobertura = servicioCobertura;
  }

  /**
   * El formulario se va completando por pasos: cada vez que el socio cambia la mascota, la
   * veterinaria o la fecha, se vuelve a pedir esta página con lo elegido hasta el momento.
   */
  @RequestMapping(path = "/turnos/nuevo", method = RequestMethod.GET)
  public ModelAndView nuevoTurno(
    @ModelAttribute("solicitud") SolicitudTurno solicitud,
    HttpSession sesion
  ) {
    Socio socio = socioLogueado(sesion);
    if (socio == null) {
      return new ModelAndView(REDIRECT_HOME);
    }
    return vistaNuevoTurno(socio, solicitud, null);
  }

  @RequestMapping(path = "/turnos", method = RequestMethod.POST)
  public ModelAndView reservar(
    @ModelAttribute("solicitud") SolicitudTurno solicitud,
    HttpSession sesion,
    RedirectAttributes redirectAttributes
  ) {
    Socio socio = socioLogueado(sesion);
    if (socio == null) {
      return new ModelAndView(REDIRECT_HOME);
    }
    try {
      servicioTurno.reservar(socio, solicitud);
    } catch (TurnoInvalido e) {
      return vistaNuevoTurno(socio, solicitud, e.getMessage());
    }
    redirectAttributes.addFlashAttribute(MENSAJE, "¡Listo! Tu turno quedó confirmado.");
    return new ModelAndView(REDIRECT_TURNOS);
  }

  @RequestMapping(path = "/turnos", method = RequestMethod.GET)
  public ModelAndView misTurnos(HttpSession sesion) {
    Socio socio = socioLogueado(sesion);
    if (socio == null) {
      return new ModelAndView(REDIRECT_HOME);
    }
    Map<String, Object> modelo = new ModelMap();
    modelo.put("turnos", servicioTurno.turnosDe(socio));
    return new ModelAndView("mis-turnos", modelo);
  }

  @RequestMapping(path = "/turnos/{id}/cancelar", method = RequestMethod.POST)
  public ModelAndView cancelar(
    @PathVariable("id") Long turnoId,
    HttpSession sesion,
    RedirectAttributes redirectAttributes
  ) {
    Socio socio = socioLogueado(sesion);
    if (socio == null) {
      return new ModelAndView(REDIRECT_HOME);
    }
    try {
      servicioTurno.cancelar(socio, turnoId);
      redirectAttributes.addFlashAttribute(MENSAJE, "Cancelaste el turno.");
    } catch (TurnoInvalido e) {
      redirectAttributes.addFlashAttribute(ERROR, e.getMessage());
    }
    return new ModelAndView(REDIRECT_TURNOS);
  }

  private ModelAndView vistaNuevoTurno(Socio socio, SolicitudTurno solicitud, String error) {
    Veterinaria veterinaria = servicioVeterinaria.buscarPorId(solicitud.getVeterinariaId());

    Map<String, Object> modelo = new ModelMap();
    modelo.put("solicitud", solicitud);
    modelo.put("mascotas", socio.getMascotas());
    modelo.put("veterinarias", servicioVeterinaria.listar(null));
    modelo.put(
      "servicios",
      servicioCobertura.coberturaEn(veterinaria, socio.buscarMascota(solicitud.getMascotaId()))
    );
    modelo.put("horarios", servicioTurno.horariosDisponibles(veterinaria, solicitud.getFecha()));
    modelo.put("hoy", LocalDate.now());
    if (error != null) {
      modelo.put(ERROR, error);
    }
    return new ModelAndView("nuevo-turno", modelo);
  }
}
