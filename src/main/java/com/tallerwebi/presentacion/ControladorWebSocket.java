package com.tallerwebi.presentacion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.MensajeEnviado;
import com.tallerwebi.dominio.MensajeRecibido;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ControladorWebSocket {

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public String getMessages(MensajeRecibido mensajeRecibido) throws Exception {

            MensajeEnviado mensajeEnviado = new MensajeEnviado(mensajeRecibido.getMessage());
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(mensajeEnviado);

            return json;
    }
}

