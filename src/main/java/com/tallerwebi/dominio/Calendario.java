package com.tallerwebi.dominio;

import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.time.LocalDate;
import java.time.Year;
import java.util.*;

public class Calendario {

    private Integer anoActual = 2024;
    private List<DiaCalendario> dias;

    public Calendario() {
        this.dias = new ArrayList<>();
        // Lógica para crear los días del calendario
    }

    public void agregarDia(DiaCalendario dia){
        boolean existeDia = dias.stream()
                .anyMatch(d -> d.equals(dia));
        if (!existeDia) {
            dias.add(dia);
        }
    }

    public Integer getCantidadDias() {
        return dias.size();
    }

    public void ordenarDiasPorFechaCronologica() {
        Comparator<DiaCalendario> comparadorFecha = Comparator.comparingInt(DiaCalendario::getAno)
                .thenComparingInt(DiaCalendario::getMes)
                .thenComparingInt(DiaCalendario::getDia)
                .reversed(); // Invertir el orden para que las fechas más recientes estén primero

        // Ordenar la lista de días utilizando el nuevo comparador
        Collections.sort(dias, comparadorFecha);
    }



    public List<DiaCalendario> getDias() {
        return dias;
    }

    public void setDias(List<DiaCalendario> dias) {
        this.dias = dias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Calendario that = (Calendario) o;
        return Objects.equals(anoActual, that.anoActual) && Objects.equals(dias, that.dias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anoActual, dias);
    }

    @Override
    public String toString() {
        return "Calendario{" +
                "dias=" + dias +
                '}';
    }
}
