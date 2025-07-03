package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

@Controller
public class ControladorLogin {

    private ServicioLogin servicioLogin;

    @Autowired
    public ControladorLogin(ServicioLogin servicioLogin) {
        this.servicioLogin = servicioLogin;
    }

    @GetMapping("/login")
    public ModelAndView irALoginConGet(@RequestParam(value = "redirectUrl", required = false) String redirectUrl) {
        ModelMap modelo = new ModelMap();
        modelo.put("datosLogin", new DatosLoginDto());
        modelo.put("redirectUrl", redirectUrl != null ? redirectUrl : "/index");
        return new ModelAndView("login", modelo);
    }
    @PostMapping("/login")
    public ModelAndView irALogin(@RequestParam(value = "redirectUrl", required = false) String redirectUrl) {
        ModelMap modelo = new ModelMap();
        modelo.put("datosLogin", new DatosLoginDto());
        modelo.put("redirectUrl", redirectUrl != null ? redirectUrl : "/index");
        return new ModelAndView("login", modelo);
    }

    @PostMapping("/validar-login")
    public ModelAndView validarLogin(@ModelAttribute("datosLogin") DatosLoginDto datosLogin,
                                     @RequestParam("redirectUrl") String redirectUrl,
                                     HttpServletRequest request) {
        ModelMap model = new ModelMap();

        UsuarioDto usuarioBuscado = servicioLogin.consultarUsuario(datosLogin.getEmail(), datosLogin.getPassword());

        if (usuarioBuscado != null) {
            request.getSession().setAttribute("usuario", usuarioBuscado);
            return new ModelAndView("redirect:" + redirectUrl);
        } else {
            model.put("error", "Usuario o clave incorrecta");
            model.put("redirectUrl", redirectUrl); // Para mantenerlo si falla
            return new ModelAndView("login", model);
        }
    }
//    @RequestMapping(path = "/validar-login", method = RequestMethod.POST)
//    public ModelAndView validarLogin(@ModelAttribute("datosLogin") DatosLoginDto datosLogin,
//                                     @RequestParam("redirectUrl") String redirectUrl,
//                                     HttpServletRequest request) {
//        ModelMap model = new ModelMap();
//
//        Usuario usuarioBuscado = servicioLogin.consultarUsuario(datosLogin.getEmail(), datosLogin.getPassword());
//
//        if (usuarioBuscado != null) {
//            request.getSession().setAttribute("ROL", usuarioBuscado.getRol());
//
//            if (redirectUrl != null && redirectUrl.startsWith("/")) {
//                return new ModelAndView("redirect:" + redirectUrl);
//            }
//            return new ModelAndView("redirect:/home");
//        } else {
//            model.put("error", "Usuario o clave incorrecta");
//            return new ModelAndView("login", model);
//        }
//    }


    @RequestMapping(path = "/registrarme", method = RequestMethod.POST)
    public ModelAndView registrarme(@ModelAttribute("usuario") Usuario usuario,
                                    @RequestParam("redirectUrl") String redirectUrl) {
        ModelMap model = new ModelMap();
        model.put("redirectUrl", redirectUrl != null ? redirectUrl : "/index");
        try {
            servicioLogin.registrar(usuario);
        } catch (UsuarioExistente e) {
            model.put("error", "El usuario ya existe");
            return new ModelAndView("nuevo-usuario", model);
        } catch (Exception e) {
            model.put("error", "Error al registrar el nuevo usuario");
            return new ModelAndView("nuevo-usuario", model);
        }
        return new ModelAndView("login", model);
    }

    @GetMapping("/registrarme")
    public ModelAndView mostrarFormularioRegistro(@RequestParam(value = "redirectUrl", required = false) String redirectUrl) {
        ModelMap model = new ModelMap();
        model.put("usuario", new Usuario()); // objeto vacío para el form
        model.put("redirectUrl", redirectUrl != null ? redirectUrl : "/index");
        return new ModelAndView("nuevo-usuario", model);
    }


    @RequestMapping(path = "/nuevo-usuario", method = RequestMethod.POST)
    public ModelAndView nuevoUsuario(@RequestParam("redirectUrl") String redirectUrl) {
        ModelMap model = new ModelMap();
        model.put("redirectUrl", redirectUrl != null ? redirectUrl : "/index");
        model.put("usuario", new Usuario());
        return new ModelAndView("nuevo-usuario", model);
    }
    @GetMapping(path = "/nuevo-usuario")
    public ModelAndView nuevoUsuarioConGet(@RequestParam(value = "redirectUrl", required = false) String redirectUrl) {
        ModelMap model = new ModelMap();
        model.put("redirectUrl", redirectUrl != null ? redirectUrl : "/index");
        model.put("usuario", new Usuario());
        return new ModelAndView("nuevo-usuario", model);
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public ModelAndView logout(HttpServletRequest request) {
        request.getSession().removeAttribute("usuario");
        return new ModelAndView("redirect:/index");
    }

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public ModelAndView inicio() {
        return new ModelAndView("redirect:/index");
    }
}