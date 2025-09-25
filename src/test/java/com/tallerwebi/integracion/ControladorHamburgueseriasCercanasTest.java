package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class ControladorHamburgueseriasCercanasTest {

	@Autowired
	private WebApplicationContext wac;
	private MockMvc mockMvc;

	@BeforeEach
	public void init() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void irAHamburgueseriasCercanasDeberiaLLevarAHamburgueseriasCercanas() throws Exception {
		MvcResult result = this.mockMvc.perform(get("/hamburgueserias-cercanas"))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assert modelAndView != null;
		if (modelAndView != null) {
			assertThat(modelAndView.getViewName(),
					equalToIgnoringCase("hamburgueserias-cercanas"));
		}
	}

	@Test
	public void listarhamburgueseriasDeberiaRetornarUnaListaNoNula() throws Exception {
		MvcResult result = this.mockMvc
				.perform(get("/hamburgueserias-cercanas/-34.6037/-58.3816/lista"))
				.andExpect(status().isOk())
				.andReturn();

		String jsonResponse = result.getResponse().getContentAsString();

		// TODO: Ver si se puede parsear el JSON con un Mapper.
		assert jsonResponse != null : "La respuesta JSON no debe ser nula";
		assert !jsonResponse.isEmpty() : "La respuesta JSON no debe estar vacía";
		assert jsonResponse.startsWith("[") : "La respuesta debe ser un array JSON";
		assert jsonResponse.contains("\"id\"") : "La respuesta debe contener elementos con id";
		assert jsonResponse.split("\"id\"").length > 2 : "La lista debe contener más de 1 elemento";
	}
}
