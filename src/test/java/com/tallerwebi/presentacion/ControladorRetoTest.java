package com.tallerwebi.presentacion;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ControladorRetoTest {

    private ServicioReto servicioReto;

    private MockMvc mockMvc;

    @Mock
    private RepositorioReto repositorioReto;

    @InjectMocks
    private ControladorReto controladorReto;

    @BeforeEach
    public void init() {
        this.servicioReto = mock(ServicioReto.class);
        this.controladorReto = new ControladorReto(this.servicioReto);
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controladorReto).build();
    }

    @Test
    public void queEmpezarRetoSeaLlamadoCorrectamente() throws Exception {
        // Configura el servicioReto mock para que no haga nada al llamar a empezarReto
        doNothing().when(servicioReto).empezarReto(123L);

        mockMvc.perform(MockMvcRequestBuilders.get("/empezar-reto")
                        .param("retoId", "123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
        // Verifica que el método empezarReto del servicioReto se haya llamado una vez con el parámetro 123L
        verify(servicioReto, times(1)).empezarReto(123L);
    }



}
