package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario.ServicioUsuario;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.NoSePudoGuardarInformacionException;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PerfilControlador {

  private final ServicioUsuario servicioUsuario;
  private static final String VISTA_PERFIL = "perfil";
  private static final String VISTA_HIJOS = "vistaHijos";
  private static final String USUARIO_SESSION = "USUARIO";
  private static final String USUARIO_MODEL = "usuario";
  private static final String REDIRECT_LOGIN = "redirect:/login";

  @Autowired
  public PerfilControlador(ServicioUsuario servicioUsuario) {
    this.servicioUsuario = servicioUsuario;
  }

  @RequestMapping(path = "/perfil", method = RequestMethod.GET)
  public ModelAndView irAlPerfil(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO_SESSION);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    ModelMap model = new ModelMap();
    ModelAndView mv = new ModelAndView();
    model.put(USUARIO_MODEL, usuario);

    mv.setViewName(VISTA_PERFIL);
    mv.addAllObjects(model);
    return mv;
  }

  public ModelAndView mostrarDatosUsuario(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO_SESSION);
    ModelAndView mv = new ModelAndView();
    mv.addObject(USUARIO_MODEL, usuario);
    mv.setViewName(VISTA_PERFIL);
    return mv;
  }

  @RequestMapping(path = "/editarPerfil", method = RequestMethod.POST)
  public ModelAndView editarPerfil(
    @Valid DatosEditarPerfilDTO datosEditarPerfil,
    BindingResult bindingResult,
    HttpSession session,
    RedirectAttributes flash
  ) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO_SESSION);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    if (bindingResult.hasErrors()) {
      return devolverVistaError(usuario, "Hay campos inválidos");
    }

    //refresco el usuario con los datos actualizados
    ModelAndView errorMail = editarMail(usuario, datosEditarPerfil.getEmail());
    if (errorMail != null) {
      return errorMail;
    }

    ModelAndView errorCelular = editarCelular(usuario, datosEditarPerfil.getCelular());
    if (errorCelular != null) {
      return errorCelular;
    }

    ModelAndView errorFoto = editarFoto(usuario, datosEditarPerfil.getFotoPerfil());
    if (errorFoto != null) {
      return errorFoto;
    }
    Usuario usuarioActualizado = servicioUsuario.buscarPorId(usuario.getId());
    session.setAttribute(USUARIO_SESSION, usuarioActualizado);
    flash.addFlashAttribute("exito", "¡Tu perfil se ha actualizado con éxito!");

    return new ModelAndView("redirect:/perfil");
  }

  //metodos auxiliaressssss-------------------

  private ModelAndView editarFoto(Usuario usuario, MultipartFile fotoPerfil) {
    if (fotoPerfil == null || fotoPerfil.isEmpty()) {
      return null;
    }

    try {
      servicioUsuario.actualizarFoto(usuario.getId(), fotoPerfil);
      return null;
    } catch (NoSePudoGuardarInformacionException e) {
      return devolverVistaError(usuario, e.getMessage());
    }
  }

  private ModelAndView editarCelular(Usuario usuario, String celular) {
    if (celular == null || celular.isEmpty()) {
      return null;
    }

    try {
      Long celularLong = Long.parseLong(celular);
      servicioUsuario.actualizarCelular(usuario.getId(), celularLong);
      return null;
    } catch (NumberFormatException e) {
      return devolverVistaError(usuario, "El celular debe contener solo números");
    } catch (NoSePudoGuardarInformacionException e) {
      return devolverVistaError(usuario, e.getMessage());
    }
  }

  private ModelAndView editarMail(Usuario usuario, String email) {
    if (email == null || email.isEmpty()) {
      return null;
    }

    try {
      servicioUsuario.actualizarMail(usuario.getId(), email);
      return null;
    } catch (NoSePudoGuardarInformacionException e) {
      return devolverVistaError(usuario, e.getMessage());
    }
  }

  private ModelAndView devolverVistaError(Usuario usuario, String mensaje) {
    ModelMap modelo = new ModelMap();
    modelo.put(USUARIO_MODEL, usuario);
    modelo.put("mensajeError", mensaje);

    return new ModelAndView(VISTA_PERFIL, modelo);
  }

  @org.springframework.web.bind.annotation.ExceptionHandler(
    org.springframework.web.multipart.MaxUploadSizeExceededException.class
  )
  public ModelAndView manejarErrorArchivoMuyGrande(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO_SESSION);

    return devolverVistaError(usuario, "La imagen supera el tamaño máximo permitido de 5MB");
  }

  @RequestMapping(path = "/perfil/eliminar-cuenta", method = RequestMethod.POST)
  public ModelAndView eliminarCuenta(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute(USUARIO_SESSION);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    servicioUsuario.eliminarCuenta(usuario.getId());
    session.invalidate();
    return new ModelAndView(REDIRECT_LOGIN);
  }
}
