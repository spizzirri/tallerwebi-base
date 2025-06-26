package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServicioMotherboardTest {

    ServicioMotherboard servicio;

    @BeforeEach
    public void init(){
        servicio = new ServicioMotherboardImpl();
    }

    // Test de compatibilidad motherboard con procesador
    @Test
    public void debeDevolverTrueCuandoSocketDeMotherboardYProcesadorCoinciden() {
        Socket socket = new Socket(1L, "AM4");
        Motherboard motherboard = new Motherboard();
        motherboard.setSocket(socket);

        Procesador procesador = new Procesador();
        procesador.setSocket(socket);

        assertTrue(servicio.verificarCompatibilidadDeMotherboardConProcesador(motherboard, procesador));
    }

    @Test
    public void debeDevolverFalseCuandoSocketDeMotherboardYProcesadorNoCoinciden() {
        Motherboard motherboard = new Motherboard();
        motherboard.setSocket(new Socket(1L, "AM4"));

        Procesador procesador = new Procesador();
        procesador.setSocket(new Socket(2L, "LGA1700"));


        assertFalse(servicio.verificarCompatibilidadDeMotherboardConProcesador(motherboard, procesador));
    }

    @Test
    public void debeDevolverFalseCuandoAlgunoDeLosParametrosEsNullEnCompatibilidadProcesador() {
        ServicioMotherboard servicio = new ServicioMotherboardImpl();
        assertFalse(servicio.verificarCompatibilidadDeMotherboardConProcesador(null, new Procesador()));
        assertFalse(servicio.verificarCompatibilidadDeMotherboardConProcesador(new Motherboard(), null));
    }

    // Test de compatibilidad motherboard con ram
    @Test
    public void debeDevolverTrueCuandoTipoDeMemoriaYTecnologiaCoinciden() {
        Motherboard motherboard = new Motherboard();
        motherboard.setTipoMemoria("DDR4");

        MemoriaRAM ram = new MemoriaRAM();
        ram.setTecnologiaRAM("DDR4");

        assertTrue(servicio.verificarCompatibilidadDeMotherboardConMemoriaRAM(motherboard, ram));
    }

    @Test
    public void debeDevolverFalseCuandoTipoDeMemoriaYTecnologiaNoCoinciden() {
        Motherboard motherboard = new Motherboard();
        motherboard.setTipoMemoria("DDR5");

        MemoriaRAM ram = new MemoriaRAM();
        ram.setTecnologiaRAM("DDR4");

        assertFalse(servicio.verificarCompatibilidadDeMotherboardConMemoriaRAM(motherboard, ram));
    }

    @Test
    public void debeDevolverFalseCuandoAlgunoDeLosParametrosEsNullEnCompatibilidadMemoria() {
        assertFalse(servicio.verificarCompatibilidadDeMotherboardConMemoriaRAM(null, new MemoriaRAM()));
        assertFalse(servicio.verificarCompatibilidadDeMotherboardConMemoriaRAM(new Motherboard(), null));
    }

    // Test de compatibilidad motherboard con almacenamiento
    @Test
    public void debeDevolverTrueSiMotherTieneSlotM2YAlmacenamientoEsM2() {
        Motherboard motherboard = new Motherboard();
        motherboard.setCantSlotsM2(1);
        motherboard.setCantPuertosSata(0);

        Almacenamiento almacenamiento = new Almacenamiento();
        almacenamiento.setTipoDeConexion("M2");

        assertTrue(servicio.verificarCompatibilidadDeMotherboardConTipoDeConexionDeAlmacenamiento(motherboard, almacenamiento));
    }

    @Test
    public void debeDevolverTrueSiMotherTienePuertoSataYAlmacenamientoEsSata() {
        Motherboard motherboard = new Motherboard();
        motherboard.setCantSlotsM2(0);
        motherboard.setCantPuertosSata(2);

        Almacenamiento almacenamiento = new Almacenamiento();
        almacenamiento.setTipoDeConexion("SATA");

        assertTrue(servicio.verificarCompatibilidadDeMotherboardConTipoDeConexionDeAlmacenamiento(motherboard, almacenamiento));
    }

    @Test
    public void debeDevolverFalseSiMotherNoTieneSlotsNiPuertosYAlmacenamientoEsM2() {
        Motherboard motherboard = new Motherboard();
        motherboard.setCantSlotsM2(0);
        motherboard.setCantPuertosSata(0);

        Almacenamiento almacenamiento = new Almacenamiento();
        almacenamiento.setTipoDeConexion("M2");

        assertFalse(servicio.verificarCompatibilidadDeMotherboardConTipoDeConexionDeAlmacenamiento(motherboard, almacenamiento));
    }

    @Test
    public void debeDevolverFalseCuandoMotherboardOAlmacenamientoEsNull() {

        assertFalse(servicio.verificarCompatibilidadDeMotherboardConTipoDeConexionDeAlmacenamiento(null, new Almacenamiento()));
        assertFalse(servicio.verificarCompatibilidadDeMotherboardConTipoDeConexionDeAlmacenamiento(new Motherboard(), null));
    }
}
