package com.tallerwebi.dominio.calendario;
import org.springframework.stereotype.Service;

import java.util.*;
@Service
public class ServicioItemRendimientoImpl implements ServicioItemRendimiento {

    private List<ItemRendimiento> calendarioItems;


    @Override
    public List<ItemRendimiento> obtenerItems() {
        return List.of();
    }
}
