# [PVP] Vista de plan publico

> Trello: https://trello.com/c/Pv128dEC/18-pvp-vista-de-plan-publico
> **Estimacion:** 3 pts Fibonacci
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.

## Objetivo

Un visitante (con o sin sesion) puede ver la informacion publica de un plan en
`GET /plans/{id}/public`: nombre, descripcion, fecha/hora y el itinerario completo con sus
lugares, para decidir si une al plan o lo clona.

Hoy eso es imposible: `PlanController.showPlan()` llama a
`PlanServiceImpl.getParticipatingPlan(id, email)`, que responde `PlanNotFoundException` salvo que
el usuario sea administrator o participant. Un plan con `isPublic = true` no tiene ninguna ruta
que lo muestre a un desconocido, y `SecurityConfig` redirige cualquier GET de `/plans/**` sin
sesion a `/auth/login`.

## Pre-requisitos

- [PLN] completed (`Plan` con `isPublic`, `shortCode`, `administrator`, `participants`, `planPlaces`)
- [AUT] completed (cadena default de `SecurityConfig`, sesiones con `maximumSessions(1)`)
- El mapa de itinerario de `pages/plans/detail.html` es la referencia visual, pero esta vista es
  **read-only**: no hereda sus formularios de edicion.
- [CLO] es quien implementa el endpoint `POST /plans/{id}/clone` al que apunta el CTA "Clonar";
  esta spec solo lo referencia.

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | `GET /plans/{id}/public` sin sesion muestra el plan: nombre, descripcion, fecha y hora del evento |
| AC-02 | Muestra el itinerario completo: cada `PlanPlace` en orden `sortOrder`, con nombre y categoria del lugar, descripcion de la parada y fecha/hora de visita |
| AC-03 | Muestra el nombre del administrador del plan (quien lo creo) y la cantidad de participantes; nunca la lista de participantes ni sus emails |
| AC-04 | Un plan con `isPublic = false` responde **exactamente igual** que un id inexistente: la vista publica no revela que el plan exista |
| AC-05 | La vista no expone controles de edicion: sin "Eliminar plan", sin "Salir del plan", sin alta/baja ni reorden de lugares |
| AC-06 | Los CTAs "Unirse al plan" (`POST /plans/join` con el `shortCode`) y "Clonar plan" (`POST /plans/{id}/clone`, ver [CLO]) estan siempre visibles bajo el titulo, arriba del contenido |
| AC-07 | Sin sesion se ven los **mismos** CTAs ("Unirse al plan" y "Clonar plan"), pero como links a `/auth/login` conservando a donde volver; no ejecutan join/clone |
| AC-08 | Si el usuario logueado ya es administrator o participant, no se muestra "Unirse"; se muestra el link "Ver mi plan" a `/plans/{id}` |
| AC-09 | `GET /plans/{id}/public` es `permitAll`: responde 200 sin sesion y sin redirigir a login |
| AC-10 | El resto de `/plans/**` sigue protegido: `GET /plans/{id}` y `POST /plans/{id}/clone` sin sesion siguen redirigiendo a `/auth/login` |
| AC-11 | El link es copiable y compartible: no lleva tokens, ni shortCode en la URL, ni parametros de sesion |
| AC-12 | Muestra un mapa (Leaflet) sobre el itinerario con un marcador numerado por parada (mismo estilo de circulo por categoria que el detalle del plan), usando las coordenadas de cada lugar del plan, y una linea que une las paradas en orden cuando hay 2 o mas |
| AC-13 | Sin sesion el mapa y el itinerario se ven igual: la lectura no depende de la sesion |

## Escenarios de Test

### Tests Unitarios (`presentation/plan/PlanControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | `showPublicPlan()` con id publico retorna la vista `pages/plans/public` con el plan en el model | AC-01 |
| U-02 | `showPublicPlan()` con id privado lanza `PlanNotFoundException` | AC-04 |
| U-03 | `showPublicPlan()` con id inexistente lanza `PlanNotFoundException` | AC-04 |
| U-04 | `showPublicPlan()` arma `itineraryPlaces` respetando el orden `sortOrder` | AC-02 |
| U-05 | `showPublicPlan()` sin sesion deja `isLogged`, `isPlanAdministrator` e `isParticipant` en false y no agrega atributos de edicion | AC-05, AC-07, AC-08 |
| U-06 | `showPublicPlan()` con sesion de no-participante deja `isLogged` en true y el `shortCode` disponible para el form de join | AC-06 |
| U-11 | `showPublicPlan()` expone `participantCount` con el tamanho de la lista de participantes, sin exponer emails ni la lista | AC-03 |
| U-12 | `showPublicPlan()` expone `latitude`/`longitude` de cada parada en `itineraryPlaces` (lo que consume el mapa) | AC-12 |

### Tests Unitarios (`infrastructure/plan/PlanServiceImplTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-07 | `getPublicPlan(id)` con plan `isPublic = true` lo retorna | AC-01 |
| U-08 | `getPublicPlan(id)` con plan privado lanza `PlanNotFoundException` | AC-04 |
| U-09 | `getPublicPlan(id)` con id inexistente lanza `PlanNotFoundException` | AC-04 |
| U-10 | `getPublicPlan()` es `readOnly` y no llama a `save` ni toca participantes | AC-05 |

### Tests de Integracion (`integration/PlanControllerIntegrationTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-01 | `GET /plans/{id}/public` anonimo con plan publico → 200 con el nombre del plan | AC-01, AC-09 |
| I-02 | `GET /plans/{id}/public` anonimo con plan privado → misma respuesta que un id inexistente (mismo status y misma Location) | AC-04 |
| I-03 | `GET /plans/{id}/public` anonimo muestra el itinerario con los lugares del plan | AC-02 |
| I-04 | `GET /plans/{id}/public` con sesion de no-participante muestra los CTAs "Unirse" y "Clonar" como forms a los endpoints | AC-06 |
| I-05 | `GET /plans/{id}/public` con sesion de participant no muestra "Unirse" y si el link al detalle | AC-08 |
| I-06 | `GET /plans/{id}/public` no renderiza el form de edicion ni el boton "Eliminar plan" | AC-05 |
| I-07 | `GET /plans/{id}` anonimo sigue redirigiendo a `/auth/login` | AC-10 |
| I-08 | `GET /plans/{id}/public` anonimo muestra los mismos CTAs pero como links al login con `redirect`, sin forms ni CSRF | AC-07 |
| I-09 | `GET /plans/{id}/public` muestra el contador de participantes sin exponer emails ni la lista | AC-03 |
| I-10 | `GET /plans/{id}/public` renderiza el contenedor del mapa y las coordenadas de cada parada del itinerario | AC-12 |

### Tests de Seguridad (`integration/PlanPublicSecurityTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| S-01 | `GET /plans/{id}/public` anonimo → 200 (regression guard del matcher `permitAll`) | AC-09 |
| S-02 | `GET /plans/{id}/public` anonimo con plan privado no responde 200 con contenido | AC-04 |
| S-03 | `POST /plans/{id}/public` anonimo → redirige a login (el `permitAll` es solo GET) | AC-10 |
| S-04 | `GET /plans/{id}/public` no filtra informacion privada: ni emails de participants ni datos de planes privados | AC-04 |

### E2E (minimos)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `PublicPlanViewE2E` | Sin sesion, abrir el link publico → ver nombre, fecha, itinerario, mapa con marcadores y los CTAs al login; el CTA anonimo te lleva al login y te devuelve al plan | AC-01, AC-02, AC-07, AC-09, AC-12, AC-13 |

## Notas / decisiones de diseño

- **Ruta anidada bajo `/plans`.** La card escribe `/plan/:id/public`; se implementa como
  `GET /plans/{id}/public` para no abrir un namespace paralelo: `/plans` ya es el recurso y
  `/plans/{id}` ya existe. El slug publico es el id, no el `shortCode` (el shortCode es la llave
  de join, no de lectura).
- **Privado ≡ inexistente.** No se devuelve 403 ni "no autorizado": se lanza `PlanNotFoundException`
  y `GlobalExceptionHandler` responde igual que ante un id que no existe, así el endpoint no se
  vuelve un oracle de ids.
- **Sin contador de vistas.** No hay `viewCount` en `Plan` y esta spec no agrega uno: la card
  [PPV] tampoco lo pide aun (ver `14-PPV.md`).
- **CTA de clonar.** El boton apunta a `POST /plans/{id}/clone`, implementado por [CLO] y ya
  disponible: el form postea de verdad y redirige al detalle de la copia.
- **Unirse reutiliza el join existente.** No se crea un flujo nuevo: `POST /plans/join` con
  `shortCode` ya existe (`PlanController.joinPlan`) y agrega al usuario como participant.
- **Los CTAs son los mismos con o sin sesion (decision posterior).** El equipo decidio que el
  anonimo no ve un texto propio ("Inicia sesion para unirte o clonar") sino los mismos botones
  que el logueado; sin sesion cada uno es un link a `/auth/login?redirect=` con la vuelta a esta
  vista. Asi el CTA que el usuario aprende a usar en la vista publica es el mismo siempre.
- **Mapa read-only, sin el catalogo.** A diferencia de `pages/plans/detail.html` (que muestra
  todos los lugares y los del plan resaltados), aca solo se marcan las paradas del plan,
  numeradas en orden de visita: la vista es para mirar, no para editar. El JSON de paradas sale
  del mismo `itineraryPlaces` del model; los popups se arman con DOM, nunca con string
  concatenado, para que un nombre de lugar con HTML se renderice como texto. El estilo de los
  marcadores y la linea que une las paradas replica al detalle (referencia visual): circulo con
  color por categoria y borde amarillo `in-itinerary`, y polyline punteada solo con 2+ paradas.
- **Participantes: solo el contador.** El model expone `participantCount`, nunca la lista ni
  los emails (consistente con [PPV] AC-09 y con `PlanPublicSecurityTest` S-04).

## Referencia de Implementacion

> Los pasos a continuacion son guia de implementacion, no reemplazan los criterios de aceptacion de arriba.

### 1. Abrir la ruta en `SecurityConfig`

File: `src/main/java/com/valhalla/config/SecurityConfig.java`

En `defaultFilterChain`, el matcher tiene que ir **antes** de `.anyRequest().authenticated()`
(la cadena evalua en orden):

```java
auth
  .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/**", "/explore")
  .permitAll()
  .requestMatchers(org.springframework.http.HttpMethod.GET, "/plans/*/public")
  .permitAll()
  .requestMatchers("/api/**")
  .authenticated()
  // ... resto igual
```

### 2. Servicio de lectura publica

File: `src/main/java/com/valhalla/domain/plan/PlanService.java` (interface)

```java
Plan getPublicPlan(Long id);
```

File: `src/main/java/com/valhalla/infrastructure/plan/PlanServiceImpl.java`

```java
@Override
@Transactional(readOnly = true)
public Plan getPublicPlan(Long id) {
  Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
  if (!plan.getIsPublic()) {
    // Mismo camino que un id inexistente: la vista publica no confirma existencia.
    throw new PlanNotFoundException();
  }
  // El detalle publico renderiza lugares y administrador fuera de esta transaccion.
  plan.getPlanPlaces().forEach(entry -> entry.getPlace().getName());
  plan.getAdministrator().getEmail();
  return plan;
}
```

`plan.getAdministrator().getEmail()` sigue el mismo patron de warming que usa
`getParticipatingPlan()`: evita `LazyInitializationException` despues del commit.

### 3. Endpoint publico en `PlanController`

File: `src/main/java/com/valhalla/presentation/plan/PlanController.java`

```java
@GetMapping("/{id}/public")
public ModelAndView showPublicPlan(@PathVariable Long id, Authentication authentication) {
  Plan plan = planService.getPublicPlan(id);
  Map<String, Object> model = new ModelMap();
  model.put(ATTR_PLAN, plan);
  model.put("itineraryPlaces",
      plan.getPlanPlaces().stream().map(PlanController::placeView).toList());
  model.put("administratorName", displayName(plan.getAdministrator()));

  String email = currentEmail(authentication);
  boolean isParticipant = plan.isParticipant(email);
  boolean isPlanAdministrator = plan.isAdministrator(email);
  // Con anonimo el `Authentication` existe (AnonymousAuthenticationToken), por eso el boolean
  // sale del principal y no de `authentication != null`.
  model.put("isLogged", email != null);
  model.put("isParticipant", isParticipant);
  model.put("isPlanAdministrator", isPlanAdministrator);
  // El form de join necesita el codigo; en un plan publico compartirlo es el proposito.
  model.put("shortCode", plan.getShortCode());
  return new ModelAndView("pages/plans/public", model);
}

/** En anonimo el principal es el String "anonymousUser", no un UserDetails. */
private static String currentEmail(Authentication authentication) {
  if (authentication == null
      || !(authentication.getPrincipal() instanceof UserDetails)) {
    return null;
  }
  return authentication.getName();
}

private static String displayName(User user) {
  if (user == null) {
    return "";
  }
  String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
  String last = user.getLastName() == null ? "" : user.getLastName().trim();
  String full = (first + " " + last).trim();
  return full.isEmpty() ? user.getEmail() : full;
}
```

`placeView()` ya existe y es private static en la misma clase: se reutiliza tal cual, sin
duplicarlo.

### 4. Vista publica

File: `src/main/webapp/WEB-INF/templates/pages/plans/public.html` (crear)

Sigue el layout compartido (`layouts/base :: head` + `layout`), Tailwind con los tokens de
`brand.css` y **cero formularios de edicion**:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{layouts/base :: head(${plan.name})}"></head>
<body th:replace="~{layouts/base :: layout(~{::main})}">

<main class="container mx-auto px-4 py-8 max-w-3xl">
  <p class="text-sm uppercase tracking-wide text-gray-500">Public plan</p>
  <h1 class="text-3xl font-bold mt-1" th:text="${plan.name}">Plan</h1>
  <p class="text-gray-600 mt-2" th:text="${plan.description}"></p>
  <p class="text-gray-700 mt-2" th:if="${plan.eventDate != null}">
    <strong>Fecha:</strong> <span th:text="${plan.eventDate}"></span>
    <span th:if="${plan.eventTime != null}" th:text="| ${plan.eventTime}|"></span>
  </p>
  <p class="text-gray-600 mt-1" th:if="${!#strings.isEmpty(administratorName)}">
    <strong>Creado por:</strong> <span th:text="${administratorName}"></span>
  </p>

  <section class="bg-white rounded-lg shadow p-6 mt-6">
    <h2 class="text-xl font-bold mb-4">Itinerario</h2>
    <ol class="space-y-4" th:if="${!itineraryPlaces.isEmpty()}">
      <li class="flex gap-3" th:each="stop, it : ${itineraryPlaces}">
        <span class="w-7 h-7 shrink-0 rounded-full bg-gray-200 grid place-items-center font-bold"
              th:text="${it.count}">1</span>
        <div>
          <p class="font-semibold" th:text="${stop.name}">Lugar</p>
          <p class="text-xs text-gray-500" th:if="${stop.category != null}"
             th:text="${stop.category}">CATEGORY</p>
          <p class="text-sm text-gray-600" th:text="${stop.description}"></p>
          <p class="text-sm text-gray-500"
             th:if="${stop.visitDate != null or stop.visitTime != null}">
            <span th:if="${stop.visitDate != null}" th:text="${stop.visitDate}"></span>
            <span th:if="${stop.visitTime != null}" th:text="| ${stop.visitTime}|"></span>
          </p>
        </div>
      </li>
    </ol>
    <p class="text-gray-500" th:if="${itineraryPlaces.isEmpty()}">Este plan aun no tiene lugares.</p>
  </section>

  <!-- CTAs: join reutiliza el endpoint existente, clone lo implementa [CLO]. Siempre visibles
       bajo el titulo; sin sesion cada uno es un link al login con la vuelta al plan.
       Nunca se decide por #authentication: con anonimo ese objeto existe y seria siempre true. -->
  <div class="flex flex-wrap gap-3 mt-6">
    <form th:if="${isLogged and !isParticipant and !isPlanAdministrator}" method="post"
          th:action="@{/plans/join}" class="inline">
      <input type="hidden" name="shortCode" th:value="${shortCode}">
      <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">
      <button type="submit" class="bg-primary text-white px-4 py-2 rounded-md font-medium">
        Unirme al plan
      </button>
    </form>

    <form th:if="${isLogged and !isParticipant and !isPlanAdministrator}" method="post"
          th:action="@{/plans/{id}/clone(id=${plan.id})}" class="inline">
      <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">
      <button type="submit" class="border border-gray-800 text-gray-800 px-4 py-2 rounded-md font-medium">
        Clonar plan
      </button>
    </form>

    <a th:if="${!isLogged}"
       th:href="${#strings.isEmpty(currentPath)} ? @{/auth/login} : @{/auth/login(redirect=${currentPath})}"
       class="bg-primary text-white px-4 py-2 rounded-md font-medium">Unirme al plan</a>
    <a th:if="${!isLogged}"
       th:href="${#strings.isEmpty(currentPath)} ? @{/auth/login} : @{/auth/login(redirect=${currentPath})}"
       class="bg-primary text-white px-4 py-2 rounded-md font-medium">Clonar plan</a>

    <a th:if="${isParticipant or isPlanAdministrator}" th:href="@{/plans/{id}(id=${plan.id})}"
       class="bg-gray-800 text-white px-4 py-2 rounded-md font-medium">Ver mi plan</a>
  </div>
</main>
</body>
</html>
```

> El form de clonar postea a `POST /plans/{id}/clone`, que [CLO] ya implemento. El ternario de
> AC-07 vive en el model (`isLogged`, calculado en el controller), no en Thymeleaf: con anonimo el
> `Authentication` existe como `AnonymousAuthenticationToken`, y `#authentication != null` seria
> siempre true.

### 5. Link desde el listado publico [PPV]

`pages/plans/public.html` es el destino de cada tarjeta de `14-PPV.md`. No se agregan links
adicionales al navbar en esta spec.

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `config/SecurityConfig.java` | Actualizar (matcher GET `/plans/*/public` → `permitAll`) |
| `domain/plan/PlanService.java` | Actualizar (`getPublicPlan(Long id)`) |
| `infrastructure/plan/PlanServiceImpl.java` | Actualizar (implementacion `readOnly`) |
| `presentation/plan/PlanController.java` | Actualizar (`@GetMapping("/{id}/public")`) |
| `templates/pages/plans/public.html` | Crear (vista read-only con base layout) |
| `presentation/plan/PlanControllerTest.java` | Actualizar (U-01..U-06) |
| `infrastructure/plan/PlanServiceImplTest.java` | Actualizar (U-07..U-10) |
| `integration/PlanControllerIntegrationTest.java` | Actualizar (I-01..I-07) |
| `integration/PlanPublicSecurityTest.java` | Crear (S-01..S-04) |
| `e2e/PublicPlanViewE2E.java` | Crear (E-01) |
