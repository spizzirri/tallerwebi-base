package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.DiaCalendario;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import com.tallerwebi.dominio.Usuario;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.tallerwebi.dominio.*;

public class RepositorioDiaCalendarioTest {

    private DiaCalendario diaCalendario;

    @Test
    public void testGuardarDiaCalendario() {
        RepositorioDiaCalendario repositorio = new RepositorioDiaCalendarioImpl(); // Suponiendo que tienes una implementación del repositorio

        DiaCalendario dia = new DiaCalendario(1,1, 1, 2024, Rendimiento.NORMAL);

        // Guardar el DiaCalendario en el repositorio
        repositorio.guardar(dia);

        // Buscar el DiaCalendario en el repositorio y verificar que fue guardado correctamente
        DiaCalendario diaGuardado = repositorio.buscar(dia.getId());
        assertNotNull(diaGuardado);
        assertEquals(dia, diaGuardado);
    }
}

