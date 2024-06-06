package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.rutina.RepositorioRutina;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.rutina.Rutina;
import com.tallerwebi.dominio.rutina.ServicioRutina;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


public class ControladorRutinaTest {

    private Usuario usuarioMock;
    private MockMvc mockMvc;

    @Mock
    private ServicioRutina servicioRutina;
    @InjectMocks
    private ControladorRutina controladorRutina;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controladorRutina).build();
        usuarioMock = new Usuario();
        usuarioMock.setNombre("Lautaro");
        usuarioMock.setObjetivo(Objetivo.PERDIDA_DE_PESO);
    }

    @Test
    public void queAlIrALaPantallaDeRutinaMeMuestreLaVistaDeRutina() throws Exception {
        // Preparación
        DatosRutina datosRutinaMock = new DatosRutina(new Rutina("Rutina de correr", Objetivo.PERDIDA_DE_PESO));

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

        assertThat(rutinaObtenida.getNombre(),equalTo("Rutina de correr"));

    }

    @Test
<<<<<<< HEAD
    public void QueAlIrALaVistaRutinasMeMuestreRutinasRelacionadasAMiObjetivo(){
        // Preparación
        DatosRutina datosRutinaMock1 = new DatosRutina(new Rutina("Rutina de correr", Objetivo.PERDIDA_DE_PESO));
        DatosRutina datosRutinaMock2 = new DatosRutina(new Rutina("Rutina de trotar", Objetivo.PERDIDA_DE_PESO));
        DatosRutina datosRutinaMock3 = new DatosRutina(new Rutina("Rutina de caminar", Objetivo.PERDIDA_DE_PESO));
=======
    public void queAlIrALaVistaRutinasMeMuestreRutinasRelacionadasAMiObjetivo(){
        //preparacion
        usuarioMock = new Usuario("Lautaro", Objetivo.PERDIDA_DE_PESO);

        DatosRutina datosRutinaMock1 = new DatosRutina(new Rutina("Rutina de correr",Objetivo.PERDIDA_DE_PESO));
        DatosRutina datosRutinaMock2 = new DatosRutina(new Rutina("Rutina de trotar",Objetivo.PERDIDA_DE_PESO));
        DatosRutina datosRutinaMock3 = new DatosRutina(new Rutina("Rutina de caminar",Objetivo.PERDIDA_DE_PESO));
>>>>>>> 4f35be0c3d69ca196298f4213a082a6984710c39
        List<DatosRutina> datosRutinasMock = new ArrayList<>();
        datosRutinasMock.add(datosRutinaMock1);
        datosRutinasMock.add(datosRutinaMock2);
        datosRutinasMock.add(datosRutinaMock3);

        when(servicioRutina.getRutinasPorObjetivo(Objetivo.PERDIDA_DE_PESO)).thenReturn(datosRutinasMock);

        // Ejecución
        ModelAndView modelAndView = controladorRutina.VerRutinasQueLeInteresanAlUsuario(usuarioMock.getObjetivo().toString());

        // Verificación
        List<DatosRutina> rutinasObtenidas = (List<DatosRutina>) modelAndView.getModel().get("rutinas");

        assertThat(rutinasObtenidas.size(),equalTo(3));
        assertThat(modelAndView.getViewName(),equalTo("rutinas"));
    }



}
