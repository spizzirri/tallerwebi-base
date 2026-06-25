package com.tallerwebi.dominio.MailTest;

import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Mail.ServicioEmailImpl;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

public class ServicioEmailTest {

  @Test
  public void deberiaEnviarUnCorreo() {
    // 1. Mockear dependencias
    JavaMailSender mailSender = mock(JavaMailSender.class);
    MimeMessage mimeMessageMock = mock(MimeMessage.class);

    // 2. Configurar comportamiento del Mock
    when(mailSender.createMimeMessage()).thenReturn(mimeMessageMock);

    // 3. Instanciar el servicio
    ServicioEmailImpl servicio = new ServicioEmailImpl(mailSender);

    // 4. Inyectar manualmente el valor en el campo privado 'username'
    //nos saltamos las restricciones de encapsulamiento
    //ASI EL HELPER.SETFROM NO QUEDA NULL
    ReflectionTestUtils.setField(servicio, "username", "remitente@kionet.com");

    // 5. Ejecutar método
    servicio.enviarEmail("test@gmail.com", "Asunto", "Mensaje");

    // 6. Verificar
    verify(mailSender, times(1)).send(any(MimeMessage.class));
  }
}
