package com.tallerwebi.integracion;

import com.tallerwebi.dominio.ServicioHamburgueseriasImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.mockito.Mockito;
import com.tallerwebi.dominio.RepositorioHamburgueserias;
import com.tallerwebi.dominio.Hamburgueseria;

public class ServicioHamburgueseriasTest {
    private ServicioHamburgueseriasImpl servicioHamburgueserias;
    private RepositorioHamburgueserias repositorioHamburgueseriasMock;

    private Double latitudMock = 45.0;
    private Double longitudMock = -75.0;

    @BeforeEach
    public void init() {
        repositorioHamburgueseriasMock = Mockito.mock(RepositorioHamburgueserias.class);
        servicioHamburgueserias = new ServicioHamburgueseriasImpl(repositorioHamburgueseriasMock);
    }

    @Test
    public void obtenerHamburgueseriasCercanasDeberiaRetornarListaNoNula() {
        List<Hamburgueseria> resultado = servicioHamburgueserias.obtenerHamburgueseriasCercanas(latitudMock,
                longitudMock);
        assertNotNull(resultado);
    }
}
