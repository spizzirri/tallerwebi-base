package com.tallerwebi.TDD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import org.junit.jupiter.api.Test;

public class TDDTest {

    @Test
    public void debeDevolverINVALIDA_cuandoLaContraseñaTieneMenosde8Caracteres() {

        String contraseñaDe3Caracteres = "abc";
        String fortalezaDeLaContraseña = TDD.validarFortaleza(contraseñaDe3Caracteres);

        assertThat(fortalezaDeLaContraseña, equalToIgnoringCase("INVALIDA"));
    }

    @Test
    public void debeDevolverDEBIL_cuandoLaContraseñaTiene8Caracteres() {

        String contraseñaDe8Caracteres = "abcdefgh";
        String fortalezaDeLaContraseña = TDD.validarFortaleza(contraseñaDe8Caracteres);

        assertThat(fortalezaDeLaContraseña, equalToIgnoringCase("DEBIL"));
    }

    @Test
    public void debeDevolverMEDIANA_cuandoLaContraseñaTiene4CaracteresYUnArrobaEspecial() {

        String contraseñaDe8Caracteres = "abc@";
        String fortalezaDeLaContraseña = TDD.validarFortaleza(contraseñaDe8Caracteres);

        assertThat(fortalezaDeLaContraseña, equalToIgnoringCase("MEDIANA"));
    }

    @Test
    public void debeDevolverFUERTE_cuandoLaContraseñaTiene4NumerosUnArrobay3Letras(){
        String contraseñaDe8Caracteres = "1234@abc";
        String fortalezaDeLaContraseña = TDD.validarFortaleza(contraseñaDe8Caracteres);

        assertThat(fortalezaDeLaContraseña, equalToIgnoringCase("FUERTE"));
    }
}
