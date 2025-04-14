package com.tallerwebi.TDD;

import org.hamcrest.core.IsEqual;
import org.hamcrest.text.IsEqualIgnoringCase;
import org.junit.jupiter.api.Test;
import com.tallerwebi.TDD.ValidadorDeContrasenia;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;

public class ValidadorDeContraseniaTest {
    /**
     * DeberiaDevolver___________Cuando_______
     * DeberiaRetornar___________Cuando_______
     *
     *
     */

    @Test
    public void deberiaDevolverINVALIDOCuandoLaContraseniaTieneUnSoloCaracter(){
        ValidadorDeContrasenia validador = new ValidadorDeContrasenia("1");
        String fortaleza = validador.evaluarFortaleza();

        assertThat(fortaleza, equalToIgnoringCase("INVALIDO"));
    }

    @Test
    public void deberiaDevolverDEBILCuandoLaContraseniaTiene8Caracteres(){
        ValidadorDeContrasenia validador = new ValidadorDeContrasenia("damian12");
        String fortaleza = validador.evaluarFortaleza();

        assertThat(fortaleza, equalToIgnoringCase("DEBIL"));
    }

    @Test
    public void deberiaDevolverMEDIANACuandoLaContraseniaEsDami3n12(){
        ValidadorDeContrasenia validador = new ValidadorDeContrasenia("dami3n12");
        String fortaleza = validador.evaluarFortaleza();

        assertThat(fortaleza, equalToIgnoringCase("DEBIL"));
    }

    @Test
    public void deberiaDevolverMEDIANACuandoLaContraseniaEsDam33n12(){
        ValidadorDeContrasenia validador = new ValidadorDeContrasenia("dam33n12");
        String fortaleza = validador.evaluarFortaleza();

        assertThat(fortaleza, equalToIgnoringCase("MEDIANA"));
    }
}
