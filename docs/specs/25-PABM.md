# [PABM] ABM de Planes en Admin

> Trello: <https://trello.com/c/sFuLF6ru>
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.

## Objetivo

Un admin puede gestionar todos los planes de la plataforma desde `/admin/plans`: listarlos,
crear nuevos (a nombre de la app como usuario promocional), editar cualquier característica
(datos, participantes, roles, lugares, settings), activar/desactivar y eliminar.

La desactivación oculta el plan de listados y feeds (`/plans` del usuario, `/for-you`,
`/explore`) pero conserva el acceso para los participantes existentes: el detalle sigue
funcionando para quienes ya están dentro.

Los planes creados desde admin pertenecen a un usuario sistema **PlanIt**
(`app@planit.com`), no al admin logueado. Son planes que la plataforma promociona.

## Pre-requisitos

- [PLN] completed (Plan entity, PlanService, PlanController funcionando)
- [PLC] completed (Place entity y ABM admin de places como patrón de referencia)
- `SecurityConfig` protege `/admin/**` con `hasRole("ADMIN")` — no cambia.
- `PlaceAdminController` y `UserController` existen como patrón de ABM admin.
- No existe usuario sistema "app" — se crea en esta card.

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | `GET /admin/plans` muestra todos los planes con nombre, dueño (nombre completo), visibilidad, estado (activo/inactivo), cantidad de participantes y acciones |
| AC-02 | `GET /admin/plans/new` muestra el alta en tres pasos (datos básicos, paradas, privacidad y permisos), los mismos que el alta del usuario, con nota indicando que el plan es de PlanIt (app@planit.com) |
| AC-03 | `POST /admin/plans` crea un plan con `administrator` = usuario sistema `app@planit.com` (nombre: PlanIt, rol: ADMIN); si el usuario no existe se crea automáticamente. Lugares, horario y settings pasan por el mismo camino que el alta del usuario (`PlanController.toPlan`, `PlanScheduler`) |
| AC-04 | `GET /admin/plans/{id}/edit` muestra el formulario con los datos actuales del plan |
| AC-05 | `POST /admin/plans/{id}` actualiza nombre, descripción, fecha, hora de inicio, modo de transporte, visibilidad y estado activo de cualquier plan, y vuelve al edit (`/admin/plans/{id}/edit`) |
| AC-06 | `POST /admin/plans/{id}/toggle-active` alterna el campo `active` del plan y redirige al listado con mensaje |
| AC-07 | `POST /admin/plans/{id}/delete` elimina el plan y sus dependencias (participantes, invitaciones, lugares) |
| AC-08 | `GET /admin/plans/{id}` muestra el detalle completo de solo lectura: datos básicos, participantes con roles, lugares del plan y settings. La edición vive en `GET /admin/plans/{id}/edit` |
| AC-09 | Desde el edit el admin puede cambiar el rol de un participante (guest ↔ editor) |
| AC-10 | Desde el edit el admin puede eliminar un participante del plan |
| AC-11 | Desde el edit el admin puede editar los settings del plan (handleRequests, inviteInternal, inviteByEmail, autoSchedule) |
| AC-12 | Un plan con `active=false` no aparece en `/for-you`, `/explore` ni en el feed de planes públicos |
| AC-13 | Un plan con `active=false` sigue siendo accesible por `/plans/{id}` para sus participantes |
| AC-14 | Un plan con `active=false` NO es accesible para usuarios que no son participantes ni dueño |
| AC-15 | `/admin/plans` y todas sus sub-rutas requieren `ROLE_ADMIN`. Sin sesión se redirige a `/admin/login`; un USER común es redirigido a su landing (`/for-you`), igual que el resto de `/admin/**` — no recibe 403 ni llega al controller |
| AC-16 | El campo `active` tiene default `true` — los planes existentes no se ven afectados por la migración |
| AC-17 | El admin no puede crear un plan sin nombre (validación `@NotBlank`) |
| AC-18 | El usuario sistema `app@planit.com` tiene rol `ADMIN` y su contraseña no es un valor loginable (no tiene cuenta de acceso) |
| AC-19 | El edit y el alta tienen un picker de lugares sobre mapa (Leaflet) con los lugares guardados del catálogo dentro de la zona visible, excluyendo los que el plan ya tiene. Filtra por categoría (colores de `categoryColor`) y por nombre sin acentos. Click en marcador o fila abre la vista previa con dirección y horarios, y "Agregar al plan". En el edit agrega por `POST /admin/plans/{id}/places/add`; en el alta agrega al paso de paradas |
| AC-20 | El edit permite invitar participantes por correo con rol guest o editor (`POST /admin/plans/{id}/participants/invite`). La invitación actúa con la autoridad del dueño del plan: las reglas de `inviteParticipant` no cambian. El edit lista las invitaciones pendientes |
| AC-21 | `dayEndTime` no aparece en el admin: ni en el alta, ni en el edit, ni en el detalle. El alta usa el default del planificador (23:00). El update no lo pisa: conserva el valor guardado |
| AC-22 | Los planes creados por PlanIt (`administrator.email = app@planit.com`) aparecen siempre primero en todos los listados públicos (`/for-you` con cualquier `sort`, el sidebar de `/explore` vía `GET /api/plans/public`), incluso con filtros de texto, categoría, fecha o alcance. Dentro de cada grupo (PlanIt / resto) se conserva el orden del listado. Un plan sin administrador no rompe el orden |

## Escenarios de Test

### Tests Unitarios (`presentation/plan/PlanAdminControllerTest.java`)

`PlanAdminController` con el servicio mockeado: vista, redirect, toast y lo que el controller le
entrega al servicio.

| # | Test | AC que cubre |
|---|------|-------------|
| U-C-01 | `listPlansRendersTheListWithEveryPlan` | AC-01 |
| U-C-02 | `showNewPlanFormRendersAnEmptyCreateForm` | AC-02 |
| U-C-03 | `createPlanRedirectsToTheDetailAndSendsTheDefaultDayEnd` | AC-03, AC-21 |
| U-C-04 | `createPlanWithABlankNameRendersTheFormWithTheError` | AC-17 |
| U-C-05 | `showEditPlanFormPreloadsTheStoredPlan` | AC-04 |
| U-C-06 | `updatePlanRedirectsBackToTheEditAndKeepsTheDayEndDefault` | AC-05, AC-21 |
| U-C-07 | `updatePlanWithUncheckedActiveDeactivatesThePlan` | AC-05, AC-06 |
| U-C-08 | `deletePlanDeletesAndRedirectsToTheList` | AC-07 |
| U-C-09 | `toggleActiveReactivatesAndRedirectsToTheList` | AC-06 |
| U-C-10 | `toggleActiveDeactivatesAndRedirectsToTheList` | AC-06 |
| U-C-11 | `showPlanDetailRendersTheReadOnlyView` | AC-08 |
| U-C-12 | `movePlaceDownSwapsWithTheNextEntryAndSendsTheFullOrder` | AC-19 |
| U-C-13 | `movePlaceUpSwapsWithThePreviousEntry` | AC-19 |
| U-C-14 | `movePlaceUpOnTheFirstEntryDoesNotReorder` | AC-19 |
| U-C-15 | `addPlaceUsesTheOwnerAuthorityAndRedirectsToTheEdit` | AC-19, AC-20 |
| U-C-16 | `inviteParticipantRejectedByBusinessRulesRedirectsWithTheReason` | AC-20 |
| U-C-17 | `inviteParticipantWithTheEditorRoleGoesThroughTheService` | AC-20 |
| U-C-18 | `updatePlanWithABlankNameRerendersTheEditWithTheStoredPlan` | AC-05, AC-17 |
| U-C-19 | `createPlanWithAGlobalErrorFallsBackToTheGenericMessage` | AC-17 |
| U-C-20 | `changeParticipantRoleGoesThroughTheServiceAndRedirectsToTheEdit` | AC-09 |
| U-C-21 | `removeParticipantGoesThroughTheServiceAndRedirectsToTheEdit` | AC-10 |
| U-C-22 | `updateSettingsSendsAllFourValuesAndRedirectsToTheEdit` | AC-11 |
| U-C-23 | `removePlaceUsesTheOwnerAuthorityAndRedirectsToTheEdit` | AC-19 |
| U-C-24 | `updatePlaceEntrySendsTheTypedChangesWithTheOwnerAuthority` | AC-19 |
| U-C-25 | `aPlaceOperationOnAPlanWithoutOwnerFailsAsNotFound` | AC-19 |
| U-C-26 | `movePlaceDownOnTheLastEntryDoesNotReorder` | AC-19 |
| U-C-27 | `movePlaceDownOnAnUnknownEntryDoesNotReorder` | AC-19 |

### Tests Unitarios (`infrastructure/plan/PlanServiceImplAdminTest.java`)

`PlanServiceImpl` con puertos mockeados. Cada negativo prueba el efecto: `never()` sobre la
escritura destructiva, o la entidad sin tocar.

| # | Test | AC que cubre |
|---|------|-------------|
| U-S-01 | `getAllPlansReturnsEveryPlanInTheRepositoryWithoutOwnershipFilter` | AC-01 |
| U-S-02 | `getAnyPlanReturnsAPlanOwnedByAnotherUserWithoutCheckingOwnership` | AC-04 |
| U-S-03 | `getAnyPlanThrowsPlanNotFoundForAnUnknownId` | AC-04 |
| U-S-04 | `createAdminPlanAssignsThePlanItSystemUserAsAdministrator` | AC-03 |
| U-S-05 | `createAdminPlanGeneratesAUniqueShortCode` | AC-03 |
| U-S-06 | `createAdminPlanCreatesThePlanItUserWithAdminRoleWhenMissing` | AC-03, AC-18 |
| U-S-07 | `updateAnyPlanCopiesTheFormFieldsAndKeepsTheStoredDayEnd` | AC-05, AC-21 |
| U-S-08 | `updateAnyPlanKeepsTheOwnerAndTheShortCode` | AC-05 |
| U-S-09 | `deleteAnyPlanRemovesRatingsFirstAndThenThePlan` | AC-07 |
| U-S-10 | `deleteAnyPlanForAnUnknownIdDeletesNothing` | AC-07 |
| U-S-11 | `togglePlanActiveDeactivatesAndPersistsTheFlag` | AC-06 |
| U-S-12 | `togglePlanActiveReactivatesAPlan` | AC-06 |
| U-S-13 | `togglePlanActiveWithAnUnknownPlanThrowsAndSavesNothing` | AC-06 |
| U-S-14 | `updateAnySettingsWritesAllFourSettingsIncludingAutoSchedule` | AC-11 |
| U-S-15 | `inviteParticipantAnyWithoutAdministratorThrowsPlanNotFound` | AC-20 |

### Tests Unitarios (`domain/plan/PlanDiscoveryTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-D-01 | `planItPlansComeFirstUnderPopularityEvenWithAHigherRatedUserPlan` | AC-22 |
| U-D-02 | `planItPlansComeFirstUnderDateSort` | AC-22 |
| U-D-03 | `planItPlansComeFirstUnderNameSort` | AC-22 |
| U-D-04 | `planItFirstStillHoldsWithATextFilter` | AC-22 |
| U-D-05 | `planItFirstKeepsTheRelativeOrderInsideEachGroup` | AC-22 |
| U-D-06 | `planWithoutAdministratorDoesNotBreakPlanItFirst` | AC-22 |
| U-D-07 | `publicDateOrderUsedByTheSidebarPutsPlanItFirst` | AC-22 |
| U-D-08 | `isPlanItIsFalseForANullPlan` | AC-22 |

`domain/plan/PlanTest.activeDefaultsToTrueWhenTheColumnIsNull` cubre el default de `active` cuando
una fila anterior a la columna la hidrata en null (AC-16).

### Tests de Integracion (`integration/PlanAdminControllerIntegrationTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-01 | `listShowsEveryPlanIncludingThePlanItOnes` | AC-01, AC-15 |
| I-02 | `newFormShowsTheThreeStepWizardInCreateMode` | AC-02 |
| I-03 | `createAlwaysAssignsThePlanItUserAndTheDefaultDayEnd` | AC-03, AC-21 |
| I-04 | `createWithABlankNameReturnsTheFormWithErrorsAndSavesNothing` | AC-17 |
| I-05 | `editPreloadsTheStoredPlanInEditMode` | AC-04 |
| I-06 | `updateChangesTheFieldsAndKeepsTheStoredDayEnd` | AC-05, AC-21 |
| I-07 | `toggleActiveDeactivatesAndReactivatesThePlan` | AC-06 |
| I-08 | `deleteRemovesThePlanAndRedirectsToTheList` | AC-07 |
| I-09 | `detailIsReadOnlyAndShowsTheWholePlan` | AC-08 |
| I-10 | `inactivePlanIsHiddenFromForYouWhileTheActiveOneIsShown` | AC-12 |
| I-11 | `inactivePlanIsHiddenFromThePublicFeedUsedByExplore` | AC-12 |
| I-12 | `movingAStopDownReordersAndRedirectsToTheEdit` | AC-19 |
| I-13 | `inviteFromTheEditCreatesAPendingInvitationForTheRecipient` | AC-20 |

### Tests de Seguridad (`integration/PlanAdminSecurityTest.java`)

La cadena de `/admin/**` en `SecurityConfig` decide el acceso. Un USER común es redirigido a
`LoginRedirects.USER_LANDING` (`/for-you`) y ninguna de sus requests llega al controller: se
verifica además que no persiste nada.

| # | Test | AC que cubre |
|---|------|-------------|
| S-01 | `anonymousTrafficIsSentToLoginFromThePlansAbm` — redirect a `**/admin/login` | AC-15 |
| S-02 | `aCommonUserIsRedirectedAwayFromThePlansAbm` — redirect a `/for-you` | AC-15 |
| S-03 | `aCommonUserPostingACreateDoesNotPersistAnything` | AC-15 |
| S-04 | `aCommonUserCannotDeleteAPlan` | AC-15 |
| S-05 | `aCommonUserCannotToggleThePlanActiveFlag` | AC-15 |
| S-06 | `anAdminReachesThePlansAbm` — 200 | AC-15 |
| S-07 | `anInactivePlanIsNotReadableByAnOutsider` | AC-14 |
| S-08 | `anInactivePlanStaysReadableByItsParticipants` | AC-13 |

### E2E (minimos)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `AdminPlansE2E.E01_adminCreatesAPlanFromTheThreeStepWizardAndSeesItInTheList` | Alta de tres pasos con una parada del picker de mapa → detalle del plan nuevo → aparece en el listado | AC-01, AC-02, AC-03, AC-19 |
| E-02 | `AdminPlansE2E.E02_detailPageIsReadOnlyAndOnlyLinksToTheEdit` | El detalle no tiene formularios y ofrece un único link al edit | AC-08 |
| E-03 | `AdminPlansE2E.E03_adminDeactivatesAndDeletesAPlanFromTheList` | Desactiva desde el listado (toast) y luego elimina (toast, la fila desaparece) | AC-06, AC-07 |

Cómo correrlos: `.\scripts\gate.ps1 e2e=AdminPlansE2E` levanta PostgreSQL, crea `valhalla_e2e`,
instala Chromium, exporta `DB_*` y corre `mvn verify`. El suite E2E borra la base antes de cada
test, por eso `ResetDatabase` rechaza cualquier `DB_NAME` que no contenga `e2e`.

## Referencia de Implementacion

> Los pasos a continuación son guía de implementación, no reemplazan los acceptance criteria de arriba.

### 1. Agregar campo `active` a Plan

File: `src/main/java/com/valhalla/domain/plan/Plan.java`

Agregar después de `isPublic`:

```java
private Boolean active = true;
```

Getter/setter:

```java
public Boolean getActive() {
  return active != null ? active : true;
}

public void setActive(Boolean active) {
  this.active = active;
}
```

Actualizar `updateFrom` para incluir `this.active = changes.getActive();`.

### 2. Agregar métodos admin a PlanService

File: `src/main/java/com/valhalla/domain/plan/PlanService.java`

```java
// Admin operations
List<Plan> getAllPlans();
Plan getAnyPlan(UUID id);
Plan createAdminPlan(Plan plan);
Plan updateAnyPlan(UUID id, Plan changes);
void deleteAnyPlan(UUID id);
void togglePlanActive(UUID id, boolean active);
```

### 3. Implementar en PlanServiceImpl

File: `src/main/java/com/valhalla/infrastructure/plan/PlanServiceImpl.java`

```java
private static final String APP_USER_EMAIL = "app@planit.com";

@Override
@Transactional(readOnly = true)
public List<Plan> getAllPlans() {
  return planRepository.findAll();
}

@Override
@Transactional(readOnly = true)
public Plan getAnyPlan(UUID id) {
  return planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
}

@Override
public Plan createAdminPlan(Plan plan) {
  User appUser = getOrCreateAppUser();
  plan.setAdministrator(appUser);
  plan.setShortCode(generateUniqueShortCode());
  plan.setActive(true);
  return planRepository.save(plan);
}

@Override
public Plan updateAnyPlan(UUID id, Plan changes) {
  Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
  plan.updateFrom(changes);
  return planRepository.save(plan);
}

@Override
public void deleteAnyPlan(UUID id) {
  planRepository.deleteById(id);
}

@Override
public void togglePlanActive(UUID id, boolean active) {
  Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
  plan.setActive(active);
  planRepository.save(plan);
}

private User getOrCreateAppUser() {
  return userRepository.findByEmail(APP_USER_EMAIL).orElseGet(() -> {
    User appUser = new User();
    appUser.setEmail(APP_USER_EMAIL);
    appUser.setPassword("N/A");
    appUser.setFirstName("PlanIt");
    appUser.setLastName("App");
    appUser.setRole("ADMIN");
    appUser.setActive(true);
    return userRepository.save(appUser);
  });
}
```

### 4. Filtrar planes inactivos en listings públicos

En `getPublicPlans()` de `PlanServiceImpl`, agregar filtro `plan.getActive()`.

En `PlanDiscovery` o `ForYouController`, asegurar que solo se muestren planes activos.

### 5. Crear PlanAdminController

File: `src/main/java/com/valhalla/presentation/plan/PlanAdminController.java`

Seguir el patrón exacto de `PlaceAdminController`. Rutas:

- `GET /admin/plans` — listado
- `GET /admin/plans/new` — form alta
- `POST /admin/plans` — crear
- `GET /admin/plans/{id}/edit` — form edición
- `POST /admin/plans/{id}` — actualizar
- `POST /admin/plans/{id}/toggle-active` — alternar active
- `POST /admin/plans/{id}/delete` — eliminar
- `GET /admin/plans/{id}` — detalle completo

### 6. Crear EditPlanRequest

File: `src/main/java/com/valhalla/presentation/plan/EditPlanRequest.java`

Form object con: `name` (@NotBlank), `description`, `eventDate`, `eventTime`,
`transportMode`, `isPublic`, `active`. `dayEndTime` no se expone en el admin (AC-21): el
controller manda el default del planificador (23:00) y el update conserva el valor guardado.

### 7. Crear templates

- `pages/admin/plans.html` — listado (patrón `places.html`)
- `pages/admin/plan-form.html` — alta/edición (patrón `place-form.html`)
- `pages/admin/plan-detail.html` — detalle con tabs: datos, participantes, lugares, settings

### 8. Tests

- `PlanAdminControllerTest` — unit tests del controller
- `PlanServiceImplAdminTest` — unit tests del service admin
- `PlanAdminControllerIntegrationTest` — integration tests
- `PlanAdminSecurityTest` - security tests (un USER es redirigido a `/for-you`, no recibe 403)
- `AdminPlansE2E` — happy path completo

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `domain/plan/Plan.java` | Actualizar (campo `active`) |
| `domain/plan/PlanService.java` | Actualizar (métodos admin) |
| `infrastructure/plan/PlanServiceImpl.java` | Actualizar (implementación admin) |
| `presentation/plan/PlanAdminController.java` | Crear |
| `presentation/plan/EditPlanRequest.java` | Crear |
| `templates/pages/admin/plans.html` | Crear |
| `templates/pages/admin/plan-form.html` | Crear |
| `templates/pages/admin/plan-detail.html` | Crear (solo lectura, sin `dayEndTime`) |
| `templates/components/plan-places-picker.html` | Crear (picker de lugares, AC-19) |
| `resources/core/js/pages/plan-create.js` | Crear (wizard de tres pasos del alta) |
| `resources/core/js/pages/plan-places-picker.js` | Crear (Vue del picker de lugares) |
| `resources/core/js/lib/map-picker.js` | Crear (mapa, markers y búsqueda compartidos con el importador) |
| `resources/core/js/pages/place-import.js` | Actualizar (usa `lib/map-picker.js`, mismos ids y hooks) |
| `test/.../PlanAdminControllerTest.java` | Crear |
| `test/.../PlanServiceImplAdminTest.java` | Crear |
| `test/.../PlanAdminControllerIntegrationTest.java` | Crear |
| `test/.../PlanAdminSecurityTest.java` | Crear |
| `test/.../AdminPlansE2E.java` | Crear |
