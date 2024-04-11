package com.tallerwebi.dominio;

import java.util.List;

public class ServicioChinchon {
    public boolean hayChinchon(List<Carta> mano){
        Carta carta1 = mano.get(0);

        for (Carta cartaActual: mano) {
            if(!carta1.getPalo().equals(cartaActual.getPalo())){
                return false;
            }
        }
        return true;
    }
}
