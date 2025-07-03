package com.tallerwebi.dominio;
import com.tallerwebi.presentacion.TarjetaDeCreditoDto;
import org.springframework.stereotype.Service;

import java.time.YearMonth;


@Service ()
public class ServicioTarjetaDeCreditoImpl implements ServicioTarjetaDeCredito {

    public ServicioTarjetaDeCreditoImpl() {

}

    @Override
  public Boolean validarLongitudDeNumeroDeTarjeta(TarjetaDeCreditoDto tarjetaDeCredito) {
       String numero =  tarjetaDeCredito.getNumeroDeTarjeta();
       Boolean numeroValido = false;
       if (numero.length() == 16) {
           numeroValido = true  ;
       }
    return numeroValido ;
    }


    @Override
    public Boolean validarVencimiento(TarjetaDeCreditoDto tarjetaDeCreditoDto) {
         String vencimiento =  tarjetaDeCreditoDto.getVencimiento();
        try {
            if (!vencimiento.matches("\\d{2}/\\d{2}")) {
                return false;
            }

            String[] partes = vencimiento.split("/");
            int mes = Integer.parseInt(partes[0]);
            int anio = 2000 + Integer.parseInt(partes[1]);

            YearMonth fecha = YearMonth.of(anio, mes);
            return !fecha.isBefore(YearMonth.now());

        } catch (Exception e) {
            return false;
        }
        }


    @Override
    public Boolean codigoDeSeguridad(TarjetaDeCreditoDto tarjetaDeCreditoDto) {
            String codigo = tarjetaDeCreditoDto.getCodigoDeSeguridad();
            if (codigo.length() != 3) {
                return false ;
            } return true;

    }
}

