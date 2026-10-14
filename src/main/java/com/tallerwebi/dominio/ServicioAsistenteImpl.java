package com.tallerwebi.dominio;

import com.tallerwebi.infraestructura.ClienteGemini;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Indica que esta clase contiene la lógica de negocio y la registra como Bean en Spring
@Service
public class ServicioAsistenteImpl implements ServicioAsistente {

  public static final String CLAVE_HISTORIAL_CHAT = "CHAT_HISTORIAL";
  private static final int MAX_MENSAJES_EN_SESION = 20;
  private final ClienteGemini clienteGemini;

  @Autowired
  public ServicioAsistenteImpl(ClienteGemini clienteGemini) {
    this.clienteGemini = clienteGemini;
  }

  @Override
  public String enviarMensaje(HttpSession sesion, String mensaje) {
    if (mensaje == null || mensaje.isBlank()) {
      return "";
    }
    String textoUsuario = mensaje.trim();
    List<MensajeConversacion> historial = historialDe(sesion);
    String respuesta = clienteGemini.completar(historial, textoUsuario);
    historial.add(new MensajeConversacion("user", textoUsuario));
    historial.add(new MensajeConversacion("model", respuesta));
    recortar(historial);
    sesion.setAttribute(CLAVE_HISTORIAL_CHAT, historial);
    return respuesta;
  }

  @SuppressWarnings("unchecked")
  private List<MensajeConversacion> historialDe(HttpSession sesion) {
    Object raw = sesion.getAttribute(CLAVE_HISTORIAL_CHAT);
    if (raw instanceof List<?>) {
      return new ArrayList<>((List<MensajeConversacion>) raw);
    }
    return new ArrayList<>();
  }

  private void recortar(List<MensajeConversacion> historial) {
    while (historial.size() > MAX_MENSAJES_EN_SESION) {
      historial.remove(0);
    }
  }
}
