package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServicioFuenteTest {

    private ServicioFuenteImpl servicio;

    @BeforeEach
    public void init() {
        servicio = new ServicioFuenteImpl();
    }

   // VERIFICAR LUEGO SI CAMBIAR LOS STRINGS POR DOUBLES

    @Test
    public void debeDevolverTrueCuandoLaFuenteTieneMasWattsQueElArmado() {
        // Armado con procesador de 65W y GPU de 150W
        Procesador cpu = new Procesador();
        cpu.setConsumo("65 W");

        PlacaDeVideo gpu = new PlacaDeVideo();
        gpu.setConsumo("150 W");

        FuenteDeAlimentacion fuente = new FuenteDeAlimentacion();
        fuente.setWattsReales("600W");

        ArmadoPc armado = new ArmadoPc();
        armado.setProcesador(cpu);
        armado.setPlacaDeVideo(gpu);

        boolean resultado = servicio.verificarCompatibilidadDeFuenteConWatsDelArmado(fuente, armado);

        assertTrue(resultado); // 600 > 215
    }

    @Test
    public void debeDevolverFalseCuandoLaFuenteTieneMenosWattsQueElArmado() {
        Procesador cpu = new Procesador();
        cpu.setConsumo("95 W");

        PlacaDeVideo gpu = new PlacaDeVideo();
        gpu.setConsumo("250 W");

        FuenteDeAlimentacion fuente = new FuenteDeAlimentacion();
        fuente.setWattsReales("300W");

        ArmadoPc armado = new ArmadoPc();
        armado.setProcesador(cpu);
        armado.setPlacaDeVideo(gpu);

        Boolean resultado = servicio.verificarCompatibilidadDeFuenteConWatsDelArmado(fuente, armado);

        assertFalse(resultado); // 300 < 345
    }

    @Test
    public void debeIncluirWatsDeMotherCoolerYGabinete() {
        Procesador cpu = new Procesador();
        cpu.setConsumo("65 W");

        Motherboard mother = new Motherboard();
        mother.setConsumo("45 W");

        CoolerCPU cooler = new CoolerCPU();
        cooler.setConsumo("20 W");

        Gabinete gabinete = new Gabinete(); // el gabinete suma 10 W hardcodeado

        FuenteDeAlimentacion fuente = new FuenteDeAlimentacion();
        fuente.setWattsReales("200W");

        ArmadoPc armado = new ArmadoPc();
        armado.setProcesador(cpu);
        armado.setMotherboard(mother);
        armado.setCoolerCPU(cooler);
        armado.setGabinete(gabinete);

        Boolean resultado = servicio.verificarCompatibilidadDeFuenteConWatsDelArmado(fuente, armado);

        assertTrue(resultado); // 200 > 65 + 45 + 20 + 10 = 140
    }

    @Test
    public void debeSumarCorrectamenteMemoriasYAlmacenamiento() {
        MemoriaRAM ram1 = new MemoriaRAM(); // cada una suma 5 W
        MemoriaRAM ram2 = new MemoriaRAM();

        Almacenamiento ssd = new Almacenamiento();
        ssd.setConsumo("10 W");

        Almacenamiento hdd = new Almacenamiento();
        hdd.setConsumo("20 W");

        FuenteDeAlimentacion fuente = new FuenteDeAlimentacion();
        fuente.setWattsReales("100W");

        ArmadoPc armado = new ArmadoPc();
        armado.setMemoriaRAM(List.of(ram1, ram2)); // 10W
        armado.setAlmacenamiento(List.of(ssd, hdd)); // 30W

        Boolean resultado = servicio.verificarCompatibilidadDeFuenteConWatsDelArmado(fuente, armado);

        assertTrue(resultado); // 100 > 10 + 30
    }

    @Test
    public void debeDevolverFalseSiFuenteEsNull() {
        ArmadoPc armado = new ArmadoPc();
        Boolean resultado = servicio.verificarCompatibilidadDeFuenteConWatsDelArmado(null, armado);
        assertFalse(resultado);
    }

    @Test
    public void debeDevolverFalseSiArmadoEsNull() {
        ArmadoPc armado = null;
        Boolean resultado = servicio.verificarCompatibilidadDeFuenteConWatsDelArmado(new FuenteDeAlimentacion(), armado);
        assertFalse(resultado);
    }

    @Test
    public void debeCalcularCorrectamenteLosWattsTotalesDelArmado() {
        Procesador cpu = new Procesador();
        cpu.setConsumo("65 W");

        PlacaDeVideo gpu = new PlacaDeVideo();
        gpu.setConsumo("150 W");

        CoolerCPU cooler = new CoolerCPU();
        cooler.setConsumo("20 W");

        Motherboard mother = new Motherboard();
        mother.setConsumo("40 W");

        Gabinete gabinete = new Gabinete(); // 10W hardcodeado

        Almacenamiento ssd = new Almacenamiento();
        ssd.setConsumo("10 W");
        Almacenamiento hdd = new Almacenamiento();
        hdd.setConsumo("15 W");

        MemoriaRAM ram1 = new MemoriaRAM(); // 5W hardcodeado
        MemoriaRAM ram2 = new MemoriaRAM(); // 5W hardcodeado

        ArmadoPc armado = new ArmadoPc();
        armado.setProcesador(cpu);         // 65
        armado.setPlacaDeVideo(gpu);       // 150
        armado.setCoolerCPU(cooler);       // 20
        armado.setMotherboard(mother);     // 40
        armado.setGabinete(gabinete);      // 10
        armado.setAlmacenamiento(List.of(ssd, hdd)); // 25
        armado.setMemoriaRAM(List.of(ram1, ram2));   // 10

        Integer watsTotales = servicio.obtenerWatsTotales(armado);

        assertEquals(320, watsTotales); // 65 + 150 + 20 + 40 + 10 + 25 + 10 = 320
    }
}