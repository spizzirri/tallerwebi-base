package com.tallerwebi.dominio.excepcion;

public class EjercicioNoExisteEnRutinaException extends Exception{
    public EjercicioNoExisteEnRutinaException() {
       super("El ejercicio no existe en esta rutina");
    }
}
