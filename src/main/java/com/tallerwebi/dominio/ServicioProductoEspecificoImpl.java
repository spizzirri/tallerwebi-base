package com.tallerwebi.dominio;


import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.dominio.entidades.Motherboard;
import com.tallerwebi.dominio.entidades.Procesador;
import com.tallerwebi.presentacion.ComponenteEspecificoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

       return List.of();
    }


}
