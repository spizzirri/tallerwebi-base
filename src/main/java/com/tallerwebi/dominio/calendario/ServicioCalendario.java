package com.tallerwebi.dominio.calendario;

import java.util.Collections;
import java.util.Comparator;

public interface ServicioCalendario {

    public Integer getCantidadDias();
    public void ordenarDiasPorFechaCronologica();
    public void agregarDia(DiaCalendario dia);










//    {
//        Comparator<DiaCalendario> comparadorFecha = Comparator.comparingInt(DiaCalendario::getAno)
//                .thenComparingInt(DiaCalendario::getMes)
//                .thenComparingInt(DiaCalendario::getDia)
//                .reversed(); // Invertir el orden para que las fechas más recientes estén primero
//
//        // Ordenar la lista de días utilizando el nuevo comparador
//        Collections.sort(dias, comparadorFecha);
//    }


}
