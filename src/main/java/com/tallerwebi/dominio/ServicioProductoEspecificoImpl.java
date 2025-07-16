package com.tallerwebi.dominio;


import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.presentacion.ComponenteEspecificoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service("servicioProductoEspecifico")
@Transactional
public class ServicioProductoEspecificoImpl implements ServicioProductoEspecifico {

    private RepositorioComponente repositorioComponente;

    @Autowired
    public ServicioProductoEspecificoImpl(RepositorioComponente repositorioComponente) {
        this.repositorioComponente = repositorioComponente;
    }

    @Override
    public Componente obtenerComponentePorId(Long idComponente) {
        return this.repositorioComponente.buscarComponenteConImagenesPorId(idComponente);
    }

    @Override
    public List<ComponenteEspecificoDto> obtenerComponentesAcomparar(Long idComponente) {
        //componente
        Componente componenteActual = this.obtenerComponentePorId(idComponente);

        List<ComponenteEspecificoDto> componentesADevolver = new ArrayList<>();
        List<ComponenteEspecificoDto> todosLosComponentesComoPrimeraOpcion = new ArrayList<>();
        List<ComponenteEspecificoDto> todosLosComponentesComoSegundaOpcion = new ArrayList<>();

        componentesADevolver.add(new ComponenteEspecificoDto(componenteActual));
        todosLosComponentesComoPrimeraOpcion.add(new ComponenteEspecificoDto(componenteActual));
        todosLosComponentesComoSegundaOpcion.add(new ComponenteEspecificoDto(componenteActual));
        List <Componente> componentesAComparar = repositorioComponente.obtenerComponentesPorTipoEnStock(componenteActual.getClass().getSimpleName());

        // si es procesador
        if (componenteActual instanceof Procesador) {
            for (Componente componente : componentesAComparar) {
                if (!componente.equals(componenteActual)
                        && ((Procesador) componente).getFamilia().equals(((Procesador) componenteActual).getFamilia())
                        && ((Procesador) componente).getSocket().equals(((Procesador) componenteActual).getSocket())) {
                    componentesADevolver.add( new ComponenteEspecificoDto(componente));
                }
                if (!componente.equals(componenteActual)
                        && componente.getMarca().equals(componenteActual.getMarca())) {
                    //Son los componentes de la misma marca y tipo
                    todosLosComponentesComoPrimeraOpcion.add(new ComponenteEspecificoDto(componente));
                } else if (!componente.equals(componenteActual)) {
                    //Son todos los componentes del mismo tipo
                    todosLosComponentesComoSegundaOpcion.add(new ComponenteEspecificoDto(componente));
                }
            }
            if (componentesADevolver.size() != 1) {
                return componentesADevolver;
            } else if (componentesADevolver.size() == 1 && todosLosComponentesComoPrimeraOpcion.size() != 1) {
                return todosLosComponentesComoPrimeraOpcion;
            } else {
                return todosLosComponentesComoSegundaOpcion;
            }
        }

        // si es perifericos
        if (componenteActual instanceof Periferico) {
            //Separacion para obtener tipo de periferico del componente actual
            String[] partesNombreActual = componenteActual.getNombre().split(" ");
            String nombrePeriferico = partesNombreActual.length > 0 ? partesNombreActual[0].toLowerCase() : "";
            for (Componente componente : componentesAComparar) {
                //Separacion para obtener tipo de periferico de los componentes que se recorren
                String[] partesNombreComponente = componente.getNombre().split(" ");
                String nombrePerifericoComponente = partesNombreComponente.length > 0 ? partesNombreComponente[0].toLowerCase() : "";
                if (!componente.equals(componenteActual)
                        && nombrePerifericoComponente.equals(nombrePeriferico)
                        && ((Periferico) componente).getTipoDeConexion().equals(((Periferico) componenteActual).getTipoDeConexion())) {
                    componentesADevolver.add(new ComponenteEspecificoDto(componente));
                }
                if (!componente.equals(componenteActual)
                        && componente.getMarca().equals(componenteActual.getMarca())) {
                    //Son los componentes de la misma marca y tipo
                    todosLosComponentesComoPrimeraOpcion.add(new ComponenteEspecificoDto(componente));
                } else if (!componente.equals(componenteActual)) {
                    //Son todos los componentes del mismo tipo
                    todosLosComponentesComoSegundaOpcion.add(new ComponenteEspecificoDto(componente));
                }
            }
            if (componentesADevolver.size() != 1) {
                return componentesADevolver;
            } else if (componentesADevolver.size() == 1 && todosLosComponentesComoPrimeraOpcion.size() != 1) {
                return todosLosComponentesComoPrimeraOpcion;
            } else {
                return todosLosComponentesComoSegundaOpcion;
            }
        }

        // si es fuente de alimentacion
        if (componenteActual instanceof FuenteDeAlimentacion) {
            for (Componente componente : componentesAComparar) {
                if (!componente.equals(componenteActual)
                        && ((FuenteDeAlimentacion) componente).getWattsNominales().equals(((FuenteDeAlimentacion) componenteActual).getWattsNominales())
                        && ((FuenteDeAlimentacion) componente).getCertificacion80Plus().equals(((FuenteDeAlimentacion) componenteActual).getCertificacion80Plus())) {
                    componentesADevolver.add(new ComponenteEspecificoDto(componente));
                }
                if (!componente.equals(componenteActual)
                        && componente.getMarca().equals(componenteActual.getMarca())) {
                    //Son los componentes de la misma marca y tipo
                    todosLosComponentesComoPrimeraOpcion.add(new ComponenteEspecificoDto(componente));
                } else if (!componente.equals(componenteActual)) {
                    //Son todos los componentes del mismo tipo
                    todosLosComponentesComoSegundaOpcion.add(new ComponenteEspecificoDto(componente));
                }
            }
            if (componentesADevolver.size() != 1) {
                return componentesADevolver;
            } else if (componentesADevolver.size() == 1 && todosLosComponentesComoPrimeraOpcion.size() != 1) {
                return todosLosComponentesComoPrimeraOpcion;
            } else {
                return todosLosComponentesComoSegundaOpcion;
            }
        }

        // si es memoria ram
        if (componenteActual instanceof MemoriaRAM) {
            for (Componente componente : componentesAComparar) {
                if (!componente.equals(componenteActual)
                        && ((MemoriaRAM) componente).getCapacidad().equals(((MemoriaRAM) componenteActual).getCapacidad())
                        && ((MemoriaRAM) componente).getTecnologiaRAM().equals(((MemoriaRAM) componenteActual).getTecnologiaRAM())) {
                    componentesADevolver.add( new ComponenteEspecificoDto(componente));
                }
                if (!componente.equals(componenteActual)
                        && componente.getMarca().equals(componenteActual.getMarca())) {
                    //Son los componentes de la misma marca y tipo
                    todosLosComponentesComoPrimeraOpcion.add(new ComponenteEspecificoDto(componente));
                } else if (!componente.equals(componenteActual)) {
                    //Son todos los componentes del mismo tipo
                    todosLosComponentesComoSegundaOpcion.add(new ComponenteEspecificoDto(componente));
                }
            }
            if (componentesADevolver.size() != 1) {
                return componentesADevolver;
            } else if (componentesADevolver.size() == 1 && todosLosComponentesComoPrimeraOpcion.size() != 1) {
                return todosLosComponentesComoPrimeraOpcion;
            } else {
                return todosLosComponentesComoSegundaOpcion;
            }
        }

        // si es placa de video
        if (componenteActual instanceof PlacaDeVideo) {
            for (Componente componente : componentesAComparar) {
                if (!componente.equals(componenteActual)
                        && componente.getPrecio() > 0.0
                        && ((PlacaDeVideo) componente).getCapacidadRAM() != null
                        && ((PlacaDeVideo) componente).getCapacidadRAM().equals(((PlacaDeVideo) componenteActual).getCapacidadRAM())) {
                    componentesADevolver.add( new ComponenteEspecificoDto(componente));
                }
                if (!componente.equals(componenteActual)
                        && componente.getMarca() != null
                        && componente.getMarca().equals(componenteActual.getMarca())) {
                    //Son los componentes de la misma marca y tipo
                    todosLosComponentesComoPrimeraOpcion.add(new ComponenteEspecificoDto(componente));
                } else if (!componente.equals(componenteActual)
                        && componente.getPrecio() > 0.0){
                    //Son todos los componentes del mismo tipo
                    todosLosComponentesComoSegundaOpcion.add(new ComponenteEspecificoDto(componente));
                }
            }
            if (componentesADevolver.size() != 1) {
                return componentesADevolver;
            } else if (componentesADevolver.size() == 1 && todosLosComponentesComoPrimeraOpcion.size() != 1) {
                return todosLosComponentesComoPrimeraOpcion;
            } else {
                return todosLosComponentesComoSegundaOpcion;
            }
        }

        // si es almacenamiento
        if (componenteActual instanceof Almacenamiento) {
            for (Componente componente : componentesAComparar) {
                if (!componente.equals(componenteActual)) {
                    if (((Almacenamiento) componente).getCapacidad().equals(((Almacenamiento) componenteActual).getCapacidad())
                            && ((Almacenamiento) componente).getTipoDeDisco().equals(((Almacenamiento) componenteActual).getTipoDeDisco())
                            && ((Almacenamiento) componente).getTipoDeConexion().equals(((Almacenamiento) componenteActual).getTipoDeConexion())) {
                        componentesADevolver.add( new ComponenteEspecificoDto(componente));
                    }
                }
                if (!componente.equals(componenteActual)
                        && componente.getMarca().equals(componenteActual.getMarca())) {
                    //Son los componentes de la misma marca y tipo
                    todosLosComponentesComoPrimeraOpcion.add(new ComponenteEspecificoDto(componente));
                } else if (!componente.equals(componenteActual)) {
                    //Son todos los componentes del mismo tipo
                    todosLosComponentesComoSegundaOpcion.add(new ComponenteEspecificoDto(componente));
                }
            }
            if (componentesADevolver.size() != 1) {
                return componentesADevolver;
            } else if (componentesADevolver.size() == 1 && todosLosComponentesComoPrimeraOpcion.size() != 1) {
                return todosLosComponentesComoPrimeraOpcion;
            } else {
                return todosLosComponentesComoSegundaOpcion;
            }
        }

        // si es motherboard
        if (componenteActual instanceof Motherboard) {
            for (Componente componente : componentesAComparar) {
                if (!componente.equals(componenteActual)
                        && ((Motherboard) componente).getSocket().equals(((Motherboard) componenteActual).getSocket())
                        && ((Motherboard) componente).getChipsetPrincipal().equals(((Motherboard) componenteActual).getChipsetPrincipal())
                        && ((Motherboard) componente).getTipoMemoria().equals(((Motherboard) componenteActual).getTipoMemoria())) {
                    componentesADevolver.add( new ComponenteEspecificoDto(componente));
                }
                if (!componente.equals(componenteActual)
                        && componente.getMarca().equals(componenteActual.getMarca())) {
                    //Son los componentes de la misma marca y tipo
                    todosLosComponentesComoPrimeraOpcion.add(new ComponenteEspecificoDto(componente));
                } else if (!componente.equals(componenteActual)) {
                    //Son todos los componentes del mismo tipo
                    todosLosComponentesComoSegundaOpcion.add(new ComponenteEspecificoDto(componente));
                }
            }
            if (componentesADevolver.size() != 1) {
                return componentesADevolver;
            } else if (componentesADevolver.size() == 1 && todosLosComponentesComoPrimeraOpcion.size() != 1) {
                return todosLosComponentesComoPrimeraOpcion;
            } else {
                return todosLosComponentesComoSegundaOpcion;
            }
        }

        // si son monitores
        if (componenteActual instanceof Monitor) {
            for (Componente componente : componentesAComparar) {
                if (!componente.equals(componenteActual)
                        && ((Monitor) componente).getTipoDePanel().equals(((Monitor) componenteActual).getTipoDePanel())
                        && ((Monitor) componente).getPulgadas().equals(((Monitor) componenteActual).getPulgadas())) {
                    componentesADevolver.add(new ComponenteEspecificoDto(componente));
                } else if (!componente.equals(componenteActual)
                        && ((Monitor) componente).getPulgadas().equals(((Monitor) componenteActual).getPulgadas())) {
                    componentesADevolver.add(new ComponenteEspecificoDto(componente));
                }
                if (!componente.equals(componenteActual)
                        && componente.getMarca().equals(componenteActual.getMarca())) {
                    //Son los componentes de la misma marca y tipo
                    todosLosComponentesComoPrimeraOpcion.add(new ComponenteEspecificoDto(componente));
                } else if (!componente.equals(componenteActual)) {
                    //Son todos los componentes del mismo tipo
                    todosLosComponentesComoSegundaOpcion.add(new ComponenteEspecificoDto(componente));
                }
            }
            if (componentesADevolver.size() != 1) {
                return componentesADevolver;
            } else if (componentesADevolver.size() == 1 && todosLosComponentesComoPrimeraOpcion.size() != 1) {
                return todosLosComponentesComoPrimeraOpcion;
            } else {
                return todosLosComponentesComoSegundaOpcion;
            }
        }

        // si son collers
        if (componenteActual instanceof CoolerCPU) {
            for (Componente componente : componentesAComparar) {
                if (!componente.equals(componenteActual)
                        && componente.getPrecio() > 0.0
                        && ((CoolerCPU) componente).getTipoDeDisipacion().equals(((CoolerCPU) componenteActual).getTipoDeDisipacion())
                        && ((CoolerCPU) componente).getCantCoolersIncluidos().equals(((CoolerCPU) componenteActual).getCantCoolersIncluidos())) {
                    componentesADevolver.add(new ComponenteEspecificoDto(componente));
                }
                if (!componente.equals(componenteActual)
                        && componente.getPrecio() > 0.0
                        && componente.getMarca().equals(componenteActual.getMarca())) {
                    //Son los componentes de la misma marca y tipo
                    todosLosComponentesComoPrimeraOpcion.add(new ComponenteEspecificoDto(componente));
                } else if (!componente.equals(componenteActual)
                        && componente.getPrecio() > 0.0){
                    //Son todos los componentes del mismo tipo
                    todosLosComponentesComoSegundaOpcion.add(new ComponenteEspecificoDto(componente));
                }
            }
            if (componentesADevolver.size() != 1) {
                return componentesADevolver;
            } else if (componentesADevolver.size() == 1 && todosLosComponentesComoPrimeraOpcion.size() != 1) {
                return todosLosComponentesComoPrimeraOpcion;
            } else {
                return todosLosComponentesComoSegundaOpcion;
            }
        }

        // si es Gabinete
        if (componenteActual instanceof Gabinete) {
            for (Componente componente : componentesAComparar) {
                if (!componente.equals(componenteActual)
                        && ((Gabinete) componente).getTamanioGabinete().equals(((Gabinete) componenteActual).getTamanioGabinete())) {
                    componentesADevolver.add(new ComponenteEspecificoDto(componente));
                }
                if (!componente.equals(componenteActual)
                        && componente.getMarca().equals(componenteActual.getMarca())) {
                    //Son los componentes de la misma marca y tipo
                    todosLosComponentesComoPrimeraOpcion.add(new ComponenteEspecificoDto(componente));
                } else if (!componente.equals(componenteActual)) {
                    //Son todos los componentes del mismo tipo
                    todosLosComponentesComoSegundaOpcion.add(new ComponenteEspecificoDto(componente));
                }
            }
            if (componentesADevolver.size() != 1) {
                return componentesADevolver;
            } else if (componentesADevolver.size() == 1 && todosLosComponentesComoPrimeraOpcion.size() != 1) {
                return todosLosComponentesComoPrimeraOpcion;
            } else {
                return todosLosComponentesComoSegundaOpcion;
            }
        }
        return List.of();
    }


}
