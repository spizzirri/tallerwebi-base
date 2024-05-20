package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.calendario.TipoRendimiento;
import com.tallerwebi.dominio.calendario.ItemRendimiento;
import java.time.LocalDate;

public class DatosItemRendimiento {


    private LocalDate fecha;
    private TipoRendimiento tipoRendimiento;

    public DatosItemRendimiento(LocalDate fecha, TipoRendimiento tipoRendimiento) {
        this.fecha = fecha;
        this.tipoRendimiento = tipoRendimiento;
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
