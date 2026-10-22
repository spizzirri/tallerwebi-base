# [PROF] Perfil de usuario, perfil público y cambio de contraseña

> Trello: https://trello.com/c/h1q9vG7N — card `[PROF] Perfil de usuario` (lista IN PROGRESS)
> **Estimación:** 8 pts Fibonacci
> **Branch:** `11-PROF` (desde `main`)
> **v2.1:** el menú del navbar pasa a ser global (sesión anonima y autenticada) y aloja el toggle
> de tema; la preferencia de tema persiste por usuario (localStorage anónimos, server-side
> autenticados). Llega escalonada: PROF-009 (modal sin tema) y PROF-010/011 (tema, tras el merge
> de [THEME]).
> **v2.3:** la vista pública de `/profile/{id}` abre sin sesión — el link compartido funciona para
> cualquiera. `GET /profile` sin sesión sigue redirigiendo a login (es el atajo "mi perfil").
> **v2.4:** la vista del perfil deja de alojar la sección placeholder Tema (`#theme`): la única
> entrada de tema es el modal del navbar (PROF-010/011, que pasan a la card `[PROF+THEME]`).
> **v2.5:** `[FOLLOW]` (`19-FOLLOW.md`) activa lo que esta spec dejaba preparado: la sección
> `#follow` y los contadores de seguidores/seguidos son reales en `view.html` y `edit.html`, y la
> entrada "Siguiendo" del modal navega a `/profile#follow`. Los AC de esta spec no cambian; donde
> decían "placeholder" ahora aplica lo que provee `[FOLLOW]`.

## Objetivo

Que un usuario tenga una cuenta completa, visible y compartible. Hoy el recorrido entero es: se
registra con email, password y nombre (el `register.html` ya colecta `firstName`/`lastName`),
entra, crea planes, y no puede hacer nada más con su cuenta. No tiene perfil: los nombres solo
salen renderizados en el detalle de planes (`plans/detail.html` lista participantes) sin poder
verlos ni editarlos, y sin null-check consistente. No existe `/profile` en el área de
usuario. Y nunca cambia su contraseña: `UserService.rotatePassword(id)` solo se dispara desde el
panel admin o desde la recuperación con password temporal.

Esta spec tiene dos superficies:

1. **Vista y edición del propio perfil** (`/profile/{id}` con id de la sesión): datos personales,
   avatar, cambio de contraseña y secciones placeholder para features futuras (Guardados `[MARK]`,
   Seguidores `[FOLLOW]`).
2. **Vista pública del perfil de otro usuario** (`/profile/{id}` ajeno): read-only, sin datos
   privados (email/rol), con los planes públicos del dueño.

Además, el menú del navbar pasa a ser la caja de entrada del sitio: **modal global** con variantes
por sesión (autenticado: perfil, cambio de contraseña, Guardados/Siguiendo "pronto", Tema, Log
Out rojo; anónimo: login/register, Tema) y la **preferencia de tema** persiste por usuario:
localStorage para anónimos (mecanismo de [THEME]), `themePreference` en el usuario para
autenticados.

`GET /profile` redirige al propio `/profile/{id}`: la URL canónica siempre lleva el id, así que el
perfil es compartible desde la barra de direcciones sin ningún botón extra — el link abre la vista
pública **sin necesidad de sesión** — y deja listo el link-sharing de metadata (card `[META]`) a
futuro.

## Pre-requisitos

- [AUT] completed (`/auth/**` público, sesión con `maximumSessions(1)`, `BCryptPasswordEncoder`)
- [BRD] completado (esta spec agrega link/entradas al `navbar.html`)
- `UserController` es hoy 100% `/admin/users/**`. `/profile` es un mapeo **nuevo** en un
  `@Controller` separado, no una extensión del admin: su superficie de authorization es distinta.
- `SecurityConfig` tiene 3 filter chains (`/auth/**`, `/admin/**`, default). `/profile` cae en la
  default: sin sesión redirige a `/auth/login`.
- No hay infraestructura de upload en el proyecto (cero `MultipartFile`): el avatar es **URL-based**
  (patrón `Place.imageUrl`), no archivo subido. El upload real será su propia card a futuro.
- `[THEME]` vive en rama propia (`origin/THEME+Correciones-de-Explore`): persiste la elección en
  `localStorage["planit-theme"]` y deja un toggle standalone (`button[data-theme-toggle]`) en cada
  bloque del navbar (autenticado y anónimo). No bloquea el modal (PROF-009), pero sí la entrada
  "Tema" del modal y la persistencia server-side (PROF-010/011), que entran una vez mergeado.
- `[MARK]` y `[FOLLOW]` no son bloqueantes: sus secciones entran como placeholders no
  interactivos y se activan en las cards de cada feature. **Actualizado en v2.5:** `[FOLLOW]` ya
  los activó — `#follow`, sus dos listas y los contadores de seguidores/seguidos son reales, y la
  entrada "Siguiendo" del modal es un link a `/profile#follow`. Guardados (`#bookmarks`) sigue
  siendo el placeholder de `[MARK]`.

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | `GET /profile/{id}` con sesión propia muestra los datos del usuario logueado: email (read-only), `firstName`, `lastName`, rol |
| AC-02 | `POST /profile` (sin id en el path) actualiza `firstName` / `lastName` / `avatarUrl` del usuario de la sesión y redirige a `/profile/{id}` con un mensaje de éxito |
| AC-03 | `POST /profile` con nombre vacío devuelve errores de validación visibles, sin escribir nada |
| AC-04 | La escritura no acepta id: el usuario es siempre el de la sesión. El path `/profile/{id}` solo sirve vistas read-only de otros — nadie puede escribir sobre un id ajeno |
| AC-05 | `GET /profile` sin sesión redirige a `/auth/login` (atajo "mi perfil"). `GET /profile/{id}` de otro usuario es accesible **sin sesión**: muestra la vista pública |
| AC-06 | `GET /profile/change-password` muestra el form: actual, nueva, confirmación |
| AC-07 | `POST /profile/change-password` exige que la contraseña actual sea correcta. Una incorrecta vuelve al form con error y no cambia nada |
| AC-08 | `POST /profile/change-password` valida la nueva contra el `BCryptPasswordEncoder` existente (`@Size(min = 8)` y confirmación coincidente) |
| AC-09 | Después del cambio, la contraseña anterior deja de servir y la nueva sí: logout + login con la nueva |
| AC-10 | El navbar, con sesión `USER`, tiene acceso al perfil vía el modal del avatar |
| AC-11 | `plans/detail.html` deja de renderizar `firstName`/`lastName` en null para un usuario que completó su perfil |
| AC-12 | `GET /profile` con sesión redirige (302) a `/profile/{id}` del usuario de la sesión — la URL canónica siempre lleva el id |
| AC-13 | `GET /profile/{id}` sobre OTRO usuario (con o sin sesión) muestra vista read-only: nombre, avatar/iniciales, placeholder de seguidores y los planes públicos del dueño. Nunca renderiza email, rol ni nada de edición (form, cambio de contraseña) — sin sesión el `isSelf` es imposible y la brecha queda garantizada |
| AC-14 | `GET /profile/{id}` sobre el PROPIO id muestra la superficie de edición del propio perfil (form de nombres/avatar, email read-only, rol, link a cambio de contraseña) |
| AC-15 | La vista pública lista los planes **públicos** del dueño; los privados no aparecen |
| AC-16 | `avatarUrl` es opcional: con valor se renderiza la imagen; vacío cae al avatar de iniciales (patrón actual). El navbar (trigger del modal, `#navbar-avatar`) y el header del modal lo reflejan al vuelo — el avatar se guarda y la render siguiente ya muestra la imagen, sin JS ni cache |
| AC-17 | El navbar abre un **menú modal** en ambas sesiones. Autenticado (trigger: avatar, `<button>`): Ver perfil, Cambiar contraseña, Guardados (pronto), Siguiendo (pronto), Tema (activa) y Cerrar sesión (activa, roja) con la ruta correcta por rol (`/admin/logout` para ADMIN, `/auth/logout` para USER). Anónimo (trigger: botón de menú que reemplaza al toggle standalone): Iniciar sesión, Registrarse, Tema (activa) |
| AC-18 | Las entradas "pronto" (Guardados, Siguiendo) se renderizan deshabilitadas con etiqueta "Pronto", sin `<a>`/`<button>` navegables — no navegan hasta que su feature (MARK/FOLLOW) aterrice |
| AC-19 | El perfil propio contiene secciones placeholder no interactivas dentro de la página: Guardados (`#bookmarks`) y Seguidores (`#follow`) — ancladas y listas para activar. La sección Tema (`#theme`) se retiró en v2.4: el tema vive solo en el modal del navbar |
| AC-20 | `GET /profile/{id}` con id inexistente → 404 |
| AC-21 | La preferencia de tema persiste por usuario: anónimo en `localStorage["planit-theme"]` (mecanismo de [THEME]); autenticado sobre el usuario (`themePreference`) y se recupera al loguearse en cualquier dispositivo. El toggle provisional standalone del navbar desaparece cuando la entrada "Tema" del modal entra (PROF-010) |
| AC-22 | Todo nombre de usuario en vistas de planes es un link a `/profile/{id}`: autor en la lista pública (`public-list.html`) y la ficha pública (`public.html`); en el detalle interno (`detail.html`) el administrador deja de mostrarse por email y pasa a nombre + link, y los participantes también son clickeables. Las vistas admin no cambian |
| AC-23 | El registro acepta un avatar opcional: picker de los 3 defaults + URL propia; sin elección el usuario entra con el avatar de iniciales |
| AC-24 | El perfil de edición permite elegir avatar: botón editar sobre el avatar actual abre el picker (3 avatars incluidos + URL propia); vacío = iniciales |
| AC-25 | `avatarUrl` valida: solo acepta `https://…` o paths `/images/avatars/…`; rechaza otros esquemas (`javascript:`, `data:`, etc.) en registro y perfil |

**Fuera de alcance (explícito),** de esta spec: cambio de email (no se permite como producto),
borrado de cuenta (será card propia), upload de avatar como archivo (card propia cuando exista
infra de upload).

## Escenarios de Test

### Tests Unitarios (`domain/user/UserProfileTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | `updateProfile()` copia `firstName` / `lastName` | AC-02 |
| U-02 | `updateProfile()` no toca `email` ni `role` | AC-02 |
| U-03 | `User.changePassword(new)` re-codifica el hash con el encoder | AC-08, AC-09 |
| U-04 | `User.changePassword()` no guarda la contraseña en claro | AC-09 |
| U-05 | `updateProfile()` guarda `avatarUrl` cuando viene con valor | AC-16 |
| U-06 | `updateProfile()` con `avatarUrl` vacío la deja en null (fallback iniciales) | AC-16 |
| U-12 | `User.setThemePreference()` acepta los valores válidos y es null-safe (null → el front cae a localStorage/sistema) | AC-21 |

### Tests Unitarios (`infrastructure/user/UserServiceTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-07 | `updateProfile(id, ...)` con id existente guarda el cambio | AC-02 |
| U-08 | `updateProfile(id, ...)` con id inexistente devuelve empty / error, no revienta | AC-04 |
| U-09 | `changePassword(id, wrongCurrent, nueva)` rechaza y no escribe | AC-07 |
| U-10 | `changePassword(id, current, nueva)` con `current` correcta persiste el hash nuevo | AC-08, AC-09 |
| U-11 | `changePassword(id, current, nueva)` con confirmación distinta rechaza | AC-08 |
| U-13 | `updateThemePreference(id, pref)` con id existente persiste la preferencia | AC-21 |

### Tests de Integracion (`integration/ProfileControllerIntegrationTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-01 | `GET /profile/{id}` con sesión sobre el PROPIO id → 200 con email y nombres | AC-01, AC-14 |
| I-02 | `GET /profile` sin sesión → redirige a `/auth/login` | AC-05 |
| I-03 | `POST /profile` válido → 302 a `/profile/{id}`, el nombre persiste | AC-02 |
| I-04 | `POST /profile` con `firstName` vacío → 200 con error de campo | AC-03 |
| I-05 | `POST /profile` no acepta `id` de otro usuario: solo escribe el de la sesión | AC-04 |
| I-06 | `GET /profile/change-password` → 200 con los 3 campos | AC-06 |
| I-07 | `POST /profile/change-password` con actual incorrecta → 200 con error, la vieja sigue sirviendo | AC-07 |
| I-08 | `POST /profile/change-password` con nueva de 5 chars → 200 con error de validación | AC-08 |
| I-09 | `POST /profile/change-password` válido → la anterior da 401 y la nueva autentica | AC-09 |
| I-10 | `GET /plans/{id}` de un usuario con perfil completo muestra el nombre, no `null` | AC-11 |
| I-11 | `GET /places` con sesión: el navbar tiene el avatar con modal perfil | AC-10 |
| I-12 | `GET /profile` con sesión USER → 302 a `/profile/{idPropio}` | AC-12 |
| I-13 | `GET /profile/{id}` de UN USUARIO DIFERENTE (con sesión) → 200 read-only, sin email ni rol | AC-13 |
| I-14 | El usuario NO EXISTENTE en `GET /profile/{id}` → 404 | AC-20 |
| I-15 | La vista pública del perfil muestra los planes PUBLICOS del dueño y ningún privado | AC-15 |
| I-16 | `POST /profile` con `avatarUrl` válido → el usuario lo guarda | AC-16 |
| I-17 | `POST /profile` con `avatarUrl` vacío → null; el perfil cae al fallback de iniciales | AC-16 |
| I-18 | El perfil PROPIO renderiza las secciones placeholder con sus anclas (`#bookmarks`, `#follow`) y sin la sección Tema (v2.4: `#theme` ya no existe en la vista) | AC-19 |
| I-19 | El navbar renderiza el modal con sus entradas y sus estados (activas / pronto) | AC-17, AC-18 |
| I-20 | `GET` anónimo del navbar: el modal muestra Iniciar sesión, Registrarse y Tema — y ninguna entrada del perfil | AC-17 |
| I-21 | `POST /profile/theme` del usuario autenticado persiste `themePreference` | AC-21 |
| I-22 | La lista pública muestra el autor como link a `/profile/{id}` con el id correcto | AC-22 |
| I-23 | La ficha pública muestra el administrador como link a `/profile/{id}` | AC-22 |
| I-24 | `GET /plans/{id}` interno muestra el administrador por nombre, no por email, y liga a su `/profile/{id}` | AC-22 |
| I-25 | Los participantes en `detail.html` son links al perfil respectivo | AC-22 |
| I-26 | El registro con avatar default o URL propia lo persiste en el alta | AC-23 |
| I-27 | `POST /profile` y el registro rechazan `avatarUrl` con esquemas prohibidos y aceptan `https://…` / `/images/avatars/…` | AC-25 |
| I-28 | `GET /profile/{idAjeno}` ANÓNIMO → 200 con la vista pública (nombre, planes), sin marcado de edición | AC-05, AC-13 |
| I-29 | Tras `POST /profile` con avatar nuevo, la render siguiente del navbar lleva el `<img>` con ese src (y sin avatar vuelve la inicial) | AC-16 |

### Tests de Seguridad (`integration/ProfileSecurityTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| S-01 | `POST /profile` sin token CSRF → 403 | AC-02 |
| S-02 | `POST /profile/change-password` sin token CSRF → 403 | AC-08 |
| S-03 | `GET /profile/{idPropio}` con rol `ADMIN` responde 200 (el perfil propio es de todos los autenticados) | AC-01 |
| S-04 | `GET /profile/{idAjeno}` con sesión USER nunca contiene el email/rol de ese usuario (ni el texto del form de edición) | AC-13 |
| S-05 | `GET /profile` sin sesión → redirect a `/auth/login` (default chain); `GET /profile/{idAjeno}` sin sesión → 200 con la vista pública | AC-05, AC-12, AC-13 |
| S-06 | No hay endpoint de escritura con id ajeno: `POST` sobre `/profile/{idAjeno}` no existe como superficie (405/404) | AC-04 |
| S-07 | `POST /profile/theme` sin token CSRF → 403 | AC-21 |
| S-08 | La vista pública ANÓNIMA no renderiza marcado de edición: ni campos de form, ni link a `/profile/change-password`, ni datos privados | AC-13 |

### E2E (minimos)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `ProfileViewE2E` | Login → modal del navbar → "Ver perfil" → completar nombre → guardar → el nombre aparece en `/plans/{id}` | AC-01, AC-02, AC-10, AC-11 |
| E-02 | `ProfileViewE2E` | Login → cambiar contraseña con la actual incorrecta → error; con la correcta → logout → login con la nueva | AC-07, AC-08, AC-09 |
| E-03 | `ProfileViewE2E` | Login (USER) → click avatar → modal con sus entradas e iconos; "Guardados"/"Siguiendo" disabled y NO navegan; "Ver perfil" lleva a `/profile/{id}`; Log Out (rojo) cierra sesión | AC-17, AC-18 |
| E-04 | `ProfileViewE2E` | Login → completar avatar URL → el avatar se renderiza; dejarlo vacío → vuelve el avatar de iniciales | AC-16 |
| E-05 | `ProfileViewE2E` | Anónimo: cambiar tema desde el modal → persiste tras reload (localStorage). Login: cambiar tema → logout → login en otra página → la preferencia sobrevive (`themePreference`) | AC-21 |
| E-06 | `ProfileViewE2E` | Login → editar avatar → elegir uno default → se renderiza en navbar y perfil; elegir "URL propia" → se renderiza; vacío → vuelve el avatar de iniciales | AC-24, AC-16 |

> Nota E2E: los helpers del navbar cuentan `body > nav a, body > nav button`. El avatar pasa de
> `<span>` a `<button>` (abre el modal) y el anónimo gana un `<button>` de menú como trigger,
> así que esos helpers y/o los items del navbar pueden cambiar: actualizar los helpers E2E está
> EN esta spec, no es un ajuste aparte. El modal
> permanece oculto (sin marcado navegable) hasta abrirse, para no inflar el conteo por defecto.

## Notas / decisiones de diseño

- **La URL canónica lleva el id: `/profile/{id}`.** `GET /profile` es solo un atajo que
  redirige. Razones: (1) el perfil es compartible desde la barra de direcciones sin botón extra;
  (2) la card `[META]` (metadata para link sharing de plans/places/perfiles) solo funciona si la
  URL lleva el id; (3) una sola route con branch `isSelf` (sesión vs path) evita duplicar
  `/my-profile` + URL pública. La sesión decide: id propio → superficie de edición (+ datos
  privados), id ajeno → vista pública read-only.
- **Qué expone la vista pública:** nombre, avatar/iniciales, contadores **reales** de
  seguidores/seguidos y la sección `#follow` con sus dos listas (`[FOLLOW]`, v2.5 — el
  `followerCount = 0` de v2 era el placeholder) y los planes **públicos** del dueño. Email, rol y
  edición son de la superficie propia; nunca aparecen para un visitante — y el visitante puede ser
  anónimo: el link
  compartido abre la vista pública sin sesión (AC-05/AC-13; el `isSelf` solo puede ser cierto con
  sesión autenticada). La diferencia entre perfil y CRUD de
  administración sigue en pie: `UserController` resuelve el caso admin con `/admin/users/{id}` y
  su `@PreAuthorize`; `/profile` no reusa esa superficie.
- **Verificar la contraseña actual es obligatorio**, no cosmético: sin ese paso, un XSS o un
  dispositivo compartido cambian la cuenta con la sesión abierta. La comparación va contra el
  `BCryptPasswordEncoder` existente, que es lo que hace `LoginService.validateLogin`. Se reusa, no
  se reimplementa el encoder.
- **`maximumSessions(1)` interactúa con el cambio de contraseña.** Cambiar la propia password no
  invalida la sesión actual: si lo hiciera, el `POST` devolvería 302 a `/auth/login` y el usuario
  perdería lo que estaba escribiendo. E-02 verifica el flujo completo (cambio → logout → login con
  la nueva) justamente para que el cambio de contraseña sea observable desde el navegador.
- **Avatar = URL incluida o propia, con picker.** No hay `MultipartFile` en el proyecto: las
  imágenes hoy son strings (`Place.imageUrl`, covers `/images/plans/cover-N.svg`). El avatar sigue
  el mismo patrón: `avatarUrl` nullable en `User`, con la diferencia de que aquí viene de un
  **picker**: 3 avatars incluidos con la app (`/images/avatars/avatar-1..3.svg`, SVGs de marca
  estilo los covers de plan, commitados en el repo) más la opción de URL propia. El mismo partial
  de picker se reusa en registro (donde el form ya colecta `firstName`/`lastName`) y en la edición
  de perfil. Validación en todo punto de escritura: `https://…` o `/images/avatars/…`; esquemas
  `javascript:`/`data:` rechazados (el perfil es público, el URL se inyecta en `<img>` — la
  validez estricta es la defensa). Fallback al componente de iniciales cuando queda vacío.
  El upload real (storage, validación MIME, cleanup) será card propia.
- **El email del administrador deja de ser identidad visible en `detail.html`.** Hoy
  `plans/detail.html:254` muestra el email del admin; pasa a nombre + link al perfil
  (AC-22), como ya hacen las vistas públicas. Los DTOs de esas vistas llevan hoy
  `administratorName` sin id: se agrega `administratorId` (y cuando aplique
  `participantIds`/`participant.id`) para evitar pasar el `User` entero a la vista. La
  superficie `/admin/users` no se toca: no es pública.
- **El modal es el menú global del sitio, no solo la caja del perfil.** Click en el avatar
  (autenticado) o en el botón de menú (anónimo) abre un panel con entradas según sesión. El
  autenticado ve: header (iniciales + nombre), Ver perfil, Cambiar contraseña, Guardados
  (pronto), Siguiendo (navegable desde v2.5), Tema y Cerrar sesión (roja). El anónimo ve: Iniciar
  sesión, Registrarse, Tema. Las entradas pronto son deshabilitadas visibles — "no interactivo hasta
  que la feature existe", no ocultas: anuncian el roadmap y mantienen el layout estable. Los anchors
  `#bookmarks` / `#follow` ya existen en la vista propia desde v2: `[MARK]` sigue siendo un cambio
  de una línea en el href cuando aterrice, y `[FOLLOW]` ya lo hizo (v2.5: "Siguiendo" linkea a
  `/profile#follow`). Implementación mínima (JS vanilla embebido o `<details>`),
  accesible: `aria-expanded`, cierre con Esc y click-afuera.
- **Secciones placeholder en el perfil propio:** Guardados (`#bookmarks`) y Seguidores/Siguiendo
  (`#follow`) con copy "a futuro"; los placeholders son la casa de estos settings, y el modal es el
  acceso rápido a cada uno. Los placeholders distribuyen quién es OWNER final de cada archivo: PROF
  no compite con THEME/MARK/FOLLOW por los mismos archivos más adelante. **v2.5:** `#follow` dejó
  de ser placeholder — `[FOLLOW]` lo provee con contadores y las dos listas (seguidores/seguidos)
  en `edit.html` y `view.html`; Guardados (`#bookmarks`) sigue siendo el placeholder de `[MARK]`.
  La sección Tema (`#theme`) se retiró de la vista en v2.4 (vive solo en el modal).
- **El toggle de tema entra al modal escalonado y persiste por sesión.** Etapa 1 (PROF-009): el
  modal sin entrada de tema, el toggle provisorio de [THEME] sigue de pie. Etapa 2 (PROF-010, tras
  el merge de [THEME]): la entrada "Tema" entra al modal para las dos sesiones, y el toggle
  standalone del navbar desaparece. Persistencia: anónimos siguen en
  `localStorage["planit-theme"]` (mecanismo de [THEME], sin backend); autenticados persisten en
  su usuario (`themePreference` en `User`, columna nueva — sin capa de migración en el proyecto:
  JPA auto-DDL + reset de schema si hace falta, avisando en la card como pide la convención) y
  esa preferencia gana sobre el localStorage al renderizar (la página pintaría con la preferencia
  del usuario antes de la primera animación de carga, resolviendo el `data-theme` server-side con
  fallback al valor previo guardado). Con `themePreference` vacía, el autenticado cae al
  localStorage/sistema, como hoy.
- **El gate de JaCoCo no deja margen.** La regla de `pom.xml` pone
  `com.valhalla.domain.*` **y** `com.valhalla.presentation.*` al **100%** de LINE COVEREDRATIO a
  nivel `PACKAGE`. `User` está excluida por nombre PERO los métodos nuevos que se agreguen sí
  cuentan: o tienen test, o CI falla. Los DTOs (`*Request.class`/`*View.class`) están excluidos.
- **No tocar `SecurityConfig`.** `/profile` cae en la cadena default, que ya manda a
  `/auth/login` a los no autenticados. Agregar una cadena sería romper la convención de 3 chains
  que [AUT] instaló.
- **Conflicto de sprint**: PROF toca `navbar.html`, así que no puede ir en paralelo con BRD ni con
  LAND. Los placeholders de THEME/MARK/FOLLOW evitan que PROF compita por los mismos archivos más
  adelante.

## Referencia de Implementacion

> Los pasos a continuación son guía de implementación, no reemplazan los acceptance criteria de arriba.

### 1. Métodos de dominio en `User`

File: `src/main/java/com/valhalla/domain/user/User.java`

`User` ya tiene `firstName` y `lastName`; agrega `avatarUrl` (nullable), `themePreference`
(nullable hasta PROF-011) y los métodos de
actualización y cambio de contraseña. Importante: `User.java` está en los `<excludes>` de JaCoCo,
pero **sí** los métodos que se agreguen — si el método nuevo tiene líneas, tiene test.

```java
public void updateProfile(String firstName, String lastName, String avatarUrl) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.avatarUrl = normalize(avatarUrl); // blank -> null
}

public void changePassword(String newRawPassword, PasswordEncoder encoder) {
    this.password = encoder.encode(newRawPassword);
}
```

### 2. `UserService`: profile y password

File: `src/main/java/com/valhalla/infrastructure/user/UserService.java` (interfaz en
`domain/user/UserService.java`, impl en `infrastructure/user/UserServiceImpl.java`)

- `updateProfile(Long id, String firstName, String lastName, String avatarUrl)` — busca,
  actualiza y guarda; devuelve la sesión inválida si el usuario no existe.
- `changePassword(Long id, String currentRaw, String newRaw)` — busca, compara con
  `passwordEncoder.matches(currentRaw, user.getPassword())`; si no coincide, devuelve error **sin
  escribir**; si coincide, `user.changePassword(newRaw, passwordEncoder)` y `save`.
- `updateThemePreference(Long id, String pref)` (PROF-011) — busca, setea `themePreference` y
  guarda; solo valores del enum del tema.

El encoder es el mismo bean inyectado que usa `LoginService`. `UserService.rotatePassword(id)` se
queda como está: lo usan el panel admin y la recuperación de [AUT].

### 3. `ProfileController` y vistas

File: `src/main/java/com/valhalla/presentation/profile/ProfileController.java`

```java
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final PlanQueryService planQueryService; // planes públicos del dueño

    @GetMapping
    public String own(@AuthenticationPrincipal AppUserDetails me) {
        return "redirect:/profile/" + me.getId();
    }

    @GetMapping("/{id:\\d+}")
    public ModelAndView view(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails me) {
        User owner = userService.getById(id); // empty -> 404 (AC-20)
        boolean self = owner.getId().equals(me.getId());
        if (self) return editView(owner);                       // AC-01/AC-14
        return publicView(owner, planQueryService.publicPlansOf(owner)); // AC-13/AC-15
    }

    @PostMapping
    public String updateProfile(@AuthenticationPrincipal AppUserDetails me,
                                @Valid @ModelAttribute("profileForm") ProfileForm form,
                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return "pages/profile/edit";
        userService.updateProfile(me.getId(), form.getFirstName(),
                                  form.getLastName(), form.getAvatarUrl());
        return "redirect:/profile/" + me.getId() + "?updated=true";
    }

    @GetMapping("/change-password")
    public ModelAndView changePasswordForm() { ... }   // igual a v1

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal AppUserDetails me,
                                 @Valid @ModelAttribute("passwordForm") ChangePasswordForm form,
                                 BindingResult bindingResult) { ... } // igual a v1

    @PostMapping("/theme")                   // PROF-011, POST desde el modal (CSRF + sesión)
    public String setTheme(@AuthenticationPrincipal AppUserDetails me,
                           @RequestParam("theme") String theme) {
        userService.updateThemePreference(me.getId(), theme);
        return "redirect:" + currentPath;    // volver a donde estaba el usuario
    }
}
```

- `ProfileForm` (nombres + `avatarUrl` opcional), `ChangePasswordForm` — DTOs en
  `presentation/profile/`, excluidos de JaCoCo. El usuario de la **escritura** sale SIEMPRE de la
  sesión; el path solo alimenta la vista.
- Sin DTO de vista: `ProfileController#publicModel` arma un `Map` con `owner`, `ownerInitial`,
  `profileName`, `profileAvatarUrl`, `followerCount`, `followingCount`, `followers`, `following`,
  `isFollowing` y `profilePlans`. **v2.5:** `[FOLLOW]` reemplazó el contador placeholder por los
  conteos reales de seguidores/seguidos y agregó las dos listas de `#follow`.
- El mapeo `/{id:\\d+}` evita colisiones con los sub-paths existentes (`/profile/change-password`,
  `/profile/theme`).

### 4. Templates

| Archivo | Contenido |
|---------|-----------|
| `templates/pages/profile/edit.html` | Superficie propia: form de `firstName`/`lastName`/`avatarUrl` con `th:action="@{/profile}"` + CSRF, email read-only, rol, link a `/profile/change-password`, aviso `?updated=true`, `th:errors`. Debajo, secciones con anclas: Guardados (`id="bookmarks"`, placeholder de `[MARK]`) y Seguidores/Seguidos (`id="follow"`, real desde v2.5 con `[FOLLOW]`: contadores y las dos listas) — la sección Tema (`id="theme"`) se retiró en v2.4 |
| `templates/pages/profile/view.html` | Vista pública: header (avatar/iniciales + nombre + contadores reales de seguidores/seguidos desde v2.5), sección `#follow` con las dos listas, grilla/lista de `profilePlans` reutilizando los patrones de `plans/public-list.html` |
| `templates/components/navbar.html` | El avatar pasa de `<span>` a `<button>` que abre el modal (autenticado); el anónimo gana un `<button>` de menú como trigger. El botón renderiza el `avatarUrl` vigente (`userAvatarUrl`, resuelto en cada request por `CurrentUserControllerAdvice`) o cae a la inicial: guardar el perfil actualiza el navbar en el render del redirect. Modal con dos variantes: autenticado — header (avatar/iniciales + nombre), Ver perfil → `/profile/{id}`, Cambiar contraseña, Guardados **deshabilitado con badge "Pronto"**, Siguiendo como link a `/profile#follow` (v2.5, `[FOLLOW]`), Tema (PROF-010, tras merge de [THEME]), Cerrar sesión (roja) con ruta por rol + CSRF; anónimo — Iniciar sesión, Registrarse, Tema. El toggle standalone de [THEME] desaparece de ambos bloques cuando la entrada "Tema" entra (PROF-010). Cierre con Esc/click afuera, `aria-expanded` |
| `templates/layouts/base.html` | PROF-011: `data-theme` server-side para autenticados (la `themePreference` del usuario gana sobre `localStorage["planit-theme"]`; null → localStorage/sistema como hoy). Sin flash de tema incorrecto |
| `templates/components/avatar-picker.html` | Partial compartido del picker: el avatar actual (o iniciales), botón "editar" que abre la grilla de los 3 defaults + campo "URL propia". Reusado en `register.html` y `profile/edit.html` |
| `templates/pages/auth/user/register.html` | Agrega el partial del picker (campo opcional) — el form ya colecta nombre/apellido, no re-diseña el alta |
| `templates/pages/plans/public-list.html` | "Por {administratorName}" como link a `/profile/{administratorId}` |
| `templates/pages/plans/public.html` | El nombre del administrador como link, igual criterio |
| `templates/pages/plans/detail.html` | El administrador pasa de email a nombre + link (línea 254); participantes (`línea 290`) como links a sus perfiles |

### 5. Avatars incluidos

`src/main/webapp/resources/images/avatars/avatar-1.svg .. avatar-3.svg` — SVGs simples producidos
con la paleta de marca (`brand.css`: primary/secondary), misma mecánica que los covers
(`/images/plans/cover-N.svg` vía `PlanFeedCards`). Sirve el resource handler `/images/**` que ya
existe; sin nuevo config. El picker los referencia como paths absolutos, así el valor persistido
en `avatarUrl` es estable ante cambios de dominio.

El detalle de planes ya renderiza `firstName`/`lastName` de los participantes (y ahora muestra al
administrador por nombre con link al perfil): no se re-plantea, solo se verifica que dejen de
salir en null (I-10).

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `domain/user/User.java` | Actualizar (`avatarUrl`, `updateProfile`, `changePassword`) |
| `domain/user/UserService.java` | Actualizar (interface: `updateProfile` con avatar, `updateThemePreference`) |
| `infrastructure/user/UserServiceImpl.java` | Actualizar (persistencia de `avatarUrl` y `themePreference`) |
| `presentation/profile/ProfileController.java` | Crear (`/profile` redirect, `/{id}` dual, `POST /profile`, change-password) |
| `presentation/profile/ProfileForm.java`, `ChangePasswordForm.java` | Crear (DTOs) |
| `presentation/shared/` o `domain/plan/` | Consulta de planes públicos por owner (según dónde viva hoy la query de planes) |
| `templates/pages/profile/edit.html` | Crear (superficie propia + placeholders) |
| `templates/pages/profile/view.html` | Crear (vista pública + planes públicos) |
| `templates/pages/profile/change-password.html` | Crear |
| `templates/components/navbar.html` | Actualizar (avatar → button + menú modal de dos variantes) |
| `templates/layouts/base.html` | Actualizar (PROF-011: resolución server-side de `data-theme` para autenticados) |
| `templates/components/avatar-picker.html` | Crear (partial del picker, reusado en registro y perfil) |
| `templates/pages/auth/user/register.html` + su form/controller | Actualizar (campo opcional `avatarUrl` con validación AC-25) |
| `resources/images/avatars/avatar-1..3.svg` | Crear (avatars default de marca) |
| `templates/pages/plans/{public-list,public,detail}.html` | Actualizar (links a `/profile/{id}`; detail: email → nombre) |
| DTOs de plan (cards/view) con `administratorName` | Actualizar (exponer `administratorId` para armar los links) |
| helpers E2E de navbar | Actualizar (conteo `nav a, nav button` con el avatar y el trigger anónimo ahora buttons) |
