package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.usuario.Usuario;
import com.tallerwebi.dominio.objetivo.Objetivo;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.rutina.Rutina;
import com.tallerwebi.dominio.rutina.ServicioRutina;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class ControladorRutinaTest {

    private Usuario usuarioMock;
    private MockMvc mockMvc;
    private MockHttpSession session;
    @Mock
    private ServicioRutina servicioRutina;
    @InjectMocks
    private ControladorRutina controladorRutina;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controladorRutina).build();
        session = new MockHttpSession();
        usuarioMock = new Usuario();
        usuarioMock.setNombre("Lautaro");
        usuarioMock.setObjetivo(Objetivo.PERDIDA_DE_PESO);
    }
    @Test
    @Transactional
    @Rollback
    public void queAlIrALaPantallaDeRutinaMeMuestreLaVistaDeRutinaSiTieneUnaRutinaActiva() throws Exception {
        // Preparación
        Rutina rutinaMock = new Rutina("Rutina de correr", Objetivo.PERDIDA_DE_PESO);
        DatosRutina datosRutinaMock = new DatosRutina(rutinaMock);
        when(servicioRutina.getRutinaParaUsuario(usuarioMock)).thenReturn(datosRutinaMock);

        // Configuración de la sesión
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuarioMock);

        // Ejecución
        MvcResult result = mockMvc.perform(get("/mi-rutina").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("rutina"))
                .andReturn();

        // Verificación
        ModelAndView modelAndView = result.getModelAndView();
        DatosRutina rutinaObtenida = (DatosRutina) modelAndView.getModel().get("rutina");
    }

    @Test
    @Transactional
    @Rollback
    public void queRedirijaAVistaObjetivosSiUsuarioNoTieneObjetivoDefinido() throws Exception {
        // Preparación
        Usuario usuarioSinObjetivo = new Usuario();
        usuarioSinObjetivo.setObjetivo(null);

        // Configuración de la sesión
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuarioSinObjetivo);

        // Ejecución
        mockMvc.perform(get("/mi-rutina").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/objetivo"));
    }

    @Test
    @Transactional
    @Rollback
    public void queRedirijaASeleccionDeRutinasSiOcurreExcepcion() throws Exception {
        // Preparación
        Objetivo objetivo = Objetivo.PERDIDA_DE_PESO;
        usuarioMock.setObjetivo(objetivo);
        when(servicioRutina.getRutinaActualDelUsuario(usuarioMock)).thenThrow(new RuntimeException());

        // Configuración de la sesión
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuarioMock);

        // Ejecución
        String objetivoFormateado = objetivo.formatear();
        String expectedRedirectUrl = "/rutinas?objetivo=" + objetivo.toString() + "&objetivoFormateado=" + objetivoFormateado;
        mockMvc.perform(get("/mi-rutina").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));
    }

    @Test
    @Transactional
    @Rollback
    public void queAsigneRutinaAlUsuarioYRedirijaAVistaDeRutina() throws Exception {
        // Preparación
        Long rutinaId = 1L;
        Usuario usuarioMock = new Usuario("Lautaro", Objetivo.PERDIDA_DE_PESO);
        usuarioMock.setId(1L);
        Rutina rutinaMock = new Rutina("Rutina de correr", Objetivo.PERDIDA_DE_PESO);
        DatosRutina datosRutinaMock = new DatosRutina(rutinaMock);

        when(servicioRutina.getRutinaById(rutinaId)).thenReturn(rutinaMock);
        when(servicioRutina.getDatosRutinaById(rutinaId)).thenReturn(datosRutinaMock);

        // Configuración de la sesión
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuarioMock);

        // Ejecución
        MvcResult result = mockMvc.perform(get("/asignar-rutina")
                        .param("id", rutinaId.toString())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("rutina"))
                .andReturn();

        // Verificación
        ModelAndView modelAndView = result.getModelAndView();
        DatosRutina rutinaObtenida = (DatosRutina) modelAndView.getModel().get("rutina");

        assertThat(rutinaObtenida.getNombre(), equalTo(datosRutinaMock.getNombre()));
        verify(servicioRutina).asignarRutinaAUsuario(rutinaMock, usuarioMock);
    }

    @Test
    @Transactional
    @Rollback
    public void queAlQuererAsignarRutinaRedirijaAVistaObjetivosSiRutinaNoExiste() throws Exception {
        // Preparación
        Long rutinaId = 1L;
        Usuario usuarioMock = new Usuario("Lautaro", Objetivo.PERDIDA_DE_PESO);
        usuarioMock.setId(1L);

        when(servicioRutina.getRutinaById(rutinaId)).thenReturn(null);

        // Configuración de la sesión
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuarioMock);

        // Ejecución
        mockMvc.perform(get("/asignar-rutina")
                        .param("id", rutinaId.toString())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/objetivo"));
    }

    @Test
    @Transactional
    @Rollback
    public void queAlQuererAsignarRutinaRedirijaAVistaObjetivosSiOcurreUnaExcepcion() throws Exception {
        // Preparación
        Long rutinaId = 1L;
        Usuario usuarioMock = new Usuario("Lautaro", Objetivo.PERDIDA_DE_PESO);
        usuarioMock.setId(1L);

        when(servicioRutina.getRutinaById(rutinaId)).thenThrow(new RuntimeException());

        // Configuración de la sesión
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuarioMock);

        // Ejecución
        mockMvc.perform(get("/asignar-rutina")
                        .param("id", rutinaId.toString())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/objetivo"));
    }

    @Test
    @Transactional
    @Rollback
    public void queLibereRutinaDelUsuarioYRedirijaASeccionDeRutinas() throws Exception {
        // Preparación
        Long rutinaId = 1L;
        Usuario usuarioMock = new Usuario("Lautaro", Objetivo.PERDIDA_DE_PESO);
        usuarioMock.setId(1L);
        Rutina rutinaMock = new Rutina("Rutina de correr", Objetivo.PERDIDA_DE_PESO);
        DatosRutina datosRutinaMock = new DatosRutina(rutinaMock);

        when(servicioRutina.getRutinaById(rutinaId)).thenReturn(rutinaMock);
        when(servicioRutina.getDatosRutinaById(rutinaId)).thenReturn(datosRutinaMock);

        // Configuración de la sesión
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuarioMock);

        // Ejecución
        MvcResult result = mockMvc.perform(post("/liberar-rutina")
                        .param("id", rutinaId.toString())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/rutinas?objetivo=PERDIDA_DE_PESO&info=*&objetivoFormateado=*"))
                .andReturn();

        // Verificación
        verify(servicioRutina).liberarRutinaActivaDelUsuario(usuarioMock);
        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getModel().get("info"), equalTo("Rutina liberada."));
        assertThat(modelAndView.getModel().get("objetivoFormateado"), equalTo(usuarioMock.getObjetivo().formatear()));
    }

    @Test
    @Transactional
    @Rollback
    public void queAlQuererLiberarRutinaMuestreErrorSiRutinaNoExiste() throws Exception {
        // Preparación
        Long rutinaId = 1L;
        Usuario usuarioMock = new Usuario("Lautaro", Objetivo.PERDIDA_DE_PESO);
        usuarioMock.setId(1L);

        when(servicioRutina.getRutinaById(rutinaId)).thenReturn(null);

        // Configuración de la sesión
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuarioMock);

        // Ejecución
        MvcResult result = mockMvc.perform(post("/liberar-rutina")
                        .param("id", rutinaId.toString())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("rutina"))
                .andReturn();

        // Verificación
        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getModel().get("error"), equalTo("Error al liberar la rutina."));
    }

    @Test
    @Transactional
    @Rollback
    public void queAlQuererLiberarRutinaMuestreErrorSiOcurreUnaExcepcion() throws Exception {
        // Preparación
        Long rutinaId = 1L;
        Usuario usuarioMock = new Usuario("Lautaro", Objetivo.PERDIDA_DE_PESO);
        usuarioMock.setId(1L);

        when(servicioRutina.getRutinaById(rutinaId)).thenThrow(new RuntimeException());

        // Configuración de la sesión
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuarioMock);

        // Ejecución
        MvcResult result = mockMvc.perform(post("/liberar-rutina")
                        .param("id", rutinaId.toString())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("rutina"))
                .andReturn();

        // Verificación
        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getModel().get("error"), equalTo("EXCEPCION: error al liberar la rutina."));
    }

}
