package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServicioCoolerTest {

    private ServicioCooler servicio;

    @BeforeEach
    public void init() {
        servicio = new ServicioCoolerImpl();
    }

    // Test para verificarCoolerIncluido

    @Test
    public void debeDevolverTrueSiProcesadorIncluyeCoolerYPrecioEsCero() {
        Procesador procesador = new Procesador();
        procesador.setIncluyeCooler(true);

        CoolerCPU cooler = new CoolerCPU();
        cooler.setPrecio(0.0);

        assertTrue(servicio.verificarCoolerIncluido(cooler, procesador));
    }

    @Test
    public void debeDevolverFalseSiProcesadorNoIncluyeCooler() {
        Procesador procesador = new Procesador();
        procesador.setIncluyeCooler(false);

        CoolerCPU cooler = new CoolerCPU();
        cooler.setPrecio(0.0);

        assertFalse(servicio.verificarCoolerIncluido(cooler, procesador));
    }

    @Test
    public void debeDevolverFalseSiPrecioDelCoolerNoEsCero() {
        Procesador procesador = new Procesador();
        procesador.setIncluyeCooler(true);

        CoolerCPU cooler = new CoolerCPU();
        cooler.setPrecio(12000.0);

        assertFalse(servicio.verificarCoolerIncluido(cooler, procesador));
    }

    @Test
    public void debeDevolverFalseSiElCoolerOElProcesadorEsNull() {
        assertFalse(servicio.verificarCoolerIncluido(null, new Procesador()));
        assertFalse(servicio.verificarCoolerIncluido(new CoolerCPU(), null));
    }

    // Test para verificar compatibilidad cooler con motherboard


    @Test
    public void debeDevolverTrueSiElCoolerTieneSocketCompatibleYPrecioMayorACero() {
        Socket socketAM4 = new Socket(1L, "AM4");

        Motherboard motherboard = new Motherboard();
        motherboard.setSocket(socketAM4);

        CoolerCPU cooler = new CoolerCPU();
        cooler.setSockets(new HashSet<>(List.of(socketAM4)));
        cooler.setPrecio(4500.0);

        assertTrue(servicio.verificarCompatibilidadDeCoolerConMotherboard(motherboard, cooler));
    }

    @Test
    public void debeDevolverFalseSiElCoolerNoEsCompatibleConElSocketDeLaMotherboard() {
        Socket socketAM4 = new Socket(1L, "AM4");
        Socket socketLGA1700 = new Socket(2L, "LGA1700");

        Motherboard motherboard = new Motherboard();
        motherboard.setSocket(socketAM4);

        CoolerCPU cooler = new CoolerCPU();
        cooler.setSockets(new HashSet<Socket>(List.of(socketLGA1700)));

        assertFalse(servicio.verificarCompatibilidadDeCoolerConMotherboard(motherboard, cooler));
    }

    @Test
    public void debeDevolverFalseSiCoolerTieneSocketCompatiblePeroPrecioEsCero() {
        Socket socketAM4 = new Socket(1L, "AM4");

        Motherboard motherboard = new Motherboard();
        motherboard.setSocket(socketAM4);

        CoolerCPU cooler = new CoolerCPU();
        cooler.setSockets(new HashSet<>(List.of(socketAM4)));
        cooler.setPrecio(0.0);

        assertFalse(servicio.verificarCompatibilidadDeCoolerConMotherboard(motherboard, cooler));
    }


    // Esta bien llamarlo 2 veces en el mismo test? son sencillitos igual

    @Test
    public void debeDevolverFalseSiElCoolerOMotherboardSonNull() {
        assertFalse(servicio.verificarCompatibilidadDeCoolerConMotherboard(null, new CoolerCPU()));
        assertFalse(servicio.verificarCompatibilidadDeCoolerConMotherboard(new Motherboard(), null));
    }
}