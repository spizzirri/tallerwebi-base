package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.calendario.ServicioCalendario;
import com.tallerwebi.dominio.reto.RepositorioReto;
import com.tallerwebi.dominio.reto.Reto;
import com.tallerwebi.dominio.reto.ServicioReto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import static java.lang.reflect.Array.get;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ControladorRetoTest {

    private ServicioReto servicioReto;
    private ServicioLogin servicioLogin;
    private MockMvc mockMvc;

    @Mock
    private RepositorioReto repositorioReto;

    @InjectMocks
    private ControladorReto controladorReto;

    @BeforeEach
    public void init() {
        this.servicioReto = mock(ServicioReto.class);
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controladorReto).build();
    }

    @Test
    public void testEmpezarRetoSuccess() throws Exception {
        Long retoId = 123L;

        doNothing().when(servicioReto).empezarReto(retoId);

        mockMvc.perform(post("/home/empezar-reto")
                        .param("retoId", retoId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("retoId"))
                .andExpect(model().attribute("retoId", retoId));

        verify(servicioReto, times(1)).empezarReto(retoId);
    }

    @Test
    public void testEmpezarRetoFailure() throws Exception {
        Long retoId = 123L;
        String errorMessage = "Error al iniciar el reto";

        doThrow(new RuntimeException(errorMessage)).when(servicioReto).empezarReto(retoId);

        mockMvc.perform(post("/home/empezar-reto")
                        .param("retoId", retoId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("errorPage"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "An error occurred while starting the challenge: " + errorMessage));

        verify(servicioReto, times(1)).empezarReto(retoId);
    }




}
