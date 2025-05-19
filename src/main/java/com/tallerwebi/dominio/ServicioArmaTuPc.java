package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.dto.ComponenteDto;

import java.util.List;

public interface ServicioArmaTuPc {
    List<ComponenteDto> obtenerListaDeComponentesDto(String tipoComponente);

    ComponenteDto obtenerComponenteDtoPorId(Long id);
}
