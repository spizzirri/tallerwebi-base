package com.tallerwebi.dominio.entidades;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class MemoriaRAM extends Componente{

    /*Caracteristicas generales*/
    @Column(length = 100)
    private String capacidad;
    @Column(length = 100)
    private String velocidad;
    @Column(length = 100)
    private String tecnologiaRAM;
    @Column(length = 100)
    private String latencia;
    @Column(length = 100)
    private String voltaje;

    /*Coolers y disipadores*/
    private Boolean disipador;
    private Boolean disipadorAlto;

    public MemoriaRAM() {}

    public MemoriaRAM(Long id, String nombre, Double precio, Integer stock, String marca,
                      String capacidad, String velocidad, String tecnologiaRAM, String latencia, String voltaje,
                      Boolean disipador, Boolean disipadorAlto) {
        super(id, nombre, precio, stock, marca);

        this.capacidad = capacidad;
        this.velocidad = velocidad;
        this.tecnologiaRAM = tecnologiaRAM;
        this.latencia = latencia;
        this.voltaje = voltaje;

        this.disipador = disipador;
        this.disipadorAlto = disipadorAlto;
    }

    public String getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(String capacidad) {
        this.capacidad = capacidad;
    }

    public String getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(String velocidad) {
        this.velocidad = velocidad;
    }

    public String getTecnologiaRAM() {
        return tecnologiaRAM;
    }

    public void setTecnologiaRAM(String tecnologiaRAM) {
        this.tecnologiaRAM = tecnologiaRAM;
    }

    public String getLatencia() {
        return latencia;
    }

    public void setLatencia(String latencia) {
        this.latencia = latencia;
    }

    public String getVoltaje() {
        return voltaje;
    }

    public void setVoltaje(String voltaje) {
        this.voltaje = voltaje;
    }

    public Boolean getDisipador() {
        return disipador;
    }

    public void setDisipador(Boolean disipador) {
        this.disipador = disipador;
    }

    public Boolean getDisipadorAlto() {
        return disipadorAlto;
    }

    public void setDisipadorAlto(Boolean disipadorAlto) {
        this.disipadorAlto = disipadorAlto;
    }
}
