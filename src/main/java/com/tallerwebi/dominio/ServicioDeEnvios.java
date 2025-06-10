package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.EnvioDto;
import com.tallerwebi.presentacion.OpcionEnvio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ServicioDeEnvios {
    // es quien se va a conectar y traer los datos de la API
    private static final String EMPRESA_ENVIA = "Andreani";

    @Value("${mockapi.envios.url:https://683b8d5e28a0b0f2fdc4ead9.mockapi.io/envios}")
    private String enviosUrl;

    private RestTemplate restTemplate;

    public ServicioDeEnvios() {
        restTemplate = new RestTemplate();
    }

    public EnvioDto calcularEnvio(String codigoPostal) {
        try {
            if (codigoPostal == null || !codigoPostal.matches("\\d{4}")) {
                throw new IllegalArgumentException("El codigo postal no es valido");
            }
            //convertir OpcionEnvio a EnvioDTO
            String url = enviosUrl + "?codigoPostal=" + codigoPostal;

            OpcionEnvio[] opciones = restTemplate.getForObject(url, OpcionEnvio[].class);

            if (opciones != null && opciones.length > 0) {
                OpcionEnvio envio = opciones[0];

                return new EnvioDto(
                        envio.getCostoEnvio(),
                        envio.getTiempoEstimado(),
                        envio.getLocalidad() + ", " + envio.getProvincia()
                );
            }
            return null;

        } catch (Exception e) {
            return null;
        }
    }
}

//            return buscarEnZonaCercana(codigoPostal);


