package com.tallerwebi.dominio.entidades;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Almacenamiento extends Componente {

    /*Caracteristicas generales*/
    @Column(length = 100)
    private String capacidad;
    @Column(length = 100)
    private String tipoDeConexion; // SATA - M2
    @Column(length = 100)
    private String consumo;
    @Column(length = 100)
    private String tipoDeDisco; // Mecanico - Solido

    /*Rendimiento*/
    @Column(length = 100)
    private String memoriaCache;
    @Column(length = 100)
    private String velocidadLecturaSecuencial;
    @Column(length = 100)
    private String velocidadEscrituraSecuencial;

    public Almacenamiento() {}

    public Almacenamiento(Long id, String nombre, Double precio, Integer stock, String marca,
                          String capacidad, String tipoDeConexion, String consumo, String tipoDeDisco,
                          String memoriaCache, String velocidadLecturaSecuencial, String velocidadEscrituraSecuencial) {
        super(id, nombre, precio, stock, marca);

        this.capacidad = capacidad;
        this.tipoDeConexion = tipoDeConexion;
        this.consumo = consumo;
        this.tipoDeDisco = tipoDeDisco;

        this.memoriaCache = memoriaCache;
        this.velocidadLecturaSecuencial = velocidadLecturaSecuencial;
        this.velocidadEscrituraSecuencial = velocidadEscrituraSecuencial;
    }

    public String getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(String capacidad) {
        this.capacidad = capacidad;
    }

    public String getTipoDeConexion() {
        return tipoDeConexion;
    }

    public void setTipoDeConexion(String tipoDeConexion) {
        this.tipoDeConexion = tipoDeConexion;
    }

    public String getConsumo() {
        return consumo;
    }

    public void setConsumo(String consumo) {
        this.consumo = consumo;
    }

    public String getTipoDeDisco() {
        return tipoDeDisco;
    }

    public void setTipoDeDisco(String tipoDeDisco) {
        this.tipoDeDisco = tipoDeDisco;
    }

    public String getMemoriaCache() {
        return memoriaCache;
    }

    public void setMemoriaCache(String memoriaCache) {
        this.memoriaCache = memoriaCache;
    }

    public String getVelocidadLecturaSecuencial() {
        return velocidadLecturaSecuencial;
    }

    public void setVelocidadLecturaSecuencial(String velocidadLecturaSecuencial) {
        this.velocidadLecturaSecuencial = velocidadLecturaSecuencial;
    }

    public String getVelocidadEscrituraSecuencial() {
        return velocidadEscrituraSecuencial;
    }

    public void setVelocidadEscrituraSecuencial(String velocidadEscrituraSecuencial) {
        this.velocidadEscrituraSecuencial = velocidadEscrituraSecuencial;
    }
}
