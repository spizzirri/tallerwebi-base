package com.valhalla.infrastructure.email;

import com.valhalla.domain.plan.EmailNotifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailConfiguration {

  private final Environment environment;

  public EmailConfiguration(Environment environment) {
    this.environment = environment;
  }

  private String env(String name, String fallback) {
    String value = environment.getProperty(name);
    return value == null || value.isBlank() ? fallback : value;
  }

  @Bean
  public EmailNotifier emailNotifier() {
    String host = env("SMTP_HOST", "");
    if (host.isEmpty()) return new LoggingEmailNotifier();
    JavaMailSenderImpl sender = new JavaMailSenderImpl();
    sender.setHost(host);
    sender.setPort(Integer.parseInt(env("SMTP_PORT", "587")));
    sender.setUsername(env("SMTP_USERNAME", ""));
    sender.setPassword(env("SMTP_PASSWORD", ""));
    var properties = sender.getJavaMailProperties();
    properties.setProperty("mail.smtp.auth", env("SMTP_AUTH", "true"));
    properties.setProperty("mail.smtp.starttls.enable", env("SMTP_STARTTLS", "true"));
    properties.setProperty("mail.smtp.ssl.enable", env("SMTP_SSL", "false"));
    properties.setProperty("mail.smtp.connectiontimeout", "5000");
    properties.setProperty("mail.smtp.timeout", "5000");
    properties.setProperty("mail.smtp.writetimeout", "5000");
    String origin = env("APP_BASE_URL", "http://localhost:8080").replaceAll("/+$", "");
    if (!origin.matches("https?://[^\\s]+")) throw new IllegalArgumentException(
      "APP_BASE_URL inválida"
    );
    return (recipient, planId, token) -> {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(env("SMTP_FROM", "planit@localhost"));
      message.setTo(recipient);
      message.setSubject("Invitación a un plan en PlanIt");
      message.setText(
        "Recibiste una invitación. Iniciá sesión con esta cuenta para aceptarla: " +
        origin +
        "/plans/" +
        planId +
        "/invitations/" +
        token +
        "/accept"
      );
      sender.send(message);
    };
  }
}
