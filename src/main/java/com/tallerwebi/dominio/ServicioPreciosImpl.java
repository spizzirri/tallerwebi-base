package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.ComponenteEspecificoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

@Service("servicioPrecios")
@Transactional
public class ServicioPreciosImpl implements ServicioPrecios {

    private ServicioDolar servicioDolar;

    @Autowired
    public ServicioPreciosImpl(ServicioDolar servicioDolar) {
        this.servicioDolar = servicioDolar;
    }

    @Override
    public String obtenerPrecioFormateado(Double precio) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat formatter = new DecimalFormat("#,##0.00", symbols);
        return formatter.format(precio);
    }

    @Override
    public String conversionPesoADolar(ComponenteEspecificoDto componenteEspecificoDto) {
        Double cotizacionDolarBlue = servicioDolar.obtenerCotizacionDolarBlue();
        Double precioEnPesos = componenteEspecificoDto.getPrecio();
        Double precioEnDolares = precioEnPesos / cotizacionDolarBlue;
        return this.obtenerPrecioFormateado(precioEnDolares);
    }

    @Override
    public String obtenerPrecioDeLista(Double precio) {
        Double precioDeLista = precio * 1.50;
        return this.obtenerPrecioFormateado(precioDeLista);
    }

    @Override
    public String obtenerPrecioCon3Cuotas(Double precio) {
        Double precioPorCuota = precio / 3;
        return this.obtenerPrecioDeLista(precioPorCuota);
    }

    @Override
    public String obtenerPrecioCon6Cuotas(Double precio) {
        Double precioPorCuota = precio / 6;
        return this.obtenerPrecioDeLista(precioPorCuota);
    }

    @Override
    public String obtenerPrecioCon12Cuotas(Double precio) {
        Double precioPorCuota = precio / 12;
        return this.obtenerPrecioDeLista(precioPorCuota);
    }
}
