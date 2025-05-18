package com.tallerwebi.dominio;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Base Stats
    private String nombre;
    private Integer nivel = 1;
    private Long dinero = 0L;
    // Combat Stats
    private Integer vida = 200;
    private Integer energia = 100;
    private Integer magia = 100;
    // Attributes
    private Integer fuerza = 5;
    private Integer agilidad = 5;
    private Integer inteligencia = 5;

    @OneToMany
    private List<ObjetoInventario> objetos = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getNivel() {
        return nivel;
    }

    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }

    public Long getDinero() {
        return dinero;
    }

    public void setDinero(Long dinero) {
        this.dinero = dinero;
    }

    public Integer getVida() {
        return vida;
    }

    public void setVida(Integer vida) {
        this.vida = vida;
    }

    public Integer getEnergia() {
        return energia;
    }

    public void setEnergia(Integer energia) {
        this.energia = energia;
    }

    public Integer getMagia() {
        return magia;
    }

    public void setMagia(Integer magia) {
        this.magia = magia;
    }

    public Integer getFuerza() {
        return fuerza;
    }

    public void setFuerza(Integer fuerza) {
        this.fuerza = fuerza;
    }

    public Integer getAgilidad() {
        return agilidad;
    }

    public void setAgilidad(Integer agilidad) {
        this.agilidad = agilidad;
    }

    public Integer getInteligencia() {
        return inteligencia;
    }

    public void setInteligencia(Integer inteligencia) {
        this.inteligencia = inteligencia;
    }

    public List<ObjetoInventario> getObjetos() {
        return objetos;
    }

    public void setObjetos(List<ObjetoInventario> objetos) {
        this.objetos = objetos;
    }
}
