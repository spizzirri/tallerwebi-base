package com.tallerwebi.dominio.entidades;

import java.util.List;

public class ArmadoPc {

    // deberia tener ID

    private Procesador procesador;
    private Motherboard motherboard;
    private CoolerCPU coolerCPU;
    private PlacaDeVideo placaDeVideo;
    private FuenteDeAlimentacion fuenteDeAlimentacion;
    private Gabinete gabinete;
    private List<MemoriaRAM> memoriaRAM;
    private List<Almacenamiento> almacenamiento;
    private Monitor monitor;
    private List<Periferico> perifericos;


    public Procesador getProcesador() {
        return this.procesador;
    }

    public Motherboard getMotherboard() {
        return this.motherboard;
    }

    public CoolerCPU getCooler() {
        return this.coolerCPU;
    }

    public PlacaDeVideo getPlacaDeVideo() {
        return this.placaDeVideo;
    }

    public FuenteDeAlimentacion getFuenteDeAlimentacion() {
        return this.fuenteDeAlimentacion;
    }

    public Gabinete getGabinete() {
        return this.gabinete;
    }

    public Monitor getMonitor() {
        return this.monitor;
    }

    public List<MemoriaRAM> getMemoriaRAM() {
        return this.memoriaRAM;
    }

    public List<Almacenamiento> getAlmacenamiento(){
        return this.almacenamiento;
    }

    public List<Periferico> getPerifericos() {
        return this.perifericos;
    }

    public void setProcesador(Procesador procesador) {
        this.procesador = procesador;
    }

    public void setMotherboard(Motherboard motherboard) {
        this.motherboard = motherboard;
    }

    public void setCoolerCPU(CoolerCPU coolerCPU) {
        this.coolerCPU = coolerCPU;
    }

    public void setPlacaDeVideo(PlacaDeVideo placaDeVideo) {
        this.placaDeVideo = placaDeVideo;
    }

    public void setFuenteDeAlimentacion(FuenteDeAlimentacion fuenteDeAlimentacion) {
        this.fuenteDeAlimentacion = fuenteDeAlimentacion;
    }

    public void setGabinete(Gabinete gabinete) {
        this.gabinete = gabinete;
    }

    public void setMemoriaRAM(List<MemoriaRAM> memoriaRAM) {
        this.memoriaRAM = memoriaRAM;
    }

    public void setAlmacenamiento(List<Almacenamiento> almacenamiento) {
        this.almacenamiento = almacenamiento;
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    public void setPerifericos(List<Periferico> perifericos) {
        this.perifericos = perifericos;
    }
}
