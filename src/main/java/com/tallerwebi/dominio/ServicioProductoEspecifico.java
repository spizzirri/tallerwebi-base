package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Componente;
import org.springframework.stereotype.Service;

@Service
public interface ServicioProductoEspecifico {

    Componente obtenerComponentePorId(Long idComponente);

}