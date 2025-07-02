package com.tallerwebi.dominio;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ServicioDolar {

    private final RestTemplate restTemplate = new RestTemplate(); //Clase que permite hacer llamdas HTTP a externos
    private final String API_URL = (System.getenv("API_DOLAR") != null)
            ? System.getenv("API_DOLAR")
            : "https://dolarapi.com/v1/dolares/blue";

    public Double obtenerCotizacionDolarBlue() {
        Map<String, Object> response = restTemplate.getForObject(API_URL, Map.class); //Hace la solicitud HTTP GET a la URL
        if (response != null && response.containsKey("venta")) {
            return Double.parseDouble(response.get("venta").toString());
        }
        return 0.0;
    }
}
