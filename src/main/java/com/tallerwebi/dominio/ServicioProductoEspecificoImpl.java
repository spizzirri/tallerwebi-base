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
         // si es procesador
          if (componenteActual instanceof Procesador) {
              List<ComponenteEspecificoDto> componentesADevolver = new ArrayList<>();
              String familia =  ((Procesador) componenteActual).getFamilia(); // ryzen 5
              //primer componente PAPA
              componentesADevolver.add(new ComponenteEspecificoDto(componenteActual));
              List <Componente> componentesAComparar = repositorioComponente.obtenerComponentesPorTipoEnStock(componenteActual.getClass().getSimpleName());
              for (Componente componente : componentesAComparar) {
                  if ( componente instanceof Procesador) {
                     if ( !componente.equals(componenteActual) &&((Procesador) componente).getFamilia().equals(familia)) {
                         componentesADevolver.add( new ComponenteEspecificoDto(componente));
                     }
                  }
              }
              return componentesADevolver;
          }

        // si es perifericos
        if (componenteActual instanceof Periferico) {
            List<ComponenteEspecificoDto> componentesADevolver = new ArrayList<>();
            String tipoDeConexion =  ((Periferico) componenteActual).getTipoDeConexion();

            String[] partesNombreActual = componenteActual.getNombre().split(" ");
            String nombrePeriferico = partesNombreActual.length > 0 ? partesNombreActual[0].toLowerCase() : "";
            componentesADevolver.add(new ComponenteEspecificoDto(componenteActual));

            List<Componente> componentesAComparar = repositorioComponente.obtenerComponentesPorTipoEnStock(componenteActual.getClass().getSimpleName());
            for (Componente componente : componentesAComparar) {
                Periferico periferico = (Periferico) componente;

                String tipoConexionComponente = periferico.getTipoDeConexion();
                String[] partesNombreComponente = componente.getNombre().split(" ");
                String nombrePerifericoComponente = partesNombreComponente.length > 0 ? partesNombreComponente[0].toLowerCase() : "";

                if (!componente.equals(componenteActual) && nombrePerifericoComponente.equals(nombrePeriferico) &&
                        tipoConexionComponente.equalsIgnoreCase(tipoDeConexion)) {
                    componentesADevolver.add(new ComponenteEspecificoDto(componente));
                }
            }
            return componentesADevolver;
        }

        // si es fuente de alimentacion
        if (componenteActual instanceof FuenteDeAlimentacion) {
            List<ComponenteEspecificoDto> componentesADevolver = new ArrayList<>();
            String wattsNominales =  ((FuenteDeAlimentacion) componenteActual).getWattsNominales();
            componentesADevolver.add(new ComponenteEspecificoDto(componenteActual));
            List <Componente> componentesAComparar = repositorioComponente.obtenerComponentesPorTipoEnStock(componenteActual.getClass().getSimpleName());
            for (Componente componente : componentesAComparar) {
                if ( !componente.equals(componenteActual) && componente instanceof FuenteDeAlimentacion) {
                    if ( ((FuenteDeAlimentacion) componente).getWattsNominales().equals(wattsNominales)) {
                        componentesADevolver.add( new ComponenteEspecificoDto(componente));
                    }
                }
            }
            return componentesADevolver;
        }

        // si es memoria ram
        if (componenteActual instanceof MemoriaRAM) {
            List<ComponenteEspecificoDto> componentesADevolver = new ArrayList<>();
            String capacidad =  ((MemoriaRAM) componenteActual).getCapacidad();
            String tecnologia =  ((MemoriaRAM) componenteActual).getTecnologiaRAM();
            componentesADevolver.add(new ComponenteEspecificoDto(componenteActual));
            List <Componente> componentesAComparar = repositorioComponente.obtenerComponentesPorTipoEnStock(componenteActual.getClass().getSimpleName());
            for (Componente componente : componentesAComparar) {
                if ( !componente.equals(componenteActual) && componente instanceof MemoriaRAM) {
                    if ( ((MemoriaRAM) componente).getCapacidad().equals(capacidad) && ((MemoriaRAM) componente).getTecnologiaRAM().equals(tecnologia)) {
                        componentesADevolver.add( new ComponenteEspecificoDto(componente));
                    }
                }
            }
            return componentesADevolver;
        }

        // si es placa de video
        if (componenteActual instanceof PlacaDeVideo) {
            List<ComponenteEspecificoDto> componentesADevolver = new ArrayList<>();
            String capacidadRAM =  ((PlacaDeVideo) componenteActual).getCapacidadRAM();
            componentesADevolver.add(new ComponenteEspecificoDto(componenteActual));
            List <Componente> componentesAComparar = repositorioComponente.obtenerComponentesPorTipoEnStock(componenteActual.getClass().getSimpleName());
            for (Componente componente : componentesAComparar) {
                if ( !componente.equals(componenteActual) && componente instanceof PlacaDeVideo) {
                    if (((PlacaDeVideo) componente).getCapacidadRAM() != null && (((PlacaDeVideo) componente).getCapacidadRAM().equals(capacidadRAM))) {
                        componentesADevolver.add( new ComponenteEspecificoDto(componente));
                    }
                }
            }
            return componentesADevolver;
        }

        // si es almacenamiento
        if (componenteActual instanceof Almacenamiento) {
            List<ComponenteEspecificoDto> componentesADevolver = new ArrayList<>();
            String capacidad =  ((Almacenamiento) componenteActual).getCapacidad();
            componentesADevolver.add(new ComponenteEspecificoDto(componenteActual));
            List <Componente> componentesAComparar = repositorioComponente.obtenerComponentesPorTipoEnStock(componenteActual.getClass().getSimpleName());
            for (Componente componente : componentesAComparar) {
                if ( !componente.equals(componenteActual) && componente instanceof Almacenamiento) {
                    if ( ((Almacenamiento) componente).getCapacidad().equals(capacidad)) {
                        componentesADevolver.add( new ComponenteEspecificoDto(componente));
                    }
                }
            }
            return componentesADevolver;
        }

        // si es motherboard
        if (componenteActual instanceof Motherboard) {
            List<ComponenteEspecificoDto> componentesADevolver = new ArrayList<>();
            String chipsetPrincipal =  ((Motherboard) componenteActual).getChipsetPrincipal();
            componentesADevolver.add(new ComponenteEspecificoDto(componenteActual));
            List <Componente> componentesAComparar = repositorioComponente.obtenerComponentesPorTipoEnStock(componenteActual.getClass().getSimpleName());
            for (Componente componente : componentesAComparar) {
                if ( !componente.equals(componenteActual) && componente instanceof Motherboard) {
                    if ( ((Motherboard) componente).getChipsetPrincipal().equals(chipsetPrincipal)) {
                        componentesADevolver.add( new ComponenteEspecificoDto(componente));
                    }
                }
            }
            return componentesADevolver;
        }

        // si son monitores
        if (componenteActual instanceof Monitor) {
            List<ComponenteEspecificoDto> componentesADevolver = new ArrayList<>();
            String tipoDePanel =  ((Monitor) componenteActual).getTipoDePanel();
            componentesADevolver.add(new ComponenteEspecificoDto(componenteActual));
            List <Componente> componentesAComparar = repositorioComponente.obtenerComponentesPorTipoEnStock(componenteActual.getClass().getSimpleName());
            for (Componente componente : componentesAComparar) {
                if ( !componente.equals(componenteActual) && componente instanceof Monitor) {
                    if ( ((Monitor) componente).getTipoDePanel().equals(tipoDePanel)) {
                        componentesADevolver.add( new ComponenteEspecificoDto(componente));
                    }
                }
            }
            return componentesADevolver;
        }

        // si son collers
        if (componenteActual instanceof CoolerCPU) {
            List<ComponenteEspecificoDto> componentesADevolver = new ArrayList<>();
            String tipoDeDisipacion = ((CoolerCPU) componenteActual).getTipoDeDisipacion();
            componentesADevolver.add(new ComponenteEspecificoDto(componenteActual));
            List <Componente> componentesAComparar = repositorioComponente.obtenerComponentesPorTipoEnStock(componenteActual.getClass().getSimpleName());
            for (Componente componente : componentesAComparar) {
                if ( !componente.equals(componenteActual) && componente instanceof CoolerCPU && componente.getPrecio() > 0.0) {
                    if ( ((CoolerCPU) componente).getTipoDeDisipacion().equals(tipoDeDisipacion)) {
                        componentesADevolver.add( new ComponenteEspecificoDto(componente));
                    }
                }
            }
            return componentesADevolver;
        }

       return List.of();
    }


}
