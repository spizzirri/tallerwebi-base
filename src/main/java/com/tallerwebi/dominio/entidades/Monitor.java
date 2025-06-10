package com.tallerwebi.dominio.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Monitor extends Componente {

    /*Caracteristicas generales*/
    @Column(length = 100)
    private String tipoDeIluminacion;
    @Column(length = 100)
    private String tipoDePanel;
    private Boolean pantallaCurva;

    /*Conectividad*/
    private Integer puertosHDMI;
    private Integer puertosDisplayPort;
    private Integer puertosMiniDisplayPort;
    private Integer puertosVGA;
    private Integer puertosDVI;
    private Integer puertosUSB;
    private Boolean conectorAuriculares;

    /*Pantalla*/
    @Column(length = 100)
    private String pulgadas;
    @Column(length = 100)
    private String resolucionMaxima;
    @Column(length = 100)
    private String frecuenciaMaxima;
    @Column(length = 100)
    private String tiempoDeRespuesta;
    private Boolean nvidiaGSync;
    private Boolean amdFreesync;

    /*Dimensiones*/
    @Column(length = 100)
    private String ancho;
    @Column(length = 100)
    private String alto;
    @Column(length = 100)
    private String espesor;
    @Column(length = 100)
    private String curvatura;

    public Monitor() {}

    public Monitor(Long id, String nombre, Double precio, Integer stock, String marca,
                   String tipoDeIluminacion, String tipoDePanel, Boolean pantallaCurva,
                   Integer puertosHDMI, Integer puertosDisplayPort, Integer puertosMiniDisplayPort, Integer puertosVGA, Integer puertosDVI, Integer puertosUSB, Boolean conectorAuriculares,
                   String pulgadas, String resolucionMaxima, String frecuenciaMaxima, String tiempoDeRespuesta, Boolean nvidiaGSync, Boolean amdFreesync,
                   String ancho, String alto, String espesor, String curvatura) {
        super(id, nombre, precio, stock, marca);

        this.tipoDeIluminacion = tipoDeIluminacion;
        this.tipoDePanel = tipoDePanel;
        this.pantallaCurva = pantallaCurva;

        this.puertosHDMI = puertosHDMI;
        this.puertosDisplayPort = puertosDisplayPort;
        this.puertosMiniDisplayPort = puertosMiniDisplayPort;
        this.puertosVGA = puertosVGA;
        this.puertosDVI = puertosDVI;
        this.puertosUSB = puertosUSB;
        this.conectorAuriculares = conectorAuriculares;

        this.pulgadas = pulgadas;
        this.resolucionMaxima = resolucionMaxima;
        this.frecuenciaMaxima = frecuenciaMaxima;
        this.tiempoDeRespuesta = tiempoDeRespuesta;
        this.nvidiaGSync = nvidiaGSync;
        this.amdFreesync = amdFreesync;

        this.ancho = ancho;
        this.alto = alto;
        this.espesor = espesor;
        this.curvatura = curvatura;
    }

    public String getTipoDeIluminacion() {
        return tipoDeIluminacion;
    }

    public void setTipoDeIluminacion(String tipoDeIluminacion) {
        this.tipoDeIluminacion = tipoDeIluminacion;
    }

    public String getTipoDePanel() {
        return tipoDePanel;
    }

    public void setTipoDePanel(String tipoDePanel) {
        this.tipoDePanel = tipoDePanel;
    }

    public Boolean getPantallaCurva() {
        return pantallaCurva;
    }

    public void setPantallaCurva(Boolean pantallaCurva) {
        this.pantallaCurva = pantallaCurva;
    }

    public Integer getPuertosHDMI() {
        return puertosHDMI;
    }

    public void setPuertosHDMI(Integer puertosHDMI) {
        this.puertosHDMI = puertosHDMI;
    }

    public Integer getPuertosDisplayPort() {
        return puertosDisplayPort;
    }

    public void setPuertosDisplayPort(Integer puertosDisplayPort) {
        this.puertosDisplayPort = puertosDisplayPort;
    }

    public Integer getPuertosMiniDisplayPort() {
        return puertosMiniDisplayPort;
    }

    public void setPuertosMiniDisplayPort(Integer puertosMiniDisplayPort) {
        this.puertosMiniDisplayPort = puertosMiniDisplayPort;
    }

    public Integer getPuertosVGA() {
        return puertosVGA;
    }

    public void setPuertosVGA(Integer puertosVGA) {
        this.puertosVGA = puertosVGA;
    }

    public Integer getPuertosDVI() {
        return puertosDVI;
    }

    public void setPuertosDVI(Integer puertosDVI) {
        this.puertosDVI = puertosDVI;
    }

    public Integer getPuertosUSB() {
        return puertosUSB;
    }

    public void setPuertosUSB(Integer puertosUSB) {
        this.puertosUSB = puertosUSB;
    }

    public Boolean getConectorAuriculares() {
        return conectorAuriculares;
    }

    public void setConectorAuriculares(Boolean conectorAuriculares) {
        this.conectorAuriculares = conectorAuriculares;
    }

    public String getPulgadas() {
        return pulgadas;
    }

    public void setPulgadas(String pulgadas) {
        this.pulgadas = pulgadas;
    }

    public String getResolucionMaxima() {
        return resolucionMaxima;
    }

    public void setResolucionMaxima(String resolucionMaxima) {
        this.resolucionMaxima = resolucionMaxima;
    }

    public String getFrecuenciaMaxima() {
        return frecuenciaMaxima;
    }

    public void setFrecuenciaMaxima(String frecuenciaMaxima) {
        this.frecuenciaMaxima = frecuenciaMaxima;
    }

    public String getTiempoDeRespuesta() {
        return tiempoDeRespuesta;
    }

    public void setTiempoDeRespuesta(String tiempoDeRespuesta) {
        this.tiempoDeRespuesta = tiempoDeRespuesta;
    }

    public Boolean getNvidiaGSync() {
        return nvidiaGSync;
    }

    public void setNvidiaGSync(Boolean nvidiaGSync) {
        this.nvidiaGSync = nvidiaGSync;
    }

    public Boolean getAmdFreesync() {
        return amdFreesync;
    }

    public void setAmdFreesync(Boolean amdFreesync) {
        this.amdFreesync = amdFreesync;
    }

    public String getAncho() {
        return ancho;
    }

    public void setAncho(String ancho) {
        this.ancho = ancho;
    }

    public String getAlto() {
        return alto;
    }

    public void setAlto(String alto) {
        this.alto = alto;
    }

    public String getEspesor() {
        return espesor;
    }

    public void setEspesor(String espesor) {
        this.espesor = espesor;
    }

    public String getCurvatura() {
        return curvatura;
    }

    public void setCurvatura(String curvatura) {
        this.curvatura = curvatura;
    }
}
