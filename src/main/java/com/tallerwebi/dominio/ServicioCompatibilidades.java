package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.ArmadoPc;
import com.tallerwebi.dominio.entidades.Componente;

public interface ServicioCompatibilidades {

    Boolean esCompatibleConElArmado(Componente componente, ArmadoPc armadoPc);
}
