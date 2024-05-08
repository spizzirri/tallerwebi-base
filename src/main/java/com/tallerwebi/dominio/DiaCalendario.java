package com.tallerwebi.dominio;

import java.util.Objects;

public class DiaCalendario {
    private Integer id;
    private Integer dia;
    private Integer mes;
    private Integer ano;
    private Rendimiento rendimiento;

    public DiaCalendario(Integer id, Integer dia, Integer mes, Integer ano, Rendimiento rendimiento) {
        this.id = id;
        this.dia = dia;
        this.mes = mes;
        this.ano = ano;
        this.rendimiento = Rendimiento.DESCANSO;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
        return Objects.equals(id, that.id) && Objects.equals(dia, that.dia) && Objects.equals(mes, that.mes) && Objects.equals(ano, that.ano) && rendimiento == that.rendimiento;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dia, mes, ano, rendimiento);
    }

    @Override
    public String toString() {
        return "DiaCalendario{" +
                "id=" + id +
                ", dia=" + dia +
                ", mes=" + mes +
                ", ano=" + ano +
                ", rendimiento=" + rendimiento +
                '}';
    }

    public Rendimiento getRendimiento() {
        return rendimiento;
    }

    public void setRendimiento(Rendimiento rendimiento) {
        this.rendimiento = rendimiento;
    }

}
