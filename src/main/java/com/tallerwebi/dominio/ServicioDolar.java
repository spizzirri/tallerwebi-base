package com.tallerwebi.dominio;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.Access;
import java.util.Map;

@Service
public class ServicioDolar {

    private final RestTemplate restTemplate; //Clase que permite hacer llamdas HTTP a externos
    private final String API_URL = (System.getenv("API_DOLAR") != null)
            ? System.getenv("API_DOLAR")
            : "https://dolarapi.com/v1/dolares/blue";

    private Double cotizacionCacheada = null;
    private long ultimoUpdate = 0;
    private static final long DURACION_CACHE_MILLIS = 10 * 60 * 1000; // 10 minutos

    public ServicioDolar(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    };

    public synchronized Double obtenerCotizacionDolarBlue() {
        long ahora = System.currentTimeMillis();

        if (cotizacionCacheada == null || (ahora - ultimoUpdate) > DURACION_CACHE_MILLIS) {
            try {
                Map<String, Object> response = restTemplate.getForObject(API_URL, Map.class);
                if (response != null && response.containsKey("venta")) {
                    cotizacionCacheada = Double.parseDouble(response.get("venta").toString());
                    ultimoUpdate = ahora;
                } else {
                    cotizacionCacheada = 0.0;
                }
            } catch (Exception e) {
                cotizacionCacheada = 0.0;
            }
        }

        return cotizacionCacheada;
    }

}
