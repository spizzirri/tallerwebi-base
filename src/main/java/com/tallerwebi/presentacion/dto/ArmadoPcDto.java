package com.tallerwebi.presentacion.dto;

import com.tallerwebi.dominio.entidades.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public ArmadoPcDto(ArmadoPc entidad) {
        this.procesador = new ComponenteDto(entidad.getProcesador());
        this.motherboard = new ComponenteDto(entidad.getMotherboard());
        this.cooler = new ComponenteDto(entidad.getCooler());
        this.rams = this.convertirListaDeComponentesEntidadADtos(entidad.getMemoriaRAM());
        this.gpu = new ComponenteDto(entidad.getPlacaDeVideo());
        this.almacenamiento = this.convertirListaDeComponentesEntidadADtos(entidad.getAlmacenamiento());
        this.fuente = new ComponenteDto(entidad.getFuenteDeAlimentacion());
        this.gabinete = new ComponenteDto(entidad.getGabinete());
        this.monitor = new ComponenteDto(entidad.getMonitor());
        this.perifericos = this.convertirListaDeComponentesEntidadADtos(entidad.getPerifericos());
    }

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

    private List<ComponenteDto> convertirListaDeComponentesEntidadADtos(List<? extends Componente> listaEntidades) {
        List<ComponenteDto> componentesDto = new ArrayList<>();
        for (Componente componenteEntidad : listaEntidades) componentesDto.add(new ComponenteDto(componenteEntidad));
        return componentesDto;
    }

    public ArmadoPc obtenerEntidad() {
        ArmadoPc entidad = new ArmadoPc();
        entidad.setProcesador((Procesador) this.procesador.obtenerEntidad());
        entidad.setMotherboard((Motherboard) this.motherboard.obtenerEntidad());
        entidad.setCoolerCPU((CoolerCPU) this.cooler.obtenerEntidad());
        entidad.setMemoriaRAM(this.convertirListaDeDtosAEntidades(this.rams, MemoriaRAM.class));
        entidad.setPlacaDeVideo((PlacaDeVideo) this.gpu.obtenerEntidad());
        entidad.setAlmacenamiento(this.convertirListaDeDtosAEntidades(this.almacenamiento, Almacenamiento.class));
        entidad.setFuenteDeAlimentacion((FuenteDeAlimentacion) this.fuente.obtenerEntidad());
        entidad.setGabinete((Gabinete) this.gabinete.obtenerEntidad());
        // recordar que estos ultimos 2 son considerados solamente como Componente por ahora.
        entidad.setMonitor(this.monitor.obtenerEntidad());
        entidad.setPerifericos(this.convertirListaDeDtosAEntidades(this.perifericos, Componente.class));
        return entidad;
    }

    private <T extends Componente> List<T> convertirListaDeDtosAEntidades(List<ComponenteDto> listaDtos, Class<T> tipo) {
        List<T> entidades = new ArrayList<>();
        for (ComponenteDto dto : listaDtos) entidades.add(tipo.cast(dto.obtenerEntidad()));
        return entidades;
    }

    public ComponenteDto getProcesador() { return this.procesador; }

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

    public Map<String, Integer> getRamsDetalladas(){
        return obtenerListaComponenteDetallado(this.rams);
    }
    public Map<String, Integer> getAlmacenamientoDetallado(){
        return obtenerListaComponenteDetallado(this.almacenamiento);
    }
    public Map<String, Integer> getPerifericosDetallados(){
        return obtenerListaComponenteDetallado(this.perifericos);
    }

    private Map<String, Integer> obtenerListaComponenteDetallado(List<ComponenteDto> componentes) {

        Map<String, Integer> componentesDetallados = new HashMap<>();

        for(ComponenteDto componente : componentes){
            if (componentesDetallados.containsKey(componente.getModelo()))
                componentesDetallados.put(componente.getModelo(), componentesDetallados.get(componente.getModelo()) + 1);
             else
                componentesDetallados.put(componente.getModelo(), 1);
        }
    return componentesDetallados;
    }

    public Double getPrecioTotal(){
        Double sumatoria = 0.0;

        if(this.procesador != null) sumatoria += this.procesador.getPrecio();
        if(this.motherboard != null) sumatoria += this.motherboard.getPrecio();
        if(this.cooler != null) sumatoria += this.cooler.getPrecio();
        if(this.gpu != null) sumatoria += this.gpu.getPrecio();
        if(this.fuente != null) sumatoria += this.fuente.getPrecio();
        if(this.gabinete != null) sumatoria += this.gabinete.getPrecio();
        if(this.monitor != null) sumatoria += this.monitor.getPrecio();
        if(this.rams != null && !this.rams.isEmpty()) sumatoria += sumatoriaDeListaDeComponente(this.rams);
        if(this.almacenamiento != null && !this.almacenamiento.isEmpty()) sumatoria += sumatoriaDeListaDeComponente(this.almacenamiento);
        if(this.perifericos != null && !this.perifericos.isEmpty()) sumatoria += sumatoriaDeListaDeComponente(this.perifericos);

        return sumatoria;
    }

    private Double sumatoriaDeListaDeComponente(List<ComponenteDto> componentes) {
        Double sumatoriaDeLista = 0.0;
        for(ComponenteDto componente : componentes){
            sumatoriaDeLista += componente.getPrecio();
        }
        return sumatoriaDeLista;
    }
}
