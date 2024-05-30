package com.tallerwebi.dominio.excepcion;

public class EjercicioNoEncontradoException extends Exception {
    public EjercicioNoEncontradoException(String s) {
        super(s);
    }
    public EjercicioNoEncontradoException() {
        super("No se encontró el ejercicio.");
    }
}
