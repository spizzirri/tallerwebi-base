package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioProductoEspecifico;
import com.tallerwebi.dominio.ServicioProductoEspecificoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class ControladorComponenteEspecifico {

    private ServicioProductoEspecifico servicioProductoEspecifico;

    @Autowired
    public ControladorComponenteEspecifico(ServicioProductoEspecifico servicioProductoEspecifico) {
        this.servicioProductoEspecifico = servicioProductoEspecifico;
    }

    @GetMapping(path = "productoEspecifico/{id}")
    public ModelAndView mostrarComponenteEspecifico(@PathVariable Long id) {

        ModelMap model = new ModelMap();

        model.put("componenteEspecificoDto", new ComponenteEspecificoDto(servicioProductoEspecifico.obtenerComponentePorId(id)));
        model.put("terminos", "Estos son los terminos y condiciones para este producto...");
        model.put("cuotas", "12 cuotas fijas sin interes con tarjetas seleccionadas.");

        return new ModelAndView("productoEspecifico", model);
    }

    @PostMapping(path = "productoEspecifico/{id}")
    public String guardarComponenteEspecifico(@PathVariable Long id,
                                              @ModelAttribute ComponenteEspecificoDto componenteEspecificoDto,
                                              HttpSession session) {

        session.setAttribute("componenteEspecificoGuardado", componenteEspecificoDto);

        return "redirect:/productoEspecifico/" + id;
    }


}
