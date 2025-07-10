package com.tallerwebi.dominio;



import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.presentacion.ComponenteEspecificoDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface ServicioProductoEspecifico {

    Componente obtenerComponentePorId(Long idComponente);
    List<ComponenteEspecificoDto> obtenerComponentesAcomparar(Long idComponente);

}