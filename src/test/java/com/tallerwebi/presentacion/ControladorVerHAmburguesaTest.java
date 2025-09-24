package com.tallerwebi.presentacion;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import com.tallerwebi.dominio.ServicioVerHamburguesa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.Mockito.mock;

public class ControladorVerHAmburguesaTest {
    @Test
    public void dadoQuePidoVerDatosDeLaHamburguesaConID1MostrarEsaHamburguesa(){
        ServicioVerHamburguesa sevicioVerHamburguesa =mock(ServicioVerHamburguesa.class);
        ControladorVerHamburguesa controladorVerHamburguesa =new ControladorVerHamburguesa(sevicioVerHamburguesa);
        ModelAndView modelAndView= new ModelAndView();
        modelAndView=controladorVerHamburguesa.irAMostrarHamburguesa();
        assertThat(modelAndView.getViewName(),equalToIgnoringCase("hamburguesa"));
        
       
    }
}
