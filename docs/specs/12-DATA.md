# [DATA] ABM de lugares + importador desde OpenStreetMap

> Trello: https://trello.com/c/ExZAxLQ5
> **Estimación:** 8 pts Fibonacci
> **Note:** This is an SDD proposal. Implementation may change based on team decisions.

## Objetivo

El catálogo de lugares deja de ser solo un seeder hardcodeado: un ADMIN puede dar de alta,
editar y dar de baja places desde `/admin/places`, siguiendo el mismo patron que el ABM de
users (`/admin/users`). Ademas, el ABM incorpora una vista "Importar del mapa" que consulta
Overpass API desde el backend y muestra POIs reales de CABA en un mapa Leaflet: al elegir un
POI, el form del ABM queda pre-lleno (nombre, direccion, coordenadas, horarios, telefono, web
y categoria OSM mapeada); el admin revisa, ajusta categoria y foto, y guarda. Sin API key y
sin dependencias de red en ningun test.

## Pre-requisitos

- [AUT] completed (cadena `/admin/**` de `SecurityConfig`: `hasRole("ADMIN")`; el ABM no
  necesita agregar reglas de seguridad, solo tests que lo verifiquen)
- [PLC] completed (`Place`, `PlaceCategory`, `PlaceRepository`, `PlaceService`,
  `PlaceRestController`, `/explore` con Leaflet)
- [BRD] completed (los colores de categoria salen de los tokens `--category-*`; esta spec no
  introduce hex nuevos — de hecho elimina el ultimo hex atado a un test, `#e74c3c` en
  `PlacesViewE2E`)

## Criterios de Aceptacion

| # | Criterio |
|---|----------|
| AC-01 | `/admin/places` lista, crea, edita y da de baja places; los cambios persisten en la base y son visibles en `/explore` y `GET /api/places` |
| AC-02 | `imageUrl` es opcional en el form: vacio guarda `null` y la vista cae al default `/images/place-placeholder.svg`; con una URL cargada, `/explore` muestra esa imagen |
| AC-03 | Un usuario con rol USER no accede a `/admin/places*`: la cadena `/admin/**` lo redirige a su home sin renderizar el ABM; un anonimo queda redirigido sin renderizarlo tampoco |
| AC-04 | La vista "Importar del mapa" muestra POIs reales de CABA (amenities/comercios) en un mapa Leaflet; la consulta a Overpass la hace el backend a traves de la interfaz `OverpassClient`, sin API key |
| AC-05 | Al elegir un POI en el importador, el form del ABM queda pre-lleno con nombre, direccion, coordenadas, horarios, telefono, web y categoria OSM mapeada; el admin puede revisar y cambiar cualquier campo antes de guardar, y guardar crea el place con esos datos |
| AC-06 | Tags OSM sin mapping explicito caen en `OTHER` y el admin siempre puede cambiar la categoria antes de guardar |
| AC-07 | El dataset mantiene los 10 places actuales del seeder y agrega lugares nuevos; cada categoria de `PlaceCategory` tiene al menos un lugar (hoy `SPORT` y `OTHER` no tienen ninguno) |
| AC-08 | El filtro por categoria y la busqueda por nombre de `/explore` siguen funcionando sobre el dataset nuevo |
| AC-09 | `PlacesViewE2E` queda recalibrado contra el dataset nuevo y deja de atar el test al hex de color: el marker se identifica por su categoria (`data-category`), no por `background-color:#e74c3c` |
| AC-10 | Ningun test de integracion ni E2E depende de red: `OverpassClient` se mockea en los tests; ninguna suite golpea Overpass ni Nominatim |
| AC-11 | No se puede dar de baja un lugar que forma parte de planes: el ABM informa el motivo, no borra, y el place sigue en la base (la FK `plan_places.place_id` no se rompe) |

## Escenarios de Test

### Tests Unitarios (`domain/place/PlaceCategoryTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-01 | `fromOsm("amenity", "restaurant")` → `RESTAURANT`; lo mismo para `fast_food` | AC-06 |
| U-02 | `fromOsm("amenity", "bar")` / `("amenity", "pub")` → `BAR`; `("amenity", "nightclub")` → `NIGHTLIFE`; `("amenity", "museum")` → `MUSEUM` | AC-06 |
| U-03 | `fromOsm("shop", "*")` → `SHOPPING` para cualquier valor de shop | AC-06 |
| U-04 | Tag sin mapping (p. ej. `("amenity", "vending_machine")`) → `OTHER`; null o blank → `OTHER` | AC-06 |

### Tests Unitarios (`infrastructure/PlaceDataSeederTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-05 | El seeder no corre si `placeRepository.count() > 0` (idempotente) | AC-07 |
| U-06 | El seeder siembra el dataset nuevo completo (12 lugares) | AC-07 |
| U-07 | Cada categoria de `PlaceCategory` tiene al menos un lugar sembrado | AC-07 |
| U-08 | Los 10 lugares originales siguen presentes con su nombre y categoria | AC-07 |
| U-09 | Ningun lugar del seeder setea `imageUrl` (todos `null`: el placeholder es el default) | AC-02 |

### Tests Unitarios (`infrastructure/place/HttpOverpassClientTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-10 | Parsea una respuesta Overpass de fixture (elemento `nwr` con `center` y `tags`) al `OsmPoi` correcto: nombre, direccion armada desde `addr:*`, lat/lng del center | AC-04 |
| U-11 | Tags ausentes dejan los campos en `null` (sin phone/web/horarios no hay excepcion); `contact:phone` es fallback de `phone` | AC-04, AC-05 |
| U-12 | Elementos sin `name` se descartan | AC-04 |
| U-13 | La query sale por bounding box de CABA (sin `area`) y valida key/value contra el allowlist | AC-04 |
| U-14 | Reintenta una vez ante 429 (esperando el delay inyectado), una vez ante 502 y hasta dos veces en total; un 500 no se reintenta | AC-04 |

### Tests de Integracion (`integration/PlaceRestControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-01 | `GET /api/places` retorna el dataset completo del seeder (12 lugares) | AC-07 |
| I-02 | `GET /api/places?category=SPORT` retorna al menos 1 (hoy 0) | AC-07 |
| I-03 | `GET /api/places?category=OTHER` retorna al menos 1 (hoy 0) | AC-07 |
| I-04 | `GET /api/places?search=Don` sigue retornando exactamente "Parrilla Don Julio" | AC-08 |
| I-05 | `GET /api/places` trae `imageUrl: null` en todos los lugares del seeder | AC-02 |

### Tests de Integracion (`integration/PlaceAdminControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-06 | `GET /admin/places` responde 200 y lista los lugares del seeder | AC-01 |
| I-07 | `POST /admin/places` con datos validos crea el place y aparece en el listado y en `GET /api/places` | AC-01 |
| I-08 | `POST /admin/places/{id}` (PUT) edita nombre/categoria/imagen y los cambios persisten | AC-01 |
| I-09 | `POST /admin/places/{id}/delete` da de baja: el place desaparece del listado y de `GET /api/places` | AC-01 |
| I-10 | `POST /admin/places` sin `name` o sin `category` re-renderiza el form con error y no persiste | AC-01 |
| I-11 | Borrar un lugar referenciado en `plan_places` es rechazado con mensaje, el place sigue en la base y el plan conserva su entrada | AC-11 |
| I-12 | Alta sin `imageUrl` guarda `null` | AC-02 |

### Tests de Integracion (`integration/PlaceImageTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-13 | Un place con `imageUrl` cargada via ABM renderiza esa URL como `img src` en `/explore` | AC-02 |
| I-14 | Un place sin `imageUrl` renderiza `/images/place-placeholder.svg` en `/explore` | AC-02 |

### Tests de Integracion (`integration/PlaceImportControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-15 | `GET /admin/places/import` responde 200 con la vista del mapa | AC-04 |
| I-16 | `GET /admin/places/import/pois?amenity=restaurant` con `OverpassClient` mockeado retorna los POIs del fixture como JSON — sin red | AC-04, AC-10 |
| I-17 | `GET /admin/places/new?name=...&latitude=...&category=...` pre-llena el form (server-side, via query params) | AC-05 |
| I-18 | `POST /admin/places` con los datos pre-llenos crea el place con coordenadas, horarios, telefono, web y categoria mapeada | AC-05 |
| I-19 | `GET /admin/places/import/pois?color=red` (query param distinto de `amenity`/`shop`) responde 400 sin tocar al cliente | AC-04, AC-10 |

### Tests de Seguridad (`integration/PlaceAdminSecurityTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| S-01 | USER con sesion: `GET /admin/places` no renderiza el ABM (redirect a su home via `HomeOnAccessDeniedHandler`) | AC-03 |
| S-02 | Anonimo: `GET /admin/places` queda redirigido sin renderizar el ABM | AC-03 |
| S-03 | USER con sesion: `POST /admin/places` / `POST /admin/places/{id}/delete` no persisten ningun cambio | AC-03 |
| S-04 | USER con sesion: `GET /admin/places/import/pois` no responde POIs (mismo destino que S-01) | AC-03 |

### E2E (minimos)

| # | Test | Flujo | AC que cubre |
|---|------|-------|-------------|
| E-01 | `e2e/PlacesViewE2E.java` (recalibrado) | Abrir `/explore`: 12 cards y 12 markers; markers de categoria RESTAURANT identificados por `data-category` (2), sin asertar el hex | AC-07, AC-09 |
| E-02 | `e2e/PlacesViewE2E.java` (recalibrado) | Filtrar RESTAURANT → 2 lugares; buscar "Don" → 1; Clear Filters → 12 | AC-08, AC-09 |
| E-03 | `e2e/PlacesViewE2E.java` (recalibrado) | Seleccionar un lugar del seeder (sin imagen): el panel muestra `/images/place-placeholder.svg` | AC-02 |
| E-04 | `e2e/PlaceViewABME2E.java` | Admin crea un place desde `/admin/places/new` → aparece en el listado del ABM y en `/explore` | AC-01 |
| E-05 | `e2e/PlaceViewABME2E.java` | Admin edita el place creado → los cambios se ven en el listado y en `/explore` | AC-01 |
| E-06 | `e2e/PlaceViewABME2E.java` | Admin da de baja el place → desaparece del listado y de `/explore` | AC-01 |
| E-07 | `e2e/PlaceImportE2E.java` | Admin abre `/admin/places/import` con el proxy stubbeado por `page.route`: la lista muestra los 3 POIs del fixture, se dibujan solo los 2 con coordenadas, y elegir uno abre `/admin/places/new` con el form pre-llenado (nombre, categoria, direccion, lat/lng, horarios, telefono, web) | AC-04, AC-05, AC-10 |
| E-08 | `e2e/PlaceImportE2E.java` | Con el proxy stubbeado en 502, la vista muestra el mensaje de error y la lista queda vacia | AC-04, AC-10 |

El importador tiene E2E sin tocar la red: el navegador ejecuta la vista real y `page.route`
intercepta `GET /admin/places/import/pois` antes de que salga, respondiendo un fixture (o un 502).
Es el mismo criterio que usa la integracion con `OverpassClient` mockeado: el doble vive en el
borde (red del browser / interfaz de dominio), nunca dentro de la logica de la vista.

## Notas / decisiones de diseño

- **Estado actual conservado.** `Place.imageUrl` existe pero `PlaceDataSeeder` nunca lo setea:
  los 10 lugares devuelven `null` y la vista cae al placeholder via
  `place.imageUrl || '/images/place-placeholder.svg'`. En el ABM el placeholder queda como
  default, no como excepcion.
- **`PlacesViewE2E` fija el dataset en 5 puntos** (recalibrar, no solo cambiar un numero):

  | Punto | Asercion actual |
  |-------|-----------------|
  | `signInAndOpenPlaces` (setup) y fin del test de filtros | `waitForPlaceCount(10)` ×2 |
  | `shouldRenderSeededPlacesAndKeepMarkersInSyncWithFilters` | `markers().count() == 10` y `placeCards().count() == 10` |
  | filtro por categoria | `div[style*='background-color:#e74c3c']` == 2 (hex de RESTAURANT) |
  | filtro / busqueda | `waitForPlaceCount(2)` y `waitForPlaceCount(1)` |

  Con el dataset nuevo (12 lugares, 2 RESTAURANT) los numeros de filtro no cambian, pero el
  total si, y el selector por hex se reemplaza por `div[data-category='RESTAURANT']` (ver
  Referencia, paso 7). El marker en `list.html` gana el atributo `data-category`, que hace el
  assert legible y sobrevive a cualquier cambio de tokens de color.
- **`ResetDatabase` hoy borra solo `plans` y `users`.** `PlaceDataSeeder` solo corre si
  `placeRepository.count() == 0`, asi que sin `DELETE FROM places` el dataset nuevo nunca
  llega a la base de E2E. Van `DELETE FROM places` + `ALTER SEQUENCE places_id_seq RESTART
  WITH 1` **antes** de `DELETE FROM plans` (el orden de FK importa: `plan_places` se borra
  aun antes, ya lo hace).
- **Baja de places y la FK `plan_places.place_id`.** `PlanPlace.place` es `@ManyToOne` sin
  cascade: un `DELETE` de un lugar referenciado por planes revienta contra la FK. El ABM lo
  previene chequeando `existsByPlaceId` antes de borrar y mostrando el motivo (AC-11). No se
  propaga el borrado a los planes: eso destruiria itinerarios de usuarios.
- **Horarios, telefono y web.** El importador pre-llena esos campos porque Overpass los
  devuelve (`opening_hours`, `phone`/`contact:phone`, `website`), y `Place` no tiene donde
  guardarlos: se agregan tres columnas opcionales (`opening_hours`, `phone`, `website`,
  todas nullable). No se muestran en el panel de `/explore` en esta card; mostrarlas ahi es
  un follow-up de producto, no parte del alcance.
- **Overpass API — hechos.**
  - Endpoint principal: `https://overpass-api.de/api/interpreter`; mirror sin limites:
    `https://overpass.private.coffee/api/interpreter`. Sin API key.
  - Query: `[out:json][timeout:60];nwr["amenity"="restaurant"]["name"](-34.7058,-58.5315,
    -34.5266,-58.3351);out center tags 300;` — se consulta por **bounding box** de CABA
    (sur,oeste,norte,este; sale del bbox de la relation 3082668 en Nominatim) y no por `area`:
    el endpoint publico responde 502 a las consultas por area cuando esta saturado, y el area id
    que decia esta spec (`3600345769`) era la relation de **Huesca, Espana** — devolvia cero
    POIs. Con `["name"]` y `out ... 300` se acota la respuesta (99 KB en vez de 870 KB).
  - Latencia real medida del endpoint publico: **25–47 s** por consulta sin cachear. Por eso el
    presupuesto del servidor es 60 s y el timeout del `HttpClient` 90 s: con 30 s el cliente
    cortaba antes de que Overpass contestara.
  - Devuelve tags `name`, `addr:*`, `opening_hours`, `phone`/`contact:phone`, `website`,
    `cuisine`, y el tipo (`amenity=*` / `shop=*`). NO devuelve fotos ni ratings: importar
    imagenes o puntajes queda fuera de alcance.
  - Uso automatizado moderado: ~100 queries/dia y 10 MB/dia en el server principal. Errores
    transitorios: **429** (limite por IP: esperar 30 s) y **502/503/504** (cola llena: esperar
    1 s). Se reintenta hasta dos veces en total; si sigue fallando, el proxy responde 502 y la
    vista invita a probar de nuevo.
- **Nominatim — hechos.** `https://nominatim.openstreetmap.org/search` (o Photon) hace
  geocoding por nombre; sin key, requiere un `User-Agent` identificable, 1 req/s. No es
  necesario en el flujo principal del importador (Overpass ya devuelve coordenadas del POI
  elegido); queda documentado por si se agrega busqueda por nombre despues.
- **Atribucion ODbL obligatoria.** Todo mapa que muestre datos OSM (el importador) debe
  incluir `© OpenStreetMap contributors` en la attribution del tile layer, igual que ya hace
  `/explore` con
  `attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'`.
- **Mapping `amenity`/`shop` OSM → `PlaceCategory`.** Mapping explicito en
  `PlaceCategory.fromOsm(key, value)` (ver Referencia, paso 4). Lo no mapeado cae en `OTHER`,
  y el admin siempre puede cambiarlo antes de guardar (el select del form es el mismo para el
  alta manual y la pre-llena): la categoria sugerida es una sugerencia, no una decision.
- **El importador depende del backend, nunca del navegador contra Overpass.** El mapa de la
  vista importa POIs desde `GET /admin/places/import/pois`, que es un proxy del backend sobre
  `OverpassClient`. La interfaz en el dominio permite mockearlo con `@MockBean` en los tests
  de integracion y sustituir el endpoint en el futuro (ej. Photon) sin tocar la vista.
- **Sin clustering.** Con 12 lugares en CABA los pines no se agrupan: no se agrega el plugin
  de clustering (decision de [CONTEXT.md] D7).
- **El seeder sigue sin setear `imageUrl`.** Las imagenes reales de places las carga el admin
  via ABM; el dataset de prueba no inventa assets.

## Referencia de Implementacion

> Los pasos a continuacion son guia de implementacion, no reemplazan los criterios de
> aceptacion de arriba.

### 1. `Place`: columnas opcionales + borrado

File: `src/main/java/com/valhalla/domain/place/Place.java`

Tres columnas nuevas, todas nullable (mismo criterio que `address`):

```java
@Column(name = "opening_hours")
private String openingHours;

private String phone;

private String website;
```

Con getters y setters, y lo mismo que `description` ya resuelve: nada de `@Lob` (en PostgreSQL
`@Lob` es un `oid` que exige transaccion para leerse en auto-commit).

File: `src/main/java/com/valhalla/domain/place/PlaceRepository.java` — el ABM necesita borrar:

```java
void deleteById(Long id);
```

File: `src/main/java/com/valhalla/infrastructure/place/PlaceRepositoryImpl.java` delega a
`jpaPlaceRepository.deleteById(id)` (`JpaPlaceRepository` ya extiende `JpaRepository`, el
metodo existe).

### 2. Baja protegida contra planes

Files: `src/main/java/com/valhalla/domain/planplace/PlanPlaceRepository.java` +
`src/main/java/com/valhalla/infrastructure/planplace/JpaPlanPlaceRepository.java`:

```java
// interface de dominio
boolean existsByPlaceId(Long placeId);

// en JpaPlaceRepository (Spring Data deriva el nombre)
boolean existsByPlaceId(Long placeId);
```

`PlaceServiceImpl.delete(id)` chequea primero y lanza `PlaceInUseException` (o
`IllegalStateException` con mensaje claro) si el lugar esta en uso; el controller lo catchea
y vuelve al listado con el error en flash attribute, sin borrar. Asi la FK
`plan_places.place_id` nunca se viola (AC-11).

### 3. Service del ABM

Files: `src/main/java/com/valhalla/domain/place/PlaceService.java` +
`src/main/java/com/valhalla/infrastructure/place/PlaceServiceImpl.java` — agregar a la
interface existente:

```java
Place create(EditPlaceRequest request);          // o guardar(Place)
Place update(Long id, EditPlaceRequest request);
void delete(Long id);                            // con la guarda del paso 2
```

El upsert mapea el request a `Place` (nombre, descripcion, categoria, direccion, imageUrl,
latitud, longitud, horarios, telefono, web). `getPlaceById` ya existe.

### 4. Mapping OSM → PlaceCategory

File: `src/main/java/com/valhalla/domain/place/PlaceCategory.java` (al lado del `fromCode`
existente):

```java
public static PlaceCategory fromOsm(String key, String value) {
  if (key == null || value == null) return OTHER;
  return switch (key + "=" + value) {
    case "amenity=restaurant", "amenity=fast_food" -> RESTAURANT;
    case "amenity=bar", "amenity=pub" -> BAR;
    case "amenity=cafe" -> CAFE;
    case "amenity=nightclub" -> NIGHTLIFE;
    case "amenity=museum" -> MUSEUM;
    case "amenity=theatre", "amenity=cinema", "amenity=arts_centre",
         "amenity=library", "amenity=planetarium" -> CULTURE;
    case "amenity=park", "amenity=garden", "amenity=playground" -> PARK;
    case "amenity=fitness_centre", "amenity=sport_centre" -> SPORT;
    default -> key.equals("shop") ? SHOPPING : OTHER;
  };
}
```

Tabla declarada una sola vez, cubierta por U-01..U-04. El admin puede cambiar cualquier
categoria antes de guardar (AC-06).

### 5. `OverpassClient` + DTO + implementacion HTTP

File nuevo: `src/main/java/com/valhalla/domain/place/OsmPoi.java`:

```java
public record OsmPoi(
  String name,
  String address,
  Double latitude,
  Double longitude,
  String openingHours,
  String phone,
  String website,
  String osmKey,     // "amenity" o "shop"
  String osmValue    // "restaurant", "bar", ...
) {}
```

File nuevo: `src/main/java/com/valhalla/domain/place/OverpassClient.java`:

```java
public interface OverpassClient {
  List<OsmPoi> findCabaPois(String osmKey, String osmValue);
}
```

File nuevo: `src/main/java/com/valhalla/infrastructure/place/HttpOverpassClient.java` — la
unica implementacion con red, marcada `@Component`:

- Construye la query con el template de Notas, con `osmKey` validado contra un allowlist
  `{amenity, shop}` (nunca interpolar el query param crudo: es un endpoint autenticado pero
  inyectar en la query de Overpass es un fluyo innecesario).
- `HttpClient` de `java.net.http` (JDK 25, sin dependencia nueva) con `timeout` y `User-Agent`
  identificable del proyecto.
- `POST application/x-www-form-urlencoded` con `data=<query>` al endpoint configurable
  (property `overpass.url`, default `https://overpass-api.de/api/interpreter`).
- Parseo con `ObjectMapper`: `elements[]` → por cada elemento, tags + `lat/lon` directo o
  `center.lat/center.lon` (por eso el `out center`); descarta los sin `name`; direccion
  armada de `addr:housenumber` + `addr:street`; telefono con fallback
  `contact:phone` → `phone`.
- En errores transitorios del endpoint (429, 502, 503, 504) reintenta hasta dos veces: espera
  30 s para el 429 y 1 s para el endpoint saturado. Mas alla, propaga una excepcion que el
  controller traduce a un mensaje visible en la vista de import (ver paso 6) y deja el detalle
  en el log con `java.util.logging`.

Unit testable sin red: el parseo se deja en un metodo aparte (package-private o estatico)
que recibe el body JSON y devuelve `List<OsmPoi>` — es lo que cubren U-10..U-12 con un
fixture.

### 6. Controller del ABM + importador

File nuevo: `src/main/java/com/valhalla/presentation/place/PlaceAdminController.java` —
espejo de `UserController` (`/admin/users`):

```java
@Controller
@RequestMapping("/admin/places")
public class PlaceAdminController {

  // GET ""                     -> listado           pages/admin/places
  // GET "/new"                 -> form vacio         pages/admin/place-form
  // POST ""                    -> alta               (redirect al listado)
  // GET "/{id}/edit"           -> form con datos
  // POST "/{id}" (o PUT)       -> edicion            (redirect al listado)
  // POST "/{id}/delete"        -> baja protegida     (paso 2)
  // GET "/import"              -> vista del mapa     pages/admin/place-import
  // GET "/import/pois"         -> JSON proxy de Overpass (paso 5)
}
```

File nuevo: `src/main/java/com/valhalla/presentation/place/EditPlaceRequest.java` — espejo de
`EditUserRequest`: bean con `@NotBlank name`, `@NotNull category`, y opcionales `description`,
`address`, `imageUrl`, `latitude`, `longitude`, `openingHours`, `phone`, `website`. El mismo
DTO se usa para alta, edicion y pre-llena.

El pre-llena del importador es server-side: `GET /admin/places/new?name=...&address=...&latitude=...&longitude=...&openingHours=...&phone=...&website=...&category=...`
arma el `EditPlaceRequest` inicial (paso 7 de E2E: I-17, I-18). El `category` del query param
es el resultado de `PlaceCategory.fromOsm(...)` ya mapeado a enum; si viene invalido, se
ignora y el form arranca en `OTHER`.

`GET /admin/places/import/pois?amenity=...&shop=...`: valida el allowlist, delega en
`OverpassClient.findCabaPois` y devuelve `List<OsmPoi>` como JSON. Si el cliente lanza
(excepcion de red / 429 agotado), responde 502 con mensaje; la vista lo muestra sin romperse.

### 7. Vistas del ABM

Files nuevos en `src/main/webapp/WEB-INF/templates/pages/admin/`:

- `places.html` — espejo de `users.html`: tabla con nombre, categoria, direccion, imagen
  (preview o placeholder), acciones Edit / Delete; link "+ New Place" y link "Importar del
  mapa" hacia `/admin/places/import`. Delete como form POST con `_csrf` y
  `<input type="hidden" name="_method" value="DELETE">`, igual que en `users.html`.
- `place-form.html` — espejo de `user-form.html`: campos del `EditPlaceRequest`, select de
  `PlaceCategory.values()` (el admin siempre puede cambiar la categoria pre-llena, AC-06),
  `imageUrl` opcional con hint de placeholder, y submit POST (PUT via `_method` en edicion).
- `place-import.html` — Leaflet igual que `places/list.html` (CDN unpkg ya usado en el repo,
  tile layer OSM con attribution `© OpenStreetMap contributors`): mapa centrado en CABA,
  select de queries predefinidas (restaurantes, bares, cafes, tiendas, museos) que llama a
  `/admin/places/import/pois`, markers por POI; al clickear un marker, link/navegacion a
  `/admin/places/new?...` con los datos del `OsmPoi`.

### 8. Dataset nuevo en `PlaceDataSeeder`

File: `src/main/java/com/valhalla/infrastructure/PlaceDataSeeder.java`

Se conservan los 10 lugares actuales y se agregan 2 (cubren `SPORT` y `OTHER`, las dos
categorias hoy vacias), total 12:

| Nombre | Categoria | Direccion | Lat | Lng |
|--------|-----------|-----------|-----|-----|
| Obelisco de Buenos Aires | OTHER | Av. 9 de Julio y Corrientes | -34.6054 | -58.3815 |
| Estadio Monumental | SPORT | Av. Figueroa Alcorta 7597 | -34.5453 | -58.4497 |

`savePlace` gana el seteo de la imagen opcional:

```java
private void savePlace(PlaceData data) {
  Place place = new Place();
  // ... campos existentes ...
  place.setImageUrl(data.imageUrl());   // null: la vista cae al placeholder
  placeRepository.save(place);
}
```

El record `PlaceData` agrega el campo `String imageUrl` (nullable). Verificado contra
`PlacesViewE2E`: el total pasa a 12; RESTAURANT sigue en 2 y la busqueda "Don" sigue en 1,
asi que solo cambian los asserts de total y el selector por hex (paso 11).

### 9. `ResetDatabase` incluye `places`

File: `src/test/java/com/valhalla/e2e/ResetDatabase.java` — al array de statements, antes de
`"DELETE FROM plans"`:

```java
"DELETE FROM places",
"ALTER SEQUENCE places_id_seq RESTART WITH 1",
```

Despues de `"DELETE FROM plan_places"` (que ya esta antes) y antes de `"DELETE FROM plans"`:
`plan_places` es lo que referencia a `places`, y una vez borrado el orden con `plans` es
libre. Con esto el seeder vuelve a correr en cada reset y el dataset nuevo llega a la base
de E2E.

### 10. `data-category` en los markers

File: `src/main/webapp/WEB-INF/templates/pages/places/list.html` — en `updateMarkers()`, el
`html` del `L.divIcon` gana el atributo en el div interior:

```js
html: '<div data-category="' + place.category + '" style="background-color:' + color + '; ...">' + icon + '</div>',
```

El E2E pasa de `div[style*='background-color:#e74c3c']` a
`#map .leaflet-marker-icon div[data-category='RESTAURANT']` (AC-09).

### 11. Recalibrar `PlacesViewE2E`

File: `src/test/java/com/valhalla/e2e/PlacesViewE2E.java` — el archivo fija el dataset en 5
puntos; recalibracion:

| Que | Donde | Accion |
|-----|-------|--------|
| Total | `waitForPlaceCount(10)` ×2, `markers().count() == 10`, `placeCards().count() == 10` | 12 (o una constante `TOTAL_PLACES = 12` que alimente al helper y a los dos asserts) |
| Marker por categoria | `div[style*='background-color:#e74c3c']` == 2 | `div[data-category='RESTAURANT']` == 2 (paso 10) |
| Filtro RESTAURANT | `waitForPlaceCount(2)` | sigue siendo 2 (los 2 restaurantes originales) |
| Busqueda "Don" | `waitForPlaceCount(1)` | sigue siendo 1 ("Parrilla Don Julio") |
| Placeholder | assert de `img src == /images/place-placeholder.svg` en el detalle | se conserva pero deja de ser el unico caso: I-13 cubre el lugar con imagen real |

### 12. Tests nuevos (resumen de archivos)

Los archivos de test nuevos siguen los nombres y paquetes existentes:
`infrastructure/PlaceDataSeederTest.java` (espejo de
`infrastructure/PublicPlanDataSeederTest.java`),
`integration/PlaceAdminControllerTest.java`,
`integration/PlaceImportControllerTest.java`,
`integration/PlaceAdminSecurityTest.java`, `integration/PlaceImageTest.java`,
`infrastructure/place/HttpOverpassClientTest.java`,
`e2e/PlaceViewABME2E.java` (espejo de `e2e/UserViewABME2E.java`, con page objects en
`e2e/views/`) y `e2e/PlaceImportE2E.java` (E-07..E-08: el proxy de Overpass se stubbea con
`page.route`, asi que la vista real corre en Chromium sin red). Los mock de `OverpassClient` en
integracion son `@MockBean` (AC-10).

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `domain/place/Place.java` | Actualizar (columnas opcionales `openingHours`, `phone`, `website`) |
| `domain/place/PlaceCategory.java` | Actualizar (`fromOsm(key, value)`) |
| `domain/place/PlaceRepository.java` | Actualizar (`deleteById`) |
| `domain/place/PlaceService.java` | Actualizar (create/update/delete) |
| `domain/place/OsmPoi.java` | Crear (DTO del POI importado) |
| `domain/place/OverpassClient.java` | Crear (interfaz, mockeable en tests) |
| `domain/planplace/PlanPlaceRepository.java` | Actualizar (`existsByPlaceId`) |
| `infrastructure/place/JpaPlanPlaceRepository.java` | Actualizar (`existsByPlaceId` derivado) |
| `infrastructure/place/PlaceRepositoryImpl.java` | Actualizar (`deleteById`) |
| `infrastructure/place/PlaceServiceImpl.java` | Actualizar (alta/edicion/baja protegida) |
| `infrastructure/place/HttpOverpassClient.java` | Crear (implementacion HTTP de `OverpassClient`) |
| `infrastructure/PlaceDataSeeder.java` | Actualizar (12 lugares, `imageUrl` opcional) |
| `presentation/place/PlaceAdminController.java` | Crear (ABM + importador) |
| `presentation/place/EditPlaceRequest.java` | Crear (DTO del form, espejo de `EditUserRequest`) |
| `templates/pages/admin/places.html` | Crear (listado, espejo de `users.html`) |
| `templates/pages/admin/place-form.html` | Crear (alta/edicion, espejo de `user-form.html`) |
| `templates/pages/admin/place-import.html` | Crear (Leaflet + capa Overpass + attribution OSM) |
| `templates/pages/places/list.html` | Actualizar (`data-category` en el marker) |
| `domain/place/PlaceCategoryTest.java` | Actualizar (U-01..U-04) |
| `infrastructure/PlaceDataSeederTest.java` | Crear (U-05..U-09) |
| `infrastructure/place/HttpOverpassClientTest.java` | Crear (U-10..U-12) |
| `integration/PlaceRestControllerTest.java` | Actualizar (I-01..I-05) |
| `integration/PlaceAdminControllerTest.java` | Crear (I-06..I-12) |
| `integration/PlaceImageTest.java` | Crear (I-13, I-14) |
| `integration/PlaceImportControllerTest.java` | Crear (I-15..I-19) |
| `integration/PlaceAdminSecurityTest.java` | Crear (S-01..S-04) |
| `e2e/PlacesViewE2E.java` | Actualizar (E-01..E-03, recalibrado sin hex) |
| `e2e/PlaceViewABME2E.java` | Crear (E-04..E-06) |
| `e2e/PlaceImportE2E.java` | Crear (E-07..E-08, proxy stubbeado con `page.route`) |
| `e2e/views/PlaceImportPage.java` | Crear (page object de la vista de import) |
| `e2e/ResetDatabase.java` | Actualizar (`DELETE FROM places` + `ALTER SEQUENCE` antes de `plans`) |
