package com.valhalla.domain.rating;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.UuidVersion7Generator;
import com.valhalla.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/**
 * [CAL] Calificacion de 1 a 5 estrellas que un usuario logueado le da a un plan publico.
 *
 * <p>La autoria se guarda (user_id + unique plan_id/user_id) pero nunca se exhibe: el HTML solo
 * muestra agregados (promedio + cantidad). La autoria interna es la que permite el upsert y que el
 * usuario reconozca su propio puntaje preseleccionado.
 *
 * <p>{@link Plan} no tiene la coleccion inversa: las calificaciones se leen por agregados, no
 * recorriendo la lista (ver 16-CAL.md).
 */
@Entity
@Table(
  name = "plan_ratings",
  uniqueConstraints = @UniqueConstraint(columnNames = { "plan_id", "user_id" })
)
public class PlanRating {

  @Id
  @UuidGenerator(algorithm = UuidVersion7Generator.class)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "plan_id", nullable = false)
  private Plan plan;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private int score;

  public PlanRating() {}

  public PlanRating(Plan plan, User user, int score) {
    this.plan = plan;
    this.user = user;
    this.score = score;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public Plan getPlan() {
    return plan;
  }

  public User getUser() {
    return user;
  }
}
