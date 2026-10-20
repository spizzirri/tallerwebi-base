# [SEARCH] Busqueda, ordenado y filtrado de planes

> Trello: https://trello.com/c/3LT2OLIr/35-search-busqueda-ordenado-y-filtrado-de-planes
> **Estimacion:** 5 pts Fibonacci
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.

## Objetivo

Un visitante de `GET /for-you` puede **buscar** planes publicos por texto, **filtrarlos** por
categoria de lugar y por fecha del evento, y **ordenarlos** por popularidad, fecha o nombre. La
pagina sigue siendo server-side: el formulario hace `GET` a la misma ruta y la URL resultante es
compartible.

Esta card **no define** el criterio de popularidad: lo consume de [TOP] (`17-TOP.md`). Los filtros
se aplican antes de ordenar, sobre el mismo conjunto publico que ya usa el feed.

## Pre-requisitos

- [PPV] completed (`GET /for-you`, `PlanFeedCards`, filtro de planes propios)
- [TOP] completed (`PlanDiscovery.popularityOrder`, conteo de participantes)

## Decisiones / aclaraciones

- **Texto**: substring case-insensitive sobre `name` y `description`. Sin `unaccent`: "cafe" no
  encuentra "Café" en una base con tildes. Es una limitacion conocida y documentada, no un bug:
  agregar `unaccent` o una columna normalizada es una card aparte.
- **Categoria**: el plan no tiene categoria propia; matchea si **al menos uno** de los lugares de
  su itinerario (`PlanPlace.place.category`) pertenece a la categoria pedida. Es el filtro que
  hace util el feature ("planes con bares"), no un adorno.
- **La categoria se resuelve en memoria.** `planPlaces` es `@OneToMany(EAGER)` y el feed ya lo
  recorre para `placeCount`: filtrar no agrega una query. Los participantes son el caso opuesto
  (LAZY, ver [TOP]) y por eso van por query.
- **Fecha**: `upcoming` incluye hoy (`eventDate >= hoy`), `past` lo excluye (`eventDate < hoy`).
  Un plan **sin fecha** no pertenece a ninguna de las dos ventanas: si el filtro esta activo, no
  aparece. Sin filtro (`all`) si aparece.
- **Orden**: `popular` es el default y el mismo comparador de [TOP]. `date` ordena por fecha
  ascendente con nulos al final; `name` por nombre case-insensitive. Todo cierra con id para que
  el orden sea determinista.
- **Parametros invalidos no rompen la pagina**: una categoria desconocida se ignora, un `when` o
  `sort` invalido cae al default. No hay 400 por query string.
- **El formulario es GET**: sin JS. Se envian `q`, `category`, `when` y `sort`; los valores
  aplicados vuelven en el modelo para que los controles queden seleccionados y el link de
  "Limpiar filtros" sea `/for-you` a secas.
- **Dos estados vacios distintos**: "no encontramos planes con esos filtros" (con salida) no es lo
  mismo que "todavia no hay planes publicos" (el estado historico de [PPV]).
- **Sin busqueda por administrador**: expone datos de terceros y no aporta al descubrimiento. Sin
  recomendacion personalizada: no existe senal de preferencias y no se va a inventar.

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | `q` filtra por substring case-insensitive sobre el nombre o la descripcion del plan |
| AC-02 | `q` vacio o compuesto solo por espacios no filtra nada |
| AC-03 | `category` filtra los planes que contienen al menos un lugar de esa categoria en su itinerario; vacio no filtra |
| AC-04 | `when=upcoming` incluye los planes con `eventDate >= hoy`; `when=past` los de `eventDate < hoy`; `when=all` o vacio no filtra; un plan sin fecha no aparece con `upcoming` ni con `past` |
| AC-05 | `sort=popular` (default) usa el orden de [TOP]; `sort=date` ordena por fecha ascendente con nulos al final; `sort=name` por nombre case-insensitive; un valor desconocido cae a `popular` sin error |
| AC-06 | Los filtros se combinan con AND: texto, categoria y fecha se aplican todos a la vez |
| AC-07 | Ningun filtro agrega planes: el conjunto visible sigue siendo solo publico y nunca incluye los planes propios del logueado |
| AC-08 | El formulario refleja los valores aplicados y la URL resultante se puede compartir (todo viaja en el query string, sin estado de sesion) |
| AC-09 | Si hay filtros activos y no hay resultados, la pagina muestra un mensaje propio con accion para limpiar los filtros; si no hay filtros, muestra el estado vacio de [PPV] |
| AC-10 | La pagina no expone `shortCode`, emails ni identidades de participantes, con o sin filtros |
| AC-11 | El orden `popular` es exactamente el de [TOP]: no existe un segundo algoritmo de popularidad |
| AC-12 | Una categoria, `when` o `sort` invalidos no rompen la pagina: se ignoran o caen al default |
| AC-13 | Con filtros activos o un orden distinto de `popular`, el formulario ofrece `Limpiar`, que vuelve a `/for-you` sin query string |

## Escenarios de Test

### Tests Unitarios (`domain/plan/PlanDiscoveryTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | `q` matchea por nombre, sin distinguir mayusculas | AC-01 |
| U-02 | `q` matchea por descripcion | AC-01 |
| U-03 | `q` sin coincidencias devuelve lista vacia | AC-01 |
| U-04 | `q` blank o con solo espacios no filtra | AC-02 |
| U-05 | Un plan con descripcion `null` no rompe la busqueda por texto | AC-01 |
| U-06 | `category=BAR` devuelve solo planes con al menos un lugar BAR | AC-03 |
| U-07 | Un plan sin lugares no matchea ninguna categoria | AC-03 |
| U-08 | `upcoming` incluye la fecha de hoy y excluye las pasadas | AC-04 |
| U-09 | `past` incluye solo fechas anteriores a hoy | AC-04 |
| U-10 | Con `upcoming` o `past`, un plan sin fecha no aparece; con `all`, si | AC-04 |
| U-11 | Texto, categoria y fecha juntos se combinan con AND | AC-06 |
| U-12 | `sort=date` ordena ascendente con nulos al final | AC-05 |
| U-13 | `sort=name` ordena case-insensitive | AC-05 |

### Tests Unitarios (`domain/plan/PlanDiscoveryQueryTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-14 | `sort` desconocido cae a `popular` | AC-05, AC-12 |
| U-15 | `category` desconocida se ignora (no filtra) | AC-12 |
| U-16 | `when` desconocido cae a `all` | AC-12 |
| U-17 | `hasFilters()` es falso con texto blank, sin categoria y `when=all` | AC-09 |
| U-18 | `hasFilters()` es verdadero con cualquiera de los tres filtros activo | AC-09 |

### Tests Unitarios (`presentation/plan/ForYouControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-19 | El controller pasa `q`, `category`, `when` y `sort` al descubrimiento y devuelve el listado filtrado | AC-01, AC-03, AC-04, AC-05 |
| U-20 | El modelo expone los valores aplicados (`q`, categoria, `when`, `sort`) y `filtersApplied` | AC-08, AC-09 |
| U-21 | Parametros `null` o invalidos no lanzan y dejan el listado sin filtros | AC-12 |
| U-22 | Con filtros y sin resultados el modelo marca `filtersApplied=true` y la lista vacia | AC-09 |

### Tests de Integracion (`integration/PlanControllerIntegrationTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-01 | `GET /for-you?q=museo` muestra solo los planes que matchean | AC-01 |
| I-02 | `GET /for-you?category=BAR` muestra solo planes con un lugar BAR | AC-03 |
| I-03 | `GET /for-you?when=past` muestra solo planes con fecha pasada | AC-04 |
| I-04 | `GET /for-you?sort=date` respeta el orden por fecha | AC-05 |
| I-05 | `GET /for-you?q=zzz` responde 200 con el estado "sin resultados" y el link para limpiar | AC-09 |
| I-06 | `GET /for-you?category=NOPE&sort=nope` responde 200 sin error (parametros invalidos) | AC-12 |
| I-07 | El HTML del listado filtrado no contiene `shortCode`, emails ni participantes | AC-10 |
| I-08 | `GET /for-you?q=zzz` incluye el link `Limpiar` del formulario; `GET /for-you` no lo incluye | AC-13 |

### E2E (minimos)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `PublicPlansSearchE2E` | Escribir un texto, ver los resultados filtrados, cambiar el orden y limpiar los filtros volviendo al feed completo | AC-01, AC-06, AC-08, AC-09 |

## Referencia de Implementacion

> Los pasos a continuacion son guia de implementacion, no reemplazan los criterios de aceptacion.

### 1. Query de descubrimiento

File: `src/main/java/com/valhalla/domain/plan/PlanDiscoveryQuery.java` (crear)

```java
package com.valhalla.domain.plan;

import com.valhalla.domain.place.PlaceCategory;
import java.util.Locale;

/** [SEARCH] Filtros y orden del feed, ya normalizados. Los parametros crudos no llegan al dominio. */
public record PlanDiscoveryQuery(String text, PlaceCategory category, When when, Sort sort) {

  public enum When {
    ALL,
    UPCOMING,
    PAST,
  }

  public enum Sort {
    POPULAR,
    DATE,
    NAME,
  }

  /** Parseo tolerante: parametro vacio o invalido cae al default, nunca lanza. */
  public static PlanDiscoveryQuery parse(
    String text,
    String category,
    String when,
    String sort
  ) {
    return new PlanDiscoveryQuery(
      text == null ? "" : text.trim(),
      parseCategory(category),
      parseWhen(when),
      parseSort(sort)
    );
  }

  /** Hay filtro activo (el orden no cuenta como filtro): decide el estado vacio de AC-09. */
  public boolean hasFilters() {
    return !text.isEmpty() || category != null || when != When.ALL;
  }

  private static PlaceCategory parseCategory(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    return PlaceCategory.fromCode(raw.trim().toUpperCase(Locale.ROOT)).orElse(null);
  }

  private static When parseWhen(String raw) {
    if (raw == null || raw.isBlank()) {
      return When.ALL;
    }
    return switch (raw.trim().toLowerCase(Locale.ROOT)) {
      case "upcoming" -> When.UPCOMING;
      case "past" -> When.PAST;
      default -> When.ALL;
    };
  }

  private static Sort parseSort(String raw) {
    if (raw == null || raw.isBlank()) {
      return Sort.POPULAR;
    }
    return switch (raw.trim().toLowerCase(Locale.ROOT)) {
      case "date" -> Sort.DATE;
      case "name" -> Sort.NAME;
      default -> Sort.POPULAR;
    };
  }
}
```

`PlaceCategory.fromCode()` ya existe en el enum: se reutiliza en vez de repetir el `try/catch`.

### 2. Filtros y orden en `PlanDiscovery`

File: `src/main/java/com/valhalla/domain/plan/PlanDiscovery.java` (extender [TOP])

```java
public static List<Plan> discover(
  List<Plan> plans,
  Map<UUID, RatingSummary> summaries,
  Map<UUID, Long> participantCounts,
  PlanDiscoveryQuery query,
  LocalDate today
) {
  return plans
    .stream()
    .filter(plan -> matchesText(plan, query.text()))
    .filter(plan -> matchesCategory(plan, query.category()))
    .filter(plan -> matchesWhen(plan, query.when(), today))
    .sorted(orderFor(query.sort(), summaries, participantCounts))
    .toList();
}

private static Comparator<Plan> orderFor(
  PlanDiscoveryQuery.Sort sort,
  Map<UUID, RatingSummary> summaries,
  Map<UUID, Long> participantCounts
) {
  return switch (sort) {
    case DATE -> Comparator
      .comparing((Plan plan) -> plan.getEventDate(), Comparator.nullsLast(Comparator.naturalOrder()))
      .thenComparing(plan -> normalize(plan.getName()))
      .thenComparing(Plan::getId);
    case NAME -> Comparator
      .comparing((Plan plan) -> normalize(plan.getName()))
      .thenComparing((Plan plan) -> plan.getEventDate(), Comparator.nullsLast(Comparator.naturalOrder()))
      .thenComparing(Plan::getId);
    case POPULAR -> popularityOrder(summaries, participantCounts);
  };
}

private static boolean matchesText(Plan plan, String text) {
  if (text.isEmpty()) {
    return true;
  }
  String needle = text.toLowerCase(Locale.ROOT);
  return (
    normalize(plan.getName()).contains(needle) ||
    normalize(plan.getDescription()).contains(needle)
  );
}

private static boolean matchesCategory(Plan plan, PlaceCategory category) {
  return (
    category == null ||
    plan
      .getPlanPlaces()
      .stream()
      .anyMatch(entry -> entry.getPlace() != null && entry.getPlace().getCategory() == category)
  );
}

private static boolean matchesWhen(Plan plan, PlanDiscoveryQuery.When when, LocalDate today) {
  return switch (when) {
    case ALL -> true;
    case UPCOMING -> plan.getEventDate() != null && !plan.getEventDate().isBefore(today);
    case PAST -> plan.getEventDate() != null && plan.getEventDate().isBefore(today);
  };
}
```

`today` entra por parametro para que el filtro sea testeable sin congelar el reloj.

Imports nuevos en `PlanDiscovery`: `java.time.LocalDate`, `java.util.List` (ya presente por [TOP])
y `com.valhalla.domain.place.PlaceCategory`.

### 3. Controller

File: `src/main/java/com/valhalla/presentation/plan/ForYouController.java`

```java
@GetMapping("/for-you")
public ModelAndView forYou(
  Authentication authentication,
  @RequestParam(required = false) String q,
  @RequestParam(required = false) String category,
  @RequestParam(required = false) String when,
  @RequestParam(required = false) String sort
) {
  String currentEmail = authentication == null ? null : authentication.getName();
  PlanDiscoveryQuery query = PlanDiscoveryQuery.parse(q, category, when, sort);
  List<Plan> publicPlans = planService
    .getPublicPlans()
    .stream()
    .filter(plan -> currentEmail == null || !plan.isAdministrator(currentEmail))
    .toList();
  Map<UUID, RatingSummary> summaries = this.planRatingService.getSummariesByPlanId();
  Map<UUID, Long> participantCounts = this.planService.getParticipantCounts(
    publicPlans.stream().map(Plan::getId).toList()
  );
  Map<String, Object> model = new ModelMap();
  model.put(
    ATTR_PUBLIC_PLANS,
    PlanDiscovery
      .discover(publicPlans, summaries, participantCounts, query, LocalDate.now())
      .stream()
      .map(plan -> PlanFeedCards.planCard(plan, summaries))
      .toList()
  );
  model.put("q", query.text());
  model.put("selectedCategory", query.category() == null ? "" : query.category().name());
  model.put("selectedWhen", query.when().name().toLowerCase(Locale.ROOT));
  model.put("selectedSort", query.sort().name().toLowerCase(Locale.ROOT));
  model.put("filtersApplied", query.hasFilters());
  model.put("categories", PlaceCategory.values());
  return new ModelAndView(VIEW_PUBLIC_LIST, model);
}
```

`categories` se pasa desde el controller en vez de resolver el enum en el template: el template
solo itera, no importa clases del dominio.

### 4. Template

File: `src/main/webapp/WEB-INF/templates/pages/plans/public-list.html`

```html
<form method="get" th:action="@{/for-you}" class="mb-6 flex flex-wrap items-end gap-3">
  <label class="flex flex-1 flex-col text-sm text-gray-600">
    Buscar
    <input type="search" name="q" th:value="${q}" placeholder="Nombre o descripcion"
      class="mt-1 rounded-md border border-gray-300 px-3 py-2" />
  </label>

  <label class="flex flex-col text-sm text-gray-600">
    Categoria
    <select name="category" class="mt-1 rounded-md border border-gray-300 px-3 py-2">
      <option value="">Todas</option>
      <option th:each="category : ${categories}"
        th:value="${category.name()}" th:text="${category.name()}"
        th:selected="${category.name() == selectedCategory}"></option>
    </select>
  </label>

  <label class="flex flex-col text-sm text-gray-600">
    Fecha
    <select name="when" class="mt-1 rounded-md border border-gray-300 px-3 py-2">
      <option value="all" th:selected="${selectedWhen == 'all'}">Cualquier fecha</option>
      <option value="upcoming" th:selected="${selectedWhen == 'upcoming'}">Proximos</option>
      <option value="past" th:selected="${selectedWhen == 'past'}">Pasados</option>
    </select>
  </label>

  <label class="flex flex-col text-sm text-gray-600">
    Ordenar por
    <select name="sort" class="mt-1 rounded-md border border-gray-300 px-3 py-2">
      <option value="popular" th:selected="${selectedSort == 'popular'}">Populares</option>
      <option value="date" th:selected="${selectedSort == 'date'}">Fecha</option>
      <option value="name" th:selected="${selectedSort == 'name'}">Nombre</option>
    </select>
  </label>

  <button type="submit" class="rounded-md bg-primary px-4 py-2 font-medium text-white">
    Buscar
  </button>
</form>

<div th:if="${publicPlans.isEmpty() and filtersApplied}" class="rounded-md bg-white p-10 text-center shadow-md">
  <p class="mb-4 text-lg text-gray-600">No encontramos planes con esos filtros.</p>
  <a th:href="@{/for-you}" class="inline-block rounded-md bg-primary px-6 py-3 font-medium text-white">
    Limpiar filtros
  </a>
</div>
```

El estado vacio historico de [PPV] queda con la condicion complementaria
`th:if="${publicPlans.isEmpty() and !filtersApplied}"`.

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `domain/plan/PlanDiscoveryQuery.java` | Crear (parseo tolerante + `hasFilters`) |
| `domain/plan/PlanDiscovery.java` | Actualizar (`discover`, filtros y `orderFor`) |
| `presentation/plan/ForYouController.java` | Actualizar (query params, modelo, categorias) |
| `templates/pages/plans/public-list.html` | Actualizar (formulario GET, estados vacios, controles sticky) |
| `presentation/plan/ForYouControllerTest.java` | Actualizar (U-19..U-22) |
| `integration/PlanControllerIntegrationTest.java` | Actualizar (I-01..I-08) |
| `domain/plan/PlanDiscoveryTest.java` | Actualizar (U-01..U-13) |
| `domain/plan/PlanDiscoveryQueryTest.java` | Crear (U-14..U-18) |
| `e2e/PublicPlansSearchE2E.java` | Crear (E-01) |
| `docs/specs/14-PPV.md` | Actualizar (el formulario cambia la cabecera del feed) |
| `odd/tasks/plan-discovery-ranking-search.md` | Actualizar (progreso y hashes) |
