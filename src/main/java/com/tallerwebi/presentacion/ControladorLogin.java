package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario.ServicioLogin;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorLogin {

  private final ServicioLogin servicioLogin;

  @Autowired
  public ControladorLogin(ServicioLogin servicioLogin) {
    this.servicioLogin = servicioLogin;
  }

  @RequestMapping("/login")
  public ModelAndView irALogin(HttpServletRequest request) {
    ModelMap modelo = new ModelMap();
    DatosLogin datosLogin = new DatosLogin();

    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("emailRecordado".equals(cookie.getName())) {
          datosLogin.setEmail(cookie.getValue());
          break;
        }
      }
    }
    modelo.put("datosLogin", datosLogin);
    return new ModelAndView("login", modelo);
  }

  @RequestMapping(path = "/validar-login", method = RequestMethod.POST)
  public ModelAndView validarLogin(
    @ModelAttribute("datosLogin") DatosLogin datosLogin,
    HttpServletRequest request,
    HttpServletResponse response
  ) {
    Usuario usuarioBuscado = servicioLogin.consultarUsuarioLogin(
      datosLogin.getEmail(),
      datosLogin.getPassword()
    );
    if (usuarioBuscado != null) {
      request.getSession().setAttribute("ROL", usuarioBuscado.getRol());
      request.getSession().setAttribute("USUARIO", usuarioBuscado);

      if (Boolean.TRUE.equals(datosLogin.getRememberMe())) {
        Cookie cookie = new Cookie("emailRecordado", datosLogin.getEmail());
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 días
        cookie.setPath("/");
        response.addCookie(cookie);
      }
      // VALIDACIÓN DE ROL PARA REDIRECCIÓN INICIAL
      if ("KIOSQUERO".equals(usuarioBuscado.getRol())) {
        return new ModelAndView("redirect:/homeKiosquero");
      }

      return new ModelAndView("redirect:/home");
    } else {
      /* Se instancia el ModelMap solo cuando es necesario (en el flujo de error) para evitar anomalías en el flujo de datos (DU-anomaly de PMD) */
      ModelMap model = new ModelMap();
      model.put("error", "Usuario o clave incorrecta");
      return new ModelAndView("login", model);
    }
  }

  @RequestMapping(path = "/registrarme", method = RequestMethod.POST)
  public ModelAndView registrarme(@ModelAttribute("usuario") Usuario usuario) {
    ModelMap model = new ModelMap();
    try {
      usuario.setRol("CLIENTE");
      servicioLogin.registrar(usuario);
    } catch (UsuarioExistente e) {
      model.put("error", "El usuario ya existe");
      return new ModelAndView("nuevo-usuario", model);
    } catch (Exception e) {
      model.put("error", "Error al registrar el nuevo usuario");
      return new ModelAndView("nuevo-usuario", model);
    }
    return new ModelAndView("redirect:/login");
  }

  @RequestMapping(path = "/nuevo-usuario", method = RequestMethod.GET)
  public ModelAndView nuevoUsuario() {
    ModelMap model = new ModelMap();
    model.put("usuario", new Usuario());
    return new ModelAndView("nuevo-usuario", model);
  }

  @RequestMapping(path = "/", method = RequestMethod.GET)
  public ModelAndView inicio() {
    return new ModelAndView("redirect:/login");
  }

  @RequestMapping(path = "/logout", method = RequestMethod.POST)
  public ModelAndView logout(HttpServletRequest request) {
    request.getSession().invalidate();

    return new ModelAndView("redirect:/login");
  }

  @RequestMapping(path = "/verificar-email", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<String> verificarEmail(@RequestParam String email) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    if (servicioLogin.usuarioYaExiste(usuario)) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
  }

  @RequestMapping(path = "/cambiarContrasenia", method = RequestMethod.GET)
  public ModelAndView irACambiarContrasenia() {
    return new ModelAndView("cambiarContrasenia");
  }

  @RequestMapping(path = "/actualizar-contrasenia", method = RequestMethod.POST)
  public ModelAndView actualizarContrasenia(
    @RequestParam String email,
    @RequestParam String nuevaClave
  ) {
    servicioLogin.cambiarContrasenia(email, nuevaClave);
    ModelMap model = new ModelMap();
    model.put("exito", "Su contraseña fue cambiada exitosamente");
    return new ModelAndView("cambiarContrasenia", model);
  }
}
