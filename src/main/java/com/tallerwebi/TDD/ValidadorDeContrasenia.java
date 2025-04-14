package com.tallerwebi.TDD;

/***
 * Debe validar si la fortaleza de la contraseña es DEBIL, MEDIANA o FUERTE
 * 1. Debo contruir un metodo que valide la contraseña en base a ciertas reglas.
 * ¿Cuales son las reglas?
 *
 * FUERTE: al menos 4 numeros y una letra ñ, o un $
 * MEDIANA: al menos 4 numeros o una letra ñ o un $
 * DEBIL: que tenga al menos 8 caracteres
 * INVALIDO: menos de 8 caracteres
 */

public class ValidadorDeContrasenia {

    private String contrasenia;

    public ValidadorDeContrasenia(String contrasenia){
        this.contrasenia = contrasenia;
    }

    public String evaluarFortaleza(){
        if(this.contrasenia.length() >= 8){
            int cantidadDeNumeros = 0;
            for(int i=0; i<this.contrasenia.length(); i++){
                if(Character.isDigit(this.contrasenia.charAt(i))){
                    cantidadDeNumeros++;
                }
            }
            if(cantidadDeNumeros >= 4)
                return "MEDIANA";
            return "DEBIL";
        }
        return "INVALIDO";
    }

}
