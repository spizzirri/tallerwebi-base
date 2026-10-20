# [PPV] Listado de planes publicos

> Trello: https://trello.com/c/2IQRizj1/20-ppv-vista-de-planes-publicos-mas-vistos
> **Estimacion:** 3 pts Fibonacci
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.

## Objetivo

Un visitante puede recorrer todos los planes publicos de la plataforma en
`GET /for-you` — con o sin sesion — y entrar desde cada tarjeta a su vista publica
(`/plans/{id}/public`, ver [PVP]).

Hoy no existe forma de descubrir un plan ajeno: `GET /plans` exige sesion y devuelve solo los
planes propios (`getPlansByUserEmail`) mas los de los que el usuario participa
(`getParticipantPlans`). Un plan con `isPublic = true` es inalcanzable salvo que alguien tenga el
link exacto.

> El nombre de la card es "planes publicos **mas vistos**". Esta card se implemento sin criterio
> de orden porque en ese momento no habia uno definido; el ranking se cerro despues en [TOP]
> (`17-TOP.md`) y hoy el feed ordena por popularidad. La busqueda y los filtros son [SEARCH]
> (`18-SEARCH.md`).

## Pre-requisitos

- [PLN] completed (`Plan.isPublic`, `administrator`, `planPlaces`)
- [AUT] completed
- [PVP] es el destino de cada tarjeta: sin su ruta `/plans/{id}/public` el listado no tiene a donde
  linkear. Si [PVP] entra despues, el link de la tarjeta puede quedar apuntando a la vista publica
  aun no existente.

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | `GET /for-you` sin sesion responde 200 con el listado completo de planes `isPublic = true` |
| AC-02 | Ningun plan privado aparece en el listado: ni nombre, ni datos, ni cantidad |
| AC-03 | Cada tarjeta muestra nombre, descripcion, fecha del evento, cantidad de lugares del itinerario y nombre del administrador |
| AC-04 | Cada tarjeta linkea a la vista publica del plan (`/plans/{id}/public`) |
| AC-05 | El listado se ordena por popularidad segun [TOP] (`17-TOP.md`) y los planes propios del logueado quedan fuera; no se pagina |
| AC-06 | Sin planes publicos, la pagina muestra un estado vacio amigable con link al registro (`/auth/register`) |
| AC-07 | El navbar tiene link "Para vos" a `/for-you`, visible siempre (con y sin sesion; fuera de la variante admin) |
| AC-08 | `GET /for-you` es `permitAll`; cualquier otro verbo sobre esa ruta y el resto de `/plans/**` siguen exigiendo sesion |
| AC-09 | El listado expone solo campos publicos: ni `shortCode`, ni emails, ni ids de participantes |
| AC-10 | Con sesion la pagina tambien funciona igual salvo por AC-05: los planes propios del logueado no aparecen |

## Escenarios de Test

### Tests Unitarios (`presentation/plan/ForYouControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | `forYou()` retorna la vista `pages/plans/public-list` con todos los planes publicos | AC-01 |
| U-02 | `forYou()` arma las tarjetas con nombre, descripcion, fecha, cantidad de lugares, administrador y `cover` (imagen generica) | AC-03 (decisiones en Notas) |
| U-02b | `forYou()` deja fuera del listado los planes cuyo administrator es el usuario logueado | AC-05 |
| U-02c | `forYou()` con principal anonimo no filtra nada: el visitante ve todos los planes publicos | AC-01, AC-10 |
| U-02d | `forYou()` con principal null no lanza (filtro de propios tolerante) | AC-01 |
| U-03 | `forYou()` con la base vacia deja la lista vacia (sin error) | AC-06 |
| U-04 | `forYou()` no pone `shortCode` ni emails de participantes en el model (claves exactas, incluye `cover`) | AC-09 |

### Tests Unitarios (`infrastructure/plan/PlanServiceImplTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-05 | `getPublicPlans()` retorna solo los planes con `isPublic = true` | AC-01, AC-02 |
| U-06 | `getPublicPlans()` con ninguna base publica retorna lista vacia | AC-06 |
| U-07 | `getPublicPlans()` no llama a `save` ni modifica planes | AC-05 |

### Tests de Integracion (`integration/PlanControllerIntegrationTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-01 | `GET /for-you` anonimo → 200 con el listado (el feed es visible sin sesion) y el CTA de la card es link al login con `redirect=/for-you` | AC-01, AC-08 |
| I-01c | `GET /plans/public` (ruta vieja borrada): anonimo → 302 a login (la cadena `/plans/**` sigue autenticada), con sesion → 404 (el literal cae en `/{id}` y no parsea Long) | Notas (ruta vieja) |
| I-01b | `GET /for-you` con sesion → 200 con el nombre de un plan publico | AC-01 |
| I-02 | `GET /for-you` no contiene el nombre de ningun plan privado | AC-02 |
| I-03 | `GET /for-you` con cero planes publicos → 200 con el estado vacio | AC-06 |
| I-04 | Cada tarjeta del listado linkea a `/plans/{id}/public` | AC-04 |
| I-05 | Los planes propios no aparecen en el feed y ninguna tarjeta ofrece "Editar"; "Clonar Plan" una vez por plan listado | decision planes propios |
| I-06 | Cada tarjeta muestra su cover visual (`/images/plans/cover-N.svg`, `alt` = nombre) y el SVG se sirve | decision "cover" |

### Tests de Seguridad (`integration/PlanPublicSecurityTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| S-01 | `GET /for-you` anonimo → 200 (matcher `permitAll` para GET) | AC-08 |
| S-01b | `GET /for-you` con sesion → 200 | AC-01 |
| S-02 | `POST /for-you` anonimo → redirige a login (el permitAll es solo GET) | AC-08 |
| S-03 | El listado no filtra informacion privada de los planes (emails, shortCode, participantes) | AC-09 |

### E2E (minimos)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `PublicPlansListE2E` (T-PPV-005) | Sin sesion: el navbar ofrece "Para vos" y la ruta rinde el feed con CTA de la card como link al login; con sesion de otro usuario: tarjeta con cover que carga y "Clonar Plan" clona; el plan propio del logueado no aparece | AC-01, AC-03, AC-04, AC-05, AC-07, AC-09 |

## Notas / decisiones de diseño

- **Ranking (actualizado por [TOP]).** Esta card se implemento sin criterio de orden porque no
  existia uno definido; `findByIsPublicTrue()` sigue sin `ORDER BY` y no es el lugar del criterio.
  [TOP] (`17-TOP.md`) define el orden: calificacion bayesiana con participantes como primer
  desempate, aplicado en la capa de descubrimiento. `AC-05` de esta spec queda superado por
  `17-TOP.md` AC-01. La busqueda, el filtrado y los selects de orden son [SEARCH]
  (`18-SEARCH.md`).
- **Pagina propia, no seccion del landing.** Se decidio una ruta propia (`/for-you`) en vez
  de un bloque en `pages/landing.html` o sumarlo a `/explore` (que es de lugares): el landing es
  contenido editorial y `/explore` ya tiene su propio modelo Vue. Si el equipo prefiere otra
  ubicacion, solo cambia el paso 4 (controller + template) y el AC-07.
- **Ruta `/for-you` y borrado de `/plans/public`.** La ruta original `/plans/public` se reemplazo
  por `GET /for-you` (nombre de producto, no tecnico) y el literal viejo se borro del todo: sin
  mapping, un anonimo que la pida rebota en la cadena `/plans/**` (302 a login, I-01c) y con sesion
  cae en `GET /plans/{id}`, `MethodArgumentTypeMismatchException` responde 404 (ver
  `GlobalExceptionHandler`; lo afirma I-01c).
- **Tarjetas server-side.** No se agrega endpoint REST nuevo ni Vue: el listado se renderiza con
  Thymeleaf como `pages/plans/list.html`. No hay paginacion porque el volumen de un TP no la
  justifica; si crece, se agrega con `?page=` sin tocar los ACs de visibilidad.
- **Feed visible con y sin sesion (decision de producto, revierte la decision anterior).**
  Primero el equipo decidio que el feed exigia sesion; despues decidio que el visitante debe poder
  verlo sin registrarse. Consecuencias vigentes: `GET /for-you` es `permitAll` para GET, el link
  "Para vos" aparece en el navbar siempre (con y sin sesion, salvo la variante admin), y login y
  registro aterrizan ahi (`LoginRedirects.USER_LANDING = "/for-you"`). Con sesion ademas se aplica
  el filtro de planes propios (AC-05); sin sesion no hay planes propios que filtrar.
- **CTA "Clonar Plan" con y sin sesion.** Logueado: form POST a `POST /plans/{id}/clone` (el
  endpoint es de [CLO]). Sin sesion: el mismo label pero como link a
  `/auth/login?redirect=/for-you`, asi el visitante vuelve al feed tras loguearse y clona — mismo
  patron que los CTAs de la vista publica del plan (13-PVP). Un POST desde el anonimo se perdia en
  el redirect al login.
- **"Clonar Plan" = clonar (el endpoint es de [CLO]).** El boton de la tarjeta ejecuta
  `POST /plans/{id}/clone`, que pertenece a [CLO]: lo implementa `PlanController` y sus pruebas de
  seguridad son `integration/PlanCloneSecurityTest.java` (ver "Ownership del endpoint" en
  15-CLO.md). Este card solo consume ese endpoint, no duplica la logica, y deja al usuario en su
  copia editable. La vista `/plans/{id}/public` de [PVP] ya existe y es el destino de cada tarjeta.
- **El feed es solo para descubrir planes de otros (decision posterior).** Los planes propios del
  logueado —publicos o privados— no aparecen en el listado; por eso la tarjeta no ofrece "Editar"
  ni el model expone el flag `own`: el feed no gestiona lo propio, eso vive en `/plans`.
- **"Crear plan" desde el feed.** Boton "+ Crear plan" en la cabecera que lleva a `/explore`,
  donde vive el modal de creacion, para el caso "nada de lo que hay me interesa".
- **Usuarios y planes fantasma de demo.** `PublicPlanDataSeeder` siembra 5 personajes con 6
  planes publicos para que el muro nunca arranque vacio. Apagado por defecto (no corre en las
  suites): se enciende con `-Dseed.demoPlans=true`, que es lo que hace `docker-dev.sh`.
- **Cover visual generico por tarjeta.** Cada tarjeta muestra una imagen fija de
  `/images/plans/cover-N.svg` elegida con `id % 6` (deterministico, sin datos en la base y con
  variantes repartidas entre los planes). No hay columna de imagen en `Plan`: cuando se quiera
  personalizar por plan (o subir fotos de los usuarios), se agrega `imageUrl` a la entidad y el
  `src` del `<img>` pasa a `${plan.imageUrl}` con el cover como fallback. Los SVGs son de la
  paleta de marca (`resources/images/plans/cover-{1..6}.svg`).

## Referencia de Implementacion

> Los pasos a continuacion son guia de implementacion, no reemplazan los criterios de aceptacion de arriba.

### 1. Query de planes publicos

File: `src/main/java/com/valhalla/infrastructure/plan/JpaPlanRepository.java`

```java
List<Plan> findByIsPublicTrue();
```

File: `src/main/java/com/valhalla/domain/plan/PlanRepository.java` (interface de dominio)

```java
List<Plan> findByIsPublicTrue();
```

File: `src/main/java/com/valhalla/infrastructure/plan/PlanRepositoryImpl.java`

```java
@Override
public List<Plan> findByIsPublicTrue() {
  return jpaPlanRepository.findByIsPublicTrue();
}
```

El nombre derived-query lee el atributo `isPublic` del entity (el campo, no el getter). Si Spring
Data no lo resuelve, se explicita en `JpaPlanRepository`:

```java
@Query("select p from Plan p where p.isPublic = true")
List<Plan> findByIsPublicTrue();
```

### 2. Servicio

File: `src/main/java/com/valhalla/domain/plan/PlanService.java` (interface)

```java
List<Plan> getPublicPlans();
```

File: `src/main/java/com/valhalla/infrastructure/plan/PlanServiceImpl.java`

```java
@Override
@Transactional(readOnly = true)
public List<Plan> getPublicPlans() {
  return planRepository.findByIsPublicTrue();
}
```

### 3. Endpoint

File: `src/main/java/com/valhalla/presentation/plan/ForYouController.java`

El feed vive en un controller propio (`ForYouController`) porque `PlanController` esta amarrado a
`@RequestMapping("/plans")` y la ruta del feed es de primer nivel:

```java
@Controller
public class ForYouController {

  @GetMapping("/for-you")
  public ModelAndView forYou(Authentication authentication) {
    String currentEmail = authentication == null ? null : authentication.getName();
    Map<String, Object> model = new ModelMap();
    List<Map<String, Object>> cards = planService
      .getPublicPlans()
      .stream()
      .filter(plan -> currentEmail == null || !plan.isAdministrator(currentEmail))
      .map(ForYouController::planCard)
      .toList();
    model.put("publicPlans", cards);
    return new ModelAndView("pages/plans/public-list", model);
  }

  /** Solo campos publicos: ni shortCode, ni emails, ni participantes. */
  private static Map<String, Object> planCard(Plan plan) {
    Map<String, Object> card = new LinkedHashMap<>();
    card.put("id", plan.getId());
    card.put("name", plan.getName());
    card.put("description", plan.getDescription());
    card.put("eventDate",
        plan.getEventDate() == null ? null : plan.getEventDate().toString());
    card.put("placeCount", plan.getPlanPlaces().size());
    card.put("administratorName", displayName(plan.getAdministrator()));
    return card;
  }
}
```

El filtro de planes propios solo aplica cuando hay sesion (AC-05/AC-10); sin sesion el principal es
un `AnonymousAuthenticationToken` ("anonymousUser") o null y no hay planes propios que filtrar.

`displayName(User)` es el helper definido en `13-PVP.md` (paso 3): si se implementan las dos specs
en paralelo, se agrega una sola vez y se comparte.

### 4. Template

File: `src/main/webapp/WEB-INF/templates/pages/plans/public-list.html` (crear)

> **Ronda 2:** el template final es el de `public-list.html` en el repo: tarjetas `article` con
> cover (`<img th:src="${plan.cover}">`), boton "Clonar Plan" (form a `POST /plans/{id}/clone`),
> boton "+ Crear plan" y estado vacio hacia `/explore`. El snippet de abajo es
> el punto de partida original.

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{layouts/base :: head('Planes publicos')}"></head>
<body th:replace="~{layouts/base :: layout(~{::main})}">

<main class="container mx-auto px-4 py-8">
  <h1 class="text-3xl font-bold">Planes publicos</h1>
  <p class="text-gray-600 mt-2">Planes que alguien decidio compartir con todo el mundo.</p>

  <div th:if="${!publicPlans.isEmpty()}" class="grid gap-4 mt-8 sm:grid-cols-2 lg:grid-cols-3">
    <a th:each="plan : ${publicPlans}"
       th:href="@{/plans/{id}/public(id=${plan.id})}"
       class="block bg-white rounded-lg shadow p-6 hover:shadow-md transition">
      <h2 class="text-xl font-bold" th:text="${plan.name}">Plan</h2>
      <p class="text-gray-600 mt-1 line-clamp-3" th:text="${plan.description}"></p>
      <p class="text-sm text-gray-500 mt-2" th:if="${plan.eventDate != null}">
        <span th:text="${plan.eventDate}"></span>
      </p>
      <p class="text-sm text-gray-500" th:if="${plan.placeCount > 0}">
        <span th:text="${plan.placeCount}">0</span> lugares
      </p>
      <p class="text-sm text-gray-500" th:if="${!#strings.isEmpty(plan.administratorName)}">
        Por <span th:text="${plan.administratorName}"></span>
      </p>
    </a>
  </div>

  <div th:if="${publicPlans.isEmpty()}" class="text-center py-16 text-gray-500">
    <p class="text-lg mb-4">Todavia no hay planes publicos.</p>
    <a th:href="@{/auth/register}" class="bg-primary text-white px-6 py-3 rounded-md font-medium inline-block">
      Crea el primero
    </a>
  </div>
</main>
</body>
</html>
```

### 5. Link en el navbar

File: `src/main/webapp/WEB-INF/templates/components/navbar.html`

En **ambos** bloques (anonimo y autenticado), con el mismo label "Para vos": el nav es flex de dos
hijos (marca + menu), asi que el link se duplica en los dos bloques en vez de colgarse suelto del
`nav`. Fuera del bloque admin (`th:unless="${showAdminNav}"`):

```html
<a th:href="@{/for-you}" class="...">Para vos</a>
```

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `domain/plan/PlanRepository.java` | Actualizar (`findByIsPublicTrue()`) |
| `infrastructure/plan/JpaPlanRepository.java` | Actualizar (derived query o `@Query`) |
| `infrastructure/plan/PlanRepositoryImpl.java` | Actualizar (delegacion) |
| `domain/plan/PlanService.java` | Actualizar (`getPublicPlans()`) |
| `infrastructure/plan/PlanServiceImpl.java` | Actualizar (implementacion `readOnly`) |
| `presentation/plan/ForYouController.java` | Crear (`GET /for-you` + `planCard` con `cover`, filtrando planes propios solo con sesion) |
| `presentation/plan/PlanController.java` | Actualizar (quitar `@GetMapping("/public")`, `planCard` y `feedDisplayName`: se mudaron a `ForYouController`) |
| `presentation/shared/LoginRedirects.java` | Actualizar (`USER_LANDING = "/for-you"`) |
| `templates/pages/plans/public-list.html` | Crear (grid de tarjetas + cover + estado vacio + CTA de clonar con/sin sesion) |
| `templates/components/navbar.html` | Actualizar (link "Para vos" en ambos bloques, salvo admin) |
| `resources/images/plans/cover-{1..6}.svg` | Crear (covers genericos de marca) |
| `presentation/plan/ForYouControllerTest.java` | Crear (U-01..U-04, migrados de `PlanControllerTest`) |
| `infrastructure/plan/PlanServiceImplTest.java` | Actualizar (U-05..U-07) |
| `integration/PlanControllerIntegrationTest.java` | Actualizar (I-01..I-06) |
| `integration/PlanPublicSecurityTest.java` | Crear/actualizar (S-01..S-03) |
| `e2e/PublicPlansListE2E.java` | Crear (E-01) |
