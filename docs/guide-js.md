# Guide: JavaScript in the View Layer

The contract for every line of JavaScript in this project, whether it lives in a `.html`
template or in a `.js` file. It exists because the view layer accumulated four
conventions at once, and one of them broke a page in production-ish conditions: an inline
`[[${...}]]` inside a plain `<script>` emitted `&amp;` into a JS string and a raw newline
that ended the literal, so the page's whole Vue app died with a `SyntaxError` and nobody
could save a place.

That is not a style preference. R3 and R7 each prevent a defect that already happened here;
the rest exist so the next one is not invisible.

**Enforced by:** `.\scripts\gate.ps1 js -Strict` in CI, which is the same checker the local
`.\scripts\gate.ps1 js` runs -- see [the gate](#the-gate).

Related: [guide-vue.md](guide-vue.md) for the Thymeleaf + Vue view structure,
[guide-css-js.md](guide-css-js.md) for the no-framework alternative. Those two describe
*what a view looks like*; this one describes *how its JavaScript is written*.

## Scope

| Path | Status |
| :--- | :--- |
| `src/main/webapp/WEB-INF/templates/**/*.html` | covered, inline `<script>` included |
| `src/main/webapp/resources/core/js/**/*.js` | covered, `pages/` and `lib/` included |
| `vue.global.prod.js`, `tailwind-browser.js`, `lucide.min.js`, `leaflet.js` | vendored, excluded |

## The rules

### R1 -- One API: Composition, via `setup()`

All page code uses `setup()` with `ref`/`computed`/`watch`. There is no `data()` left in the
tree: the eight Options API views were converted in phase 4, the largest last.

```js
createApp({
  setup() {
    const places = ref([]);
    return { places };
  }
}).mount('#explore-app');
```

Why: state is declared where it is created, a template ref is a variable instead of
`this.$refs`, and it is the only form whose state can be tested without mounting a
component.

One habit worth copying from the conversion: when a view reads six fields off its own
`data-*` root, share `const field = (key) => ref(data[key] || '')` instead of writing six
identical `ref` lines. It is shorter than the `data()` object it replaced.

Two things stay plain `let` rather than `ref`: handles the page does not render, such as a
Leaflet map, its markers and the index of the row being dragged. A ref there only wraps the
object in a reactive proxy nobody reads.

### R2 -- One root per page, named after the view

The mount target is `#<view>-app`: `#place-form-app`, `#users-app`, `#explore-app`,
`#place-import-app`. Not `#app`.

Mount exactly one app per page. A fragment that mounts is a bug: the host page owns the
lifecycle of the subtree.

The root id is not free decoration: it is a structural selector in the E2E page objects
(`#explore-app > aside`), so renaming one is a coordinated change -- the template, the module
and every page object that anchors on it, in the same commit.

### R3 -- Server data enters through `data-*`, never as an inline expression

**Never** `[[${...}]]` or `[(${...})]` inside `<script>`. Thymeleaf renders those as
escaped HTML text, not as a JavaScript literal: `&` becomes `&amp;`, and a newline in the
value ends the string and kills the page's script.

```html
<!-- no: placeForm.description comes from a <textarea>, so newlines are normal -->
<script>
  description: '[[${placeForm.description}]]'
</script>

<!-- yes: the attribute is HTML-escaped by the browser's own parser and read back decoded -->
<div id="place-form-app" th:attr="data-description=${placeForm.description}"></div>
<script>
  const data = document.getElementById('place-form-app').dataset;
  const description = data.description;
</script>
```

`th:inline="javascript"` with the comment form is the accepted alternative when the value
really must sit in a literal -- it is what `layouts/base.html` does for the dev-reload
token. Prefer `data-*`: it keeps the template free of JavaScript syntax.

There is one case `data-*` cannot carry: an **object graph**. A `data-*` attribute holds a
string, so a list of places reaches a module only through a serializer the controller does
not need to own. The accepted form is one seed object on `window`, assigned by a
`th:inline` literal, with the module reading it and defaulting every field:

```html
<script th:inline="javascript">
  window.PUBLIC_PLAN_SEED = { stops: /*[[${itineraryPlaces}]]*/ [] };
</script>
<script type="module" th:src="@{/js/pages/plans-public.js}"></script>
```

```js
const stops = (window.PUBLIC_PLAN_SEED || {}).stops || [];
```

Three pages use it (`PLAN_DETAIL_SEED`, `PLACE_IMPORT_SEED`, `PUBLIC_PLAN_SEED`). It is the
same handoff as `data-*`, one scope wider, and it stays confined to the template: the
module never reads Thymeleaf.

Gate: **JS01**.

### R4 -- Inline scripts stop at 20 lines

Under 20 lines, inline is right: the behaviour belongs to the markup that uses it. Over
that, the template has become a page whose JavaScript happens to live in HTML, and it
moves to `/js/pages/<view>.js`:

```html
<script type="module" th:src="@{/js/pages/place-import.js}"></script>
```

Modules are native ES modules. No bundler, no npm, no build step -- the project vendors
its libraries and compiles Tailwind in the browser on purpose, and none of that changes.

Gate: **JS06**.

### R5 -- `const`, or `let` when it is reassigned

No `var`. Code targets ES2020: arrow functions, optional chaining, `async`/`await`. Vue 3
already sets the browser floor, so nothing here needs transpiling.

Gate: **JS02**.

### R6 -- One request helper

`/js/lib/api.js` is the only place that writes `fetch`. It adds the CSRF header from the
`_csrf` metas, turns an expired session into a sentence instead of a JSON parse error, and
raises one error type that carries the status:

```js
export class ApiError extends Error { /* message, status */ }
export async function apiFetch(url, { method, body, headers, signal } = {}) { /* ... */ }
```

`apiFetch` returns the `Response`, so the caller keeps its own `.json()` / `.text()`. It
throws `ApiError` in two cases:

- the response is a redirect or carries `text/html` -- an expired session answers `/api/*`
  with the login page and **status 200**, which is what used to reach the user as a parse
  error;
- `!response.ok`, with the API's `message` when the body has one, or the status otherwise.

A failed request -- network, abort -- is **not** wrapped. `apiFetch` awaits `fetch()`
directly, so an `AbortError` arrives with its own name and a caller that cancels on purpose
can still tell a cancel from a failure:

```js
try {
  const response = await apiFetch(url, { signal: controller.signal });
} catch (failure) {
  if (failure.name === 'AbortError') return;   // superseded by a newer request
  error.value = failure.status === 401 ? failure.message : 'No se pudo cargar.';
}
```

That is why `pages/place-import.js` uses it too, `AbortController` and all. The one direct
`fetch` left in the tree is not API traffic: `layouts/base.html` polls `/reload/version`,
exists only under the `dev` profile, and has no CSRF, no JSON and no error surface to
normalize. It stays as it is.

Callers branch on `error.status`: `409` is a product answer, `401` already carries the
right copy, everything else gets the page's own sentence.

Manual review rule; no gate id.

### R7 -- Build DOM, never markup

Two sinks, both real:

```js
// no: Leaflet parses a string popup as HTML, and place names come from
// the admin ABM and the OSM importer, neither of which sanitizes them
marker.bindPopup('<strong>' + place.name + '</strong>');

// yes
marker.bindPopup(el('div', {}, [el('strong', { text: place.name })]));
```

`innerHTML` follows the same rule: assigning a literal is fine, assigning data is not.
`textContent` is the only way a value becomes text.

`el(tag, props, children)` in `/js/lib/dom.js` is the one constructor for new code, and the
only file under `pages/` and `lib/` that calls `document.createElement`. The name of the
text prop is `text` -- it sets `textContent`:

```js
el('a', {
  class: 'text-primary fw-medium d-inline-block mt-1',
  href: directionsUrl(place),
  target: '_blank',
  rel: 'noopener',
  text: 'Cómo llegar'
})
```

Every other prop becomes an attribute, `dataset` is merged, and `null`/`undefined` props
**and children** are skipped, so `cond ? el('br') : null` is a conditional node.

Gate: **JS03**, **JS05**.

### R8 -- Errors render inline, never `alert()`

A `v-if` block bound to an `error` ref. `alert()` blocks the thread, cannot be styled, and
reads as a browser failure rather than a product message.

```js
try {
  await apiFetch(url, { method: 'POST', body });
} catch (failure) {
  error.value = failure.message;
}
```

A module with no error surface of its own does not invent one: it dispatches a `CustomEvent`
and the host page renders it. `add-to-plan.js` emits `plan-error`; `pages/places-list.js`
feeds that into the same banner as its own failures.

Every `await` on a request sits inside a `try`, and the loading flag is cleared in the
`catch` as well: a spinner that never stops is a worse failure than the original error.

Gate: **JS04**.

### R9 -- Submits are guarded

An in-flight flag, bound to `:disabled`, on every submit that leaves the page. Two native
POSTs to `/plans` because the button was clickable is a data defect, not a UX nit.

Note that a `:disabled` binding alone does not stop a form submit fired by Enter; the
handler checks the flag too.

### R10 -- Accessibility floor

Not a full audit -- four things, because they are cheap and their absence is severe:

1. Icon-only buttons carry `aria-label`.
2. `@click` on a non-interactive element carries `role`, `tabindex="0"` and `@keydown.enter`
   / `@keydown.space`.
3. A dialog moves focus into itself on open and returns it to the trigger on close.
4. `v-for` always has a stable `:key` -- an id, never the array index.

### R11 -- No remote origin at runtime

Libraries are vendored under `/js/` and `/css/`. A remote `<script src>` or stylesheet
`<link>` breaks offline runs, defeats any future CSP, and puts the supply chain on someone
else's release schedule. It also fails silently in the worst way: a missing stylesheet is a
page that renders, just wrong.

Every one of them is local now: Leaflet's script and stylesheet, Bootstrap 5.3 for
`plans/detail`, Lucide, Vue and the Tailwind browser build. Font Awesome was not vendored --
`plans/detail` draws its 16 glyphs with the Lucide library every other page already uses.

The one accepted exception is Google Fonts, because `brand.css` declares a local fallback
family: if `fonts.googleapis.com` is unreachable the typeface changes and nothing else.

Gate: **JS07**.

## File layout

```
src/main/webapp/resources/core/js/
  pages/places-list.js     the Places view: map, filters, plan panel      [shipped]
  pages/plans-detail.js    one plan's map, itinerary and catalogue        [shipped]
  pages/place-import.js    the OSM importer                              [shipped]
  pages/plans-public.js    the read-only public plan map                 [shipped]
  lib/api.js               csrfToken(), apiFetch(), ApiError             [shipped]
  lib/dom.js               el()                                          [shipped]
  lib/places-map.js        buildMarker(), directionsLink(), categoryIcon*() [shipped]
  <vendored>.js            vue.global.prod.js, tailwind-browser.js, lucide.min.js, leaflet.js
```

`lib/api.js`, `lib/dom.js` and `lib/places-map.js` are live, and the four extracted views
import from them. Nothing under `WEB-INF/templates` builds a node by hand any more:
`document.createElement` appears once in the tree, inside `el()`. A classic script cannot
`import`, so `lib/` is only usable from a module.

`lib/places-map.js` is the answer to "how is a place drawn on a map", and it replaced three
hand-built copies of the same `L.divIcon` + `L.marker` pair and three copies of the
directions URL: the category icon as a Lucide name, an SVG element or markup
(`categoryIconName`, `categoryIconNode`, `categoryIconHtml`), the "Cómo llegar" link built
from the place's address text (`directionsUrl`, `directionsLink`), and `buildMarker()`,
which returns a marker that is **not** added to a map -- callers chain
`.addTo(map).bindPopup(popup)`. Its `badge` option is a DOM Element, not markup: Leaflet 1.9
appends an `Element` passed as `divIcon({ html })`, which is why no view hands Leaflet HTML
any more. The two icon tables that used to live in `plans-detail.js` and `places-list.js`
are the one table here.

Vendored files follow their serving path: a script goes in `resources/core/js/` and a
stylesheet in `resources/css/`, because `/js/*` and `/css/*` are what the templates request.
A vendored stylesheet's relative `url()` targets go beside it -- `leaflet.css` asks for
`url(images/marker-icon.png)`, so Leaflet's five PNGs sit in `resources/css/images/` next to
it.

Naming: files `kebab-case.js` matching their view, functions `camelCase`, event handlers
prefixed `on`, module-level constants `UPPER_SNAKE`, `data-*` attributes `kebab-case`.

## The gate

```powershell
.\scripts\gate.ps1 js            # report only; exit 0
.\scripts\gate.ps1 js -Strict    # exit 1 on any FAIL rule; what CI runs
.\scripts\js-check.ps1 -Strict   # the checker on its own
```

A grep gate, not a linter: it reads templates and project JS, never touches the files, and
matches the shapes below. It is deliberately narrow -- a rule that needs a parser stops
being cheap enough to trust. It walks `resources/core/js` recursively, so a module under
`pages/` or `lib/` is checked exactly like a template's inline script.

| Rule | Severity | Catches |
| :--- | :--- | :--- |
| **JS01** | FAIL | `[[${`, `[[@{`, `[(${`, `[(@{` inside a `<script>` without `th:inline="javascript"` |
| **JS02** | FAIL | `var` declarations (comments excluded) |
| **JS03** | FAIL | `bindPopup(` opening with a quoted string, line break included |
| **JS04** | FAIL | `alert(` outside comments |
| **JS05** | FAIL | `innerHTML` assigned anything but a literal |
| **JS06** | INFO | an inline `<script>` over 20 lines |
| **JS07** | FAIL | a `<script src>` or stylesheet `<link>` on an `http(s)://` origin, Google Fonts excepted |

INFO rules never fail the gate, with or without `-Strict`: they mark structure to fix when
the file is open anyway, not defects. A FAIL rule, on the other hand, is a red build --
CI runs the gate with `-Strict`.

### What the gate cannot see

- `bindPopup(popupText)` -- the popup expression assigned to a variable first. The gate reads
  the call site, so it cannot tell a node from a string there. `pages/plans-detail.js` hid the
  tree's last HTML popup this way until phase 4; review R7 by eye on any marker code.
- `v-for` without `:key`, focus handling, `role`/`tabindex`: R10 is review-only.
- `@import` and `url()` inside a stylesheet: JS07 reads HTML tags, so a remote asset pulled
  in by CSS is invisible to it.
- Anything inside `v-html`, `insertAdjacentHTML` or a `data:` URL.
- `Vue.createApp` with `data()`: R1 is review-only. The tree has none left, but the gate
  would not see one coming back.
- A remote asset the gate cannot read: it matches `<script src>` and stylesheet `<link>` tags
  in templates, so a URL assembled in JavaScript or injected by another script still gets
  through. The shapes it does read are FAIL since phase 6, so the regression path that
  matters -- a new page copying an old CDN tag -- is closed.

### Current debt

The contract landed on a baseline of **72 FAIL, 21 INFO** -- every FAIL row was the backlog.
Each phase lowers it; this table is the live count.

| Rule | Findings | Where |
| :--- | ---: | :--- |
| JS01 | 0 | shipped in phase 1 |
| JS02 | 0 | shipped in phase 4: `avatar-picker`, `layouts/base`, `landing`, `navbar-menu` |
| JS03 | 0 | shipped in phase 1 |
| JS04 | 0 | shipped in phase 2 |
| JS05 | 0 | preventive: no `innerHTML` in the tree today |
| JS06 | 4 | `avatar-picker` 52 lines, `navbar-menu` 40, `landing` 41 and 65 |
| JS07 | 0 | shipped in phase 5: Leaflet js + css, Bootstrap 5.3 and Lucide all local. FAIL since, so a reintroduced CDN tag is a red build |

**0 FAIL, 4 INFO.** The `backend` job in `.github/workflows/ci.yml` runs the gate with
`-Strict` in its `Frontend JS contract` step, before the Maven build, so a FAIL rule is a
red build; the four INFOs above are the whole remaining report.

Phase 3 paid back what phases 1 and 2 borrowed: the four big views carried 1164 lines of
inline JavaScript between them (`places/list` 532, `plans/detail` 322, `place-import` 215,
`public` 95) and JS06 does not flag any of them now.

Phase 4 left four inline scripts that are all structurally large -- a component with its own
Avatar picker, the navbar menu, two sections of the landing page. They are not "ordinary
pages" waiting for a move; each is a self-contained component, and extracting it means giving
it a module of its own. `login` and `home` left the budget by declaring their state in
`setup()` instead of a `data()` object.

The 42 `var` declarations in those four scripts closed in the same phase: the bindings are
`const`, and `let` only where the value is assigned again -- the nine loop counters plus the
`stored`/`inOpener` accumulators. Nothing about the behavior changed, which is why the sweep
was worth doing before the components move: the extract is a move, and a move on top of a
rewrite is two changes to review at once.

## Migration order

Each phase is independently shippable and independently reviewable.

| Phase | Work | State |
| :--- | :--- | :--- |
| 0 | This document + the gate | shipped: 72 FAIL, report only |
| 1 | Fix every JS01 and JS03: `data-*` bridge, DOM popups | shipped: FAIL 72 -> 50 |
| 2 | Ship `lib/api.js`, migrate the API `fetch` calls, replace the 8 `alert()`s with an inline error surface | shipped: JS04 0, FAIL 50 -> 42 |
| 3 | Extract `places/list`, `plans/detail`, `place-import`, `plans/public` to `pages/`; ship `lib/dom.js` and consolidate every popup on it | shipped: JS06 11 -> 7 |
| 4 | Convert the 8 remaining Options API views to `setup()`: `place-form`, `places`, `user-form`, `users`, `login`, `new-user`, `home`, `plans-detail.js`. Rename each root to `#<view>-app`, E2E page objects included | shipped: R1 and R2 satisfied, JS06 7 -> 4 |
| 5 | Vendor Leaflet (js + css); extract `buildMarker()` to `lib/places-map.js`; resolve the Bootstrap/Font Awesome conflict in `plans/detail` | shipped: JS07 10 -> 0, every asset served from `/js/` or `/css/` |
| 6 | Flip CI to `.\scripts\gate.ps1 js -Strict` | shipped: `Frontend JS contract` step in the `backend` job, 0 FAIL. JS07 promoted from INFO to FAIL, so a remote asset is a red build, not a report |

## Two things that are not JavaScript, but block the claims above

1. **The site does not work without JavaScript.** `layouts/base.html` sets
   `html { visibility: hidden }` and only an inline script reveals it, with no `<noscript>`
   fallback. For that reason, rule 3 of guide-vue.md ("the form still posts when the script
   fails to load") and rule 1 of guide-css-js.md are both false today. Either add the
   `<noscript>` un-hide or stop claiming progressive enhancement in the guides.
2. **A `defer` on an inline `<script>` is ignored.** `layouts/base.html` puts `defer` on
   inline scripts; the attribute only orders *external* scripts, so the ordering those
   comments describe is not the ordering the browser applies.
