package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.ReseniaInvalida;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class ServicioReseniaImplTest {

    private ServicioReseniaImpl servicioReseniaImpl = new ServicioReseniaImpl();
    private Resenia resenia;

    @Test
    public void deberiaDevolverFalsoSiLaCalificacionEsMenosQue1(){
        resenia = new Resenia(new Long(1), (short) 0,"Test");
        try {
            assertEquals(servicioReseniaImpl.esReseniaValida(resenia),false);
        } catch (ReseniaInvalida e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void deberiaDevolverFalsoSiLaCalificacionEsMayorQue5(){
        resenia = new Resenia(new Long(1), (short) 6,"Test");
        try{
            assertEquals(servicioReseniaImpl.esReseniaValida(resenia),false);
        } catch (ReseniaInvalida e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void deberiaDevolverTrueSiLaCalificacionEsIgualA5(){
        resenia = new Resenia(new Long(1), (short) 5,"Test");
        try{
            assertEquals(servicioReseniaImpl.esReseniaValida(resenia),true);
        }catch (ReseniaInvalida e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void deberiaDevolverTrueSiLaCalificacionEsIgualA1(){
        resenia = new Resenia(new Long(1), (short) 1,"Test");
        try{
            assertEquals(servicioReseniaImpl.esReseniaValida(resenia),true);
        } catch (ReseniaInvalida e) {
            throw new RuntimeException(e);
        }
    }
}
