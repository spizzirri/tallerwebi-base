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
    private String precioFormateado;

    public ArmadoPcDto(ArmadoPc entidad) {
        this.procesador = new ComponenteDto(entidad.getProcesador());
        this.motherboard = new ComponenteDto(entidad.getMotherboard());
        this.cooler = new ComponenteDto(entidad.getCoolerCPU());
        this.rams = this.convertirListaDeComponentesEntidadADtos(entidad.getMemoriaRAM());
        this.gpu = new ComponenteDto(entidad.getPlacaDeVideo());
        this.almacenamiento = this.convertirListaDeComponentesEntidadADtos(entidad.getAlmacenamiento());
        this.fuente = new ComponenteDto(entidad.getFuenteDeAlimentacion());
        this.gabinete = new ComponenteDto(entidad.getGabinete());
        this.monitor = new ComponenteDto(entidad.getMonitor());
        this.perifericos = this.convertirListaDeComponentesEntidadADtos(entidad.getPerifericos());
        this.precioFormateado = "0.0";
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
        this.precioFormateado = "0.0";
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
        if (this.procesador != null) entidad.setProcesador((Procesador) this.procesador.obtenerEntidad());
        if (this.motherboard != null) entidad.setMotherboard((Motherboard) this.motherboard.obtenerEntidad());
        if (this.cooler != null) entidad.setCoolerCPU((CoolerCPU) this.cooler.obtenerEntidad());
        entidad.setMemoriaRAM(this.convertirListaDeDtosAEntidades(this.rams, MemoriaRAM.class));
        if (this.gpu != null) entidad.setPlacaDeVideo((PlacaDeVideo) this.gpu.obtenerEntidad());
        entidad.setAlmacenamiento(this.convertirListaDeDtosAEntidades(this.almacenamiento, Almacenamiento.class));
        if (this.fuente != null) entidad.setFuenteDeAlimentacion((FuenteDeAlimentacion) this.fuente.obtenerEntidad());
        if (this.gabinete != null) entidad.setGabinete((Gabinete) this.gabinete.obtenerEntidad());
        if (this.monitor != null) entidad.setMonitor((Monitor) this.monitor.obtenerEntidad());
        entidad.setPerifericos(this.convertirListaDeDtosAEntidades(this.perifericos, Periferico.class));
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

    public String getPrecioFormateado() {
        return precioFormateado;
    }

    public void setPrecioFormateado(String precioFormateado) {
        this.precioFormateado = precioFormateado;
    }

    public Map<ComponenteDto, Integer> getRamsDetalladas(){
        return obtenerListaComponenteDetallado(this.rams);
    }
    public Map<ComponenteDto, Integer> getAlmacenamientoDetallado(){
        return obtenerListaComponenteDetallado(this.almacenamiento);
    }
    public Map<ComponenteDto, Integer> getPerifericosDetallados(){
        return obtenerListaComponenteDetallado(this.perifericos);
    }

    private Map<ComponenteDto, Integer> obtenerListaComponenteDetallado(List<ComponenteDto> componentes) {

        Map<ComponenteDto, Integer> componentesDetallados = new HashMap<>();

        for(ComponenteDto componente : componentes){
            if (componentesDetallados.containsKey(componente))
                componentesDetallados.put(componente, componentesDetallados.get(componente) + 1);
             else
                componentesDetallados.put(componente, 1);
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


    // deberia tener test esto?
    public List<ComponenteDto> getComponentesDto() {
        List<ComponenteDto> componentesDto = new ArrayList<>();
        componentesDto.add(this.procesador);
        componentesDto.add(this.motherboard);
        componentesDto.add(this.cooler);
        componentesDto.add(this.gpu);
        componentesDto.add(this.fuente);
        componentesDto.add(this.gabinete);
        componentesDto.add(this.monitor);
        componentesDto.addAll(this.rams);
        componentesDto.addAll(this.almacenamiento);
        componentesDto.addAll(this.perifericos);
        return componentesDto;
    }

    public Map<Long, Integer> getIdYCantidadComponentes() {
        Map<Long, Integer> idYCantidad = new HashMap<>();

        List<ComponenteDto> componentesDto = this.getComponentesDto();


        for(ComponenteDto componente : componentesDto){

            if (idYCantidad.containsKey(componente.getId()))
                idYCantidad.put(componente.getId(), idYCantidad.get(componente.getId()) + 1);
            else
                idYCantidad.put(componente.getId(), 1);

        }
        return idYCantidad;
    }
}
