package com.tallerwebi.dominio.excepcion;

public class UsuarioNoEsInstructorException extends Exception{

    public UsuarioNoEsInstructorException() {
        super("El usuario no tiene los permisos necesarios");
    }

}
