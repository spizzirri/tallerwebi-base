package com.valhalla.domain.plan;

import com.valhalla.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(
  name = "plan_invitations",
  uniqueConstraints = @UniqueConstraint(columnNames = { "plan_id", "recipient_id" })
)
public class PlanInvitation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  private long version;

  @ManyToOne(optional = false)
  @JoinColumn(name = "plan_id", nullable = false)
  private Plan plan;

  @ManyToOne(optional = false)
  @JoinColumn(name = "recipient_id", nullable = false)
  private User recipient;

  @ManyToOne(optional = false)
  @JoinColumn(name = "inviter_id", nullable = false)
  private User inviter;

  @Column(nullable = false)
  private String role;

  protected PlanInvitation() {}

  public PlanInvitation(Plan plan, User recipient, User inviter, String role) {
    this.plan = plan;
    this.recipient = recipient;
    this.inviter = inviter;
    this.role = role;
  }

  public Long getId() {
    return id;
  }

  public Plan getPlan() {
    return plan;
  }

  public User getRecipient() {
    return recipient;
  }

  public User getInviter() {
    return inviter;
  }

  public String getRole() {
    return role;
  }
}
