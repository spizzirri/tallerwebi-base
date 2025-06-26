package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.*;

import java.util.*;
import java.util.stream.Collectors;

public class ComponenteEspecificoDto {

    private Long id;
    private String nombre;
    private Double precio;
    private Integer stock;
    private String marca;
    private List<String> imagenes;

    private Map<String, Map<String, String>> caracteristicas;

    public ComponenteEspecificoDto() {}

    public ComponenteEspecificoDto(Long id, String nombre, Double precio, Integer stock, String marca,
                                   List<String> imagenes, Map<String, Map<String, String>> caracteristicas) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.marca = marca;
        this.imagenes = imagenes;
        this.caracteristicas = new LinkedHashMap<>();
    }

    public ComponenteEspecificoDto(Componente productoEspecifico) {

        this.id = productoEspecifico.getId();
        this.nombre = productoEspecifico.getNombre();
        this.precio = productoEspecifico.getPrecio();
        this.stock = productoEspecifico.getStock();
        this.marca = productoEspecifico.getMarca();
        if (productoEspecifico.getImagenes() != null) {
            this.imagenes = new ArrayList<>();
            for(int i = 0; i < productoEspecifico.getImagenes().size(); i++){
                imagenes.add(productoEspecifico.getImagenes().get(i).getUrlImagen());
            }
        }
        this.caracteristicas = new LinkedHashMap<>();

        if (productoEspecifico instanceof Procesador) {

            Map<String, String> caracteristicasGenerales = this.obtenerCarcteristicasGeneralesProcesador((Procesador) productoEspecifico);
            caracteristicas.put("Caracteristicas generales", caracteristicasGenerales);
            Map<String, String> especificacionesCPU = this.obtenerEspecificacionesCPUProcesador((Procesador) productoEspecifico);
            caracteristicas.put("Especificaciones CPU", especificacionesCPU);
            Map<String, String> coolersYDisipadores = this.obtenerCoolersYDisipadoresProcesador((Procesador) productoEspecifico);
            caracteristicas.put("Coolers y Disipadores", coolersYDisipadores);
            Map<String, String> memoria = this.obtenerMemoriaProcesador((Procesador) productoEspecifico);
            caracteristicas.put("Memoria", memoria);

        } else if (productoEspecifico instanceof Motherboard) {

            Map<String, String> caracteristicasGenerales = this.obtenerCaracteristicasGeneralesMotherboard((Motherboard) productoEspecifico);
            caracteristicas.put("Caracteristicas Generales", caracteristicasGenerales);
            Map<String, String> conectividad = this.obtenerConectividadMotherboard((Motherboard) productoEspecifico);
            caracteristicas.put("Conectividad", conectividad);
            Map<String, String> memoria = this.obtenerMemoriaMotherboard((Motherboard) productoEspecifico);
            caracteristicas.put("Memoria", memoria);
            Map<String, String> energia = this.obtenerEnergiaMotherboard((Motherboard) productoEspecifico);
            caracteristicas.put("Energia", energia);

        } else if (productoEspecifico instanceof CoolerCPU) {

            Map<String, String> caracteristicasGenerales = this.obtenerCaracteristicasGeneralesCoolerCPU((CoolerCPU) productoEspecifico);
            caracteristicas.put("Caracteristicas Generales", caracteristicasGenerales);
            Map<String, String> compatibilidad = this.obtenerCompatibilidadCoolerCPU((CoolerCPU) productoEspecifico);
            caracteristicas.put("Compatibilidad", compatibilidad);
            Map<String, String> coolersYDisipadores = this.obtenerCoolersYDisipadoresCoolerCPU((CoolerCPU) productoEspecifico);
            caracteristicas.put("Coolers y Disipadores", coolersYDisipadores);

        } else if (productoEspecifico instanceof MemoriaRAM) {

            Map<String, String> caracteristicasGenerales = this.obtenerCaracteristicasGeneralesMemoriaRAM((MemoriaRAM) productoEspecifico);
            caracteristicas.put("Caracteristicas Generales", caracteristicasGenerales);
            Map<String, String> coolersYDisipadores = this.obtenerCoolersYDisipadoresMemoriaRAM((MemoriaRAM) productoEspecifico);
            caracteristicas.put("Coolers y Disipadores", coolersYDisipadores);

        } else if (productoEspecifico instanceof PlacaDeVideo) {

            Map<String, String> caracteristicasGenerales = this.obtenerCaracteristicasGeneralesPlacaDeVideo((PlacaDeVideo) productoEspecifico);
            caracteristicas.put("Caracteristicas Generales", caracteristicasGenerales);
            Map<String, String> conectividad = this.obtenerConectividadPlacaDeVideo((PlacaDeVideo) productoEspecifico);
            caracteristicas.put("Conectividad", conectividad);
            Map<String, String> energia = this.obtenerEnergiaPlacaDeVideo((PlacaDeVideo) productoEspecifico);
            caracteristicas.put("Energia", energia);
            Map<String, String> extras = this.obtenerExtrasPlacaDeVideo((PlacaDeVideo) productoEspecifico);
            caracteristicas.put("Extras", extras);

        } else if (productoEspecifico instanceof Almacenamiento) {

            Map<String, String> caracteristicasGenerales = this.obtenerCaracteristicasGeneralesAlmacenamiento((Almacenamiento) productoEspecifico);
            caracteristicas.put("Caracteristicas Generales", caracteristicasGenerales);
            Map<String, String> rendimiento = this.obtenerRendimientoAlmacenamiento((Almacenamiento) productoEspecifico);
            caracteristicas.put("Rendimiento", rendimiento);

        } else if (productoEspecifico instanceof FuenteDeAlimentacion) {

            Map<String, String> caracteristicasGenerales = this.obtenerCaracteristicasGeneralesFuenteDeAlimentacion((FuenteDeAlimentacion) productoEspecifico);
            caracteristicas.put("Caracteristicas Generales", caracteristicasGenerales);
            Map<String, String> cableado = this.obtenerCableadoFuenteDeAlimentacion((FuenteDeAlimentacion) productoEspecifico);
            caracteristicas.put("Cableado", cableado);

        } else if (productoEspecifico instanceof Gabinete) {

            Map<String, String> caracteristicasGenerales = this.obtenerCaracteristicasGeneralesGabinete((Gabinete) productoEspecifico);
            caracteristicas.put("Caracteristicas Generales", caracteristicasGenerales);
            Map<String, String> conectividad = this.obtenerConectividadGabinete((Gabinete) productoEspecifico);
            caracteristicas.put("Conectividad", conectividad);
            Map<String, String> coolersYDisipadores = this.obtenerCoolersYDisipadoresGabinete((Gabinete) productoEspecifico);
            caracteristicas.put("Coolers y Disipadores", coolersYDisipadores);

        } else if (productoEspecifico instanceof Monitor) {

            Map<String, String> caracteristicasGenerales = this.obtenerCaracteristicasGeneralesMonitor((Monitor) productoEspecifico);
            caracteristicas.put("Caracteristicas Generales", caracteristicasGenerales);
            Map<String, String> conectividad = this.obtenerConectividadMonitor((Monitor) productoEspecifico);
            caracteristicas.put("Conectividad", conectividad);
            Map<String, String> pantalla = this.obtenerPantallaMonitor((Monitor) productoEspecifico);
            caracteristicas.put("Pantalla", pantalla);
            Map<String, String> dimensiones = this.obtenerDimensionesMonitor((Monitor) productoEspecifico);
            caracteristicas.put("Dimensiones", dimensiones);

        } else if (productoEspecifico instanceof Periferico) {

            Map<String, String> caracteristicasGenerales = this.obtenerCaracteristicasGeneralesPeriferico((Periferico) productoEspecifico);
            caracteristicas.put("Caracteristicas Generales", caracteristicasGenerales);
        }
    }

    /*Creacion del SubMapa de PROCESADOR*/
    private Map<String, String> obtenerCarcteristicasGeneralesProcesador(Procesador productoEspecifico) {
        Map<String, String> caracteristicasGenerales = new LinkedHashMap<>();
        caracteristicasGenerales.put("Modelo", productoEspecifico.getModelo());
        caracteristicasGenerales.put("Socket", String.valueOf(productoEspecifico.getSocket()));
        caracteristicasGenerales.put("Proceso de Fabricacion", productoEspecifico.getProcesoDeFabricacion());
        //caracteristicasGenerales.put("Graficos Integrados", ((Procesador) productoEspecifico).getIncluyeGraficosIntegrados());
        caracteristicasGenerales.put("Gráficos Integrados", productoEspecifico.getIncluyeGraficosIntegrados() != null &&
                productoEspecifico.getIncluyeGraficosIntegrados() ? "Si" : "No");
        caracteristicasGenerales.put("Chipset GPU", productoEspecifico.getChipsetGPU());
        caracteristicasGenerales.put("Familia", productoEspecifico.getFamilia());
        return caracteristicasGenerales;
    }

    private Map<String, String> obtenerEspecificacionesCPUProcesador(Procesador productoEspecifico) {
        Map<String, String> especificacionesCPU = new LinkedHashMap<>();
        especificacionesCPU.put("Nucleos", String.valueOf(productoEspecifico.getNucleos()));
        especificacionesCPU.put("Hilos", String.valueOf(productoEspecifico.getHilos()));
        especificacionesCPU.put("Frecuencia", productoEspecifico.getFrecuencia());
        especificacionesCPU.put("Frecuencia Turbo", productoEspecifico.getFrecuenciaTurbo());
        return especificacionesCPU;
    }

    private Map<String, String> obtenerCoolersYDisipadoresProcesador(Procesador productoEspecifico) {
        Map<String, String> coolersYDisipadores = new LinkedHashMap<>();
        //coolersYDisipadores.put("Incluye Cooler", ((Procesador) productoEspecifico).getIncluyeCooler());
        coolersYDisipadores.put("Incluye Cooler", productoEspecifico.getIncluyeCooler() != null &&
                productoEspecifico.getIncluyeCooler() ? "Si" : "No");
        coolersYDisipadores.put("TDP Predeterminado", productoEspecifico.getConsumo());
        return coolersYDisipadores;
    }

    private Map<String, String> obtenerMemoriaProcesador(Procesador productoEspecifico) {
        Map<String, String> memoria = new LinkedHashMap<>();
        memoria.put("L1", productoEspecifico.getL1Cache());
        memoria.put("L2", productoEspecifico.getL2Cache());
        memoria.put("L3", productoEspecifico.getL3Cache());
        return memoria;
    }

    /*Creacion del SubMapa de MOTHERBOARD*/
    private Map<String, String> obtenerCaracteristicasGeneralesMotherboard(Motherboard productoEspecifico) {
        Map<String, String> caracteristicasGenerales = new LinkedHashMap<>();
        caracteristicasGenerales.put("Socket", String.valueOf(productoEspecifico.getSocket().getNombre()));
        caracteristicasGenerales.put("Chipset Principal", productoEspecifico.getChipsetPrincipal());
        caracteristicasGenerales.put("Plataforma", productoEspecifico.getPlataforma());
        caracteristicasGenerales.put("Factor", productoEspecifico.getFactor());
        return caracteristicasGenerales;
    }

    private Map<String, String> obtenerConectividadMotherboard(Motherboard productoEspecifico) {
        Map<String, String> conectividad = new LinkedHashMap<>();
        conectividad.put("Cantidad de Slots M2", String.valueOf(productoEspecifico.getCantSlotsM2()));
        conectividad.put("Cantidad de Puertos SATA", String.valueOf(productoEspecifico.getCantPuertosSata()));
        conectividad.put("Cantidad de Puertos USB", String.valueOf(productoEspecifico.getCantPuertosUSB()));
        return conectividad;
    }

    private Map<String, String> obtenerMemoriaMotherboard(Motherboard productoEspecifico) {
        Map<String, String> memoria = new LinkedHashMap<>();
        memoria.put("Tipo de Memoria", productoEspecifico.getTipoMemoria());
        memoria.put("Cantidad Slots de Memoria RAM", String.valueOf(productoEspecifico.getCantSlotsRAM()));
        return memoria;
    }

    private Map<String, String> obtenerEnergiaMotherboard(Motherboard productoEspecifico) {
        Map<String, String> energia = new LinkedHashMap<>();
        energia.put("Consumo", productoEspecifico.getConsumo());
        energia.put("Cantidad de Conectores 24 Pines", String.valueOf(productoEspecifico.getCantConector24Pines()));
        energia.put("Cantidad de Conectores 4 Pines", String.valueOf(productoEspecifico.getCantConector4Pines()));
        return energia;
    }

    /*Creacion del SubMapa de COOLER CPU*/
    private Map<String, String> obtenerCaracteristicasGeneralesCoolerCPU(CoolerCPU productoEspecifico) {
        Map<String, String> caracteristicasGenerales = new LinkedHashMap<>();
        caracteristicasGenerales.put("Consumo", productoEspecifico.getConsumo());
        caracteristicasGenerales.put("TDP Predeterminado", productoEspecifico.getTdpPredeterminado());
        caracteristicasGenerales.put("Tipo de Disipacion", productoEspecifico.getTipoDeDisipacion());
        return caracteristicasGenerales;
    }

    private Map<String, String> obtenerCompatibilidadCoolerCPU(CoolerCPU productoEspecifico) {
        Map<String, String> compatibilidad = new LinkedHashMap<>();
        String socketsCompatibles = productoEspecifico.getSockets().stream()
                .map(Socket::getNombre).collect(Collectors.joining(", "));
        compatibilidad.put("Sockets Compatibles", socketsCompatibles);
        return compatibilidad;
    }

    private Map<String, String> obtenerCoolersYDisipadoresCoolerCPU(CoolerCPU productoEspecifico) {
        Map<String, String> coolersYDisipadores = new LinkedHashMap<>();
        coolersYDisipadores.put("Cantidad de Coolers Incluidos", String.valueOf(productoEspecifico.getCantCoolersIncluidos()));
        coolersYDisipadores.put("Tamaño Coolers", productoEspecifico.getTamanioCoolers());
        coolersYDisipadores.put("Tipo de Iluminacion", productoEspecifico.getTipoDeIlumninacion());
        coolersYDisipadores.put("Nivel Maximo de Ruido", productoEspecifico.getNivelMaximoDeRuido());
        return coolersYDisipadores;
    }

    /*Creacion del SubMapa de MEMORIA RAM*/
    private Map<String, String> obtenerCaracteristicasGeneralesMemoriaRAM(MemoriaRAM productoEspecifico) {
        Map<String, String> caracteristicasGenerales = new LinkedHashMap<>();
        caracteristicasGenerales.put("Capacidad", productoEspecifico.getCapacidad());
        caracteristicasGenerales.put("Velocidad", productoEspecifico.getVelocidad());
        caracteristicasGenerales.put("Tecnologia", productoEspecifico.getTecnologiaRAM());
        caracteristicasGenerales.put("Latencia", productoEspecifico.getLatencia());
        caracteristicasGenerales.put("Voltaje", productoEspecifico.getVoltaje());
        return caracteristicasGenerales;
    }

    private Map<String, String> obtenerCoolersYDisipadoresMemoriaRAM(MemoriaRAM productoEspecifico) {
        Map<String, String> coolersYDisipadores = new LinkedHashMap<>();
        coolersYDisipadores.put("Disipador", productoEspecifico.getDisipador() != null &&
                productoEspecifico.getDisipador() ? "Si" : "No");
        coolersYDisipadores.put("Disipador Alto", productoEspecifico.getDisipadorAlto() != null &&
                productoEspecifico.getDisipadorAlto() ? "Si" : "No");
        return coolersYDisipadores;
    }

    /*Creacion del SubMapa de PLACA DE VIDEO*/
    private Map<String, String> obtenerCaracteristicasGeneralesPlacaDeVideo(PlacaDeVideo productoEspecifico) {
        Map<String, String> caracteristicasGenerales = new LinkedHashMap<>();
        caracteristicasGenerales.put("Chipset GPU", productoEspecifico.getChipsetGPU());
        caracteristicasGenerales.put("Cantidad de Coolers", String.valueOf(productoEspecifico.getCantidadDeCoolers()));
        return caracteristicasGenerales;
    }

    private Map<String, String> obtenerConectividadPlacaDeVideo(PlacaDeVideo productoEspecifico) {
        Map<String, String> conectividad = new LinkedHashMap<>();
        conectividad.put("Cantidad de VGA", String.valueOf(productoEspecifico.getCantidadDeVGA()));
        conectividad.put("Cantidad de DVI Digital", String.valueOf(productoEspecifico.getCantidadDeDVIDigital()));
        conectividad.put("Cantidad de HDMI", String.valueOf(productoEspecifico.getCantidadDeHDMI()));
        conectividad.put("Cantidad de Displayport", String.valueOf(productoEspecifico.getCantidadDeDisplayport()));
        return conectividad;
    }

    private Map<String, String> obtenerEnergiaPlacaDeVideo(PlacaDeVideo productoEspecifico) {
        Map<String, String> energia = new LinkedHashMap<>();
        energia.put("Consumo", productoEspecifico.getConsumo());
        energia.put("Cantidad de PCIE 6 Pines", String.valueOf(productoEspecifico.getCantidadDePCIE6Pines()));
        energia.put("Cantidad de PCIE 8 Pines", String.valueOf(productoEspecifico.getCantidadDePCIE8Pines()));
        energia.put("Cantidad de PCIE 16 Pines", String.valueOf(productoEspecifico.getCantidadDePCIE16Pines()));
        energia.put("Cantidad de Adaptadores 16 Pines", String.valueOf(productoEspecifico.getCantidadDeAdaptadores16Pines()));
        return energia;
    }

    private Map<String, String> obtenerExtrasPlacaDeVideo(PlacaDeVideo productoEspecifico) {
        Map<String, String> extras = new LinkedHashMap<>();
        extras.put("Velocidad del Core Base", productoEspecifico.getVelocidadDelCoreBase());
        extras.put("Velocidad del Core Turbo", productoEspecifico.getVelocidadDelCoreTurbo());
        extras.put("Tecnologia", productoEspecifico.getTecnologiaRAM());
        extras.put("Capacidad de RAM", productoEspecifico.getCapacidadRAM());
        return extras;
    }

    /*Creacion del SubMapa de ALMACENAMIENTO*/
    private Map<String, String> obtenerCaracteristicasGeneralesAlmacenamiento(Almacenamiento productoEspecifico) {
        Map<String, String> caracteristicasGenerales = new LinkedHashMap<>();
        caracteristicasGenerales.put("Capacidad", productoEspecifico.getCapacidad());
        caracteristicasGenerales.put("Tipo de Conexion", productoEspecifico.getTipoDeConexion());
        caracteristicasGenerales.put("Consumo", productoEspecifico.getConsumo());
        caracteristicasGenerales.put("Tipo de Disco", productoEspecifico.getTipoDeDisco());
        return caracteristicasGenerales;
    }

    private Map<String, String> obtenerRendimientoAlmacenamiento(Almacenamiento productoEspecifico) {
        Map<String, String> rendimiento = new LinkedHashMap<>();
        rendimiento.put("Memoria Cache", productoEspecifico.getMemoriaCache());
        rendimiento.put("Velocidad Lectura Secuencial", productoEspecifico.getVelocidadLecturaSecuencial());
        rendimiento.put("Velocidad Escritura Secuencial", productoEspecifico.getVelocidadLecturaSecuencial());
        return rendimiento;
    }

    /*Creacion del SubMapa de FUENTE DE ALIMENTACION*/
    private Map<String, String> obtenerCaracteristicasGeneralesFuenteDeAlimentacion(FuenteDeAlimentacion productoEspecifico) {
        Map<String, String> caracteristicasGenerales = new LinkedHashMap<>();
        caracteristicasGenerales.put("Formato", productoEspecifico.getFormato());
        caracteristicasGenerales.put("Watts Nominales", productoEspecifico.getWattsNominales());
        caracteristicasGenerales.put("Watts Reales", productoEspecifico.getWattsReales());
        caracteristicasGenerales.put("Certificacion 80 Plus", productoEspecifico.getCertificacion80Plus());
        caracteristicasGenerales.put("Tipo de Cableado", productoEspecifico.getTipoDeCableado());
        return caracteristicasGenerales;
    }

    private Map<String, String> obtenerCableadoFuenteDeAlimentacion(FuenteDeAlimentacion productoEspecifico) {
        Map<String, String> cableado = new LinkedHashMap<>();
        cableado.put("Conector 24 Pines", productoEspecifico.getConector24Pines() != null &&
                productoEspecifico.getConector24Pines() ? "Si" : "No");
        cableado.put("Cantidad de Conectores CPU 4 Pines", String.valueOf(productoEspecifico.getCantConectoresCpu4Pines()));
        cableado.put("Cantidad de Conectores CPU 4 Pines Plus", String.valueOf(productoEspecifico.getCantConectoresCpu4PinesPlus()));
        cableado.put("Cantidad de Conectores CPU 6 Pines", String.valueOf(productoEspecifico.getCantConectoresCpu6Pines()));
        cableado.put("Cantidad de Conectores CPU 2 Pines Plus", String.valueOf(productoEspecifico.getCantConectoresCpu2PinesPlus()));
        cableado.put("Cantidad de Conexiones SATA", String.valueOf(productoEspecifico.getCantConexionesSata()));
        cableado.put("Cantidad de Conexiones Molex", String.valueOf(productoEspecifico.getCantConexionesMolex()));
        cableado.put("Cantidad de Conexiones PCIE 16 Pines", String.valueOf(productoEspecifico.getCantConexionesPcie16Pines()));
        return cableado;
    }

    /*Creacion del SubMapa de GABINETE*/
    private Map<String, String> obtenerCaracteristicasGeneralesGabinete(Gabinete productoEspecifico) {
        Map<String, String> caracteristicasGenerales = new LinkedHashMap<>();
        caracteristicasGenerales.put("Color", productoEspecifico.getColor());
        caracteristicasGenerales.put("Con ventana", productoEspecifico.getConVentana() != null &&
                productoEspecifico.getConVentana() ? "Si" : "No");
        caracteristicasGenerales.put("Tipo de Ventana", productoEspecifico.getTipoDeVentana());
        caracteristicasGenerales.put("Tamaño Gabinete", productoEspecifico.getTamanioGabinete());
        return caracteristicasGenerales;
    }

    private Map<String, String> obtenerConectividadGabinete(Gabinete productoEspecifico) {
        Map<String, String> conectividad = new LinkedHashMap<>();
        conectividad.put("Cantidad de Puertos USB", String.valueOf(productoEspecifico.getCantUSB()));
        conectividad.put("Audio Frontal", productoEspecifico.getAudioFrontal() != null &&
                productoEspecifico.getAudioFrontal() ? "Si" : "No");
        return conectividad;
    }

    private Map<String, String> obtenerCoolersYDisipadoresGabinete(Gabinete productoEspecifico) {
        Map<String, String> coolersYDisipadores = new LinkedHashMap<>();
        coolersYDisipadores.put("Cant de Coolers de 80mm Soportados", String.valueOf(productoEspecifico.getCantCoolerFanDe80mmSoportados()));
        coolersYDisipadores.put("Cant de Coolers de 80mm Incluidos", String.valueOf(productoEspecifico.getCantCoolerFanDe80mmIncluidos()));
        coolersYDisipadores.put("Cant de Coolers de 120mm Soportados", String.valueOf(productoEspecifico.getCantCoolerFanDe120mmSoportados()));
        coolersYDisipadores.put("Cant de Coolers de 120mm Incluidos", String.valueOf(productoEspecifico.getCantCoolerFanDe120mmIncluidos()));
        coolersYDisipadores.put("Cant de Coolers de 140mm Soportados", String.valueOf(productoEspecifico.getCantCoolerFanDe140mmSoportados()));
        coolersYDisipadores.put("Cant de Coolers de 140mm Incluidos", String.valueOf(productoEspecifico.getCantCoolerFanDe140mmIncluidos()));
        coolersYDisipadores.put("Cant de Coolers de 200mm Soportados", String.valueOf(productoEspecifico.getCantCoolerFanDe200mmSoportados()));
        coolersYDisipadores.put("Cant de Coolers de 200mm Incluidos", String.valueOf(productoEspecifico.getCantCoolerFanDe200mmIncluidos()));
        coolersYDisipadores.put("Cant de Radiadores de 240mm Soportados", String.valueOf(productoEspecifico.getCantRadiador240mmSoportados()));
        coolersYDisipadores.put("Cant de Radiadores de 280mm Soportados", String.valueOf(productoEspecifico.getCantRadiador280mmSoportados()));
        coolersYDisipadores.put("Cant de Radiadores de 360mm Soportados", String.valueOf(productoEspecifico.getCantRadiador360mmSoportados()));
        coolersYDisipadores.put("Cant de Radiadores de 420mm Soportados", String.valueOf(productoEspecifico.getCantRadiador420mmSoportados()));
        return coolersYDisipadores;
    }

    /*Creacion del SubMapa de MONITOR*/
    private Map<String, String> obtenerCaracteristicasGeneralesMonitor(Monitor productoEspecifico) {
        Map<String, String> caracteristicasGenerales = new LinkedHashMap<>();
        caracteristicasGenerales.put("Tipo de Iluminacion", productoEspecifico.getTipoDeIluminacion());
        caracteristicasGenerales.put("Tipo de Panel", productoEspecifico.getTipoDePanel());
        caracteristicasGenerales.put("Pantalla Curva", productoEspecifico.getPantallaCurva() != null &&
                productoEspecifico.getPantallaCurva() ? "Si" : "No");
        return caracteristicasGenerales;
    }

    private Map<String, String> obtenerConectividadMonitor(Monitor productoEspecifico) {
        Map<String, String> conectividad = new LinkedHashMap<>();
        conectividad.put("Puertos HDMI", String.valueOf(productoEspecifico.getPuertosHDMI()));
        conectividad.put("Puertos Display Port", String.valueOf(productoEspecifico.getPuertosDisplayPort()));
        conectividad.put("Puertos Mini Display Port", String.valueOf(productoEspecifico.getPuertosMiniDisplayPort()));
        conectividad.put("Puertos VGA", String.valueOf(productoEspecifico.getPuertosVGA()));
        conectividad.put("Puertos DVI", String.valueOf(productoEspecifico.getPuertosDVI()));
        conectividad.put("Puertos USB", String.valueOf(productoEspecifico.getPuertosUSB()));
        conectividad.put("Conector Auriculares", productoEspecifico.getConectorAuriculares() != null &&
                productoEspecifico.getConectorAuriculares() ? "Si" : "No");
        return conectividad;
    }

    private Map<String, String> obtenerPantallaMonitor(Monitor productoEspecifico) {
        Map<String, String> pantalla = new LinkedHashMap<>();
        pantalla.put("Pulgadas", productoEspecifico.getPulgadas());
        pantalla.put("Resolucion Maxima", productoEspecifico.getResolucionMaxima());
        pantalla.put("Frecuencia Maxima", productoEspecifico.getFrecuenciaMaxima());
        pantalla.put("Tiempo de Respuesta", productoEspecifico.getTiempoDeRespuesta());
        pantalla.put("Nvidia G-Sync", productoEspecifico.getNvidiaGSync() != null &&
                productoEspecifico.getNvidiaGSync() ? "Si" : "No");
        pantalla.put("AMD Freesync", productoEspecifico.getAmdFreesync() != null &&
                productoEspecifico.getAmdFreesync() ? "Si" : "No");
        return pantalla;
    }

    private Map<String, String> obtenerDimensionesMonitor(Monitor productoEspecifico) {
        Map<String, String> dimensiones = new LinkedHashMap<>();
        dimensiones.put("Ancho", productoEspecifico.getAncho());
        dimensiones.put("Alto", productoEspecifico.getAlto());
        dimensiones.put("Espesor", productoEspecifico.getEspesor());
        dimensiones.put("Curvatura", productoEspecifico.getCurvatura());
        return dimensiones;
    }

    /*Creacion del SubMapa de PERIFERICO*/
    private Map<String, String> obtenerCaracteristicasGeneralesPeriferico(Periferico productoEspecifico) {
        Map<String, String> caracteristicasGenerales = new LinkedHashMap<>();
        caracteristicasGenerales.put("USB Requeridos", String.valueOf(productoEspecifico.getUsbRequeridos()));
        caracteristicasGenerales.put("Tipo de Conexion", productoEspecifico.getTipoDeConexion());
        caracteristicasGenerales.put("Conexion Bluetooth", productoEspecifico.getConexionBluetooth() != null &&
                                                            productoEspecifico.getConexionBluetooth() ? "Si" : "No");
        caracteristicasGenerales.put("Receptor Bluetooth Incluido", productoEspecifico.getReceptorBluetoothIncluido() != null &&
                                                                    productoEspecifico.getReceptorBluetoothIncluido() ? "Si" : "No");
        caracteristicasGenerales.put("Conexion Wireless", productoEspecifico.getConexionWireless() != null &&
                                                            productoEspecifico.getConexionWireless() ? "Si" : "No");
        caracteristicasGenerales.put("Receptor Wireless Incluido", productoEspecifico.getReceptorWirelessIncluido() != null &&
                                                                    productoEspecifico.getReceptorWirelessIncluido() ? "Si" : "No");
        caracteristicasGenerales.put("Tipo de Cable", productoEspecifico.getTipoDeCable());
        caracteristicasGenerales.put("Cable Extraible", productoEspecifico.getCableExtraible() != null &&
                                                        productoEspecifico.getCableExtraible() ? "Si" : "No");
        caracteristicasGenerales.put("Largo del Cable", productoEspecifico.getLargoDelCable());
        return caracteristicasGenerales;
    }

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

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public List<String> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<String> imagenes) {
        this.imagenes = imagenes;
    }

    public Map<String, Map<String, String>> getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(Map<String, Map<String, String>> caracteristicas) {
        this.caracteristicas = caracteristicas;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ComponenteEspecificoDto that = (ComponenteEspecificoDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
