# [TOP] Ordenado de planes populares

> Trello: https://trello.com/c/ErLgnl51/34-top-algoritmo-de-ordenado-de-planes-populares-recomendados
> **Estimacion:** 3 pts Fibonacci
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.

## Objetivo

`GET /for-you` deja de mostrar los planes publicos en el orden que devuelve la base y los ordena
por **popularidad**: una calificacion bayesiana que combina el promedio y la cantidad de votos,
con la cantidad de participantes como primer desempate. Cualquier visitante ve primero los planes
mejor valorados por la comunidad, y los planes nuevos sin calificaciones no compiten contra el
promedio de quien ya tiene historial.

Esta card es **solo el criterio de orden**. La busqueda y los filtros son [SEARCH]
(`18-SEARCH.md`), que consume el comparador definido aca y no define uno propio.

## Pre-requisitos

- [PPV] completed (`GET /for-you`, `PlanFeedCards`, filtro de planes propios)
- [CAL] completed (`PlanRatingService.getSummariesByPlanId()`, `RatingSummary`)

## Decisiones / aclaraciones

- **Promedio crudo no.** Con promedio simple, un plan con un unico voto de 5 le gana a uno con 50
  votos de 4.8. La formula bayesiana pondera por evidencia:

  ```
  score = (5 · 3.7 + promedio · cantidad) / (5 + cantidad)
  ```

  `5` son "votos virtuales" de peso y `3.7` el promedio de ese prior. Un plan con 0 votos arranca
  en 3.7 y se acerca a su promedio real a medida que junta votos. Las dos constantes viven en un
  solo lugar (`PlanDiscovery`), no repartidas por el codigo.
- **Los planes sin calificaciones van despues de los calificados.** Es una decision de producto,
  no un efecto de la formula: asi el feed no queda dominado por el prior y un plan nuevo no
  aparece por encima de uno con historial real. Entre los no calificados manda el resto de los
  desempates.
- **Participantes = `plan_participants`.** El owner **no** cuenta (vive en `administrator`, no en
  la tabla de participantes). Un editor **si** cuenta: es un participante con rol elevado, y su
  fila existe en `plan_participants` ademas de `plan_editors`.
- **El conteo va por query.** `plan.getParticipants()` es `@ManyToMany(LAZY)`: recorrerlo desde el
  controller dispara `LazyInitializationException` o un N+1 sobre cada tarjeta. El conteo es una
  query agrupada (`count(participant)` con `left join`), y un plan sin participantes aparece con
  `0`, no ausente.
- **El score no se muestra.** La tarjeta sigue mostrando promedio y cantidad ([CAL] AC-10); el
  score solo ordena. Exponerlo agregaria un numero que el usuario no puede interpretar.
- **Orden determinista.** Fecha, nombre e id cierran todos los empates: dos requests con los
  mismos datos devuelven el mismo orden, y los tests no dependen del orden de la base.
- **Alcance.** Solo `GET /for-you`. `GET /api/plans/public` (el sidebar de `/explore`) mantiene su
  orden por fecha proxima y su limite de 5: su label dice "proximos", no "populares".
- **Efecto en el seeder de demo.** Con los datos de `PublicPlanDataSeeder`, el orden esperado es:
  Parrillada (3.93) → Sabado de museos (3.83) → Cerveza (3.69) → Asado (sin votos, 4
  participantes) → Milonga (sin votos, 1) → Cafe (sin votos, 1; la fecha desempata). Es lo que se
  ve en `docker-dev.sh` (`-Dseed.demoPlans=true`). Ni la suite de integracion ni la E2E encienden
  ese seeder: arman sus propios planes, asi que el orden de demo no es un fixture de test.

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | `GET /for-you` sin filtros devuelve los planes publicos ordenados por popularidad descendente |
| AC-02 | La popularidad se calcula como `(5 · 3.7 + promedio · cantidad) / (5 + cantidad)`; un plan sin calificaciones se ordena despues de todos los calificados |
| AC-03 | Entre dos planes con el mismo grupo y la misma popularidad, gana el que tiene mas participantes |
| AC-04 | Si sigue el empate: fecha del evento mas proxima primero (los planes sin fecha van al final), despues nombre alfabetico case-insensitive y despues id |
| AC-05 | Dos requests sobre los mismos datos devuelven exactamente el mismo orden |
| AC-06 | El orden no cambia la visibilidad: siguen apareciendo solo planes publicos y nunca los propios del usuario logueado |
| AC-07 | El orden no altera el contrato de la tarjeta: las mismas claves y los mismos valores de `ratingAverage` / `ratingCount` que antes de esta card |
| AC-08 | `GET /api/plans/public` conserva su orden por fecha y su limite de 5 tarjetas, sin cambios |
| AC-09 | Un plan sin participantes y sin calificaciones se ordena igual que el resto: cuenta `0` en ambos insumos, no rompe ni excluye el listado |

## Escenarios de Test

### Tests Unitarios (`domain/plan/PlanDiscoveryTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | `popularityScore(null, 0)` devuelve el prior (3.7) | AC-02 |
| U-02 | 4 votos con promedio 4.0 dan 3.83 (redondeado a 2 decimales) | AC-02 |
| U-03 | Un plan con 1 voto de 5 (3.92) queda debajo de uno con 20 votos de 4.5 (4.34) | AC-02 |
| U-04 | Un plan sin calificaciones queda despues de uno calificado aunque tenga mas participantes | AC-02 |
| U-05 | Misma popularidad entre calificados: gana el de mas participantes | AC-03 |
| U-06 | Mismo grupo, misma popularidad y mismos participantes: gana la fecha mas proxima | AC-04 |
| U-07 | Fechas nulas van al final del desempate por fecha | AC-04 |
| U-08 | Empate total: nombre case-insensitive; si persiste, id ascendente | AC-04, AC-05 |
| U-09 | El mismo input ordenado dos veces produce la misma lista | AC-05 |
| U-10 | Lista vacia devuelve lista vacia sin error | AC-09 |
| U-11 | Un plan sin participantes se ordena con 0 y no se descarta | AC-09 |
| U-12 | `orderByPopularity` no modifica la lista original: devuelve una lista nueva | AC-05 |

### Tests Unitarios (`infrastructure/plan/PlanRepositoryImplTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-13 | `countParticipantsByPlanIds()` delega en `JpaPlanRepository` y devuelve la lista de conteos | AC-03 |

### Tests Unitarios (`infrastructure/plan/PlanServiceImplTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-14 | `getParticipantCounts()` mapea la lista del repositorio a `Map<UUID, Long>` | AC-03 |
| U-15 | `getParticipantCounts()` con coleccion vacia devuelve mapa vacio y no consulta el repositorio | AC-09 |

### Tests de Integracion (`infrastructure/plan/JpaPlanRepositoryTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-01 | `countParticipantsByPlanIds()` cuenta solo participantes (no al owner) de cada plan | AC-03 |
| I-02 | Un plan sin participantes aparece con `0` en el resultado | AC-09 |

### Tests de Integracion (`integration/PlanControllerIntegrationTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-03 | `GET /for-you` con tres planes publicos los muestra en orden de popularidad | AC-01, AC-02 |
| I-04 | El plan con calificaciones aparece antes que el plan sin calificaciones con mas participantes | AC-02 |
| I-05 | `GET /for-you` sigue sin mostrar planes privados ni los propios del logueado | AC-06 |

### E2E (minimos)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `PublicPlansRankingE2E` (nuevo) | El admin crea dos planes publicos por UI; un visitante se registra y califica uno de ellos; el feed muestra primero el calificado | AC-01, AC-02 |

La E2E **no** puede apoyarse en el seeder de demo: `E2eBase` limpia la base antes de cada test y
solo resiembra al admin y los lugares (`ResetDatabase.cleanDatabase()`), y `PublicPlanDataSeeder`
esta apagado por defecto. El fixture se arma como en `RatingE2E`: crear el plan por UI, capturar
la URL publica y calificar desde ahi.

## Referencia de Implementacion

> Los pasos a continuacion son guia de implementacion, no reemplazan los criterios de aceptacion.

### 1. Conteo de participantes

File: `src/main/java/com/valhalla/domain/plan/PlanParticipantCount.java` (crear)

```java
package com.valhalla.domain.plan;

import java.util.UUID;

/**
 * [TOP] Conteo de participantes por plan. Top-level para poder usarlo como constructor
 * expression en JPQL, igual que RatingSummary.
 */
public record PlanParticipantCount(UUID planId, Long count) {}
```

File: `src/main/java/com/valhalla/infrastructure/plan/JpaPlanRepository.java`

```java
@Query(
  "select new com.valhalla.domain.plan.PlanParticipantCount(plan.id, count(participant)) " +
  "from Plan plan left join plan.planManager.participants participant " +
  "where plan.id in :planIds group by plan.id"
)
List<PlanParticipantCount> countParticipantsByPlanIds(@Param("planIds") Collection<UUID> planIds);
```

El `left join` + `group by` devuelve `0` para un plan sin participantes: no hay que completar el
mapa con ceros en Java.

File: `src/main/java/com/valhalla/domain/plan/PlanRepository.java`

```java
List<PlanParticipantCount> countParticipantsByPlanIds(Collection<UUID> planIds);
```

File: `src/main/java/com/valhalla/infrastructure/plan/PlanRepositoryImpl.java`

```java
@Override
public List<PlanParticipantCount> countParticipantsByPlanIds(Collection<UUID> planIds) {
  return jpaPlanRepository.countParticipantsByPlanIds(planIds);
}
```

File: `src/main/java/com/valhalla/domain/plan/PlanService.java`

```java
/** [TOP] Participantes por plan, para el ranking del feed. Vacio si no hay ids. */
Map<UUID, Long> getParticipantCounts(Collection<UUID> planIds);
```

File: `src/main/java/com/valhalla/infrastructure/plan/PlanServiceImpl.java`

```java
@Override
@Transactional(readOnly = true)
public Map<UUID, Long> getParticipantCounts(Collection<UUID> planIds) {
  if (planIds == null || planIds.isEmpty()) {
    return Map.of();
  }
  return planRepository
    .countParticipantsByPlanIds(planIds)
    .stream()
    .collect(Collectors.toMap(PlanParticipantCount::planId, PlanParticipantCount::count));
}
```

`in :planIds` con una coleccion vacia no es valido en JPQL: el corte temprano no es optimizacion,
es correccion.

### 2. Ranking

File: `src/main/java/com/valhalla/domain/plan/PlanDiscovery.java` (crear)

```java
package com.valhalla.domain.plan;

import com.valhalla.domain.rating.RatingSummary;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** [TOP] Ranking de popularidad del feed: funcion pura, sin Spring y sin acceso a datos. */
public final class PlanDiscovery {

  /** Votos virtuales del prior: cuanto pesa el promedio inicial frente a los votos reales. */
  static final double PRIOR_VOTES = 5.0;
  /** Promedio del prior: un plan sin votos arranca aca. */
  static final double PRIOR_MEAN = 3.7;

  private PlanDiscovery() {}

  public static double popularityScore(Double average, Long count) {
    long votes = count == null ? 0L : count;
    double sum = average == null ? 0.0 : average * votes;
    return (PRIOR_VOTES * PRIOR_MEAN + sum) / (PRIOR_VOTES + votes);
  }

  /**
   * Orden de popularidad: calificados antes que sin calificar, despues score descendente,
   * participantes descendentes, fecha ascendente (nulos al final), nombre y id.
   */
  public static Comparator<Plan> popularityOrder(
    Map<UUID, RatingSummary> summaries,
    Map<UUID, Long> participantCounts
  ) {
    return Comparator
      .comparingInt((Plan plan) -> hasRatings(plan, summaries) ? 0 : 1)
      .thenComparing(
        Comparator.comparingDouble(
          (Plan plan) -> -popularityScore(average(plan, summaries), votes(plan, summaries))
        )
      )
      .thenComparing(
        Comparator.comparingLong((Plan plan) -> -participants(plan, participantCounts))
      )
      .thenComparing(Plan::getEventDate, Comparator.nullsLast(Comparator.naturalOrder()))
      .thenComparing(plan -> normalize(plan.getName()))
      .thenComparing(Plan::getId);
  }

  public static List<Plan> orderByPopularity(
    List<Plan> plans,
    Map<UUID, RatingSummary> summaries,
    Map<UUID, Long> participantCounts
  ) {
    return plans.stream().sorted(popularityOrder(summaries, participantCounts)).toList();
  }

  private static boolean hasRatings(Plan plan, Map<UUID, RatingSummary> summaries) {
    RatingSummary summary = summaries.get(plan.getId());
    return summary != null && summary.count() != null && summary.count() > 0;
  }

  private static Double average(Plan plan, Map<UUID, RatingSummary> summaries) {
    RatingSummary summary = summaries.get(plan.getId());
    return summary == null ? null : summary.average();
  }

  private static Long votes(Plan plan, Map<UUID, RatingSummary> summaries) {
    RatingSummary summary = summaries.get(plan.getId());
    return summary == null ? 0L : summary.count();
  }

  private static long participants(Plan plan, Map<UUID, Long> participantCounts) {
    return participantCounts.getOrDefault(plan.getId(), 0L);
  }

  private static String normalize(String value) {
    return value == null ? "" : value.toLowerCase(Locale.ROOT);
  }
}
```

`stream().sorted(...).toList()` devuelve una lista nueva: nunca se reordena la que devuelve el
repositorio.

### 3. Controller

File: `src/main/java/com/valhalla/presentation/plan/ForYouController.java`

```java
Map<UUID, RatingSummary> summaries = this.planRatingService.getSummariesByPlanId();
List<Plan> publicPlans = planService
  .getPublicPlans()
  .stream()
  .filter(plan -> currentEmail == null || !plan.isAdministrator(currentEmail))
  .toList();
Map<UUID, Long> participantCounts = this.planService.getParticipantCounts(
  publicPlans.stream().map(Plan::getId).toList()
);
model.put(
  ATTR_PUBLIC_PLANS,
  PlanDiscovery
    .orderByPopularity(publicPlans, summaries, participantCounts)
    .stream()
    .map(plan -> PlanFeedCards.planCard(plan, summaries))
    .toList()
);
```

El filtro de propios se aplica **antes** del ranking: el conteo de participantes solo se pide para
los planes que se van a mostrar.

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `domain/plan/PlanParticipantCount.java` | Crear |
| `domain/plan/PlanDiscovery.java` | Crear (score + `popularityOrder` + `orderByPopularity`) |
| `domain/plan/PlanRepository.java` | Actualizar (`countParticipantsByPlanIds`) |
| `domain/plan/PlanService.java` | Actualizar (`getParticipantCounts`) |
| `infrastructure/plan/JpaPlanRepository.java` | Actualizar (query agrupada de participantes) |
| `infrastructure/plan/PlanRepositoryImpl.java` | Actualizar (delegacion) |
| `infrastructure/plan/PlanServiceImpl.java` | Actualizar (`getParticipantCounts`) |
| `presentation/plan/ForYouController.java` | Actualizar (ordena por popularidad) |
| `docs/specs/14-PPV.md` | Actualizar (AC-05 y nota de ranking) |
| `docs/specs/16-CAL.md` | Actualizar (AC-11 y decision "Feed sin reordenar") |
| `presentation/plan/ForYouControllerTest.java` | Actualizar (mocks y orden) |
| `infrastructure/plan/PlanRepositoryImplTest.java` | Actualizar (U-13) |
| `infrastructure/plan/PlanServiceImplTest.java` | Actualizar (U-14, U-15) |
| `infrastructure/plan/JpaPlanRepositoryTest.java` | Actualizar (I-01, I-02) |
| `integration/PlanControllerIntegrationTest.java` | Actualizar (I-03..I-05) |
| `domain/plan/PlanDiscoveryTest.java` | Crear (U-01..U-12) |
| `e2e/PublicPlansRankingE2E.java` | Crear (E-01) |
| `odd/tasks/plan-discovery-ranking-search.md` | Actualizar (progreso y hashes) |
