package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class ControladorObjetivoTest {

    @Mock
    private DataSource dataSource;

    @InjectMocks
    private ControladorObjetivo controladorObjetivo;

    private MockMvc mockMvc;

    private Usuario usuario;

    @Mock
    private HttpSession session;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controladorObjetivo).build();
        usuario = new Usuario(); // Crear un usuario simulado
        when(session.getAttribute("usuario")).thenReturn(usuario);
    }

    @Test
    public void queAlIrAVistaDeObjetivosSinUsuarioRedirijaALogin() throws Exception {
        // Preparación
        when(session.getAttribute("usuario")).thenReturn(null);

        // Ejecución y verificación
        ModelAndView modelAndView = controladorObjetivo.mostrarVistaObjetivos(session);

        // Verificación
        assertEquals("redirect:/login", modelAndView.getViewName());
    }

    @Test
    public void queAlIrAVistaDeObjetivosConUsuarioMuestreLaVista() throws Exception {
        // Preparación
        when(session.getAttribute("usuario")).thenReturn(usuario);

        // Ejecución
        ModelAndView modelAndView = controladorObjetivo.mostrarVistaObjetivos(session);

        // Verificación
        assertEquals("objetivo", modelAndView.getViewName());
        assertEquals(usuario, modelAndView.getModel().get("usuario"));
    }

}
