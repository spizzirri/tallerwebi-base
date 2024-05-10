package com.tallerwebi.dominio.calendario;
import java.util.*;
public class ServicioCalendarioImpl implements ServicioCalendario{
    private List<DiaCalendario> calendario;

    public Integer getCantidadDias(){


        return 0;
    };

    public void ordenarDiasPorFechaCronologica() {

    }

    public void agregarDia(DiaCalendario diaCalendario) {
        if (diaCalendario == null) {
            throw new IllegalArgumentException("El parámetro 'diaCalendario' no puede ser null");
        }
        if (this.calendario.contains(diaCalendario)) {
            throw new IllegalArgumentException("La fecha del día ya existe en el calendario");
        }
        this.calendario.add(diaCalendario);
    }

}
