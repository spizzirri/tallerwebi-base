package com.tallerwebi.dominio.calendario;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "itemrendimiento")
public class ItemRendimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "tipoRendimiento", nullable = false)
    private TipoRendimiento tipoRendimiento;

    public ItemRendimiento() {
    }

    public ItemRendimiento(TipoRendimiento tipoRendimiento) {
        this.fecha = LocalDate.now();
        this.tipoRendimiento = tipoRendimiento;
    }

    public ItemRendimiento(LocalDate fecha, TipoRendimiento tipoRendimiento) {
        this.fecha = fecha;
        this.tipoRendimiento = tipoRendimiento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemRendimiento that = (ItemRendimiento) o;
        return Objects.equals(id, that.id) && Objects.equals(fecha, that.fecha) && tipoRendimiento == that.tipoRendimiento;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fecha, tipoRendimiento);
    }

    @Override
    public String toString() {
        return "DiaCalendario{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", tipoRendimiento=" + tipoRendimiento +
                '}';
    }

    public TipoRendimiento getTipoRendimiento() {
        return tipoRendimiento;
    }

    public void setTipoRendimiento(TipoRendimiento tipoRendimiento) {
        this.tipoRendimiento = tipoRendimiento;
    }

}
