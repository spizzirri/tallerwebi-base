package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.perfil.ServicioPerfil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ModelAndView;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class ControladorPerfilTest {

    private ServicioPerfil servicioPerfil;
    private ControladorPerfil controladorPerfil;

    @Autowired
    private MockMvc mockMvc;

//    @BeforeEach
//    public void init() {
//        this.servicioPerfil = mock(ServicioPerfil.class);
//        this.controladorPerfil = new ControladorPerfil(this.servicioPerfil);
//    }


}
