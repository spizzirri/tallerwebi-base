# [DIS] Estandarizar diseños a lo largo del sitio

> Trello: https://trello.com/c/XhCwVzj7/24-dis-estandarizar-dise%C3%B1os-a-lo-largo-del-sitio
> **Estimacion:** 2 pts Fibonacci
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.
> **Estado (2026-10-22):** rama `task-17-DIS` sincronizada con `main` (ya incluye [MOBILE]) y
> con el fix de contraste en modo oscuro (`fix(dis)`, commit `1e8f5f1`). Implementacion
> pendiente.

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
- No toca backend ni DB: no hay tests U/I/S nuevos, ni migraciones, ni reset de esquema.
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
| AC-07 | La letra P del logo (`resources/images/logo.svg`) se traza con una tipografía cercana a la usada en el resto del sitio (la serif de `brand.css` o la sans de la app), de modo que el logo no se lea como tipografía ajena |
| AC-08 | En la página de edición de un plan (`/plans/{id}`), el título del plan y los botones de acción (Eliminar plan / Salir del plan / Clonar plan) comparten la misma fila: título a la izquierda, botones a la derecha, con separación visible entre botones |
| AC-09 | Los tiles del resumen del plan (DESCRIPCIÓN, FECHA, HORA, VISIBILIDAD, ADMINISTRADOR, CÓDIGO) muestran separación entre sí (vertical y horizontal); ningún tile toca al siguiente |
| AC-10 | La visibilidad del plan en "Editar plan" se maneja con un switch estilo iOS con la paleta de la marca, que muestra el estado actual (Privado/Público) con un label claro al lado |
| AC-11 | La página de edición muestra padding inferior suficiente: al scrollear hasta el final, la última sección no toca el borde inferior de la ventana |
| AC-12 | Cada card de lugar del itinerario muestra un ícono de drag & drop (handle) en su borde izquierdo cuando el usuario puede editar el plan; sin permiso de edición el ícono no aparece |
| AC-13 | El botón "Clonar plan" solo se muestra a participantes que no son administradores: un plan recién clonado (donde el clonador quedó como administrador) no vuelve a mostrar el botón |
| AC-14 | La cabecera del plan lleva un banner con imagen por defecto detrás del título, sin taparlo ni solaparse con el contenido; subir/cambiar/quitar la imagen queda fuera de esta spec (card [IMG] aparte) |

## Escenarios de Test

Es una spec de diseño: no hay lógica nueva, así que no hay clases de test U/I/S nuevas. La
verificación es por inspección visual más la puerta completa (excepción: el AC-13 introduce una
condición de template — quién ve "Clonar plan" — verificada por inspección con los tres estados
de sesión; sigue sin tocar backend).

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
| V-14 | Banner con imagen por defecto detrás del título del plan; el título mantiene contraste y no se solapa con el contenido | AC-14 |

Los tres estados de sesión (anónimo, USER, ADMIN) en dos anchos (375px y 1280px).

### E2E (sin casos nuevos)

La suite E2E existente (`e2e/`), en particular la del landing (`LandingPage`), debe seguir en
verde sin cambios: los templates que los page objects ya tocan no cambian selects ni textos de los
helpers. Si un selector de test se rompe por el markup nuevo, se actualiza el page object, no el
test.

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
- **Banner de imagen: slot hoy, contenido después (AC-14).** La cabecerá del plan lleva una
  banda con imagen por defecto detrás del título (overlay con los tokens de marca para que el
  texto respire). Subir/cambiar/quitar la imagen exige un campo `image` en Plan, migración y
  storage: eso es otra card ([IMG]) y no entra aquí; el AC deja el slot listo.
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

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `templates/components/place-card.html` | Crear (fragmento de card de lugar compartido) |
| `resources/css/brand.css` | Actualizar (`.place-card`, `.btn-cta`, indicador del FAQ, estilos del switch de visibilidad y del banner del plan para AC-08..AC-14) |
| `templates/pages/landing.html` | Actualizar (hero usa el fragmento, CTAs con `.btn-cta`, FAQ con indicador CSS, sin `<style>` de card propio en el hero) |
| `templates/pages/plans/detail.html` | Actualizar (AC-01: `.itinerary-stop` pasa a consumir `.place-card`; AC-08: header título+botones en una fila con `gap`; AC-09: gutter del summary; AC-10: switch de visibilidad; AC-11: padding inferior; AC-12: handle de drag en cada card; AC-13: condición de "Clonar plan"; AC-14: banner con imagen por defecto) |
| `templates/components/navbar.html` | Actualizar (sticky; botones con `.btn-cta`) |
| `resources/core/js/lucide.min.js` | Actualizar (regenerar con los iconos faltantes, si se elige el camino (a)) |
| `resources/images/logo.svg` | Actualizar (P re-traceada) |

No se tocan: controladores, servicios, repos, SecurityConfig, datos de semilla, tests de
cualquier capa. La única expresión no-puramente-visual de la ronda AC-08..AC-14 es la
condición del AC-13 en el template; los íconos que sobran (handle de drag) salen del set
de íconos ya cargado en la página.
