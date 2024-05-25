package com.tallerwebi.dominio.excepcion;

public class UsuarioNoTieneLaRutinaBuscadaException extends Exception{
    public UsuarioNoTieneLaRutinaBuscadaException() {
        super("El usuario no tiene asignada la rutina buscada.");
    }
}
