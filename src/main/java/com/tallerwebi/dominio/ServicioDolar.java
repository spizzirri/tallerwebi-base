package com.tallerwebi.dominio;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ServicioDolar {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_URL = "https://dolarapi.com/v1/dolares/blue";

    public Double obtenerCotizacionDolarBlue() {
        Map<String, Object> response = restTemplate.getForObject(API_URL, Map.class);
        if (response != null && response.containsKey("venta")) {
            return Double.parseDouble(response.get("venta").toString());
        }
        return 0.0;
    }
}
