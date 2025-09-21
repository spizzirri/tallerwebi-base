package com.tallerwebi.dominio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CategoriasTest {


    @Test
    void funcionanLosGetterYSettersDeCategorias() {
        Categorias categoria = new Categorias();

        categoria.setId(1L);
        categoria.setNombre("Electrónica");
        categoria.setNombreEnUrl("electronica");

        assertEquals(1L, categoria.getId());
        assertEquals("Electrónica", categoria.getNombre());
        assertEquals("electronica", categoria.getNombreEnUrl());
    }

}
