package com.tallerwebi.dominio.excepcion;

public class DiferenciaDeObjetivoExcepcion extends Exception{

    public DiferenciaDeObjetivoExcepcion(String s) {
        super("Se encontraron diferencias en los objetivos.");
    }
}
