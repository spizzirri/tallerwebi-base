package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ControladorLogin {

    private ServicioLogin servicioLogin;

    /* hacer un controlador Home y ordenar Luego ***/
    @Autowired
    private ServicioPublicado servicioPublicado;
          /******     ***/

    @Autowired
    public ControladorLogin(ServicioLogin servicioLogin) {
        this.servicioLogin = servicioLogin;
    }

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
            // guardás el rol por separado
            request.getSession().setAttribute("ROL", usuarioBuscado.getRol());

            // y también guardás el objeto usuario entero //*agregado*//
            request.getSession().setAttribute("usuarioLogueado", usuarioBuscado);

            return new ModelAndView("redirect:/home");
        } else {
            model.put("error", "Usuario o clave incorrecta");
        }
        return new ModelAndView("login", model);
    }


    @RequestMapping(path = "/registrarme", method = RequestMethod.POST)
    public ModelAndView registrarme(@ModelAttribute("usuario") Usuario usuario) {
        ModelMap model = new ModelMap();

        try {
            // Asignar las carreras al usuario directamente
            servicioLogin.asignarCarrerasPorNombre(usuario, usuario.getCarrerasNombres());

            // Registrar usuario
            servicioLogin.registrar(usuario);

        } catch (UsuarioExistente e) {
            model.put("error", "El usuario ya existe");
            return new ModelAndView("nuevo-usuario", model);
        } catch (Exception e) {
            model.put("error", "Error al registrar el nuevo usuario");
            return new ModelAndView("nuevo-usuario", model);
        }

        // Redirigir al login si todo sale bien
        return new ModelAndView("redirect:/login");
    }
    @RequestMapping(path = "/nuevo-usuario", method = RequestMethod.GET)
    public ModelAndView nuevoUsuario() {
        ModelMap model = new ModelMap();
        model.put("usuario", new Usuario());

        List<Carrera> todasLasCarreras = new ArrayList<>();

        // Carrera 1
        Carrera c1 = new Carrera();
        c1.setNombre("Licenciatura en Administración");

        Materia m1 = new Materia();
        m1.setNombre("Contabilidad");
        m1.setCodigo("ADM101");
        m1.setComision("A");

        Materia m2 = new Materia();
        m2.setNombre("Economía");
        m2.setCodigo("ADM102");
        m2.setComision("A");

        Materia m3 = new Materia();
        m3.setNombre("Matemática");
        m3.setCodigo("ADM103");
        m3.setComision("B");

        c1.setMaterias(Arrays.asList(m1, m2, m3));
        todasLasCarreras.add(c1);

        // Carrera 2
        Carrera c2 = new Carrera();
        c2.setNombre("Licenciatura en Comercio Internacional");

        Materia m4 = new Materia();
        m4.setNombre("Comercio Exterior");
        m4.setCodigo("COM101");
        m4.setComision("A");

        Materia m5 = new Materia();
        m5.setNombre("Marketing");
        m5.setCodigo("COM102");
        m5.setComision("A");

        Materia m6 = new Materia();
        m6.setNombre("Finanzas");
        m6.setCodigo("COM103");
        m6.setComision("B");

        c2.setMaterias(Arrays.asList(m4, m5, m6));
        todasLasCarreras.add(c2);

        // Carrera 3
        Carrera c3 = new Carrera();
        c3.setNombre("Licenciatura en Economía");

        Materia m7 = new Materia();
        m7.setNombre("Microeconomía");
        m7.setCodigo("ECO101");
        m7.setComision("A");

        Materia m8 = new Materia();
        m8.setNombre("Macroeconomía");
        m8.setCodigo("ECO102");
        m8.setComision("A");

        Materia m9 = new Materia();
        m9.setNombre("Estadística");
        m9.setCodigo("ECO103");
        m9.setComision("B");

        c3.setMaterias(Arrays.asList(m7, m8, m9));
        todasLasCarreras.add(c3);

        // Carrera 4
        Carrera c4 = new Carrera();
        c4.setNombre("Contador Público");

        Materia m10 = new Materia();
        m10.setNombre("Contabilidad Avanzada");
        m10.setCodigo("CP101");
        m10.setComision("A");

        Materia m11 = new Materia();
        m11.setNombre("Auditoría");
        m11.setCodigo("CP102");
        m11.setComision("A");

        Materia m12 = new Materia();
        m12.setNombre("Impuestos");
        m12.setCodigo("CP103");
        m12.setComision("B");

        c4.setMaterias(Arrays.asList(m10, m11, m12));
        todasLasCarreras.add(c4);

        // Carrera 5
        Carrera c5 = new Carrera();
        c5.setNombre("Ingeniería Civil");

        Materia m13 = new Materia();
        m13.setNombre("Mecánica");
        m13.setCodigo("IC101");
        m13.setComision("A");

        Materia m14 = new Materia();
        m14.setNombre("Estructuras");
        m14.setCodigo("IC102");
        m14.setComision("A");

        Materia m15 = new Materia();
        m15.setNombre("Hidráulica");
        m15.setCodigo("IC103");
        m15.setComision("B");

        c5.setMaterias(Arrays.asList(m13, m14, m15));
        todasLasCarreras.add(c5);

        model.put("todasLasCarreras", todasLasCarreras);

        return new ModelAndView("nuevo-usuario", model);
    }









}

