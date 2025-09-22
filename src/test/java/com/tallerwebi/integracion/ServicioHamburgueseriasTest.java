package com.tallerwebi.integracion;

import com.tallerwebi.dominio.ServicioCoordenadas;
import com.tallerwebi.dominio.ServicioHamburgueserias;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import com.tallerwebi.dominio.Hamburgueseria;
public class ServicioHamburgueseriasTest {
    private ServicioHamburgueserias servicioHamburgueserias;
    
	private Double latitudMock = 45.0;
	private Double longitudMock = -75.0;

    @BeforeEach
    public void init() {
        servicioHamburgueserias = new ServicioHamburgueserias();
    }
    
    @Test
    public void obtenerHamburgueseriasCercanasDeberiaRetornarListaNoNula() {
        ArrayList<Hamburgueseria> resultado = servicioHamburgueserias.obtenerHamburgueseriasCercanas(latitudMock,longitudMock);
        assertNotNull(resultado);
    }
}
