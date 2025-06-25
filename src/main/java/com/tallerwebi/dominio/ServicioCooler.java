package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.CoolerCPU;
import com.tallerwebi.dominio.entidades.Motherboard;
import com.tallerwebi.dominio.entidades.Procesador;

public interface ServicioCooler {
    Boolean verificarCoolerIncluido(CoolerCPU coolerCPU, Procesador procesador);

    Boolean verificarCompatibilidadDeCoolerConMotherboard(Motherboard motherboard, CoolerCPU coolerCPU);
}
