package com.tallerwebi.dominio.excepcion;

public class NoPudoGuardarPerfilException extends RuntimeException{

    public NoPudoGuardarPerfilException(String message) {
        super(message);
    }

}
