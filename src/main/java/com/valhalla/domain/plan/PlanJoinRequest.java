package com.valhalla.domain.plan;

import com.valhalla.domain.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(
  name = "plan_join_requests",
  uniqueConstraints = @UniqueConstraint(columnNames = { "plan_id", "requester_id" })
)
public class PlanJoinRequest {

  @Id
  @UuidGenerator(algorithm = UuidVersion7Generator.class)
  private UUID id;

  @Version
  private long version;

  @ManyToOne(optional = false)
  @JoinColumn(name = "plan_id", nullable = false)
  private Plan plan;

  @ManyToOne(optional = false)
  @JoinColumn(name = "requester_id", nullable = false)
  private User requester;

  protected PlanJoinRequest() {}

  public PlanJoinRequest(Plan plan, User requester) {
    this.plan = plan;
    this.requester = requester;
  }

  public UUID getId() {
    return id;
  }

  public User getRequester() {
    return requester;
  }
}
