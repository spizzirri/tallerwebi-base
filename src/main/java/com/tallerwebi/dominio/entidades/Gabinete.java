package com.tallerwebi.dominio.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Gabinete extends Componente {

    /*Caracteristicas generales*/
    @Column(length = 100)
    private String color;
    private Boolean conVentana;
    @Column(length = 100)
    private String tipoDeVentana;
    @Column(length = 100)
    private String tamanioGabinete;

    /*Conectividad*/
    private Integer cantUSB;
    private Boolean audioFrontal;

    /*Coolers y disipadores*/
    private Integer cantCoolerFanDe80mmSoportados;
    private Integer cantCoolerFanDe80mmIncluidos;
    private Integer cantCoolerFanDe120mmSoportados;
    private Integer cantCoolerFanDe120mmIncluidos;
    private Integer cantCoolerFanDe140mmSoportados;
    private Integer cantCoolerFanDe140mmIncluidos;
    private Integer cantCoolerFanDe200mmSoportados;
    private Integer cantCoolerFanDe200mmIncluidos;
    private Integer cantRadiador240mmSoportados;
    private Integer cantRadiador280mmSoportados;
    private Integer cantRadiador360mmSoportados;
    private Integer cantRadiador420mmSoportados;

    public Gabinete() {}

    public Gabinete(Long id, String nombre, Double precio, Integer stock, String marca,
                    String color, Boolean conVentana, String tipoDeVentana, String tamanioGabinete,
                    Integer cantUSB, Boolean audioFrontal,
                    Integer cantCoolerFanDe80mmSoportados, Integer cantCoolerFanDe80mmIncluidos,
                    Integer cantCoolerFanDe120mmSoportados, Integer cantCoolerFanDe120mmIncluidos,
                    Integer cantCoolerFanDe140mmSoportados, Integer cantCoolerFanDe140mmIncluidos,
                    Integer cantCoolerFanDe200mmSoportados, Integer cantCoolerFanDe200mmIncluidos,
                    Integer cantRadiador240mmSoportados, Integer cantRadiador280mmSoportados,
                    Integer cantRadiador360mmSoportados, Integer cantRadiador420mmSoportados) {
        super(id, nombre, precio, stock, marca);

        this.color = color;
        this.conVentana = conVentana;
        this.tipoDeVentana = tipoDeVentana;
        this.tamanioGabinete = tamanioGabinete;

        this.cantUSB = cantUSB;
        this.audioFrontal = audioFrontal;

        this.cantCoolerFanDe80mmSoportados = cantCoolerFanDe80mmSoportados;
        this.cantCoolerFanDe80mmIncluidos = cantCoolerFanDe80mmIncluidos;
        this.cantCoolerFanDe120mmSoportados = cantCoolerFanDe120mmSoportados;
        this.cantCoolerFanDe120mmIncluidos = cantCoolerFanDe120mmIncluidos;
        this.cantCoolerFanDe140mmSoportados = cantCoolerFanDe140mmSoportados;
        this.cantCoolerFanDe140mmIncluidos = cantCoolerFanDe140mmIncluidos;
        this.cantCoolerFanDe200mmSoportados = cantCoolerFanDe200mmSoportados;
        this.cantCoolerFanDe200mmIncluidos = cantCoolerFanDe200mmIncluidos;
        this.cantRadiador240mmSoportados = cantRadiador240mmSoportados;
        this.cantRadiador280mmSoportados = cantRadiador280mmSoportados;
        this.cantRadiador360mmSoportados = cantRadiador360mmSoportados;
        this.cantRadiador420mmSoportados = cantRadiador420mmSoportados;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getConVentana() {
        return conVentana;
    }

    public void setConVentana(Boolean conVentana) {
        this.conVentana = conVentana;
    }

    public String getTipoDeVentana() {
        return tipoDeVentana;
    }

    public void setTipoDeVentana(String tipoDeVentana) {
        this.tipoDeVentana = tipoDeVentana;
    }

    public String getTamanioGabinete() {
        return tamanioGabinete;
    }

    public void setTamanioGabinete(String tamanioGabinete) {
        this.tamanioGabinete = tamanioGabinete;
    }

    public Integer getCantUSB() {
        return cantUSB;
    }

    public void setCantUSB(Integer cantUSB) {
        this.cantUSB = cantUSB;
    }

    public Boolean getAudioFrontal() {
        return audioFrontal;
    }

    public void setAudioFrontal(Boolean audioFrontal) {
        this.audioFrontal = audioFrontal;
    }

    public Integer getCantCoolerFanDe80mmSoportados() {
        return cantCoolerFanDe80mmSoportados;
    }

    public void setCantCoolerFanDe80mmSoportados(Integer cantCoolerFanDe80mmSoportados) {
        this.cantCoolerFanDe80mmSoportados = cantCoolerFanDe80mmSoportados;
    }

    public Integer getCantCoolerFanDe80mmIncluidos() {
        return cantCoolerFanDe80mmIncluidos;
    }

    public void setCantCoolerFanDe80mmIncluidos(Integer cantCoolerFanDe80mmIncluidos) {
        this.cantCoolerFanDe80mmIncluidos = cantCoolerFanDe80mmIncluidos;
    }

    public Integer getCantCoolerFanDe120mmSoportados() {
        return cantCoolerFanDe120mmSoportados;
    }

    public void setCantCoolerFanDe120mmSoportados(Integer cantCoolerFanDe120mmSoportados) {
        this.cantCoolerFanDe120mmSoportados = cantCoolerFanDe120mmSoportados;
    }

    public Integer getCantCoolerFanDe120mmIncluidos() {
        return cantCoolerFanDe120mmIncluidos;
    }

    public void setCantCoolerFanDe120mmIncluidos(Integer cantCoolerFanDe120mmIncluidos) {
        this.cantCoolerFanDe120mmIncluidos = cantCoolerFanDe120mmIncluidos;
    }

    public Integer getCantCoolerFanDe140mmSoportados() {
        return cantCoolerFanDe140mmSoportados;
    }

    public void setCantCoolerFanDe140mmSoportados(Integer cantCoolerFanDe140mmSoportados) {
        this.cantCoolerFanDe140mmSoportados = cantCoolerFanDe140mmSoportados;
    }

    public Integer getCantCoolerFanDe140mmIncluidos() {
        return cantCoolerFanDe140mmIncluidos;
    }

    public void setCantCoolerFanDe140mmIncluidos(Integer cantCoolerFanDe140mmIncluidos) {
        this.cantCoolerFanDe140mmIncluidos = cantCoolerFanDe140mmIncluidos;
    }

    public Integer getCantCoolerFanDe200mmSoportados() {
        return cantCoolerFanDe200mmSoportados;
    }

    public void setCantCoolerFanDe200mmSoportados(Integer cantCoolerFanDe200mmSoportados) {
        this.cantCoolerFanDe200mmSoportados = cantCoolerFanDe200mmSoportados;
    }

    public Integer getCantCoolerFanDe200mmIncluidos() {
        return cantCoolerFanDe200mmIncluidos;
    }

    public void setCantCoolerFanDe200mmIncluidos(Integer cantCoolerFanDe200mmIncluidos) {
        this.cantCoolerFanDe200mmIncluidos = cantCoolerFanDe200mmIncluidos;
    }

    public Integer getCantRadiador240mmSoportados() {
        return cantRadiador240mmSoportados;
    }

    public void setCantRadiador240mmSoportados(Integer cantRadiador240mmSoportados) {
        this.cantRadiador240mmSoportados = cantRadiador240mmSoportados;
    }

    public Integer getCantRadiador280mmSoportados() {
        return cantRadiador280mmSoportados;
    }

    public void setCantRadiador280mmSoportados(Integer cantRadiador280mmSoportados) {
        this.cantRadiador280mmSoportados = cantRadiador280mmSoportados;
    }

    public Integer getCantRadiador360mmSoportados() {
        return cantRadiador360mmSoportados;
    }

    public void setCantRadiador360mmSoportados(Integer cantRadiador360mmSoportados) {
        this.cantRadiador360mmSoportados = cantRadiador360mmSoportados;
    }

    public Integer getCantRadiador420mmSoportados() {
        return cantRadiador420mmSoportados;
    }

    public void setCantRadiador420mmSoportados(Integer cantRadiador420mmSoportados) {
        this.cantRadiador420mmSoportados = cantRadiador420mmSoportados;
    }
}
