package com.valhalla.domain.follow;

import com.valhalla.domain.plan.UuidVersion7Generator;
import com.valhalla.domain.user.User;
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
 * [FOLLOW] Arista dirigida follower -> followed: un usuario sigue a otro.
 *
 * <p>Tabla propia con unique (follower_id, followed_id), mismo patron que {@code PlanRating}:
 * {@link User} no gana la coleccion inversa. Los conteos y las listas se leen por consultas
 * agregadas sobre esta tabla, nunca con un {@code size()} sobre una coleccion LAZY (AC-15).
 */
@Entity
@Table(
  name = "follows",
  uniqueConstraints = @UniqueConstraint(columnNames = { "follower_id", "followed_id" })
)
public class Follow {

  @Id
  @UuidGenerator(algorithm = UuidVersion7Generator.class)
  private UUID id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "follower_id", nullable = false)
  private User follower;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "followed_id", nullable = false)
  private User followed;

  public Follow() {}

  public Follow(User follower, User followed) {
    this.follower = follower;
    this.followed = followed;
  }

  public UUID getId() {
    return id;
  }

  public User getFollower() {
    return follower;
  }

  public User getFollowed() {
    return followed;
  }
}
