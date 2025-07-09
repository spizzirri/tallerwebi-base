package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioCompra;
import com.tallerwebi.dominio.entidades.Compra;
import org.dom4j.rule.Mode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ControladorHistorialCompras {

    private ServicioCompra servicioCompra;

    public ControladorHistorialCompras(ServicioCompra servicioCompra) {
        this.servicioCompra = servicioCompra;
    }

    @GetMapping("/historialDeCompras")
    public ModelAndView mostrarHistorialDeCompras(HttpSession session) {
        ModelMap model = new ModelMap();
        agregarComprasAlModelo(model, session);
        return new ModelAndView("historialDeCompras", model);
    }

    @GetMapping("/comprasUsuario")
    @ResponseBody
    public Map<String, Object> mostrarComprasUsuario(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        UsuarioDto usuarioLogueado = (UsuarioDto) session.getAttribute("usuario");

        if (usuarioLogueado == null) {
            response.put("mensaje", "Usuario no logueado");
            return response;
        }

        List<Compra> comprasUsuario = this.servicioCompra.obtenerCompraComponenteDeUnUsuarioLogueado(usuarioLogueado);
        response.put("success", true);
        response.put("comprasUsuario", comprasUsuario);
        return response;
    }

    private void agregarComprasAlModelo(ModelMap modelo, HttpSession session) {
        try {
            UsuarioDto usuarioLogueado = (UsuarioDto) session.getAttribute("usuario");

            if (usuarioLogueado != null) {
                List<Compra> comprasUsuario = this.servicioCompra.obtenerCompraComponenteDeUnUsuarioLogueado(usuarioLogueado);

                modelo.addAttribute("comprasUsuario", comprasUsuario);
                modelo.addAttribute("cantidadDeCompras", comprasUsuario.size());
            } else {
                modelo.addAttribute("comprasUsuario", new ArrayList<>());
                modelo.addAttribute("cantidadDeCompras", 0);
            }

        } catch (Exception e) {
            e.printStackTrace();
            modelo.addAttribute("comprasUsuario", new ArrayList<>());
            modelo.addAttribute("cantidadDeCompras", 0);
        }
    }
}
