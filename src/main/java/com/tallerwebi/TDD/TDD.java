package com.tallerwebi.TDD;

public class TDD {

    public static String validarFortaleza(String contraseña) {
        if (contraseña.length() < 8) {
            if(contraseña.contains("@"))
                return "MEDIANA";
            else
            return "INVALIDA";
        } else {
           Integer largo = contraseña.length();
           Integer cantidadDeNumeros = contarNumeros(contraseña);
           Integer cantidadDeCarateresEspeciales = contarCaracteresEspeciales(contraseña)

           if(largo >= 8 && cantidadDeCarateresEspeciales >= 1 && cantidadDeNumeros >= 4)
                return "FUERTE";
           return "DEBIL";
        }
    }
}
