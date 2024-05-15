package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.calendario.TipoRendimiento;
import com.tallerwebi.dominio.calendario.ItemRendimiento;
import java.time.LocalDate;

public class DatosItemRendimiento {


    private LocalDate fecha;
    private TipoRendimiento tipoRendimiento;

    public DatosItemRendimiento(TipoRendimiento tipoRendimiento, LocalDate fecha) {
        this.tipoRendimiento = tipoRendimiento;
        this.fecha = fecha;
    }

    public DatosItemRendimiento(ItemRendimiento itemRendimiento) {
        this.tipoRendimiento = itemRendimiento.getTipoRendimiento();
        this.fecha = itemRendimiento.getFecha();
    }

    public DatosItemRendimiento() {

    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public TipoRendimiento getTipoRendimiento() {
        return tipoRendimiento;
    }

    public void setTipoRendimiento(TipoRendimiento tipoRendimiento) {
        this.tipoRendimiento = tipoRendimiento;
    }
}
