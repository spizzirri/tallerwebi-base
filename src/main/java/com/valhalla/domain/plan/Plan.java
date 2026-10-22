package com.valhalla.domain.plan;

import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "plans")
public class Plan {

  @Id
  @UuidGenerator(algorithm = UuidVersion7Generator.class)
  private UUID id;

  @Column(nullable = false)
  private String name;

  // Mismo motivo que Place.description: sin @Lob (oid en PostgreSQL) y con largo explicito, para
  // que una descripcion escrita por el usuario (el detalle demo siembra ~1500 caracteres) entre
  // en ambos dialectos.
  @Column(length = 2048)
  private String description;

  @Embedded
  private PlanSchedule schedule = new PlanSchedule();

  private Boolean isPublic = false;

  @Column(unique = true)
  private String shortCode;

  @ManyToOne
  @JoinColumn(name = "administrator_id")
  private User administrator;

  @Embedded
  private PlanManager planManager = new PlanManager();

  @Embedded
  private PlanSettings settings = new PlanSettings();

  public PlanSettings getSettings() {
    if (settings == null) {
      settings = new PlanSettings();
    }
    return settings;
  }

  public List<PlanJoinRequest> getJoinRequests() {
    return planManager.getJoinRequests();
  }

  public boolean canShareLink(String email) {
    return getSettings().getShareLink().allows(this, email);
  }

  public boolean canInviteInternal(String email) {
    return getSettings().getInviteInternal().allows(this, email);
  }

  public boolean canInviteByEmail(String email) {
    return getSettings().getInviteByEmail().allows(this, email);
  }

  public boolean canHandleRequests(String email) {
    return getSettings().getHandleRequests().allows(this, email);
  }

  public boolean hasPendingJoinRequest(String email) {
    return (
      email != null &&
      getJoinRequests()
        .stream()
        .anyMatch(request -> email.equals(request.getRequester().getEmail()))
    );
  }

  public List<PlanInvitation> getInvitations() {
    return planManager.getInvitations();
  }

  public List<User> getEditors() {
    return planManager.getEditors();
  }

  public boolean isEditor(String email) {
    return (
      isParticipant(email) && getEditors().stream().anyMatch(user -> email.equals(user.getEmail()))
    );
  }

  public boolean canEdit(String email) {
    return isAdministrator(email) || isEditor(email);
  }

  public List<User> getParticipants() {
    return planManager.getParticipants();
  }

  public boolean isAdministrator(String email) {
    return email != null && administrator != null && email.equals(administrator.getEmail());
  }

  public boolean isParticipant(String email) {
    return (
      email != null && getParticipants().stream().anyMatch(user -> email.equals(user.getEmail()))
    );
  }

  @OneToMany(mappedBy = "plan", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @OrderBy("sortOrder ASC")
  private List<PlanPlace> planPlaces = new ArrayList<>();

  public List<PlanPlace> getPlanPlaces() {
    return planPlaces;
  }

  public void addPlanPlace(PlanPlace planPlace) {
    planPlaces.add(planPlace);
    planPlace.setPlan(this);
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  public LocalDate getEventDate() {
    return schedule == null ? null : schedule.getDate();
  }

  public void setEventDate(LocalDate eventDate) {
    ensureSchedule().setDate(eventDate);
  }

  @DateTimeFormat(pattern = "HH:mm")
  public LocalTime getEventTime() {
    return schedule == null ? null : schedule.getTime();
  }

  public void setEventTime(LocalTime eventTime) {
    ensureSchedule().setTime(eventTime);
  }

  private PlanSchedule ensureSchedule() {
    if (schedule == null) {
      schedule = new PlanSchedule();
    }
    return schedule;
  }

  public Boolean getIsPublic() {
    return isPublic != null ? isPublic : false;
  }

  public void setIsPublic(Boolean isPublic) {
    this.isPublic = isPublic;
  }

  public String getShortCode() {
    return shortCode;
  }

  public void setShortCode(String shortCode) {
    this.shortCode = shortCode;
  }

  public User getAdministrator() {
    return administrator;
  }

  public void setAdministrator(User administrator) {
    this.administrator = administrator;
  }

  /**
   * Copies only the fields the form owns. {@code id}, {@code shortCode} and {@code administrator}
   * are deliberately left alone: a form post never carries them, so copying them would blank the
   * owner and the share code on every update.
   */
  public void updateFrom(Plan changes) {
    this.name = changes.getName();
    this.description = changes.getDescription();
    setEventDate(changes.getEventDate());
    setEventTime(changes.getEventTime());
    this.isPublic = changes.getIsPublic();
  }
}
