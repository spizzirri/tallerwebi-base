# [MOBILE] Responsive layouts/design for all views/pages

> Trello: card **[MOBILE] Responsive layouts/design for all views/pages** (link pendiente)
> **Estado (2026-10-22):** implementada en la rama `22-MOBILE` (3 commits: `cde9955`,
> `2a715f4`, `4f3192a`). Gates en verde: 825 unit/integration + 66 E2E, 0 fallos,
> checkstyle/PMD/prettier limpios, cobertura TOTAL 99.4%.

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
- **AC-03:** `/explore` (explorador) y `/admin/place-import` pasan a **columna única** en mobile:
  filtros arriba, mapa/contenido abajo.
- **AC-04:** el detalle del plan es legible a **320 px** (ancho de los modelos viejos): el título
  ocupa un ancho razonable (≥60 px medidos), las acciones tienen su propia fila y el itinerario
  no se corta.
- **AC-05:** los paneles superpuestos del explorador (detalle de lugar y "Tus planes") se
  convierten en **bottom-sheets** en mobile (35 vh; el formulario de creación, 70 vh con
  `overflow-y-auto`).
- **AC-06:** el target de la hamburguesa cumple **≥44×44 px** (umbrales Apple/Google de touch).
- **AC-07:** los filtros del explorador en mobile son un **panel apilado** sobre el mapa
  (40 vh, scrolleable) — no un bottom-sheet.
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
- **T-MOB-004 — explorador en phone:** filtros apilados arriba (≤40 vh), mapa abajo con franja
  visible ≥100 px; el sheet de detalle cierra con botón de 44 px.
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
- **Filtros del explorador = panel apilado (AC-07)**; bottom-sheet **solo** para el detalle de
  lugar y el panel "Tus planes". Desviación respecto de una primera idea de bottom-sheet
  general: los filtros sobre el mapa son parte del flujo de una sola mano y el apilado los deja
  siempre a la vista.
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

### 3. Explorador y place-import apilados

`places/list.html` y `admin/place-import.html`: de dos columnas a `flex-col` en mobile (filtros
o formulario arriba, mapa/resultado abajo).

### 4. Bottom-sheets (`places/list.html`)

`#plans-aside` y el sheet de detalle con variantes de altura (35 vh / 70 vh + `overflow-y-auto`)
y botón de cierre ≥44 px; filtros en panel apilado de 40 vh. Guard del `map.panBy`.

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
