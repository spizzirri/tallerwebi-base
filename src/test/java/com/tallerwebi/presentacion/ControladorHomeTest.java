package com.tallerwebi.presentacion;



import com.tallerwebi.dominio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ControladorHomeTest {
    private ControladorHome controlador;
    private ServicioPublicado servicioPublicadoMock;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    private Usuario usuarioLogueado;
    private ServicioLike servicioLikeMock;
    private RepositorioUsuario repositorioUsuarioMock;

    @BeforeEach
    public void init() {
        // Mocks
        servicioPublicadoMock = mock(ServicioPublicado.class);
        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);
        when(requestMock.getSession()).thenReturn(sessionMock);


        // Usuario de prueba
        usuarioLogueado = new Usuario();
        usuarioLogueado.setNombre("Ana");
        usuarioLogueado.setApellido("Perez");
        usuarioLogueado.setCarreras(List.of(new Carrera()));

        // Simular sesión
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(usuarioLogueado);
        when(servicioPublicadoMock.findAll()).thenReturn(List.of(new Publicacion(), new Publicacion()));

        // Mock publicaciones
        when(servicioPublicadoMock.findAll()).thenReturn(List.of(new Publicacion(), new Publicacion()));

        // Controlador
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        servicioLikeMock = mock(ServicioLike.class);
        controlador = new ControladorHome(servicioPublicadoMock, servicioLikeMock);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void iniciarSesion_seCarganDatosDeUsuarioLogueadoYPublicaciones() {
        ModelAndView mav = controlador.home(requestMock);

        // Verificar vista
        assertEquals("home", mav.getViewName());

        // Obtener model //tomando el modelo (model) que envía el controlador a la vista
        // y lo guarda en un Map para que puedas inspeccionarlo o hacer asserts en un test.
        Map<String, Object> model = mav.getModel();

        // Verificar DTO de usuario
        DatosUsuario datosUsuario = (DatosUsuario) model.get("usuario");

        assertNotNull(datosUsuario);
        assertEquals("Ana", datosUsuario.getNombre());
        assertEquals("Perez", datosUsuario.getApellido());
        assertNotNull(datosUsuario.getCarreras());
        assertFalse(datosUsuario.getCarreras().isEmpty());

        // Verificar publicaciones
        List<Publicacion> publicaciones = (List<Publicacion>) model.get("publicaciones");
        assertEquals(2, publicaciones.size());

        // Verificar formulario vacío
        assertNotNull(model.get("publicacion"));


    }






}
