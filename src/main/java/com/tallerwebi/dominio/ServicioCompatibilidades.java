package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.ArmadoPc;
import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.dominio.excepcion.ComponenteDeterminateDelArmadoEnNullException;

public interface ServicioCompatibilidades {

    Boolean esCompatibleConElArmado(Componente componente, ArmadoPc armadoPc) throws ComponenteDeterminateDelArmadoEnNullException;
    ArmadoPc completarEntidadArmadoPcParaEvaluarFuente(ArmadoPc armadoPcEntidad);
    Integer obtenerWattsDeArmado(ArmadoPc armadoPc);
}
