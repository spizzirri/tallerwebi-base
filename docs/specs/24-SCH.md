# [SCH] Programación del itinerario

> Trello: pendiente de crear la card (el número de spec se ajusta cuando exista)
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.

## Objetivo

El creador de planes pide **una sola fecha y una hora de inicio**, y el sistema calcula
el horario de cada parada a partir de la duración de cada lugar, el traslado entre paradas
y una ventana diaria. El usuario nunca carga una hora por parada en el camino feliz: solo
puede ajustarla cuando quiere fijar algo.

Un plan puede durar varios días sin límite. Cuando el itinerario no entra en la ventana
del día, continúa solo en el día siguiente.

## Problema actual

El flujo de hoy pide datos de calendario en tres lugares distintos y ninguno manda:

| # | Problema | Dónde |
|---|----------|-------|
| P-1 | Al crear, el paso 2 ordena con ↑/↓ pero el servidor reordena por `visitTime` al guardar: el orden manual se descarta | `list.html:440-447`, `PlanController.toPlanOrderedByVisitTime` |
| P-2 | En el detalle la regla es la opuesta: el drag & drop manda y las horas no se tocan | `plans-detail.js:287-302` |
| P-3 | Cada parada arranca con la hora vacía y sin default: N paradas = N decisiones, todas opcionales | `places-list.js:210-212` |
| P-4 | `eventTime` se muestra y se edita pero nunca se pide al crear: todo plan nace con "Sin hora" | `detail.html:313-316`, `list.html:413-417` |
| P-5 | La fecha del plan y la de las paradas divergen en silencio; cambiar la fecha del plan no mueve ninguna parada | `PlanController.toPlan:507-532` |
| P-6 | Un editor puede agregar, quitar y reordenar paradas pero no ponerles hora | `detail.html:470` vs `canEditPlan` |
| P-7 | El seeder crea las paradas sin hora ni fecha: todos los planes de demo muestran "Horario por definir" | `PublicPlanDataSeeder.java:317-321` |
| P-8 | No existe el concepto de duración en el modelo: solo se puede expresar "cuándo llego", nunca "cuánto me quedo" | `Place.java`, `PlanPlace.java:34-44` |

## Decisiones de diseño

Estas decisiones están tomadas y son las que la implementación debe respetar.

| # | Decisión | Motivo |
|---|----------|--------|
| D-1 | Una fecha (Día 1) y una hora de inicio por plan; el fin de día es el default fijo 23:00, sin input editable | Es el único calendario que el usuario tipea |
| D-2 | Las horas de las paradas son **derivadas** y se persisten en `visitDate`/`visitTime` | La vista pública, el listado y el clon siguen leyendo lo mismo |
| D-3 | La duración efectiva es una cascada: override de la visita → duración del lugar → default por categoría | El camino feliz no pide nada nuevo por parada |
| D-4 | El orden del itinerario es el orden de las paradas; las horas lo siguen | Elimina P-1/P-2: una sola regla en creación y edición |
| D-5 | El traslado se estima por distancia y modo de transporte; solo el override manual por tramo (`PlanPlace.travelMinutesToNext`) se persiste, los tramos calculados siguen derivándose | Reordenar es determinista: el override es una excepción visible, no estado oculto |
| D-6 | Un pin explícito de hora (y opcionalmente de día) corta la cadena | Es el escape hatch manual, y es visible |
| D-7 | El backfill deriva todo y descarta las horas viejas cargadas a mano | No hay usuarios reales; respetarlas produce itinerarios mixtos y peores |
| D-8 | El plan es multi-día sin límite, con rollover por ventana diaria | Sin ventana, un viaje largo termina a las 03:00 |

## Modelo de datos

Cuatro columnas nuevas, todas aditivas. El proyecto usa `hbm2ddl.auto=update` sin
migraciones (`JpaConfig.java:40`, `docs/setup.md:85`), así que se crean solas.

| Campo | Tabla | Tipo | Rol |
|-------|-------|------|-----|
| `Place.defaultDurationMinutes` | `places` | `Integer` nullable | Duración típica del lugar en el catálogo |
| `PlanPlace.durationMinutes` | `plan_places` | `Integer` nullable | Override de esta visita |
| `PlanPlace.transportModeToNext` | `plan_places` | `TransportMode` nullable | Override del tramo hacia la parada siguiente |
| `PlanPlace.travelMinutesToNext` | `plan_places` | `Integer` nullable | Override de minutos del tramo hacia la parada siguiente; `null` = calculado |
| `Plan.transportMode` | `plans` | `TransportMode` no nulo, default `WALK` | Modo por defecto del plan |
| `Plan.dayEndTime` | `plans` | `LocalTime` no nulo, default `23:00` | Fin de la ventana diaria |
| `PlanSettings.autoSchedule` | `plans` | `boolean` default `true` | Si reordenar recalcula las horas |

`Plan.eventTime` (ya existe) pasa a significar **hora de inicio de cada día**.
`PlanPlace.visitDate` y `PlanPlace.visitTime` pasan a estar **siempre poblados** por la derivación.

`TransportMode` es un enum nuevo: `WALK`, `BIKE`, `BUS`, `CAR`.

Duración por categoría, último fallback de la cascada:

| Categoría | Minutos | Categoría | Minutos |
|-----------|---------|-----------|---------|
| RESTAURANT | 90 | PARK | 60 |
| BAR | 60 | SHOPPING | 60 |
| CAFE | 45 | NIGHTLIFE | 120 |
| MUSEUM | 90 | CULTURE | 90 |
| SPORT | 90 | OTHER | 60 |

## Algoritmo de derivación

Función pura, sin acceso a repositorios: recibe el plan y las paradas ya ordenadas, y
devuelve fecha y hora por parada. Es la única fuente de verdad del horario.

```
día    = plan.eventDate            // Día 1
cursor = plan.eventTime            // inicio de la ventana

para cada parada en orden:
    dur = parada.durationMinutes
       ?? parada.place.defaultDurationMinutes
       ?? CATEGORY_DEFAULT[parada.place.category]

    si parada tiene hora fijada:
        si tiene día fijado: día = parada.pinnedDate
        cursor = parada.pinnedTime
    si no, si cursor + dur > plan.dayEndTime:
        día    = día + 1
        cursor = plan.eventTime

    parada.visitDate = día
    parada.visitTime = cursor
    cursor = cursor + dur + gap(parada, siguiente)   // la última no suma gap
```

Reglas de borde, todas obligatorias:

| Caso | Comportamiento |
|------|----------------|
| Pin de hora sin día | Usa el día que la cadena ya tenía |
| Pin de día sin hora | Reinicia ese día en `plan.eventTime` |
| Duración sola mayor que la ventana | Se deja desbordar, nunca se itera |
| Gap que cruza `dayEndTime` | La parada siguiente rueda al día siguiente |
| Última parada | No suma gap |
| Parada sin coordenadas | Gap de fallback de 15 min |
| Plan sin fecha | Sin derivación (la fecha pasa a ser obligatoria) |

## Traslado

```
distancia_calle = haversine(a, b) × 1.3          // calle vs línea recta
gap = clamp( round5(distancia_calle / velocidad × 60), 5, 90 )
```

Velocidades efectivas, que ya incluyen espera y estacionamiento para no sobre-prometer:

| Modo | Velocidad | Nota |
|------|-----------|------|
| A pie | 4.5 km/h | |
| Bicicleta | 14 km/h | |
| Colectivo | 16 km/h | espera incluida |
| Auto | 22 km/h | estacionar incluido |

Si el tramo tiene `travelMinutesToNext` cargado, se usa ese valor tal cual (piso de 1
minuto, sin tope de 90) y la fórmula no corre; el clamp 5-90 solo aplica a los tramos
calculados.

En la UI se rotula siempre como **"traslado estimado"**. No es ruteo real y no debe
presentarse como tal.

## UX por pantalla

### Creación (`/explore`, panel de 3 pasos)

- **Paso 1**: nombre, descripción, **fecha (obligatoria, `min=hoy`)** y **hora de inicio**
  (default 10:00). No pide fin de día: la ventana cierra siempre a las 23:00.
- **Paso 2**: cada parada muestra un chip con el rango calculado y la duración; los
  tramos entre paradas se editan en UNA sola línea: el selector de modo junto al input
  de minutos, cuyo placeholder muestra el default calculado (vacío = calculado por el
  servidor). Sin rótulo propio ni hint adicional. La lista se agrupa por día derivado.
  El pie muestra el total.

```
Día 1 · sáb 24/10
┌──────────────────────────────────────────────────────┐
│ 1  Café Tortoni     [Café]    10:00 – 10:45   ↑↓ ×   │
│      modo ▾  [12 min]                                │
│ 2  MALBA            [Museo]   10:57 – 12:27   ↑↓ ×   │
└──────────────────────────────────────────────────────┘
Día 2 · dom 25/10
┌──────────────────────────────────────────────────────┐
│ 3  Tigre            [Parque]  10:00 – 11:00   ↑↓ ×   │
└──────────────────────────────────────────────────────┘
3 paradas · 2 días · 3h 15m en lugares
```

- El separador de día se oculta cuando el plan entra en un solo día.

### Detalle (`/plans/{id}`)

- Mismos chips y agrupación por día en la lista del itinerario.
- La fecha por parada se muestra solo cuando difiere de `plan.eventDate`.
- Reordenar recalcula los horarios cuando `autoSchedule` está activo.
- El diálogo "Ver más" conserva un ajuste manual de duración, hora y día (pin).
- "Editar plan" pide fecha y hora de inicio; el fin de día es el default fijo 23:00.
- Se corrige el permiso: el editor de horario usa `canEditPlan`, no `isPlanAdministrator`.

### ABM de lugares

- Campo "Duración típica" en `place-form.html`, junto a Opening Hours.
- `EditPlaceRequest` pasa de 11 a 12 campos: hay que actualizar el `NOPMD` existente,
  no silenciarlo. `PlaceAdminController.formFromQuery` mapea campo por campo y necesita
  la línea nueva.

### Vista pública

- Cada parada muestra rango horario y, si corresponde, el día.

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | Al crear un plan se pide una única fecha y una hora de inicio; el cierre de la jornada es el default fijo 23:00, sin input editable |
| AC-02 | La fecha del plan es obligatoria: sin fecha no se crea el plan |
| AC-03 | Cada parada del itinerario muestra un rango horario calculado (inicio–fin) |
| AC-04 | El horario se recalcula al agregar, quitar o reordenar paradas, al cambiar una duración y al cambiar la hora de inicio |
| AC-05 | La duración efectiva de una parada es: override de la visita, si no la del lugar, si no la de su categoría |
| AC-06 | El usuario puede fijar manualmente la hora, y opcionalmente el día, de una parada; las siguientes se recalculan desde ese pin |
| AC-07 | Una parada fijada manualmente se distingue visualmente de una derivada |
| AC-08 | Un itinerario que no entra en la ventana diaria continúa en el día siguiente |
| AC-09 | El itinerario se agrupa por día y el separador se omite cuando el plan dura un solo día |
| AC-10 | El plan tiene un modo de transporte por defecto y cada tramo puede sobrescribirlo |
| AC-11 | El traslado se estima por distancia y modo, se rotula como estimación y queda entre 5 y 90 minutos; un valor fijo cargado por el usuario se usa tal cual con piso de 1 minuto |
| AC-12 | El orden del itinerario es el orden de las paradas; las horas lo siguen y crear un plan ya no reordena por hora |
| AC-13 | Cambiar la fecha del plan corre el Día 1 y recalcula todas las fechas derivadas |
| AC-14 | El resumen del plan muestra la hora de inicio real; ningún plan creado con el flujo nuevo muestra "Sin hora" |
| AC-15 | Un editor puede editar duración, fecha y hora de una parada igual que el dueño |
| AC-16 | El ABM de lugares permite editar la duración típica del lugar |
| AC-17 | El backfill deja todos los planes existentes y los del seeder con horario derivado |
| AC-18 | La vista pública muestra el rango horario y el día de cada parada |
| AC-19 | Los formatos de fecha y el copy están en español y son consistentes entre creación, detalle y vista pública |
| AC-20 | Un plan existente que se edita conserva sus paradas, descripciones y orden |

## Escenarios de Test

### Tests Unitarios (`infrastructure/plan/PlanScheduleCalculatorTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | Tres paradas en un día se encadenan con duración y traslado | AC-03, AC-11 |
| U-02 | La duración del lugar gana sobre el default de categoría | AC-05 |
| U-03 | El override de la visita gana sobre la duración del lugar | AC-05 |
| U-04 | Un pin de hora recalcula solo las paradas siguientes | AC-06 |
| U-05 | Un pin de día sin hora reinicia ese día en la hora de inicio | AC-06, AC-08 |
| U-06 | Una parada que no entra en la ventana rueda al día siguiente | AC-08 |
| U-07 | Una parada de duración mayor que la ventana desborda sin iterar | AC-08 |
| U-08 | El gap se calcula por modo y queda entre 5 y 90 minutos | AC-10, AC-11 |
| U-09 | Sin coordenadas se usa el gap de fallback | AC-11 |
| U-10 | La última parada no suma traslado | AC-03 |
| U-11 | Un plan de un solo día no genera separador de día | AC-09 |
| U-23 | El traslado fijo gana sobre la distancia y no se conea en 90 | AC-11 |
| U-24 | El traslado fijo tiene piso de 1 minuto y no necesita coordenadas | AC-11 |

### Tests de Integracion (`integration/PlanControllerIntegrationTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-01 | `POST /plans` con fecha, hora de inicio y paradas persiste el horario derivado | AC-01, AC-03 |
| I-02 | `POST /plans` sin fecha no crea el plan | AC-02 |
| I-03 | `POST /plans` respeta el orden enviado y no reordena por hora | AC-12 |
| I-04 | `PUT /plans/{id}` con nueva fecha corre las fechas de las paradas | AC-13 |
| I-05 | `POST /api/plans/{id}/places` calcula el horario de la parada nueva | AC-03 |
| I-06 | `POST /api/plans/{id}/places/reorder` recalcula con `autoSchedule` activo | AC-04 |
| I-07 | `PUT /api/plans/{id}/places/{id}` con pin recalcula las siguientes | AC-06 |
| I-08 | Un editor puede editar el horario de una parada | AC-15 |
| I-09 | El resumen del plan muestra la hora de inicio persistida | AC-14 |
| I-13 | `POST /plans` acepta `travelMinutesToNext` por tramo y lo persiste | AC-11 |

### Tests de Integracion (`infrastructure/plan/PlanScheduleBackfillTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-10 | El backfill completa duraciones y recalcula un plan existente | AC-17 |
| I-11 | Una segunda corrida no modifica nada (idempotente) | AC-17 |
| I-12 | El seeder deja los planes de demo con horario derivado | AC-17 |

### E2E (minimos)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `PlanScheduleE2E` | Crear plan con 3 paradas, ver chips calculados, reordenar y ver el recalculo | AC-01, AC-03, AC-04, AC-12 |

## Plan de entrega

Cinco slices. Los dos primeros no tocan ninguna pantalla y ya arreglan los planes del
seeder, así que se pueden mergear solos.

| # | Slice | Contenido | Cierra | Costo |
|---|-------|-----------|--------|-------|
| 1 | Núcleo | enum `TransportMode`, columnas, cascada de duración, calculadora pura multi-día + gaps, tests U-01..U-11 | P-8 | ~2 días |
| 2 | Datos | duraciones en el seeder, ventana y modo en los planes de demo, runner de backfill, tests I-10..I-12 | P-7 | ~1 día |
| 3 | Creación | paso 1 con hora de inicio (fin de día fijo 23:00), paso 2 con chips, grupos por día, tramos y total, tests I-01..I-05 | P-1, P-3, P-4 | ~2-3 días |
| 4 | Detalle | chips y agrupación, recompute al reordenar, pin en el diálogo, fix de permiso de editor, tests I-06..I-09 | P-2, P-5, P-6 | ~2 días |
| 5 | Pulido | duración en el ABM y en el importador, copy en español, formatos de fecha unificados, vista pública | P-5 | ~1 día |

Total estimado: 8-9 días.

## Backfill

Pasada única al arrancar, con el mismo patrón que `PublicPlanDataSeeder`:

1. Busca planes donde alguna parada tenga `durationMinutes == null`.
2. Resuelve la duración por cascada y la persiste en la parada.
3. Recalcula la cadena completa del plan y guarda fecha y hora por parada.

Idempotente por construcción: después de la primera pasada no queda ningún `null`, así
que la segunda corrida no encuentra nada que hacer.

Según D-7, las horas cargadas a mano **se descartan**: la base es contenido de demo y
planes de prueba. Si más adelante hubiera datos reales, este es el punto a cambiar.

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `domain/plan/TransportMode.java` | Crear |
| `domain/plan/PlanScheduleCalculator.java` | Crear |
| `domain/plan/PlanSchedule.java` | Actualizar (hora de inicio, fin de día, modo) |
| `domain/plan/PlanSettings.java` | Actualizar (`autoSchedule`) |
| `domain/planplace/PlanPlace.java` | Actualizar (duración, modo y minutos del tramo) |
| `domain/place/Place.java` | Actualizar (duración típica) |
| `infrastructure/plan/PlanScheduleBackfill.java` | Crear |
| `infrastructure/PublicPlanDataSeeder.java` | Actualizar (duraciones, ventana, modo) |
| `infrastructure/planplace/PlanPlaceServiceImpl.java` | Actualizar (recalculo al agregar y reordenar) |
| `infrastructure/plan/PlanServiceImpl.java` | Actualizar (recalculo al crear y editar) |
| `presentation/plan/PlanController.java` | Actualizar (derivacion, ya no ordena por hora) |
| `presentation/plan/PlanPlaceRestController.java` | Actualizar (duracion y modo en el payload) |
| `presentation/plan/PlanRequest.java` | Actualizar (hora de inicio, fin de día, modo) |
| `presentation/plan/PlanPlaceRequest.java` | Actualizar (duracion, modo del tramo) |
| `presentation/place/EditPlaceRequest.java` | Actualizar (duracion tipica, NOPMD a 12) |
| `presentation/place/PlaceAdminController.java` | Actualizar (mapeo del campo nuevo) |
| `webapp/.../pages/places/list.html` | Actualizar (paso 1 y paso 2) |
| `webapp/.../pages/plans/detail.html` | Actualizar (chips, agrupacion, pin, permiso) |
| `webapp/.../pages/plans/public.html` | Actualizar (rango horario y dia) |
| `webapp/.../pages/admin/place-form.html` | Actualizar (duracion tipica) |
| `webapp/.../js/pages/places-list.js` | Actualizar (preview en vivo, tramos) |
| `webapp/.../js/pages/plans-detail.js` | Actualizar (chips, recompute, pin) |
| `webapp/.../js/lib/plan-schedule.js` | Crear (espejo de la calculadora para el preview) |
| `CONTEXT.md` | Actualizar (duracion, ventana diaria, modo de transporte) |
| `docs/specs/05-APL-FE.md` | Actualizar (AC-05 y AC-06 quedan superados) |

## Riesgos y fuera de alcance

| Riesgo | Mitigación |
|--------|------------|
| La calculadora existe en Java y en JS y pueden divergir | El cálculo de JS es solo preview; el guardado siempre usa el valor del servidor |
| El gap es una estimación sin ruteo real | Se rotula como estimación y los límites 5-90 acotan el error |
| `hbm2ddl.auto=update` no borra ni ensancha columnas | Las cuatro columnas son aditivas y nullable o con default |
| El backfill toca datos existentes | Es una pasada única e idempotente, documentada en D-7 |

Fuera de alcance: horarios de apertura del lugar (`Place.openingHours` es texto libre del
importador OSM), ruteo real con API externa, y notificaciones o recordatorios por horario.
