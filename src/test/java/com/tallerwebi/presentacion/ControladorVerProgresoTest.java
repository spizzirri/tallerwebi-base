package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.calendario.ServicioCalendario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.reflect.Array.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ControladorVerProgresoTest {

    private ServicioCalendario servicioCalendario;
    private ControladorVerProgreso controladorVerProgreso;
//  private RepositorioCalendario repositorioCalendario;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        this.servicioCalendario = mock(ServicioCalendario.class);
        this.controladorVerProgreso = new ControladorVerProgreso(this.servicioCalendario);
    }

}
