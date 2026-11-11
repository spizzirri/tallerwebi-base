# [STD] Vista del plan (dueño): sin datos repetidos y con menos scroll

> Trello: pendiente de crear la card — `[DIS][STD] Vista del plan (dueño): sin datos repetidos y
> con menos scroll`, lista `BACKLOG` (spec 1:1 con la card)
> **Estimacion:** 3 pts Fibonacci
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.
> **Estado (2026-10-28):** implementada y verde — TDD (rojo `64b3995`, verde `e28b3e8`) y
> `gate.ps1 all` exit 0 (surefire 998/0/0 · E2E 84/0/0). Decisiones de diseño cerradas con la
> usuaria (ver Notas). Card pendiente de pegar en Trello (hand-off preparado).
> **Variante B (2026-10-29, descartada):** la usuaria pidió probar "explore de verdad": mapa
> primero a todo el ancho y las cards como pestañas exclusivas que se despliegan **sobre** el
> mapa desde una barra inferior (ver "Variante B"). Se implementó y quedó verde en el lote, pero
> la usuaria la reemplazó por la variante C antes de publicarla.
> **Variante C (2026-11-03, en curso):** tablero de tres columnas — **ficha fija a la izquierda**
> con el recorrido debajo, **mapa al centro** y **participantes a la derecha** —, tomado del
> layout de explore que mostró como referencia. En mobile se apila en el orden ficha → mapa →
> recorrido → participantes. Reemplaza a AC-06 y AC-08 (ver AC-10..AC-12).

## Objetivo

La vista de dueño de `/plans/{id}` —la que se ve al crear un plan o al clonar uno— debe mostrar
cada dato del plan **una sola vez** y caber en **menos pantallas**, sin perder ninguna acción que
hoy ofrece. Es deuda de diseño: **el alcance es visual, no funcional** (ninguna ruta,
controlador, servicio, permiso ni regla de negocio cambia).

Hoy es la página más larga del sitio: 2.145 px en desktop y 3.589 px en un celular. Casi la mitad
de la altura la ocupan dos bloques que se pueden plegar (edición de metadatos e invitación) y seis
datos que se ven repetidos entre la ficha y el form de edición.

## Problema actual (baseline medido el 2026-10-28)

Medido con Playwright sobre el plan clonado en dev (`/plans/01a184dd-1546-7de9-b7a7-94fa8903b5c5`
con arranque de `test@`); capturas baseline en el hand-off de la card.

| Ancho | Alto total (`scrollHeight`) | Pantallas |
|-------|-----------------------------|-----------|
| 1440×900 (desktop) | 2.145 px | 2,4 |
| 390×844 (mobile) | 3.589 px | 4,3 |

Bloques por alto (desktop): editar plan 731 px (34 % de la página) · Invitar 545 px · mapa 514 px ·
ficha 426 px · recorrido 372 px · solicitudes con estado vacío 106 px · agregar lugar 128 px.

Orden vertical real en 390 (medido, no inferido): `solicitudes → mapa → agregar lugar → recorrido →
ficha → participantes → editar` — el resumen del plan queda **después** del recorrido.

Duplicados en la vista de dueño (texto visible + `value` de los inputs, sin abrir nada):

| Dato | Veces | Dónde se ve |
|------|-------|-------------|
| nombre | 2 | cabecera (`h2`) + input del form |
| descripción | 2 | ficha + textarea |
| fecha | 2 | ficha + input date |
| hora | 2 | ficha + input time |
| visibilidad | 2 | ficha + switch del form |
| administrador | 2 | ficha + badge "Dueño" en Participantes |

La hora/fecha de las tarjetas del recorrido son dato de parada, no duplicados.

## Pre-requisitos

- [DIS] (17-DIS) mergeada: navbar compartido, banda de cabecera (AC-14) y el modal "Ver más" con el
  patrón de diálogo a11y (`aria-labelledby`, foco, Escape).
- [SCH] (24-SCH) mergeada: la ficha FECHA ya muestra dd/mm/yyyy y el form de edición lleva "Fin del
  día" y "Cómo se trasladan" (ambos campos viajan a la card de configuración, PLN #58).
- [MOBILE] mergeada: el responsive ≤575 px, donde aplica el nuevo orden de secciones.
- El model ya trae todo lo necesario (`canEditPlan`, `canInvite`, `canHandleRequests`,
  `joinRequests`, `participants`): **no hay backend nuevo ni migraciones**.
- Sin clases de test nuevas: los métodos E2E nuevos van dentro de la clase existente
  `PlanPlaceDetailE2E`; la verificación es gate completo + checks visuales.

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | En la vista base de dueño cada dato del plan se ve **una sola vez**: nombre (cabecera), descripción, fecha, hora y visibilidad viven en la ficha y ya no en un form en línea. La ficha es compacta —chips `FECHA · HORA · VISIBILIDAD · CÓDIGO` y descripción con clamp de 3 líneas— y **no muestra el tile ADMINISTRADOR**: la fuente única es el badge "Dueño" de Participantes |
| AC-02 | La **configuración del plan se abre como una card centrada** (`<dialog>` con el chrome de AC-13) desde "Editar" al pie de la ficha: conserva `canEditPlan`, el mismo form (`POST /plans/{id}` con `th:object="${plan}"`) y el mismo guardado. Declara `aria-labelledby` hacia su título, recibe el foco al abrirse, Escape la cierra y devuelve el foco al botón. No suma scroll a la página; PLN #58 aportó los ids `#configuracion`/`#config-datos`/`#config-roles`, que viven dentro de la card |
| AC-03 | ~~Sin solicitudes pendientes **no se renderiza la card de Solicitudes**.~~ **ACTUALIZADO (2026-11-05):** la card de Solicitudes es un **resumen sin scroll** —muestra el conteo y, sin pendientes, "No hay solicitudes pendientes."—. Con ≥ 1 se destaca como **aviso** ("Nueva" + "Tenés N solicitudes para responder.") y su botón "Ver" abre la **card centrada** `#join-requests-dialog` con la lista completa de Aceptar/Rechazar; la fila de cabecera repite el conteo en un botón badge (solo con ≥ 1) que abre la misma card. La lista no vive en la columna |
| AC-04 | "Invitar a alguien" deja de ser una card permanente y pasa a un **accordion dentro de la card centrada de Participantes** (`#participants-dialog`): la card resumen de la columna muestra el conteo y hasta tres nombres con sus badges, y "Ver" abre la card centrada con la lista completa; allí un botón "Invitar a alguien" con `aria-expanded` expande y colapsa el form actual completo (mismo action, mismos campos, mismo `th:if="${canInvite}"`). Los avisos de invitación quedan en la card resumen para verse tras el redirect |
| AC-05 | "Agregar lugar al plan" deja de ser una card propia y pasa a un **botón en la cabecera de "Tu recorrido"** (`th:if="${canEditPlan}"`): al presionarlo se despliega el mismo picker (select de lugares disponibles + "+ Agregar") debajo del título, sin recargar la página |
| AC-06 | ~~En 390×844 el orden vertical es cabecera → ficha → Tu recorrido → Mapa → Solicitudes → Participantes; en desktop se conserva la composición de dos columnas~~ **SUPEREDIDO por AC-10** (variante C: el orden mobile pasa a ficha → mapa → recorrido → participantes) |
| AC-07 | El alto total de la página queda en **≤ 1.500 px en 1440×900** (−30 % vs 2.145) y **≤ 2.800 px en 390×844** (−22 % vs 3.589), sin perder ninguna acción actual: la configuración y los baneados no viven en el flujo, se abren en cards centradas. Se mide con `document.documentElement.scrollHeight` sobre el arrange estándar de los E2E. **Vigente en todas las variantes** |
| AC-08 | ~~**Variante B:** el mapa va **primero**, a todo el ancho dentro de un stage con altura tipo viewport (`calc(100dvh − 15rem)`); las cards son **pestañas exclusivas** que se despliegan **sobre** el mapa desde una barra al pie del stage — el patrón de radios `sr-only` + labels de explore —, **cerradas por defecto**; la ✕ (`card-none`) las cierra todas~~ **SUPEREDIDO por AC-10** (la usuaria descartó las pestañas el 2026-11-03) |
| AC-09 | La cabecera queda **sin "Más opciones"**: el título de la banda baja a ~1.35 rem (sin protagonismo) para que "Compartir" y "Eliminar" respiren, y **"Ver miembros baneados" pasa a un botón dentro de Participantes** que abre su propio `<dialog>` (mismo `aria-controls="banned-participants"` y retorno de foco con Escape). **Vigente en las variantes B y C** |
| AC-10 | **Variante C:** en ≥ 992 px la vista es un **tablero de tres columnas** dentro de un stage con altura tipo viewport (`calc(100dvh − 15rem)`): **izquierda** la ficha, siempre visible, con **Participantes debajo** (y Solicitudes arriba de Participantes) como **cards resumen sin scroll interno**: el contenido completo de ambas se abre en cards centradas; **centro** el mapa, que ocupa todo el alto restante; **derecha** "Tu recorrido", con scroll propio. La ficha **no se mueve mientras se scrollea el recorrido** (fila `auto` + `1fr` en la columna izquierda, sin `position: fixed`). En < 992 px se apila con scroll de página en el orden **ficha → mapa → solicitudes → participantes → recorrido**. El DOM sigue el orden ficha → mapa → recorrido → solicitudes → participantes → diálogos (participantes, solicitudes, configuración, baneados y ficha del lugar) para los `substring()` de integración; las áreas de la grilla ubican las cards |
| AC-11 | **Variante C:** las acciones de cada card se agrupan en **una fila propia al pie de la card** para que no se aplasten en columnas angostas: en Participantes, nombre + badge arriba y las acciones (rol, Eliminar del plan, Banear) en una fila debajo; en el recorrido, "Ver más" y "Eliminar" debajo del contenido de la parada; en la ficha, "Editar" en la cabecera junto a los chips. Ninguna acción existente desaparece |
| AC-12 | **Claro y oscuro:** el tablero usa solo variables `--bs-*` (superficie, borde, texto, secundarios) y no colores fijos, así que ambas opciones del tema se ven correctas. Se verifica con capturas en los dos temas |
| AC-13 | **La ficha del lugar ("Ver más" dentro de "Tu recorrido") se estandariza como una card más:** los tres `<dialog>` del detalle (ficha del lugar, configuración y baneados) comparten el mismo chrome —centrado en el viewport, radio de 16 px, `padding` de 24 px, superficie `--bs-body-bg`, sombra y backdrop—, y los bloques del `dl` de la ficha se leen como los chips de la ficha del plan (fondo `--bs-secondary-bg`, radio de 10 px, sin líneas separadoras). El chip de categoría repite el label en español y el icono de la card del recorrido, no el enum del catálogo. Antes el reset de márgenes de Tailwind dejaba el `<dialog>` nativo pegado al borde superior izquierdo, con radio de 6 px |
| AC-14 | **La cabecera comparte la grilla del tablero:** la banda azul del título ocupa las dos primeras columnas (termina exactamente donde termina el mapa) y las acciones viven en la tercera, sobre la columna derecha. La configuración se abre desde "Editar" en la ficha, así que la cabecera no repite el botón: las acciones que quedan (Compartir y Eliminar plan) comparten alto, y el borrado va en `btn-outline-danger` para no competir con la banda. En < 992 px la banda y las acciones se apilan y las acciones se alinean a la izquierda |

## Escenarios de Test

Es una spec de diseño: no hay lógica nueva, así que no hay clases de test U/I/S nuevas. La
verificación son los checks V, tres métodos E2E nuevos dentro de `PlanPlaceDetailE2E` y la puerta
completa.

### Checks visuales (V-XX, manuales o asistidos por los E2E)

| # | Check | AC que cubre |
|---|-------|-------------|
| V-01 | Vista de dueño sin abrir nada: nombre, descripción, fecha, hora y visibilidad ocurren una vez cada uno en texto visible + `value` de inputs; `#plan-summary` muestra los 4 chips y una descripción de ≤ 3 líneas, y no el tile ADMINISTRADOR; Participantes sí muestra "Dueño" | AC-01 |
| V-02 | Como dueño/editor: "Editar" de la ficha abre la card centrada de configuración —foco adentro, Escape la cierra y lo devuelve—; guardar un nombre nuevo en `#config-datos` actualiza cabecera y ficha tras el redirect. Sin permiso de edición no hay ni botón ni card | AC-02 |
| V-03 | Con 0 solicitudes: la card resumen muestra "No hay solicitudes pendientes." y el badge de la cabecera no se pinta. Con ≥ 1 (otra sesión se une al plan): la card se destaca con "Nueva" + "Tenés 1 solicitud para responder.", el botón de la cabecera lleva el conteo, y card y badge abren la card centrada con Aceptar/Rechazar | AC-03 |
| V-04 | Participantes: la card resumen muestra el conteo y hasta tres nombres con sus badges; "Ver" abre la card centrada con la lista completa, sin scroll interno. "Invitar a alguien" arranca colapsado con `aria-expanded="false"`, al expandir muestra el form completo (email, rol, canal) y al colapsar lo oculta | AC-04 |
| V-05 | "Tu recorrido": el botón "Agregar lugar al plan" vive en su cabecera y no existe card propia de agregar; el picker se despliega y agrega una parada al recorrido | AC-05 |
| V-06 | A 1440×900 la ficha queda a la izquierda del mapa y "Tu recorrido" a la derecha, con Participantes debajo de la ficha; la ficha no se mueve al scrollear el recorrido. A 390×844 el `y` cumple ficha < mapa < participantes < recorrido | AC-10 |
| V-07 | El `scrollHeight` de la página queda ≤ 1.500 px @1440×900 y ≤ 2.800 px @390×844, medido antes/después con Playwright | AC-07 |
| V-08 | Capturas en claro y oscuro: el tablero, las cards y los botones se leen con el tema del sistema en ambas opciones | AC-12 |
| V-09 | En 340–380 px de columna, las acciones de Participantes y del recorrido bajan a su propia fila y ninguna queda aplastada ni se solapa | AC-11 |
| V-10 | "Ver más" abre la ficha del lugar centrada (mismo aire a izquierda y derecha, sin tocar los bordes del viewport), con radio de 16 px, sombra y los bloques del `dl` como chips; el chip de categoría dice lo mismo que el de la card ("Restaurante", no "RESTAURANT") y lleva icono. Se compara contra las cards de configuración y baneados, que usan el mismo chrome | AC-13 |
| V-11 | A 1440×900 la banda azul termina en la misma coordenada que el borde derecho del mapa y las acciones quedan en la columna derecha; la cabecera ya no repite "Configuración" (la card se abre desde "Editar" en la ficha) y las acciones Compartir/Eliminar comparten alto. En 390 la banda y las acciones se apilan, alineadas a la izquierda | AC-14 |

Verificar como administrador y como editor, en 1440×900 y 390×844, en **claro y oscuro**.

### E2E

Tres métodos nuevos en `e2e/PlanPlaceDetailE2E`, sobre el arranque existente (crea un plan con una
visita y abre el detalle), más el del modal estandarizado:

- `keepsTheOwnerViewCompactAndWithoutRepeatedData` → AC-01, AC-03, AC-04 y AC-05: cada dato
  ocurre una vez en el tablero (texto visible + `value`), chips en `#plan-summary`, la card
  resumen de solicitudes con el estado vacío, y el botón "Agregar lugar al plan" dentro de la
  cabecera de "Tu recorrido" que muestra el select. El accordion de Invitar vive en la card
  centrada de Participantes: se abre con `plans.openParticipants()` antes de tocarlo.
  Las cards resumen (`#config-solicitudes`, `#participants-section`) se leen sin abrir nada.
- `editsPlanMetadataFromAModalWithDialogA11y` → AC-02: el botón "Editar" de la ficha abre la card
  centrada (`aria-labelledby` hacia el título, foco adentro, Escape cierra y devuelve el foco), el
  form de `#config-datos` trae los valores del plan y el round-trip de guardado (cambia la hora →
  el chip de la ficha refleja el valor nuevo) sin salir de la página.
- `laysOutTheFichaFixedOnTheLeftWithParticipantsUnderItAndTheItineraryOnTheRight` (reemplaza a
  `deploysTheCardsAsExclusiveTabsOverTheMap`) → AC-10, AC-11, AC-14 y AC-07: a 1440×900 la ficha
  queda a la izquierda del mapa y "Tu recorrido" a la derecha, con Participantes debajo de la ficha
  y compartiendo su columna; la banda del título termina donde termina el mapa, la cabecera no
  repite "Configuración" (se abre desde "Editar" en la ficha) y las acciones Compartir/Eliminar
  comparten alto; al scrollear el recorrido la ficha no se mueve; las tres cards están visibles al
  cargar; a 390×844 el orden vertical es ficha < mapa <
  participantes < recorrido; y el tablero queda bajo ambos techos (el borde inferior del stage).
- `opensThePlaceCardAsACenteredModalLikeTheRestOfTheCards` → AC-13: "Ver más" abre el `<dialog>`
  centrado en horizontal (mismo gap a ambos lados), sin tocar los bordes superior/inferior, con
  radio de 16 px, `padding` de 24 px y sombra; cada `.place-fact` comparte el fondo de los chips de
  `#plan-summary`, radio de 10 px y ya no lleva borde superior; el chip de categoría repite el texto
  y el icono de la card del recorrido.

La suite E2E existente debe seguir en verde: si un selector se rompe por el markup nuevo se
actualiza el page object, no la lógica del test. El form de edición vive en la card de
configuración, la lista de participantes y el invitar en su card centrada, y las solicitudes en la
suya: `PlansPage` expone `openPlanConfig()`, `openParticipants()` y `openRequests()`
(idempotentes, para poder llamarlos también después de cada reload) y los tests que tocan esos
bloques los abren primero. Con una card modal abierta el resto del documento queda inerte, así que
se cierra con Escape antes de seguir.

> El único `<dialog>` del detalle es el de la ficha del lugar; sus selectores se anclan por
> `aria-labelledby` (`place-detail-title`), nunca por `querySelector('dialog')`.

### Impactos previstos en la suite existente (se ajustan en la misma card)

- `PlanPlaceDetailE2E.addsPersistsAndDisplaysVisitDetails`: `input[name='eventTime']`.fill y el
  click en "Guardar cambios" ocurren en `#config-datos`, visible desde que carga la página (PLN
  #58); ya no hay diálogo que abrir.
- `PublicPlanCloneE2E.makePlanPublic` y `PublicPlanViewE2E.makePlanPublic`: `#isPublic`.check() +
  click "Guardar cambios" en la card de configuración (se abre antes).
- `PlanParticipantsE2E`: `#invitation-email`.fill requiere expandir el accordion antes.
- `PlanScheduleE2E`: `hasValue` sobre `eventTime` / `dayEndTime` / `transportMode` en la
  card de configuración (se abre antes). **[STD-011] En la variante C el reorden pasa a `DragEvent`
  sintéticos** (los mismos de `PlanPlaceDetailE2E`): con el recorrido en una columna con scroll
  propio, el arrastre por coordenadas de Playwright se quedaba a mitad de camino y el
  `POST /reorder` nunca salía.
- `PlanControllerIntegrationTest` (lo actualizó PLN #58 en main): los recortes ahora son
  `plan-summary → siguiente <section>` y `participants-heading → id="configuracion"`, y las
  aserciones de configuración esperan `#configuracion`, `#config-datos`, `#config-solicitudes`,
  `#config-roles` y `#banned-participants` en el detalle del dueño.
- `ActionIconsTest`: los 5 glyphs mínimos de DETAIL se mantienen (SAVE vive en la configuración al
  pie) y los nuevos botones suman íconos del set (Editar, Agregar lugar).
- `MobileLayoutE2E.T_MOB_008` (detalle legible a 320 px, sin scroll lateral) debe seguir en verde.
- `PlanBanE2E` y `PublicPlanJoinRequestE2E`: el form `join-requests` sigue existiendo cuando hay
  ≥ 1 solicitud (los tests crean la solicitud antes de aceptarla; al aceptar la última, la card
  desaparece — comportamiento nuevo esperado).
- **Variante B (pestañas):** toda interacción dentro de una card exige abrir su pestaña antes —
  helper `PlansPage.openCard("ficha|participantes|recorrido|solicitudes")` (click en
  `label[for='card-…']`). Ajustes previstos: `PlanBanE2E` (aceptar solicitud → pestaña
  Solicitudes; ban/baneados → pestaña Participantes; el trigger de baneados es ahora
  `[aria-controls='banned-participants']` directamente — "Más opciones" ya no existe),
  `PlanParticipantsE2E` (invite → Participantes; `innerText` de la sección con la pestaña
  abierta), `PlanScheduleE2E` y `MobileLayoutE2E.T_MOB_008` (recorrido abierto para
  `isVisible`/`innerText`/`boundingBox`/drag), `PublicPlanJoinRequestE2E` (aceptar → Solicitudes)
  y los helpers de `PlanPlaceDetailE2E` (`openStopDetails`, `addPlaceToPlan`,
  `screenshotItinerary` abren Recorrido). Las aserciones de texto (`containsText`/`hasCount`) y
  `hasValue` funcionan con la pestaña cerrada (v-show mantiene el DOM) y no se tocan.

### Puerta

El cierre de la card es el gate completo en verde con los counts escritos en el item de la card:
`powershell.exe -File scripts/gate.ps1 all` (unit+integration y E2E). No se agregan suites.

## Notas / decisiones de diseño

- **Decisiones cerradas (2026-10-28, con la usuaria):** edición → modal (**vigente: la
  configuración es una card centrada; PLN #58 aportó el contenido**); solicitudes → badge y card solo con ≥
  1 (**la card pasa a mostrar estado vacío con #58**); invitar → accordion; "Agregar lugar" →
  dentro de "Tu recorrido"; orden mobile resumen → recorrido → mapa → personas; techos de scroll ≤
  1.500 / ≤ 2.800 px.
- **Variante B — descartada (2026-10-29):** la usuaria pidió "el resto de las cards en acordeón
  como en la vista de explore" + "mapa primero" + título más chico para que Eliminar/Compartir
  tengan espacio + "Más opciones" fuera de la cabecera + miembros baneados dentro de la card de
  Participantes. Se implementó (pestañas exclusivas sobre el mapa, barra al pie) y quedó verde en
  el lote, pero el 2026-11-03 la usuaria la reemplazó por la variante C. Del trabajo de B quedan
  vigentes el título chico de la banda (AC-09) y el botón de baneados dentro de Participantes.
- **Variante C — en curso (2026-11-03):** la usuaria mostró la vista de explore como referencia y
  pidió "la ficha del lado izquierdo de forma fija, ya que es información predeterminada, y debajo
  el recorrido; del lado derecho la card de participantes". Concretado como:
  1. **Tablero de tres columnas** (≥ 992 px): izquierda ficha + recorrido, centro mapa, derecha
     participantes (solicitudes ≥ 1 arriba). Altura del stage tipo viewport, como en B.
  2. **Ficha quieta:** la columna izquierda es una grilla `fit-content(40%)` + `1fr`; la ficha no
     se mueve mientras el recorrido scrollea dentro de su celda.
  3. **Mobile:** apilado con scroll de página en el orden **ficha → mapa → recorrido →
     participantes** (pedido de la usuaria: "la ficha primero, después el mapa y después el
     resto").
  4. **Botones dentro de las cards:** fila propia al pie de cada card para que no se aplasten en
     columnas angostas (AC-11).
  5. **Claro y oscuro** con variables `--bs-*` (AC-12), como el resto de la app.
  - Supersede: AC-06 y AC-08. Vigentes: AC-01..AC-05, AC-07 y AC-09.
- **Merge con main (2026-11-04):** main traía **PLN #58 ("Unificar configuración del plan en el
  detalle")**. Se conservan sus ids y formularios (`#configuracion`, `#config-datos`,
  `#config-roles`), pero la configuración **se presenta como card centrada** en vez de sección al
  pie (pedido de la usuaria: evitar el scroll). Los baneados vuelven a su `<dialog>` propio,
  abierto desde Participantes. Se mantienen el tablero de variante C, la cabecera alineada
  (AC-14), la ficha con chips y el chip de la ficha del lugar (AC-13). La card de solicitudes
  sigue mostrando estado vacío (AC-03).
- **Alternativas descartadas:** (B) edición en `/plans/{id}/settings` — esa página es de permisos,
  requeriría backend nuevo y saca al usuario del contexto del plan; (C) accordion en línea para
  todo — cambio mínimo pero mucho menor ahorro de scroll.
- **Reordenar sin mover el DOM.** El orden mobile se logra con utilities `order` (más un `order`
  interno entre mapa y recorrido); el DOM queda como hoy, y por eso los `substring()` de
  `PlanControllerIntegrationTest` (`plan-summary` → siguiente `<section>`; `participants-heading` →
  "Editar plan") siguen cortando regiones válidas.
- **Literales que no cambian** (los usan tests de permisos): "Editar plan" (título del diálogo),
  "Agregar lugar al plan" (del botón, con `th:if="${canEditPlan}"`), "Invitar a alguien" (del
  accordion, con `th:if="${canInvite}"`), las etiquetas FECHA/HORA de la ficha y el
  `<span>valor</span>` crudo dentro de los chips.
- **La ficha compacta reemplaza a la grilla de tiles** del AC-09 de 17-DIS (mismo motivo: altura).
  Al implementar, 17-DIS se anota con la actualización al estilo de "Actualización 24-SCH" en
  `05-APL-FE.md`. Si en revisión se prefiere conservar tiles, se reducen a 4 con clamp — la meta de
  alto del AC-07 no cambia.
- **Trampas heredadas:** `.bg-white` con `!important` de Bootstrap en modo oscuro (hallazgo de
  17-DIS) — verificar ambos temas; la hora/fecha de las tarjetas del recorrido es dato de parada,
  no un duplicado; el modal "Ver más" y su a11y son de 17-DIS y no se tocan.
- **Fuera de alcance:** migrar Bootstrap del detail a Tailwind (deuda aparte, decisión de 17-DIS),
  backend/permisos (`canEditPlan`, `canInvite`, `canHandleRequests`), `/plans/{id}/settings`
  (es de permisos), el flujo de clonado, la lógica de solicitudes/invitaciones y la configuración
  del mapa (Leaflet queda intacto).

## Notas de implementación (fase verde, 2026-10-28)

- **[STD-001..006]** Todo el cambio vive en `templates/pages/plans/detail.html` + `plans-detail.js`:
  - **Ficha:** 4 × `.plan-chip` (label chico sobre valor, `background: var(--bs-secondary-bg)` para
    que el modo oscuro no pida reglas extra) y `#plan-description` con
    `-webkit-line-clamp: 3` (`.plan-description-clamp`, el valor que clava el E2E vía
    `getComputedStyle`). El label va en `<small>` — no es capricho: `DetailPlanCloneE2E` lee el
    código con `small:has-text('CÓDIGO') + span` y con `<span>` en el label el selector se rompía.
    El tile ADMINISTRADOR se fue del todo; el badge "Dueño" de Participantes es la fuente.
  - **Edición:** `#plan-edit-dialog` conserva el mismo form y el mismo POST; `ref="editDialog"` +
    `openEdit()` replican la mecánica de `bannedParticipantsDialog`. El trigger es
    `#plan-summary button:has-text('Editar')` (texto exacto "Editar") y el literal completo
    vive solo en el `<h5 id="plan-edit-title">`. El diálogo sigue **inmediatamente después de
    Participantes** en el DOM porque los `substring()` de integración cortan de
    `participants-heading` hasta ese literal. **Trampa:** los comentarios HTML se sirven crudos —
    escribir "Editar plan" en un comentario de este template rompe los `not(containsString(...))`
    de integración y desordena los `substring()` (el comentario del trigger no lo contiene a
    propósito).
  - **Solicitudes:** badge ancla en la fila de cabecera y card con
    `th:if="${canHandleRequests and !#lists.isEmpty(joinRequests)}"`: con la lista vacía no hay
    card, no hay mensaje de vacío y no hay ancla (el heading desaparece).
  - **Invitar / agregar lugar:** `inviteOpen` y `addPlaceOpen` (refs Vue, arrancan en `false`) con
    `aria-expanded` + `aria-controls`; ambos paneles llevan además `th:if` de permiso, así la
    vista anónima no sirve markup de edición. El picker pasó a la cabecera de "Tu recorrido" y
    quedó colapsado: la card propia ya no existe.
  - **Orden mobile sin mover el DOM:** `.itinerary-col` y `.plan-cols` hacen `display: contents`
    en `max-width: 767.98px` y cada hijo promovido gana `order` (ficha 1 → recorrido 2 → mapa 3 →
    solicitudes 4 → participantes 5). Detalle que no es cosmético: los promovidos dejan de
    calificar como `.row > *`, así que el CSS les devuelve a mano el `width: 100%`, el gutter y el
    `margin-top` que Bootstrap les daba. En desktop no se toca nada: los wrappers vuelven a ser
    columnas y la composición (mapa+recorrido a la izquierda, ficha+participantes a la derecha) es
    la de siempre.
- **Presupuestos (V-07 / AC-07) medidos sobre un plan con datos** (capturas del 2026-10-28):
  desktop 1440×900 → **1.174 px** (techo 1.500; el baseline era 2.121-2.145) y 390 → **1.873 px**
  (techo 2.800; baseline 3.589). Orden mobile medido: ficha y=332 → recorrido y=628 → mapa
  y=1116 → participantes y=1689. Los dos temas verificados (claro/oscuro, desktop/móvil, diálogo
  abierto): los chips heredan `--bs-*` y no dejaron manchas blancas.
- **Variante C — mediciones (2026-11-03, script `shot.js` sobre la app dev):** desktop 1440×900 →
  **900 px** de `scrollHeight` (el tablero ocupa el viewport; techo 1.500) y 390×844 → **1.815 px**
  (techo 2.800). Orden mobile medido (y) tras el intercambio del 2026-11-04: ficha 292 → mapa 588
  → participantes 924 → recorrido 1.220. En desktop las tres columnas miden 340 / 612 / 360 px y
  la ficha (280 px de alto) queda quieta mientras scrollea la columna de participantes o el
  recorrido. Los dos temas verificados con capturas (desktop y 390): las cards usan `--bs-*`; en
  oscuro los `btn-outline-secondary` del tablero se aclaran al color de texto porque heredaban
  `--bs-secondary` y quedaban casi ilegibles.
- **Ficha del lugar estandarizada (AC-13, 2026-11-04):** el `<dialog>` nativo salía pegado al
  borde superior izquierdo porque el preflight de Tailwind (`*, ::before, ::after { margin: 0 }`)
  pisa el `margin: auto` del user-agent: medido, `leftGap 0 / rightGap 720`, radio 6 px y bloques
  del `dl` sin superficie. Los tres diálogos pasaron a `.plan-modal` (margin auto, radio 16,
  padding 24, sombra y backdrop) y los `.place-fact` a chips (`--bs-secondary-bg`, radio 10, las
  descripciones a todo el ancho). Medición final: desktop 1440×900 → 440/440 de aire, 560×634;
  390×844 → 19/19, 352×726. El chip de categoría ahora repite label en español + icono de la card
  del recorrido ("Restaurante", no `RESTAURANT`).
- **Estáticos con revalidación (2026-11-05):** los `@click` nuevos vivían en `plans-detail.js` y la
  plantilla sí se servía fresca, pero el módulo no tenía `Cache-Control`: el navegador le aplicaba
  frescura heurística y podía usar el JS anterior contra el HTML nuevo, con lo que los botones
  "Ver" de Solicitudes y Participantes no hacían nada. `BaseWebConfig` ahora manda
  `Cache-Control: no-cache` en `/css/**`, `/js/**`, `/images/**` y `/manifest.json`, y
  `BrandingPagesTest` fija la cabecera. no-cache no apaga la caché: revalida y responde 304.
- **Nota de entorno (no es de la card):** `PublicPlansRankingE2E.T_TOP_001_elPlanCalificadoLideraElFeed`
  falla en el paso 3 del modal de crear plan de explore (`#plan-public` interceptado por el track
  del carrusel, timeout de 30 s). Se reprodujo con y sin los cambios de esta card (stash sobre
  `HEAD`), así que es previo y ajeno a 25-STD.
- **Tests existentes migrados en la fase verde:** `PlanPlaceDetailE2E.addsPersistsAndDisplaysVisitDetails`
  y los dos `makePlanPublic` abren el diálogo con `PlansPage.openEditDialog()` antes de tocar los
  inputs; `PlanParticipantsE2E` despliega el accordion antes de `#invitation-email`.
- **Comentarios de plantilla servidos crudos:** los `substring()` de integración buscan literales
  en el HTML, y los comentarios `<!-- -->` viajan tal cual al cliente. En la variante C un
  comentario nuevo mencionaba dos de esas anclas y movió el recorte de participantes: los 4 tests
  de integración fallaron con la sección vacía. Pasó de nuevo con AC-13: el comentario de
  `.plan-modal` decía `"Editar plan"` y `participantsSection()` cortaba desde `participants-heading`
  hasta esa primera aparición (antes del heading) → `substring` invertido. La regla que ya estaba
  anotada en el template se reescribió: los comentarios no repiten los literales ancla.
- **Nota de entorno (no es de la card):** `PlacesViewE2E.shouldShowDetailsFromMarkerWithoutLeavingPlacesPage`
  fallaba en local por el orden **físico** de la tabla `places` en `valhalla_e2e`: `findAll()`
  sin `ORDER BY` devuelve el orden de heap, y "Estadio Monumental" (noroeste, fuera de la vista
  fija del explore a zoom 13) quedó en la ranura (0,14) → era el `.first()` del test. El CI nunca
  lo vio porque su volumen de Postgres nace vacío y el seeder inserta en orden de catálogo.
  `TRUNCATE places CASCADE` + el arranque siguiente del Jetty resembraron `(0,1) El Sanjuanino…`
  y el test pasó. No había código roto.

## Plan de entrega

Items de la card (se van tildando con su commit, `(commit <hash>)` al landing) — **cerrados
2026-10-28**:

- [STD-001] Ficha compacta: chips (fecha · hora · visibilidad · código), clamp de descripción y
  tile ADMINISTRADOR fuera (AC-01). ✔ (commit e28b3e8)
- [STD-002] "Editar plan" → `<dialog>` con a11y + botón "Editar" en la ficha (AC-02). ✔ (commit e28b3e8)
- [STD-003] Solicitudes: badge en la cabecera y card solo con ≥ 1 (AC-03). ✔ (commit e28b3e8)
- [STD-004] "Invitar a alguien" → accordion dentro de Participantes (AC-04). ✔ (commit e28b3e8)
- [STD-005] "Agregar lugar al plan" → botón en la cabecera de "Tu recorrido" (AC-05). ✔ (commit e28b3e8)
- [STD-006] Orden mobile con `order`, sin mover el DOM (AC-06) y techos de scroll (AC-07). ✔ (commit e28b3e8)
- [STD-007] Spec y docs: nota de actualización en 17-DIS (AC-09) + hand-off de la card. ✔ (este commit)
- [STD-008] **Variante B (descartada el 2026-11-03):** mapa primero a todo el ancho + cards como
  pestañas exclusivas sobre el mapa estilo explore. No se commiteó: la reemplaza la variante C
  (AC-08 supersedido).
- [STD-009] Cabecera sin "Más opciones": título de banda a ~1.35 rem y banda más baja (AC-09). ✔
  (commit 5834257)
- [STD-010] "Ver miembros baneados" → botón dentro de Participantes con el mismo diálogo,
  `aria-controls` y retorno de foco; el ancla de solicitudes deja de abrir pestañas (AC-09). ✔
  (commit 5834257)
- [STD-011] **Variante C:** tablero de tres columnas (ficha fija + Participantes a la izquierda,
  mapa al centro, "Tu recorrido" a la derecha) con stage de altura viewport y scroll propio por
  columna; en mobile apilado ficha → mapa → solicitudes → participantes → recorrido (AC-10). Se
  van las pestañas y la barra de la variante B. ✔ (commit 874bd24; intercambio recorrido ↔
  participantes el 2026-11-04)
- [STD-012] Acciones de cada card en fila propia al pie para columnas angostas (AC-11). ✔
  (commit 874bd24)
- [STD-013] Claro y oscuro con variables `--bs-*` (AC-12) y capturas de ambos temas. ✔
  (commit 874bd24)
- [STD-014] **Ficha del lugar estandarizada (AC-13):** clase compartida `.plan-modal` para los
  tres diálogos del detalle (ficha del lugar, "Editar plan" y baneados) con el chrome de las cards
  —`margin: auto` para vencer el reset de Tailwind, radio 16, `padding` 24, superficie y sombra,
  backdrop—, y los `.place-fact` como chips (`--bs-secondary-bg`, radio 10, sin líneas). ✔
  (commit 874bd24)
- [STD-015] **Cabecera alineada al tablero (AC-14):** `.plan-toolbar` reusa las tres columnas del
  stage, la banda ocupa las dos primeras y las acciones la tercera; "Configuración del plan" pasa a
  "Configuración" y las dos acciones comparten alto, con el borrado en outline.
- [STD-016] **Intercambio Participantes ↔ "Tu recorrido" (AC-10, 2026-11-04):** la columna
  izquierda pasa a ficha + Participantes y el recorrido a la derecha; en mobile el orden queda
  ficha → mapa → solicitudes → participantes → recorrido. Solo cambian las áreas de la grilla (el
  DOM no se mueve). ✔ (commit 219e97a)
- [STD-017] **Solicitudes y Participantes como cards resumen (AC-03/AC-04/AC-10, pedido 2026-11-05):**
  la columna izquierda no scrollea sus cards; cada una muestra el vistazo y abre su contenido
  completo en una card centrada (`#join-requests-dialog`, `#participants-dialog`). Solicitudes se
  destaca con "Nueva" cuando hay pedidos y comparte el disparador con el badge de la cabecera. ✔
  (commit 64433a4)
- [STD-018] **Estáticos con `Cache-Control: no-cache`:** un módulo JS viejo en caché dejaba sin
  handler los `@click` nuevos (botones "Ver" de las cards resumen). Revalida con 304; test en
  `BrandingPagesTest`. ✔ (commit 5a807ad)
- [STD-019] **Red nativa para abrir y cerrar las cards:** `no-cache` no destraba una respuesta que
  el navegador ya tiene guardada, así que el disparador ("Ver", cabecera, "Editar", baneados)
  lleva `data-dialog` y la X `data-close-dialog`, resueltos por un listener clásico en la
  plantilla. Un `plans-detail.js` viejo pierde el `@click` (Vue lo compila como
  `_ctx.openParticipants && ...` y no falla ni avisa), pero la card abre igual porque el markup
  siempre llega fresco. Con el módulo al día el `@click` abre primero y `!dialog.open` evita el
  `showModal` duplicado. Verificado sirviendo a propósito un módulo sin los dos handlers. ✔
  (commit fff9909)
- [T-STD-004] E2E: `opensThePlaceCardAsACenteredModalLikeTheRestOfTheCards` en verde. ✔
  (commit 5834257 — PlanPlaceDetailE2E 8/8)
- [T-STD-003] E2E: `laysOutTheFichaFixedOnTheLeftWithParticipantsUnderItAndTheItineraryOnTheRight`
  en verde (AC-10, AC-14 y techos) y la suite de detalle sin `openCard` (variante C sin pestañas). ✔
  (commit 5834257; renombrado e intercambio en 219e97a)
- [T-STD-001] E2E: los tres métodos de "### E2E" en verde. ✔ (commit e28b3e8 — PlanPlaceDetailE2E 7/7)
  → en variante B: `deploysTheCardsAsExclusiveTabsOverTheMap` reemplaza al de orden.
- [T-STD-002] Gate: `gate.ps1 all` con los counts al cierre. ✔ (surefire 1035/0/0 tras el merge de
  `main` con el calendario; 998 en la rama sola · E2E 88/0/1 · checkstyle 0 · prettier/PMD/CPD ·
  `gate js -Strict` 0 FAIL; el único error E2E es `PublicPlansRankingE2E`, previo y ajeno — falla
  igual en `HEAD`)

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `templates/pages/plans/detail.html` | Actualizar (tablero de tres columnas: ficha con chips + "Editar" al pie, recorrido con acciones en su fila, mapa central y participantes con acciones bajo el nombre; `<dialog>` "Editar plan" después de Participantes y antes de `banned-participants`; badge de solicitudes en la cabecera y card solo con ≥ 1; accordion "Invitar a alguien" dentro de Participantes; botón "Agregar lugar al plan" en la cabecera de "Tu recorrido"; mobile apilado con `grid-template-areas`) |
| `resources/core/js/pages/plans-detail.js` | Actualizar (refs del diálogo de edición: abrir con foco y cerrar con Escape, misma mecánica que `bannedParticipantsDialog`; toggles del accordion y del picker de lugares con los refs Vue que hagan falta) |
| `resources/css/brand.css` (o el `<style>` de `detail.html`) | Actualizar/crear (chips, badge, estados del accordion, clamp de la descripción, estilos del diálogo — claro y oscuro) |
| `test/java/com/valhalla/e2e/PlanPlaceDetailE2E.java` | Actualizar (4 métodos nuevos + abrir el modal en `addsPersistsAndDisplaysVisitDetails`) |
| `test/java/com/valhalla/e2e/views/PlansPage.java` | Actualizar (helpers idempotentes `openPlanConfig()`, `openParticipants()` y `openRequests()` para los tests que tocan las cards centradas, también tras cada reload) |
| `test/java/com/valhalla/e2e/PlanParticipantsE2E.java` | Actualizar (expandir el accordion antes de `#invitation-email`) |
| `test/java/com/valhalla/e2e/PublicPlanCloneE2E.java` | Actualizar (`makePlanPublic` abre el modal antes de `#isPublic`) |
| `test/java/com/valhalla/e2e/PublicPlanViewE2E.java` | Actualizar (`makePlanPublic` abre el modal antes de `#isPublic`) |
| `test/java/com/valhalla/e2e/PlanScheduleE2E.java` | Verificar (`hasValue` con el diálogo cerrado; abrir solo si Playwright lo exige) |
| `docs/specs/17-DIS.md` | Actualizar (nota de actualización AC-09 → ficha en chips, al implementar) |

No se tocan controladores, servicios, repos, SecurityConfig, datos de semilla, ni ningún test de
unidad, integración o seguridad (los de integración se **verifican** en el gate sin editar). No
hay backend nuevo: toda la inteligencia de la card es markup, estilos, un poco de JS de presentación
y tests.
