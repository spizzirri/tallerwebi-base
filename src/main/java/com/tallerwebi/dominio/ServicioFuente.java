package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.ArmadoPc;
import com.tallerwebi.dominio.entidades.FuenteDeAlimentacion;

public interface ServicioFuente {
    Boolean verificarCompatibilidadDeFuenteConWatsDelArmado(FuenteDeAlimentacion fuenteDeAlimentacion, ArmadoPc armadoPc);
    Integer obtenerWatsTotales(ArmadoPc armadoPc);
}
