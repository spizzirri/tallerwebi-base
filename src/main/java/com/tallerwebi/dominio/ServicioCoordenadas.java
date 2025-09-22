package com.tallerwebi.dominio;


public class ServicioCoordenadas {
    public boolean validarLatitud(Double latitud) {
        if (latitud == null) {
            return false;
        }
        return latitud >= -90.0 && latitud <= 90.0;
    }
    public boolean validarLongitud(Double longitud) {
        if (longitud == null) {
            return false;
        }
        return longitud >= -180.0 && longitud <= 180.0;
    }
}
