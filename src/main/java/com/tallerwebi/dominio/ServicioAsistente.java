package com.tallerwebi.dominio;

import jakarta.servlet.http.HttpSession;

@FunctionalInterface
public interface ServicioAsistente {
  String enviarMensaje(HttpSession sesion, String mensaje);
}
