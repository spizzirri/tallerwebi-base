# [APL-BE] Agregar Lugares al Plan (Backend)

> Trello: https://trello.com/c/zHj13Gy6/6-apl-be-agregar-lugares-al-plan-backend
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.

## Objetivo

Backend para agregar lugares a planes: entity PlanPlace, repository, service, REST endpoints y actualizacion de PlaceController para exponer los planes del usuario en el panel lateral de `/places`. Los endpoints de escritura exigen sesion, validan ownership con el criterio "not found" y el ignore list de CSRF se reduce a las rutas de lectura.

## Pre-requisitos

- [PLC] completed (Place entity)
- [PLN] completed (Plan entity)

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | Se puede agregar un lugar a un plan via POST /api/plans/{planId}/places?placeId=X |
| AC-02 | No se puede agregar el mismo lugar dos veces al mismo plan |
| AC-03 | Al agregar un lugar se asigna un sortOrder automatico (siguiente al maximo) |
| AC-04 | GET /api/plans/{planId}/places retorna el itinerario ordenado por sortOrder |
| AC-05 | PUT /api/plans/{planId}/places/{id} actualiza visitDate y visitTime |
| AC-06 | DELETE /api/plans/{planId}/places/{id} elimina un lugar del plan |
| AC-07 | POST /api/plans/{planId}/places/reorder reordena los lugares del plan |
| AC-08 | El panel lateral de `/places` incluye los planes del usuario logueado para el lugar seleccionado |
| AC-09 | PlanPlace tiene relacion ManyToOne con Plan y Place (LAZY) |
| AC-10 | La combinacion (plan_id, place_id) es unica (unique constraint) |
| AC-11 | La escritura en `/api/plans/**` (POST/PUT/DELETE) exige sesion autenticada. `GET /api/places` sigue siendo lectura publica — es catalogo, no dato de usuario. `GET /api/plans/{id}/places` sigue siendo legible sin sesion **solo si el plan es publico** (lo necesita la vista compartida de [VPC]); con plan privado responde 404 si no sos el owner |
| AC-12 | El ignore list de CSRF se reduce a las rutas de lectura. Hoy `csrf.ignoringRequestMatchers("/api/**")` deja **toda** la API exenta: exigir sesion solo no cierra CSRF, porque un `<form>` cross-origin con la cookie de sesion del usuario entra igual. O se reduce a `/api/places/**`, o se saca `/api/**` del ignore y se confía en el token de Spring para toda la escritura |
| AC-13 | Ownership con criterio "not found, no forbidden": un usuario que no es owner del plan recibe 404, nunca 403, para no confirmar que el id existe. Reusa el patron ya implementado en `PlanServiceImpl.getOwnedPlan` |

## Escenarios de Test

### Tests Unitarios (`domain/planplace/PlanPlaceServiceImplTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | `addPlaceToPlan()` agrega lugar con sortOrder correcto | AC-01, AC-03 |
| U-02 | `addPlaceToPlan()` con lugar duplicado lanza IllegalStateException | AC-02 |
| U-03 | `getItinerary()` retorna lugares ordenados por sortOrder | AC-04 |
| U-04 | `updatePlanPlace()` actualiza visitDate/visitTime | AC-05 |
| U-05 | `removePlaceFromPlan()` elimina el lugar del plan | AC-06 |
| U-06 | `reorderPlaces()` reordena segun la lista recibida | AC-07 |
| U-07 | `isPlaceInPlan()` retorna true si el lugar esta en el plan | AC-02 |
| U-08 | `isPlaceInPlan()` retorna false si el lugar no esta en el plan | AC-02 |

### Tests de Integracion (`integration/PlanPlaceRestControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-01 | `POST /api/plans/{planId}/places?placeId=X` agrega lugar | AC-01 |
| I-02 | `POST` con lugar duplicado retorna error | AC-02 |
| I-03 | `GET /api/plans/{planId}/places` retorna itinerario ordenado | AC-04 |
| I-04 | `PUT /api/plans/{planId}/places/{id}` actualiza fecha/hora | AC-05 |
| I-05 | `DELETE /api/plans/{planId}/places/{id}` elimina lugar | AC-06 |
| I-06 | `POST /api/plans/{planId}/places/reorder` reordena | AC-07 |

### Tests de Integracion (`infrastructure/PlanPlaceRepositoryTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-07 | `findByPlanIdOrderBySortOrder()` retorna ordenados | AC-04 |
| I-08 | `existsByPlanIdAndPlaceId()` retorna true si existe | AC-10 |
| I-09 | `existsByPlanIdAndPlaceId()` retorna false si no existe | AC-10 |
| I-10 | Unique constraint previene duplicados | AC-10 |

### Tests de Integracion (`integration/PlaceControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-11 | `GET /places` con sesion incluye `userPlans` en el model | AC-08 |
| I-12 | `GET /places` sin sesion no incluye `userPlans` | AC-08 |

### Tests de Seguridad (`integration/PlanPlaceSecurityTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| S-01 | `POST /api/plans/{id}/places?placeId=X` sin sesion → 302 a `/auth/login`, no escribe nada | AC-11 |
| S-02 | `PUT /api/plans/{id}/places/{planPlaceId}` sin sesion → 302, no escribe nada | AC-11 |
| S-03 | `DELETE /api/plans/{id}/places/{planPlaceId}` sin sesion → 302, no borra nada | AC-11 |
| S-04 | `GET /api/places` sin sesion → 200 con JSON (lectura publica, catalogo) | AC-11 |
| S-05 | `POST /api/plans/{id}/places` con sesion ajena al plan → **404**, nunca 403 | AC-13 |
| S-06 | `POST /api/plans/{id}/places` con sesion ajena al plan no crea el PlanPlace | AC-13 |
| S-07 | `POST /api/plans/{id}/places` sin token CSRF → 403 (una sesion valida no alcanza) | AC-12 |
| S-08 | `GET /api/plans/{id}/places` de un plan **privado** sin sesion → 404; del mismo plan **publico** → 200 con itinerario | AC-11, AC-13 |

### E2E (minimos)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `AddPlaceToPlanE2E` | Agregar lugar a plan, verificar en itinerario | AC-01, AC-04 |

## Notas / decisiones de diseño

- **CSRF: exigir sesion no alcanza (AC-12)**. Hoy `SecurityConfig` @Order(3) tiene
  `.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))` y ademas
  `.requestMatchers("/", "/share/**", "/api/**", "/reload/**").permitAll()`. Si solo se agrega
  `authenticated()` a la escritura, un `<form>` cross-origin `POST` con la `JSESSIONID` del
  usuario entra igual y el token no se pide nunca. El AC obliga a **reducir el ignore list**:
  `ignoringRequestMatchers("/api/places/**")` para las lecturas, o sacar `/api/**` del ignore y
  confiar en el token de Spring para toda la escritura. S-07 es el test que distingue las dos
  opciones.
- **Ownership "not found, no forbidden" (AC-13)**. `PlanServiceImpl.getOwnedPlan` ya lo hace y
  dice por que en un comentario: *"A plan owned by somebody else answers as missing: 'forbidden'
  would confirm the id exists."* Un 403 en `/api/plans/{id}/places` confirmaria que ese id
  existe. Los endpoints de APL-BE reciben `ownerEmail` y resuelven con `getOwnedPlan`; los ids
  no existentes y los ajenos responden igual.
- **Gate de JaCoCo, sin margen**. La regla de `pom.xml` es a nivel `PACKAGE` con
  `com.valhalla.domain.*` y `com.valhalla.presentation.*` al **100%** de LINE COVEREDRATIO,
  `infrastructure` al 80% y el bundle al 80%. `PlanPlace`, `PlanPlaceService` y
  `PlanPlaceController` caen en las dos primeras reglas: o están al 100% o CI falla.
- **`SecurityConfig` sí está excluido** del gate (`com/valhalla/config/*` en `<excludes>`), asi
  que AC-11/12/13 no se prueban con cobertura JaCoCo sino con los tests funcionales S-01..S-07.
  Los DTOs también están excluidos (`*Request.class`), lo que deja la validacion de entrada
  cubierta sin costo de cobertura.
- **El itinerario no es catálogo (AC-11/S-08)**. `GET /api/places` es el catalogo: puede seguir
  abierto. `GET /api/plans/{id}/places` es dato de usuario, asi que sin sesion solo responde si
  el plan es publico — es lo que necesita la vista compartida de [VPC] para leer el itinerario
  desde el `fetch`. Si el plan es privado y no sos el owner, 404 por el mismo criterio de AC-13.
- **AC-08 y el panel lateral**. No hay `pages/places/detail.html` ni ruta `/places/{id}`: la ficha
  se rediseñó como panel lateral dentro de `/places` (D1). El "Add to plan" de 05-APL-FE vive
  en ese panel, que ya tiene toda la data del lugar, así que el backend solo tiene que poner
  `userPlans` en el model de `GET /places`.

## Referencia de Implementacion

> Los pasos a continuación son guía de implementación, no reemplazan los acceptance criteria de arriba.

### 1. Create PlanPlace entity

File: `src/main/java/com/valhalla/domain/planplace/PlanPlace.java`

```java
package com.valhalla.domain.planplace;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.plan.Plan;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "plan_places", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"plan_id", "place_id"})
})
public class PlanPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "visit_date")
    private LocalDate visitDate;

    @Column(name = "visit_time")
    private LocalTime visitTime;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // Getters and setters
}
```

### 2. Create PlanPlaceRepository

File: `src/main/java/com/valhalla/domain/planplace/PlanPlaceRepository.java`

```java
package com.valhalla.domain.planplace;

import java.util.List;
import java.util.Optional;

public interface PlanPlaceRepository {
    PlanPlace save(PlanPlace planPlace);
    List<PlanPlace> findByPlanId(Long planId);
    Optional<PlanPlace> findById(Long id);
    boolean existsByPlanIdAndPlaceId(Long planId, Long placeId);
    void deleteById(Long id);
}
```

### 3. Create PlanPlaceService

File: `src/main/java/com/valhalla/domain/planplace/PlanPlaceService.java`

```java
package com.valhalla.domain.planplace;

import java.util.List;

public interface PlanPlaceService {
    PlanPlace addPlaceToPlan(Long planId, Long placeId);
    List<PlanPlace> getItinerary(Long planId);
    PlanPlace updatePlanPlace(PlanPlace planPlace);
    void removePlaceFromPlan(Long planPlaceId);
    void reorderPlaces(Long planId, List<Long> placeIds);
    boolean isPlaceInPlan(Long planId, Long placeId);
}
```

File: `src/main/java/com/valhalla/domain/planplace/PlanPlaceServiceImpl.java`

```java
package com.valhalla.domain.planplace;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanPlaceServiceImpl implements PlanPlaceService {

    private final PlanPlaceRepository planPlaceRepository;
    private final PlanRepository planRepository;
    private final PlaceRepository placeRepository;

    @Autowired
    public PlanPlaceServiceImpl(
        PlanPlaceRepository planPlaceRepository,
        PlanRepository planRepository,
        PlaceRepository placeRepository
    ) {
        this.planPlaceRepository = planPlaceRepository;
        this.planRepository = planRepository;
        this.placeRepository = placeRepository;
    }

    @Override
    @Transactional
    public PlanPlace addPlaceToPlan(Long planId, Long placeId) {
        if (isPlaceInPlan(planId, placeId)) {
            throw new IllegalStateException("Place already in plan");
        }

        Plan plan = planRepository.findById(planId)
            .orElseThrow(() -> new RuntimeException("Plan not found"));
        Place place = placeRepository.findById(placeId)
            .orElseThrow(() -> new RuntimeException("Place not found"));

        List<PlanPlace> existing = planPlaceRepository.findByPlanId(planId);
        int nextOrder = existing.stream()
            .mapToInt(PlanPlace::getSortOrder)
            .max()
            .orElse(0) + 1;

        PlanPlace planPlace = new PlanPlace();
        planPlace.setPlan(plan);
        planPlace.setPlace(place);
        planPlace.setSortOrder(nextOrder);

        return planPlaceRepository.save(planPlace);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanPlace> getItinerary(Long planId) {
        return planPlaceRepository.findByPlanId(planId);
    }

    @Override
    public PlanPlace updatePlanPlace(PlanPlace planPlace) {
        return planPlaceRepository.save(planPlace);
    }

    @Override
    public void removePlaceFromPlan(Long planPlaceId) {
        planPlaceRepository.deleteById(planPlaceId);
    }

    @Override
    @Transactional
    public void reorderPlaces(Long planId, List<Long> placeIds) {
        AtomicInteger order = new AtomicInteger(1);
        placeIds.forEach(placeId -> {
            planPlaceRepository.findByPlanId(planId).stream()
                .filter(pp -> pp.getPlace().getId().equals(placeId))
                .findFirst()
                .ifPresent(pp -> {
                    pp.setSortOrder(order.getAndIncrement());
                    planPlaceRepository.save(pp);
                });
        });
    }

    @Override
    public boolean isPlaceInPlan(Long planId, Long placeId) {
        return planPlaceRepository.existsByPlanIdAndPlaceId(planId, placeId);
    }
}
```

### 4. Create JPA Repository

File: `src/main/java/com/valhalla/infrastructure/planplace/JpaPlanPlaceRepository.java`

```java
package com.valhalla.infrastructure.planplace;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPlanPlaceRepository extends JpaRepository<PlanPlace, Long> {
    @EntityGraph(attributePaths = {"place"})
    List<PlanPlace> findByPlanIdOrderBySortOrder(Long planId);

    boolean existsByPlanIdAndPlaceId(Long planId, Long placeId);
}
```

### 5. Create PlanPlaceRepositoryImpl

File: `src/main/java/com/valhalla/infrastructure/planplace/PlanPlaceRepositoryImpl.java`

```java
package com.valhalla.infrastructure.planplace;

import com.valhalla.domain.planplace.PlanPlaceRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PlanPlaceRepositoryImpl implements PlanPlaceRepository {

    private final JpaPlanPlaceRepository jpa;

    @Autowired
    public PlanPlaceRepositoryImpl(JpaPlanPlaceRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public PlanPlace save(PlanPlace planPlace) {
        return jpa.save(planPlace);
    }

    @Override
    public List<PlanPlace> findByPlanId(Long planId) {
        return jpa.findByPlanIdOrderBySortOrder(planId);
    }

    @Override
    public Optional<PlanPlace> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public boolean existsByPlanIdAndPlaceId(Long planId, Long placeId) {
        return jpa.existsByPlanIdAndPlaceId(planId, placeId);
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}
```

### 6. Create REST endpoints

File: `src/main/java/com/valhalla/presentation/plan/PlanPlaceRestController.java`

```java
package com.valhalla.presentation.plan;

import com.valhalla.domain.planplace.PlanPlaceService;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plans/{planId}/places")
public class PlanPlaceRestController {

    private final PlanPlaceService planPlaceService;

    @Autowired
    public PlanPlaceRestController(PlanPlaceService planPlaceService) {
        this.planPlaceService = planPlaceService;
    }

    @GetMapping
    public List<PlanPlace> getItinerary(@PathVariable Long planId) {
        return planPlaceService.getItinerary(planId);
    }

    @PostMapping
    public PlanPlace addPlace(
            @PathVariable Long planId,
            @RequestParam Long placeId
    ) {
        return planPlaceService.addPlaceToPlan(planId, placeId);
    }

    @PutMapping("/{id}")
    public PlanPlace updatePlanPlace(
            @PathVariable Long planId,
            @PathVariable Long id,
            @RequestBody PlanPlace planPlace
    ) {
        planPlace.setId(id);
        return planPlaceService.updatePlanPlace(planPlace);
    }

    @DeleteMapping("/{id}")
    public Map<String, Boolean> removePlace(
            @PathVariable Long planId,
            @PathVariable Long id
    ) {
        planPlaceService.removePlaceFromPlan(id);
        return Map.of("success", true);
    }

    @PostMapping("/reorder")
    public Map<String, Boolean> reorder(
            @PathVariable Long planId,
            @RequestBody List<Long> placeIds
    ) {
        planPlaceService.reorderPlaces(planId, placeIds);
        return Map.of("success", true);
    }
}
```

### 7. Update PlaceController (add user plans to the places page model)

File: `src/main/java/com/valhalla/presentation/place/PlaceController.java`

La ficha de lugar es el panel lateral de `/places` (D1), no una pagina aparte: no existe
`pages/places/detail.html` ni `@GetMapping("/{id}")`. Los planes del usuario van en el model de
`listPlaces`, que es la unica vista que los renderiza.

```java
@GetMapping
public ModelAndView listPlaces(
    @RequestParam(required = false) String category,
    @RequestParam(required = false) String search,
    @AuthenticationPrincipal UserDetails userDetails
) {
    Map<String, Object> model = new ModelMap();
    // ... filtrado de lugares, sin cambios ...

    if (userDetails != null) {
        List<Plan> userPlans = planService.getPlansByUserEmail(userDetails.getUsername());
        model.put("userPlans", userPlans);
    }

    return new ModelAndView("pages/places/list", model);
}
```

Add required imports:
```java
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
```

Add `PlanService` to constructor:
```java
private final PlaceService placeService;
private final PlanService planService;

@Autowired
public PlaceController(PlaceService placeService, PlanService planService) {
    this.placeService = placeService;
    this.planService = planService;
}
```

### 8. Narrow the CSRF ignore list in SecurityConfig

File: `src/main/java/com/valhalla/config/SecurityConfig.java`

Cadena @Order(3). Hoy `ignoringRequestMatchers("/api/**")` exime de CSRF a toda la API y
`permitAll()` abre `/api/**`. Dos cambios, ambos exigidos por AC-11 y AC-12:

```java
// Escritura autenticada, CSRF cubierto por el token de Spring.
.requestMatchers(HttpMethod.GET, "/api/**").permitAll()
.requestMatchers("/api/**").authenticated()

.csrf(csrf -> csrf.ignoringRequestMatchers("/api/places/**"))
```

La alternativa aceptada es sacar `/api/**` del ignore y confiar en el token de Spring para toda
la escritura (los forms de la UI ya mandan `_csrf`). Lo que **no** es aceptable es dejar
`ignoringRequestMatchers("/api/**")`: con el ignore abierto, AC-11 no cierra CSRF (S-07 falla).

### 9. Ownership en los endpoints de escritura (AC-13)

Los endpoints reciben el owner de la sesion y resuelven el plan con
`PlanServiceImpl.getOwnedPlan(id, ownerEmail)`, que ya lanza `PlanNotFoundException` cuando el
plan no es del usuario. Se traduce a **404**, nunca a 403. `removePlaceFromPlan` y
`updatePlanPlace` no aceptan ids sueltos: operan sobre el `PlanPlace` que pertenece al plan
resuelto, para que un id ajeno no escape del scope del owner.

### 10. ResetDatabase: limpiar `plan_place` antes que `plans` (obligatorio)

File: `src/test/java/com/valhalla/e2e/ResetDatabase.java`

**Este item no es opcional.** `ResetDatabase.cleanDatabase()` hoy corre solo
`DELETE FROM plans` y `DELETE FROM users`. APL-BE mete la tabla `plan_place` con FK a `plans`, asi
que `DELETE FROM plans` viola la FK, el `SQLException` envuelto en `IllegalStateException` revienta
**toda** la suite E2E — no un test, todos.

```java
String[] statements = {
  // plan_place first: plan_places.plan_id points at plans, so deleting the plans first makes
  // this reset fail with a foreign key violation and takes the whole E2E suite down with it.
  "DELETE FROM plan_place",
  "ALTER SEQUENCE plan_place_id_seq RESTART WITH 1",
  "DELETE FROM plans",
  "ALTER SEQUENCE plans_id_seq RESTART WITH 1",
  "DELETE FROM users",
  "ALTER SEQUENCE users_id_seq RESTART WITH 1",
  // ... INSERT del admin por defecto
};
```

El `ALTER SEQUENCE` reinicia el id para que los tests no dependan del autoincrement de corridas
anteriores. Es el mismo motivo del `plans_id_seq` que ya esta ahi.

## Archivos a crear/modificar

| File | Action |
|------|--------|
| `domain/planplace/PlanPlace.java` | Create |
| `domain/planplace/PlanPlaceRepository.java` | Create |
| `domain/planplace/PlanPlaceService.java` | Create |
| `domain/planplace/PlanPlaceServiceImpl.java` | Create |
| `infrastructure/planplace/JpaPlanPlaceRepository.java` | Create |
| `infrastructure/planplace/PlanPlaceRepositoryImpl.java` | Create |
| `presentation/plan/PlanPlaceRestController.java` | Create (escritura autenticada + ownership 404) |
| `presentation/place/PlaceController.java` | Update (add PlanService, `userPlans` en `GET /places`) |
| `config/SecurityConfig.java` | Update (narrow del ignore list de CSRF, escritura autenticada) |
| `e2e/ResetDatabase.java` | Update (`DELETE FROM plan_place` antes de `DELETE FROM plans` + `ALTER SEQUENCE`) |
