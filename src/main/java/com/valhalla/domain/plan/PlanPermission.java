package com.valhalla.domain.plan;

public enum PlanPermission {
  OWNER,
  EDITORS,
  PARTICIPANTS;

  public boolean allows(Plan plan, String email) {
    return (
      plan.isAdministrator(email) ||
      (this != OWNER && plan.isEditor(email)) ||
      (this == PARTICIPANTS && plan.isParticipant(email))
    );
  }
}
