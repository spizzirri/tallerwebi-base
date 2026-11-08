# [DIS] Estandarizar diseños a lo largo del sitio

> Trello: https://trello.com/c/XhCwVzj7/24-dis-estandarizar-dise%C3%B1os-a-lo-largo-del-sitio
> **Estimacion:** 2 pts Fibonacci
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.
> **Estado (2026-10-25):** rama `task-17-DIS` sincronizada con `main` (merge `42e9b0d`, ya
> incluye [MOBILE] y el merge de baneo) e implementación completa (AC-01..AC-18). Gate en
> verde: 927 unit+integration, 78 E2E. Pendiente: revisión visual y merge del PR #48.

## Objetivo

Unificar el look & feel del sitio: los mismos cards, botones, íconos y comportamiento visual en la
landing y dentro de la app, sin duplicar estilos ni inventar tokens por página.

Hoy el landing y la app usan dos stacks de presentación distintos (landing en Tailwind con tokens
de `brand.css`; `/plans/{id}` en Bootstrap + CSS custom por página). El resultado: la misma entidad
—un lugar de un itinerario— se ve distinta según dónde la mires, y hay detalles que quedaron a
medio camino (íconos que no renderizan, CTAs que no coinciden, navbar que se va con el scroll).

El alcance de esta spec es **visual, no funcional**: ninguna ruta, controlador ni dato cambia.
La card puede crecer ("MORE TO COME"): los items nuevos se suman al final como `[DIS-008]+`
y esta spec se amplía con un AC nuevo por item, sin renumerar los existentes.

## Pre-requisitos

- [LAND] completed y mergeado: `pages/landing.html` existe, el itinerario del hero sale de
  `LandingController` (`heroItinerary`).
- [PPV] completed: el navbar (`components/navbar.html`) es el fragmento compartido de todo el sitio.
- Tokens de marca ya declarados: `resources/css/brand.css` es la única fuente de verdad de paleta y
  tipografía (`--primary`, `--secondary`, `--typeface-serif` = "Bree Serif"), expuestos a Tailwind
  por el `@theme` de `layouts/base :: head`.
- Íconos: librería vendorizada en `resources/core/js/lucide.min.js`, servida en `/js/**`
  (`BaseWebConfig.addResourceHandlers`). No hay CDN; un icono que no está en el bundle no se dibuja.
- No toca backend ni DB: no hay tests de unitarios ni de integración nuevos, ni migraciones, ni
  reset de esquema. *Ampliación AC-15..AC-18 (card deuda de UI): sí hay 2 métodos E2E nuevos, pero
  dentro de la clase existente `PlanPlaceDetailE2E` y pedidos por la card; ninguna clase de test
  nueva.*
- [MOBILE] mergeada en main: el responsive ≤575 px, el explorador full-screen y el guard de
  `panBy` ya viven en el sitio; los items que tocan esas zonas conviven con esas decisiones
  (ver "Interacciones con [MOBILE]" en Notas).

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | Los cards de lugar del landing (itinerario del hero "Un sábado en Recoleta") usan el mismo componente visual que las cards de lugar dentro de la app (`/plans/{id}`, `.itinerary-stop`): mismos border/radius/padding/tipografía y colores, tomados de los mismos tokens |
| AC-02 | Todos los botones CTA del sitio comparten un único estilo (padding, font weight, radius, colores, hover/active/focus), declarado una sola vez. Casos punta: el "Crear mi plan gratis" del hero del landing con el de la sección final del landing, y los botones del navbar |
| AC-03 | Cada card de Preguntas Frecuentes del landing muestra un indicador visual de que el contenido es abrible (ícono bien visible y estado abierto marcado), y el indicador funciona también sin JavaScript |
| AC-04 | Las cards de la sección "Un plan que se arma una vez y no se vuelve a discutir" del landing muestran sus íconos (clock, link, map-pin, calendar): ningún círculo de ícono queda vacío |
| AC-05 | El tag "Público" de la sección "Un sábado en Recoleta" del hero del landing renderiza su ícono |
| AC-06 | El navbar superior queda fijo durante el scroll (sticky) en landing y app, sin tapar contenido ni perder accesibilidad (el skip-link sigue siendo alcanzable al navegar con teclado) |
| AC-07 | La letra P del logo (`resources/images/logo.svg`) se traza con una tipografía cercana a la usada en el resto del sitio (la serif de `brand.css` o la sans de la app), de modo que el logo no se lea como tipografía ajena. *Revertido en revisión: se implementó (re-trace con Bree Serif) y en la revisión final la usuaria prefirió el vector original, así que el logo vuelve al anterior* |
| AC-08 | En la página de edición de un plan (`/plans/{id}`), el título del plan y los botones de acción (Eliminar plan / Salir del plan / Clonar plan) comparten la misma fila: título a la izquierda, botones a la derecha, con separación visible entre botones |
| AC-09 | Los tiles del resumen del plan (DESCRIPCIÓN, FECHA, HORA, VISIBILIDAD, ADMINISTRADOR, CÓDIGO) muestran separación entre sí (vertical y horizontal); ningún tile toca al siguiente |
| AC-10 | La visibilidad del plan en "Editar plan" se maneja con un switch estilo iOS con la paleta de la marca, que muestra el estado actual (Privado/Público) con un label claro al lado |
| AC-11 | La página de edición muestra padding inferior suficiente: al scrollear hasta el final, la última sección no toca el borde inferior de la ventana |
| AC-12 | Cada card de lugar del itinerario muestra un ícono de drag & drop (handle) en su borde izquierdo cuando el usuario puede editar el plan; sin permiso de edición el ícono no aparece |
| AC-13 | El botón "Clonar plan" solo se muestra a participantes que no son administradores: un plan recién clonado (donde el clonador quedó como administrador) no vuelve a mostrar el botón |
| AC-14 | La cabecera del plan lleva una banda de color de marca sólido detrás del título, sin taparlo ni solaparse con el contenido; subir/cambiar/quitar una imagen decorativa queda fuera de esta spec (card [IMG] aparte). *Ajustado en revisión visual: el ticket pedía imagen por defecto, se implementó y probó, y la usuaria la descartó pidiendo color sólido* |
| AC-15 | En `/plans/{id}` el navbar compartido luce como en el resto del sitio pese al reboot de Bootstrap de esa página: avatar redondo, avatar con la serif de la marca, wordmark en `text-xl`, fondo de token y el gap real de `gap-4` (1rem, no el 1.5rem de `.gap-4` de Bootstrap); y el menú móvil `#navbar-menu` (hermano del `<nav>`) con los mismos links, espaciados y radius que en el resto del sitio |
| AC-16 | El modal "Ver más" del detalle del plan es una ficha con jerarquía: título + chip de categoría arriba, y un bloque por dato (dirección, cómo llegar, descripción del lugar, descripción de la visita, fecha/hora y posición) con icono en el `dt` y su valor en el `dd` |
| AC-17 | El editor de fecha/hora del modal se muestra a quien puede editar el plan (administrador o editor) y no aparece para el resto |
| AC-18 | El diálogo del detalle declara `aria-labelledby` apuntando a su título, recibe el foco al abrirse y se cierra con Escape |

> **Actualización 2026-10-28 — 25-STD:** AC-09 y V-09 quedan **supersedidos**, y con ellos la
> decisión de "Notas / decisiones de diseño" que decía *"Los tiles del resumen no se tocan
> (AC-09)"*. La ficha pasó a ser 4 chips (fecha · hora · visibilidad · código) sobre
> `--bs-secondary-bg` con la descripción en clamp de 3 líneas: no hay celdas grises, no hay tile
> ADMINISTRADOR (el badge "Dueño" vive en Participantes) y la regla dark de `.bg-light` de esta
> vista se retiró con los tiles. Ver `docs/specs/25-STD.md`.

## Escenarios de Test

Es una spec de diseño: no hay lógica nueva, así que no hay clases de test U/I/S nuevas. La
verificación es por inspección visual más la puerta completa (excepción: el AC-13 introduce una
condición de template — quién ve "Clonar plan" — verificada por inspección con los tres estados
de sesión; y los AC-15..AC-18 suman dos métodos en la clase E2E existente `PlanPlaceDetailE2E`,
los items [T-DIS-B01] y [T-DIS-B02] de la card, listados más abajo). Ni backend ni clases de
test nuevas: esos dos tests son la verificación automatizada de AC-15..AC-18.

### Checks visuales (V-XX, manuales o asistidos por los E2E existentes)

| # | Check | AC que cubre |
|---|-------|-------------|
| V-01 | Card del hero del landing comparada a ojo con una parada de `/plans/{id}`: mismo borde, radius, padding y tipografía | AC-01 |
| V-02 | "Crear mi plan gratis" del hero del landing vs el de la sección final vs los botones del navbar: mismo padding, peso y colores calculados (getComputedStyle) | AC-02 |
| V-03 | Preguntas Frecuentes: cada summary muestra el indicador; con JS deshabilitado sigue habiendo indicador y el `<details>` abre | AC-03 |
| V-04 | Íconos de beneficios renderizan (los 4 círculos tienen un SVG adentro) | AC-04 |
| V-05 | Tag "Público" del hero: SVG dentro del tag | AC-05 |
| V-06 | Navbar: `getComputedStyle(nav).position == "sticky"`, scrolleando la landing y `/plans`; skip-link sigue operable con teclado | AC-06 |
| V-07 | Logo en navbar y favicon: la P se lee más vecina a la tipografía del sitio que al vector actual | AC-07 |
| V-08 | Header de `/plans/{id}`: título y botones en la misma fila; hay gap calculado entre "Eliminar plan" y "Clonar plan" (no se tocan) | AC-08 |
| V-09 | Tiles del resumen del plan: distancia visible entre todos, sin superposición de bordes redondeados | AC-09 |
| V-10 | Switch de visibilidad renderiza en 375px y 1280px; al alternarlo cambia el knob y el label Privado/Público | AC-10 |
| V-11 | Scrolleando al final de `/plans/{id}`, la última sección deja padding visible respecto al borde inferior de la ventana | AC-11 |
| V-12 | Handle de drag visible en el borde izquierdo de cada card de lugar con `canEditPlan`; invisible para un usuario sin permiso de edición | AC-12 |
| V-13 | Como ADMINISTRADOR del plan no se ve "Clonar plan"; como participante no-admin sí; tras clonar un plan público, la página del clon (redirect) tampoco lo muestra | AC-13 |
| V-14 | Banda de color sólido de marca detrás del título del plan (en 320, 390 y desktop, light y dark); el título en blanco mantiene contraste y no se solapa con el contenido | AC-14 |
| V-15 | `/plans/{id}` contra `/for-you` con `getComputedStyle`: avatar con radius > 1000px y serif de la marca, fondo del nav `rgb(17, 24, 39)`, gap del menú 16px, wordmark 20px; a 390px, `#navbar-menu` con links grises sin subrayado, padding 16px y logout con radius 6px | AC-15 |
| V-16 | Modal "Ver más": título + chip `cat-*`, 6 bloques con ícono (dirección, cómo llegar, descripciones, fecha/hora y posición), "Sin fecha"/"Sin hora" cuando no hay datos y la posición al final del `dl` | AC-16 |
| V-17 | Como administrador (o editor) el modal muestra los inputs de fecha y hora; como participante sin permiso no aparecen | AC-17 |
| V-18 | El diálogo abierto tiene `aria-labelledby="place-detail-title"`, `document.activeElement` queda dentro del diálogo y Escape lo cierra | AC-18 |

Los tres estados de sesión (anónimo, USER, ADMIN) en dos anchos (375px y 1280px).

### E2E

Dos tests nuevos en `e2e/PlanPlaceDetailE2E`, sobre el arranque existente (crea un plan con una
visita y abre el detalle):

- `keepsTheSharedNavbarOnThePlanDetailPage` → AC-15: lee con `getComputedStyle` el avatar
  (radius, font-family), el fondo del nav, el gap del menú desktop y el tamaño del wordmark.
- `showsTheStopDetailsAsAStructuredFichaAndClosesWithEscape` → AC-16, AC-17 y AC-18: chip de
  categoría, un icono por `dt`, los valores de la ficha, el editor de fecha/hora visible para el
  administrador del arrange, el foco dentro del diálogo al abrir y el cierre con Escape.

La suite E2E existente (`e2e/`), en particular la del landing (`LandingPage`), debe seguir en
verde sin cambios: los templates que los page objects ya tocan no cambian selects ni textos de los
helpers. Si un selector de test se rompe por el markup nuevo, se actualiza el page object, no el
test.

> Los selectores de los dos tests nuevos se anclan por `aria-labelledby=place-detail-title`, no
> por `document.querySelector('dialog')`: desde el merge de `main` hay otro `<dialog>`
> (`banned-participants`) antes en el DOM del detalle y el selector genérico lo agarra a él.

### Puerta

El cierre de la card es el gate completo en verde con los counts escritos en el item
(`pwsh -File scripts/gate.ps1 all`, unit+integration y E2E). No se agregan suites.

## Notas / decisiones de diseño

- **Dos stacks, una sola entidad visual.** El landing es Tailwind (tokens de `brand.css` via
  `@theme`); `detail.html` es Bootstrap + un `<style>` propio con variables `--bs-*`. Unificar no
  significa migrar el detail a Tailwind (cambio enorme); significa extraer la card de lugar a un
  fragmento único con estilos declarados en `brand.css`, que ambos lados consumen. Migrar stacks es
  otra card si algún día decide el equipo.
- **Íconos vacíos y el tag que no renderiza: no es magia, es el bundle.** `lucide.createIcons()`
  reemplaza cada `<i data-lucide="X">` si y solo si `X` existe en el bundle vendorizado; si no,
  consola: "icon name was not found" y el `<i>` queda vacío. En `lucide.min.js` actual no aparece
  el icono `users` (el del tag "Público"): la raiz del AC-05 es esa ausencia, y el mismo problema
  cubre el AC-04. Dos caminos validos: (a) regenerar el bundle con los iconos que faltan desde los
  fuentes de lucide (mismo comando, mismo archivo, nada de CDN), (b) cambiar el markup a nombres
  que ya existen en el bundle. Preferimos (a): el nombre conceptual correcto gana sobre el nombre
  disponible por accidente.
- **El indicador del FAQ ya existe a medias.** El markup actual de la landing ya pone un `plus` con
  `group-open:rotate-45` en los summaries. Lo que falta es que el indicador no dependa de que
  lucide renderice (el problema de arriba: si el ícono cae, el summary queda sin nada). El
  indicador nuevo se declara en CSS (chevron en `::after` del summary, o el plus quedando como CSS
  con `clip-path`), visible sin JS, y el estado abierto marcado (`details[open]`).
- **Sticky es global, no por página.** El navbar es un fragmento único incluido por
  `layouts/base :: layout`: la clase sticky va en `components/navbar.html` y cubre landing y app de
  una vez. `top-0` + un z-index alto, cuidando que el skip-link del landing (mismo orden fuente)
  siga quedando arriba al scrollear.
- **La P del logo no es texto: son paths dibujados a mano.** Cambiar "la fuente" del logo es
  re-tracear los paths desde la fuente elegida (o regenerar el SVG con un text-to-path de la misma
  familia), no escribir `<text>`: el SVG tiene que verse igual aunque la fuente web no cargue.
  Fuente candidata: la serif de `brand.css` (Bree Serif), que es la que usan los titulares del
  landing.
- **La fuente del logo NO introduce dependencias nuevas.** No se agregan Google Fonts extra ni
  CDN: el re-trace usa la familia ya cargada por el sitio.
- **Botón CTA: una clase, no copies.** El estilo CTA ya aparece repetido 4+ veces en el landing
  con la misma lista de utilidades Tailwind pegada string por string. Se declara como una clase
  propia (`.btn-cta`, dueño: `brand.css`) y se reusa; el navbar usa la misma clase para sus botones
  primarios.
- **La fila de botones de la edit page baja a la cabecera (AC-08).** Hoy "Eliminar plan" y
  "Clonar plan" viven solos en una fila entera (y se tocan entre sí). La fila única del header
  (título a la izquierda, botones a la derecha con `gap` declarado) devuelve una franja de alto
  y despeja el layout. "Salir del plan" (participante no-admin) ocupa el mismo lugar, es la
  misma ranura de "acciones destructivas".
- **Los tiles del resumen no se tocan (AC-09).** Los `.col-*  bg-light p-3 rounded` del summary
  quedan apilados sin margen vertical visible: con las esquinas redondeadas y el fondo distinto
  del padre, la costura se nota. La corrección es de gutter/spacing del grid del summary, no un
  restyle de cada tile.
- **Switch de visibilidad, no checkbox suelta (AC-10).** Para reemplazar el
  `.form-check` de "Hacer público este plan" se usa un `<input type="checkbox" class="form-switch">`
  (nativo, accesible, cero JS) con los colores de marca: el knob/tramo activo sale del
  `--primary`, inactivo de los grises del sistema. El label de al lado siempre nombra el estado
  resultante (Privado/Público) para que la lectura no dependa del color.
- **El momento de clonar se agota en la primera vez (AC-13).** El único toque "funcional" de la
  ronda nueva: el botón "Clonar plan" sale con una condición de template (visible solo a
  participantes no-administradores). Tras clonar un plan público el clonador queda como
  administrador del clon, así que la edit page del clon ya no lo ofrece: se rompe el loop
  "clonar el clon" sin campos nuevos ni tracking. Sin backend ni migraciones.
- **Banda sólida del header, imagen fuera (AC-14).** La cabecera del plan lleva una banda
  de color de marca sólido detrás del título. El ticket pedía una imagen por defecto en ese
  slot; se implementó (cover genérico + overlay de tokens) y en la revisión visual la
  usuaria la descartó —"la barra no se ve bien"— pidiendo color sólido. Subir/cambiar/quitar
  una imagen decorativa exige un campo `image` en Plan, migración y storage: eso es otra
  card ([IMG]) y no entra aquí; el AC deja el slot listo.
- **Interacciones con [MOBILE] (ya en main).** La spec se escribió antes del merge de 22-MOBILE
  y desde entonces conviven reglas que tocan los mismos lugares:
  - **AC-06 / sticky:** `/explore` es mapa full-screen y su detalle de lugar es una card
    `z-[1100]`; el `z-50` del navbar sticky queda por debajo, como los modales del sitio. Los
    sheets del explorador (`#plans-aside`) y la barra de tabs son mobile-only y no se pisan con
    la barra sticky.
  - **AC-08 / acciones del plan:** a ≤575 px la media query de `plans/detail.html` (fix de
    letras sueltas de 22-MOBILE) ya mueve las acciones a su propia fila; la fila única
    título+botones del AC-08 vale para ≥576 px y no pisa esa media query.
  - **AC-11 / padding inferior:** `/explore` no debe heredar un padding que le robe altura al
    mapa full-screen; el padding aplica a las vistas de contenido.
- **Contraste en modo oscuro ya incluido en la rama.** `fix(dis) 1e8f5f1` (hallazgo de QA
  durante la revisión del PR #36; no es un AC de esta card): `brand.css` les da complemento
  legible a los `text-blue-600` (17 links sueltos) y `text-red-600` (11 errores de formulario)
  que en oscuro no llegaban a AA sobre las tarjetas, los hovers rojos de los botones
  destructivos pasan a superficie roja oscura y el hover de las cruces del explorador pasa a
  `hover:text-gray-900`. `ThemeModeTest` pinnega los overrides nuevos; los chips con fondo
  pastel no se tocan: su fondo queda claro y su texto oscuro sigue leyéndose.

## Referencia de Implementacion

> Los pasos a continuacion son guia de implementacion, no reemplazan los criterios de aceptacion de arriba.

### 1. Card de lugar compartida (AC-01)

Nuevo fragmento: `src/main/webapp/WEB-INF/templates/components/place-card.html`

```html
<div th:fragment="place-card(stop)" class="place-card">
  ...
</div>
```

La clase `.place-card` se declara en `resources/css/brand.css`, con los mismos tokens de
`.itinerary-stop` de `detail.html` (borde `1px solid`, radius `8px`, padding `10px 12px`, hover
translate/shadow). El hero del landing llama al fragmento en vez de su `<li>` local; en
`detail.html`, `.itinerary-stop` pasa a ser `.place-card` (el nombre de clase se reemplaza, la
estructura del `<li>` del itinerario no cambia para el JS de drag & drop).

### 2. Estilo CTA único (AC-02)

En `resources/css/brand.css`:

```css
.btn-cta {
  display: inline-flex;
  align-items: center;
  border-radius: 8px;
  padding: 0.5rem 0.75rem; /* px-3 py-2, lo que ya usan los CTAs */
  font-weight: 600;
  background: var(--primary);
  color: var(--white);
  transition: ...;
}
.btn-cta:hover { background: var(--secondary); }
```

Se aplica al CTA del hero y de la sección final del landing, y a los botones primarios del navbar
(`Para vos`, `Explore`, `Plans`, `Login`, `Logout`). Los botones de otro formato (los de
texto con underline del hero) NO son CTAs y quedan como están.

### 3. Indicador del FAQ (AC-03)

En el summary del landing, el ícono de lucide se reemplaza por una flecha en CSS:

```css
details > summary::after {
  content: "";
  width: .5rem; height: .5rem;
  border-right: 2px solid var(--gray);
  border-bottom: 2px solid var(--gray);
  transform: rotate(45deg);
  transition: transform 200ms ease;
}
details[open] > summary::after { transform: rotate(-135deg); }
```

Además del chevron, el summary mantiene `cursor-pointer` (ya lo tiene) y gana un fondo/hover
marcado que hace obvio que la fila es abrible.

### 4. Íconos que no renderizan (AC-04, AC-05)

Regenerar `resources/core/js/lucide.min.js` desde los fuentes de lucide incluyendo los iconos que
usa el sitio hoy (`clock`, `link`, `map-pin`, `calendar`, `plus`, `users`, los de `/explore`) y los
que añade esta spec, o cambiar los nombres en el markup si el equipo prefiere no tocar el bundle.
Después del cambio, la consola no debe loguear ningún "icon name was not found" en landing ni app,
y los V-04/V-05 pasan.

### 5. Navbar sticky (AC-06)

En `components/navbar.html`, el `<nav>` gana las clases de sticky:

```html
<nav ... class="sticky top-0 z-50 flex items-center justify-between bg-black px-4 text-white shadow">
```

Cero JS. Con `prefers-reduced-motion` no cambia nada (sticky no anima).

### 6. Logo (AC-07)

Re-tracear la P del logo con la familia elegida (font-to-path offline) y reemplazar los `<path>`
del glyph en `resources/images/logo.svg`. Los círculos decorativos y los colores quedan como
están: solo cambia el glyph de la letra.

**Cierre: revertido a pedido de la usuaria (2026-10-25).** El re-trace se implementó
(`81c8396`: P trazada desde `BreeSerif-Regular.ttf` con Java2D offline, sin dependencias ni
CDN), pero en la revisión final la usuaria prefirió el vector original. El archivo vuelve a
su versión previa a `81c8396` y el AC queda cerrado **sin cambio visual**: el logo es idéntico
al de antes de la card.

## Notas de implementación (hallazgos)

Correcciones y decisiones tomadas al implementar, que ajustan o precisan lo anotado arriba.

- **AC-04/AC-05: la raíz NO era el bundle, era el `return` con reduced-motion.** La nota de
  diseño de arriba diagnóstica el tag "Público" como un icono `users` ausente del bundle y
  proponía regenerarlo. En la práctica el bundle vendorizado está completo: los 12
  `data-lucide` del landing se reemplazan (12/12 SVG dibujados, 0 avisos en consola). La causa
  real era que el IIFE de reveal de la landing hace `return` temprano bajo
  `prefers-reduced-motion` (muestra todo sin animar), y ese `return` saltaba el
  `createIcons()`: con reduced-motion activo todos los `<i>` quedaban vacíos. El fix es un
  `<script>` propio que llama a `lucide.createIcons()` antes y aparte de ese IIFE (no depende
  del observer ni de `reveal-ready`). No hizo falta regenerar el bundle.
- **La card de lugar: un fragmento para el landing, la misma clase para el detalle.** El
  itinerario del hero consume `components/place-card.html` (raíz `<li th:fragment>`); el
  hero lo itera con `th:block th:each>` por afuera, porque `th:each` y `th:replace` en el
  mismo elemento rompen (el contexto del fragmento queda `null`). El detalle no puede invocar
  un fragmento de Thymeleaf dentro de su `<li>` que clona `v-for` de Vue, así que usa la
  **clase doble** `.itinerary-stop.place-card`: `.place-card` (brand.css) aporta el chrome
  compartido y `.itinerary-stop` conserva las reglas propias del detalle (fondo
  `--bs-body-bg` que re-colorea el modo oscuro y que clava `ThemeModeTest`, ajuste mobile y
  reduced-motion), en un bloque `<style>` posterior al `<link>` de brand.css para ganar la
  cascada.
- **La P del logo se re-trazó con Java2D, sin dependencias.** `Font.createFont` +
  `GlyphVector.getOutline` sobre `BreeSerif-Regular.ttf` (la serif de brand.css), escalado
  uniforme por altura de mayúscula y alineado al baseline y al centro-x del vector anterior.
  La salida es un `<path>` estático: el SVG no introduce ninguna fuente en tiempo de
  ejecución.
- **AC-14: banda sólida de marca (decisión final de revisión, 2026-10-25).** El ticket pedía
  banner con imagen por defecto; se implementó (`cover-3.svg` + overlay de tokens, recorte
  anclado al 65% sobre la columnata) y se probó en la revisión, pero la usuaria lo descartó
  —"la barra no se ve bien"— y pidió color sólido. Implementación final en `detail.html`:
  `background-color: var(--primary)` con `min-height: 96px` y el título en blanco encima
  (sombra de texto: contraste garantizado a cualquier ancho y en ambos temas). La imagen
  decorativa queda para la card [IMG]. Verificado con Playwright en 320/390/1280 ×
  claro/oscuro: título legible y sin solapes.
- **Revisión visual posterior: tres ajustes sobre lo implementado.** La revisión de la
  usuaria en 320×752 / 390×844 y desktop levantó tres defectos, corregidos sin tocar
  markup ni tests:
  - **Tiles del resumen ilegibles en oscuro.** `.bg-light` usa `--bs-light`, que Bootstrap
    no redefine en dark: quedaba mancha blanca (#f8f9fa) y el texto hereda
    `--bs-body-color` (#adb5bd), gris sobre blanco. Fix en `detail.html`:
    `[data-bs-theme="dark"] .bg-light { background-color: var(--bs-tertiary-bg) !important }`
    (solo el resumen usa `.bg-light` en el sitio).
  - **Iconos del itinerario por encima del texto.** Los pares icono+texto ("Cómo llegar",
    "Eliminar", fecha/hora) dependían del baseline del SVG inline y se desalineaban.
    Fix: `inline-flex; align-items: center` en `.stop-directions`, `.stop-remove` y
    `.stop-schedule > span`.
- **El mapa Leaflet tapaba el nav sticky al hacer scroll (hallazgo de la revisión).**
  `.leaflet-container` venía con `z-index: auto`: no creaba stacking context, y sus
  paneles (z-index 400–800, marcadores custom hasta 1000) escapaban al contexto raíz y se
  dibujaban por encima del nav (`sticky z-50`) al scrollear el detalle del plan. Fix
  global en `brand.css`: `.leaflet-container { position: relative; z-index: 0; }`, que
  convierte al mapa en su propio stacking context y contiene los paneles dentro de él.
  Cubre el detalle y `/explore` sin tocar el `leaflet.css` vendorizado. Diagnosticado con
  Playwright sobre la app corriendo (`elementFromPoint` dentro de la banda del nav devolvía
  un marcador del mapa; tras el fix devuelve el nav).
- **Revisión visual: separación en modo día, en TODAS las vistas (2026-10-25).** La
  saturación —blanco sobre blanco sin distinguir fondo, cards, tiles e inputs— no era solo
  del detalle: aparece en landing, catálogo público, home, admin, perfil y auth. Fix global
  en `brand.css`, solo tema claro, en dos llaves:
  1. `html:not([data-bs-theme="dark"]) { --bs-border-color: #adb5bd }`: redifinir el token
     hace que todo lo que pinta Bootstrap (cards, inputs, list-group, tablas, tiles
     `.bg-light`, panel del itinerario) salga con un gris visible en lugar de `#dee2e6`,
     en cualquier vista; `.card` además recupera el ancho pisando el `border-0`.
  2. las utilidades faint de Tailwind se suben al mismo gris (`.border-gray-200` /
     `.border-gray-300`, con `hover:border-gray-300` un paso más oscuro como antes) y las
     cajas blancas con sombra que no traían borde (`.bg-white.shadow*`: catálogo, home,
     admin, auth, perfil) reciben `1px solid`. Las `<section>` blancas del landing quedan
     fuera a propósito: no traen borde ni sombra.
  Además los textos/meta que "no se notaban" pasan de `gray-300/400` a `gray-500`: los
  números 01/02/03 del landing, el nombre del administrador en la vista pública del plan,
  el rol del perfil y los meta-íconos del catálogo de lugares. En oscuro no se toca nada.
  Medido en 320: el borde no altera el layout de los tiles (el wrap de labels a 320 es
  preexistente —`word-wrap: break-word` de `.card` en Bootstrap—).

- **AC-15: el reboot de Bootstrap pisa el navbar compartido, no Tailwind.** `/plans/{id}`
  carga Bootstrap en su propio `<head>` y encima de las utilidades de `layouts/base`, así que
  en esa vista el avatar salía rectangular (`nav button { border-radius: 0.375rem }` le ganaba
  a `.rounded-full` por vivir sin capa), el wordmark quedaba en `1rem` y los botones perdían
  la serif. El fix son pines de utilidad, no de elemento: `.rounded-full`, `.rounded-md`,
  `.navbar-brand`, `.font-serif` y `.gap-4 { 1rem }` (el valor de Tailwind, no el `1.5rem` de
  `.gap-4` de Bootstrap). `#navbar-menu` vive como hermano del `<nav>` en `layouts/base`, así
  que queda fuera de esos pines y recibe los suyos.
- **AC-15 (oscuro): Bootstrap `.bg-white` con `!important` blanqueaba el menú.** El remapeo
  `html[data-theme="dark"] .bg-white { var(--dark-card) }` de `brand.css` no lleva
  `!important`, así que en `/plans/{id}` —única vista que carga Bootstrap— el menú quedaba con
  fondo `rgb(255, 255, 255)` donde el resto del sitio lo pinta `rgb(31, 41, 55)` (medido contra
  `/for-you` con la sonda de reglas ganadoras). El detalle repite el valor de brand con
  `!important`: en claro los dos pintan blanco (sin cambio visible) y el pin también cubre el
  `.bg-white` del toast del layout.
- **AC-15: dos diffs descartados por no tener efecto visual.** El `box-shadow` del nav difiere
  porque las capas de ring de Tailwind son transparentes (la pintura es idéntica) y el radius
  del avatar se pinnea a `9999px` en vez de `calc(infinity * 1px)` (ambos dejan el avatar
  totalmente redondo a 36px; `9999px` es más compatible). Los diffs de `color` heredado que
  quedan en contenedores del menú no pintan texto visible: todos los textos llevan su propia
  utilidad de color.
- **AC-16/AC-17: la ficha se estila en el `<style>` del detalle, no en `brand.css`.** Los
  bloques usan variables `--bs-*` para que Bootstrap re-coloree en modo oscuro como el resto de
  la página, y el chip de categoría reutiliza los `.cat-*` que ya define el mapa. El editor de
  horario va con `th:if="${canEditPlan}"`, la misma condición que el resto de la edición del
  itinerario (administrador **o** editor): no es un permiso nuevo, es la misma llave del AC-12.
- **AC-18: `nextTick` antes de `showModal()`.** El `<template v-if>` de la ficha se monta en el
  siguiente flush de Vue: abrir el diálogo sin esperarlo dejaba `aria-labelledby` apuntando a un
  título que todavía no existía y el foco caía fuera. Con `await nextTick()` el título ya está
  montado cuando entra el `showModal`, y el `tabindex="-1"` del `<dialog>` hace el resto.
- **El merge de `main` agregó un segundo `<dialog>` al detalle.** El dropdown "Más opciones" y
  el diálogo de baneados (`#banned-participants`) entraron desde la card de baneo y quedaron
  **antes** en el DOM: cualquier test que diga `document.querySelector('dialog')` sobre esa
  página agarra el de baneados. Los dos tests nuevos se anclan por
  `aria-labelledby=place-detail-title`, que solo el diálogo del detalle declara.

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `templates/components/place-card.html` | Crear (fragmento de card de lugar compartido) |
| `resources/css/brand.css` | Actualizar (`.place-card`, `.btn-cta`, indicador del FAQ, estilos del switch de visibilidad y del banner del plan para AC-08..AC-14) |
| `templates/pages/landing.html` | Actualizar (hero usa el fragmento, CTAs con `.btn-cta`, FAQ con indicador CSS, sin `<style>` de card propio en el hero) |
| `templates/pages/plans/detail.html` | Actualizar (AC-01: `.itinerary-stop` pasa a consumir `.place-card`; AC-08: header título+botones en una fila con `gap`; AC-09: gutter del summary; AC-10: switch de visibilidad; AC-11: padding inferior; AC-12: handle de drag en cada card; AC-13: condición de "Clonar plan"; AC-14: banda sólida con imagen decorativa fuera; AC-15: pines de utilidad del navbar; AC-16: ficha del modal; AC-17: editor con `canEditPlan`; AC-18: `aria-labelledby` + `tabindex`) |
| `templates/components/navbar.html` | Actualizar (sticky; botones con `.btn-cta`) |
| `resources/core/js/lucide.min.js` | Actualizar (regenerar con los iconos faltantes, si se elige el camino (a)) |
| `resources/images/logo.svg` | Actualizar (P re-traceada) |
| `resources/core/js/pages/plans-detail.js` | Actualizar (AC-18: `await nextTick()` antes de `showModal`) |
| `test/java/com/valhalla/e2e/PlanPlaceDetailE2E.java` | Actualizar (2 tests E2E: AC-15 por uno, AC-16/17/18 por el otro) |

No se tocan: controladores, servicios, repos, SecurityConfig, datos de semilla, ni ningún test
de unitario, de integración o de seguridad. La única expresión no-puramente-visual de la ronda
AC-08..AC-14 es la condición del AC-13 en el template; los íconos que sobran (handle de drag)
salen del set de íconos ya cargado en la página. En AC-15..AC-18 tampoco hay backend: lo único
que no es markup/estilo es el `nextTick` de Vue y los dos tests E2E.
