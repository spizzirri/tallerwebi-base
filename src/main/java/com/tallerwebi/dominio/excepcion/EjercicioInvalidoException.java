package com.tallerwebi.dominio.excepcion;

public class EjercicioInvalidoException extends Exception {

    public EjercicioInvalidoException(String s) {
        super(s);
    }
    public EjercicioInvalidoException() {
        super("Los datos del ejercicio son invalidos.");
    }
}
