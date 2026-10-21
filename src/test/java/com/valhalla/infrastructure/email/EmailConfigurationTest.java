package com.valhalla.infrastructure.email;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.plan.EmailNotifier;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mock.env.MockEnvironment;

public class EmailConfigurationTest {

  private static final UUID PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000002a");

  @Test
  public void missingOrBlankHostUsesFallbackWithoutLeakingRecipientOrToken() {
    assertThat(
      new EmailConfiguration(new MockEnvironment()).emailNotifier(),
      instanceOf(LoggingEmailNotifier.class)
    );
    EmailNotifier notifier = new EmailConfiguration(
      new MockEnvironment().withProperty("SMTP_HOST", " ")
    )
      .emailNotifier();
    StringBuilder messages = new StringBuilder();
    Handler handler = new Handler() {
      @Override
      public void publish(LogRecord record) {
        messages.append(record.getMessage());
      }

      @Override
      public void flush() {}

      @Override
      public void close() {}
    };
    Logger logger = Logger.getLogger(LoggingEmailNotifier.class.getName());
    logger.addHandler(handler);
    try {
      notifier.sendInvitation("private@test.com", PLAN_ID, "secret-token");
      assertThat(messages.toString(), containsString(PLAN_ID.toString()));
      assertThat(messages.toString(), not(containsString("private@test.com")));
      assertThat(messages.toString(), not(containsString("secret-token")));
    } finally {
      logger.removeHandler(handler);
    }
  }

  @Test
  public void smtpSendsRecipientBoundConfirmationLinkWithConfiguredTransport() {
    MockEnvironment environment = new MockEnvironment()
      .withProperty("SMTP_HOST", "smtp.test")
      .withProperty("SMTP_PORT", "2525")
      .withProperty("SMTP_USERNAME", "sender")
      .withProperty("SMTP_PASSWORD", "password")
      .withProperty("SMTP_FROM", "planit@test.com")
      .withProperty("SMTP_AUTH", "false")
      .withProperty("SMTP_STARTTLS", "false")
      .withProperty("SMTP_SSL", "true")
      .withProperty("APP_BASE_URL", "https://planit.test///");
    try (MockedConstruction<JavaMailSenderImpl> senders = senderMocks()) {
      new EmailConfiguration(environment)
        .emailNotifier()
        .sendInvitation("recipient@test.com", PLAN_ID, "token");
      JavaMailSenderImpl sender = senders.constructed().getFirst();
      verify(sender).setHost("smtp.test");
      verify(sender).setPort(2525);
      verify(sender).setUsername("sender");
      verify(sender).setPassword("password");
      Properties properties = sender.getJavaMailProperties();
      assertThat(properties.getProperty("mail.smtp.auth"), is("false"));
      assertThat(properties.getProperty("mail.smtp.starttls.enable"), is("false"));
      assertThat(properties.getProperty("mail.smtp.ssl.enable"), is("true"));
      assertThat(properties.getProperty("mail.smtp.connectiontimeout"), is("5000"));
      ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
      verify(sender).send(message.capture());
      assertThat(message.getValue().getTo(), is(new String[] { "recipient@test.com" }));
      assertThat(message.getValue().getFrom(), is("planit@test.com"));
      assertThat(
        message.getValue().getText(),
        containsString("https://planit.test/plans/" + PLAN_ID + "/invitations/token/accept")
      );
    }
  }

  @Test
  public void smtpDefaultsAndSendFailuresArePreserved() {
    try (MockedConstruction<JavaMailSenderImpl> senders = senderMocks()) {
      EmailNotifier notifier = new EmailConfiguration(
        new MockEnvironment().withProperty("SMTP_HOST", "smtp.test")
      )
        .emailNotifier();
      JavaMailSenderImpl sender = senders.constructed().getFirst();
      verify(sender).setPort(587);
      assertThat(
        sender.getJavaMailProperties().getProperty("mail.smtp.starttls.enable"),
        is("true")
      );
      doThrow(new MailSendException("delivery failed"))
        .when(sender)
        .send(any(SimpleMailMessage.class));
      assertThrows(
        MailSendException.class,
        () -> notifier.sendInvitation("recipient@test.com", PLAN_ID, "token")
      );
    }
  }

  @Test
  public void invalidOriginIsRejectedBeforeSending() {
    try (MockedConstruction<JavaMailSenderImpl> senders = senderMocks()) {
      MockEnvironment environment = new MockEnvironment()
        .withProperty("SMTP_HOST", "smtp.test")
        .withProperty("APP_BASE_URL", "javascript:invalid");
      assertThrows(
        IllegalArgumentException.class,
        () -> new EmailConfiguration(environment).emailNotifier()
      );
      assertThat(senders.constructed().size(), is(1));
    }
  }

  private MockedConstruction<JavaMailSenderImpl> senderMocks() {
    return mockConstruction(
      JavaMailSenderImpl.class,
      (sender, context) -> when(sender.getJavaMailProperties()).thenReturn(new Properties())
    );
  }
}
