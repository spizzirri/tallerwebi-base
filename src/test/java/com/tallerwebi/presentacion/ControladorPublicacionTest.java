package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Publicacion;
import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.ServicioPublicado;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

public class ControladorPublicacionTest {

    private ControladorLogin controladorLogin;
    private Usuario usuarioMock;
    private DatosLogin datosLoginMock;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    private ServicioLogin servicioLoginMock;
    private Publicacion publicMock;
    private ControladorPublicacion controladorPublicacion;
    private ServicioPublicado servicioPublicadoMock;

    @BeforeEach
    public void init() {
        datosLoginMock = new DatosLogin("dami@unlam.com", "123");
        usuarioMock = mock(Usuario.class);
        when(usuarioMock.getEmail()).thenReturn("dami@unlam.com");
        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);

        servicioLoginMock = mock(ServicioLogin.class);
        controladorLogin = new ControladorLogin(servicioLoginMock);

        publicMock = mock(Publicacion.class);
        when(publicMock.getDescripcion()).thenReturn("Lo que se me cante");

        servicioPublicadoMock = mock(ServicioPublicado.class);
        controladorPublicacion = new ControladorPublicacion(servicioPublicadoMock);


    }

    @Test
    public void queSePuedaCrearUnaPublicacionConDescripcionYUsuarioYQueVayaAPublicaciones() { /*cambio a Home porque es donde se va a ver*/
        // preparacion
        Publicacion publicacionMock = mock(Publicacion.class);


        // Mockear sesión para que el controlador obtenga el usuario
        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("usuarioLogueado")).thenReturn(usuarioMock);

        // ejecucion
        ModelAndView modelAndView = controladorPublicacion.agregarPublicacion(publicacionMock,  requestMock);

        // validacion
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
    }


    /*que se muestre en nuevo-publicacion */
    @Test
    public void queSeRedirijaAlHomeDespuesDePublicar() throws Exception {
        // Preparación
        Publicacion publiMock = mock(Publicacion.class);
        Usuario userMock = mock(Usuario.class);
        when(publiMock.getDescripcion()).thenReturn("Hola mundo");
        when(publiMock.getUsuario()).thenReturn(userMock);
        when(servicioPublicadoMock.publicacionEntera(anyString(), any())).thenReturn(publiMock);

        // Ejecución
        ModelAndView modelAndView = controladorPublicacion.agregarPublicacion(publiMock, requestMock);

        // Validación
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));


    }

}