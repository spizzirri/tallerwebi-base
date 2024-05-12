package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.calendario.ServicioItemRendimiento;
import com.tallerwebi.dominio.calendario.TipoRendimiento;
import com.tallerwebi.dominio.rutina.ServicioRutina;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class ControladorCalendario {

    private ServicioItemRendimiento servicioItemRendimiento;

    @Autowired
    public ControladorCalendario(ServicioItemRendimiento servicioItemRendimiento) {
        this.servicioItemRendimiento = servicioItemRendimiento;
    }

    @RequestMapping(path = "/calendario", method = RequestMethod.GET)
    public ModelAndView irCalendario() {
        String viewNam = "calendario";
        ModelMap model = new ModelMap();
        model.put("message", "¿Como fué tu entrenamiento hoy?");//mensaje agregado
        return new ModelAndView(viewNam,model);
    }

//    @RequestMapping(path = "/calendario", method = RequestMethod.GET)
//    public void seleccionarTipoRendimiento(MockHttpServletRequest request, MockHttpServletResponse response, ModelAndView modelAndView) {
//        String viewNam = "calendario";
//        String tipoRendimientoSeleccionado = request.getParameter("tipoRendimiento");
//
//        List<TipoRendimiento> opcionesRendimiento = servicioItemRendimiento.obtenerOpcionesRendimiento();
//
//        // Add options to the model view
//        modelAndView.addObject("opcionesRendimiento", opcionesRendimiento);
//
//        // Handle potential null value (optional)
//        if (tipoRendimientoSeleccionado != null) {
//            modelAndView.addObject("tipoRendimientoSeleccionado", tipoRendimientoSeleccionado);
//        } else {
//            // Handle the case where no type is selected (optional)
//        }
//
//        // Rest of your logic to render the view (replace with your implementation)
//        // ...
//    }


}
