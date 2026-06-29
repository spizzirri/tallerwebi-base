package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Hijos.Curso;
import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Hijos.ServicioHijo;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.AliasExistenteException;
import com.tallerwebi.dominio.excepcion.AliasInvalidoException;
import com.tallerwebi.dominio.excepcion.AliasVacioException;
import com.tallerwebi.dominio.excepcion.HijoExistenteException;
import com.tallerwebi.dominio.excepcion.HijoNoEncontradoException;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HijosControlador {

  ServicioHijo servicioHijo;
  private static final String VISTA_HIJOS = "vistaHijos";
  private static final String USUARIO_SESSION = "USUARIO";
  private static final String USUARIO_MODEL = "usuario";
  private static final String REDIRECT_LOGIN = "redirect:/login";
  private static final String REDIRECT_VISTA_HIJOS = "redirect:/vistaHijos";

  public HijosControlador(ServicioHijo servicioHijo) {
    this.servicioHijo = servicioHijo;
  }

  @RequestMapping(path = "/vistaHijos", method = RequestMethod.GET)
  public ModelAndView irAvistaHijos(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO_SESSION);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    ModelMap model = new ModelMap();
    ModelAndView mv = new ModelAndView();
    model.put(USUARIO_MODEL, usuario);
    model.put("hijo", new Hijo()); // ← para el formulario oculto, agrego un hijo vacio

    List<Hijo> hijos = this.servicioHijo.obtenerHijosPorUsuario(usuario.getId());
    if (hijos == null || hijos.isEmpty()) {
      model.put("mensajeError", "Aún no tenés hijos registrados");
    } else {
      model.put("hijos", hijos);
    }
    mv.setViewName(VISTA_HIJOS);
    mv.addAllObjects(model);
    return mv;
  }

  @RequestMapping(path = "/guardarHijo", method = RequestMethod.POST)
  public ModelAndView guardarHijos(
    @ModelAttribute("hijo") Hijo hijo,
    @RequestParam String anio,
    @RequestParam String division,
    @RequestParam("fotoPerfilHijo") MultipartFile fotoPerfil,
    HttpSession session,
    RedirectAttributes flash
  ) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO_SESSION);

    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    try {
      String cursoNombre = anio + "_" + division;
      Curso curso = Curso.valueOf(cursoNombre);
      hijo.setCurso(curso);
      servicioHijo.guardarHijo(hijo, fotoPerfil, usuario);

      flash.addFlashAttribute("exito", "¡Hijo registrado con éxito en el sistema!");
    } catch (HijoExistenteException e) {
      ModelMap model = new ModelMap();
      model.put("error", "El hijo ya se encuentra registrado");
      return new ModelAndView("vistaHijos", model);
    } catch (IllegalArgumentException e) {
      return devolverVistaConError(usuario, "El año o división seleccionados no son válidos");
    }

    return new ModelAndView(REDIRECT_VISTA_HIJOS);
  }

  @RequestMapping(path = "/editarHijo", method = RequestMethod.POST)
  public ModelAndView editarHijo(
    @Valid @ModelAttribute("datosEditarHijo") DatosEditarHijoDTO datosEditarHijo,
    BindingResult bindingResult,
    HttpSession session,
    RedirectAttributes flash
  ) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO_SESSION);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    // Si las anotaciones del DTO (como @NotNull) fallan, manejamos el error acá
    if (bindingResult.hasErrors()) {
      return devolverVistaConError(usuario, "Hay campos inválidos en el formulario de edición.");
    }

    try {
      String cursoNombre = datosEditarHijo.getAnio() + "_" + datosEditarHijo.getDivision();
      Curso curso = Curso.valueOf(cursoNombre);

      Hijo datosNuevos = new Hijo();
      datosNuevos.setNombre(datosEditarHijo.getNombreH());
      datosNuevos.setApellido(datosEditarHijo.getApellidoH());
      datosNuevos.setFechaNac(datosEditarHijo.getFechaH());
      datosNuevos.setCurso(curso);
      datosNuevos.setDni(datosEditarHijo.getDniH());
      datosNuevos.setAliasRetiro(datosEditarHijo.getAliasRetiro());
      MultipartFile foto = datosEditarHijo.getFotoPerfilH();

      servicioHijo.editarHijo(datosEditarHijo.getIdHijo(), datosNuevos, foto, usuario);
      flash.addFlashAttribute("exito", "La información de tu hijo fue actualizada correctamente.");
    } catch (HijoNoEncontradoException e) {
      return devolverVistaConError(usuario, "El hijo no existe o no pertenece al usuario");
    } catch (IllegalArgumentException e) {
      return devolverVistaConError(usuario, "El año o división seleccionados no son válidos");
    } catch (AliasExistenteException e) {
      return devolverVistaConError(usuario, "El alias ya está en uso.");
    } catch (AliasVacioException e) {
      return devolverVistaConError(usuario, e.getMessage());
    } catch (AliasInvalidoException e) {
      return devolverVistaConError(usuario, e.getMessage());
    }

    return new ModelAndView(REDIRECT_VISTA_HIJOS);
  }

  @RequestMapping(path = "/credenciales", method = RequestMethod.GET)
  public ModelAndView irACredenciales(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO_SESSION);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    ModelMap model = new ModelMap();
    model.put(USUARIO_MODEL, usuario);

    List<Hijo> hijos = this.servicioHijo.obtenerHijosPorUsuario(usuario.getId());
    model.put("hijos", hijos);

    return new ModelAndView("credenciales", model);
  }

  @RequestMapping(path = "/guardar-alias", method = RequestMethod.POST)
  public ModelAndView guardarAlias(
    @RequestParam Long hijoId,
    @RequestParam String aliasRetiro,
    HttpSession session
  ) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO_SESSION);

    try {
      servicioHijo.actualizarAlias(hijoId, aliasRetiro, usuario);

      return new ModelAndView(REDIRECT_VISTA_HIJOS);
    } catch (AliasExistenteException e) {
      return devolverVistaConError(usuario, "Ese alias ya está en uso.");
    } catch (AliasVacioException e) {
      return devolverVistaConError(usuario, e.getMessage());
    } catch (AliasInvalidoException e) {
      return devolverVistaConError(usuario, e.getMessage());
    }
  }

  @RequestMapping(path = "/eliminarHijo", method = RequestMethod.POST)
  public ModelAndView darDeBajaUnHijo(
    @RequestParam Long hijoId,
    HttpSession session,
    RedirectAttributes flash
  ) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO_SESSION);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    try {
      servicioHijo.eliminarHijo(hijoId, usuario);
      flash.addFlashAttribute("exito", "El hijo fue dado de baja correctamente.");
    } catch (HijoNoEncontradoException e) {
      flash.addFlashAttribute("error", "No se pudo eliminar: el hijo no existe o no te pertenece.");
    }

    return new ModelAndView(REDIRECT_VISTA_HIJOS);
  }

  // Métodos auxiliares
  private ModelAndView devolverVistaConError(Usuario usuario, String mensajeError) {
    ModelMap model = new ModelMap();
    model.put(USUARIO_MODEL, usuario);
    model.put("hijo", new Hijo()); // Formulario modal vacío
    model.put("error", mensajeError);

    // Volvemos a buscar la lista para que no desaparezcan las tarjetas de los hijos al recargar con error
    List<Hijo> hijos = this.servicioHijo.obtenerHijosPorUsuario(usuario.getId());
    if (hijos != null && !hijos.isEmpty()) {
      model.put("hijos", hijos);
    }

    return new ModelAndView(VISTA_HIJOS, model);
  }
}
