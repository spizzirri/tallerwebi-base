# [CLO] Clonar plan publico/privado

> Trello: https://trello.com/c/XQ1o5CBK/17-clo-clonar-plan-publico-privado
> **Estimacion:** 3 pts Fibonacci
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.

## Objetivo

Un usuario con acceso a un plan (publico, o privado del que es administrator/participant) puede
clonarlo: crea una copia propia y editable —nuevo `shortCode`, el como unico administrador, el
itinerario copiado— sin tocar el original.

Hacia falta porque el unico camino para "hacer el mio" un plan ajeno es copiarlo a mano. El
shortCode ya es el mecanismo de invitacion (`POST /plans/join`), pero unirte como participant no
te deja editar: `updatePlan()` exige ser administrator. Clonar es el camino que transforma un plan
visto en un plan propio.

## Pre-requisitos

- [PLN] completed (`Plan`, `PlanPlace`, `generateUniqueShortCode()` en `PlanServiceImpl`)
- [AUT] completed
- [PVP] (`GET /plans/{id}/public`) es donde vive el CTA "Clonar plan". Ya esta mergeado
  (`task-12-PVP` integrado en esta rama): la vista existe y el CTA apunta a `POST /plans/{id}/clone`.
- La card [PPV] (`14-PPV.md`) es la puerta de entrada para descubrir planes publicos que
  despues se clonan.
- **Ownership del endpoint.** `POST /plans/{id}/clone` es de [CLO]: lo implementa `PlanController`
  y sus pruebas de seguridad son `integration/PlanCloneSecurityTest.java` (S-01..S-03). El "Usar
  plan" del feed [PPV] consume ese mismo endpoint; no duplica la logica.

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | `POST /plans/{id}/clone` con sesion redirige al detalle de la copia (`/plans/{nuevoId}`) |
| AC-02 | La copia pertenece al usuario logueado (`administrator` = cloner) y aparece en su lista `/plans` |
| AC-03 | La copia tiene `shortCode` nuevo y unico; nunca hereda el del original |
| AC-04 | La copia nace **privada** (`isPublic = false`) aunque el original sea publico |
| AC-05 | Se copian nombre, descripcion, fecha y hora del evento |
| AC-06 | Se copia el itinerario completo: cada `PlanPlace` con mismo lugar, descripcion, fecha/hora de visita y posicion; los `Place` son los mismos persistidos, no se duplican |
| AC-07 | La copia no tiene participantes |
| AC-08 | El original queda intacto: mismos participantes, mismo `shortCode`, mismo itinerario |
| AC-09 | Un plan publico lo puede clonar cualquier usuario autenticado, aunque no sea participant |
| AC-10 | Un plan privado solo lo clona su administrator o un participant; para el resto la respuesta es identica a la de un id inexistente |
| AC-11 | Sin sesion, `POST /plans/{id}/clone` redirige a `/auth/login` |
| AC-12 | El CTA "Clonar plan" aparece en la vista publica [PVP]; sin sesion el CTA lleva a `/auth/login` |
| AC-13 | El CTA "Clonar plan" aparece en la vista detalle (`/plans/{id}`) para todo el que puede verla (administrator y participants); la copia queda en `/plans/{nuevoId}` |

## Escenarios de Test

### Tests Unitarios (`infrastructure/plan/PlanServiceImplTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | `clonePlan()` con plan publico devuelve una copia con `administrator` = cloner | AC-01, AC-02 |
| U-02 | `clonePlan()` genera un `shortCode` distinto del original | AC-03 |
| U-03 | `clonePlan()` de un plan publico deja la copia con `isPublic = false` | AC-04 |
| U-04 | `clonePlan()` copia nombre, descripcion, fecha y hora | AC-05 |
| U-05 | `clonePlan()` copia todos los `PlanPlace` con sus lugares, descripciones, fechas y orden | AC-06 |
| U-06 | `clonePlan()` deja la copia sin participantes | AC-07 |
| U-07 | `clonePlan()` con plan privado y cloner que no es administrator ni participant lanza `PlanNotFoundException` | AC-10 |
| U-08 | `clonePlan()` con plan privado clonado por su participant funciona | AC-10 |
| U-09 | `clonePlan()` con id inexistente lanza `PlanNotFoundException` | AC-10 |
| U-10 | `clonePlan()` no modifica el plan original (participantes, shortCode e itinerario sin cambios) | AC-08 |
| U-11 | `clonePlan()` con email de usuario inexistente lanza `UserNotFoundException` | AC-02 |

### Tests Unitarios (`presentation/plan/PlanControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-12 | `clonePlan()` con plan accesible redirige a `/plans/{idDeLaCopia}` | AC-01 |
| U-13 | `clonePlan()` toma el email de la sesion y no recibe el dueño del plan por form | AC-02 |

### Tests de Integracion (`integration/PlanControllerIntegrationTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-01 | `POST /plans/{id}/clone` sobre un plan publico → 302 a `/plans/{nuevoId}`, la copia aparece en `/plans` del cloner | AC-01, AC-02 |
| I-02 | La copia creada tiene shortCode nuevo y `isPublic = false` | AC-03, AC-04 |
| I-03 | La copia renderiza en `/plans/{id}` con el mismo itinerario que el original | AC-06 |
| I-04 | Clonar un plan privado de otro usuario → mismo resultado que clonar un id inexistente | AC-10 |
| I-05 | Despues de clonar, el plan original sigue con sus participantes y su itinerario | AC-08 |
| I-06 | `GET /plans/{id}/public` con sesion: el form "Clonar plan" apunta a `POST /plans/{id}/clone` | AC-12 |
| I-07 | `GET /plans/{id}/public` sin sesion: el CTA lleva a `/auth/login?redirect=...`, sin form de clone | AC-12 |

### Tests de Seguridad (`integration/PlanCloneSecurityTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| S-01 | `POST /plans/{id}/clone` anonimo → redirige a `/auth/login` y no crea nada | AC-11 |
| S-02 | `POST /plans/{id}/clone` sin sesion no incrementa la cantidad de planes del sistema | AC-11 |
| S-03 | `GET /plans/{id}/clone` anonimo no ejecuta el clon (el mapping es solo POST) | AC-11 |

### E2E (minimos)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `PublicPlanCloneE2E` | Registro de un usuario sin acceso → abrir la vista publica de un plan → "Clonar plan" → caer en `/plans/{id}` de la copia con el itinerario | AC-01, AC-06, AC-12 |
| E-02 | `DetailPlanCloneE2E` | Sesión de un participant → abrir `/plans/{id}` del plan → "Clonar plan" → caer en `/plans/{id}` de la copia con el itinerario | AC-01, AC-06, AC-13 |

## Notas / decisiones de diseño

- **La copia nace privada.** `isPublic = false` no es reversible automaticamente: publicar es una
  decision del nuevo dueño. El form de edicion de `/plans/{id}` ya expone el flag (`PlanRequest`),
  asi que promoverla a publica es un paso del usuario, no de esta spec.
- **Mismo nombre, sin prefijo "Copia de".** Mantener el nombre original deja al dueño renombrar
  como quiera y evita una decision de diseno que nadio pidio. Si el equipo lo quiere, es un cambio
  de una linea en el paso 2.
- **Los `Place` no se duplican.** El clon referencia las mismas entidades `Place` del catalogo: lo
  que se copia es la fila de `PlanPlace` (descripcion, fechas, posicion), no el lugar. Borrar un
  lugar del catalogo sigue teniendo el mismo efecto sobre todas las copias que sobre el original —
  es el mismo contrato que ya rige para los planes normales.
- **Privado ≡ inexistente, igual que [PVP].** `clonePlan()` lanza `PlanNotFoundException` y
  `GlobalExceptionHandler` responde como ante un id que no existe; no se devuelve 403 ni "no
  autorizado".
- **No se copian participantes ni el shortCode.** El shortCode es la llave de join del plan
  original; heredarlo pondria los joins de ambas copias en la misma canasta.
- **El clon reutiliza `generateUniqueShortCode()`.** Es private en `PlanServiceImpl`; al estar la
  copia en la misma clase no hace falta exponerlo.
- **El CTA de la vista detalle es incondicional (AC-13).** `showPlan()` ya filtra por
  `getParticipatingPlan()`: solo administrator y participants pueden abrir la pagina, y ambos
  benefician del clon (el participant no puede editar; el admin puede duplicar su plan como
  plantilla). No se esconde a nadie que tenga acceso — decision de producto 2026-10-09.

## Referencia de Implementacion

> Los pasos a continuacion son guia de implementacion, no reemplazan los criterios de aceptacion de arriba.

### 1. Caso de uso en el servicio

File: `src/main/java/com/valhalla/domain/plan/PlanService.java` (interface)

```java
Plan clonePlan(Long id, String clonerEmail);
```

File: `src/main/java/com/valhalla/infrastructure/plan/PlanServiceImpl.java`

```java
@Override
@Transactional
public Plan clonePlan(Long id, String clonerEmail) {
  User cloner = userRepository.findByEmail(clonerEmail).orElseThrow(UserNotFoundException::new);
  Plan source = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
  boolean accessible =
    source.getIsPublic() || source.isAdministrator(clonerEmail) || source.isParticipant(clonerEmail);
  if (!accessible) {
    // Mismo camino que un id inexistente: no se confirma la existencia del plan privado.
    throw new PlanNotFoundException();
  }

  Plan copy = new Plan();
  copy.setName(source.getName());
  copy.setDescription(source.getDescription());
  copy.setEventDate(source.getEventDate());
  copy.setEventTime(source.getEventTime());
  // La copia nace privada: publicar es decision del nuevo dueño (AC-04).
  copy.setIsPublic(false);
  copy.setAdministrator(cloner);
  // participants queda vacio por defecto (AC-07); el shortCode se genera abajo (AC-03).

  int order = 1;
  for (PlanPlace entry : source.getPlanPlaces()) {
    PlanPlace entryCopy = new PlanPlace();
    entryCopy.setPlace(entry.getPlace()); // misma entidad Place persistida, no una copia (AC-06)
    entryCopy.setDescription(entry.getDescription());
    entryCopy.setVisitDate(entry.getVisitDate());
    entryCopy.setVisitTime(entry.getVisitTime());
    entryCopy.setSortOrder(order++);
    copy.addPlanPlace(entryCopy);
  }

  copy.setShortCode(generateUniqueShortCode());
  return planRepository.save(copy);
}
```

`Plan.planPlaces` ya tiene `cascade = CascadeType.ALL`, asi que `save(copy)` persiste las filas
clonadas. `source` nunca se modifica: no se le agregan participantes ni se le toca el itinerario
(AC-08, cubierto por U-10).

### 2. Endpoint

File: `src/main/java/com/valhalla/presentation/plan/PlanController.java`

Va debajo de `deletePlan()` y usa las constantes de redireccion que ya existen en la clase:

```java
@PostMapping("/{id}/clone")
public ModelAndView clonePlan(@PathVariable Long id, Authentication authentication) {
  Plan copy = planService.clonePlan(id, authentication.getName());
  return new ModelAndView(REDIRECT_PLAN_DETAIL + copy.getId());
}
```

Solo POST: `GET /plans/{id}/clone` no esta mapeado, asi que cae en el handler 404
(`NoHandlerFoundException` / `MethodArgumentTypeMismatchException`) en vez de clonar con un link
(S-03).

### 3. CTA en la vista publica

File: `src/main/webapp/WEB-INF/templates/pages/plans/public.html`

Ya esta previsto en `13-PVP.md` (paso 4): el `form method="post"` con
`th:action="@{/plans/{id}/clone(id=${plan.id})}"` y el CSRF token, condicionado a `isLogged`.
No se repite aca.

### 3b. CTA en la vista detalle (AC-13)

File: `src/main/webapp/WEB-INF/templates/pages/plans/detail.html`

`showPlan()` solo resuelve planes con `getParticipatingPlan()`: la pagina ya es visible
exclusivamente para administrator y participants, asi que el form va sin condicional, junto a
los forms de eliminar/salir:

```html
<form th:action="@{/plans/{id}/clone(id=${plan.id})}" method="POST">
  <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
  <button type="submit" class="btn btn-outline-primary">Clonar plan</button>
</form>
```

### 4. Pruebas E2E

File: `src/test/java/com/valhalla/e2e/PublicPlanCloneE2E.java` (creado, E-01 — desbloqueado con [PVP])

Flujo minimo: login como usuario sin acceso al plan → abrir `/plans/{id}/public` de un plan
publico con lugares → submit del form "Clonar plan" → verificar que la URL es `/plans/{id}` de un
plan distinto al original y que el itinerario se renderiza. Sigue la base de `e2e/E2eBase.java` y
el estilo page-object de `e2e/views/PlansPage.java`.

File: `src/test/java/com/valhalla/e2e/DetailPlanCloneE2E.java` (crear, E-02 — desbloqueado hoy)

Flujo: login como participant de un plan con lugares → abrir `/plans/{id}` → submit del form
"Clonar plan" de la vista detalle → verificar que la URL es `/plans/{id}` de un plan distinto,
con el mismo itinerario renderizado.

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `domain/plan/PlanService.java` | Actualizar (`clonePlan(Long id, String clonerEmail)`) |
| `infrastructure/plan/PlanServiceImpl.java` | Actualizar (implementacion con acceso check) |
| `presentation/plan/PlanController.java` | Actualizar (`@PostMapping("/{id}/clone")`) |
| `infrastructure/plan/PlanServiceImplTest.java` | Actualizar (U-01..U-11) |
| `presentation/plan/PlanControllerTest.java` | Actualizar (U-12..U-13) |
| `integration/PlanControllerIntegrationTest.java` | Actualizar (I-01..I-05) |
| `integration/PlanCloneSecurityTest.java` | Crear (S-01..S-03) |
| `pages/plans/detail.html` | Actualizar (form "Clonar plan", AC-13) |
| `e2e/PublicPlanCloneE2E.java` | Creado (E-01) |
| `e2e/DetailPlanCloneE2E.java` | Crear (E-02) |
