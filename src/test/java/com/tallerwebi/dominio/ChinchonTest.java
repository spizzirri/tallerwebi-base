package com.tallerwebi.dominio;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ChinchonTest {

    @Test
    public void debeDevolverCHINCHONCuandoTodasLasCartasSonConsecutivasYDelMismoPalo(){

        // GIVEN
        Carta carta1 = new Carta(1, Palo.ORO);
        Carta carta2 = new Carta(2, Palo.ORO);
        Carta carta3 = new Carta(3, Palo.ORO);
        Carta carta4 = new Carta(4, Palo.ORO);
        Carta carta5 = new Carta(5, Palo.ORO);
        Carta carta6 = new Carta(6, Palo.ORO);
        Carta carta7 = new Carta(7, Palo.ORO);

        List<Carta> mano = Arrays.asList(carta1, carta2, carta3, carta4, carta5, carta6, carta7);

        ServicioChinchon servicioChinchon = new ServicioChinchon();

        // WHEN
        boolean hayChinchon = servicioChinchon.hayChinchon(mano);

        // THEN
        assertThat(hayChinchon, is(true));

    }

    @Test
    public void debeDevolverSINCHINCHONCuandoTodasLasCartasSonDeDistintoPalo(){

        // GIVEN
        Carta carta1 = new Carta(1, Palo.ORO);
        Carta carta2 = new Carta(2, Palo.ORO);
        Carta carta3 = new Carta(3, Palo.ORO);
        Carta carta4 = new Carta(4, Palo.ORO);
        Carta carta5 = new Carta(5, Palo.ORO);
        Carta carta6 = new Carta(6, Palo.ORO);
        Carta carta7 = new Carta(7, Palo.BASTO);

        List<Carta> mano = Arrays.asList(carta1, carta2, carta3, carta4, carta5, carta6, carta7);

        ServicioChinchon servicioChinchon = new ServicioChinchon();

        // WHEN
        boolean hayChinchon = servicioChinchon.hayChinchon(mano);

        // THEN
        assertThat(hayChinchon, is(false));

    }

    @Test
    public void debeDevolverSINCHINCHONCuandoLosNumerosDeLasCartasNoSonConsecutivos(){

        // GIVEN
        Carta carta1 = new Carta(1, Palo.ORO);
        Carta carta2 = new Carta(2, Palo.ORO);
        Carta carta3 = new Carta(3, Palo.ORO);
        Carta carta4 = new Carta(4, Palo.ORO);
        Carta carta5 = new Carta(10, Palo.ORO);
        Carta carta6 = new Carta(6, Palo.ORO);
        Carta carta7 = new Carta(7, Palo.ORO);

        List<Carta> mano = Arrays.asList(carta1, carta2, carta3, carta4, carta5, carta6, carta7);

        ServicioChinchon servicioChinchon = new ServicioChinchon();

        // WHEN
        boolean hayChinchon = servicioChinchon.hayChinchon(mano);

        // THEN
        assertThat(hayChinchon, is(false));

    }
}
