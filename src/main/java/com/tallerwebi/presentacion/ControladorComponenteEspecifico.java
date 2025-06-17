package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioDolar;
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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

@Controller
public class ControladorComponenteEspecifico {

    private ServicioProductoEspecifico servicioProductoEspecifico;
    private ServicioDolar servicioDolar;

    @Autowired
    public ControladorComponenteEspecifico(ServicioProductoEspecifico servicioProductoEspecifico, ServicioDolar servicioDolar) {
        this.servicioProductoEspecifico = servicioProductoEspecifico;
        this.servicioDolar = servicioDolar;
    }

    @GetMapping(path = "productoEspecifico/{id}")
    public ModelAndView mostrarComponenteEspecifico(@PathVariable Long id) {

        ComponenteEspecificoDto componenteEspecificoDto = new ComponenteEspecificoDto(servicioProductoEspecifico.obtenerComponentePorId(id));

        ModelMap model = new ModelMap();

        model.put("componenteEspecificoDto", componenteEspecificoDto);
        model.put("precioFormateado", this.obtenerPrecioFormateado(componenteEspecificoDto.getPrecio()));
        model.put("precioDeLista", this.obtenerPrecioDeLista(componenteEspecificoDto.getPrecio()));
        model.put("precioDolar", this.conversionPesoADolar(componenteEspecificoDto));
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

    private String obtenerPrecioFormateado(Double precio) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat formatter = new DecimalFormat("#,##0.00", symbols);
        return formatter.format(precio);
    }

    private String conversionPesoADolar(ComponenteEspecificoDto componenteEspecificoDto) {
        Double cotizacionDolarBlue = servicioDolar.obtenerCotizacionDolarBlue();
        Double precioEnPesos = componenteEspecificoDto.getPrecio();
        Double precioEnDolares = precioEnPesos / cotizacionDolarBlue;

        return this.obtenerPrecioFormateado(precioEnDolares);
    }

    private String obtenerPrecioDeLista(Double precio) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat formatter = new DecimalFormat("#,##0.00", symbols);
        Double precioDeLista = precio * 1.50;
        return formatter.format(precioDeLista);
    }


}
