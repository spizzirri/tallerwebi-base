package com.tallerwebi.presentacion;

import java.util.List;
import java.util.stream.Collectors;

import com.tallerwebi.dominio.Hamburgueseria;

public class HamburgueseriasCercanasMapper {
    public static HamburgueseriaCercana map(Hamburgueseria hamburgueseria) {
        HamburgueseriaCercana dto = new HamburgueseriaCercana();
        dto.setId(hamburgueseria.getId());
        dto.setNombre(hamburgueseria.getNombre());
        dto.setLatitud(hamburgueseria.getLatitud());
        dto.setLongitud(hamburgueseria.getLongitud());
        dto.setPuntuacion(hamburgueseria.getPuntuacion());
        dto.setDireccion(hamburgueseria.getDireccion());
        dto.setEsComercioAdherido(hamburgueseria.getEsComercioAdherido());
        return dto;
    }

    public static List<HamburgueseriaCercana> mapList(List<Hamburgueseria> hamburgueserias) {
        return hamburgueserias.stream()
                .map(HamburgueseriasCercanasMapper::map)
                .collect(Collectors.toList());
    }
}
