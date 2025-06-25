package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.ComponenteEspecificoDto;

public interface ServicioPrecios {

    String obtenerPrecioFormateado(Double precio);

    String conversionPesoADolar(ComponenteEspecificoDto componenteEspecificoDto);

    String obtenerPrecioDeLista(Double precio);

    String obtenerPrecioCon3Cuotas(Double precio);

    String obtenerPrecioCon6Cuotas(Double precio);

    String obtenerPrecioCon12Cuotas(Double precio);
}
