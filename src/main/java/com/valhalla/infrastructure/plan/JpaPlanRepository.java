package com.valhalla.infrastructure.plan;

import com.valhalla.domain.plan.Plan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaPlanRepository extends JpaRepository<Plan, Long> {
  List<Plan> findByAdministratorId(Long administratorId);
  List<Plan> findByParticipantsEmail(String email);
  Optional<Plan> findByShortCode(String shortCode);
  boolean existsByShortCode(String shortCode);

  /**
   * [PPV] El listado publico. Sin ORDER BY a proposito: todavia no hay criterio de relevancia
   * (ver 14-PPV.md). La query es explicita en vez de derived porque el atributo se llama
   * {@code isPublic} y la resolucion derived-query de ese prefijo es ambigua.
   */
  @Query("select p from Plan p where p.isPublic = true")
  List<Plan> findByIsPublicTrue();
}
