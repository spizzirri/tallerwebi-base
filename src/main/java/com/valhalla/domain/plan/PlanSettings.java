package com.valhalla.domain.plan;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Objects;

@Embeddable
public class PlanSettings {

  @Enumerated(EnumType.STRING)
  @Column(name = "share_link_permission")
  private PlanPermission shareLink = PlanPermission.OWNER;

  @Enumerated(EnumType.STRING)
  @Column(name = "handle_requests_permission")
  private PlanPermission handleRequests = PlanPermission.OWNER;

  public PlanPermission getShareLink() {
    return shareLink == null ? PlanPermission.OWNER : shareLink;
  }

  public void setShareLink(PlanPermission shareLink) {
    this.shareLink = Objects.requireNonNull(shareLink);
  }

  public PlanPermission getHandleRequests() {
    return handleRequests == null ? PlanPermission.OWNER : handleRequests;
  }

  public void setHandleRequests(PlanPermission handleRequests) {
    if (handleRequests == PlanPermission.PARTICIPANTS) {
      throw new IllegalArgumentException(
        "Solo el dueño o los editores pueden gestionar solicitudes."
      );
    }
    this.handleRequests = Objects.requireNonNull(handleRequests);
  }

  public boolean isSharingEnabled() {
    return getShareLink() != PlanPermission.OWNER;
  }
}
