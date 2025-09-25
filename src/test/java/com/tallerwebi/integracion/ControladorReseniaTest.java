package com.tallerwebi.integracion;

import com.tallerwebi.dominio.Resenia;
import com.tallerwebi.dominio.ServicioResenia;
import com.tallerwebi.presentacion.ControladorResenia;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;

class ControladorReseniaTest {

    private ServicioResenia servicioResenia;
    private ControladorResenia controladorResenia;
    private Resenia resenia;
    private WebApplicationContext wac;
    private MockMvc mockMvc;
    private HttpServletRequest mockRequest;

    @Test
    public void agregarUnaReseniaDeberiaLlevarAlHome() {

    }
}
