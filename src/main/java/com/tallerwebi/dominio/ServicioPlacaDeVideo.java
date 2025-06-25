package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.PlacaDeVideo;
import com.tallerwebi.dominio.entidades.Procesador;

public interface ServicioPlacaDeVideo {
    Boolean verificarGraficosIntegrados(PlacaDeVideo placaDeVideo, Procesador procesador);

    Boolean verificarPrecioMayorACero(PlacaDeVideo placaDeVideo);
}
