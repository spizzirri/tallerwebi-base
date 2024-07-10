package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.calendario.TipoRendimiento;
import com.tallerwebi.dominio.calendario.ItemRendimiento;
import java.time.LocalDate;

public class DatosItemRendimiento {


    private LocalDate fecha;
    private TipoRendimiento tipoRendimiento;
    private String diaNombre;

    public DatosItemRendimiento(ItemRendimiento itemRendimiento) {
        this.tipoRendimiento = itemRendimiento.getTipoRendimiento();
        this.fecha = itemRendimiento.getFecha();
        this.diaNombre = itemRendimiento.getDiaNombre();
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

    public String getDiaNombre() {
        return diaNombre;
    }

    public void setDiaNombre(String diaNombre) {
        this.diaNombre = diaNombre;
    }

}
