package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.CoolerCPU;
import com.tallerwebi.dominio.entidades.Motherboard;
import com.tallerwebi.dominio.entidades.Procesador;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service("servicioCooler")
@Transactional
public class ServicioCoolerImpl implements ServicioCooler {
    @Override
    public Boolean verificarCoolerIncluido(CoolerCPU coolerCPU, Procesador procesador) {
        return coolerCPU != null && procesador != null && procesador.getIncluyeCooler() && coolerCPU.getPrecio() == 0.0;
    }

    @Override
    public Boolean verificarCompatibilidadDeCoolerConMotherboard(Motherboard motherboard, CoolerCPU coolerCPU) {
        return coolerCPU != null && motherboard != null && coolerCPU.getSockets().contains(motherboard.getSocket());
    }
}
