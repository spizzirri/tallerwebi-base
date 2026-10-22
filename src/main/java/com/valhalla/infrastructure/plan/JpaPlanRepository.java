package com.valhalla.infrastructure.plan;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanParticipantCount;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaPlanRepository extends JpaRepository<Plan, UUID> {
  List<Plan> findByAdministratorId(UUID administratorId);

  @Query(
    "select distinct plan from Plan plan join plan.planManager.participants participant where participant.email = :email"
  )
  List<Plan> findByParticipantsEmail(@Param("email") String email);

  @Query(
    "select distinct plan from Plan plan join plan.planManager.invitations invitation where invitation.recipient.email = :email"
  )
  List<Plan> findByInvitationsRecipientEmail(@Param("email") String email);

  Optional<Plan> findByShortCode(String shortCode);
  boolean existsByShortCode(String shortCode);

  /**
   * [PPV] El listado publico. Sin ORDER BY a proposito: todavia no hay criterio de relevancia
   * (ver 14-PPV.md). La query es explicita en vez de derived porque el atributo se llama
   * {@code isPublic} y la resolucion derived-query de ese prefijo es ambigua.
   */
  @Query("select p from Plan p where p.isPublic = true")
  List<Plan> findByIsPublicTrue();

  /**
   * [FOLLOW] Planes publicos de un conjunto de autores, en una sola consulta (AC-08, AC-15). Sin
   * ORDER BY a proposito: el orden y los filtros siguen siendo de {@code PlanDiscovery}. La query
   * es explicita por el mismo motivo que {@link #findByIsPublicTrue()}: el atributo se llama
   * {@code isPublic} y la resolucion derived de ese prefijo es ambigua.
   */
  @Query(
    "select plan from Plan plan where plan.isPublic = true and plan.administrator.id in :administratorIds"
  )
  List<Plan> findByIsPublicTrueAndAdministratorIdIn(
    @Param("administratorIds") Collection<UUID> administratorIds
  );

  /**
   * [TOP] Participantes por plan, con {@code left join} para que un plan sin participantes
   * aparezca con {@code 0} en vez de ausente. El owner no cuenta: no vive en esta tabla.
   */
  @Query(
    "select new com.valhalla.domain.plan.PlanParticipantCount(plan.id, count(participant)) " +
    "from Plan plan left join plan.planManager.participants participant " +
    "where plan.id in :planIds group by plan.id"
  )
  List<PlanParticipantCount> countParticipantsByPlanIds(@Param("planIds") Collection<UUID> planIds);
}
