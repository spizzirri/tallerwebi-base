# [NOTIF] Notificaciones: centro in-app

> Trello: [NOTIF] Notificaciones: centro in-app + push (origen: relevamiento de bugs 2026-10-22)
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.

## Objetivo

Un usuario con sesion tiene un **centro de notificaciones in-app** (`/notifications`) que acumula
actividad relevante sobre el:

- **Nuevos seguidores** — "X te empezó a seguir" (evento de [FOLLOW]).
- **Nuevos participantes en planes propios** — "X se unió a tu plan Y".
- **Actividad de usuarios seguidos** — "X publicó un plan nuevo Y".
- **Acceso a planes propios** — alguien te expulsó o bloqueó tu acceso, o el dueño cambió tu rol de participante/editor.

La navbar muestra una **campana con el contador de no-leidas** (badge). El centro lista leidas y no
leidas, mas nuevas primero, paginado de a 10, y permite marcar una o todas como leidas.

**Sin push en esta entrega**: las notificaciones push (planes populares cerca/pronto, invitaciones)
quedan como card separada.

## Pre-requisitos

- [FOLLOW] completed (`19-FOLLOW.md`): la relacion `follows` y `FollowRepository.findFollowers` son
  la fuente de destinatarios para "actividad de seguidos" y del evento de nuevo seguidor.
- [PPV]/[PLN] completed: `createPlan`, `joinPlan` por short code, solicitudes de unión e
  invitaciones resuelven participantes ya en `PlanServiceImpl`.
- [PROF] completed: el patron de identidad "el usuario sale de la sesión, el path nombra al objeto"
  se reusa en `/notifications/{id}/read`.

## Decisiones / aclaraciones

- **Sin agrupación en esta entrega.** Cada evento persiste su propia fila: "Ana te empezó a seguir"
  y "Luis te empezó a seguir" son dos notificaciones. Agrupar ("Ana, Luis y 3 más") queda para una
  entrega siguiente; no condiciona el modelo porque cada fila guarda actor y tipo por separado.
- **El evento se escribe en la transacción del hecho.** No hay bus de eventos: el que dispara la
  accion (`FollowServiceImpl`, `PlanServiceImpl`) ya tiene los usuarios resueltos y llama directo a
  `NotificationService`. Un follow repetido o un join de quien ya participa no notifican porque el
  early-return del hecho original corta antes.
- **Qué cuenta como "actividad de seguidos".** Solo **publicaciones**: crear un plan publico, o
  pasar un plan existente de privado a publico. Editar un plan ya publico no genera ruido (AC-03).
- **Nombre del plan congelado.** La notificacion guarda el nombre del plan al nacer; si el plan se
  renombra o se borra despues, el texto viejo queda y el link puede caer al 404 del plan. Es
  decision de producto: la notificacion es un hecho historico, no una vista viva.
- **Unfollow no borra ni marca.** Dejar de seguir no toca las notificaciones existentes: leer o
  marcar es decision del destinatario, no del evento que las genero (AC-11).
- **El badge es server-rendered.** `CurrentUserControllerAdvice` expone `unreadNotificationCount`
  en cada request; el render del redirect ya descuenta lo marcado, sin JS ni polling. En 0 no se
  pinta badge.
- **El numero del badge no es texto del DOM.** Viaja en `data-notifications-count` y se pinta con
  CSS `::after { content: attr(...) }` (brand.css): el pinneo E2E `WebPage.getNavbarItems`
  (`allTextContents`) sigue viendo el mismo set de siempre.
- **Nadie opera filas ajenas.** El id de la notificacion se cruza con el destinatario de la sesion
  en la busqueda (`findByIdAndRecipientId`): marcar una fila de otro es un no-op silencioso, no un
  403 con informacion (AC-09).
- **Rutas en la cadena default.** `/notifications**` cae en `anyRequest().authenticated()`: sin
  sesion redirige al login; los POST exigen CSRF como en [FOLLOW] (AC-08/AC-13).
- **Dejarse de seguir no retrocede el badge.** Las notificaciones viejas siguen contando hasta que
  el usuario las marca (AC-11).
- **Invitar también notifica (extensión, decisión de producto 2026-10-31).** La card original
  enumeraba tres eventos y las invitaciones no estaban ni a favor ni en contra; se decidió que el
  invitado reciba "X te invitó al plan Y". El destinatario siempre existe como `User`
  (`findInvitationRecipient` corta antes si no), así que la fila se escribe siempre, incluso si
  uno se invita a sí mismo. El link va a `/plans` (donde se aceptan las pendientes), no a
  `/plans/{id}`: el invitado todavía no participa y el detalle lo cortaría (AC-14).
- **Solicitudes de unión: ida y vuelta (decisión de producto 2026-11-01).** Pedir unirse a un plan
  privado notifica al dueño ("X solicitó unirse a tu plan Y", AC-15) y la respuesta —aceptada o
  rechazada— notifica al solicitante (AC-16): la solicitud es un pendiente de los dos y ninguna de
  las dos partes debería tener que entrar al plan para saber en qué quedó. El hook vive en
  `addJoinRequest`, no en `requestToJoin`, porque `joinPlan` con short code sobre un plan privado
  toma el mismo camino; la solicitud repetida corta antes y no duplica.
- **El centro es una bandeja de acciones, no un historial.** Cada fila enlaza al lugar donde esa
  notificacion se resuelve, y cuando la accion vive a mitad de pagina se lleva el ancla: la
  solicitud de union cae en `#join-requests-heading` del plan y la invitacion en
  `#pending-invitations-heading` de `/plans`, no arriba. El rechazo no tiene acceso al plan, así
  que enlaza al landing publico, que siempre muestra el resumen y permite volver a solicitar
  (AC-07). El tile de pendiente se define en `brand.css` (`.notification-row*`) y no como
  utilidad suelta: `bg-blue-50` no tiene complemento oscuro, así que en modo oscuro la fila no
  leida quedaba con slab celeste brilloso y texto casi blanco encima (AC-17).
- **Vaciar la bandeja (decisión de producto 2026-11-01).** Además de marcar leída, cada fila se
  puede tirar con un tachito: la borrada es física, no un `read=true` tardío, porque la bandeja es
  una lista de pendientes y no un archivo histórico. El destinatario vuelve a salir de la sesión
  (`findByIdAndRecipientId` + `delete`), así que borrar por id ajeno no-opera igual que marcarlo
  (AC-09/AC-18). Desaparecer una fila no toca el hecho origen: unfollow, salida del plan y
  renombre siguen sin limpiar nada (AC-11).
- **Qué NO notifica.** Que alguien salga de un plan, levante un baneo, edite un plan público ya
  publicado o califique un plan. Dejar de seguir tampoco avisa. Las expulsiones, los baneos y los
  cambios de rol sí se notifican porque cambian el acceso o las capacidades de la persona.

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | Seguir a un usuario crea UNA notificacion "X te empezó a seguir" para el seguido; repetir el follow no duplica la notificacion |
| AC-02 | Cuando un usuario pasa a ser participante de un plan ajeno (unirse por short code, solicitud aceptada o invitacion aceptada), el dueño recibe "X se unió a tu plan Y" |
| AC-03 | Crear un plan publico, o pasar un plan de privado a publico, crea "X publicó un plan nuevo Y" para cada seguidor del autor; editar un plan ya publico no notifica |
| AC-04 | `GET /notifications` lista las notificaciones del usuario de la sesion, mas nuevas primero, 10 por pagina, con paginacion previa/siguiente por `?page=N` |
| AC-05 | La navbar autenticada muestra una campana a `/notifications` con el numero de no-leidas; el numero desaparece en 0 |
| AC-06 | Marcar una leida (individual o "Marcar todas") baja el badge en el render siguiente y es idempotente |
| AC-07 | La notificacion linkia al lugar donde se resuelve: actor → `/profile/{id}`, plan propio → `/plans/{id}`, plan ajeno → `/plans/{id}/public`; cuando la accion vive en una seccion de la pagina, el link lleva su ancla (`/plans/{id}#join-requests-heading`, `/plans#pending-invitations-heading`) |
| AC-08 | Sin sesion, `/notifications` redirige al login; los POST de marcado exigen CSRF |
| AC-09 | El destinatario sale siempre de la sesion: marcar el id de una notificacion ajena no la modifica ni revela nada |
| AC-10 | El centro vacio muestra "Todavía no tenés notificaciones." |
| AC-11 | Borrar/deshacer el hecho origen (unfollow, salir del plan) no elimina ni marca notificaciones existentes |
| AC-12 | El texto de la notificacion es historico: renombre o eliminacion posterior del plan no reescriben la fila |
| AC-13 | `User` no gana una coleccion de notificaciones: listado, conteos y marcado son consultas sobre la tabla `notifications` |
| AC-14 | Invitar a un usuario a un plan crea "X te invitó al plan Y" para el invitado, con link a las pendientes de `/plans`; una invitacion que falla no notifica |
| AC-15 | Pedir unirse a un plan privado (solicitud o short code) crea "X solicitó unirse a tu plan Y" para el dueño, con link a la seccion Solicitudes del plan; repetir la solicitud no duplica |
| AC-16 | Aceptar o rechazar una solicitud crea "X aprobó/rechazó tu solicitud para el plan Y" para el solicitante: aceptada abre el plan, rechazada abre el landing publico (donde puede volver a solicitar) |
| AC-17 | La fila no leida se distingue de la leida (tinte + punto) y el tinte tiene su pareja oscura: ninguna fila queda con texto claro sobre fondo claro |
| AC-18 | Cada fila tiene un tachito: `POST /notifications/{id}/delete` (con CSRF) saca la fila propia de la bandeja y conserva la pagina; una fila ajena o inexistente es el mismo no-op silencioso de AC-09 |
| AC-19 | Expulsar a un participante crea una notificación para esa persona con el nombre del plan, pero sin `relatedPlanId` ni enlace al plan |
| AC-20 | Banear a un participante crea una notificación para esa persona con el nombre del plan, pero sin `relatedPlanId` ni enlace al plan |
| AC-21 | Promover o degradar a un participante crea una notificación con enlace al plan; repetir el rol actual no crea otra |

## Escenarios de Test

### Tests Unitarios (`domain/notification/NotificationTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | La notificacion nace no-leida, sin id ni timestamp (los pone la base) | AC-01 |
| U-02 | La notificacion con plan congela id y nombre | AC-12 |
| U-03 | `markAsRead` es idempotente | AC-06 |
| U-04 | Los once tipos existen y `valueOf` round-tripa | AC-01..AC-03, AC-14..AC-16, AC-19..AC-21 |

### Tests Unitarios (`infrastructure/notification/NotificationServiceImplTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | `notifyNewFollower` guarda la notificacion al seguido | AC-01 |
| U-02 | `notifyNewParticipant` guarda al dueño con el plan congelado | AC-02 |
| U-03 | Sin dueño o dueño == participante no escribe nada | AC-02 |
| U-04 | `notifyNewPlan` escribe una fila por cada seguidor | AC-03 |
| U-05 | Autor sin seguidores no escribe nada | AC-03 |
| U-06 | `list`/`count`/`unreadCount` delegan al repositorio | AC-04, AC-05 |
| U-07 | `markRead` encuentra la fila del destinatario y la marca | AC-06 |
| U-08 | `markRead` con id inexistente o ajeno es no-op silencioso | AC-09 |
| U-09 | `markAllRead` delega en una sola escritura | AC-06 |
| U-10 | `notifyInvitation` guarda al invitado con el plan congelado | AC-14 |
| U-11 | `notifyJoinRequest` guarda al dueño con el plan congelado | AC-15 |
| U-12 | `notifyJoinRequestResponse` aceptada guarda al solicitante con tipo `JOIN_REQUEST_ACCEPTED` | AC-16 |
| U-13 | `notifyJoinRequestResponse` rechazada usa `JOIN_REQUEST_REJECTED` | AC-16 |
| U-14 | `delete` tira la fila del destinatario; id ajeno o inexistente no borra nada | AC-18, AC-09 |

### Tests Unitarios (`infrastructure/follow/FollowServiceImplTest.java` y `infrastructure/plan/PlanServiceImplTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-10 | Follow feliz notifica; follow repetido no notifica de nuevo | AC-01 |
| U-11 | `createPlan` publico notifica plan nuevo; privado no | AC-03 |
| U-12 | `updatePlan` privado→publico notifica una vez; editar un publico no | AC-03 |
| U-13 | `joinPlan` publico notifica al dueño; el que ya participa no | AC-02 |
| U-14 | Aceptar solicitud de unión notifica al dueño y cierra la del solicitante | AC-02, AC-16 |
| U-15 | Aceptar invitación notifica al dueño | AC-02 |
| U-16 | `inviteParticipant` notifica al invitado; una invitacion rechazada por permisos no notifica | AC-14 |
| U-17 | `requestToJoin` notifica al dueño; solicitud repetida no duplica | AC-15 |
| U-18 | Rechazar una solicitud no suma participante pero si notifica al solicitante | AC-16 |

### Tests Unitarios (`presentation/notification/NotificationControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-20 | Primera pagina con mas filas: `hasNext` sin `hasPrev` | AC-04 |
| U-21 | Pagina negativa se aclampa a 0 | AC-04 |
| U-22 | Pagina fuera de rango cae a la ultima | AC-04 |
| U-23 | Cero notificaciones: vacio en pagina 0 | AC-10 |
| U-24 | `markRead` marca al destinatario de la sesion y conserva la pagina | AC-06, AC-09 |
| U-25 | `markAllRead` delega la escritura masiva y redirige al centro | AC-06 |
| U-26 | `delete` saca la fila del destinatario de la sesion y conserva la pagina | AC-18 |

### Tests Unitarios (`infrastructure/web/CurrentUserControllerAdviceTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-30 | El advice expone el unread count del usuario con sesion | AC-05 |
| U-31 | Principal sintetico (MockMvc) badge en 0, sin romper la pagina | AC-05 |
| U-32 | Sin sesion no se consultan las notificaciones | AC-08 |

### Tests de Integracion (`integration/NotificationIntegrationTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-01 | El follow crea la notificacion y el listado la muestra como fila de pendiente | AC-01, AC-04, AC-17 |
| I-03 | Marcar leida individual baja el badge y la fila pasa a estado leido | AC-06, AC-17 |
| I-04 | Dejar de seguir no borra ni marca la notificacion | AC-11 |
| I-05 | Marcar todas vacia el badge de una accion | AC-06 |
| I-06 | Paginacion de a 10 con ventana siguiente y links | AC-04 |
| I-07 | Marcar el id de una fila ajena es no-op | AC-09 |
| I-08 | `GET /notifications` sin sesion redirige al login | AC-08 |
| I-09 | Join por short code notifica al dueño del plan | AC-02 |
| I-10 | Plan publico nuevo notifica a los seguidores | AC-03 |
| I-11 | Invitacion notifica al invitado, linkia las pendientes y no filtra el email del invitador | AC-14, AC-12, AC-07 |
| I-12 | Solicitud de unión notifica al dueño con link a la seccion Solicitudes | AC-15, AC-07 |
| I-13 | Aceptar y rechazar notifican al solicitante, cada uno a su destino | AC-16, AC-07 |
| I-14 | El tachito saca la fila propia y la bandeja queda vacia; el id de una fila ajena no borra nada y sin CSRF es 403 | AC-18, AC-09, AC-13, AC-10 |

### E2E (minimos)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `NotificationE2E` | Seguir a un autor por UI; el autor ve el badge en la campana, navega al centro, marca leida, el badge vuelve a 0 y el tachito deja la bandeja vacia | AC-01, AC-05, AC-06, AC-07, AC-18 |

## Referencia de Implementacion

> Los pasos a continuacion son guia de implementacion, no reemplazan los criterios de aceptacion.

### 1. Entidad y tipos

File: `src/main/java/com/valhalla/domain/notification/Notification.java` (crear)

```java
@Entity
@Table(name = "notifications")
public class Notification {

  @Id
  @UuidGenerator(algorithm = UuidVersion7Generator.class)
  private UUID id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "recipient_id", nullable = false)
  private User recipient;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "actor_id", nullable = false)
  private User actor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type; // incluye eventos de seguimiento, planes, acceso y rol

  @Column(name = "related_plan_id")
  private UUID relatedPlanId;

  @Column(name = "related_plan_name")
  private String relatedPlanName;

  @Column(name = "read_flag", nullable = false)
  private boolean read;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false, nullable = false)
  private Instant createdAt;

  public void markAsRead() { this.read = true; }
}
```

`hbm2ddl.auto=update` crea la tabla: no hay migraciones en el proyecto. `ResetDatabase` (E2E) debe
borrar `notifications` antes de `users` por las dos FK.

### 2. Puertos y adaptadores

`domain/notification/NotificationRepository` (save, saveAll, `findByRecipientId(id, page, size)`,
`countByRecipientId`, `countUnreadByRecipientId`, `findByIdAndRecipientId`, `markAllRead`, `delete`)
con `infrastructure/notification/NotificationRepositoryImpl` delegando en `JpaNotificationRepository`
(Spring Data; la ventana usa `Pageable` para no ensuciar el puerto).

### 3. Servicio

`domain/notification/NotificationService` + `infrastructure/notification/NotificationServiceImpl`:

```java
void notifyNewFollower(User follower, User followed);            // recipient = followed
void notifyNewParticipant(User participant, User owner, UUID planId, String planName); // no-op si owner null o owner == participant
void notifyNewPlan(User owner, UUID planId, String planName);    // una fila por seguidor (FollowRepository.findFollowers)
List<Notification> list(UUID recipientId, int page, int size);
long count(UUID recipientId);  long unreadCount(UUID recipientId);
void markRead(UUID recipientId, UUID notificationId);            // findByIdAndRecipientId -> no-op si ajeno
void markAllRead(UUID recipientId);
void delete(UUID recipientId, UUID notificationId);              // mismo cruce: ajena o inexistente, no-op (AC-18)
```

### 4. Hooks en origen

- `FollowServiceImpl.follow`: tras `save(new Follow(...))`, `notifyNewFollower(follower, followed)`.
  El early-return del follow repetido corta antes: no hay duplicados (AC-01).
- `PlanServiceImpl`: `notifyNewParticipant` en los tres caminos que suman participante
  (`joinPlan` publico, `respondToJoinRequest` accept, `respondToInvitation` accept);
  `notifyNewPlan` en `createPlan` si `isPublic` y en `updatePlan` cuando `isPublic` pasa de
  falso a verdadero (AC-03).

### 5. Controller y badge

`presentation/notification/NotificationController`: `GET /notifications` (page aclampado,
PAGE_SIZE=10), `POST /notifications/{id}/read` (conserva `?page`), `POST /notifications/read-all`.
Identidad desde `Authentication` como en [FOLLOW] (AC-09).

`CurrentUserControllerAdvice.unreadNotificationCount` expone el contador a todos los templates;
`components/navbar.html` pinta `#navbar-notifications` con `data-notifications-count` y `brand.css`
lo renderiza por `::after { content: attr(data-notifications-count) }` (AC-05, sin tocar el pinneo
de `getNavbarItems`). En mobile, el item `#menu-notifications` del modal del navbar lleva el mismo
contador como texto.

### 6. Vista

`templates/pages/notifications/list.html`: header con "Marcar todas como leídas" (solo si hay
pendientes), lista con estado vacio `#notifications-empty`, filas `data-testid="notification"` con
`data-read`, texto por tipo (`th:switch` sobre `n.type.name()`), link al actor y al plan segun AC-07,
boton "Marcar leída" por fila no-leida y paginacion Anteriores/Siguientes.

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `domain/notification/Notification.java` | Crear (entidad) |
| `domain/notification/NotificationType.java` | Crear (enum de tipos) |
| `domain/notification/NotificationRepository.java` | Crear (puerto) |
| `domain/notification/NotificationService.java` | Crear (interfaz) |
| `infrastructure/notification/JpaNotificationRepository.java` | Crear (Spring Data) |
| `infrastructure/notification/NotificationRepositoryImpl.java` | Crear (adaptador) |
| `infrastructure/notification/NotificationServiceImpl.java` | Crear (reglas AC-01..AC-03, AC-09) |
| `infrastructure/follow/FollowServiceImpl.java` | Actualizar (hook de nuevo seguidor) |
| `infrastructure/plan/PlanServiceImpl.java` | Actualizar (hooks de participante y publicacion) |
| `presentation/notification/NotificationController.java` | Crear (listado + marcado) |
| `infrastructure/web/CurrentUserControllerAdvice.java` | Actualizar (`unreadNotificationCount`) |
| `templates/components/navbar.html` | Actualizar (campana con badge) |
| `templates/components/navbar-menu.html` | Actualizar (item "Notificaciones" mobile) |
| `templates/pages/notifications/list.html` | Crear (centro) |
| `resources/css/brand.css` | Actualizar (badge por `::after attr()`) |
| `domain/follow/FollowService.java` (javadoc) / `CONTEXT.md` | Actualizar ("no envía notificaciones" deja de ser cierto) |
| `infrastructure/notification/*Test.java`, `domain/notification/NotificationTest.java` | Crear (U-01..U-13) |
| `infrastructure/follow/FollowServiceImplTest.java` | Actualizar (U-10) |
| `infrastructure/plan/PlanServiceImplTest.java` | Actualizar (U-11..U-18) |
| `presentation/notification/NotificationControllerTest.java` | Crear (U-20..U-25) |
| `infrastructure/web/CurrentUserControllerAdviceTest.java` | Actualizar (U-30..U-32) |
| `integration/NotificationIntegrationTest.java` | Crear (I-01..I-13) |
| `e2e/NotificationE2E.java` | Crear (E-01) |
| `e2e/ResetDatabase.java` | Actualizar (`DELETE FROM notifications`) |
