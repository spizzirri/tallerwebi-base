package com.tallerwebi.dominio;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ServicioDolar {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_URL = (System.getenv("API_DOLAR") != null)
            ? System.getenv("API_DOLAR")
            : "https://dolarapi.com/v1/dolares/blue";

    // variables para no pegarle a la api todo el tiempo por cada producto sino cada 10 minutos.
    private Double cotizacionCacheada = null;
    private long ultimoUpdate = 0;
    private static final long DURACION_CACHE_MILLIS = 10 * 60 * 1000; // 10 minutos

    public synchronized Double obtenerCotizacionDolarBlue() {
        long ahora = System.currentTimeMillis();
        if (cotizacionCacheada == null || (ahora - ultimoUpdate) > DURACION_CACHE_MILLIS) {
            Map<String, Object> response = restTemplate.getForObject(API_URL, Map.class);
            if (response != null && response.containsKey("venta")) {
                cotizacionCacheada = Double.parseDouble(response.get("venta").toString());
                ultimoUpdate = ahora;
            } else {
                cotizacionCacheada = 0.0; // por si falla la API
            }
        }
        return cotizacionCacheada;
    }
}