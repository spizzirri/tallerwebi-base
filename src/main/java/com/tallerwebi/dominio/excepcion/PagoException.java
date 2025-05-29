package com.tallerwebi.dominio.excepcion;

public class PagoException extends Exception {

    public PagoException(String mensaje) {
        super(mensaje);
    }

    public PagoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}