package com.valhalla.domain.plan;

import com.valhalla.domain.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class PlanManager {

  private static final String PLAN_ID_COLUMN = "plan_id";
  private static final String USER_ID_COLUMN = "user_id";

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
    name = "plan_participants",
    joinColumns = @JoinColumn(name = PLAN_ID_COLUMN),
    inverseJoinColumns = @JoinColumn(name = USER_ID_COLUMN),
    uniqueConstraints = @UniqueConstraint(columnNames = { PLAN_ID_COLUMN, USER_ID_COLUMN })
  )
  private List<User> participants = new ArrayList<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
    name = "plan_editors",
    joinColumns = @JoinColumn(name = PLAN_ID_COLUMN),
    inverseJoinColumns = @JoinColumn(name = USER_ID_COLUMN),
    uniqueConstraints = @UniqueConstraint(columnNames = { PLAN_ID_COLUMN, USER_ID_COLUMN })
  )
  private List<User> editors = new ArrayList<>();

  @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PlanInvitation> invitations = new ArrayList<>();

  public List<User> getParticipants() {
    return participants;
  }

  public List<User> getEditors() {
    return editors;
  }

  public List<PlanInvitation> getInvitations() {
    return invitations;
  }
}
