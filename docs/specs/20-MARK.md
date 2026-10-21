# [MARK] Guardar planes publicos para clonarlos despues

> Trello: https://trello.com/c/cOWUE3Hx
> **Estimacion:** 8 pts Fibonacci
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.

## Objetivo

Un usuario autenticado puede guardar planes **publicos** en una lista privada ("Guardados")
para volver a encontrarlos y clonarlos cuando quiera usarlos como base de un plan propio.

Guardar no es un "me gusta": es una accion privada y utilitaria. Por eso la lista vive en el
perfil propio, nadie mas la ve, y al guardar se persiste un **snapshot** del plan (encabezado e
itinerario). Ese snapshot es lo que permite que un plan guardado siga siendo clonable aunque su
autor lo borre o lo vuelva privado, sin exponer el original.

## Pre-requisitos

- [PROF] mergeado (`/profile/{id}` con la seccion `#bookmarks`, modal del navbar con la entrada
  "Guardados" deshabilitada).
- [CLO] mergeado (`POST /plans/{id}/clone` + `PlanService.clonePlan()`): el clon de un plan vivo se
  reusa tal cual.
- [CAL] como referencia de patron: entidad usuario↔plan con unique, sin coleccion inversa, y
  respuesta "privado ≡ inexistente".
- [PVP]/[PPV]/[SEARCH]: las superficies donde se muestran planes publicos (vista publica, feed
  `/for-you`, sidebar de `/explore`).
- [PLN]/[APL-BE]: `PlanService.createPlan()` ya resuelve los `Place` por id y genera el shortCode,
  que es lo que hace falta para reconstruir un plan desde el snapshot.

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | Un usuario autenticado puede guardar un plan publico desde cualquier superficie que lo muestre: feed `/for-you`, sidebar de `/explore`, detalle `/plans/{id}`, vista publica `/plans/{id}/public` y las cards de planes publicos de `/profile/{id}` |
| AC-02 | El control es un boton con icono de bookmark: relleno si el plan ya esta guardado y de contorno si no, con `aria-pressed`, `title` y label accesible en espanol |
| AC-03 | Guardar es idempotente: un segundo guardado del mismo plan no crea una segunda fila |
| AC-04 | Solo se pueden guardar planes publicos. Un plan privado o inexistente responde igual que un id inexistente (404) y no crea fila |
| AC-05 | Sin sesion el control no se renderiza en ninguna superficie; un POST anonimo redirige a `/auth/login` y no modifica nada |
| AC-06 | El usuario puede quitar un plan de sus guardados; la accion es idempotente y pide confirmacion en todas las superficies |
| AC-07 | La seccion `#bookmarks` del perfil propio lista los planes guardados, mas recientes primero, con cover, nombre, fecha, cantidad de lugares, autor y estado; tiene estado vacio propio |
| AC-08 | Cada card de Guardados ofrece Abrir (si el plan sigue accesible), Clonar plan y Quitar |
| AC-09 | Al guardar se persiste un snapshot del plan: nombre, descripcion, fecha y hora del evento, autor y el itinerario completo (lugar, descripcion, fecha/hora de visita y orden) |
| AC-10 | Mientras el plan siga publico, la card y el clon usan el plan vivo, de modo que las ediciones del autor se reflejan |
| AC-11 | Si el plan fue borrado, la card sigue visible con badge "Eliminado" y el tooltip "El plan original ya no esta disponible. Podes clonarlo para seguir usandolo." |
| AC-12 | Si el plan paso a privado y el usuario no es su dueno ni participante, la card sigue visible con badge "Privado" y el mismo tooltip. Si el plan sigue accesible para el usuario, no lleva badge y abre el detalle |
| AC-13 | Clonar desde Guardados funciona en los tres estados: con el plan vivo reusa `clonePlan()`; si no esta disponible reconstruye un plan privado propio desde el snapshot, omitiendo los lugares que ya no existan en el catalogo |
| AC-14 | Los guardados son privados: no se renderizan en el perfil de otro usuario ni en ningun endpoint publico, y el estado de guardado de un usuario nunca se expone a otro |
| AC-15 | Borrar un plan no borra los guardados de otros usuarios: sus cards quedan como "Eliminado" |
| AC-16 | Borrar un usuario desde el ABM admin borra sus guardados sin violar FKs |
| AC-17 | La entrada "Guardados" del modal del navbar es un link activo a `/profile#bookmarks`, sin badge "Pronto" |
| AC-18 | El email del usuario sale siempre de la sesion, nunca del formulario; todo POST exige CSRF |
| AC-19 | El estado de guardado se resuelve por request en el modelo: los toggles funcionan como form POST sin JavaScript, salvo el sidebar de `/explore`, que se renderiza con Vue y usa el endpoint REST |

## Escenarios de Test

### Tests Unitarios (`infrastructure/bookmark/PlanBookmarkServiceImplTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | `save()` de un plan publico crea la fila con el usuario de la sesion y el `planId` | AC-01 |
| U-02 | `save()` copia el encabezado del plan al snapshot (nombre, descripcion, fecha, hora, autor) | AC-09 |
| U-03 | `save()` copia el itinerario completo con lugar, descripcion, fechas y orden | AC-09 |
| U-04 | `save()` dos veces del mismo plan no duplica la fila | AC-03 |
| U-05 | `save()` de un plan privado lanza `PlanNotFoundException` y no escribe | AC-04 |
| U-06 | `save()` de un id inexistente lanza `PlanNotFoundException` | AC-04 |
| U-07 | `remove()` borra la fila del par usuario/plan | AC-06 |
| U-08 | `remove()` de un plan que no estaba guardado no lanza y no escribe | AC-06 |
| U-09 | `getBookmarks()` devuelve los guardados del usuario, mas recientes primero | AC-07 |
| U-10 | `getBookmarks()` no devuelve guardados de otro usuario | AC-14 |
| U-11 | `getBookmarks()` marca `AVAILABLE` cuando el plan sigue publico | AC-10 |
| U-12 | `getBookmarks()` marca `DELETED` cuando el plan ya no existe y usa el snapshot | AC-11 |
| U-13 | `getBookmarks()` marca `PRIVATE` cuando el plan existe pero no es accesible | AC-12 |
| U-14 | `getBookmarks()` deja `AVAILABLE` un plan privado del que el usuario es participante | AC-12 |
| U-15 | `cloneBookmark()` de un plan vivo reusa `PlanService.clonePlan()` | AC-13 |
| U-16 | `cloneBookmark()` de un plan borrado reconstruye la copia desde el snapshot con el usuario como administrador | AC-13 |
| U-17 | `cloneBookmark()` omite los lugares del snapshot que ya no existen en el catalogo | AC-13 |
| U-18 | `cloneBookmark()` de un guardado ajeno o inexistente lanza `PlanNotFoundException` | AC-14 |
| U-19 | `isBookmarked()` / `getBookmarkedPlanIds()` solo miran las filas del usuario | AC-02, AC-14 |

### Tests Unitarios (`presentation/bookmark/BookmarkControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-20 | `save()` toma el email de la sesion, ignora un email del form y redirige a `returnTo` | AC-01, AC-18 |
| U-21 | `save()` con `returnTo` ausente redirige a la vista publica del plan | AC-01 |
| U-22 | `save()` con `returnTo` externo o invalido cae al default (sin open redirect) | AC-18 |
| U-23 | `remove()` y `clone()` redirigen a destino/`/plans/{nuevoId}` | AC-06, AC-13 |

### Tests de Integracion (`integration/BookmarkControllerIntegrationTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-01 | `POST /bookmarks/{id}` sobre un plan publico crea el guardado y redirige | AC-01 |
| I-02 | El guardado persiste el snapshot del encabezado y del itinerario | AC-09 |
| I-03 | Guardar dos veces deja una sola fila | AC-03 |
| I-04 | Guardar un plan privado responde como un id inexistente y no crea fila | AC-04 |
| I-05 | `POST /bookmarks/{id}/remove` borra el guardado | AC-06 |
| I-06 | `/profile/{id}` propio renderiza la card y el estado vacio cuando no hay guardados | AC-07 |
| I-07 | Borrar el plan deja la card con badge "Eliminado" y el tooltip | AC-11, AC-15 |
| I-08 | Volver privado el plan deja la card con badge "Privado" | AC-12 |
| I-09 | Clonar desde Guardados un plan borrado crea una copia propia con el itinerario del snapshot | AC-13 |
| I-10 | El perfil de otro usuario no expone los guardados | AC-14 |
| I-11 | El modal del navbar renderiza "Guardados" como link a `/profile#bookmarks` | AC-17 |
| I-12 | Las cards de `/for-you` y la vista publica muestran el icono relleno o de contorno segun el estado | AC-02, AC-19 |
| I-13 | `POST /api/plans/{id}/bookmarks` y `DELETE` responden 204 y reflejan el estado en `/api/plans/public` | AC-19 |

### Tests de Seguridad (`integration/BookmarkSecurityTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| S-01 | `POST /bookmarks/{id}` anonimo redirige a login y no crea la fila | AC-05 |
| S-02 | `POST /bookmarks/{id}/remove` anonimo redirige a login y no borra nada | AC-05 |
| S-03 | `POST` sin token CSRF responde 403 en las rutas MVC | AC-18 |
| S-04 | `POST`/`DELETE` sin token CSRF responde 403 en el endpoint REST | AC-18 |
| S-05 | Un usuario no puede clonar el guardado de otro | AC-14 |
| S-06 | El HTML publico de un plan no contiene datos de los guardados de nadie | AC-14 |

### E2E (minimos)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `BookmarkE2E` | Login USER → guardar un plan publico desde una card de `/for-you` → el icono queda relleno → abrir el modal y "Guardados" → la card aparece en `#bookmarks` → "Clonar plan" → caer en el detalle de la copia → volver y "Quitar" con confirmacion | AC-01, AC-02, AC-07, AC-08, AC-13, AC-17 |

## Notas / decisiones de diseno

- **Snapshot, no solo puntero.** El proposito de la card es "clonar despues". Un puntero se rompe
  justo cuando el plan desaparece, que es cuando el guardado tiene valor. El snapshot guarda el
  encabezado y el itinerario; `plan_id` se guarda como columna plana, **sin FK**, para que la fila
  sobreviva al borrado del plan.
- **El plan vivo gana.** Mientras el plan exista y sea accesible, la card y el clon usan el plan
  vivo (las ediciones del autor se ven). El snapshot es el fallback, no la fuente principal.
- **Privado despues de guardado.** El guardado es privado del usuario que guardo; mostrarle el
  nombre y el estado de un plan que el mismo ya vio publico no filtra nada a terceros. El
  itinerario del original no se muestra: solo la card y el boton de clonar.
- **Borrar un plan no toca guardados ajenos.** A diferencia de [CAL], que borra sus calificaciones
  por la FK, aca el guardado debe sobrevivir. El unico borrado en cascada es usuario →
  sus guardados, en el ABM admin.
- **Un solo guardado por usuario y plan**, garantizado por la unique `(user_id, plan_id)`.
- **Confirmacion al quitar.** Se usa `confirm()` nativo en el submit del form (y antes del fetch en
  `/explore`). Sin JS el form sigue funcionando, solo no pregunta.
- **Sin contador publico.** La cantidad de guardados no se muestra: es una accion privada y un
  numero publico agrega presion social sin aportar al producto. Si se quisiera, va en card aparte.
- **Fuera de alcance:** paginacion, carpetas/etiquetas/notas, notificaciones de planes guardados,
  guardar planes privados, "clonado" como estado del guardado, contador publico.

## Referencia de Implementacion

> Los pasos a continuacion son guia de implementacion, no reemplazan los criterios de aceptacion.

### 1. Modelo

`src/main/java/com/valhalla/domain/bookmark/PlanBookmark.java`

```java
@Entity
@Table(name = "plan_bookmarks",
  uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "plan_id" }))
public class PlanBookmark {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional = false) @JoinColumn(name = "user_id") private User user;
  @Column(name = "plan_id", nullable = false) private UUID planId; // sin FK: sobrevive al borrado
  private String name;
  private String description;
  private LocalDate eventDate;
  private LocalTime eventTime;
  private int placeCount;
  private Long administratorId;
  private String administratorName;
  private LocalDateTime createdAt = LocalDateTime.now();
  @OneToMany(mappedBy = "bookmark", cascade = CascadeType.ALL, orphanRemoval = true,
    fetch = FetchType.LAZY) @OrderBy("sortOrder ASC")
  private List<PlanBookmarkPlace> places = new ArrayList<>();
}
```

`PlanBookmarkPlace`: `placeId` (Long, sin FK), `placeName`, `placeCategory`, `placeAddress`,
`description`, `visitDate`, `visitTime`, `sortOrder`.

`BookmarkStatus` (`AVAILABLE`/`PRIVATE`/`DELETED`) y `BookmarkEntry` (record de lectura con
`planId`, datos del snapshot o del plan vivo, `status` y `publicPlan`).

### 2. Servicio

`PlanBookmarkService`: `save(planId, email)`, `remove(planId, email)`, `isBookmarked(planId, email)`,
`getBookmarkedPlanIds(email)`, `getBookmarks(email)`, `cloneBookmark(planId, email)`.

- `save`: resuelve usuario y plan; exige `plan.getIsPublic()` (si no, `PlanNotFoundException`);
  si ya existe el par no hace nada; si no, copia encabezado e itinerario.
- `getBookmarks`: trae las filas del usuario ordenadas por `createdAt DESC, id DESC`, resuelve el
  plan de cada una y arma el `BookmarkEntry` (vivo si esta disponible, snapshot si no).
- `cloneBookmark`: busca el guardado del usuario; si el plan vivo es accesible delega en
  `planService.clonePlan()`; si no, arma un `Plan` con el snapshot y llama a
  `planService.createPlan()`, salteando lugares cuyo id ya no exista.

### 3. Endpoints

| Ruta | Verbo | Uso |
|------|-------|-----|
| `/bookmarks/{planId}` | POST | Guardar (form, `returnTo`) |
| `/bookmarks/{planId}/remove` | POST | Quitar (form, `returnTo`, con `confirm()`) |
| `/bookmarks/{planId}/clone` | POST | Clonar desde Guardados |
| `/api/plans/{planId}/bookmarks` | POST / DELETE | Toggle del sidebar de `/explore` (204) |

`returnTo` se valida con `LoginRedirects.isSafeInternalPath`; default `/plans/{planId}/public`.

### 4. UI

- `components/bookmark-button.html`: fragmento `toggle(planId, bookmarked, returnTo)` con el icono
  relleno/contorno, `aria-pressed` y `title`.
- `pages/plans/detail.html`, `pages/plans/public.html`, `pages/plans/public-list.html`,
  `pages/profile/view.html`: montan el fragmento.
- `pages/profile/edit.html`: la seccion `#bookmarks` deja de ser placeholder y lista las cards.
- `pages/places/list.html` (Vue): boton arriba a la derecha de cada card + fetch con los metas
  `_csrf` que ya expone `layouts/base.html`.
- `components/navbar-menu.html`: "Guardados" pasa de `<span aria-disabled>` a `<a>`.

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `docs/specs/20-MARK.md` | Crear |
| `domain/bookmark/PlanBookmark.java` | Crear |
| `domain/bookmark/PlanBookmarkPlace.java` | Crear |
| `domain/bookmark/BookmarkStatus.java` | Crear |
| `domain/bookmark/BookmarkEntry.java` | Crear |
| `domain/bookmark/PlanBookmarkRepository.java` | Crear |
| `domain/bookmark/PlanBookmarkService.java` | Crear |
| `infrastructure/bookmark/JpaPlanBookmarkRepository.java` | Crear |
| `infrastructure/bookmark/PlanBookmarkRepositoryImpl.java` | Crear |
| `infrastructure/bookmark/PlanBookmarkServiceImpl.java` | Crear |
| `infrastructure/user/UserServiceImpl.java` | Actualizar (borra guardados del usuario) |
| `presentation/bookmark/BookmarkController.java` | Crear |
| `presentation/bookmark/PlanBookmarkRestController.java` | Crear |
| `presentation/bookmark/BookmarkCards.java` | Crear |
| `presentation/plan/PlanController.java` | Actualizar (`isBookmarked` en detalle/publica) |
| `presentation/plan/ForYouController.java` | Actualizar (`bookmarked` por card) |
| `presentation/plan/PlanFeedCards.java` | Actualizar (clave `bookmarked`) |
| `presentation/plan/PlanRestController.java` | Actualizar (`bookmarked` en el JSON) |
| `presentation/profile/ProfileController.java` | Actualizar (guardados del perfil) |
| `components/bookmark-button.html` | Crear |
| `components/navbar-menu.html` | Actualizar |
| `pages/plans/detail.html` | Actualizar |
| `pages/plans/public.html` | Actualizar |
| `pages/plans/public-list.html` | Actualizar |
| `pages/profile/edit.html` | Actualizar |
| `pages/profile/view.html` | Actualizar |
| `pages/places/list.html` | Actualizar |
| `CONTEXT.md` | Actualizar (glosario) |
| `infrastructure/bookmark/PlanBookmarkServiceImplTest.java` | Crear (U-01..U-19) |
| `presentation/bookmark/BookmarkControllerTest.java` | Crear (U-20..U-23) |
| `integration/BookmarkControllerIntegrationTest.java` | Crear (I-01..I-13) |
| `integration/BookmarkSecurityTest.java` | Crear (S-01..S-06) |
| `presentation/plan/ForYouControllerTest.java` | Actualizar (clave `bookmarked`) |
| `presentation/plan/PlanRestControllerTest.java` | Actualizar (clave `bookmarked`) |
| `infrastructure/user/UserServiceTest.java` | Actualizar (borrado en cascada) |
| `e2e/BookmarkE2E.java` | Crear (E-01) |
| `e2e/ProfileViewE2E.java` | Actualizar (Guardados ya navega) |
