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

//    @Test
//    public void queAlIrALaPantallaDeVerProgresoMeMuestreLaVistaDeCalendario(){
//        ModelAndView modelAndView = this.controladorVerProgreso.irVerProgreso();
//
//        assertThat(modelAndView.getViewName(),equalTo("verProgreso"));//vista correcta
//    }

//    @Test
//    public void testIrVerProgreso() throws Exception {
//        // Mock de los datos que se esperan retornar
//        List<DatosItemRendimiento> mockDatos = Stream.of(
//                new DatosItemRendimiento(),
//                new DatosItemRendimiento("Item 2"),
//                new DatosItemRendimiento("Item 3")
//        ).collect(Collectors.toList());
//
//        // Mock del servicio para obtener los datos
//        when(servicioCalendario.obtenerItemsRendimiento()).thenReturn(mockDatos);
//
//        // Realizar la solicitud GET a la URL /verProgreso
//        MockHttpServletRequestBuilder requestBuilder = get("/verProgreso");
//        MvcResult result = mockMvc.perform(requestBuilder)
//                .andExpect(status().isOk())
//                .andExpect(model().attributeExists("datosItemRendimiento"))
//                .andReturn();
//
//        // Obtener la lista de datos retornada por el controlador
//        List<DatosItemRendimiento> datosRetornados = (List<DatosItemRendimiento>) result.getModelAndView()
//                .getModelMap().getAttribute("datosItemRendimiento");
//
//        // Verificar que la lista retornada tenga los mismos elementos que el mock de datos
//        assertEquals(mockDatos.size(), datosRetornados.size());
//        assertTrue(mockDatos.containsAll(datosRetornados));
//    }

}
