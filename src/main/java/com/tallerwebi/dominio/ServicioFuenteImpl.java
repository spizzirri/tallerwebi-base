package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service("servicioFuente")
@Transactional
public class ServicioFuenteImpl implements ServicioFuente {
    @Override
    public Boolean verificarCompatibilidadDeFuenteConWatsDelArmado(FuenteDeAlimentacion fuenteDeAlimentacion, ArmadoPc armadoPc) {
        return armadoPc != null && fuenteDeAlimentacion != null && obtenerConsumoFormateado(fuenteDeAlimentacion) > obtenerWatsTotales(armadoPc); // se le puede sumar un margen
    }

    public Integer obtenerWatsTotales(ArmadoPc armadoPc) {
        Integer watsTotales = 0;

        if(armadoPc.getProcesador() != null) watsTotales += this.obtenerConsumoFormateado(armadoPc.getProcesador());
        if(armadoPc.getMotherboard() != null) watsTotales += this.obtenerConsumoFormateado(armadoPc.getMotherboard());
        if(armadoPc.getCoolerCPU() != null) watsTotales += this.obtenerConsumoFormateado(armadoPc.getCoolerCPU());
        if(armadoPc.getPlacaDeVideo() != null) watsTotales += this.obtenerConsumoFormateado(armadoPc.getPlacaDeVideo());
        if(armadoPc.getGabinete() != null) watsTotales += 10; // valor que tiene compragamer hardcodeado
        if(armadoPc.getMemoriaRAM() != null && !armadoPc.getMemoriaRAM().isEmpty()) watsTotales += sumatoriaDeWatsDeListaDeComponente(armadoPc.getMemoriaRAM());
        if(armadoPc.getAlmacenamiento() != null && !armadoPc.getAlmacenamiento().isEmpty()) watsTotales += sumatoriaDeWatsDeListaDeComponente(armadoPc.getAlmacenamiento());

        return watsTotales;
    }

    private Integer obtenerConsumoFormateado(Componente componente) {

        String watsEnString = "";

        //
        if (componente instanceof FuenteDeAlimentacion) return Integer.parseInt(((FuenteDeAlimentacion) componente).getWattsReales().split("W")[0]);

        if (componente instanceof Motherboard) watsEnString = ((Motherboard) componente).getConsumo();
        if (componente instanceof CoolerCPU) watsEnString = ((CoolerCPU) componente).getConsumo();
        if (componente instanceof PlacaDeVideo) watsEnString = ((PlacaDeVideo) componente).getConsumo();
        if (componente instanceof Almacenamiento) watsEnString = ((Almacenamiento) componente).getConsumo();
        if (componente instanceof Procesador) watsEnString = ((Procesador) componente).getConsumo();

        return Integer.parseInt(watsEnString.split(" ")[0]);
    }


    private Integer sumatoriaDeWatsDeListaDeComponente(List<? extends Componente> componentes) {
        Integer sumatoriaDeWats = 0;
        for(Componente componente : componentes){
            if (componente instanceof MemoriaRAM) sumatoriaDeWats += 5; // valor que tiene compragamer hardcodeado
            if (componente instanceof Almacenamiento) sumatoriaDeWats += obtenerConsumoFormateado(componente);
        }
        return sumatoriaDeWats;
    }
}
