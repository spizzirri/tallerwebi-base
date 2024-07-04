package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.excepcion.NoCambiosRestantesException;
import com.tallerwebi.dominio.usuario.ServicioLogin;
import com.tallerwebi.dominio.reto.ServicioReto;
import com.tallerwebi.dominio.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import javax.servlet.http.HttpSession;
import java.util.NoSuchElementException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.ArgumentMatchers.anyLong;

public class ControladorRetoTest {

    @Mock
    private ServicioReto servicioReto;

    @Mock
    private ServicioLogin servicioLogin;

    @Mock
    private HttpSession session;

    @InjectMocks
    private ControladorReto controladorReto;

    @BeforeEach
    public void init() {
        this.servicioReto = mock(ServicioReto.class);
        MockitoAnnotations.initMocks(this);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controladorReto).build();
    }

    @Test
    public void queAlEmpezarRetoConRetoInexistenteMuestreError() throws NoSuchElementException {
        // Preparación
        Usuario usuarioMock = new Usuario();
        when(session.getAttribute("usuario")).thenReturn(usuarioMock);
        doThrow(new NoSuchElementException("Reto no encontrado")).when(servicioReto).empezarRetoActualizado(anyLong());

        // Ejecución
        ModelAndView mav = controladorReto.empezarReto(1L, "test@example.com", "password", session);

        // Verificación
        assertThat(mav.getViewName(), equalTo("home")); // Vista correcta
        assertThat(mav.getModel().get("error").toString(), equalTo("El reto no existe: Reto no encontrado")); // Mensaje de error
    }

    @Test
    public void queAlEmpezarRetoConErrorGenericoMuestreError() throws RuntimeException {
        // preparación
        HttpSession session = mock(HttpSession.class);
        Usuario usuarioMock = new Usuario();
        when(session.getAttribute("usuario")).thenReturn(usuarioMock);

        long retoId = 1L;
        String email = "test@example.com";
        String password = "password";

        doThrow(new RuntimeException("error")).when(servicioReto).empezarRetoActualizado(eq(retoId));

        // ejecución
        ModelAndView mav = controladorReto.empezarReto(retoId, email, password, session);

        // verificación
        assertThat((mav.getViewName()), equalTo("home")); // Verificar la vista correcta
        assertTrue(mav.getModel().containsKey("error")); // Verificar que el modelo contiene la clave "error"
        assertThat((mav.getModel().get("error").toString()), equalTo("Ocurrió un error al iniciar el reto: error")); // Verificar el mensaje de error
    }

    @Test
    public void queAlTerminarRetoConRetoInexistenteMuestreError() throws NoSuchElementException {
        // Preparación
        Usuario usuarioEncontradoMock = mock(Usuario.class);
        when(session.getAttribute("usuario")).thenReturn(usuarioEncontradoMock);
        doThrow(new NoSuchElementException("Reto no encontrado")).when(servicioLogin).modificarRachaRetoTerminado(any(), anyLong());
        when(servicioLogin.consultarUsuario(anyString(), anyString())).thenReturn(usuarioEncontradoMock);

        // Ejecución
        ModelAndView mav = controladorReto.terminarReto(1L, "test@example.com", "password", session);

        // Verificación
        assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/home")); // Redirige a home
        assertThat((mav.getModel().get("error").toString()), equalTo("El reto no existe: Reto no encontrado")); // Verificar el mensaje de error
    }

    @Test
    public void queAlTerminarRetoConErrorGenericoMuestreError() throws RuntimeException {
        // Preparación
        Usuario usuarioMock = mock(Usuario.class);
        when(session.getAttribute("usuario")).thenReturn(usuarioMock);
        doThrow(new RuntimeException("Error genérico")).when(servicioLogin).modificarRachaRetoTerminado(any(), anyLong());
        when(servicioLogin.consultarUsuario(anyString(), anyString())).thenReturn(usuarioMock);
        // Ejecución
        ModelAndView mav = controladorReto.terminarReto(1L, "test@example.com", "password", session);

        // Verificación
        assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/home")); // Redirige a home
        assertThat(mav.getModel().get("error").toString(), equalTo("Ocurrió un error al terminar el reto: Error genérico")); // Mensaje de error
    }

    @Test
    public void queAlCambiarRetoConRetoInexistenteMuestreError() throws Exception {
        // Preparación
        Usuario usuarioMock = new Usuario();
        when(session.getAttribute("usuario")).thenReturn(usuarioMock);
        when(servicioLogin.consultarUsuario(anyString(), anyString())).thenReturn(usuarioMock);
        doThrow(new NoSuchElementException("Reto no encontrado")).when(servicioLogin).cambiarReto(anyLong(), any(Usuario.class));

        // Ejecución
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        ModelAndView mav = controladorReto.cambiarReto(1L, "test@example.com", "password", session, redirectAttributes);

        // Verificación
        assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/home")); // Redirige a home
        assertThat(redirectAttributes.getFlashAttributes().get("error").toString(), equalTo("No se encontró un nuevo reto disponible: Reto no encontrado")); // Mensaje de error
    }

    @Test
    public void queAlCambiarRetoSinCambiosRestantesMuestreError() throws Exception {
        // Preparación
        Usuario usuarioMock = new Usuario();
        when(session.getAttribute("usuario")).thenReturn(usuarioMock);
        when(servicioLogin.consultarUsuario(anyString(), anyString())).thenReturn(usuarioMock);
        doThrow(new NoCambiosRestantesException("No te quedan cambios")).when(servicioLogin).cambiarReto(anyLong(), any(Usuario.class));

        // Ejecución
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        ModelAndView mav = controladorReto.cambiarReto(1L, "test@example.com", "password", session, redirectAttributes);

        // Verificación
        assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/home")); // Redirige a home
        assertThat(redirectAttributes.getFlashAttributes().get("error").toString(), equalTo("No te quedan cambios. Debes terminar los retos.")); // Mensaje de error
    }

    @Test
    public void queAlCambiarRetoConErrorGenericoMuestreError() throws RuntimeException {
        // Preparación
        Usuario usuarioMock = new Usuario();
        when(session.getAttribute("usuario")).thenReturn(usuarioMock);
        when(servicioLogin.consultarUsuario(anyString(), anyString())).thenReturn(usuarioMock);
        doThrow(new RuntimeException("Error genérico")).when(servicioLogin).cambiarReto(anyLong(), any(Usuario.class));

        // Ejecución
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        ModelAndView mav = controladorReto.cambiarReto(1L, "test@example.com", "password", session, redirectAttributes);

        // Verificación
        assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/home")); // Redirige a home
        assertThat(redirectAttributes.getFlashAttributes().get("error").toString(), equalTo("Ocurrió un error al cambiar el reto: Error genérico")); // Mensaje de error
    }
}
