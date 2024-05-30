package com.tallerwebi.dominio.excepcion;

public class RutinaNoEncontradaException extends Exception{
    public RutinaNoEncontradaException(String s) {
        super(s);
    }
    public RutinaNoEncontradaException() {
        super("No se encontró la rutina.");
    }
}
