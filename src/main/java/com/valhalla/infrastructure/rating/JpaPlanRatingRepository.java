package com.valhalla.infrastructure.rating;

import com.valhalla.domain.rating.PlanRating;
import com.valhalla.domain.rating.RatingSummary;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaPlanRatingRepository extends JpaRepository<PlanRating, UUID> {
  /** Autoria interna: busca la calificacion de un usuario sobre un plan para el upsert. */
  @Query(
    "select rating from PlanRating rating where rating.plan.id = :planId and rating.user.email = :email"
  )
  Optional<PlanRating> findByPlanIdAndUserEmail(UUID planId, String email);

  /**
   * [MYRAT] Las calificaciones del usuario para la seccion "Mis calificaciones" del perfil. El id
   * es UUID v7, asi que el orden descendente es el de calificacion mas reciente primero, el mismo
   * criterio que Guardados.
   */
  @Query(
    "select rating from PlanRating rating where rating.user.email = :email order by rating.id desc"
  )
  List<PlanRating> findByUserEmail(String email);

  /**
   * [CAL] Una sola query agrupada alimenta el indicador de todas las tarjetas: sin N+1 en el feed
   * y sin coleccion inversa en Plan (ver 16-CAL.md).
   */
  @Query(
    "select new com.valhalla.domain.rating.RatingSummary(rating.plan.id, avg(rating.score), count(rating)) from PlanRating rating group by rating.plan.id"
  )
  List<RatingSummary> findSummaries();

  /** [CAL] Al borrar el plan hay que borrar sus calificaciones primero: FK owner. */
  void deleteAllByPlanId(UUID planId);
}
