package com.tallerwebi.dominio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.mockito.Mockito;

public class ServicioHamburgueseriasImplTest {
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
        // Preparacion
        Hamburgueseria adherida1 = new Hamburgueseria();
        adherida1.setId(1L);
        adherida1.setNombre("Adherida 1");
        adherida1.setEsComercioAdherido(true);

        Hamburgueseria adherida2 = new Hamburgueseria();
        adherida2.setId(2L);
        adherida2.setNombre("Adherida 2");
        adherida2.setEsComercioAdherido(true);

        Hamburgueseria noAdherida1 = new Hamburgueseria();
        noAdherida1.setId(3L);
        noAdherida1.setNombre("No Adherida 1");
        noAdherida1.setEsComercioAdherido(false);

        List<Hamburgueseria> mockLista = List.of(adherida1, adherida2, noAdherida1);
        Mockito.when(repositorioHamburgueseriasMock.buscarHamburgueseriasCercanas(latitudMock, longitudMock))
                .thenReturn(mockLista);

        // Ejecucion
        List<Hamburgueseria> resultado = servicioHamburgueserias.obtenerHamburgueseriasCercanas(latitudMock,
                longitudMock);

        // Validacion
        Mockito.verify(repositorioHamburgueseriasMock, Mockito.times(1)).buscarHamburgueseriasCercanas(latitudMock,
                longitudMock);
        assertEquals(mockLista, resultado);
    }
}
