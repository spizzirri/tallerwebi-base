package com.tallerwebi.dominio.AliasDeRetiro;

import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Hijos.RepositorioHijo;
import com.tallerwebi.dominio.Hijos.ServicioHijoImpl;
import com.tallerwebi.dominio.Usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ServicioGeneradorAliasTest {

    private ServicioGeneradorAlias servicioGeneradorAlias;
    private RepositorioHijo repositorioHijoMock;

    @BeforeEach
    public void init() {
        repositorioHijoMock = Mockito.mock(RepositorioHijo.class);
        servicioGeneradorAlias = new ServicioGeneradorAliasImpl(repositorioHijoMock);
    }

    @Test
    void deberiaGenerarTodasLasCombinacionesPosibles() {

        List<String> aliases =
                servicioGeneradorAlias.obtenerTodasLasCombinaciones();

        assertEquals(125, aliases.size());
    }

    @Test
    void deberiaGenerarUnAliasDisponible() {

        when(repositorioHijoMock.existeAlias(anyString()))
                .thenReturn(false);

        String alias =
                servicioGeneradorAlias.generarAliasDisponible();

        assertNotNull(alias);
    }


    @Test
    void deberiaRetornarNullCuandoNoHayAliasesDisponibles() {

        when(repositorioHijoMock.existeAlias(anyString()))
                .thenReturn(true);

        String alias =
                servicioGeneradorAlias.generarAliasDisponible();

        assertNull(alias);
    }


}
