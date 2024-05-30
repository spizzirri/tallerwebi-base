package com.tallerwebi.dominio.excepcion;

public class DiferenciaDeObjetivosExcepcion extends Exception {

    public DiferenciaDeObjetivosExcepcion() {
        super("Se encontraron diferencias en los objetivos.");
    }
    public DiferenciaDeObjetivosExcepcion(String s) {
        super(s);
    }
}
