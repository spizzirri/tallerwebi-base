package com.tallerwebi.dominio.excepcion;

public class CoordenadaInvalida extends RuntimeException {
    public CoordenadaInvalida(String mensaje) {
        super(mensaje);
    }
}
