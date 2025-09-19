package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import com.tallerwebi.dominio.excepcion.NoCumpleRequisitos;

public class ServicioAutenticacionImplTest {

    @Test
    public void deberiaDevolverFalsoSiElUsuarioEsMenorDe18() throws NoCumpleRequisitos {

        Usuario usuarioMenorA18 = new Usuario("nombreEjemplo", "emailEjemplo", "passwordEjemplo", 17, "rolEjemplo");

        ServicioAutenticacion servicio = new ServicioAutenticacionImpl();
        Boolean esValido = servicio.esUsuarioValido(usuarioMenorA18);
        assertThat(esValido, is(false));
    }

    @Test
    public void deberiaDevolverVerdaderoSiElUsuarioTiene18() throws NoCumpleRequisitos {

        Usuario usuarioCon18 = new Usuario("nombreEjemplo", "emailEjemplo", "passwordEjemplo", 18, "rolEjemplo");

        ServicioAutenticacion servicio = new ServicioAutenticacionImpl();
        Boolean esValido = servicio.esUsuarioValido(usuarioCon18);
        assertThat(esValido, is(true));
    }

    @Test
    public void deberiaDevolverFalsoSiElUsuarioEsMayorDe40() throws NoCumpleRequisitos{
        
        Usuario usuarioMenorA41 = new Usuario("nombreEjemplo", "emailEjemplo", "passwordEjemplo", 41, "rolEjemplo");

        ServicioAutenticacion servicio = new ServicioAutenticacionImpl();
        Boolean esValido = servicio.esUsuarioValido(usuarioMenorA41);
        assertThat(esValido, is(false));
    }

}
