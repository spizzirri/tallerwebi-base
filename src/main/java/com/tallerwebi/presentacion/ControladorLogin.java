package com.tallerwebi.presentacion;

import com.tallerwebi.core.FragmentRenderer;
import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class ControladorLogin {

    private ServicioLogin servicioLogin;

    private FragmentRenderer fragmentRenderer;

    @Autowired
    public ControladorLogin(ServicioLogin servicioLogin, FragmentRenderer fragmentRenderer) {
        this.servicioLogin = servicioLogin;
        this.fragmentRenderer = fragmentRenderer;
    }


    @RequestMapping("/login")
    public ModelAndView irALogin() {
        ModelMap modelo = new ModelMap();
        modelo.put("datosLogin", new DatosLoginDto());
        return new ModelAndView("fragments/login", modelo);
    }

//    @RequestMapping(path = "/nuevo-login", method = RequestMethod.GET)
//    public ModelAndView nuevoLogin() {
//        ModelMap model = new ModelMap();
//        model.put("datosLogin", new DatosLoginDto()); // 👈 esto es lo importante
//        return new ModelAndView("fragments/login", model);
//    }

//    @RequestMapping(path = "/validar-login", method = RequestMethod.POST)
//    public ModelAndView validarLogin(@ModelAttribute("datosLogin") DatosLoginDto datosLogin, HttpServletRequest request) {
//        ModelMap model = new ModelMap();
//
//        Usuario usuarioBuscado = servicioLogin.consultarUsuario(datosLogin.getEmail(), datosLogin.getPassword());
//        if (usuarioBuscado != null) {
//            request.getSession().setAttribute("ROL", usuarioBuscado.getRol());
//            return new ModelAndView("redirect:/home");
//        } else {
//            model.put("error", "Usuario o clave incorrecta");
//        }
//        return new ModelAndView("login", model);
//    }
    @RequestMapping(path = "/validar-login", method = RequestMethod.POST)
    public ModelAndView validarLogin(@ModelAttribute("datosLogin") DatosLoginDto datosLogin,
//                                     @RequestParam("redirectUrl") String redirectUrl,
                                     HttpServletRequest request) {
        ModelMap model = new ModelMap();

//        Usuario usuarioBuscado = servicioLogin.consultarUsuario(datosLogin.getEmail(), datosLogin.getPassword());
        Usuario usuarioBuscado = servicioLogin.obtenerUsuarioPorEmailYPassword(datosLogin.getEmail(), datosLogin.getPassword());
        if (usuarioBuscado != null) {
            request.getSession().setAttribute("ROL", usuarioBuscado.getRol());

//            if (redirectUrl != null && redirectUrl.startsWith("/")) {
//                return new ModelAndView("redirect:" + redirectUrl);
//            }
            return new ModelAndView("redirect:/index");
        } else {
            model.put("error", "Usuario o clave incorrecta");
            return new ModelAndView("fragments/login", model);
        }
    }




//    @PostMapping("/validar-login")
//    public ResponseEntity<?> validarLogin(@ModelAttribute("datosLogin") DatosLoginDto datosLogin,
//                                          HttpServletRequest request) { // No need for response, model anymore
//
//        Usuario usuarioBuscado = servicioLogin.consultarUsuario(datosLogin.getEmail(), datosLogin.getPassword());
//
//        if (usuarioBuscado != null) {
//            // Success: User is valid, set session attribute and return "OK"
//            request.getSession().setAttribute("ROL", usuarioBuscado.getRol());
//            return ResponseEntity.ok("OK");
//        } else {
//            // Failure: User is not valid. Let Spring render the fragment.
//            // 1. Create a ModelAndView object, specifying the template path.
//            //    IMPORTANT: You might need to specify the fragment name itself.
//            //    Let's assume your form is wrapped in a div with id="login-form-content"
//            ModelAndView modelAndView = new ModelAndView("fragments/login");
//
//            // 2. Add the objects required by the template to the ModelAndView.
//            modelAndView.addObject("error", "Usuario o clave incorrecta");
//            modelAndView.addObject("datosLogin", datosLogin); // The form-backing object
//
//            // 3. Return the ModelAndView within a ResponseEntity with the desired status code.
//            //    Spring will now render the fragment using the full RequestContext.
//            return new ResponseEntity<>(modelAndView, org.springframework.http.HttpStatus.UNAUTHORIZED);
//        }
//    }






    @RequestMapping(path = "/registrarme", method = RequestMethod.POST)
    public ModelAndView registrarme(@ModelAttribute("usuario") Usuario usuario, @RequestParam("redirectUrl") String redirectUrl) {
        ModelMap model = new ModelMap();
        try {
            servicioLogin.registrar(usuario);
        } catch (UsuarioExistente e) {
            model.put("error", "El usuario ya existe");
            return new ModelAndView("fragments/nuevo-usuario", model);
        } catch (Exception e) {
            model.put("error", "Error al registrar el nuevo usuario");
            return new ModelAndView("fragments/nuevo-usuario", model);
        }
        model.put("mensajeRegistroExitoso", "El Registro fue exitoso");

        return new ModelAndView("redirect:/index", model);
    }

    @RequestMapping(path = "/nuevo-usuario", method = RequestMethod.GET)
    public ModelAndView nuevoUsuario() {
        ModelMap model = new ModelMap();
        model.put("usuario", new Usuario());
        return new ModelAndView("fragments/nuevo-usuario", model);
    }

    @RequestMapping(path = "/home", method = RequestMethod.GET)
    public ModelAndView irAHome() {
        return new ModelAndView("home");
    }

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public ModelAndView inicio() {
        return new ModelAndView("redirect:/index");
    }
}