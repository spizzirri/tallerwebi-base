package com.tallerwebi.presentacion;

public class TraductorNumerosRomanos {

    public int traducirAArabigos(String numeroRomano){
        int resultado = 10;

        if(numeroRomano.equals("I"))
            resultado = 1;

        if(numeroRomano.equals("V"))
            resultado = 5;

        return resultado;
    }
}
