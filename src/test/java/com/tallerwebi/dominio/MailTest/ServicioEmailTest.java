package com.tallerwebi.dominio.MailTest;

import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Mail.ServicioEmailImpl;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class ServicioEmailTest {

  @Test
  public void deberiaEnviarUnCorreo() {
    JavaMailSender mailSender = mock(JavaMailSender.class);

    ServicioEmailImpl servicio = new ServicioEmailImpl(mailSender);

    servicio.enviarEmail("test@gmail.com", "Asunto", "Mensaje");

    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }
}
