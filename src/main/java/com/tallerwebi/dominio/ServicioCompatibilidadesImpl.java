package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.dominio.excepcion.ComponenteDeterminateDelArmadoEnNullException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service("servicioCompatibilidades")
@Transactional
public class ServicioCompatibilidadesImpl implements ServicioCompatibilidades {

    private ServicioMotherboard servicioMotherboard;
    private ServicioCooler servicioCooler;
    private ServicioPlacaDeVideo servicioPlacaDeVideo;
    private ServicioFuente servicioFuente;
    private RepositorioComponente repositorioComponente;

    @Autowired
    public ServicioCompatibilidadesImpl(ServicioMotherboard servicioMotherboard,
                                        ServicioCooler servicioCooler,
                                        ServicioPlacaDeVideo servicioPlacaDeVideo,
                                        ServicioFuente servicioFuente,
                                        RepositorioComponente repositorioComponente) {
    this.servicioMotherboard = servicioMotherboard;
    this.servicioCooler = servicioCooler;
    this.servicioPlacaDeVideo = servicioPlacaDeVideo;
    this.servicioFuente = servicioFuente;
    this.repositorioComponente = repositorioComponente;
    }

    public Boolean esCompatibleConElArmado(Componente componente, ArmadoPc armadoPc) throws ComponenteDeterminateDelArmadoEnNullException {

        // Compatibilidades
        // Motherboard
        // Cooler
        // PlacaDeVideo
        // Fuente

        Boolean esCompatible = false;

            switch (componente.getClass().getSimpleName()) {
                case "Motherboard": {

                    if (armadoPc.getProcesador() == null) throw new ComponenteDeterminateDelArmadoEnNullException("Debe seleccionar un Procesador para poder elegir una Motherboard.");

                    Procesador procesadorArmado = (Procesador) this.repositorioComponente.obtenerComponentePorId(armadoPc.getProcesador().getId());

                    esCompatible = servicioMotherboard.verificarCompatibilidadDeMotherboardConProcesador((Motherboard) componente, procesadorArmado);

                    break;
                }
                case "CoolerCPU": {

                    if (armadoPc.getProcesador() == null) throw new ComponenteDeterminateDelArmadoEnNullException("Debe seleccionar un Procesador para poder elegir un Cooler.");
                    if (armadoPc.getMotherboard() == null) throw new ComponenteDeterminateDelArmadoEnNullException("Debe seleccionar una Motherboard para poder elegir un Cooler.");

                    Procesador procesadorArmado = (Procesador) this.repositorioComponente.obtenerComponentePorId(armadoPc.getProcesador().getId());
                    Motherboard motherboardArmado = (Motherboard) this.repositorioComponente.obtenerComponentePorId(armadoPc.getMotherboard().getId());

                    esCompatible = servicioCooler.verificarCoolerIncluido((CoolerCPU) componente, procesadorArmado) // boolean = componente.precio == 0 && procesador.coolerIncluido.
                            || servicioCooler.verificarCompatibilidadDeCoolerConMotherboard(motherboardArmado, (CoolerCPU) componente);

                    break;
                }
                case "MemoriaRAM": {

                    if (armadoPc.getMotherboard() == null) throw new ComponenteDeterminateDelArmadoEnNullException("Debe seleccionar una Motherboard para poder elegir un Memoria RAM.");

                    Motherboard motherboardArmado = (Motherboard) this.repositorioComponente.obtenerComponentePorId(armadoPc.getMotherboard().getId());

                    esCompatible = servicioMotherboard.verificarCompatibilidadDeMotherboardConMemoriaRAM(motherboardArmado, (MemoriaRAM) componente); // boolean = comparar DDRR

                    break;
                }
                case "PlacaDeVideo": {

                    if (armadoPc.getProcesador() == null) throw new ComponenteDeterminateDelArmadoEnNullException("Debe seleccionar un Procesador para poder elegir una Placa de Video.");
                    // analizar si despues comparar el ddr de la mother

                    Procesador procesadorArmado = (Procesador) this.repositorioComponente.obtenerComponentePorId(armadoPc.getProcesador().getId());

                    esCompatible = servicioPlacaDeVideo.verificarGraficosIntegrados((PlacaDeVideo) componente, procesadorArmado)
                                    || servicioPlacaDeVideo.verificarPrecioMayorACero((PlacaDeVideo) componente);

                    break;
                }
                case "Almacenamiento": {

                    if (armadoPc.getMotherboard() == null) throw new ComponenteDeterminateDelArmadoEnNullException("Debe seleccionar una Motherboard para poder elegir el Almacenamiento.");

                    Motherboard motherboardArmado = (Motherboard) this.repositorioComponente.obtenerComponentePorId(armadoPc.getMotherboard().getId());

                    esCompatible = servicioMotherboard.verificarCompatibilidadDeMotherboardConTipoDeConexionDeAlmacenamiento(motherboardArmado, (Almacenamiento) componente);

                    break;
                }
                case "FuenteDeAlimentacion": {

                    esCompatible = servicioFuente.verificarCompatibilidadDeFuenteConWatsDelArmado((FuenteDeAlimentacion) componente, completarEntidadArmadoPcParaEvaluarFuente(armadoPc));

                    break;
                }
//                case "Gabinete":
//                    esCompatible = true; //servicioGabinete que compare la cantidad de ventiladores del radiador del coolerCPU?
//                    break;

                default: // A este entrarian Procesador(porque entra directo en el paso a paso) / Monitor / y Periferico
                    esCompatible = true;
                    break;
            }
        return esCompatible;
    }

    @Override
    public ArmadoPc completarEntidadArmadoPcParaEvaluarFuente(ArmadoPc armadoPcEntidad) {

        armadoPcEntidad.setProcesador((armadoPcEntidad.getProcesador() != null) ? (Procesador)this.repositorioComponente.obtenerComponentePorId(armadoPcEntidad.getProcesador().getId()) : null);
        armadoPcEntidad.setMotherboard((armadoPcEntidad.getMotherboard() != null) ? (Motherboard)this.repositorioComponente.obtenerComponentePorId(armadoPcEntidad.getMotherboard().getId()) : null);
        armadoPcEntidad.setCoolerCPU((armadoPcEntidad.getCoolerCPU() != null) ? (CoolerCPU)this.repositorioComponente.obtenerComponentePorId(armadoPcEntidad.getCoolerCPU().getId()) : null);
        armadoPcEntidad.setPlacaDeVideo((armadoPcEntidad.getPlacaDeVideo() != null) ? (PlacaDeVideo)this.repositorioComponente.obtenerComponentePorId(armadoPcEntidad.getPlacaDeVideo().getId()) : null);

        if (armadoPcEntidad.getAlmacenamiento() != null && !armadoPcEntidad.getAlmacenamiento().isEmpty()) armadoPcEntidad.setAlmacenamiento(this.obtenerListaDeComponentesEntidadConInformacionCompleta(armadoPcEntidad.getAlmacenamiento(), Almacenamiento.class));

        return armadoPcEntidad;
    }

    @Override
    public Integer obtenerWattsDeArmado(ArmadoPc armadoPc) {
        return this.servicioFuente.obtenerWatsTotales(armadoPc);
    }

    private <T extends Componente> List<T>  obtenerListaDeComponentesEntidadConInformacionCompleta(List<T> componentesACompletar, Class<T> tipoDeDato) {
        List<T> entidadesCompletas = new ArrayList<>();
        for( T componente : componentesACompletar ) {
            entidadesCompletas.add(tipoDeDato.cast(this.repositorioComponente.obtenerComponentePorId(componente.getId())));
        }
        return entidadesCompletas;
    }

}
