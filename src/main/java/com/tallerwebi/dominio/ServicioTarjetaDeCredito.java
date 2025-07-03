package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.TarjetaDeCreditoDto;

public interface ServicioTarjetaDeCredito {

    Boolean validarLongitudDeNumeroDeTarjeta(TarjetaDeCreditoDto tarjetaDeCredito);
    Boolean validarVencimiento(TarjetaDeCreditoDto tarjetaDeCreditoDto);
    Boolean codigoDeSeguridad(TarjetaDeCreditoDto tarjetaDeCreditoDto);

}


