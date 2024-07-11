package com.tallerwebi.dominio.excepcion;

public class ListaDeRutinasVaciaException extends Exception {

    public ListaDeRutinasVaciaException(String s) {
        super(s);
    }
    public ListaDeRutinasVaciaException() {
        super("La lista de rutinas está vacia");
    }
}
