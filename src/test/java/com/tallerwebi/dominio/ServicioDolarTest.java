package com.tallerwebi.dominio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ServicioDolarTest {

    private ServicioDolar servicioDolar;
    private RestTemplate restTemplate;

    @BeforeEach
    public void init() {
        restTemplate = mock(RestTemplate.class);
        servicioDolar = new ServicioDolar(restTemplate);
    }

    @Test
    public void dadoQueExisteUnLlamadoAUnaAPICorroboramosQueDevuelveUnaCotizacionCorrectamente() {
        Map<String, Object> respuestaSimulada = new HashMap<>();
        respuestaSimulada.put("venta", 1250.0);

        Mockito.when(restTemplate.getForObject("https://dolarapi.com/v1/dolares/blue", Map.class))
                .thenReturn(respuestaSimulada);

        Double resultado = servicioDolar.obtenerCotizacionDolarBlue();
        assertEquals(1250.0, resultado);
    }

    @Test
    public void dadoQueExisteUnLlamadoAUnaAPIQueNoDevuelveUn200DebeRetornarCeroComoCotizacion() {
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(Map.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        Double cotizacion = servicioDolar.obtenerCotizacionDolarBlue();

        assertEquals(0.0, cotizacion);
    }

    @Test
    public void dadoQueYaExisteUnValorCacheadoNoDebeVolverALlamarALaAPI() {
        Map<String, Object> respuestaSimulada = new HashMap<>();
        respuestaSimulada.put("venta", 1250.0);

        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(Map.class)))
                .thenReturn(respuestaSimulada);

        // Primer llamado: debería llamar a la API
        Double cotizacion1 = servicioDolar.obtenerCotizacionDolarBlue();
        assertEquals(1250.0, cotizacion1);

        // Segundo llamado inmediato: NO debería llamar de nuevo (usa cache)
        Double cotizacion2 = servicioDolar.obtenerCotizacionDolarBlue();
        assertEquals(1250.0, cotizacion2);

        // Verifica que solo se llamó una vez
        Mockito.verify(restTemplate, Mockito.times(1))
                .getForObject(Mockito.anyString(), Mockito.eq(Map.class));
    }

}
