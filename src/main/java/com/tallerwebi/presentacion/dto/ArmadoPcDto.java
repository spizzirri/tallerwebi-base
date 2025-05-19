package com.tallerwebi.presentacion.dto;

import com.fasterxml.classmate.AnnotationOverrides;

import java.util.ArrayList;
import java.util.List;

public class ArmadoPcDto {


    private ComponenteDto procesador;
    private ComponenteDto motherboard;
    private ComponenteDto cooler;
    private List<ComponenteDto> rams;
    private ComponenteDto gpu;
    private List<ComponenteDto> almacenamiento;
    private ComponenteDto fuente;
    private ComponenteDto gabinete;
    private ComponenteDto monitor;
    private List<ComponenteDto> perifericos;

    public ArmadoPcDto(ComponenteDto procesador,
                       ComponenteDto motherboard,
                       ComponenteDto cooler,
                       List<ComponenteDto> rams,
                       ComponenteDto gpu,
                       List<ComponenteDto> almacenamiento,
                       ComponenteDto fuente,
                       ComponenteDto gabinete,
                       ComponenteDto monitor,
                       List<ComponenteDto> perifericos) {
        this.procesador = procesador;
        this.motherboard = motherboard;
        this.cooler = cooler;
        this.rams = rams;
        this.gpu = gpu;
        this.almacenamiento = almacenamiento;
        this.fuente = fuente;
        this.gabinete = gabinete;
        this.monitor = monitor;
        this.perifericos = perifericos;
    }

    public ArmadoPcDto() {
        this.rams = new ArrayList<>();
        this.almacenamiento = new ArrayList<>();
        this.perifericos = new ArrayList<>();
    }

    public ComponenteDto getProcesador() {
        return this.procesador;
    }

    public void setProcesador(ComponenteDto procesador) {
        this.procesador = procesador;
    }

    public List<ComponenteDto> getPerifericos() {
        return perifericos;
    }

    public void setPerifericos(List<ComponenteDto> perifericos) {
        this.perifericos = perifericos;
    }

    public ComponenteDto getMonitor() {
        return monitor;
    }

    public void setMonitor(ComponenteDto monitor) {
        this.monitor = monitor;
    }

    public ComponenteDto getGabinete() {
        return gabinete;
    }

    public void setGabinete(ComponenteDto gabinete) {
        this.gabinete = gabinete;
    }

    public ComponenteDto getFuente() {
        return fuente;
    }

    public void setFuente(ComponenteDto fuente) {
        this.fuente = fuente;
    }

    public List<ComponenteDto> getAlmacenamiento() {
        return almacenamiento;
    }

    public void setAlmacenamiento(List<ComponenteDto> almacenamiento) {
        this.almacenamiento = almacenamiento;
    }

    public ComponenteDto getGpu() {
        return gpu;
    }

    public void setGpu(ComponenteDto gpu) {
        this.gpu = gpu;
    }

    public List<ComponenteDto> getRams() {
        return rams;
    }

    public void setRams(List<ComponenteDto> rams) {
        this.rams = rams;
    }

    public ComponenteDto getCooler() {
        return cooler;
    }

    public void setCooler(ComponenteDto cooler) {
        this.cooler = cooler;
    }

    public ComponenteDto getMotherboard() {
        return motherboard;
    }

    public void setMotherboard(ComponenteDto motherboard) {
        this.motherboard = motherboard;
    }
}
