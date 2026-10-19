# [CAL] Calificaciones de planes publicos

> Trello: https://trello.com/c/37QoApLq/25-com-comentarios-calificaciones-de-planes-públicos
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.

## Objetivo

Un usuario logueado puede calificar un plan publico con un puntaje de 1 a 5 estrellas, y cualquier
visitante de la vista publica (`GET /plans/{id}/public`, ver [PVP]) ve el promedio y la cantidad de
calificaciones del plan. Las tarjetas del feed (`GET /for-you`, ver [PPV]) muestran el mismo
indicador.

**El alcance de esta card es solo la calificacion.** La card de Trello tambien menciona
comentarios, pero se decide implementarlos como una feature futura separada: nada de esta spec
(modelo, endpoints ni UI) anticipa comentarios, no hay columna `comment` "por si acaso". Cuando
entren comentarios, extienden el modelo; no reutilizan un stub.

## Pre-requisitos

- [PLN] completed (`Plan` con `isPublic`, `administrator`, `planPlaces`)
- [AUT] completed (cadena default de `SecurityConfig`, sesiones)
- [PVP] completed (`GET /plans/{id}/public` es `permitAll`; ahi vive el widget)
- [PPV] completed (`GET /for-you` y sus tarjetas; ahi vive el indicador)

## Decisiones / aclaraciones

- **La calificacion no es anonima, es de exhibicion privada.** Cada calificacion guarda la autoria
  interna (`user_id` FK + unique constraint `(plan_id, user_id)`), pero nadie ve *quien mas*
  califico ni con que puntaje individual: lo unico que sale al HTML es el agregado (promedio +
  cantidad). La autoria interna es la que permite que el usuario reconozca su propia calificacion:
  la fila unica del widget rellena las estrellas hasta su puntaje, preseleccionado, siempre, en
  cualquier visita. Ese es el mecanismo de "ya califique este plan".
- **Quien califica**: cualquier usuario logueado, incluido el administrador del plan calificando su
  propia creacion.
- **Upsert**: recalificar reemplaza el puntaje anterior. Nunca hay dos filas por usuario y plan: la
  unique constraint es el guard real, el servicio hace find-or-create.
- **Feed sin reordenar**: las tarjetas muestran el indicador, pero el feed sigue sin `ORDER BY` (la
  decision de [PPV]: "sin criterio de relevancia"; rankear por promedio seria una decision nueva de
  producto, no parte de esta card).

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | Un usuario logueado puede calificar un plan publico con un puntaje de 1 a 5 estrellas desde la vista publica del plan |
| AC-02 | La calificacion guarda la autoria del usuario, pero el HTML nunca expone quien califico ni su puntaje individual: solo el promedio y la cantidad |
| AC-03 | La fila unica de estrellas rellena hasta la calificacion propia (preseleccionada en el radio), aunque ya haya votado antes |
| AC-04 | Recalificar reemplaza la calificacion anterior del usuario: nunca hay dos calificaciones del mismo usuario sobre el mismo plan |
| AC-05 | Sin sesion la fila unica de estrellas es el mismo widget con el relleno del promedio, envuelto en un link a `/auth/login?redirect=` que conserva la vuelta a la vista del plan (soft gate); no hay form de voto |
| AC-06 | Solo planes publicos aceptan calificaciones: calificar un plan privado o un id inexistente responde exactamente igual que un id inexistente en la vista publica |
| AC-07 | La vista publica muestra el promedio con 1 decimal y la cantidad de calificaciones; sin calificaciones muestra "Todavia sin calificaciones" |
| AC-08 | La fila de estrellas se llena de amarillo en proporcion al promedio (no solo estrellas enteras) mientras el usuario no haya calificado; ya calificado, rellena hasta su puntaje propio |
| AC-09 | El widget de voto es claramente interactivo al hover: las estrellas se rellenan hasta la hovered (preview del voto) y cambian de color/opacidad al pasar el mouse |
| AC-10 | Cada tarjeta de `/for-you` muestra el mismo indicador de estrellas junto al promedio y la cantidad |
| AC-11 | El feed de `/for-you` no se reordena por calificacion |
| AC-12 | Al borrar un plan se borran sus calificaciones; no quedan filas huerfanas |
| AC-13 | `POST /plans/{id}/ratings` exige sesion: un POST anonimo redirige a login y `GET /plans/*/public` sigue `permitAll` sin cambios |

## Escenarios de Test

### Tests Unitarios (`infrastructure/rating/PlanRatingServiceImplTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | `rate()` con plan publico y usuario nuevo crea la calificacion | AC-01 |
| U-02 | `rate()` con calificacion existente del mismo usuario reemplaza el puntaje (un solo save, una sola fila) | AC-04 |
| U-03 | `rate()` con score fuera de 1..5 lanza `IllegalArgumentException` | AC-01 |
| U-04 | `rate()` con plan privado lanza `PlanNotFoundException` | AC-06 |
| U-05 | `rate()` con id inexistente lanza `PlanNotFoundException` | AC-06 |
| U-06 | `rate()` no depende de la autoria: el administrador del plan puede calificar su propio plan | Decisiones |
| U-07 | `getSummary(planId)` devuelve promedio y cantidad correctos | AC-07 |
| U-08 | `getSummary()` con cero calificaciones devuelve count 0 y promedio null | AC-07 |
| U-09 | `getMyRating()` devuelve el puntaje propio o null si no califico | AC-03 |

### Tests Unitarios (`presentation/plan/PlanControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-10 | `showPublicPlan()` expone `ratingAverage` (1 decimal), `ratingCount` y `myRating` en el model | AC-02, AC-03, AC-07 |
| U-11 | `ratePlan()` con email de sesion llama al servicio con ese email y redirige a `/plans/{id}/public` | AC-01 |

### Tests de Integracion (`integration/PlanControllerIntegrationTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-01 | `POST /plans/{id}/ratings` con sesion y score valido → 302 a `/plans/{id}/public`; la vista muestra el promedio y la cantidad actualizados | AC-01, AC-07 |
| I-02 | Segundo `POST /plans/{id}/ratings` del mismo usuario con otro score → la cantidad sigue 1 y el promedio refleja el nuevo valor (reemplazo, no suma) | AC-04 |
| I-03 | `POST /plans/{id}/ratings` anonimo → 302 a login (exige sesion) | AC-05, AC-13 |
| I-04 | `POST /plans/{id}/ratings` sobre un plan privado → misma respuesta que un id inexistente | AC-06 |
| I-05 | La vista publica nunca renderiza emails ni ids de calificadores | AC-02 |
| I-06 | El indicador de la vista publica y de cada tarjeta de `/for-you` usa la capa de relleno proporcional al promedio | AC-08, AC-10 |
| I-07 | El widget con sesion tiene los 5 radios de score con el propio preseleccionado y la clase/estilo de hover | AC-03, AC-09 |

### Tests de Seguridad (`integration/PlanPublicSecurityTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| S-01 | `GET /plans/{id}/public` anonimo sigue 200 (regression guard del matcher `permitAll`) | AC-13 |
| S-02 | `POST /plans/{id}/ratings` anonimo → redirige a login | AC-13 |
| S-03 | La vista publica y el feed no filtran identidades de calificadores (ni emails ni ids de usuario) | AC-02 |

### E2E (minimos)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `RatingE2E` | Anonimo ve el promedio de un plan publico (fila unica al 80%, sin widget de voto) → login → vota 4 estrellas con un click → el radio queda preseleccionado y el agregado se actualiza al lado → recalifica 4: mismo valor marcado, la cantidad no crece | AC-01, AC-03, AC-04, AC-07, AC-08 |

## Notas / decisiones de diseño

- **Autoria interna, exhibicion privada.** La unica manera robusta de que el usuario sepa "ya
  califique" es que la calificacion este atada a su cuenta: `user_id` + unique
  `(plan_id, user_id)`. El anonimato real (sin autoria) haria imposible el upsert y el estado "ya
  vote". Lo privado es la *exhibicion*: el HTML muestra agregados, nunca individuos (S-03).
- **Indicador proporcional con dos capas.** La fila de estrellas se pinta dos veces: una capa base
  de estrellas vacias (gris) y encima una capa de estrellas amarillas clipada con
  `overflow: hidden` y `width: (promedio / 5 * 100)%`. Sin glifos de media estrella, sin JS, y
  funciona para cualquier promedio (3.5 → 70% de la capa amarilla visible). Accesible con
  `aria-label="X de 5, N calificaciones"`.
- **El widget con sesion es UNA fila de radios estilizados.** Cinco `<input type="radio"
  name="score">` con `@{/plans/{id}/ratings}` + `_csrf` oculto: un form postear comun, igual que
  join/clone. La fila es la misma para indicador y voto: mientras no hay voto propio, la capa
  amarilla del fragment pinta el promedio; ya votado, el radio `:checked` (via `:has`) rellena la
  fila hasta el puntaje propio. El DOM va en orden inverso (5..1) con `flex-row-reverse` para que
  el combinator `~` rellene hasta la estrella hover/selected (AC-09, sin JS). Con JS el click
  envia el form solo (auto-submit); `<noscript>` conserva un boton "Calificar" como fallback.
- **Anonimo: indicador envuelto en link al login.** Mismo patron que los CTAs de [PVP]: mismas
  estrellas, mismo relleno, pero el click lleva a `/auth/login?redirect=` con la vuelta a la vista.
  Un POST anonimo se perderia en el redirect al login (por eso el widget de voto solo existe con
  sesion).
- **Sin `@OneToMany ratings` en `Plan`.** Las calificaciones se consultan por agregados
  (`planId → avg, count`), nunca recorriendo la coleccion: agregar la coleccion a `Plan` pesaria en
  cada query de plan (la relacion es EAGER en este proyecto) por un dato que casi nunca se usa
  entero. El borrado en cascada se hace con `deleteAllByPlanId` en `deleteOwnedPlan`, no con
  cascada de coleccion (AC-12).
- **No hay N+1 en el feed.** Una sola query agrupada `planId → (avg, count)` alimenta todas las
  tarjetas; el `ForYouController` junta esa mapa con los planes al armar las cards.
- **El administrador puede calificar su propio plan.** La card no pone restricciones y el dueño es
  usuario igual que cualquiera; un "no te auto-califiques" seria una regla de producto nueva,
  fuera de alcance.

## TODOs futuros (fuera de alcance de esta card)

- **Comentarios en planes publicos**: feature propia, card nueva. El modelo de calificaciones no
  la anticipa.
- **"Mis calificaciones" en el perfil**: una seccion del perfil del usuario con todas las
  calificaciones que hizo, para reconocer de un vistazo los planes que ya califico. Bloqueado por
  la feature [PROF] de perfil de usuario: se construye cuando el perfil exista.

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `domain/rating/PlanRating.java` | Crear (entidad, unique `(plan_id, user_id)`) |
| `domain/rating/PlanRatingRepository.java` | Crear (interface de dominio) |
| `infrastructure/rating/JpaPlanRatingRepository.java` | Crear (Spring Data + query de agregados) |
| `infrastructure/rating/PlanRatingRepositoryImpl.java` | Crear (delegacion) |
| `infrastructure/plan/PlanServiceImpl.java` | Actualizar (`deleteOwnedPlan` borra calificaciones) |
| `domain/rating/PlanRatingService.java` | Crear (interface) |
| `infrastructure/rating/PlanRatingServiceImpl.java` | Crear (upsert + summary) |
| `presentation/plan/RatingRequest.java` | Crear (DTO `score` 1–5) |
| `presentation/plan/PlanController.java` | Actualizar (`POST /{id}/ratings`, model de la vista publica) |
| `presentation/plan/PlanFeedCards.java` | Actualizar (avg/count en `Card` y `planCard`) |
| `presentation/plan/ForYouController.java` | Actualizar (pasa summaries) |
| `templates/pages/plans/public.html` | Actualizar (indicador + widget) |
| `templates/pages/plans/public-list.html` | Actualizar (indicador en tarjetas) |
| `infrastructure/rating/PlanRatingServiceImplTest.java` | Crear (U-01..U-09) |
| `presentation/plan/PlanControllerTest.java` | Actualizar (U-10, U-11) |
| `integration/PlanControllerIntegrationTest.java` | Actualizar (I-01..I-07) |
| `integration/PlanPublicSecurityTest.java` | Actualizar (S-01..S-03) |
| `e2e/RatingE2E.java` | Crear (E-01) |
