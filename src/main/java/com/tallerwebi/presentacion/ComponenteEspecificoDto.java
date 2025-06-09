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
        /*if (productoEspecifico.getImagenes() != null) {
            this.imagenes = new ArrayList<>();
            for(int i = 0; i < productoEspecifico.getImagenes().size(); i++){
                imagenes.add(productoEspecifico.getImagenes().get(i).getUrlImagen());
            }
        }*/
        this.caracteristicas = new LinkedHashMap<>();

        if (productoEspecifico instanceof Procesador) {

            Map<String, String> caracteristicasGenerales = new LinkedHashMap<>();
            caracteristicasGenerales.put("Modelo", ((Procesador) productoEspecifico).getModelo());
            caracteristicasGenerales.put("Socket", String.valueOf(((Procesador) productoEspecifico).getSocket()));
            caracteristicasGenerales.put("Proceso de Fabricacion", ((Procesador) productoEspecifico).getProcesoDeFabricacion());
            //caracteristicasGenerales.put("Graficos Integrados", ((Procesador) productoEspecifico).getIncluyeGraficosIntegrados());
            caracteristicasGenerales.put("Gráficos Integrados", ((Procesador) productoEspecifico).getIncluyeGraficosIntegrados() != null &&
                                                                ((Procesador) productoEspecifico).getIncluyeGraficosIntegrados() ? "Si" : "No");
            caracteristicasGenerales.put("Chipset GPU", ((Procesador) productoEspecifico).getChipsetGPU());
            caracteristicasGenerales.put("Familia", ((Procesador) productoEspecifico).getFamilia());
            caracteristicas.put("Caracteristicas generales", caracteristicasGenerales);

            Map<String, String> especificacionesCPU = new LinkedHashMap<>();
            especificacionesCPU.put("Nucleos", String.valueOf(((Procesador) productoEspecifico).getNucleos()));
            especificacionesCPU.put("Hilos", String.valueOf(((Procesador) productoEspecifico).getHilos()));
            especificacionesCPU.put("Frecuencia", ((Procesador) productoEspecifico).getFrecuencia());
            especificacionesCPU.put("Frecuencia Turbo", ((Procesador) productoEspecifico).getFrecuenciaTurbo());
            caracteristicas.put("Especificaciones CPU", especificacionesCPU);

            Map<String, String> coolersYDisipadores = new LinkedHashMap<>();
            //coolersYDisipadores.put("Incluye Cooler", ((Procesador) productoEspecifico).getIncluyeCooler());
            coolersYDisipadores.put("Incluye Cooler", ((Procesador) productoEspecifico).getIncluyeCooler() != null &&
                                                        ((Procesador) productoEspecifico).getIncluyeCooler() ? "Si" : "No");
            coolersYDisipadores.put("TDP Predeterminado", ((Procesador) productoEspecifico).getTdpPredeterminado());
            caracteristicas.put("Coolers y Disipadores", coolersYDisipadores);

            Map<String, String> memoria = new LinkedHashMap<>();
            memoria.put("L1", ((Procesador) productoEspecifico).getL1Cache());
            memoria.put("L2", ((Procesador) productoEspecifico).getL2Cache());
            memoria.put("L3", ((Procesador) productoEspecifico).getL3Cache());
            caracteristicas.put("Memoria", memoria);

        } else if (productoEspecifico instanceof Motherboard) {

            Map<String, String> caracteristicasGenerales = new LinkedHashMap<>();
            caracteristicasGenerales.put("Socket", String.valueOf(((Motherboard) productoEspecifico).getSocket().getNombre()));
            caracteristicasGenerales.put("Chipset Principal", ((Motherboard) productoEspecifico).getChipsetPrincipal());
            caracteristicasGenerales.put("Plataforma", ((Motherboard) productoEspecifico).getPlataforma());
            caracteristicasGenerales.put("Factor", ((Motherboard) productoEspecifico).getFactor());
            caracteristicas.put("Caracteristicas Generales", caracteristicasGenerales);

            Map<String, String> conectividad = new LinkedHashMap<>();
            conectividad.put("Cantidad de Slots M2", String.valueOf(((Motherboard) productoEspecifico).getCantSlotsM2()));
            conectividad.put("Cantidad de Puertos SATA", String.valueOf(((Motherboard) productoEspecifico).getCantPuertosSata()));
            conectividad.put("Cantidad de Puertos USB", String.valueOf(((Motherboard) productoEspecifico).getCantPuertosUSB()));
            caracteristicas.put("Conectividad", conectividad);

            Map<String, String> memoria = new LinkedHashMap<>();
            memoria.put("Tipo de Memoria", ((Motherboard) productoEspecifico).getTipoMemoria());
            memoria.put("Cantidad Slots de Memoria RAM", String.valueOf(((Motherboard) productoEspecifico).getCantSlotsRAM()));
            caracteristicas.put("Memoria", memoria);

            Map<String, String> energia = new LinkedHashMap<>();
            energia.put("Consumo", ((Motherboard) productoEspecifico).getConsumo());
            energia.put("Cantidad de Conectores 24 Pines", String.valueOf(((Motherboard) productoEspecifico).getCantConector24Pines()));
            energia.put("Cantidad de Conectores 4 Pines", String.valueOf(((Motherboard) productoEspecifico).getCantConector4Pines()));
            caracteristicas.put("Energia", energia);

        } else if (productoEspecifico instanceof CoolerCPU) {

            Map<String, String> caracteristicasGenerales = new LinkedHashMap<>();
            caracteristicasGenerales.put("Consumo", ((CoolerCPU) productoEspecifico).getConsumo());
            caracteristicasGenerales.put("TDP Predeterminado", ((CoolerCPU) productoEspecifico).getTdpPredeterminado());
            caracteristicasGenerales.put("Tipo de Disipacion", ((CoolerCPU) productoEspecifico).getTipoDeDisipacion());
            caracteristicas.put("Caracteristicas Generales", caracteristicasGenerales);

            Map<String, String> compatibilidad = new LinkedHashMap<>();
            String socketsCompatibles = ((CoolerCPU) productoEspecifico).getSockets().stream()
                                        .map(Socket::getNombre).collect(Collectors.joining(", "));
            compatibilidad.put("Sockets Compatibles", socketsCompatibles);
            caracteristicas.put("Compatibilidad", compatibilidad);

            Map<String, String> coolersYDisipadores = new LinkedHashMap<>();
            coolersYDisipadores.put("Cantidad de Coolers Incluidos", String.valueOf(((CoolerCPU) productoEspecifico).getCantCoolersIncluidos()));
            coolersYDisipadores.put("Tamaño Coolers", ((CoolerCPU) productoEspecifico).getTamanioCoolers());
            coolersYDisipadores.put("Tipo de Iluminacion", ((CoolerCPU) productoEspecifico).getTipoDeIlumninacion());
            coolersYDisipadores.put("Nivel Maximo de Ruido", ((CoolerCPU) productoEspecifico).getNivelMaximoDeRuido());
            caracteristicas.put("Coolers y Disipadores", coolersYDisipadores);

        } else if (productoEspecifico instanceof MemoriaRAM) {

            Map<String, String> caracteristicasGenerales = new LinkedHashMap<>();
            caracteristicasGenerales.put("Capacidad", ((MemoriaRAM) productoEspecifico).getCapacidad());
            caracteristicasGenerales.put("Velocidad", ((MemoriaRAM) productoEspecifico).getVelocidad());
            caracteristicasGenerales.put("Tecnologia", ((MemoriaRAM) productoEspecifico).getTecnologiaRAM());
            caracteristicasGenerales.put("Latencia", ((MemoriaRAM) productoEspecifico).getLatencia());
            caracteristicasGenerales.put("Voltaje", ((MemoriaRAM) productoEspecifico).getVoltaje());
            caracteristicas.put("Caracteristicas Generales", caracteristicasGenerales);

            Map<String, String> coolersYDisipadores = new LinkedHashMap<>();
            coolersYDisipadores.put("Disipador", ((MemoriaRAM) productoEspecifico).getDisipador() != null &&
                                                    ((MemoriaRAM) productoEspecifico).getDisipador() ? "Si" : "No");
            coolersYDisipadores.put("Disipador Alto", ((MemoriaRAM) productoEspecifico).getDisipadorAlto() != null &&
                                                        ((MemoriaRAM) productoEspecifico).getDisipadorAlto() ? "Si" : "No");
            caracteristicas.put("Coolers y Disipadores", coolersYDisipadores);

        } else if (productoEspecifico instanceof PlacaDeVideo) {

            Map<String, String> caracteristicasGenerales = new LinkedHashMap<>();
            caracteristicasGenerales.put("Chipset GPU", ((PlacaDeVideo) productoEspecifico).getChipsetGPU());
            caracteristicasGenerales.put("Cantidad de Coolers", String.valueOf(((PlacaDeVideo) productoEspecifico).getCantidadDeCoolers()));
            caracteristicas.put("Caracteristicas Generales", caracteristicasGenerales);

            Map<String, String> conectividad = new LinkedHashMap<>();
            conectividad.put("Cantidad de VGA", String.valueOf(((PlacaDeVideo) productoEspecifico).getCantidadDeVGA()));
            conectividad.put("Cantidad de DVI Digital", String.valueOf(((PlacaDeVideo) productoEspecifico).getCantidadDeDVIDigital()));
            conectividad.put("Cantidad de HDMI", String.valueOf(((PlacaDeVideo) productoEspecifico).getCantidadDeHDMI()));
            conectividad.put("Cantidad de Displayport", String.valueOf(((PlacaDeVideo) productoEspecifico).getCantidadDeDisplayport()));
            caracteristicas.put("Conectividad", conectividad);

            Map<String, String> energia = new LinkedHashMap<>();
            energia.put("Consumo", ((PlacaDeVideo) productoEspecifico).getConsumo());
            energia.put("Cantidad de PCIE 6 Pines", String.valueOf(((PlacaDeVideo) productoEspecifico).getCantidadDePCIE6Pines()));
            energia.put("Cantidad de PCIE 8 Pines", String.valueOf(((PlacaDeVideo) productoEspecifico).getCantidadDePCIE8Pines()));
            energia.put("Cantidad de PCIE 16 Pines", String.valueOf(((PlacaDeVideo) productoEspecifico).getCantidadDePCIE16Pines()));
            energia.put("Cantidad de Adaptadores 16 Pines", String.valueOf(((PlacaDeVideo) productoEspecifico).getCantidadDeAdaptadores16Pines()));
            caracteristicas.put("Energia", energia);

            Map<String, String> extras = new LinkedHashMap<>();
            extras.put("Velocidad del Core Base", ((PlacaDeVideo) productoEspecifico).getVelocidadDelCoreBase());
            extras.put("Velocidad del Core Turbo", ((PlacaDeVideo) productoEspecifico).getVelocidadDelCoreTurbo());
            extras.put("Tecnologia", ((PlacaDeVideo) productoEspecifico).getTecnologiaRAM());
            extras.put("Capacidad de RAM", ((PlacaDeVideo) productoEspecifico).getCapacidadRAM());
            caracteristicas.put("Extras", extras);

        } else if (productoEspecifico instanceof Almacenamiento) {

            Map<String, String> caracteristicasGenerales = new LinkedHashMap<>();
            caracteristicasGenerales.put("Capacidad", ((Almacenamiento) productoEspecifico).getCapacidad());
            caracteristicasGenerales.put("Tipo de Conexion", ((Almacenamiento) productoEspecifico).getTipoDeConexion());
            caracteristicasGenerales.put("Consumo", ((Almacenamiento) productoEspecifico).getConsumo());
            caracteristicasGenerales.put("Tipo de Disco", ((Almacenamiento) productoEspecifico).getTipoDeDisco());
            caracteristicas.put("Caracteristicas Generales", caracteristicasGenerales);

            Map<String, String> rendimiento = new LinkedHashMap<>();
            rendimiento.put("Memoria Cache", ((Almacenamiento) productoEspecifico).getMemoriaCache());
            rendimiento.put("Velocidad Lectura Secuencial", ((Almacenamiento) productoEspecifico).getVelocidadLecturaSecuencial());
            rendimiento.put("Velocidad Escritura Secuencial", ((Almacenamiento) productoEspecifico).getVelocidadLecturaSecuencial());
            caracteristicas.put("Rendimiento", rendimiento);

        } else if (productoEspecifico instanceof FuenteDeAlimentacion) {

            Map<String, String> caracteristicasGenerales = new LinkedHashMap<>();
            caracteristicasGenerales.put("Formato", ((FuenteDeAlimentacion) productoEspecifico).getFormato());
            caracteristicasGenerales.put("Watts Nominales", ((FuenteDeAlimentacion) productoEspecifico).getWattsNominales());
            caracteristicasGenerales.put("Watts Reales", ((FuenteDeAlimentacion) productoEspecifico).getWattsReales());
            caracteristicasGenerales.put("Certificacion 80 Plus", ((FuenteDeAlimentacion) productoEspecifico).getCertificacion80Plus());
            caracteristicasGenerales.put("Tipo de Cableado", ((FuenteDeAlimentacion) productoEspecifico).getTipoDeCableado());
            caracteristicas.put("Caracteristicas Generales", caracteristicasGenerales);

            Map<String, String> cableado = new LinkedHashMap<>();
            cableado.put("Conector 24 Pines", ((FuenteDeAlimentacion) productoEspecifico).getConector24Pines() != null &&
                                                ((FuenteDeAlimentacion) productoEspecifico).getConector24Pines() ? "Si" : "No");
            cableado.put("Cantidad de Conectores CPU 4 Pines", String.valueOf(((FuenteDeAlimentacion) productoEspecifico).getCantConectoresCpu4Pines()));
            cableado.put("Cantidad de Conectores CPU 4 Pines Plus", String.valueOf(((FuenteDeAlimentacion) productoEspecifico).getCantConectoresCpu4PinesPlus()));
            cableado.put("Cantidad de Conectores CPU 6 Pines", String.valueOf(((FuenteDeAlimentacion) productoEspecifico).getCantConectoresCpu6Pines()));
            cableado.put("Cantidad de Conectores CPU 2 Pines Plus", String.valueOf(((FuenteDeAlimentacion) productoEspecifico).getCantConectoresCpu2PinesPlus()));
            cableado.put("Cantidad de Conexiones SATA", String.valueOf(((FuenteDeAlimentacion) productoEspecifico).getCantConexionesSata()));
            cableado.put("Cantidad de Conexiones Molex", String.valueOf(((FuenteDeAlimentacion) productoEspecifico).getCantConexionesMolex()));
            cableado.put("Cantidad de Conexiones PCIE 16 Pines", String.valueOf(((FuenteDeAlimentacion) productoEspecifico).getCantConexionesPcie16Pines()));
            caracteristicas.put("Cableado", cableado);

        } else if (productoEspecifico instanceof Gabinete) {

            Map<String, String> caracteristicasGenerales = new LinkedHashMap<>();
            caracteristicasGenerales.put("Color", ((Gabinete) productoEspecifico).getColor());
            caracteristicasGenerales.put("Con ventana", ((Gabinete) productoEspecifico).getConVentana() != null &&
                                                        ((Gabinete) productoEspecifico).getConVentana() ? "Si" : "No");
            caracteristicasGenerales.put("Tipo de Ventana", ((Gabinete) productoEspecifico).getTipoDeVentana());
            caracteristicasGenerales.put("Tamaño Gabinete", ((Gabinete) productoEspecifico).getTamanioGabinete());
            caracteristicas.put("Caracteristicas Generales", caracteristicasGenerales);

            Map<String, String> conectividad = new LinkedHashMap<>();
            conectividad.put("Cantidad de Puertos USB", String.valueOf(((Gabinete) productoEspecifico).getCantUSB()));
            conectividad.put("Audio Frontal", ((Gabinete) productoEspecifico).getAudioFrontal() != null &&
                                                ((Gabinete) productoEspecifico).getAudioFrontal() ? "Si" : "No");
            caracteristicas.put("Conectividad", conectividad);

            Map<String, String> coolersYDisipadores = new LinkedHashMap<>();
            coolersYDisipadores.put("Cantidad de Coolers de 80mm Soportados", String.valueOf(((Gabinete) productoEspecifico).getCantCoolerFanDe80mmSoportados()));
            coolersYDisipadores.put("Cantidad de Coolers de 80mm Incluidos", String.valueOf(((Gabinete) productoEspecifico).getCantCoolerFanDe80mmIncluidos()));
            coolersYDisipadores.put("Cantidad de Coolers de 120mm Soportados", String.valueOf(((Gabinete) productoEspecifico).getCantCoolerFanDe120mmSoportados()));
            coolersYDisipadores.put("Cantidad de Coolers de 120mm Incluidos", String.valueOf(((Gabinete) productoEspecifico).getCantCoolerFanDe120mmIncluidos()));
            coolersYDisipadores.put("Cantidad de Coolers de 140mm Soportados", String.valueOf(((Gabinete) productoEspecifico).getCantCoolerFanDe140mmSoportados()));
            coolersYDisipadores.put("Cantidad de Coolers de 140mm Incluidos", String.valueOf(((Gabinete) productoEspecifico).getCantCoolerFanDe140mmIncluidos()));
            coolersYDisipadores.put("Cantidad de Coolers de 200mm Soportados", String.valueOf(((Gabinete) productoEspecifico).getCantCoolerFanDe200mmSoportados()));
            coolersYDisipadores.put("Cantidad de Coolers de 200mm Incluidos", String.valueOf(((Gabinete) productoEspecifico).getCantCoolerFanDe200mmIncluidos()));
            coolersYDisipadores.put("Cantidad de Radiadores de 240mm Soportados", String.valueOf(((Gabinete) productoEspecifico).getCantRadiador240mmSoportados()));
            coolersYDisipadores.put("Cantidad de Radiadores de 280mm Soportados", String.valueOf(((Gabinete) productoEspecifico).getCantRadiador280mmSoportados()));
            coolersYDisipadores.put("Cantidad de Radiadores de 360mm Soportados", String.valueOf(((Gabinete) productoEspecifico).getCantRadiador360mmSoportados()));
            coolersYDisipadores.put("Cantidad de Radiadores de 420mm Soportados", String.valueOf(((Gabinete) productoEspecifico).getCantRadiador420mmSoportados()));
            caracteristicas.put("Coolers y Disipadores", coolersYDisipadores);

        } else if (productoEspecifico instanceof Monitor) {



        } else if (productoEspecifico instanceof Periferico) {

        }
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
}
