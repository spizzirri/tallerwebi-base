package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.usuario.ServicioLogin;
import com.tallerwebi.dominio.reto.RepositorioReto;
import com.tallerwebi.dominio.reto.ServicioReto;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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








}
