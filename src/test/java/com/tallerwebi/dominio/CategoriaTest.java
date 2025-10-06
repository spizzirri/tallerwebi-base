package com.tallerwebi.dominio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CategoriaTest {


    @Test
    void funcionanLosGetterYSettersDeCategorias() {
        Categoria categoria = new Categoria();

        categoria.setId(1L);
        categoria.setNombre("Electrónica");
        categoria.setNombreEnUrl("electronica");

        assertEquals(1L, categoria.getId());
        assertEquals("Electrónica", categoria.getNombre());
        assertEquals("electronica", categoria.getNombreEnUrl());
    }

}
