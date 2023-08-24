package com.tallerwebi.presentacion;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;

public class TraductorNumerosRomanosTest {

    @Test
    public void debeDevolverUnoCuandoReciveUnaI(){

        TraductorNumerosRomanos traductorNumerosRomanos = new TraductorNumerosRomanos();

        int numeroArabigo = traductorNumerosRomanos.traducirAArabigos("I");

        assertThat(numeroArabigo, comparesEqualTo(1));
    }

    @Test
    public void debeDevolverDiezCuandoReciveUnaX(){

        TraductorNumerosRomanos traductorNumerosRomanos = new TraductorNumerosRomanos();

        int numeroArabigo = traductorNumerosRomanos.traducirAArabigos("X");

        assertThat(numeroArabigo, comparesEqualTo(10));
    }

    @Test
    public void debeDevolverCincoCuandoReciveUnaV(){

        TraductorNumerosRomanos traductorNumerosRomanos = new TraductorNumerosRomanos();

        int numeroArabigo = traductorNumerosRomanos.traducirAArabigos("V");

        assertThat(numeroArabigo, comparesEqualTo(5));
    }
}
