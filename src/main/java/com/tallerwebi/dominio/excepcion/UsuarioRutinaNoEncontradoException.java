package com.tallerwebi.dominio.excepcion;

public class UsuarioRutinaNoEncontradoException extends Exception {

    public UsuarioRutinaNoEncontradoException(String s) {
        super(s);
    }
    public UsuarioRutinaNoEncontradoException() {
        super("Los datos del ejercicio son invalidos.");
    }
}
