package com.tallerwebi.dominio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

@Service("servicioPrecios")
@Transactional
public class ServicioPreciosImpl implements ServicioPrecios {

    private Logger log = LoggerFactory.getLogger(ServicioPreciosImpl.class);
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
        precio = Math.round(precio * 100.0) / 100.0;

        return formatter.format(precio);
    }

    @Override
    public String conversionDolarAPeso(Double precio) {
        Double cotizacionDolarBlue = servicioDolar.obtenerCotizacionDolarBlue();
        Double precioEnPesos = precio * cotizacionDolarBlue;
        return this.obtenerPrecioFormateado(precioEnPesos);
    }

    @Override
    public Double conversionDolarAPesoDouble(Double precio) {
        Double cotizacionDolarBlue = servicioDolar.obtenerCotizacionDolarBlue();
        Double precioEnPesos = precio * cotizacionDolarBlue;
        return precioEnPesos;
    }

    @Override
    public String obtenerPrecioDeLista(Double precio) {
        Double precioDeLista = precio * 1.50;
        return this.conversionDolarAPeso(precioDeLista);
    }

    @Override
    public String obtenerPrecioCon3Cuotas(Double precio) {
        Double precioPorCuota = precio / 3;
        return this.conversionDolarAPeso(precioPorCuota);
    }

    @Override
    public String obtenerPrecioCon6Cuotas(Double precio) {
        Double precioPorCuota = precio / 6;
        return this.conversionDolarAPeso(precioPorCuota);
    }

    @Override
    public String obtenerPrecioCon12Cuotas(Double precio) {
        Double precioPorCuota = precio / 12;
        return this.conversionDolarAPeso(precioPorCuota);
    }
}
