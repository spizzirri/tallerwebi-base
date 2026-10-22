# [PET] Peticiones para entrar a plan privado

> Trello: https://trello.com/c/jp3Rb46u/28-pet-peticiones-para-entrar-a-plan-privado
> **Estimacion:** 8 pts Fibonacci
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.
> **Nota:** spec renumerada de `16-PET` a `21-PET` al integrarse a `main` (16 ya es [CAL]).
> **Estado (2026-10-21):** implementada en `task-20-PET`; los bans (AC-05/10/14/15) llegan con `task-19-BAN`.

## Objetivo

Un plan privado puede llegar a gente nueva mediante un **link compartible**. El dueño del plan
configura **quien puede hacer cada cosa** (compartir el link, invitar por la app, invitar por
email, gestionar solicitudes) con un selector de roles; quien recibe el link ve un **resumen
sanitizado** del plan y puede **solicitar participacion**; el dueño (y/o los editores, segun
configuracion) responde cada solicitud con **aceptar / rechazar / banear**.

Es el modelo de los grupos de WhatsApp: un grupo privado (solo se entra si el admin lo permite)
con permisos configurables por rol. La via actual de entrada de un privado (short code via
`POST /plans/join`) sigue existiendo; el link compartido de un privado hoy responde "como
inexistente", y aca pasa a resolver una landing de solicitud.

## Pre-requisitos

- [PLN] completed (`Plan`, `PlanManager`, `generateUniqueShortCode()`, join por short code).
- [AUT] completed (login, `LoginUrlAuthenticationEntryPoint`, `?redirect=` mecanica existente).
- [PVP] (`GET /plans/{id}/public`) es la ruta donde vive la landing: el entry point de Spring
  Security ya permite el GET anonimo y el redirect a login lo hace el controlador.
- [PROF] (`/users/{id}` perfil publico) para el link al perfil del solicitante en la seccion de
  solicitudes.
- Las invitaciones internas existentes (`PlanInvitation` + respond desde Mis Planes) siguen
  funcionando igual; PET les agrega permisos por selector y el canal email.

Decisiones (2026-10-17, con equipo):

1. **Resumen de la landing**: nombre, descripcion, fecha, cantidad de lugares del itinerario y
   cover del evento. Sin dueño, sin participantes, sin itinerario, sin emails (mismo criterio
   que S-04 de [PVP]).
2. **Settings = selector de roles por capacidad** (no booleans por rol): una dropdown con
   opciones fijas `OWNER` / `EDITORS` / `PARTICIPANTS` (participantes incluye editores,
   editores incluyen al dueño).
3. **Defaults**: `shareLink = OWNER` (compartir por link deshabilitado),
   `handleRequests = OWNER`, `inviteInternal = EDITORS` (comportamiento
   actual), `inviteByEmail = OWNER`.
4. **Configuracion post-creacion**: solo el dueño, en la pagina dedicada
   "Configuracion del plan". En creacion se configuran desde el **paso 3** del wizard de
   `/explore` (mismo aside de siempre, sin pagina de creacion standalone).
5. **Invitacion por email**: email **real** (SMTP) solo a usuarios **ya registrados**, con link
   de aceptacion por token. Sin SMTP configurado, el envio cae a un logger de consola
   (dev/tests).

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | `Plan` tiene 4 selectores de roles embebidos en `PlanSettings` (`shareLink`, `handleRequests`, `inviteInternal`, `inviteByEmail`); `shareLink = OWNER` significa compartir por link **deshabilitado**; los valores fuera de `OWNER/EDITORS/PARTICIPANTS` (y de `OWNER/EDITORS` para solicitudes) se rechazan |
| AC-02 | Los planes nuevos nacen con los defaults: sharing link `OWNER` (off), solicitudes `OWNER`, invitar interno `EDITORS`, invitar por email `OWNER` |
| AC-03 | En la creacion (wizard de `/explore`, paso 3) el dueño elige los 4 selectores y los valores nacen aplicados al plan |
| AC-04 | `GET/POST /plans/{id}/settings` (pagina "Configuracion del plan") es **owner-only**: para cualquier otro usuario responde igual que un id inexistente |
| AC-05 | La pagina de configuracion muestra las 4 dropdowns y la lista de **bans** (con quitar-ban por usuario) |
| AC-06 | Plan privado + no participante + sharing distinto de `OWNER` + con sesion: `GET /plans/{id}/public` muestra la landing de solicitud con el resumen sanitizado (nombre, descripcion, fecha, placeCount, cover) y **sin** datos privados (dueño, participantes, itinerario, emails) |
| AC-07 | Plan privado + sin sesion: la landing redirige a `/auth/login?redirect=/plans/{id}/public`; al volver del login cae en la landing |
| AC-08 | Plan privado + `shareLink = OWNER`: la landing informa "plan privado, no tenes acceso a la informacion", sin resumen ni form |
| AC-09 | Plan privado + ya participante/editor/dueño: la landing redirige al detalle del plan (no ven la landing) |
| AC-10 | Plan privado + solicitud pendiente: la landing muestra "ya enviaste tu solicitud" y no permite duplicar; un usuario ya banneado ve "no podes unirte a este plan" y tampoco el form |
| AC-11 | `POST /plans/{id}/join-requests` (boton "Solicitar participacion") crea la solicitud con sesion; sin sesion redirige a login y no crea nada |
| AC-12 | Plan publico: `GET /plans/{id}/public` se comporta igual que hoy (vista publica read-only, sin landing) |
| AC-13 | Seccion "Solicitudes" en el detalle: visible para el dueño, y para los editores solo si `handleRequests = EDITORS`; cada solicitud muestra el usuario con link a su perfil y las tres acciones (aceptar/rechazar/banear) |
| AC-14 | `POST /plans/{id}/join-requests/{requestId}/respond?action=accept|reject|ban`: **accept** agrega al requester como participant y elimina la solicitud; **reject** la elimina sin mas; **ban** la elimina, lo agrega a `bannedUsers` y, si ya participaba o era editor, lo remueve y pierde el rol |
| AC-15 | Un usuario banneado no puede solicitar de nuevo ni entrar via short code; para quien responde sin permiso (participant ajeno, otro usuario) la respuesta de respond es identica a un id inexistente |
| AC-16 | El boton "Compartir link" del detalle copia `/plans/{id}/public` y solo lo ve quien cumple `shareLink`: editores si `EDITORS`/`PARTICIPANTS`, participantes solo si `PARTICIPANTS`; con `OWNER` (sharing off) nadie lo ve |
| AC-17 | `inviteParticipant`: el canal **app** valida `inviteInternal` y el canal **email** valida `inviteByEmail`; ademas del front (form habilitado o no), el service responde como inexistente a quien no cumple |
| AC-18 | Invitacion con canal email: se genera `token` en la `PlanInvitation` y se envia **email real** al usuario registrado con el link de aceptacion; si no hay SMTP configurado el envio cae a un logger y la invitacion queda igualmente pendiente en la app |
| AC-19 | `GET /plans/{id}/invitations/{token}/accept` (permitAll, el token es la credencial): sin sesion redirige a login con `redirect` de vuelta; con sesion muestra la confirmacion y el POST (con CSRF) acepta si el logueado es el destinatario y cae en `/plans/{id}`; si el logueado no es el destinatario, error amigable sin aceptar nada |
| AC-20 | El token es de un solo uso: aceptar (o rechazar desde Mis Planes) invalida el link; invitar al mismo usuario dos veces genera tokens distintos |
| AC-21 | Las invitaciones internas responden desde Mis Planes igual que hoy (accept/reject); PET no cambia su flujo, solo agrega el canal email como via de llegada |

## Escenarios de Test

### Tests Unitarios (`domain/plan/PlanTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | `new Plan()` deja los 4 selectores en sus defaults | AC-01, AC-02 |
| U-02 | reject de valores invalidos en los selectores (p.ej. `"FOO"`) | AC-01 |
| U-03 | `canShareLink(email)`: dueño exige sharing habilitado, editor cumple con `EDITORS/PARTICIPANTS`, participant solo con `PARTICIPANTS`, ajeno nunca | AC-16 |
| U-04 | `canInviteInternal(email)` / `canInviteByEmail(email)` / `canHandleRequests(email)` con cada nivel del selector | AC-17, AC-13 |

### Tests Unitarios (`infrastructure/plan/PlanServiceImplTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-05 | `requestToJoin()` crea la solicitud pendiente | AC-11 |
| U-06 | `requestToJoin()` duplicado lanza `IllegalArgumentException` (pendiente ya existe) | AC-10 |
| U-07 | `requestToJoin()` con participant/editor/dueño lanza `IllegalArgumentException` | AC-10 |
| U-08 | `requestToJoin()` de un banneado lanza `IllegalArgumentException` | AC-10, AC-15 |
| U-09 | `requestToJoin()` con `shareLink = OWNER` lanza `PlanNotFoundException` | AC-08 |
| U-10 | `respondToJoinRequest(ACCEPT)` deja al requester como participant y saca la solicitud | AC-14 |
| U-11 | `respondToJoinRequest(REJECT)` deja los roles como estaban y saca la solicitud | AC-14 |
| U-12 | `respondToJoinRequest(BAN)` agrega el requester a `bannedUsers` y lo remueve de participants/editors | AC-14 |
| U-13 | `respondToJoinRequest()` por un participant sin permiso lanza `PlanNotFoundException` | AC-15 |
| U-14 | `respondToJoinRequest()` por un editor con `handleRequests = OWNER` lanza `PlanNotFoundException` | AC-13, AC-15 |
| U-15 | `showJoinLanding()` (via `getSharedPlan`) devuelve solo los campos sanitizados (sin emails, sin itinerario, sin dueño) | AC-06 |
| U-16 | `getSharedPlan()` con id inexistente lanza `PlanNotFoundException` (mismo trato que not found, sin confirmar existencia) | AC-12, AC-15 |
| U-17 | `inviteParticipant()` canal app con participant permitido por `inviteInternal = PARTICIPANTS` funciona | AC-17 |
| U-18 | `inviteParticipant()` canal email sin permiso por selector lanza `PlanNotFoundException` | AC-17 |
| U-19 | invitacion por email genera token unico; dos invitaciones al mismo usuario tienen tokens distintos | AC-19, AC-20 |
| U-20 | `updateSettings()` guarda los 4 selectores y rechaza valores invalidos | AC-04 |
| U-21 | `updateSettings()` de un editor lanza `PlanNotFoundException` | AC-04 |
| U-22 | `joinPlan(shortCode)` con usuario banneado lanza `IllegalArgumentException` y no agrega el participante | AC-15 |

### Tests Unitarios (`presentation/plan/PlanControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-23 | `showPublicPlan()` de un plan privado: `getSharedPlan` devuelve el plan y el controlador usa `showJoinLanding()`; sin sesion redirige a `/auth/login?redirect=...` | AC-07 |
| U-24 | `showPublicPlan()` con sesion y banneado/pending/can-request renderiza el estado correcto y nunca administratorName/participants en el model | AC-06, AC-08, AC-10 |
| U-25 | `showPublicPlan()` con participante del privado redirige al detalle | AC-09 |
| U-26 | `createPlan` mapea los 4 selectores del form al plan nuevo | AC-03 |
| U-27 | `respondToJoinRequest()` y `updateSettings()` toman el rol del usuario de la sesion (nunca del form) | AC-04, AC-14, AC-15 |

### Tests de Integracion (`integration/PlanControllerIntegrationTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-01 | `POST /plans` con los 4 selectores del paso 3 crea el plan con esos valores | AC-03 |
| I-02 | `GET /plans/{id}/settings` como dueño renderiza las dropdowns + bans; para editor/participante/extraño → redirect "Plan not found" | AC-04 |
| I-03 | `POST /plans/{id}/settings` del dueño cambia los selectores y persisten tras reload | AC-04, AC-05 |
| I-04 | `GET /plans/{id}/public` (privado, sharing on, sesion de extraño) renderiza la landing con resumen y sin datos privados en el HTML | AC-06 |
| I-05 | La landing sin sesion es 302 a `/auth/login?redirect=...`; despues del login cae en la landing | AC-07 |
| I-06 | `POST /plans/{id}/join-requests` crea la pendiente; segundo POST no duplica | AC-10, AC-11 |
| I-07 | Aceptar la solicitud aparece como participant en el detalle; rechazar y banear dejan `plan_banned_users` con el usuario | AC-14 |
| I-08 | `GET /plans/{id}/invitations/{token}/accept` con sesion del destinatario lo deja como participant y el token queda invalido (segunda vez error) | AC-19, AC-20 |
| I-09 | `GET /plans/{id}/public` de un plan privado con sharing `OWNER` renderiza "plan privado, no tenes acceso" sin resumen | AC-08 |
| I-10 | `GET /plans/{id}/public` de un plan publico sigue renderizando la vista publica sin landing | AC-12 |

### Tests de Seguridad (`integration/PlanJoinRequestSecurityTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| S-01 | El HTML de la landing no contiene emails, ni nombres de participantes/editores, ni lugares del itinerario, ni el "Creado por" del dueño | AC-06 |
| S-02 | `POST /plans/{id}/join-requests` anonimo → redirect a `/auth/login` y el estado de la base no cambia | AC-11 |
| S-03 | `POST /plans/{id}/settings` por un editor → mismo resultado que un id inexistente; los selectores no cambian | AC-04 |
| S-04 | `POST /plans/{id}/join-requests/{rid}/respond` por un participant/extraño no cambia los roles del plan | AC-15 |
| S-05 | El link de invitacion por token de A no acepta la invitacion de B (logueado distinto del destinatario) y no consume el token | AC-19, AC-20 |
| S-06 | La landing con sharing `OWNER` no renderiza el form de solicitud ni el resumen | AC-08 |

### E2E (minimos)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `PublicPlanJoinRequestE2E` | Dueño habilita sharing en `/plans/{id}/settings` → comparte `/plans/{id}/public` → extraño registrado la abre: ve resumen sanitizado → "Solicitar participacion" → dueño acepta en la seccion Solicitudes del detalle → el solicitante aparece en los participantes del detalle y su solicitud desaparece | AC-06, AC-11, AC-13, AC-14 |

## Notas / decisiones de diseño

- **`shareLink = OWNER` significa "compartir por link deshabilitado".** Quien recibe un
  link nunca es el dueño, entonces "solo el dueño" equivale a "nadie lo comparte": la landing
  responde "plan privado, no tenes acceso" — exactamente lo que pide la card. En ese modo el
  dueño incorpora gente con invitaciones (internas o por email). Cuando sube el nivel
  (`EDITORS`/`PARTICIPANTS`), la landing pasa a resumen + "Solicitar participacion". El
  requisito literal de la card ("si el dueño no eligio que participants/editores compartan el
  link, la vista informa que el plan es privado y no tienen acceso") queda modelado con un solo
  selector.
- **Privado ≡ inexistente donde el proyecto ya lo hace.** Settings (`GET/POST /settings`),
  responder solicitudes y `requestToJoin` con sharing off lanzan `PlanNotFoundException` y
  `GlobalExceptionHandler` responde como ante un id que no existe; no hay 403 ni "no autorizado".
  La landing es la unica excepcion consciente: para el visitante con un link legitimo se
  confirma la existencia del plan (que es privado) sin revelar ningun dato del plan — ese es el
  mensaje pedido por la card PET.
- **`PlanJoinRequest` e invitaciones son tablas hijas de `PlanManager`** (mismo patrón que
  invitations): pendiente unica por plan+requester con `@Version` optimista; las respuestas
  (accept/reject) eliminan el registro. El "ban" no es estado de la solicitud: `bannedUsers`
  sera una coleccion aparte (`task-19-BAN`), para que el ban sobreviva a la eliminacion de la
  solicitud.
- **El resumen se armo a imagen de `PlanFeedCards`** (nombre/desc/fecha/placeCount) pero con su
  propio record sin `administratorName` y con un cover generico: la landing no muestra
  participantes ni creador (decision 1); el controlador formula el record con los campos exactos
  y el template nunca recibe el `Plan` completo (U-15 y S-01 lo testean).
- **El token de invitacion es UUID con unique constraint** y llega por email real; al aceptar
  por link se consume (se elimina la invitacion). Sin SMTP: `LoggingEmailNotifier` escribe el
  mail en consola y la invitacion queda pendiente en Mis Planes igualmente — el canal email es
  una via de aviso, no un canal de control.

## Referencia de Implementacion

> Los pasos a continuacion son guia de implementacion, no reemplazan los criterios de aceptacion de arriba.

### 1. Selector de roles: modelo de settings

File: `src/main/java/com/valhalla/domain/plan/PlanPermission.java` (nueva)

```java
/** Nivel minimo requerido para una capacidad del plan. OWNER <= EDITORS <= PARTICIPANTS. */
public enum PlanPermission {
  OWNER,
  EDITORS,
  PARTICIPANTS;

  public boolean allows(Plan plan, String email) {
    return (
      plan.isAdministrator(email) ||
      (this != OWNER && plan.isEditor(email)) ||
      (this == PARTICIPANTS && plan.isParticipant(email))
    );
  }
}
```

File: `src/main/java/com/valhalla/domain/plan/PlanSettings.java` (nueva, `@Embeddable`)

```java
@Enumerated(EnumType.STRING)
private PlanPermission shareLink = PlanPermission.OWNER;        // OWNER == compartir link off
@Enumerated(EnumType.STRING)
private PlanPermission handleRequests = PlanPermission.OWNER;   // OWNER | EDITORS
@Enumerated(EnumType.STRING)
private PlanPermission inviteInternal = PlanPermission.EDITORS;
@Enumerated(EnumType.STRING)
private PlanPermission inviteByEmail = PlanPermission.OWNER;
```

Los getters devuelven el default cuando la columna es nula (planes heredados). `setHandleRequests`
rechaza `PARTICIPANTS` y `isSharingEnabled()` es `shareLink != OWNER` (AC-16).

File: `src/main/java/com/valhalla/domain/plan/Plan.java`

```java
@Embedded
private PlanSettings settings = new PlanSettings();

public boolean canShareLink(String email) {
  return getSettings().getShareLink().allows(this, email);
}
public boolean canInviteInternal(String email) { ... }
public boolean canInviteByEmail(String email) { ... }
public boolean canHandleRequests(String email) { ... }
public List<PlanJoinRequest> getJoinRequests() { ... }
public boolean hasPendingJoinRequest(String email) { ... }
```

Las capacidades son helpers que delegan en `PlanPermission.allows`; `updateFrom(Plan changes)`
copia los 4 selectores solo cuando el form los posee (una edicion ordinaria no los resetea).

### 2. `PlanJoinRequest` en `PlanManager`

File: `src/main/java/com/valhalla/domain/plan/PlanJoinRequest.java` (nueva)

```java
@Entity
@Table(
  name = "plan_join_requests",
  uniqueConstraints = @UniqueConstraint(columnNames = { "plan_id", "requester_id" })
)
public class PlanJoinRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "plan_id", nullable = false)
  private Plan plan;

  @ManyToOne(optional = false)
  @JoinColumn(name = "requester_id", nullable = false)
  private User requester;

  @Version
  private long version;
  // constructor(plan, requester) + getters
}
```

File: `src/main/java/com/valhalla/domain/plan/PlanManager.java`

```java
@OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
private List<PlanJoinRequest> joinRequests = new ArrayList<>();
```

`Plan` expone `hasPendingJoinRequest(email)`; el check recorre la coleccion embebida, sin queries
nuevas (mismo criterio que `isParticipant`).

> **Bans fuera de esta entrega.** `bannedUsers`, la tabla `plan_banned_users`, `isBanned(email)`,
> la decision `ban` y el quitar-ban de la configuracion se integran desde `task-19-BAN`; por eso
> los AC-05, AC-10, AC-14 y AC-15 se cumplen recien al mergear esa rama.

### 3. Token de invitacion (canal email)

File: `src/main/java/com/valhalla/domain/plan/PlanInvitation.java`

```java
@Column(unique = true, length = 64)
private String token; // generado en el constructor: UUID + sufijo aleatorio

public PlanInvitation(Plan plan, User recipient, User inviter, String role) {
  // ...
  this.token =
    java.util.UUID.randomUUID().toString() +
    java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 28);
}
```

El token se genera siempre al crear la invitacion (una invitacion nueva, un token nuevo — U-19).
El canal email no cambia la logica de roles: solo agrega el envio del mail despues de validar
`inviteByEmail`.

### 4. Servicio: solicitudes + settings + canales

File: `src/main/java/com/valhalla/domain/plan/PlanService.java` (interface)

```java
void requestToJoin(UUID id, String userEmail);
void respondToJoinRequest(UUID id, Long requestId, String action, String userEmail);
List<PendingJoinRequest> getPendingJoinRequests(UUID id, String userEmail);
void updateSettings(
  UUID id,
  PlanPermission shareLink,
  PlanPermission handleRequests,
  PlanPermission inviteInternal,
  PlanPermission inviteByEmail,
  String userEmail
);
Plan getSharedPlan(UUID id);

record PendingJoinRequest(Long id, Long requesterId, String requesterName) {}
```

File: `src/main/java/com/valhalla/infrastructure/plan/PlanServiceImpl.java`

```java
@Override
@Transactional
public void requestToJoin(UUID id, String userEmail) {
  Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
  if (!plan.getSettings().isSharingEnabled()) {
    // AC-08: sharing off, la landing dice "sin acceso"; el POST tiene que ser coherente.
    throw new PlanNotFoundException();
  }
  validateJoinRequestState(plan, userEmail);
  User requester = userRepository.findByEmail(userEmail)
    .orElseThrow(UserNotFoundException::new);
  plan.getPlanManager().getJoinRequests().add(new PlanJoinRequest(plan, requester));
}

private void validateJoinRequestState(Plan plan, String email) {
  if (plan.isAdministrator(email) || plan.isParticipant(email)) {
    throw new IllegalArgumentException("Ya formas parte de este plan.");
  }
  if (plan.hasPendingJoinRequest(email)) {
    throw new IllegalArgumentException("Ya enviaste tu solicitud.");
  }
}
```

`respondToJoinRequest` busca la `PlanJoinRequest` dentro del plan, verifica permiso con
`canHandleRequests` (sino `PlanNotFoundException`) y aplica:

```java
if (!"accept".equals(action) && !"reject".equals(action)) {
  throw new IllegalArgumentException("Respuesta de solicitud invalida.");
}
if ("accept".equals(action) && !plan.isParticipant(requester.getEmail())) {
  plan.getParticipants().add(requester);
  plan.getInvitations().removeIf(...); // limpia una invitacion pendiente del mismo usuario
}
plan.getJoinRequests().remove(request); // reject solo elimina la solicitud
```

La decision `ban` no existe en esta entrega: la agrega `task-19-BAN` junto con `isBanned` en
`joinPlan(shortCode)` (AC-15).

`updateSettings` aplica los 4 valores si `plan.isAdministrator(ownerEmail)`; sino,
`PlanNotFoundException` (AC-04). `setHandleRequests` rechaza `PARTICIPANTS` y el enum rechaza
cualquier valor desconocido.

### 5. Landing de solicitud en la vista publica

File: `src/main/java/com/valhalla/presentation/plan/PlanController.java`

```java
@GetMapping("/{id}/public")
public ModelAndView showPublicPlan(@PathVariable UUID id, Authentication authentication) {
  Plan plan = planService.getSharedPlan(id); // id inexistente -> PlanNotFoundException
  if (!plan.getIsPublic()) {
    return showJoinLanding(plan, currentEmail(authentication));
  }
  // ... vista publica de siempre (AC-12)
}
```

`showJoinLanding(plan, email)` resuelve los estados de la landing sin un enum aparte:

```java
if (email == null) return redirect("/auth/login?redirect=/plans/" + id + "/public"); // AC-07
if (plan.isAdministrator(email) || plan.isParticipant(email)) return redirect(detalle); // AC-09
model.put("sharingEnabled", plan.getSettings().isSharingEnabled());
model.put("requestPending", plan.hasPendingJoinRequest(email)); // AC-10
if (sharingEnabled) model.put("summary", new JoinPlanSummary(...)); // AC-06
return new ModelAndView("pages/plans/request", model);
```

El resumen es un record del controlador con solo nombre, descripcion, fecha, cantidad de lugares
y un cover generico:

```java
public record JoinPlanSummary(
  String name, String description, LocalDate eventDate, int placeCount, String cover
) {}
```

El template nunca recibe el `Plan` completo: sin dueño, sin participantes, sin emails ni
itinerario (U-15, S-01).

### 6. Pagina "Configuracion del plan" (owner-only)

File: `src/main/java/com/valhalla/presentation/plan/PlanController.java`

```java
private static final String VIEW_SETTINGS = "pages/plans/settings";

@GetMapping("/{id}/settings")
public ModelAndView showSettings(@PathVariable UUID id, Authentication authentication) {
  return new ModelAndView(
    "pages/plans/settings",
    ATTR_PLAN,
    planService.getOwnedPlan(id, authentication.getName()) // owner-only, sino not-found
  );
}

@PostMapping("/{id}/settings")
public ModelAndView updateSettings(
    @PathVariable UUID id,
    @RequestParam PlanPermission shareLink,
    @RequestParam PlanPermission handleRequests,
    @RequestParam PlanPermission inviteInternal,
    @RequestParam PlanPermission inviteByEmail,
    Authentication authentication) {
  planService.updateSettings(
    id, shareLink, handleRequests, inviteInternal, inviteByEmail, authentication.getName()
  );
  return new ModelAndView("redirect:/plans/" + id + "/settings?saved=true");
}
```

`pages/plans/settings.html` (nueva) reusa el fragmento `components/plan-permissions.html`: 4
`<select>` con las opciones "Solo el dueño / Dueño y editores / Dueño, editores y participantes"
(solicitudes solo con las dos primeras). La lista de bans con quitar-ban llega con `task-19-BAN`
(AC-05).

### 7. Secciones y CTAs en el detalle (`detail.html`)

- **Seccion Solicitudes** (AC-13): solo si `model["canHandleRequests"]`; cada card del
  requester con `th:href="@{/users/{id}(...)}"` al perfil y dos forms POST a
  `/plans/{id}/join-requests/{rid}/respond?action=accept|reject` (la accion `ban` llega con
  `task-19-BAN`).
- **Boton "Compartir link"** (AC-16): solo si `model["canShareLink"]`; copia
  `/plans/{id}/public` al portapapeles.
- **Form de invitacion** (AC-17): el input de email y el boton de canal app/email habilitados
  segun `model["canInviteInternal"]` / `model["canInviteByEmail"]`.

Los flags los computa `Plan` (`canShareLink`, `canInviteInternal`, `canInviteByEmail`,
`canHandleRequests`); el template nunca decide permisos por su cuenta.

### 8. Aceptacion por token + envio por email

File: `src/main/java/com/valhalla/presentation/plan/PlanController.java`

```java
@GetMapping("/{id}/invitations/{token}/accept") // permitAll; el token es la credencial
public ModelAndView acceptInvitationLink(...) {
  String email = currentEmail(authentication);
  if (email == null) {
    return new ModelAndView(
      "redirect:/auth/login?redirect=/plans/" + id + "/invitations/" + token + "/accept");
  }
  return new ModelAndView("pages/plans/accept-invitation", Map.of("planId", id, "token", token));
}

@PostMapping("/{id}/invitations/{token}/accept") // CSRF; exige la cuenta destinataria
public ModelAndView acceptInvitationLinkPost(...) {
  planService.acceptInvitationToken(id, token, authentication.getName());
  return new ModelAndView(REDIRECT_PLAN_DETAIL + id);
}
```

Abrir el GET no incorpora nada: muestra la confirmacion. El POST valida que el token exista y
que la sesion sea del destinatario, aplica el rol propuesto y elimina la invitacion (AC-19,
AC-20). Logueado distinto del destinatario: flash error amigable, sin tocar estado (S-05).
`SecurityConfig` agrega `GET /plans/*/invitations/*/accept` a los permitAll; el POST queda
detras de la sesion y del CSRF.

File: `src/main/java/com/valhalla/domain/plan/EmailNotifier.java` (nueva interfaz, sin framework)

```java
public interface EmailNotifier {
  void send(String recipient, UUID planId, String token);
}
```

File: `src/main/java/com/valhalla/infrastructure/email/EmailConfiguration.java` +
`LoggingEmailNotifier.java`

`EmailConfiguration` lee el `Environment` de Spring y devuelve el bean: sin `SMTP_HOST` usa
`LoggingEmailNotifier` (escribe el link de aceptacion por consola, sin tokens ni destinatarios);
con host arma un `JavaMailSenderImpl` (`SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`,
`SMTP_FROM`, `SMTP_AUTH`, `SMTP_STARTTLS`, `SMTP_SSL`) y envia con `APP_BASE_URL` como base del
link. `spring-context-support` + `org.eclipse.angus:angus-mail` aportan `JavaMailSender`. En
tests un fake reemplaza al bean, sin contactar el transporte. El mail lleva solo el link de
aceptacion — sin itinerario ni participantes.

### 9. Wizard paso 3 en `/explore`

File: `src/main/webapp/WEB-INF/templates/pages/places/list.html`

- `planStep` pasa a 3; el track interno de `w-[200%]` a `w-[300%]`; barra de progreso de 3
  segmentos y label "Paso X de 3" con el paso 3 = "Privacidad".
- Paso 1 (Datos) y paso 2 (Lugares) sin cambios.
- Paso 3: los 4 `<select>` con defaults preseleccionados (OWNER / OWNER / EDITORS / OWNER) y
  una nota corta: "el modo 'solo el dueño' para compartir link deja a las invitaciones como la
  via de entrada".
- `onPlanSubmit` ya serializa el form completo: los selects viajan en `POST /plans` con los
  nombres de los selector; `PlanRequest` agrega un objeto `settings` con los 4 campos y
  `createPlan` los aplica al plan nuevo (AC-03).
- Error server-side: vuelve a `/explore` como hoy (el form resetea; los selects son enum-only y
  su unico error real es un valor corrompido).

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `domain/plan/PlanPermission.java` | Crear (enum OWNER/EDITORS/PARTICIPANTS + `allows`) |
| `domain/plan/PlanSettings.java` | Crear (`@Embeddable` con los 4 selectores y sus defaults) |
| `domain/plan/Plan.java` | Actualizar (`@Embedded PlanSettings`, helpers de capacidad, `hasPendingJoinRequest`) |
| `domain/plan/PlanInvitation.java` | Actualizar (`token` unico generado al crear) |
| `domain/plan/PlanJoinRequest.java` | Crear (solicitudes pendientes) |
| `domain/plan/PlanManager.java` | Actualizar (coleccion `joinRequests`) |
| `domain/plan/PlanService.java` | Actualizar (requestToJoin, respond, settings, acceptInvitationToken) |
| `domain/plan/EmailNotifier.java` | Crear (interfaz de envio de mails) |
| `infrastructure/plan/PlanServiceImpl.java` | Actualizar (toda la logica PET) |
| `infrastructure/email/EmailConfiguration.java` | Crear (SMTP o fallback, AC-18) |
| `infrastructure/email/LoggingEmailNotifier.java` | Crear (fallback sin SMTP, AC-18) |
| `presentation/plan/PlanController.java` | Actualizar (landing, settings, join-requests, accept-by-token) |
| `presentation/plan/PlanRequest.java` | Actualizar (4 selectores del paso 3) |
| `config/SecurityConfig.java` | Actualizar (permitAll del GET accept-by-token) |
| `config/BaseJpaConfig.java` | Actualizar (paquete de entidades de email/plan) |
| `components/plan-permissions.html` | Crear (4 selectores reusables) |
| `pages/plans/settings.html` | Crear (AC-04) |
| `pages/plans/request.html` | Crear (AC-06/08/10) |
| `pages/plans/accept-invitation.html` | Crear (confirmacion del token) |
| `pages/plans/detail.html` | Actualizar (Solicitudes, compartir link, gating de invitaciones) |
| `pages/places/list.html` | Actualizar (paso 3 del wizard, AC-03) |
| `resources/core/js/plan-share.js` | Crear (copiar el link al portapapeles) |
| `.env.example` | Actualizar (SMTP_* y APP_BASE_URL) |
| `docker-compose.yml` | Actualizar (pasa las variables SMTP al proceso) |
| `pom.xml` | Actualizar (spring-context-support + angus-mail) |
| `domain/plan/PlanTest.java` | Actualizar (U-01..U-04) |
| `infrastructure/plan/PlanServiceImplTest.java` | Actualizar (U-05..U-22) |
| `presentation/plan/PlanControllerTest.java` | Actualizar (U-23..U-27) |
| `integration/PlanControllerIntegrationTest.java` | Actualizar (I-01..I-10) |
| `integration/PlanJoinRequestSecurityTest.java` | Crear (S-01..S-06) |
| `e2e/PublicPlanJoinRequestE2E.java` | Crear (E-01) |
