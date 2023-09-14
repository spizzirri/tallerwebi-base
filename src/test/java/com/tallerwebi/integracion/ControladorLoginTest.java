package com.tallerwebi.integracion;

import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import com.tallerwebi.dominio.Usuario;
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

import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringWebTestConfig.class, HibernateTestConfig.class})
public class ControladorLoginTest {

	private Usuario usuarioMock;

	@Autowired
	private WebApplicationContext wac;
	private MockMvc mockMvc;


	@BeforeEach
	public void init(){
		usuarioMock = mock(Usuario.class);
		when(usuarioMock.getEmail()).thenReturn("dami@unlam.com");
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void debeRetornarLaPaginaLoginCuandoSeNavegaALaRaiz() throws Exception {

		MvcResult result = this.mockMvc.perform(get("/"))
				/*.andDo(print())*/
				.andExpect(status().is3xxRedirection())
				.andReturn();

		ModelAndView modelAndView = result.getModelAndView();
        assert modelAndView != null;
		assertThat("redirect:/login", equalToIgnoringCase(Objects.requireNonNull(modelAndView.getViewName())));
		assertThat(true, is(modelAndView.getModel().isEmpty()));
	}

	@Test
	public void debeRetornarLaPaginaLoginCuandoSeNavegaALLogin() throws Exception {

		MvcResult result = this.mockMvc.perform(get("/login"))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView modelAndView = result.getModelAndView();
        assert modelAndView != null;
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
		assertThat(modelAndView.getModel().get("datosLogin").toString(),  containsString("com.tallerwebi.presentacion.DatosLogin"));

	}
}
