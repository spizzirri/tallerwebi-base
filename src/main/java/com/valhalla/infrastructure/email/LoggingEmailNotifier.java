package com.valhalla.infrastructure.email;

import com.valhalla.domain.plan.EmailNotifier;
import java.util.UUID;
import java.util.logging.Logger;

public class LoggingEmailNotifier implements EmailNotifier {

  private static final Logger LOGGER = Logger.getLogger(LoggingEmailNotifier.class.getName());

  @Override
  public void sendInvitation(String recipient, UUID planId, String token) {
    LOGGER.info(() ->
      "SMTP no configurado: invitación disponible en Mis planes para el plan " + planId
    );
  }
}
