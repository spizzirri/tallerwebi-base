package com.tallerwebi.dominio;

import org.springframework.stereotype.Service;
import com.tallerwebi.dominio.excepcion.NoCumpleRequisitos;

@Service("servicioAutenticacion")
public class ServicioAutenticacionImpl implements ServicioAutenticacion {
    @Override
    public boolean esUsuarioValido(Usuario usuario) throws NoCumpleRequisitos {
        
        Integer edad = usuario.getEdad();
        if (edad < 18 || edad > 40)
           return false;
        return true;
    }
}

/**
 
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:3000/deudores/" + usuario.getDni()))
            .header("apikey", "tallerwebi")
            .GET()
            .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                throw new NoCumpleRequisitos();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return true;


 */