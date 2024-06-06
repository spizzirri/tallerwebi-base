package com.tallerwebi.dominio.objetivo;

public enum Objetivo {
    GANANCIA_MUSCULAR,
    DEFINICION,
    PERDIDA_DE_PESO;

    public String formatear() {
        return this.name().toLowerCase().replace('_', ' ');
    }

}
