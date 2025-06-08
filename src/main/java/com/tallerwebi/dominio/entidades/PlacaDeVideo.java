package com.tallerwebi.dominio.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class PlacaDeVideo extends Componente{

    /*Caracteristicas generales*/
    @Column(length = 100)
    private String chipsetGPU;
    private Integer cantidadDeCoolers;

    /*Conectividad*/
    private Integer cantidadDeVGA;
    private Integer cantidadDeDVIDigital;
    private Integer cantidadDeHDMI;
    private Integer cantidadDeDisplayport;

    /*Energia*/
    @Column(length = 100)
    private String consumo;
    private Integer cantidadDePCIE6Pines;
    private Integer cantidadDePCIE8Pines;
    private Integer cantidadDePCIE16Pines;
    private Integer cantidadDeAdaptadores16Pines;

    /*Extras*/
    @Column(length = 100)
    private String velocidadDelCoreBase;
    @Column(length = 100)
    private String velocidadDelCoreTurbo;
    @Column(length = 100)
    private String tecnologiaRAM;
    @Column(length = 100)
    private String capacidadRAM;

    public PlacaDeVideo() {}

    public PlacaDeVideo(Long id, String nombre, Double precio, Integer stock, String marca,
                        String chipsetGPU, Integer cantidadDeCoolers,
                        Integer cantidadDeVGA, Integer cantidadDeDVIDigital, Integer cantidadDeHDMI, Integer cantidadDeDisplayport,
                        String consumo, Integer cantidadDePCIE6Pines, Integer cantidadDePCIE8Pines, Integer cantidadDePCIE16Pines, Integer cantidadDeAdaptadores16Pines,
                        String velocidadDelCoreBase, String velocidadDelCoreTurbo, String tecnologiaRAM, String capacidadRAM) {
        super(id, nombre, precio, stock, marca);

        this.chipsetGPU = chipsetGPU;
        this.cantidadDeCoolers = cantidadDeCoolers;

        this.cantidadDeVGA = cantidadDeVGA;
        this.cantidadDeDVIDigital = cantidadDeDVIDigital;
        this.cantidadDeHDMI = cantidadDeHDMI;
        this.cantidadDeDisplayport = cantidadDeDisplayport;

        this.consumo = consumo;
        this.cantidadDePCIE6Pines = cantidadDePCIE6Pines;
        this.cantidadDePCIE8Pines = cantidadDePCIE8Pines;
        this.cantidadDePCIE16Pines = cantidadDePCIE16Pines;
        this.cantidadDeAdaptadores16Pines = cantidadDeAdaptadores16Pines;

        this.velocidadDelCoreBase = velocidadDelCoreBase;
        this.velocidadDelCoreTurbo = velocidadDelCoreTurbo;
        this.tecnologiaRAM = tecnologiaRAM;
        this.capacidadRAM = capacidadRAM;
    }

    public String getChipsetGPU() {
        return chipsetGPU;
    }

    public void setChipsetGPU(String chipsetGPU) {
        this.chipsetGPU = chipsetGPU;
    }

    public Integer getCantidadDeCoolers() {
        return cantidadDeCoolers;
    }

    public void setCantidadDeCoolers(Integer cantidadDeCoolers) {
        this.cantidadDeCoolers = cantidadDeCoolers;
    }

    public Integer getCantidadDeVGA() {
        return cantidadDeVGA;
    }

    public void setCantidadDeVGA(Integer cantidadDeVGA) {
        this.cantidadDeVGA = cantidadDeVGA;
    }

    public Integer getCantidadDeDVIDigital() {
        return cantidadDeDVIDigital;
    }

    public void setCantidadDeDVIDigital(Integer cantidadDeDVIDigital) {
        this.cantidadDeDVIDigital = cantidadDeDVIDigital;
    }

    public Integer getCantidadDeHDMI() {
        return cantidadDeHDMI;
    }

    public void setCantidadDeHDMI(Integer cantidadDeHDMI) {
        this.cantidadDeHDMI = cantidadDeHDMI;
    }

    public Integer getCantidadDeDisplayport() {
        return cantidadDeDisplayport;
    }

    public void setCantidadDeDisplayport(Integer cantidadDeDisplayport) {
        this.cantidadDeDisplayport = cantidadDeDisplayport;
    }

    public String getConsumo() {
        return consumo;
    }

    public void setConsumo(String consumo) {
        this.consumo = consumo;
    }

    public Integer getCantidadDePCIE6Pines() {
        return cantidadDePCIE6Pines;
    }

    public void setCantidadDePCIE6Pines(Integer cantidadDePCIE6Pines) {
        this.cantidadDePCIE6Pines = cantidadDePCIE6Pines;
    }

    public Integer getCantidadDePCIE8Pines() {
        return cantidadDePCIE8Pines;
    }

    public void setCantidadDePCIE8Pines(Integer cantidadDePCIE8Pines) {
        this.cantidadDePCIE8Pines = cantidadDePCIE8Pines;
    }

    public Integer getCantidadDePCIE16Pines() {
        return cantidadDePCIE16Pines;
    }

    public void setCantidadDePCIE16Pines(Integer cantidadDePCIE16Pines) {
        this.cantidadDePCIE16Pines = cantidadDePCIE16Pines;
    }

    public Integer getCantidadDeAdaptadores16Pines() {
        return cantidadDeAdaptadores16Pines;
    }

    public void setCantidadDeAdaptadores16Pines(Integer cantidadDeAdaptadores16Pines) {
        this.cantidadDeAdaptadores16Pines = cantidadDeAdaptadores16Pines;
    }

    public String getVelocidadDelCoreBase() {
        return velocidadDelCoreBase;
    }

    public void setVelocidadDelCoreBase(String velocidadDelCoreBase) {
        this.velocidadDelCoreBase = velocidadDelCoreBase;
    }

    public String getVelocidadDelCoreTurbo() {
        return velocidadDelCoreTurbo;
    }

    public void setVelocidadDelCoreTurbo(String velocidadDelCoreTurbo) {
        this.velocidadDelCoreTurbo = velocidadDelCoreTurbo;
    }

    public String getTecnologiaRAM() {
        return tecnologiaRAM;
    }

    public void setTecnologiaRAM(String tecnologiaRAM) {
        this.tecnologiaRAM = tecnologiaRAM;
    }

    public String getCapacidadRAM() {
        return capacidadRAM;
    }

    public void setCapacidadRAM(String capacidadRAM) {
        this.capacidadRAM = capacidadRAM;
    }
}
