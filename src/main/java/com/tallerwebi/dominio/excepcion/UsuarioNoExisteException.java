package com.tallerwebi.dominio.excepcion;

public class UsuarioNoExisteException extends Exception {
    public UsuarioNoExisteException(String s) {
        super(s);
    }
    public UsuarioNoExisteException() {
        super("El usuario no existe.");
    }
}
