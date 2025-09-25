package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import com.tallerwebi.dominio.Hamburgueseria;
import com.tallerwebi.dominio.ServicioCoordenadas;
import com.tallerwebi.dominio.ServicioHamburgueserias;

public class ControladorHamburgueseriasTest {
    private Double latitudMock = 45.0;
    private Double longitudMock = -75.0;
    private ControladorHamburgueserias controladorHamburgueserias;

    @Autowired
    private ServicioCoordenadas servicioCoordenadasMock;
    private ServicioHamburgueserias servicioHamburgueseriasMock;

    @BeforeEach
    public void init() {
        servicioCoordenadasMock = mock(ServicioCoordenadas.class);
        servicioHamburgueseriasMock = mock(ServicioHamburgueserias.class);
        controladorHamburgueserias = new ControladorHamburgueserias(servicioCoordenadasMock,
                servicioHamburgueseriasMock);
        Mockito.spy(HamburgueseriasCercanasMapper.class);
    }

    @Test
    public void irAHamburgueseriasCercanasDeberiaLLevarAHamburgueseriasCercanas() {
        // preparacion

        // ejecucion
        ModelAndView modelAndView = controladorHamburgueserias.irAHamburgueseriasCercanas();

        // validacion
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("hamburgueserias-cercanas"));
    }

    @Test
    public void listarhamburgueseriasDeberiaRetornarBadRequestSiLatitudEsInvalida() {
        // Preparación
        when(this.servicioCoordenadasMock.validarLatitud(null)).thenReturn(false);
        when(this.servicioCoordenadasMock.validarLongitud(longitudMock)).thenReturn(true);

        // Ejecución y Validación
        org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class,
                () -> controladorHamburgueserias.listarhamburgueserias(null, longitudMock));

        Mockito.verify(servicioCoordenadasMock, Mockito.times(1)).validarLatitud(null);
        Mockito.verify(servicioCoordenadasMock, Mockito.never()).validarLongitud(longitudMock);
        Mockito.verify(servicioHamburgueseriasMock, Mockito.never()).obtenerHamburgueseriasCercanas(latitudMock,
                longitudMock);
    }

    @Test
    public void listarhamburgueseriasDeberiaRetornarBadRequestSiLongitudEsInvalida() {
        // Preparación
        when(this.servicioCoordenadasMock.validarLatitud(latitudMock)).thenReturn(true);
        when(this.servicioCoordenadasMock.validarLongitud(null)).thenReturn(false);

        // Ejecución y Validación
        org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class,
                () -> controladorHamburgueserias.listarhamburgueserias(latitudMock, null));

        Mockito.verify(servicioCoordenadasMock, Mockito.times(1)).validarLatitud(latitudMock);
        Mockito.verify(servicioCoordenadasMock, Mockito.times(1)).validarLongitud(null);
        Mockito.verify(servicioHamburgueseriasMock, Mockito.never()).obtenerHamburgueseriasCercanas(latitudMock,
                longitudMock);
    }

    @Test
    public void listarhamburgueseriasDeberiaRetornarUnaListaVaciaDeHamburgueseriassCercanasSiLatitudYLongitudSonValidasPeroNoHayNingunaCerca() {
        // Preparación
        when(this.servicioCoordenadasMock.validarLatitud(latitudMock)).thenReturn(true);
        when(this.servicioCoordenadasMock.validarLongitud(longitudMock)).thenReturn(true);
        when(this.servicioHamburgueseriasMock.obtenerHamburgueseriasCercanas(latitudMock, longitudMock))
                .thenReturn(List.of());

        // Ejecución
        ResponseEntity<List<HamburgueseriaCercana>> resultado = controladorHamburgueserias
                .listarhamburgueserias(latitudMock, longitudMock);

        // Validación
        assertNotNull(resultado);
        List<HamburgueseriaCercana> listaHamburgueserias = resultado.getBody();
        assertNotNull(listaHamburgueserias);
        if (listaHamburgueserias != null) {
            assert (listaHamburgueserias.size() == 0);
        }

        Mockito.verify(servicioCoordenadasMock, Mockito.times(1)).validarLatitud(latitudMock);
        Mockito.verify(servicioCoordenadasMock, Mockito.times(1)).validarLongitud(longitudMock);
        Mockito.verify(servicioHamburgueseriasMock, Mockito.times(1)).obtenerHamburgueseriasCercanas(latitudMock,
                longitudMock);
    }

    @Test
    public void listarhamburgueseriasDeberiaRetornarUnaListaDeHamburgueseriassCercanasSiLatitudYLongitudSonValidas() {
        // Preparación
        when(this.servicioCoordenadasMock.validarLatitud(latitudMock)).thenReturn(true);
        when(this.servicioCoordenadasMock.validarLongitud(longitudMock)).thenReturn(true);
        when(this.servicioHamburgueseriasMock.obtenerHamburgueseriasCercanas(latitudMock, longitudMock))
                .thenReturn(List.of(new Hamburgueseria()));

        // Ejecución
        ResponseEntity<List<HamburgueseriaCercana>> resultado = controladorHamburgueserias
                .listarhamburgueserias(latitudMock, longitudMock);

        // Validación
        assertNotNull(resultado);
        List<HamburgueseriaCercana> listaHamburgueserias = resultado.getBody();
        assertNotNull(listaHamburgueserias);
        if (listaHamburgueserias != null) {
            assert (listaHamburgueserias.size() >= 1);
        }

        Mockito.verify(servicioCoordenadasMock, Mockito.times(1)).validarLatitud(latitudMock);
        Mockito.verify(servicioCoordenadasMock, Mockito.times(1)).validarLongitud(longitudMock);
        Mockito.verify(servicioHamburgueseriasMock, Mockito.times(1)).obtenerHamburgueseriasCercanas(latitudMock,
                longitudMock);
    }
}
