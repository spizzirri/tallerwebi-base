# [MOBILE] Responsive layouts/design for all views/pages

> Trello: card **[MOBILE] Responsive layouts/design for all views/pages** (link pendiente)
> **Estado (2026-10-22):** implementada en la rama `22-MOBILE` (desde `cde9955`, incluye el
> rediseño del explorador con pestañas). Gates en verde: 825 unit/integration + 66 E2E, 0
> fallos, checkstyle/PMD/prettier limpios, cobertura TOTAL 99.4%.

## Objetivo

Que **todas** las vistas de PlanIt sean usables en un celular angosto (320–390 px) sin scroll
horizontal, con targets tocables y una jerarquía visual clara. Cero JS nuevo como principio
(AC-08): la responsive se resuelve con clases Tailwind/Bootstrap, media queries en el propio
template y los toggles de menú ya existentes. **Desktop queda pixel-idéntico**: las clases
responsivas se aplican con prefijos `md:` (≥768 px) o en breakpoints por debajo del mínimo de
escritorio.

## Pre-requisitos

- Layout base `layouts/base :: head` exponiendo los tokens de marca a Tailwind (bloque `@theme`).
- `data-menu-toggle` + `navbar-menu.js` existentes (hamburguesa se reutiliza, no se reinventa).
- Playwright E2E con login dev (`test@unlam.edu.ar` / `password`) y Docker `valhalla-postgres`.

## Criterios de Aceptacion

- **AC-01:** el navbar es usable a 320–390 px: los links fluyen con `flex-wrap` a su propia fila
  y no fuerzan scroll horizontal.
- **AC-02:** ninguna vista tiene scroll **horizontal** del documento a ≥320 px; las tablas
  anchas (`/plans`, `/admin/users`, `/admin/places`) scrollean **en su propio contenedor**
  (`overflow-x-auto`) sin romper el layout.
- **AC-03:** `/explore` en mobile es **mapa a pantalla completa** con una barra de pestañas
  Places/Plans abajo que despliega cada panel; `/admin/place-import` pasa a columna única.
- **AC-04:** el detalle del plan es legible a **320 px** (ancho de los modelos viejos): el título
  ocupa un ancho razonable (≥60 px medidos), las acciones tienen su propia fila y el itinerario
  no se corta.
- **AC-05:** en mobile los paneles del explorador se despliegan como **pestañas** desde la
  barra inferior (Places ≤40 vh, Plans ≤35 vh; el formulario de creación, 70 vh con
  `overflow-y-auto`), con **cruz de cierre**; el detalle de un lugar aparece como **card
  superpuesta centrada** (márgenes a los costados, esquinas redondeadas, scroll interno),
  no edge-to-edge.
- **AC-06:** el target de la hamburguesa cumple **≥44×44 px** (umbrales Apple/Google de touch).
- **AC-07:** los filtros viven dentro del panel de la pestaña Places (≤40 vh, scrolleable) y el
  mapa conserva siempre su franja visible detrás; sin tab activo, el mapa queda limpio.
- **AC-08:** **cero JS nuevo**: se reutiliza `data-menu-toggle`; las ediciones mínimas a JS
  existente se limitan a guards defensivos (ver "Notas / decisiones de diseño").

## Escenarios de Test

### Tests Unitarios de plantilla (`presentation/mobile/MobileTemplatesTest.java`)

- **T-MOB-001 — guard de plantillas responsive:** cada vista de `pages/**` declara tokens
  responsive (`sm:|md:|lg:|xl:`), `overflow-x-auto|flex-wrap|flex-col` según su caso, y `base`
  expone el viewport meta. Falla en cuanto aparezca una vista nueva sin decisión responsive.

### E2E (`com.valhalla.e2e.MobileLayoutE2E`)

Se corren a **390×844** (iPhone 14) salvo T-MOB-008 (320 px). Helper `awaitStyles()` (espera a
que Tailwind compile en el browser, umbral >2000 chars igual al reveal de base) para evitar
carreras al medir boxes.

- **T-MOB-002 — feed en una columna en phone, tres en desktop** (una sola corrida, ambas
  medidas).
- **T-MOB-003 — login en phone:** sin scroll horizontal, inputs con ancho de teclado usable
  (≥16 px de font-size, sin zoom de iOS).
- **T-MOB-004 — explorador en phone:** al cargar, el mapa llena la pantalla y ambos paneles
  están ocultos; la tab Places despliega filtros+listado (≤40 vh, sentado sobre la barra de
  tabs) y la tab Plans intercambia el panel (≤35 vh) —mutuamente excluyentes—; las cruzes
  cierran cualquiera de los dos; el detalle abre como card centrada con márgenes; en desktop
  los paneles vuelven visibles sin tocar ninguna tab.
- **T-MOB-005 — tablas del admin en phone:** scrolleable en su contenedor; el resto de la vista
  sin desborde; una fila de datos visible.
- **T-MOB-006 — navbar:** anónimo **no** tiene hamburguesa (pinned por `PublicPlansListE2E`,
  que además pinea el orden de texto `PlanIt, Para vos, Register, Login, Cambiar tema`);
  autenticado la tiene con ≥44 px, el menú se abre con los mismos destinos + toggle de tema, y
  el toggle de tema anónimo **comparte fila con el logo** (no huérfano en una tercera fila).
- **T-MOB-008 — detalle del plan a 320 px:** sin scroll horizontal, el título del plan mide
  ≥60 px de ancho (catch del bug de letras-sueltas por `overflow-wrap: anywhere` + paddings
  encadenados), acciones y datos del itinerario visibles. (La numeración salta el 007: esa
  verificación quedó cubierta por T-MOB-001 y T-MOB-006.)

## Notas / decisiones de diseño

- **Hamburguesa solo autenticada.** El navbar anónimo (3 links) fluye con `flex-wrap`: un E2E
  de PPV pinea que el navbar anónimo **no** tiene `#navbar-menu-btn`, y sumarle la hamburguesa
  rompía ese contrato sin ganancia real. En desktop el navbar anónimo muestra todo en una fila.
- **Toggle de tema anónimo como hijo directo del `<nav>`.** Vivió dentro del bloque de links y
  a 296–390 px el bloque envolvía a una tercera fila con el toggle suelto. Pasa a hijo directo
  del nav, **después** de los links (el orden de texto que pinnegan los helpers E2E no cambia),
  con `order-last md:order-none`: en mobile queda en la fila del logo; en desktop,
  `md:ml-auto` del bloque de links lo devuelve al cluster derecho.
- **Touch targets:** ≥44 px solo en la hamburguesa (AC-06 literal de la card); el resto de los
  controles quedó en el estándar actual (~36 px) — desviación asumida.
- **Padding inferior de la fila de links en mobile.** El nav no declara padding vertical: con
  `flex-wrap`, la última fila (links anónimos, `w-full`) tocaba el canto inferior y los pills
  azules quedaban pegados al borde con la sección siguiente, en todas las vistas. La fila gana
  `pb-3` solo en mobile (`md:pb-0` conserva el desktop pixel-idéntico).
- **Explorador: mapa arriba + pestañas Places/Plans (AC-03/05/07).** Evolución de las dos
  primeras propuestas (columna apilada; después sheet colapsado con barra de peek): el mapa
  pasa a ocupar toda el área y los paneles se despliegan desde una barra de tabs. Implementado
  con **dos radios sr-only dentro de su label** + variantes `group-has-[#tab-…:checked]` sobre
  `#app` (Tailwind v4 compilado en browser) — cero JS. Un radio no se destilda solo: las
  **cruces** de cada panel apuntan a un **tercer radio `tab-none`** del mismo grupo, que deja
  ambos paneles ocultos. Ojo con `transition-all`: `visibility` tarda lo que dura la
  transición en cambiar y los cierres se ven "colgados" — se transiciona solo
  `[opacity,transform]`.
- **Detalle de lugar = card superpuesta centrada** en mobile (`left-4 right-4`, `top-1/2` con
  `-translate-y-1/2`, `rounded-2xl`, scroll interno a ≤60 vh); en desktop sigue siendo el
  panel lateral de 320 px.
- **Paddings encadenados a ≤575 px.** Bootstrap declara `px-5` en `main.container-fluid` con
  `!important`; el fix es una media query en `plans/detail.html` que pisa el padding a 12 px y
  las acciones pasan a `flex: 1 1 100%` (fila propia). Sin eso, a 320 px el ancho útil del
  título caía a ~90 px y `overflow-wrap: anywhere` partía el título letra por letra **sin**
  desborde (por eso un `fitsOnPhone` clásico no lo detectaba).
- **`#plans-aside` ×3 variantes → bottom-sheet.** El panel `absolute inset-y-0 w-80` tapaba el
  mapa entero en mobile; ahora tiene variantes de altura (35 vh sheets, 70 vh con scroll para
  el formulario de creación de planes).
- **Guard del `map.panBy`** (edición mínima de JS, AC-08): solo se ejecuta en `innerWidth >=
  768`, porque en mobile la geometría del mapa apilado no admite el pan que evita el solape
  con el sidebar en desktop.

## Referencia de Implementacion

### 1. Navbar responsive (`components/navbar.html`)

`flex flex-wrap items-center justify-between gap-x-4 gap-y-1`. Bloque anónimo con
`order-last md:order-none md:ml-auto w-full md:w-auto` (fila propia full-width en mobile,
cluster derecho en desktop). Hamburguesa `md:hidden` con `h-11 w-11` (44 px) y
`data-menu-toggle`. Menú (`components/navbar-menu.html`): el menú modal existente con un bloque
`md:hidden` que replica los destinos (Para vos/Explore/Plans, o Users/Places si admin, + toggle
de tema).

### 2. Tablas con scroll propio

`plans/list.html`, `admin/users.html`, `admin/places.html`: la `<table>` envuelta en un div
`overflow-x-auto`; la vista no se desborda a 320 px.

### 3. Explorador con pestañas + place-import apilado

`places/list.html`: `#app` lleva la clase `group` y la barra de tabs (radios sr-only dentro de
sus labels + `tab-none`, `md:hidden`) vive al pie del contenedor del mapa; el panel de
filtros/lugares y los tres `#plans-aside` declaran sus variantes `group-has-…` con resets
`md:…` para escritorio; las cruzes de cierre son `label[for="tab-none"]`; el detalle
(`#place-details-aside`) es card centrada en mobile y panel lateral en desktop.
`admin/place-import.html`: de dos columnas a `flex-col` en mobile.

### 4. Paneles del explorador (`places/list.html`)

Los tres `#plans-aside` (descubrimiento 35 vh, plan activo 35 vh, creación 70 vh +
`overflow-y-auto`) y el panel Places (40 vh) suben/bajan con las pestañas; el detalle de lugar
es la card centrada. Las cruzes de cierre miden 44 px. Guard del `map.panBy` (solo
`innerWidth >= 768`).

### 5. Detalle de plan a 320 px (`plans/detail.html`)

Media query `@media (max-width: 575px)`: padding de `main.container-fluid` a 12 px (pisa el
`!important` de Bootstrap) y acciones a fila propia.

### 6. Guard de plantillas + infraestructura de tests

`MobileTemplatesTest` (regex por vista) y `MobileLayoutE2E` (`awaitStyles()`, screenshots de
verificación en `target/`: `navbar-anon-mobile.png`, `detail-mobile-320.png`,
`explore-mobile.png`).

## Archivos a crear/modificar

- `src/main/webapp/WEB-INF/templates/components/navbar.html` — flex-wrap, hamburguesa,
  bloque anónimo con `order`, toggle de tema como hijo directo del nav.
- `src/main/webapp/WEB-INF/templates/components/navbar-menu.html` — bloque `md:hidden` de
  destinos para el menú de la hamburguesa + toggle de tema.
- `src/main/webapp/WEB-INF/templates/pages/plans/detail.html` — media query ≤575 px.
- `src/main/webapp/WEB-INF/templates/pages/plans/list.html` — tabla con `overflow-x-auto`.
- `src/main/webapp/WEB-INF/templates/pages/places/list.html` — explorador apilado +
  bottom-sheets + guard de `panBy`.
- `src/main/webapp/WEB-INF/templates/pages/admin/users.html` — tabla con `overflow-x-auto`.
- `src/main/webapp/WEB-INF/templates/pages/admin/places.html` — tabla con `overflow-x-auto`.
- `src/main/webapp/WEB-INF/templates/pages/admin/place-import.html` — apilado en mobile.
- `src/test/java/com/valhalla/presentation/mobile/MobileTemplatesTest.java` — T-MOB-001 (nuevo).
- `src/test/java/com/valhalla/e2e/MobileLayoutE2E.java` — T-MOB-002…006, 008 (nuevo).
