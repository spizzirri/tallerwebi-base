package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.PlacaDeVideo;
import com.tallerwebi.dominio.entidades.Procesador;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service("servicioPlacaDeVideo")
@Transactional
public class ServicioPlacaDeVideoImpl implements ServicioPlacaDeVideo {
    @Override
    public Boolean verificarGraficosIntegrados(PlacaDeVideo placaDeVideo, Procesador procesador) {
        return procesador.getIncluyeGraficosIntegrados() && placaDeVideo.getPrecio() == 0.0;
    }

    @Override
    public Boolean verificarPrecioMayorACero(PlacaDeVideo placaDeVideo) {
        return placaDeVideo.getPrecio() > 0.0;
    }
}
