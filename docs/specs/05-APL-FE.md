# [APL-FE] Agregar Lugares al Plan (Frontend)

> Trello: https://trello.com/c/K7XqvGWZ/9-apl-fe-agregar-lugares-al-plan-frontend
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.
> **Actualización 24-SCH (2026-10-25):** el horario ya no se tipea por parada. El wizard
> pide la ventana del día (fecha, inicio, fin de día y modo de traslado) y las horas se
> derivan; el itinerario agrupa por día y la edición vive en el diálogo de la parada.

## Objetivo

Frontend para agregar lugares a planes: ficha de lugar de solo lectura en `/places`, itinerario con Vue.js, mapa con rutas y marcadores numerados en el detalle del plan. El agregado vive en el wizard de creación y en el detalle del plan.

## Pre-requisitos

- [APL-BE] completed (PlanPlace entity, REST endpoints)
- [PLC] completed (panel lateral de detalles del lugar dentro de `/places`)

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | El panel lateral de `/places` ofrece un picker de plan inline (select de TODOS los planes propios; los que ya contienen el lugar salen deshabilitados y marcados "(ya contiene el lugar)" + botón "Agregar al plan"), sin modal; el bloque se pinta con sesión (gate `isLogged`), y un usuario logueado sin planes ve un CTA "Crear un plan" que abre el wizard de la misma página en vez de nada; con el wizard de creación abierto el botón agrega el lugar al plan EN PROCESO (borrador, mismo destino que el "+" de las tarjetas) en lugar de ofrecer crear otro plan o listar planes guardados |
| AC-02 | Al hacer click en un lugar de `/explore` nunca se abre un modal de "agregar a plan": el agregado desde la ficha es inline, con feedback en el mismo panel (éxito o error); un plan que ya contiene el lugar no se puede re-agregar (opción deshabilitada, guard en el cliente porque el backend no rechaza duplicados) |
| AC-03 | Al agregar un lugar desde el wizard o desde el detalle del plan se muestra feedback visual (redirect o mensaje) |
| AC-04 | El detalle del plan (/plans/{id}) muestra el itinerario con los lugares en orden |
| AC-05 | El itinerario agrupa las paradas por día y cada una muestra numero de orden, nombre, categoria y su horario como chip con rango `HH:mm – HH:mm`, duración y fecha (la fecha solo aparece cuando difiere del Día 1) |
| AC-06 | Se puede editar duración, día y hora de visita desde el diálogo de la parada, y los minutos de traslado del tramo anterior desde ahí o desde el paso 2; fijar el día o la hora recalcula las paradas siguientes y las marca como fijadas a mano |
| AC-07 | Se puede eliminar un lugar del itinerario con boton X |
| AC-08 | El mapa muestra markers numerados por cada lugar del itinerario |
| AC-09 | El mapa dibuja una linea de ruta entre los lugares en orden |
| AC-10 | El mapa hace fitBounds para mostrar todos los markers |
| AC-11 | Los forms usan CSRF token para proteger POST/PUT/DELETE |
| AC-12 | El form de creación de plan pide fecha (Día 1), hora de inicio y modo de traslado (el fin de día es el default fijo 23:00, sin input); no pide hora por parada, el paso de lugares muestra el rango derivado de cada parada y permite fijar los minutos de traslado por tramo; la fila del tramo es una sola línea con el selector de modo y el input de minutos (sin rótulo propio ni hint), y el input muestra el default calculado como placeholder |
| AC-13 | Mientras el wizard de `/explore` tiene paradas en el borrador, el mapa dibuja el itinerario como overlay: marcadores numerados (círculo oscuro, estilo propio) con popup de paso, nombre, llegada, permanencia y traslado, más una polyline punteada entre ellas en orden; se repinta con cada cambio del borrador y no hace fitBounds |

## Escenarios de Test

### Tests Unitarios (Vue.js logic, si aplica)

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | El panel lateral de `/places` no contiene el botón "Add to plan" ni el modal; el picker inline existe con `id="plan-picker"` y `aria-label="Agregar al plan"`, tras `v-if="isLogged"` (flag server-side por `data-is-logged` en `#explore-app`); con el wizard abierto (`v-if="showCreatePlan"`) el mismo bloque expone un botón "Agregar al plan" que llama a `addSelectedToDraft` (lugar al borrador, sin select ni CTA), y logueado sin planes renderiza el CTA "Crear un plan"; las opciones deshabilitadas llevan el sufijo "(ya contiene el lugar)" | AC-01, AC-02 |

### Tests de Integracion (`presentation/plan/PlanControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-01 | `GET /plans/{id}` con sesion retorna 200 y vista con itinerary | AC-04 |
| I-02 | `GET /plans/{id}` sin sesion redirige a login | AC-04 |

### E2E (minimos)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `AddPlaceToPlanE2E` | Crear plan desde /explore, agregar un lugar vía API en la sesión del navegador y verificar el itinerario | AC-04, AC-05 |
| E-02 | `PlanPickerE2E` | Con sesión y un plan propio: el picker se pinta para un lugar que el plan ya contiene (opción deshabilitada y marcada), y para otro lugar el agregado inline funciona y la API confirma el itinerario | AC-01, AC-02 |

## Notas / decisiones de diseño

- **El "Add to plan" y su modal se quitaron de `/places`; el picker volvió inline**. No existe
  `pages/places/detail.html` ni ruta `/places/{id}`: la ficha vive como
  `<aside aria-label="Details for ...">` dentro de `/places` (nombre, categoría,
  dirección, descripción e imagen) y ahora cierra con un picker de plan: select de TODOS los
  planes propios (`availablePlans`, UUIDs comparados como string); el plan que ya contiene el
  lugar sale deshabilitado y marcado "(ya contiene el lugar)", porque el backend no rechaza
  duplicados (no hay 409) y la UI es la única guarda; el botón "Agregar al plan" hace
  `apiFetch` POST, sin modal y sin salir de la página. El gate del bloque es la sesión
  (`v-if="isLogged"`, flag server-side que viaja como `data-is-logged` en `#explore-app`):
  `/api/plans` devuelve `[]` tanto sin sesión como en un usuario sin planes, así que por el
  fetch solo no se distinguían y el usuario logueado sin planes no veía nada. Con sesión y
  cero planes el panel ofrece un CTA "Crear un plan" que abre el wizard de la misma página; con
  el wizard de creación ya abierto el botón agrega el lugar al plan en proceso (`addToDraft`,
  el mismo destino que el "+" de las tarjetas) en vez de ofrecer crear otro; sin sesión la
  ficha queda informativa. El agregado también sigue vivo en el wizard de creación de `/explore` (tarjeta
  "+" → borrador) y en el detalle del plan; el usuario no pierde el mapa, el filtro aplicado
  ni la posición del scroll.
- **El "+" "Create plan" del panel de planes manda al login sin sesión** (`th:if` server-side
  con `isLogged`): `/explore` es `permitAll` pero `POST /plans` exige sesión, así que abrir el
  wizard a un anónimo solo lo mandaría a chocar en el submit. El `<a>` lleva
  `@{/auth/login?redirect=/explore}` para volver al explorador tras autenticarse, el mismo
  patrón del CTA anónimo de la vista pública (T_PVP_007); con sesión sigue siendo el `<button>`
  que abre la creación.
- **`add-to-plan.js` queda como puente de eventos**: el click en la tarjeta "+" emite
  `add-to-plan` y `places-list.js` lo enruta al borrador del wizard o al plan activo. El camino
  que abría el modal (fetch de `/api/plans` + select) se reemplazó por el picker inline del
  panel de detalle.
- **CSRF (AC-11)**: APL-BE reduce el ignore list a las lecturas, así que el `POST` de agregado
  necesita el token. Un form de Thymeleaf lo trae con `th:name="${_csrf.parameterName}"`; si la
  llamada se hace con `fetch`, el token va en el header que indique `${_csrf.parameterName}`.
- **El horario no se tipea al crear** (24-SCH): el paso 1 del wizard pide fecha (Día 1), hora de
  inicio y modo de traslado (`WALK`/`BIKE`/`BUS`/`CAR`) —el fin de día es el default fijo 23:00,
  sin input—; el paso 2 muestra por parada
  el rango derivado y el tramo al siguiente con su selector de modo (opción "Según el plan") y un
  input de minutos de traslado (vacío = calculado, mínimo 1). La fila del tramo es una sola
  línea —selector de modo + input de minutos, sin rótulo propio ni hint— y el default calculado
  queda a la vista como placeholder del input. El diálogo "Ver más" del detalle no lo muestra porque
  `plans-detail.js` no tiene los minutos del tramo (no están en el itinerario del servidor): ahí
  el input sigue con placeholder "auto". El
  detalle de la programación vive en la spec 24-SCH (PR #42), no acá.
- **El itinerario agrupa por día y edita por diálogo**: los chips son de solo lectura (rango,
  duración, fecha solo si difiere del Día 1 y badge "Fijado" cuando el usuario fijó el horario).
  La edición de duración/día/hora está en el diálogo de la parada y manda un
  `PlanPlaceUpdateRequest` completo a `PUT /api/plans/{id}/places/{entryId}`, que recalcula la
  cadena de horarios. El diálogo también edita los minutos de traslado del tramo: vacío vuelve al
  cálculo, un valor se persiste como override.
- **El form "Editar plan" expone el modo del plan**: además de fecha y hora de inicio tiene
  `transportMode`, uno de los campos que usa el planificador para derivar el horario. El cierre
  de la ventana (`dayEndTime`) es el default fijo 23:00 y ya no tiene input.
- **El overlay del borrador vive solo en `/explore`** (AC-13): `places-list.js` dibuja
  marcadores numerados (círculo oscuro vía CSS propio en el style block, badge sin
  `data-category`) y una polyline punteada con las mismas opciones que la del detalle del plan
  (`#0d6efd`, `dashArray '6, 6'`), en `let` planos aparte de los marcadores de lugares. Un
  `watch(schedule)` las repinta ante cualquier cambio del borrador (paradas, fecha, hora, modo,
  minutos fijos) y no hay fitBounds. Los popups se construyen con nodos (guide-js R7). El E2E
  de conteo tarjeta↔marcador se ata a `[data-category]`, así el overlay no lo rompe.
- **El ABM de lugares expone la duración típica** (`defaultDurationMinutes`, campo
  "Duración típica (min)"): es el segundo eslabón de la cascada de duraciones, entre la parada
  y el default por categoría.

## Referencia de Implementacion

> Los pasos a continuación son guía de implementación, no reemplazan los acceptance criteria de arriba.
>
> **El código de ejemplo es anterior a la implementación actual**: el "Add to plan" con su modal
> se eliminó de `/places` —el picker de planes que se ve abajo volvió, pero inline en el panel de
> detalle, sin form server-side ni `sec:authorize`—, y desde 24-SCH el itinerario agrupa por día y
> renderiza chips de solo
> lectura, con la edición del horario en el diálogo de la parada (`plans-detail.js` →
> `updateEntry`, que manda los cuatro campos del horario de una vez). El detalle usa Bootstrap con
> el módulo JS, no el CDN de Vue de abajo.

### 1. Update the places panel (add "Add to plan" form)

File: `src/main/webapp/WEB-INF/templates/pages/places/list.html`

Agregar dentro del `<aside>` de detalles, después del bloque de imagen/dirección/descripción. No
hay vista nueva que crear:

```html
<div sec:authorize="isAuthenticated()" class="mt-6" th:if="${userPlans}">
    <h3 class="font-medium mb-2">Add to plan</h3>
    <form th:action="@{/api/plans/{planId}/places(planId=${selectedPlace.id})}" method="post" class="flex gap-2">
        <input type="hidden" name="placeId" th:value="${selectedPlace.id}">
        <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">
        <select name="planId" class="px-3 py-2 border rounded focus:outline-none">
            <option value="">Select a plan...</option>
            <option th:each="plan : ${userPlans}" th:value="${plan.id}" th:text="${plan.name}"></option>
        </select>
        <button type="submit" class="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded">
            Add
        </button>
    </form>
</div>
```

### 2. Update plan detail template with Vue.js

File: `src/main/webapp/WEB-INF/templates/pages/plans/detail.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="${plan?.name}">Plan Detail</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/vue@3/dist/vue.global.js"></script>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <meta name="_csrf" th:content="${_csrf.token}">
    <meta name="_csrf_header" th:content="${_csrf.parameterName}">
</head>
<body class="bg-gray-100 min-h-screen">
    <div th:replace="~{fragments/header :: header}"></div>

    <div id="app" class="container mx-auto px-4 py-8" th:if="${plan}">
        <div class="flex justify-between items-start mb-8">
            <div>
                <h1 class="text-3xl font-bold" th:text="${plan.name}"></h1>
                <span class="inline-block mt-2 px-2 py-0.5 text-xs text-white rounded"
                      th:classappend="${plan.visibility.name() == 'PUBLIC'} ? 'bg-green-500' : 'bg-gray-500'"
                      th:text="${plan.visibility}"></span>
            </div>
            <div class="flex gap-2">
                <a th:href="@{/places}" class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded">
                    Add Places
                </a>
                <button @click="sharePlan" class="bg-purple-500 hover:bg-purple-600 text-white px-4 py-2 rounded">
                    Share
                </button>
            </div>
        </div>

        <p class="text-gray-600 mb-4" th:text="${plan.description}"></p>
        <p class="text-gray-700 mb-8" th:if="${plan.visitDate}">
            <strong>Date:</strong> <span th:text="${plan.visitDate}"></span>
        </p>

        <div class="flex gap-8">
            <div class="flex-1">
                <div class="bg-white p-6 rounded-lg shadow">
                    <h2 class="text-xl font-bold mb-4">Itinerary</h2>

                    <div v-if="itinerary.length === 0" class="text-gray-500 text-center py-8">
                        No places added yet.
                    </div>

                    <div v-else class="space-y-3">
                        <div v-for="(item, index) in itinerary" :key="item.id"
                             class="flex items-center gap-4 p-3 border rounded-lg">
                            <div class="w-8 h-8 bg-blue-500 text-white rounded-full flex items-center justify-center font-bold">
                                {{ index + 1 }}
                            </div>
                            <div class="flex-1">
                                <strong>{{ item.place.name }}</strong>
                                <span class="ml-2 px-2 py-0.5 text-xs text-white bg-blue-500 rounded">
                                    {{ item.place.category }}
                                </span>
                                <div class="mt-1 flex gap-2">
                                    <input type="date" :value="item.visitDate"
                                           @change="updateDateTime(item.id, 'visitDate', $event.target.value)"
                                           class="px-2 py-1 border rounded text-sm">
                                    <input type="time" :value="item.visitTime"
                                           @change="updateDateTime(item.id, 'visitTime', $event.target.value)"
                                           class="px-2 py-1 border rounded text-sm">
                                </div>
                            </div>
                            <button @click="removePlace(item.id)"
                                    class="text-red-500 hover:text-red-700 font-bold">
                                X
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="flex-1">
                <div class="bg-white p-6 rounded-lg shadow">
                    <h2 class="text-xl font-bold mb-4">Route Map</h2>
                    <div id="plan-map" class="h-96 rounded-lg"></div>
                </div>
            </div>
        </div>
    </div>

    <script th:inline="javascript">
        const planId = [[${plan?.id}]];

        const { createApp, ref, onMounted } = Vue;

        createApp({
            setup() {
                const itinerary = ref([]);
                let map = null;
                let markers = [];
                let routeLine = null;

                function getCsrfToken() {
                    const meta = document.querySelector('meta[name="_csrf"]');
                    return meta ? meta.getAttribute('content') : '';
                }
                function getCsrfHeader() {
                    const meta = document.querySelector('meta[name="_csrf_header"]');
                    return meta ? meta.getAttribute('content') : 'X-CSRF-TOKEN';
                }

                onMounted(async () => {
                    await loadItinerary();
                    initMap();
                });

                async function loadItinerary() {
                    const response = await fetch('/api/plans/' + planId + '/places');
                    itinerary.value = await response.json();
                    updateMap();
                }

                function initMap() {
                    map = L.map('plan-map').setView([-34.6037, -58.3816], 13);
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '&copy; OpenStreetMap contributors'
                    }).addTo(map);
                }

                function updateMap() {
                    markers.forEach(m => map.removeLayer(m));
                    markers = [];
                    if (routeLine) map.removeLayer(routeLine);

                    const coordinates = [];

                    itinerary.value.forEach((item, index) => {
                        if (item.place.latitude && item.place.longitude) {
                            coordinates.push([item.place.latitude, item.place.longitude]);

                            const marker = L.marker([item.place.latitude, item.place.longitude])
                                .addTo(map)
                                .bindPopup('<strong>' + (index + 1) + '. ' + item.place.name + '</strong>');
                            markers.push(marker);
                        }
                    });

                    if (coordinates.length > 1) {
                        routeLine = L.polyline(coordinates, {
                            color: '#3498db', weight: 3, dashArray: '10, 5'
                        }).addTo(map);
                    }

                    if (coordinates.length > 0) {
                        map.fitBounds(coordinates, { padding: [50, 50] });
                    }
                }

                async function removePlace(planPlaceId) {
                    if (confirm('Remove this place?')) {
                        await fetch('/api/plans/' + planId + '/places/' + planPlaceId, {
                            method: 'DELETE',
                            headers: { [getCsrfHeader()]: getCsrfToken() }
                        });
                        await loadItinerary();
                    }
                }

                async function updateDateTime(planPlaceId, field, value) {
                    await fetch('/api/plans/' + planId + '/places/' + planPlaceId, {
                        method: 'PUT',
                        headers: {
                            'Content-Type': 'application/json',
                            [getCsrfHeader()]: getCsrfToken()
                        },
                        body: JSON.stringify({ [field]: value })
                    });
                }

                function sharePlan() {
                    fetch('/plans/' + planId + '/share', {
                        method: 'POST',
                        headers: { [getCsrfHeader()]: getCsrfToken() }
                    })
                        .then(r => r.json())
                        .then(data => {
                            const url = window.location.origin + data.url;
                            prompt('Share this link:', url);
                        });
                }

                return { itinerary, removePlace, updateDateTime, sharePlan };
            }
        }).mount('#app');
    </script>
</body>
</html>
```

## Archivos a crear/modificar

| File | Action |
|------|--------|
| `templates/pages/places/list.html` | Update (ficha de lugar con picker de plan inline, sin modal) |
| `templates/pages/plans/detail.html` | Update (add Vue.js itinerary with CSRF helpers) |
| `resources/core/js/pages/places-list.js` | Update (wizard: ventana del día y rango derivado por parada, AC-12; picker del panel de detalle, AC-01/02; overlay del borrador en el mapa, AC-13) |
| `resources/core/js/pages/plans-detail.js` | Update (itinerario agrupado por día, chips y PUT completo del horario, AC-05/AC-06) |
