package com.tallerwebi.dominio.Mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ServicioEmailImpl implements ServicioEmail {

  private final JavaMailSender mailSender;

  @Autowired
  public ServicioEmailImpl(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Override
  public void enviarEmail(String destinatario, String asunto, String mensaje) {
    SimpleMailMessage mail = new SimpleMailMessage();

    mail.setTo(destinatario);
    mail.setSubject(asunto);
    mail.setText(mensaje);

    mailSender.send(mail);
  }
}
