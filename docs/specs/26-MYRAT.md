# [MYRAT] Mis calificaciones en el perfil

> Trello: https://trello.com/c/PkTrDSEA/56-myrat-mis-calificaciones-en-el-perfil
> **Estimacion:** 3 pts Fibonacci
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.

## Objetivo

El perfil propio lista todas las calificaciones que el usuario dio, para que recuerde que opino de
cada plan y pueda volver a el. Cada fila trae el nombre del plan, el puntaje propio, el agregado
global (promedio y cantidad) y un link al plan. Un plan que dejo de ser accesible sigue apareciendo,
con su nombre persistido y un badge, pero sin link.

La seccion es **privada del dueno**: el perfil publico de otro usuario no la renderiza ni expone
calificaciones ajenas. Es el complemento natural de [CAL] (`16-CAL.md`), que guarda la autoria
interna de cada calificacion pero solo publica el agregado.

## Pre-requisitos

- [CAL] completed (`PlanRating` con unique `(plan_id, user_id)`, `PlanRatingService`, agregados por
  plan, y AC-12: borrar el plan borra sus calificaciones).
- [PROF] completed (`/profile/{id}` propio y publico, secciones con ancla en `edit.html`, modal del
  navbar).
- [MARK] completed como referencia de patron: seccion privada del perfil, contador, estado vacio y
  entrada del menu que linkea a `/profile#<ancla>`.
- [PABM] completed: el ban de un usuario sobre un plan es una de las reglas de acceso que la fila
  respeta.

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | `PlanRatingRepository.findByUserEmail(String email)` devuelve las calificaciones del usuario, mas recientes primero (`id` descendente) |
| AC-02 | `PlanRatingService.getRatingsByUser(String email)` devuelve, por cada calificacion, el plan y el puntaje del usuario. Un email nulo o vacio devuelve lista vacia sin consultar la base |
| AC-03 | El read model `RatedPlanView` lleva plan, puntaje propio y si el plan sigue siendo accesible para el usuario: dueno o participante entran siempre; el resto solo a un plan publico, activo y sin ban |
| AC-04 | El perfil PROPIO renderiza la seccion "Mis calificaciones" (`#my-ratings`) entre Guardados (`#bookmarks`) y Seguidores (`#follow`) |
| AC-05 | Cada fila muestra el nombre del plan, las estrellas del puntaje propio, el promedio y la cantidad globales, y un link al detalle `/plans/{id}` si el usuario es dueno o participante, o a la vista publica `/plans/{id}/public` si no |
| AC-06 | Un plan que dejo de ser accesible conserva su nombre persistido, suma el badge `No disponible` y no lleva link |
| AC-07 | Sin calificaciones la seccion muestra el texto exacto `Todavía no calificaste ningún plan` con link a `/for-you` |
| AC-08 | El encabezado de la seccion muestra el contador de calificaciones, y la entrada "Mis calificaciones" del modal del navbar linkea a `/profile#my-ratings` |
| AC-09 | La seccion es privada del dueno: el perfil publico de otro usuario no la renderiza ni expone calificaciones ajenas |
| AC-10 | `pwsh -File scripts/gate.ps1 all` en verde |

## Decisiones / aclaraciones

- **Nombre congelado = nombre persistido del plan.** La calificacion no guarda snapshot del plan (a
  diferencia de Guardados): [CAL] AC-12 borra las calificaciones cuando el plan se borra, asi que el
  unico caso de "plan no disponible" es un plan que existe pero dejo de ser accesible. Se lee su
  nombre actual, no una copia.
- **Orden: calificacion mas reciente primero.** `id` es UUID v7, asi que descendente por `id` es
  descendente por creacion, el mismo criterio que Guardados (`created_at desc, id desc`). No se
  agrega columna de fecha.
- **El badge es copy de presentacion, no regla de negocio.** La accesibilidad la resuelve el servicio
  (`RatedPlanView.accessible`); el texto `No disponible` vive en `RatedPlanCards`, espejo de
  `BookmarkCards`.
- **El promedio y la cantidad se resuelven en presentacion.** El controlador pide
  `getSummariesByPlanId()` una sola vez y cada fila toma su `RatingSummary`; sin fila en el mapa usa
  `new RatingSummary(planId, null, 0L)`, que el componente `:: indicator` no renderiza.
- **El link depende de la membresia, no solo del acceso.** Un plan publico que el usuario no
  administra ni participa se abre por la vista publica; el detalle es para dueno y participantes.
- **La fila no lleva cover.** Guardados muestra cover porque la card se usa para clonar; aca la fila
  es una linea de historial: nombre, estrellas propias y agregado.

## Escenarios de Test

### Tests Unitarios (`infrastructure/rating/PlanRatingServiceImplTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-MYRAT-01 | `getRatingsByUser` devuelve solo las calificaciones del usuario y en orden estable | AC-01, AC-02 |
| U-MYRAT-02 | `getRatingsByUser` marca no accesible un plan privado ajeno | AC-03 |
| U-MYRAT-03 | `getRatingsByUser` deja accesible un plan privado del que el usuario es participante | AC-03 |
| U-MYRAT-04 | `getRatingsByUser` quita el acceso con ban o con el plan desactivado | AC-03 |
| U-MYRAT-05 | `getRatingsByUser` sin email no consulta la base | AC-02 |

### Tests Unitarios (`presentation/profile/RatedPlanCardsTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-MYRAT-06 | Un plan accesible no lleva badge y conserva el puntaje propio | AC-05, AC-06 |
| U-MYRAT-07 | El agregado global se redondea a un decimal | AC-05 |
| U-MYRAT-08 | Un plan no accesible lleva el badge `No disponible` y el nombre persistido | AC-06 |

### Tests Unitarios (`presentation/profile/ProfileControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| `U10` (extendido) | La vista propia arma `myRatings` con el agregado de cada plan | AC-04, AC-05 |
| `U10f` | Sin calificaciones la lista queda vacia y el contador en cero | AC-07 |
| `U09` (extendido) | El modelo del perfil publico no lleva calificaciones | AC-09 |

### Tests de Integracion (`integration/ProfileRatingsIntegrationTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-MYRAT-01 | El perfil propio muestra la seccion con el plan calificado y el perfil ajeno no la expone | AC-04, AC-05, AC-09 |
| I-MYRAT-02 | Un plan calificado que paso a privado ajeno se muestra sin link y con el badge | AC-06 |
| I-MYRAT-03 | Sin calificaciones se renderiza el estado vacio con el link a `/for-you` | AC-07 |
| I-MYRAT-04 | El modal del navbar renderiza la entrada con el ancla de la seccion | AC-08 |

### E2E (`e2e/MyRatingsE2E.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| E-MYRAT-01 | Calificar un plan desde su vista publica lo hace aparecer en el perfil propio | AC-05 |
| E-MYRAT-02 | La entrada "Mis calificaciones" del menu navega al ancla de la seccion | AC-08 |

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `domain/rating/RatedPlanView.java` | Crear (read model: plan, puntaje propio, acceso y membresia) |
| `domain/rating/PlanRatingRepository.java` | Actualizar (`findByUserEmail`) |
| `domain/rating/PlanRatingService.java` | Actualizar (`getRatingsByUser`) |
| `infrastructure/rating/JpaPlanRatingRepository.java` | Actualizar (query por email, `order by rating.id desc`) |
| `infrastructure/rating/PlanRatingRepositoryImpl.java` | Actualizar (delegacion) |
| `infrastructure/rating/PlanRatingServiceImpl.java` | Actualizar (`getRatingsByUser` + `toView` con la regla de acceso) |
| `presentation/profile/RatedPlanCards.java` | Crear (modelo de la fila + copy del badge) |
| `presentation/profile/ProfileController.java` | Actualizar (`PlanRatingService` y `myRatings` en la vista propia) |
| `templates/pages/profile/edit.html` | Actualizar (seccion `#my-ratings` entre `#bookmarks` y `#follow`) |
| `templates/components/navbar-menu.html` | Actualizar (entrada `#menu-my-ratings` a `/profile#my-ratings`) |
| `test/.../PlanRatingServiceImplTest.java` | Actualizar (U-MYRAT-01..05) |
| `test/.../RatedPlanCardsTest.java` | Crear (U-MYRAT-06..08) |
| `test/.../ProfileControllerTest.java` | Actualizar (U-MYRAT-09..11) |
| `test/.../ProfileRatingsIntegrationTest.java` | Crear (I-MYRAT-01..04) |
| `test/.../MyRatingsE2E.java` | Crear (E-MYRAT-01..02) |
