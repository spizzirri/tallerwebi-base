package com.tallerwebi.config;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

  @Value("${MAIL_USERNAME}")
  private String username;

  @Value("${MAIL_PASSWORD}")
  private String password;

  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl sender = new JavaMailSenderImpl();

    sender.setHost("smtp.gmail.com");
    sender.setPort(587);

    sender.setUsername(username);
    sender.setPassword(password);

    Properties props = sender.getJavaMailProperties();

    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");

    return sender;
  }
}
