package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Mail.ServicioEmail;
import com.tallerwebi.dominio.Usuario.ServicioLogin;
import com.tallerwebi.dominio.Usuario.ServicioRecuperacionContrasenia;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import java.time.LocalDateTime;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorLogin {

  private final ServicioLogin servicioLogin;
  private final ServicioRecuperacionContrasenia servicioRecuperacionContrasenia;
  private final ServicioEmail servicioEmail;

  private static final String CODIGO_RECUPERACION = "codigoRecuperacion";
  private static final String CODIGO_EXPIRACION = "codigoExpiracion";
  private static final String CODIGO_VERIFICADO = "codigoVerificado";
  private static final String EMAIL_RECUPERACION = "emailRecuperacion";

  @Autowired
  public ControladorLogin(
    ServicioLogin servicioLogin,
    ServicioRecuperacionContrasenia servicioRecuperacionContrasenia,
    ServicioEmail servicioEmail
  ) {
    this.servicioLogin = servicioLogin;
    this.servicioRecuperacionContrasenia = servicioRecuperacionContrasenia;
    this.servicioEmail = servicioEmail;
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
  public ResponseEntity<String> verificarEmail(@RequestParam String email, HttpSession session) {
    session.removeAttribute(CODIGO_RECUPERACION);
    session.removeAttribute(CODIGO_EXPIRACION);
    session.removeAttribute(CODIGO_VERIFICADO);
    session.removeAttribute(EMAIL_RECUPERACION);

    Usuario usuario = new Usuario();
    usuario.setEmail(email);

    if (!servicioLogin.usuarioYaExiste(usuario)) {
      return ResponseEntity.notFound().build();
    }

    String codigo = servicioRecuperacionContrasenia.generarCodigo();

    session.setAttribute(CODIGO_RECUPERACION, codigo);
    session.setAttribute(EMAIL_RECUPERACION, email);
    session.setAttribute(CODIGO_EXPIRACION, LocalDateTime.now().plusMinutes(10));

    String asunto = "Recuperación de contraseña - KioNet";

    String mensaje =
      "Hola.\n\n" +
      "Recibimos una solicitud para recuperar la contraseña de tu cuenta de KioNet.\n\n" +
      "Tu código de verificación es:\n\n" +
      codigo +
      "\n\n" +
      "Este código es válido durante 10 minutos.\n\n" +
      "Si no solicitaste este cambio, simplemente ignorá este correo.\n\n" +
      "Equipo de KioNet.";

    servicioEmail.enviarEmail(email, asunto, mensaje);

    return ResponseEntity.ok().build();
  }

  @RequestMapping(path = "/verificar-codigo", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<String> verificarCodigo(@RequestParam String codigo, HttpSession session) {
    String codigoGuardado = (String) session.getAttribute(CODIGO_RECUPERACION);

    LocalDateTime expiracion = (LocalDateTime) session.getAttribute(CODIGO_EXPIRACION);

    if (codigoGuardado == null || expiracion == null) {
      return ResponseEntity.badRequest().build();
    }

    if (LocalDateTime.now().isAfter(expiracion)) {
      session.removeAttribute(CODIGO_RECUPERACION);
      session.removeAttribute(CODIGO_EXPIRACION);
      return ResponseEntity.badRequest().build();
    }

    if (!codigoGuardado.equals(codigo)) {
      return ResponseEntity.badRequest().build();
    }

    session.setAttribute(CODIGO_VERIFICADO, true);

    session.removeAttribute(CODIGO_RECUPERACION);
    session.removeAttribute(CODIGO_EXPIRACION);

    return ResponseEntity.ok().build();
  }

  @RequestMapping(path = "/cambiarContrasenia", method = RequestMethod.GET)
  public ModelAndView irACambiarContrasenia() {
    return new ModelAndView("cambiarContrasenia");
  }

  @RequestMapping(path = "/actualizar-contrasenia", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<Void> actualizarContrasenia(
    @RequestParam String email,
    @RequestParam String nuevaClave,
    HttpSession session
  ) {
    if (nuevaClave == null) {
      return ResponseEntity.badRequest().build();
    }

    Boolean codigoVerificado = (Boolean) session.getAttribute(CODIGO_VERIFICADO);

    if (codigoVerificado == null || !codigoVerificado) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    String emailRecuperacion = (String) session.getAttribute(EMAIL_RECUPERACION);

    if (emailRecuperacion == null || !email.equals(emailRecuperacion)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    servicioLogin.cambiarContrasenia(email, nuevaClave);

    // Limpiar la sesión
    session.removeAttribute(CODIGO_RECUPERACION);
    session.removeAttribute(CODIGO_EXPIRACION);
    session.removeAttribute(CODIGO_VERIFICADO);
    session.removeAttribute(EMAIL_RECUPERACION);

    return ResponseEntity.ok().build();
    //    ModelMap model = new ModelMap();
    //    model.put("exito", "Su contraseña fue cambiada exitosamente");
    //    return new ModelAndView("cambiarContrasenia", model);
  }
}
