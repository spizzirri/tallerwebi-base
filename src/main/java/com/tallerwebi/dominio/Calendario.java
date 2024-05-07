package com.tallerwebi.dominio;

import org.springframework.core.env.SystemEnvironmentPropertySource;

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

    public void ordenarDiasPorFechaCercana() {
        // Utilizar un comparador personalizado para ordenar los días por fecha
        Comparator<DiaCalendario> comparadorFecha = Comparator.comparingInt(DiaCalendario::getAno)
                .thenComparingInt(DiaCalendario::getMes)
                .thenComparingInt(DiaCalendario::getDia);

        // Ordenar la lista de días utilizando el comparador
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
        return Objects.equals(dias, that.dias);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(dias);
    }

    @Override
    public String toString() {
        return "Calendario{" +
                " " + dias +
                "}";
    }
}
