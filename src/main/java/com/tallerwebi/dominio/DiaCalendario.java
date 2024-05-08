package com.tallerwebi.dominio;

import java.util.Objects;

public class DiaCalendario {

    private Integer dia;
    private Integer mes;
    private Integer ano;
    private Rendimiento rendimiento;

    public DiaCalendario(Integer dia, Integer ano, Integer mes, Rendimiento rendimiento) {
        this.dia = dia;
        this.ano = ano;
        this.mes = mes;
        this.rendimiento = Rendimiento.DESCANSO; // Valor predeterminado
    }

    public Integer getDia() {
        return dia;
    }

    public void setDia(Integer dia) {
        this.dia = dia;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiaCalendario that = (DiaCalendario) o;
        return Objects.equals(dia, that.dia) && Objects.equals(mes, that.mes) && Objects.equals(ano, that.ano);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dia, mes, ano);
    }

    @Override
    public String toString() {
        return "DiaCalendario{" +
                "dia=" + dia +
                ", mes=" + mes +
                ", ano=" + ano +
                '}';
    }

    public Rendimiento getRendimiento() {
        return rendimiento;
    }

    public void setRendimiento(Rendimiento rendimiento) {
        this.rendimiento = rendimiento;
    }

}
