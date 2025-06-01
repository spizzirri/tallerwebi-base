package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.EnvioDto;
import com.tallerwebi.presentacion.OpcionEnvio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Optional;

@Service
public class ServicioDeEnvios {

    private static final Logger logger = LoggerFactory.getLogger(ServicioDeEnvios.class);
    private static final String EMPRESA_ENVIA = "Andreani";

    @Value("${mockapi.envios.url:https://683b8d5e28a0b0f2fdc4ead9.mockapi.io/envios}")
    private String enviosUrl;

    private RestTemplate restTemplate;

    public ServicioDeEnvios() {
        restTemplate = new RestTemplate();
    }

    public EnvioDto calcularEnvio(String codigoPostal){

        try{
            logger.info("Calculando envio de postal: " + codigoPostal);
            if(codigoPostal == null || !codigoPostal.matches("\\d{4}")){
                throw new IllegalArgumentException("El codigo de postal no es valido");
            }

            String url = enviosUrl + "?codigoPostal=" + codigoPostal;
            //hacer que la api devuelva un json con los datos de la api
            OpcionEnvio[] opciones = restTemplate.getForObject(url, OpcionEnvio[].class);

            if(opciones != null){
                //convierto el array en un Stream (flujo de datos)
                Optional<OpcionEnvio> streamEnvio = Arrays.stream(opciones)
                        .filter(opcion -> EMPRESA_ENVIA.equals(opcion.getEmpresa()))
                        .findFirst();

                if(streamEnvio.isPresent()){
                    OpcionEnvio envio = streamEnvio.get();
                    logger.info("Envio encontrado: " + envio.getCostoEnvio(), envio.getTiempoEstimado());

                    return new EnvioDto(
                            envio.getCostoEnvio(),
                            envio.getTiempoEstimado(),
                            envio.getLocalidad() + ", " + envio.getProvincia()
                    );
                }
            }
            return null;
//            return buscarEnZonaCercana(codigoPostal);
        } catch (Exception e){
            logger.error("Error al calcular envio de postal: " + codigoPostal, e.getMessage());
            return null;
        }
    }
}

