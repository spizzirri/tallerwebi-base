package com.tallerwebi.presentacion;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.tallerwebi.dominio.Hamburgueseria;
import com.tallerwebi.dominio.ServicioCoordenadas;
import com.tallerwebi.dominio.ServicioHamburgueserias;

public class ControladorHamburgueseriasTest {
	private Double latitudMock = 45.0;
	private Double longitudMock = -75.0;
    private ControladorHamburgueserias controladorHamburgueserias;
    private ServicioCoordenadas servicioCoordenadas = mock(ServicioCoordenadas.class);
    private ServicioHamburgueserias servicioHamburgueserias = mock(ServicioHamburgueserias.class);
    
	@BeforeEach
	public void init(){
        controladorHamburgueserias = new ControladorHamburgueserias(servicioCoordenadas , servicioHamburgueserias);
	}

    @Test
    public void listarhamburgueseriasDeberiaRetornarBadRequestSiLatitudEsInvalida() {
        when(this.servicioCoordenadas.validarLatitud(null)).thenReturn(false);
        when(this.servicioCoordenadas.validarLongitud(longitudMock)).thenReturn(true);
        org.junit.jupiter.api.Assertions.assertThrows(
            ResponseStatusException.class,
            () -> controladorHamburgueserias.listarhamburgueserias(null, longitudMock)
        );
    }

    @Test
    public void listarhamburgueseriasDeberiaRetornarBadRequestSiLongitudEsInvalida() {
        when(this.servicioCoordenadas.validarLatitud(latitudMock)).thenReturn(true);
        when(this.servicioCoordenadas.validarLongitud(null)).thenReturn(false);
        org.junit.jupiter.api.Assertions.assertThrows(
            ResponseStatusException.class,
            () -> controladorHamburgueserias.listarhamburgueserias(latitudMock, null)
        );
    }

    @Test
    public void listarhamburgueseriasDeberiaRetornarUnaListaDeHamburgueseriasSiLatitudYLongitudSonValidas() {
        when(this.servicioCoordenadas.validarLatitud(latitudMock)).thenReturn(true);
        when(this.servicioCoordenadas.validarLongitud(longitudMock)).thenReturn(true);
        when(this.servicioHamburgueserias.obtenerHamburgueseriasCercanas(latitudMock, longitudMock)).thenReturn(new ArrayList<Hamburgueseria>());
        ResponseEntity<ArrayList<Hamburgueseria>> resultado = controladorHamburgueserias.listarhamburgueserias(latitudMock, longitudMock);
        org.junit.jupiter.api.Assertions.assertNotNull(resultado);
    }
}
