package com.tallerwebi.dominio.excepcion;

public class NoCumpleRequisitos extends Exception {
    public NoCumpleRequisitos() {
        super("El usuario no cumple con los requisitos establecidos de admision.");
    }
}
