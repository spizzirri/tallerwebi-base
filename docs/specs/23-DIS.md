# [DIS] Iconografía y pulido del feed

> Trello: (link pendiente)
> **Estimacion:** 2 pts Fibonacci
> **Origen:** relevamiento de bugs 2026-10-22
> **Estado (2026-10-23):** rama `23-DIS` creada desde `main` (788eaaa). Implementacion completa.
> Pendiente: revision visual (T-DIS-05) y merge.

## Objetivo

Que las acciones del sitio se reconozcan de un vistazo: cada botón de acción lleva un icono de un
único set, con el mismo trazo, el mismo tamaño y la misma accesibilidad. El set existe a medias
hoy —el menú del navbar, el toggle de Guardados y los controles del navbar mobile ya lo usan—,
pero la mayoría de los CTAs (feed, vista pública, detalle del plan, listado y perfil) es texto
pelado. La segunda mitad de la card es un fix de layout del feed: "Por {autor}" se parte en dos
renglones cuando el nombre no entra.

Sin backend, sin DB y sin JavaScript nuevo: los iconos se dibujan server-side.

## Pre-requisitos

- [BRD] completed: tokens de identidad en `resources/css/brand.css` y el set vendorizado de
  Lucide (`resources/core/js/lucide.min.js`, sin CDN).
- [PPV] y [MOBILE] en main: el feed `/for-you`, el menú del navbar y el responsive son el
  terreno de la card.
- El patrón de icono de acción ya está en producción: `components/bookmark-button.html` (outline
  + filled + `aria-label`) y `components/navbar-menu.html` (items con svg `h-4 w-4`). Esta card
  extiende ese set; no inventa uno nuevo.
- No toca backend ni DB: no hay tests de integración ni seguridad nuevos, ni migraciones.

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | Cada acción del inventario (Crear, Buscar, Unirse, Clonar, Abrir/Ver, Quitar/Eliminar, Configuración, Copiar link, Aceptar/Rechazar, Guardar) lleva un icono SVG inline del set Lucide, con `stroke="currentColor"`, `aria-hidden="true"` y tamaño fijo (`h-4 w-4`) |
| AC-02 | El set es outline por defecto y filled solo para estado activo (el toggle de Guardados, que ya existía): un solo glyph por acción, `stroke-width="2"` en todo el set |
| AC-03 | Los botones con texto conservan el texto como nombre accesible (el icono va `aria-hidden`); los botones de solo icono mantienen `aria-label` + `title` + `sr-only` (Guardados, hamburguesa, tema) |
| AC-04 | "Por {autor}" del feed `/for-you` renderiza en una sola línea, con el nombre truncado accesiblemente (`title` con el nombre completo); para que el link del autor pueda existir, el link de la card pasa a ser un overlay (HTML inválido: `<a>` anidado dentro de `<a>`); el mismo fix de una línea aplica al bloque de Guardados del perfil, que comparte payload |
| AC-05 | Cero JS nuevo: ningún archivo de la card agrega runtime de Lucide; los iconos viajan en el HTML server-side |
| AC-06 | La puerta completa queda verde (`pwsh -File scripts/gate.ps1 all`) |

## Escenarios de Test

### Tests Unitarios (`presentation/brand/ActionIconsTest.java`)

Lee los templates como texto, igual que `BrandTokensTest`: las garantías son sobre lo que el
markup declara, no sobre lo que un controller devuelve.

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | `everyActionCarriesItsIconFromTheSameSet` | AC-01 |
| U-02 | `theSetIsOutlinedWithCurrentColorAndAFixedSize` | AC-01, AC-02 |
| U-03 | `everyControlKeepsItsAccessibleName` | AC-03 |
| U-04 | `theFeedAuthorLineStaysOnOneLineWithAccessibleTruncation` | AC-04 |
| U-05 | `theNewIconsShipAsInlineSvgWithoutTheLucideRuntime` | AC-05 |

### E2E (`e2e/PublicPlansListE2E.java`, 1 test nuevo)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `T_DIS_04_porYAutorCompartenLineaEnElFeed` | Una usuaria registrada (con nombre) publica un plan; otra usuaria ve el feed: "Por" y el link del autor comparten línea, el link trae `title` con el nombre completo y `text-overflow: ellipsis` computado | AC-04 |

### Checks visuales (V-XX, manuales — checklist T-DIS-05)

| # | Check | AC que cubre |
|---|-------|-------------|
| V-01 | Feed `/for-you` a 320 px y 1280 px: iconos alineados al texto, sin salto de layout en los CTAs | AC-01 |
| V-02 | Detalle del plan, vista pública, Mis planes y perfil: mismo set, mismo tamaño, mismo trazo | AC-01 |
| V-03 | Modo oscuro: los iconos heredan el color del botón (`currentColor`) sin reglas propias | AC-01 |
| V-04 | Nombres de autor largos en el feed: una sola línea con elipsis, y el `title` muestra el nombre completo | AC-04 |

### Puerta

El cierre de la card es el gate completo en verde con los counts escritos en el item
(`pwsh -File scripts/gate.ps1 all`, unit+integration y E2E).

## Notas / decisiones de diseño

- **SVG inline, no `createIcons()`.** La casa ya dibuja sus iconos de acción como SVG inline
  (bookmark-button, navbar-menu, hamburguesa, sol/luna): es el patrón con identidad vigente.
  Elegirlo para todo el set (a) agrega cero JavaScript y cero dependencia por vista —
  `createIcons()` solo corre donde alguien lo invoca, y el fallo silencioso de un
  `<i data-lucide>` sin runtime es exactamente el bug de íconos vacíos que documenta 17-DIS—,
  (b) funciona igual dentro del template Vue del explorador, y (c) no parpadea: el icono viaja
  en el HTML. Los paths son los de Lucide (mismo set vendorizado), no glyphs nuevos. Dos
  vistas ya cargaban `lucide.min.js` de antes de esta card para sus módulos JS (detalle y
  explorador); la card no agrega ni una invocación nueva — U-05 fija que los iconos de acción
  no usan `data-lucide`.
- **Outline/filled es un solo glyph.** Lucide es outline por diseño (stroke). El estado filled
  se logra con el mismo path en `fill="currentColor"` y sin stroke, como ya hace el bookmark:
  dos estados, un set, ninguna librería extra.
- **`currentColor` y tamaño fijo, sin excepciones.** El icono hereda el color del botón (blanco
  del primario, rojo del peligro, gray del outline) sin clases propias de color: así el set
  sobrevive al modo oscuro y convive con los dos stacks (Tailwind y Bootstrap) sin una regla
  por página. `h-4 w-4` (16 px) en toda acción con texto —el texto manda el alto del botón—;
  los controles de solo icono ya tenían su medida y no se tocan.
- **El nombre accesible lo pone el texto.** Un botón "Eliminar plan" no necesita `aria-label`:
  el texto ya es el nombre accesible y el svg va `aria-hidden`. Los de solo icono (Guardados,
  hamburguesa, tema) sí llevan `aria-label` + `title` + `sr-only`; ese contrato existía y
  AC-03 lo vuelve explícito para el set entero.
- **"Por {autor}" es un flex, no un wrap.** El `<p>` pasa a fila (`flex items-baseline gap-1`),
  "Por" no encoge (`shrink-0`) y el link trunca (`truncate` = ellipsis + nowrap). El `title` del
  link lleva el nombre completo: hover para mouse y texto completo en el DOM (la elipsis es
  visual; lector de pantalla lee el nombre entero). El bloque de Guardados del perfil comparte
  payload con el feed y el mismo defecto: se arregla en la misma pasada.
- **La card se linkea con overlay, no con `<a>` anidado.** El feed envolvía la card entera en
  `<a href=".../public">` y el link del autor vivía adentro: `<a>` dentro de `<a>` es HTML
  inválido y el parser lo resolvía partiendo el link de la card en fragmentos y metiendo un clon
  dentro del párrafo del autor (el E2E lo delató como locador ambiguo de 2 elementos). Ahora el
  destino es un `<a class="absolute inset-0 z-0">` hermano —mismo href, nombre accesible
  `sr-only` con el nombre del plan— y lo que es interactivo de verdad —Guardados, el autor y la
  fila de acciones Clonar— queda por encima con `z-10` (la fila de acciones también: sin eso el
  overlay le roba el click y el E2E de clonar del feed lo detecta). Card entera clicable, HTML
  válido, y los tests previos no cambian: el primer `<a>` de la card sigue siendo el de la vista
  pública.
- **No choca con 17-DIS (estandarizar diseños).** Esa spec propone `.btn-cta` para unificar los
  CTAs: cuando se implemente, los iconos quedan dentro de la misma clase (`inline-flex` ya es
  parte del diseño de `.btn-cta`) y no hay retoque posterior.
- **Inventario acotado a acciones.** Los links de navegación del navbar (Para vos, Explore,
  Plans) quedan texto: son etiquetas de destino, no acciones, y el navbar ya tiene su lenguaje
  (pills sobre negro; iconos solo en hamburguesa, tema y menú). La acción "Salir" vive en el
  menú del navbar, que ya tiene su icono `log-out`.
- **El explorador (Vue) suma el plus a "Create plan" y nada más.** Su toggle de Guardados ya era
  del set; el panel lateral está en inglés y su icono es neutro al idioma.

## Referencia de Implementacion

> Los pasos a continuacion son guia de implementacion, no reemplazan los criterios de aceptacion de arriba.

### 1. El icono-texto (AC-01)

```html
<button type="submit"
  class="inline-flex cursor-pointer items-center gap-2 rounded-md bg-primary px-4 py-2 font-medium text-white transition hover:bg-secondary">
  <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
    stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
    <rect width="14" height="14" x="8" y="8" rx="2" ry="2" />
    <path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2" />
  </svg>
  Clonar Plan
</button>
```

En `detail.html` (stack Bootstrap) la misma pieza con las utilidades del stack:
`class="btn btn-outline-primary d-inline-flex align-items-center gap-2"`.

### 2. Tabla de iconos por acción

| Acción | Glyph | Dónde |
|--------|-------|-------|
| Crear plan | plus | feed, Mis planes, explorador (Vue) |
| Buscar | search | feed |
| Unirse al plan | user-plus | vista pública, Mis planes |
| Clonar | copy | feed, vista pública, detalle, perfil (Guardados) |
| Abrir / Ver | eye | vista pública ("Ver mi plan"), Mis planes ("Ver detalle"), perfil ("Abrir"), feed ("Ver todos los planes") |
| Quitar / Eliminar | trash-2 | detalle ("Eliminar plan"), Mis planes, perfil ("Quitar") |
| Configuración | settings | detalle |
| Copiar link | link | detalle (sección Compartir) |
| Aceptar / Rechazar | check / x | Mis planes (invitaciones pendientes) |
| Guardar | save | detalle ("Guardar cambios"), perfil, contraseña, configuración del plan |
| Salir | log-out | menú del navbar (ya existía) |
| Guardar en Guardados | bookmark outline/filled | ya existía (bookmark-button, explorador) |

### 3. Autor en una línea (AC-04)

```html
<p th:if="${!#strings.isEmpty(plan.administratorName)}" data-testid="plan-card-author"
  class="flex min-w-0 items-baseline gap-1">
  <span class="shrink-0">Por</span>
  <a th:href="@{/profile/{id}(id=${plan.administratorId})}"
    class="relative z-10 truncate text-blue-600 hover:underline"
    th:title="${plan.administratorName}"
    th:text="${plan.administratorName}">Administrador</a>
</p>
```

### 4. Link de la card como overlay (AC-04)

```html
<a th:href="@{/plans/{id}/public(id=${plan.id})}" class="absolute inset-0 z-0">
  <span class="sr-only" th:text="${plan.name}">Ver plan</span>
</a>
<div class="flex flex-1 flex-col">
  <img ... />
  <div class="flex flex-1 flex-col p-5">… meta con el link del autor (relative z-10) …</div>
</div>
```

## Archivos

| Archivo | Accion |
|---------|--------|
| `templates/pages/plans/public-list.html` | Actualizar (iconos de acción del feed; autor en una línea) |
| `templates/pages/plans/public.html` | Actualizar (iconos) |
| `templates/pages/plans/detail.html` | Actualizar (iconos, stack Bootstrap) |
| `templates/pages/plans/list.html` | Actualizar (iconos) |
| `templates/pages/plans/settings.html` | Actualizar (icono save) |
| `templates/pages/profile/edit.html` | Actualizar (iconos; autor de Guardados en una línea) |
| `templates/pages/profile/change-password.html` | Actualizar (icono save) |
| `templates/pages/places/list.html` | Actualizar (plus en "Create plan") |
| `test/java/com/valhalla/presentation/brand/ActionIconsTest.java` | Crear (contrato de markup del set) |
| `test/java/com/valhalla/e2e/PublicPlansListE2E.java` | Actualizar (E-01: autor en una línea) |

No se tocan: controladores, servicios, repos, SecurityConfig, datos de semilla, `brand.css` ni
el bundle de Lucide.
