package com.tallerwebi.dominio.excepcion;

public class UsuarioExistente extends Exception {

    private String mensaje;
    public UsuarioExistente(String mensaje) { super (mensaje); }
}

