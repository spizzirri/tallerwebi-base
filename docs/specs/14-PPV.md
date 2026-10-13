# [PPV] Listado de planes publicos

> Trello: https://trello.com/c/2IQRizj1/20-ppv-vista-de-planes-publicos-mas-vistos
> **Estimacion:** 3 pts Fibonacci
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.

## Objetivo

Un visitante puede recorrer todos los planes publicos de la plataforma en
`GET /plans/public` y entrar desde cada tarjeta a su vista publica
(`/plans/{id}/public`, ver [PVP]).

Hoy no existe forma de descubrir un plan ajeno: `GET /plans` exige sesion y devuelve solo los
planes propios (`getPlansByUserEmail`) mas los de los que el usuario participa
(`getParticipantPlans`). Un plan con `isPublic = true` es inalcanzable salvo que alguien tenga el
link exacto.

> El nombre de la card es "planes publicos **mas vistos**". El equipo decidio que todavia no hay
> un criterio para mostrar un plan por sobre otro: por ahora se muestran **todos** los planes
> publicos, sin orden garantizado. El ranking queda pendiente de definicion (ver Notas).

## Pre-requisitos

- [PLN] completed (`Plan.isPublic`, `administrator`, `planPlaces`)
- [AUT] completed
- [PVP] es el destino de cada tarjeta: sin su ruta `/plans/{id}/public` el listado no tiene a donde
  linkear. Si [PVP] entra despues, el link de la tarjeta puede quedar apuntando a la vista publica
  aun no existente.

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | `GET /plans/public` sin sesion responde 200 con el listado completo de planes `isPublic = true` |
| AC-02 | Ningun plan privado aparece en el listado: ni nombre, ni datos, ni cantidad |
| AC-03 | Cada tarjeta muestra nombre, descripcion, fecha del evento, cantidad de lugares del itinerario y nombre del administrador |
| AC-04 | Cada tarjeta linkea a la vista publica del plan (`/plans/{id}/public`) |
| AC-05 | Se muestra el conjunto completo sin criterio de orden: no se filtra, no se pagina, no se prioriza ningun plan |
| AC-06 | Sin planes publicos, la pagina muestra un estado vacio amigable con link al registro (`/auth/register`) |
| AC-07 | El navbar tiene link "Planes publicos" a `/plans/public`, visible tambien sin sesion |
| AC-08 | `GET /plans/public` es `permitAll`; el resto de `/plans/**` sigue exigiendo sesion |
| AC-09 | El listado expone solo campos publicos: ni `shortCode`, ni emails, ni ids de participantes |
| AC-10 | Con sesion la pagina tambien funciona igual (no cambia segun el estado de sesion) |

## Escenarios de Test

### Tests Unitarios (`presentation/plan/PlanControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | `publicPlans()` retorna la vista `pages/plans/public-list` con todos los planes publicos | AC-01 |
| U-02 | `publicPlans()` arma las tarjetas con nombre, descripcion, fecha, cantidad de lugares, administrador, `own` (si es del logueado) y `cover` (imagen generica) | AC-03 (decisiones en Notas) |
| U-03 | `publicPlans()` con la base vacia deja la lista vacia (sin error) | AC-06 |
| U-04 | `publicPlans()` no pone `shortCode` ni emails de participantes en el model (claves exactas, incluye `cover`) | AC-09 |

### Tests Unitarios (`infrastructure/plan/PlanServiceImplTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-05 | `getPublicPlans()` retorna solo los planes con `isPublic = true` | AC-01, AC-02 |
| U-06 | `getPublicPlans()` con ninguna base publica retorna lista vacia | AC-06 |
| U-07 | `getPublicPlans()` no llama a `save` ni modifica planes | AC-05 |

### Tests de Integracion (`integration/PlanControllerIntegrationTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-01 | `GET /plans/public` anonimo → 302 a `/auth/login` (feed solo con sesion, ver Notas) | AC-07/AC-08 vacantes |
| I-01b | `GET /plans/public` con sesion → 200 con el nombre de un plan publico | AC-01 |
| I-02 | `GET /plans/public` no contiene el nombre de ningun plan privado | AC-02 |
| I-03 | `GET /plans/public` con cero planes publicos → 200 con el estado vacio | AC-06 |
| I-04 | Cada tarjeta del listado linkea a `/plans/{id}/public` | AC-04 |
| I-05 | "Editar" aparece solo en los planes propios del usuario logueado | decision "Editar" |
| I-06 | Cada tarjeta muestra su cover visual (`/images/plans/cover-N.svg`, `alt` = nombre) y el SVG se sirve | decision "cover" |

### Tests de Seguridad (`integration/PlanPublicSecurityTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| S-01 | `GET /plans/public` anonimo → 302 a `/auth/login` (matcher exige sesion) | AC-08 vacante |
| S-01b | `GET /plans/public` con sesion → 200 | AC-01 |
| S-02 | `POST /plans/public` anonimo → redirige a login | AC-08 |
| S-03 | El listado no filtra informacion privada de los planes (emails, shortCode, participantes) | AC-09 |

### E2E (minimos)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `PublicPlansListE2E` (T-PPV-005) | Sin sesion: el navbar no ofrece el feed y la ruta cae en login. Con sesion: tarjeta con cover que carga, "Usar plan" clona y "Editar" abre el propio | AC-01, AC-03, AC-04, AC-09 |

## Notas / decisiones de diseño

- **Sin ranking.** La card habla de "mas vistos"; no existe `viewCount` en `Plan` y el equipo
  decidio no inventar un criterio. `findByIsPublicTrue()` no lleva `ORDER BY`: si dos
  implementaciones devuelven distinto orden, ninguna es incorrecta. Cuando haya criterio (vistas,
  fecha de creacion, relevancia) se cambia la query, no la spec.
- **Pagina propia, no seccion del landing.** Se decidio una ruta propia (`/plans/public`) en vez
  de un bloque en `pages/landing.html` o sumarlo a `/explore` (que es de lugares): el landing es
  contenido editorial y `/explore` ya tiene su propio modelo Vue. Si el equipo prefiere otra
  ubicacion, solo cambia el paso 4 (controller + template) y el AC-07.
- **Ruta literal vs `{id}`.** `GET /plans/public` y `GET /plans/{id}` conviven: Spring resuelve el
  patron literal primero y `public` nunca llega a parsearse como `Long`. El guard real es I-01 y
  S-01: si alguien renombra el mapping, `/plans/public` pasaria a caer en `/{id}` y
  `MethodArgumentTypeMismatchException` responderia 404 (ver `GlobalExceptionHandler`).
- **Tarjetas server-side.** No se agrega endpoint REST nuevo ni Vue: el listado se renderiza con
  Thymeleaf como `pages/plans/list.html`. No hay paginacion porque el volumen de un TP no la
  justifica; si crece, se agrega con `?page=` sin tocar los ACs de visibilidad.
- **Feed solo con sesion (decision de producto posterior a la spec).** El AC-01/AC-07/AC-08
  originales preveian acceso anonimo; el equipo decidio que el feed es el home del usuario
  logueado, tipo muro de Instagram. Consecuencias: el link "Planes públicos" no aparece en el
  navbar del landing, `/plans/public` redirige a `/auth/login` sin sesion, y login y registro
  aterrizan ahi (`LoginRedirects.USER_LANDING = "/plans/public"`). Los ACs de acceso anonimo
  quedan vacantes hasta que el equipo diga lo contrario.
- **"Usar plan" = clonar (el endpoint es de [CLO]).** El boton de la tarjeta ejecuta
  `POST /plans/{id}/clone`, que pertenece a [CLO]: lo implementa `PlanController` y sus pruebas de
  seguridad son `integration/PlanCloneSecurityTest.java` (ver "Ownership del endpoint" en
  15-CLO.md). Este card solo consume ese endpoint, no duplica la logica, y deja al usuario en su
  copia editable; las tarjetas de planes propios muestran ademas "Editar" (via el detalle). La
  vista `/plans/{id}/public` de [PVP] ya existe y es el destino de cada tarjeta.
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

File: `src/main/java/com/valhalla/presentation/plan/PlanController.java`

El mapping se puede declarar en `PlanController` (arriba de `@GetMapping("/{id}")`, junto a los
demas) o en un `PublicPlanController` separado; esta referencia lo deja en `PlanController` para
no duplicar la dependencia de `PlanService`:

```java
@GetMapping("/public")
public ModelAndView publicPlans() {
  Map<String, Object> model = new ModelMap();
  List<Map<String, Object>> cards = planService
    .getPublicPlans()
    .stream()
    .map(PlanController::planCard)
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
```

`displayName(User)` es el helper definido en `13-PVP.md` (paso 3): si se implementan las dos specs
en paralelo, se agrega una sola vez y se comparte.

### 4. Template

File: `src/main/webapp/WEB-INF/templates/pages/plans/public-list.html` (crear)

> **Ronda 2:** el template final es el de `public-list.html` en el repo: tarjetas `article` con
> cover (`<img th:src="${plan.cover}">`), boton "Usar plan" (form a `POST /plans/{id}/clone`),
> "Editar" si `own`, boton "+ Crear plan" y estado vacio hacia `/explore`. El snippet de abajo es
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

Dentro del bloque de sesion iniciada (`sec:authorize="isAuthenticated()"`), **no** en el navbar
anonimo (decision de producto, ver Notas: sin sesion el link no existe y la ruta manda a login):

```html
<a th:href="@{/plans/public}" class="...">Planes publicos</a>
```

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `domain/plan/PlanRepository.java` | Actualizar (`findByIsPublicTrue()`) |
| `infrastructure/plan/JpaPlanRepository.java` | Actualizar (derived query o `@Query`) |
| `infrastructure/plan/PlanRepositoryImpl.java` | Actualizar (delegacion) |
| `domain/plan/PlanService.java` | Actualizar (`getPublicPlans()`) |
| `infrastructure/plan/PlanServiceImpl.java` | Actualizar (implementacion `readOnly`) |
| `presentation/plan/PlanController.java` | Actualizar (`@GetMapping("/public")` + `planCard` con `own` y `cover`) |
| `templates/pages/plans/public-list.html` | Crear (grid de tarjetas + cover + estado vacio) |
| `templates/components/navbar.html` | Actualizar (link solo autenticado) |
| `resources/images/plans/cover-{1..6}.svg` | Crear (covers genericos de marca) |
| `presentation/plan/PlanControllerTest.java` | Actualizar (U-01..U-04) |
| `infrastructure/plan/PlanServiceImplTest.java` | Actualizar (U-05..U-07) |
| `integration/PlanControllerIntegrationTest.java` | Actualizar (I-01..I-06) |
| `integration/PlanPublicSecurityTest.java` | Crear/actualizar (S-01..S-03) |
| `e2e/PublicPlansListE2E.java` | Crear (E-01) |
