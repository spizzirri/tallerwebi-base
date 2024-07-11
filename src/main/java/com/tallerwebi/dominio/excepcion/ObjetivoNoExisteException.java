package com.tallerwebi.dominio.excepcion;

public class ObjetivoNoExisteException extends Exception {
    public ObjetivoNoExisteException(String s) {
        super(s);
    }
    public ObjetivoNoExisteException() {
        super("El objetivo no existe.");
    }
}
