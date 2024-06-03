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
import org.springframework.web.servlet.ModelAndView;

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
    public void testEmpezarReto() {
        // Mock de los servicios
        ServicioReto servicioRetoMock = mock(ServicioReto.class);
        ServicioLogin servicioLoginMock = mock(ServicioLogin.class);

        // Creación del controlador con los mocks
        ControladorReto controlador = new ControladorReto(servicioRetoMock, servicioLoginMock);

        // ID del reto simulado
        Long retoId = 123L;

        // Mock del reto disponible
        Reto retoDisponibleMock = new Reto();
        retoDisponibleMock.setId(retoId);

        // Simular comportamiento del servicio de reto
        when(servicioRetoMock.obtenerRetoPorId(retoId)).thenReturn(retoDisponibleMock);

        // Simular comportamiento del servicio de login
        DatosItemRendimiento itemMasSeleccionadoMock = new DatosItemRendimiento();
        when(servicioLoginMock.obtenerItemMasSeleccionado()).thenReturn(itemMasSeleccionadoMock);

        // Ejecutar el método del controlador
        ModelAndView modelAndView = controlador.empezarReto(retoId);

        // Verificar el resultado
        assertEquals("home", modelAndView.getViewName()); // Comprobar que se redirige a la vista "home"
        assertEquals(retoId, modelAndView.getModel().get("retoId")); // Comprobar que el modelo contiene el retoId
        assertEquals(itemMasSeleccionadoMock, modelAndView.getModel().get("itemMasSeleccionado")); // Comprobar que el modelo contiene el itemMasSeleccionado
        assertEquals(retoDisponibleMock, modelAndView.getModel().get("retoDisponible")); // Comprobar que el modelo contiene el retoDisponible

        // Verificar que se llama al servicio de reto con el ID del reto
        verify(servicioRetoMock).empezarRetoActualizado(retoId);
    }






}
