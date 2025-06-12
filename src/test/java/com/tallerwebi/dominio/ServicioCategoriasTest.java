package com.tallerwebi.dominio;

import com.tallerwebi.dominio.ServicioCategorias;
import com.tallerwebi.presentacion.CategoriaDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ServicioCategoriasTest {

    private ServicioCategorias serviceCategorias;

    @BeforeEach
    public void init() {
        serviceCategorias = new ServicioCategorias();
    }

    @Test
    public void cuandoPidoUnaListaDeCategoriasDebeRetornarUnaListaDe9CategoriasYNoEstarVacia() {
        List<CategoriaDto> categorias = serviceCategorias.getCategorias();

        assertThat(categorias, is(not(empty())));
        assertThat(categorias.size(), is(9));
    }
    @Test
    public void cuandoPidoLaPrimerCategoriaMeDevuelveProcesador() {
        List<CategoriaDto> categorias = serviceCategorias.getCategorias();

        CategoriaDto primera = categorias.get(0);
        assertThat(primera.getNombre(), equalTo("Procesador"));
    }
}