package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.calendario.RepositorioCalendario;
import com.tallerwebi.dominio.calendario.ServicioCalendario;
import com.tallerwebi.dominio.calendario.ServicioCalendarioImpl;
import com.tallerwebi.infraestructura.RepositorioCalendarioImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.servlet.ModelAndView;
import com.tallerwebi.dominio.calendario.ServicioCalendario;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.Mockito.mock;

public class ControladorCalendarioTest {

  private ServicioCalendario servicioCalendario;
  private ControladorCalendario controladorCalendario;
//  private RepositorioCalendario repositorioCalendario;

    @BeforeEach
    public void init() {
        this.servicioCalendario = mock(ServicioCalendario.class);
        this.controladorCalendario = new ControladorCalendario(this.servicioCalendario);
    }

    @Test
    public void queAlIrALaPantallaDeCalendarioMeMuestreLaVistaDeCalendario(){
        ModelAndView modelAndView = this.controladorCalendario.irCalendario();
        //mensaje
        String message = modelAndView.getModel().get("message").toString();

        assertThat(modelAndView.getViewName(),equalTo("calendario"));//vista correcta
        assertThat(message, equalToIgnoringCase("¿Cómo fue tu entrenamiento hoy?"));//mensaje correcto
    }

//    @Test
//    public void queAlIrALaPantallaDeCalendarioMeMuestreLosTiposRendimientos(){
//        ModelAndView modelAndView = this.controladorCalendario.mostrarTiposRendimientos();
//        //mensaje
//        String message = modelAndView.getModel().get("message").toString();
//
//        assertThat(modelAndView.getViewName(),equalTo("calendario"));//vista correcta
//        assertThat(message, equalToIgnoringCase("¿Cmo fue tu entrenamiento hoy?"));//mensaje correcto
//        assertThat(message, equalToIgnoringCase("¿Cómo fue tu entrenamiento hoy?"));//mensaje correcto
//    }


//    @Test
//    public void queSeMuestreElTipoDeRendimientoSeleccionado() throws Exception {
//        //preparacion
//        ModelAndView modelAndView = new ModelAndView();
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//
//        //ejecucion
//        request.setParameter("tipoRendimiento", "ALTO");
//        controladorCalendario.seleccionarTipoRendimiento(request, response, modelAndView);
//
//        //verficacion
//        Map<String, Object> model = modelAndView.getModel();
//        assertThat(model.get("tipoRendimientoSeleccionado"), equalTo("ALTO"));
//    }


}
