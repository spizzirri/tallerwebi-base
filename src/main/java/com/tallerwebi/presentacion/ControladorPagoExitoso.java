package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioCompra;
import com.tallerwebi.dominio.ServicioDeEnviosImpl;
import com.tallerwebi.dominio.entidades.Compra;
import com.tallerwebi.dominio.entidades.CompraComponente;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ControladorPagoExitoso {

    private ServicioCompra servicioCompra;

    public ControladorPagoExitoso(ServicioCompra servicioCompra) {
        this.servicioCompra = servicioCompra;
    }

    @GetMapping("/pagoExitoso")
    public ModelAndView mostrarPagoExitoso(HttpSession session) {
        ModelMap model = new ModelMap();
        UsuarioDto usuarioLogueado = (UsuarioDto) session.getAttribute("usuario");
        if (usuarioLogueado == null) {
            return new ModelAndView("redirect:/login");
        }

        List<Compra> comprasUsuarioObtenidas = this.servicioCompra.obtenerCompraComponenteDeUnUsuarioLogueado(usuarioLogueado);

        List<Compra> comprasUsuario = comprasUsuarioObtenidas.stream().limit(1).collect(Collectors.toList());

        model.put("tarjeta", session.getAttribute("tarjeta"));
        model.put("tiempo", session.getAttribute("tiempo"));
        model.put("destino", session.getAttribute("destino"));
        model.put("moneda", session.getAttribute("moneda"));
        model.put("iva", session.getAttribute("iva"));
        model.put("comprasUsuario", comprasUsuario);

        return new ModelAndView("pagoExitoso", model);
    }
}