package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioCobertura;
import com.tallerwebi.dominio.ServicioTurno;
import com.tallerwebi.dominio.ServicioVeterinaria;
import com.tallerwebi.dominio.Turno;
import com.tallerwebi.dominio.Veterinaria;
import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;
import com.tallerwebi.dominio.excepcion.TurnoInvalido;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.servlet.http.HttpSession;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorPanelVeterinaria extends ControladorConVeterinaria {

  private static final String REDIRECT_PANEL = "redirect:/veterinaria/panel";
  private static final String REDIRECT_TURNOS = "redirect:/veterinaria/turnos";
  private static final String REDIRECT_HOME = "redirect:/home";
  private static final String ERROR = "error";

  private final ServicioVeterinaria servicioVeterinaria;
  private final ServicioCobertura servicioCobertura;
  private final ServicioTurno servicioTurno;

  @Autowired
  public ControladorPanelVeterinaria(
    ServicioVeterinaria servicioVeterinaria,
    ServicioCobertura servicioCobertura,
    ServicioTurno servicioTurno
  ) {
    super(servicioVeterinaria);
    this.servicioVeterinaria = servicioVeterinaria;
    this.servicioCobertura = servicioCobertura;
    this.servicioTurno = servicioTurno;
  }

  @RequestMapping(path = "/veterinaria/panel", method = RequestMethod.GET)
  public ModelAndView panel(HttpSession sesion) {
    Veterinaria veterinaria = veterinariaLogueada(sesion);
    return veterinaria == null
      ? new ModelAndView(REDIRECT_HOME)
      : vistaPerfil(veterinaria, null, null);
  }

  @RequestMapping(path = "/veterinaria/inicio", method = RequestMethod.GET)
  public ModelAndView inicio(HttpSession sesion) {
    Veterinaria veterinaria = veterinariaLogueada(sesion);
    if (veterinaria == null) {
      return new ModelAndView(REDIRECT_HOME);
    }
    Map<String, Object> modelo = new ModelMap();
    modelo.put("veterinaria", veterinaria);
    modelo.put("turnos", servicioTurno.turnosDe(veterinaria));
    return new ModelAndView("inicio-veterinaria", modelo);
  }

  @RequestMapping(path = "/veterinaria/perfil", method = RequestMethod.POST)
  public ModelAndView actualizarPerfil(
    @ModelAttribute("datosVeterinaria") DatosVeterinaria datos,
    HttpSession sesion,
    RedirectAttributes redirectAttributes
  ) {
    Veterinaria veterinaria = veterinariaLogueada(sesion);
    if (veterinaria == null) {
      return new ModelAndView(REDIRECT_HOME);
    }
    try {
      servicioVeterinaria.actualizarPerfil(
        veterinaria.getUsuario().getId(),
        datos.crearDatosPerfil(),
        datos.getEmail(),
        datos.getServiciosIds()
      );
    } catch (UsuarioExistente e) {
      return vistaPerfil(veterinaria, datos, "Ya existe una cuenta con ese email");
    } catch (DatosDeRegistroInvalidos e) {
      return vistaPerfil(veterinaria, datos, e.getMessage());
    }
    redirectAttributes.addFlashAttribute("mensaje", "Actualizaste los datos de tu veterinaria.");
    return new ModelAndView(REDIRECT_PANEL);
  }

  @RequestMapping(path = "/veterinaria/turnos", method = RequestMethod.GET)
  public ModelAndView turnos(HttpSession sesion) {
    Veterinaria veterinaria = veterinariaLogueada(sesion);
    if (veterinaria == null) {
      return new ModelAndView(REDIRECT_HOME);
    }
    Map<String, Object> modelo = new ModelMap();
    modelo.put("turnos", servicioTurno.turnosDe(veterinaria));
    modelo.put("veterinaria", veterinaria);
    return new ModelAndView("turnos-veterinaria", modelo);
  }

  @RequestMapping(path = "/veterinaria/turnos/{id}/cancelar", method = RequestMethod.POST)
  public ModelAndView cancelar(
    @PathVariable("id") Long turnoId,
    HttpSession sesion,
    RedirectAttributes redirectAttributes
  ) {
    Veterinaria veterinaria = veterinariaLogueada(sesion);
    if (veterinaria == null) {
      return new ModelAndView("redirect:/home");
    }
    try {
      servicioTurno.cancelarPorVeterinaria(veterinaria, turnoId);
      redirectAttributes.addFlashAttribute("mensaje", "Cancelaste el turno.");
    } catch (TurnoInvalido e) {
      redirectAttributes.addFlashAttribute(ERROR, e.getMessage());
    }
    return new ModelAndView(REDIRECT_TURNOS);
  }

  @RequestMapping(path = "/veterinaria/turnos/{id}/reprogramar", method = RequestMethod.GET)
  public ModelAndView formularioReprogramar(
    @PathVariable("id") Long turnoId,
    @RequestParam(value = "fecha", required = false) @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE
    ) LocalDate fecha,
    HttpSession sesion,
    RedirectAttributes redirectAttributes
  ) {
    Veterinaria veterinaria = veterinariaLogueada(sesion);
    if (veterinaria == null) {
      return new ModelAndView(REDIRECT_HOME);
    }
    try {
      Turno turno = servicioTurno.buscarTurnoDeVeterinaria(veterinaria, turnoId);
      Map<String, Object> modelo = new ModelMap();
      modelo.put("turno", turno);
      modelo.put("fecha", fecha);
      modelo.put("horarios", servicioTurno.horariosDisponibles(veterinaria, fecha));
      modelo.put("hoy", LocalDate.now());
      modelo.put("esVeterinaria", true);
      modelo.put("urlFormulario", "/veterinaria/turnos/" + turnoId + "/reprogramar");
      modelo.put("urlPost", "/veterinaria/turnos/" + turnoId + "/reprogramar");
      modelo.put("urlVolver", "/veterinaria/turnos");
      return new ModelAndView("reprogramar-turno", modelo);
    } catch (TurnoInvalido e) {
      redirectAttributes.addFlashAttribute(ERROR, e.getMessage());
      return new ModelAndView(REDIRECT_TURNOS);
    }
  }

  @RequestMapping(path = "/veterinaria/turnos/{id}/reprogramar", method = RequestMethod.POST)
  public ModelAndView reprogramar(
    @PathVariable("id") Long turnoId,
    @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
    @RequestParam("hora") @DateTimeFormat(pattern = "HH:mm") LocalTime hora,
    HttpSession sesion,
    RedirectAttributes redirectAttributes
  ) {
    Veterinaria veterinaria = veterinariaLogueada(sesion);
    if (veterinaria == null) {
      return new ModelAndView(REDIRECT_HOME);
    }
    try {
      servicioTurno.reprogramarPorVeterinaria(veterinaria, turnoId, fecha, hora);
      redirectAttributes.addFlashAttribute("mensaje", "Reprogramaste el turno.");
    } catch (TurnoInvalido e) {
      redirectAttributes.addFlashAttribute(ERROR, e.getMessage());
      return new ModelAndView("redirect:/veterinaria/turnos/" + turnoId + "/reprogramar");
    }
    return new ModelAndView(REDIRECT_TURNOS);
  }

  private ModelAndView vistaPerfil(Veterinaria veterinaria, DatosVeterinaria datos, String error) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("datosVeterinaria", datos == null ? DatosVeterinaria.desde(veterinaria) : datos);
    modelo.put("diasSemana", DayOfWeek.values());
    modelo.put("servicios", servicioCobertura.listarServicios());
    modelo.put("veterinaria", veterinaria);
    if (error != null) {
      modelo.put(ERROR, error);
    }
    return new ModelAndView("panel-veterinaria", modelo);
  }
}
