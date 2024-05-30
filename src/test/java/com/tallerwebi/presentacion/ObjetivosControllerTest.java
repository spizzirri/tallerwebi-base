package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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

public class ObjetivosControllerTest {

    @Mock
    private DataSource dataSource;

    @InjectMocks
    private ObjetivosController objetivosController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(objetivosController).build();
    }

    @Test
    public void testGuardarObjetivo() throws Exception {
        String objetivo = "Nuevo Objetivo";
        String email = "test@unlam.edu.ar";

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail(email);

        when(dataSource.getConnection()).thenReturn(mock(Connection.class));
        when(dataSource.getConnection().prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));
        when(dataSource.getConnection().prepareStatement(anyString()).executeQuery()).thenReturn(mock(ResultSet.class));

        mockMvc.perform(post("/guardar-objetivo")
                        .param("objetivo", objetivo)
                        .param("email", email))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/objetivos-guardados"));

        verify(dataSource).getConnection();
    }

    @Test
    public void testGuardarObjetivoUsuario() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        Long idUsuario = 1L;
        String objetivo = "Nuevo Objetivo";

        objetivosController.guardarObjetivoUsuario(idUsuario, objetivo);

        verify(preparedStatement).setLong(1, idUsuario);
        verify(preparedStatement).setString(2, objetivo);
        verify(preparedStatement).executeUpdate();
        verify(connection).close();
        verify(preparedStatement).close();
    }

    @Test
    public void testGuardarObjetivoUsuarioSQLException() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException());

        Long idUsuario = 1L;
        String objetivo = "Nuevo Objetivo";

        objetivosController.guardarObjetivoUsuario(idUsuario, objetivo);

        verify(dataSource).getConnection();
    }

    @Test
    public void testMostrarVistaObjetivos() throws Exception {
        mockMvc.perform(get("/vistaObjetivos"))
                .andExpect(status().isOk())
                .andExpect(view().name("vistaObjetivos"));
    }

    @Test
    public void testObtenerUsuarioPorEmail() throws SQLException {
        String email = "test@unlam.edu.ar";
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail(email);

        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(usuario.getId());
        when(resultSet.getString("email")).thenReturn(usuario.getEmail());

        Usuario result = objetivosController.obtenerUsuarioPorEmail(email);

        verify(preparedStatement).setString(1, email);
        assertEquals(usuario.getId(), result.getId());
        assertEquals(usuario.getEmail(), result.getEmail());
    }

    @Test
    public void testObtenerUsuarioPorEmailNoEncontrado() throws SQLException {
        String email = "test@unlam.edu.ar";

        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Usuario result = objetivosController.obtenerUsuarioPorEmail(email);

        verify(preparedStatement).setString(1, email);
        assertNull(result);
    }

    @Test
    public void testGuardarObjetivoConParametroVacio() throws Exception {
        String objetivo = "";
        String email = "test@unlam.edu.ar";

        mockMvc.perform(post("/guardar-objetivo")
                        .param("objetivo", objetivo)
                        .param("email", email))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/objetivos-guardados"));
    }

    @Test
    public void testGuardarObjetivoConParametroNulo() throws Exception {
        String email = "test@unlam.edu.ar";

        mockMvc.perform(post("/guardar-objetivo")
                        .param("objetivo", (String) null)
                        .param("email", email))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/objetivos-guardados"));
    }

    @Test
    public void testGuardarObjetivoUsuarioSinUsuario() throws Exception {
        String objetivo = "Nuevo Objetivo";
        String email = "inexistente@unlam.edu.ar";

        mockMvc.perform(post("/guardar-objetivo")
                        .param("objetivo", objetivo)
                        .param("email", email))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error"));
    }
}
