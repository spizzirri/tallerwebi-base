package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.ServicioSubasta;
import com.tallerwebi.dominio.Subasta;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ControladorLogin {

    private ServicioLogin servicioLogin;
    private ServicioSubasta servicioSubasta;

    @Autowired
    public ControladorLogin(ServicioLogin servicioLogin, ServicioSubasta servicioSubasta) {
        this.servicioLogin = servicioLogin;
        this.servicioSubasta = servicioSubasta;
    }

    //NOTA: Creo que el profe se enojara si pongo 2 servicios en 1 controlador. Despues arreglare eso.



    @RequestMapping("/login")
    public ModelAndView irALogin() {

        ModelMap modelo = new ModelMap();
        modelo.put("datosLogin", new DatosLogin());
        return new ModelAndView("login", modelo);
    }

    @RequestMapping(path = "/validar-login", method = RequestMethod.POST)
    public ModelAndView validarLogin(@ModelAttribute("datosLogin") DatosLogin datosLogin, HttpServletRequest request) {
        ModelMap model = new ModelMap();
        Usuario usuarioBuscado = servicioLogin.consultarUsuario(datosLogin.getEmail(), datosLogin.getPassword());
        if (usuarioBuscado != null) {
            request.getSession().setAttribute("USUARIO", usuarioBuscado.getEmail());
            request.getSession().setAttribute("PASS", usuarioBuscado.getPassword());
            request.getSession().setAttribute("ROL", usuarioBuscado.getRol());
            return new ModelAndView("redirect:/home");
        } else {
            model.put("error", "Usuario o clave incorrecta");
        }
        return new ModelAndView("login", model);
    }

    @RequestMapping(path = "/registrarme", method = RequestMethod.POST)
    public ModelAndView registrarme(@ModelAttribute("usuario") Usuario usuario) {
        ModelMap model = new ModelMap();
        try{
            servicioLogin.registrar(usuario);
        } catch (UsuarioExistente e){
            model.put("error", "El usuario ya existe");
            return new ModelAndView("nuevo-usuario", model);
        } catch (Exception e){
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

    @RequestMapping(path = "/home", method = RequestMethod.GET)
    public ModelAndView irAHome() {
        return new ModelAndView("home");
    }

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public ModelAndView inicio() {
        return new ModelAndView("redirect:/login");
    }

    //TO-DO: Mover estas clases de subastas a un controlador exclusivo, en vez de usar el de Login.
    @RequestMapping(path = "/nuevaSubasta", method = RequestMethod.GET)
    public ModelAndView irANuevaSubasta() {
        ModelMap model = new ModelMap();
        model.put("subasta", new Subasta());
        return new ModelAndView("nuevaSubasta", model);
    }

    @RequestMapping(path = "/crearSubasta", method = RequestMethod.POST)
    public ModelAndView crearSubasta(@ModelAttribute("subasta") Subasta subasta, HttpServletRequest request) {

        ModelMap model = new ModelMap();
        Usuario creador;
        String creadorEmail = (String) request.getSession().getAttribute("USUARIO");
        String creadorPass = (String) request.getSession().getAttribute("PASS");
        creador = servicioLogin.consultarUsuario(creadorEmail, creadorPass);
        subasta.setCreador(creador);
        try{
            servicioSubasta.crearSubasta(subasta);
        }catch (Exception e){
            model.put("error", "Hubo un error al crear la subasta. Intentelo nuevamente.");
            return new ModelAndView("nuevaSubasta", model);
        }
        return new ModelAndView("redirect:/home");
    }
}

