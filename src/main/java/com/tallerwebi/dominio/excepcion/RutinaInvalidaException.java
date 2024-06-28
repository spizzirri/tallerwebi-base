package com.tallerwebi.dominio.excepcion;

public class RutinaInvalidaException extends Exception {

    public RutinaInvalidaException(String s) {
        super(s);
    }
    public RutinaInvalidaException() {
        super("Los datos de la rutina son invalidos.");
    }
}
