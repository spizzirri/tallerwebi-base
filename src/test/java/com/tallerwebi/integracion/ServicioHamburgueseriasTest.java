package com.tallerwebi.integracion;

import com.tallerwebi.dominio.ServicioHamburgueserias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ServicioHamburgueseriasTest {
    
    private ServicioHamburgueserias servicioHamburgueserias;
    
    @BeforeEach
    public void init() {
        servicioHamburgueserias = new ServicioHamburgueserias();
    }
    
    @Test
    public void validarLatitudConValorNuloDeberiaRetornarFalse() {
        Double latitud = null;
        boolean resultado = servicioHamburgueserias.validarLatitud(latitud);
        assertThat(resultado, is(false));
    }
    
    @Test
    public void validarLatitudConValorValidoPositivoDeberiaRetornarTrue() {
        Double latitud = 45.0;
        boolean resultado = servicioHamburgueserias.validarLatitud(latitud);
        assertThat(resultado, is(true));
    }
    
    @Test
    public void validarLatitudConValorValidoNegativoDeberiaRetornarTrue() {
        Double latitud = -45.0;
        boolean resultado = servicioHamburgueserias.validarLatitud(latitud);
        assertThat(resultado, is(true));
    }
    
    @Test
    public void validarLatitudConValorLimiteInferiorDeberiaRetornarTrue() {
        Double latitud = -90.0;
        boolean resultado = servicioHamburgueserias.validarLatitud(latitud);
        assertThat(resultado, is(true));
    }
    
    @Test
    public void validarLatitudConValorLimiteSuperiorDeberiaRetornarTrue() {
        Double latitud = 90.0;
        boolean resultado = servicioHamburgueserias.validarLatitud(latitud);
        assertThat(resultado, is(true));
    }
    
    @Test
    public void validarLatitudConValorMenorAlLimiteInferiorDeberiaRetornarFalse() {
        Double latitud = -90.1;
        boolean resultado = servicioHamburgueserias.validarLatitud(latitud);
        assertThat(resultado, is(false));
    }
    
    @Test
    public void validarLatitudConValorMayorAlLimiteSuperiorDeberiaRetornarFalse() {
        Double latitud = 90.1;
        boolean resultado = servicioHamburgueserias.validarLatitud(latitud);
        assertThat(resultado, is(false));
    }
    
    @Test
    public void validarLatitudConCeroDeberiaRetornarTrue() {
        Double latitud = 0.0;
        boolean resultado = servicioHamburgueserias.validarLatitud(latitud);
        assertThat(resultado, is(true));
    }
    
    // Tests para validarLongitud
    
    @Test
    public void validarLongitudConValorNuloDeberiaRetornarFalse() {
        Double longitud = null;
        boolean resultado = servicioHamburgueserias.validarLongitud(longitud);
        assertThat(resultado, is(false));
    }
    
    @Test
    public void validarLongitudConValorValidoPositivoDeberiaRetornarTrue() {
        Double longitud = 90.0;
        boolean resultado = servicioHamburgueserias.validarLongitud(longitud);
        assertThat(resultado, is(true));
    }
    
    @Test
    public void validarLongitudConValorValidoNegativoDeberiaRetornarTrue() {
        Double longitud = -90.0;
        boolean resultado = servicioHamburgueserias.validarLongitud(longitud);
        assertThat(resultado, is(true));
    }
    
    @Test
    public void validarLongitudConValorLimiteInferiorDeberiaRetornarTrue() {
        Double longitud = -180.0;
        boolean resultado = servicioHamburgueserias.validarLongitud(longitud);
        assertThat(resultado, is(true));
    }
    
    @Test
    public void validarLongitudConValorLimiteSuperiorDeberiaRetornarTrue() {
        Double longitud = 180.0;
        boolean resultado = servicioHamburgueserias.validarLongitud(longitud);
        assertThat(resultado, is(true));
    }
    
    @Test
    public void validarLongitudConValorMenorAlLimiteInferiorDeberiaRetornarFalse() {
        Double longitud = -180.1;
        boolean resultado = servicioHamburgueserias.validarLongitud(longitud);
        assertThat(resultado, is(false));
    }
    
    @Test
    public void validarLongitudConValorMayorAlLimiteSuperiorDeberiaRetornarFalse() {
        Double longitud = 180.1;
        boolean resultado = servicioHamburgueserias.validarLongitud(longitud);
        assertThat(resultado, is(false));
    }
    
    @Test
    public void validarLongitudConCeroDeberiaRetornarTrue() {
        Double longitud = 0.0;
        boolean resultado = servicioHamburgueserias.validarLongitud(longitud);
        assertThat(resultado, is(true));
    }
}
