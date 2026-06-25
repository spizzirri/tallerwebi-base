package com.tallerwebi.dominio.Mail;

public interface ServicioEmail {
  void enviarEmail(String destinatario, String asunto, String mensaje);
}
