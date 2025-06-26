package com.tallerwebi.dominio;

import com.tallerwebi.dominio.ServicioPlacaDeVideo;
import com.tallerwebi.dominio.ServicioPlacaDeVideoImpl;
import com.tallerwebi.dominio.entidades.PlacaDeVideo;
import com.tallerwebi.dominio.entidades.Procesador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServicioPlacaDeVideoTest {

    private ServicioPlacaDeVideo servicio;

    @BeforeEach
    public void init() {
        servicio = new ServicioPlacaDeVideoImpl();
    }

    // Test para verificarGraficosIntegrados

    @Test
    public void debeDevolverTrueSiProcesadorTieneGraficosIntegradosYPrecioEsCero() {
        Procesador procesador = new Procesador();
        procesador.setIncluyeGraficosIntegrados(true);

        PlacaDeVideo placa = new PlacaDeVideo();
        placa.setPrecio(0.0);

        assertTrue(servicio.verificarGraficosIntegrados(placa, procesador));
    }

    @Test
    public void debeDevolverFalseSiProcesadorNoTieneGraficosIntegrados() {
        Procesador procesador = new Procesador();
        procesador.setIncluyeGraficosIntegrados(false);

        PlacaDeVideo placa = new PlacaDeVideo();
        placa.setPrecio(0.0);

        assertFalse(servicio.verificarGraficosIntegrados(placa, procesador));
    }

    @Test
    public void debeDevolverFalseSiPrecioDeLaPlacaEsMayorACero() {
        Procesador procesador = new Procesador();
        procesador.setIncluyeGraficosIntegrados(true);

        PlacaDeVideo placa = new PlacaDeVideo();
        placa.setPrecio(45000.0);

        assertFalse(servicio.verificarGraficosIntegrados(placa, procesador));
    }

    @Test
    public void debeDevolverFalseSiAlgunoDeLosDosEsNullEnGraficosIntegrados() {
        assertFalse(servicio.verificarGraficosIntegrados(null, new Procesador()));
        assertFalse(servicio.verificarGraficosIntegrados(new PlacaDeVideo(), null));
    }

    // Test para verificarPrecioMayorACero

    @Test
    public void debeDevolverTrueSiPrecioEsMayorACero() {
        PlacaDeVideo placa = new PlacaDeVideo();
        placa.setPrecio(60000.0);

        assertTrue(servicio.verificarPrecioMayorACero(placa));
    }

    @Test
    public void debeDevolverFalseSiPrecioEsCero() {
        PlacaDeVideo placa = new PlacaDeVideo();
        placa.setPrecio(0.0);

        assertFalse(servicio.verificarPrecioMayorACero(placa));
    }

    @Test
    public void debeDevolverFalseSiPrecioEsNegativo() {
        PlacaDeVideo placa = new PlacaDeVideo();
        placa.setPrecio(-100.0);

        assertFalse(servicio.verificarPrecioMayorACero(placa));
    }

    @Test
    public void debeDevolverFalseSiPlacaEsNull() {
        assertFalse(servicio.verificarPrecioMayorACero(null));
    }
}
