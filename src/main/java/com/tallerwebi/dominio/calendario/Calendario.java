package com.tallerwebi.dominio.calendario;

import java.util.*;

public class Calendario {

    private Integer anoActual = 2024;
    private List<DiaCalendario> dias;

    public Calendario() {
        this.dias = new ArrayList<>();
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
