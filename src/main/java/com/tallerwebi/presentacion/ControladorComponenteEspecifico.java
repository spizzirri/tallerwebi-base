package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

@Controller
public class ControladorComponenteEspecifico {

    private ServicioPrecios servicioPrecios;
    private ServicioProductoEspecifico servicioProductoEspecifico;
    private ServicioDolar servicioDolar;

    @Autowired
    public ControladorComponenteEspecifico(ServicioProductoEspecifico servicioProductoEspecifico, ServicioDolar servicioDolar, ServicioPrecios servicioPrecios) {
        this.servicioProductoEspecifico = servicioProductoEspecifico;
        this.servicioDolar = servicioDolar;
        this.servicioPrecios = servicioPrecios;
    }

    @GetMapping(path = "productoEspecifico/{id}")
    public ModelAndView mostrarComponenteEspecifico(@PathVariable Long id) {

        ComponenteEspecificoDto componenteEspecificoDto = new ComponenteEspecificoDto(servicioProductoEspecifico.obtenerComponentePorId(id));

        ModelMap model = new ModelMap();

        model.put("componentesEspecifico", servicioProductoEspecifico.obtenerComponentesAcomparar(id));
        model.put("componenteEspecificoDto", componenteEspecificoDto);
        model.put("precioFormateado", servicioPrecios.obtenerPrecioFormateado(componenteEspecificoDto.getPrecio()));
        model.put("precioEnPesos", servicioPrecios.conversionDolarAPeso(componenteEspecificoDto.getPrecio()));
        model.put("precioDeLista", servicioPrecios.obtenerPrecioDeLista(componenteEspecificoDto.getPrecio()));
        model.put("precio3Cuotas", servicioPrecios.obtenerPrecioCon3Cuotas(componenteEspecificoDto.getPrecio()));
        model.put("precio6Cuotas", servicioPrecios.obtenerPrecioCon6Cuotas(componenteEspecificoDto.getPrecio()));
        model.put("precio12Cuotas", servicioPrecios.obtenerPrecioCon12Cuotas(componenteEspecificoDto.getPrecio()));

        return new ModelAndView("productoEspecifico", model);
    }

    @PostMapping(path = "productoEspecifico/{id}")
    public String guardarComponenteEspecifico(@PathVariable Long id,
                                              @ModelAttribute ComponenteEspecificoDto componenteEspecificoDto,
                                              HttpSession session) {

        session.setAttribute("componenteEspecificoGuardado", componenteEspecificoDto);

        return "redirect:/productoEspecifico/" + id;
    }

    @GetMapping("/terminosYCondiciones.html")
    public String verTerminos() {
        return "terminosYCondiciones";
    }

}
