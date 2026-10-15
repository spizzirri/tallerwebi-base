package com.tallerwebi.dominio;

import jakarta.servlet.http.HttpSession;

@FunctionalInterface
public interface ServicioAsistente {
  String enviarMensaje(Socio socio, HttpSession sesion, String mensaje);
}
