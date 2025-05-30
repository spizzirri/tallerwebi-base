package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tallerwebi.dominio.implementaciones.ServicioCartaImpl;
import com.tallerwebi.presentacion.dtos.CartaDto;

public class ServicioCartaTest {

    private RepositorioCarta repositorioCarta;
    private ServicioCarta servicioCarta;
    
    @BeforeEach
    public void init(){
        this.repositorioCarta = mock(RepositorioCarta.class);
        this.servicioCarta = new ServicioCartaImpl(this.repositorioCarta);
    }

    @Test
    public void cuandoCreoUnaCartaCompletaEntoncesObtengoUnResultadoPositivo(){

        when(repositorioCarta.crear(any())).thenReturn(true);

        Boolean creado = this.servicioCarta.crear(mock(CartaDto.class));

        assertThat(creado, is(true));
    }

}
