package com.tallerwebi.integracion;

import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringWebTestConfig.class, HibernateTestConfig.class})
public class ControladorCategoriaTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void debeRetornarLaVistaCategoriasConElModelo() throws Exception {
        // GET /categorias
        MvcResult result = this.mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView mv = result.getModelAndView();
        assert mv != null;

        assertThat(mv.getViewName(), equalToIgnoringCase("categorias"));
        assertThat(mv.getModel().containsKey("categorias"), is(true));
    }

    @Test
    public void debePermitirAccederDirectoALaRutaCategorias() throws Exception {
        this.mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk());
    }
}
