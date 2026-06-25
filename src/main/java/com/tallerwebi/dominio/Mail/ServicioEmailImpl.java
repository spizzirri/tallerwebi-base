package com.tallerwebi.dominio.Mail;

import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class ServicioEmailImpl implements ServicioEmail {

  private final JavaMailSender mailSender;

  @Value("${MAIL_USERNAME}")
  private String username;

  @Autowired
  public ServicioEmailImpl(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Override
  public void enviarEmail(String destinatario, String asunto, String mensaje) {
    try {
      MimeMessage mail = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mail, true, "UTF-8");
      helper.setFrom(username);
      helper.setTo(destinatario);
      helper.setSubject(asunto);
      helper.setText(mensaje);
      mailSender.send(mail);
    } catch (Exception e) {
      throw new RuntimeException("Error al enviar email", e);
    }
  }
}
