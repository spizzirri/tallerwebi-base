package com.tallerwebi.dominio.excepcion;

public class RutinaYaExisteException extends Exception {

    public RutinaYaExisteException(String s) {
        super(s);
    }
    public RutinaYaExisteException() {
        super("La rutina ya existe");
    }
}
