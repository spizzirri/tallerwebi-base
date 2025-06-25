package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.*;
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

    public Boolean esCompatibleConElArmado(Componente componente, ArmadoPc armadoPc) {

        // Compatibilidades
        // Motherboard
        // Cooler
        // PlacaDeVideo
        // Fuente

        Boolean esCompatible = false;

            switch (componente.getClass().getSimpleName()) {
                case "Motherboard": {
                    Procesador procesadorArmado = (Procesador) this.repositorioComponente.obtenerComponentePorId(armadoPc.getProcesador().getId());

                    esCompatible = servicioMotherboard.verificarCompatibilidadDeMotherboardConProcesador((Motherboard) componente, procesadorArmado);

                    break;
                }
                case "CoolerCPU": {

                    Procesador procesadorArmado = (Procesador) this.repositorioComponente.obtenerComponentePorId(armadoPc.getProcesador().getId());
                    Motherboard motherboardArmado = (Motherboard) this.repositorioComponente.obtenerComponentePorId(armadoPc.getMotherboard().getId());

                    esCompatible = servicioCooler.verificarCoolerIncluido((CoolerCPU) componente, procesadorArmado) // boolean = componente.precio == 0 && procesador.coolerIncluido.
                            || servicioCooler.verificarCompatibilidadDeCoolerConMotherboard(motherboardArmado, (CoolerCPU) componente);

                    break;
                }
                case "MemoriaRAM": {

                    Motherboard motherboardArmado = (Motherboard) this.repositorioComponente.obtenerComponentePorId(armadoPc.getMotherboard().getId());

                    esCompatible = servicioMotherboard.verificarCompatibilidadDeMotherboardConMemoriaRAM(motherboardArmado, (MemoriaRAM) componente); // boolean = comparar DDRR

                    break;
                }
                case "PlacaDeVideo": {

                    Procesador procesadorArmado = (Procesador) this.repositorioComponente.obtenerComponentePorId(armadoPc.getProcesador().getId());

                    esCompatible = servicioPlacaDeVideo.verificarGraficosIntegrados((PlacaDeVideo) componente, procesadorArmado)
                                    || servicioPlacaDeVideo.verificarPrecioMayorACero((PlacaDeVideo) componente);

                    break;
                }
                case "Almacenamiento": {

                    Motherboard motherboardArmado = (Motherboard) this.repositorioComponente.obtenerComponentePorId(armadoPc.getMotherboard().getId());

                    esCompatible = servicioMotherboard.verificarCompatibilidadDeMotherboardConTipoDeConexionDeAlmacenamiento(motherboardArmado, (Almacenamiento) componente);

                    break;
                }
                case "FuenteDeAlimentacion": {

                    esCompatible = servicioFuente.verificarCompatibilidadDeFuenteConWatsDelArmado((FuenteDeAlimentacion) componente, completarEntidadArmadoPcParaEvaluarFuente(armadoPc));
                    // hacer sumatoria de wats del armado hasta ahora y verificar que la fuente tenga capacidad superior.
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

        private ArmadoPc completarEntidadArmadoPcParaEvaluarFuente(ArmadoPc armadoPcEntidad) {

        armadoPcEntidad.setProcesador((armadoPcEntidad.getProcesador() != null) ? (Procesador)this.repositorioComponente.obtenerComponentePorId(armadoPcEntidad.getProcesador().getId()) : null);
        armadoPcEntidad.setMotherboard((armadoPcEntidad.getMotherboard() != null) ? (Motherboard)this.repositorioComponente.obtenerComponentePorId(armadoPcEntidad.getMotherboard().getId()) : null);
        armadoPcEntidad.setCoolerCPU((armadoPcEntidad.getCoolerCPU() != null) ? (CoolerCPU)this.repositorioComponente.obtenerComponentePorId(armadoPcEntidad.getCoolerCPU().getId()) : null);
        armadoPcEntidad.setPlacaDeVideo((armadoPcEntidad.getPlacaDeVideo() != null) ? (PlacaDeVideo)this.repositorioComponente.obtenerComponentePorId(armadoPcEntidad.getPlacaDeVideo().getId()) : null);

        if (!armadoPcEntidad.getAlmacenamiento().isEmpty()) armadoPcEntidad.setAlmacenamiento(this.obtenerListaDeComponentesEntidadConInformacionCompleta(armadoPcEntidad.getAlmacenamiento(), Almacenamiento.class));

        return armadoPcEntidad;
    }

    private <T extends Componente> List<T>  obtenerListaDeComponentesEntidadConInformacionCompleta(List<T> componentesACompletar, Class<T> tipoDeDato) {
        List<T> entidadesCompletas = new ArrayList<>();
        for( T componente : componentesACompletar ) {
            entidadesCompletas.add(tipoDeDato.cast(this.repositorioComponente.obtenerComponentePorId(componente.getId())));
        }
        return entidadesCompletas;
    }

}
