package com.tallerwebi.dominio.excepcion;

public class EjercicioYaExisteException extends Exception {

    public EjercicioYaExisteException(String s) {
        super(s);
    }
    public EjercicioYaExisteException() {
        super("El ejercicio ya existe");
    }
}
