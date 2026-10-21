package com.valhalla.domain.plan;

import java.util.UUID;

@FunctionalInterface
public interface EmailNotifier {
  void sendInvitation(String recipient, UUID planId, String token);
}
