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
| AC-04 | La vista "Importar del mapa" muestra POIs reales de CABA (amenities/comercios) en un mapa Leaflet; al entrar y al mover el mapa los pinta **desde la base**, sin consultar Overpass. El admin elige el tipo de lugar y presiona "Cargar puntos de esta zona": recién ahí el backend consulta Overpass (a través de `OverpassClient`, sin API key, acotado al área visible del mapa y recortado a CABA) y guarda lo traído. Si esa zona+tag ya se trajo dentro de la ventana de frescura, el botón responde con lo guardado sin consultar Overpass (AC-15) |
| AC-05 | Al elegir un POI en el importador (la vista previa y despues "Crear lugar"), el form del ABM queda pre-lleno con nombre, direccion, coordenadas, horarios, telefono, web y categoria OSM mapeada; el admin puede revisar y cambiar cualquier campo antes de guardar, y guardar crea el place con esos datos |
| AC-05b | El panel de detalle de `/explore` muestra horarios, telefono y web de un place cuando existen (el telefono como `tel:`, la web como enlace `https`), y no muestra filas vacias para los que no los tienen |
| AC-06 | Tags OSM sin mapping explicito caen en `OTHER` y el admin siempre puede cambiar la categoria antes de guardar |
| AC-07 | El dataset mantiene los 10 places actuales del seeder y agrega lugares nuevos; cada categoria de `PlaceCategory` tiene al menos un lugar (hoy `SPORT` y `OTHER` no tienen ninguno) |
| AC-08 | El filtro por categoria y la busqueda por nombre de `/explore` siguen funcionando sobre el dataset nuevo |
| AC-09 | `PlacesViewE2E` queda recalibrado contra el dataset nuevo y deja de atar el test al hex de color: el marker se identifica por su categoria (`data-category`), no por `background-color:#e74c3c` |
| AC-10 | Ningun test de integracion ni E2E depende de red: `OverpassClient` se mockea en los tests; ninguna suite golpea Overpass ni Nominatim |
| AC-11 | No se puede dar de baja un lugar que forma parte de planes: el ABM informa el motivo, no borra, y el place sigue en la base (la FK `plan_places.place_id` no se rompe) |
| AC-12 | Lo traído queda en la base: una fila por POI en `poi` (identidad natural `osm:<type>/<id>` en `source_key`, unique) y una de cobertura por zona y tag en `poi_zone(zone_key, tag_key, tag_value, fetched_at)`. Cuando la zona se vuelve a pedir (vencida la ventana de frescura), aplica **sólo el diff**: inserta lo nuevo, actualiza únicamente lo que cambió, marca `REMOVED` lo que Overpass dejó de devolver (baja lógica) y reactiva lo que reaparece; recargar con datos idénticos no escribe ninguna fila. Un fallo de Overpass no toca lo guardado, y la vista muestra la antigüedad de la última carga o que la zona nunca se cargó |
| AC-13 | Elegir el mismo POI del importador dos veces **no duplica el lugar**: el link del ABM lleva `externalId=osm:<type>/<id>`, el form lo postea como hidden y el alta reusa la fila de ese `places.external_id` (nullable, unique), quedándose con los datos de la última importación. Un lugar creado a mano queda sin identidad y editar un lugar importado no la pierde |
| AC-14 | El importador pinta cada punto con **el color de su categoría**, el mismo token que usa `/explore` (sin hex nuevos), y el click en un punto —marker o fila de la lista— abre una **vista previa** sobre el mapa con nombre, categoría, dirección, horarios, teléfono, web y coordenadas, sin navegar; desde ahí se abre el alta pre-llenada. Un punto que ya es un lugar del catálogo (`places.external_id` igual a su `source_key`) se marca como **guardado** en la vista previa y en la lista, ofrece "Ver el lugar guardado" (link al ABM de ese lugar) en vez de crear, y el alta abierta con ese `externalId` muestra el mismo aviso |
| AC-15 | Repetir "Cargar puntos de esta zona" sobre una zona+tag traída dentro de `place.import.freshness-minutes` (default 15) **no consulta Overpass**: responde con lo guardado en milisegundos y el sobre lo declara con `refreshed: false`. Una zona vencida o nunca traída sí consulta y responde `refreshed: true`. `/pois` nunca consulta y siempre responde `refreshed: false`. Con `place.import.freshness-minutes=0` la ventana queda deshabilitada |

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
| U-13 | `buildQuery` arma la consulta con la caja que recibe (no con una constante) y valida key/value contra el allowlist | AC-04 |
| U-14 | Reintenta una vez ante 429 (esperando el delay inyectado), una vez ante 502 y hasta dos veces en total; un 500 no se reintenta | AC-04 |
| U-15 | Un elemento sin `type`/`id` deja `osmType`/`osmId` en `null` y no rompe el parseo: la identidad la resuelve después `MapPoi.sourceKeyOf` | AC-04 |

### Tests Unitarios (`domain/place/BoundingBoxTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-16 | La caja de CABA se serializa en el orden de Overpass (`sur,oeste,norte,este`) con `Locale.ROOT` | AC-04 |
| U-17 | Rechaza latitud/longitud fuera de rango, coordenadas no numericas (`NaN`) y cajas invertidas | AC-04 |
| U-18 | `intersection` recorta una caja que se sale de CABA y devuelve vacio cuando no se tocan | AC-04 |

### Tests Unitarios (`domain/place/MapPoiTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-19 | La clave natural es `osm:way/1`; sin `id` (o con `type` pero sin `id`) cae a `fallback:<nombre>@<lat>,<lon>` | AC-12 |
| U-20 | `from` nace `ACTIVE`, con el tag de la consulta y la categoría derivada de `PlaceCategory.fromOsm` | AC-12 |
| U-21 | `apply` devuelve `false` con los mismos datos y `true` cuando cambió un campo visible o sólo la categoría, copiando el valor nuevo | AC-12 |
| U-22 | `toOsmPoi` devuelve el mismo `OsmPoi` que el importador le manda al navegador, con el tag con el que se guardó | AC-04 |

### Tests Unitarios (`infrastructure/place/PoiIngestServiceTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-23 | Un POI que no estaba se inserta una vez (`created=1`); el mismo elemento repetido en la respuesta se cuenta una sola vez | AC-12 |
| U-24 | Recargar con los mismos datos no escribe nada (`created=0, updated=0, removed=0`) y `saveAll` recibe una lista vacía | AC-12 |
| U-25 | Un campo visible que cambió se actualiza; el que ya no viene pasa a `REMOVED` y no se cuenta dos veces si ya estaba dado de baja; el que reaparece se reactiva (`reactivated=1`) | AC-12 |
| U-26 | `read` devuelve sólo los `ACTIVE` de la caja con el `fetchedAt` de la zona, y `null` cuando esa zona nunca se cargó; no escribe nada | AC-04, AC-12 |
| U-27 | `zoneOf` agranda la caja pedida a la grilla de 0.01° (la zona contiene la vista) y la deja intacta si ya está en la grilla; una caja entera fuera de CABA se devuelve tal cual | AC-12 |

### Tests Unitarios (`domain/place/PlaceServiceImplTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| U-28 | Con un `externalId` ya guardado, `create` reusa esa fila (misma instancia), aplica los datos nuevos y guarda una sola vez | AC-13 |
| U-29 | Con un `externalId` desconocido, `create` arma un lugar nuevo con la identidad y lo guarda | AC-13 |
| U-30 | Un alta sin `externalId` no consulta la identidad guardada y deja el lugar en `null` | AC-13 |
| U-31 | Editar sin `externalId` no borra la identidad del lugar importado | AC-13 |
| U-32 | `savedPlaceIds` con una lista vacia devuelve un mapa vacio y no consulta el repositorio | AC-14 |
| U-33 | `savedPlaceIds` descarta identidades `null` o en blanco: sin claves utiles no toca la base | AC-14 |
| U-34 | `savedPlaceIds` mapea cada identidad guardada a su `id` y omite las que no son ningun lugar | AC-14 |

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
| I-26 | `POST /admin/places` dos veces con el mismo `externalId` deja **una** fila con esa identidad, con el nombre de la segunda importación | AC-13 |
| I-27 | `GET /admin/places/new?externalId=osm:node/9001&…` pre-llena el hidden `externalId` del form | AC-13 |
| I-30 | El alta abierta con un `externalId` que ya es un lugar publica `savedPlaceId` con el id de ese lugar y mantiene el pre-llenado | AC-14 |
| I-31 | El alta abierta con un `externalId` desconocido deja `savedPlaceId` en `null` | AC-14 |

### Tests de Integracion (`infrastructure/place/PlaceRepositoryTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-28 | `findByExternalId` devuelve el lugar guardado con esa identidad | AC-13 |
| I-29 | `findByExternalId` queda vacío para una identidad que nunca se importó (los 12 lugares del seeder no tienen identidad) | AC-13 |
| I-32 | `findByExternalIdIn` devuelve exactamente los lugares de las identidades pedidas y omite las desconocidas | AC-14 |
| I-33 | `findByExternalIdIn` con un conjunto vacío no explota y devuelve una lista vacía | AC-14 |

### Tests de Integracion (`integration/PlaceImageTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-13 | Un place con `imageUrl` cargada via ABM renderiza esa URL como `img src` en `/explore` | AC-02 |
| I-14 | Un place sin `imageUrl` renderiza `/images/place-placeholder.svg` en `/explore` | AC-02 |

### Tests de Integracion (`integration/PlaceImportControllerTest.java`)

| # | Test | AC que cubre |
|---|------|-------------|
| I-15 | `GET /admin/places/import` responde 200 con la vista del mapa | AC-04 |
| I-16 | `GET /admin/places/import/pois?amenity=restaurant&south=..&west=..&north=..&east=..` con una fila sembrada en `poi` devuelve el sobre `{pois, fetchedAt}` con lo guardado en esa caja, y no toca `OverpassClient` — sin red | AC-04, AC-10, AC-12 |
| I-17 | `GET /admin/places/new?name=...&latitude=...&category=...` pre-llena el form (server-side, via query params) | AC-05 |
| I-18 | `POST /admin/places` con los datos pre-llenos crea el place con coordenadas, horarios, telefono, web y categoria mapeada | AC-05 |
| I-19 | `GET /admin/places/import/pois?color=red`, con dos tags o con un parametro de mas responde 400 sin tocar al cliente | AC-04, AC-10 |
| I-20 | Una vista mas grande que CABA se recorta a la caja de la ciudad antes de consultar (el cliente recibe la zona sellada de `BoundingBox.CABA`) | AC-04 |
| I-21 | Una caja sin bordes, invertida, no numerica o fuera de CABA responde 400 sin tocar al cliente, tanto en `/pois` como en `/refresh` | AC-04, AC-10 |
| I-22 | `GET /admin/places/import/refresh?…` con `OverpassClient` mockeado consulta la zona sellada, ingesta y devuelve el mismo sobre; la lectura siguiente ya sale de la base y el cliente se llamó una sola vez | AC-04, AC-12 |
| I-23 | Cargar dos veces la misma zona con datos idénticos deja una sola fila con el mismo `id`: el diff actualiza, no borra y recrea | AC-12 |
| I-24 | `fetchedAt` es `null` cuando la zona nunca se cargó, aunque haya POIs guardados de otra zona | AC-12 |
| I-25 | Un 502 de Overpass responde 502 con el mensaje de error y deja las filas guardadas intactas (`ACTIVE`) | AC-12 |
| I-34 | El sobre de `/pois` trae la `category` y el `externalId` de cada punto; un POI sin identidad de OSM responde `externalId: null` | AC-14 |
| I-35 | Con un lugar guardado que tiene `external_id` igual al `source_key` de un POI, ese punto responde con su `savedPlaceId` y los demás con `null`, con una sola consulta al catálogo | AC-14 |

### Tests de Integración (`integration/PlaceImportFreshnessTest.java`)

La clase corre con `place.import.freshness-minutes=15`; `PlaceImportControllerTest` declara la misma
propiedad en `0` para seguir probando el diff en cada carga.

| # | Test | AC que cubre |
|---|------|-------------|
| I-36 | Dos `/refresh` seguidos sobre la misma zona+tag: el primero responde `refreshed: true`, el segundo `refreshed: false`, el `fetchedAt` no se mueve y `OverpassClient` se llamó una sola vez | AC-15 |
| I-37 | Un `/refresh` sobre una zona cuyo `fetched_at` se envejeció fuera de la ventana vuelve a consultar Overpass y responde `refreshed: true` | AC-12, AC-15 |
| I-38 | Un `/refresh` sobre una zona nunca traída consulta Overpass y responde `refreshed: true` | AC-15 |
| I-39 | `/pois` responde `refreshed: false` siempre, aunque la zona se haya traído hace un instante | AC-04, AC-15 |

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
| E-04 | `e2e/PlaceViewABME2E.java` | Admin crea un place desde `/admin/places/new` (con horarios, telefono y web) → aparece en el listado del ABM y en `/explore`, y su panel de detalle muestra los tres datos (telefono como `tel:`, web como `https`) | AC-01, AC-05b |
| E-05 | `e2e/PlaceViewABME2E.java` | Admin edita el place creado → los cambios se ven en el listado y en `/explore`, y los datos opcionales siguen en el panel de detalle | AC-01, AC-05b |
| E-06 | `e2e/PlaceViewABME2E.java` | Admin da de baja el place → desaparece del listado y de `/explore` | AC-01 |
| E-07 | `e2e/PlaceImportE2E.java` | Admin entra a `/admin/places/import`: la lista se pinta desde `GET /pois` (stub) sin un solo pedido de ingesta; con el click en "Cargar puntos de esta zona" llega exactamente 1 pedido a `/refresh`, aparecen los 3 POIs, el buscador se habilita y elegir uno abre la vista previa sobre el mapa (sin navegar) con nombre, categoría, dirección, horarios, teléfono, web y coordenadas; "Crear lugar" lleva a `/admin/places/new` con el form pre-llenado (nombre, categoria, direccion, lat/lng, horarios, telefono, web) y el hidden `externalId` con `osm:way/1` | AC-04, AC-05, AC-10, AC-12, AC-13, AC-14 |
| E-08 | `e2e/PlaceImportE2E.java` | Con la ingesta stubbeada en 502, tras el click la vista muestra el mensaje de error y la lista queda vacia | AC-04, AC-10 |
| E-09 | `e2e/PlaceImportE2E.java` | El buscador por nombre filtra la lista y los markers en el acto: "PARRILLA" → 1, "cafe" encuentra "Café" (sin acentos ni mayusculas), "zzz" → 0 y limpiar el filtro devuelve los 3 | AC-04, AC-10 |
| E-10 | `e2e/PlaceImportE2E.java` | La lectura viaja con el area visible (`south`/`west`/`north`/`east` en la URL) y a zoom 17 pide una zona mas chica que la vista inicial; mover el mapa (zoom y arrastres) vuelve a leer de la base sin un solo pedido de ingesta y sin vaciar la lista | AC-04, AC-10, AC-12 |
| E-11 | `e2e/PlaceImportE2E.java` | Con un POI sembrado por JDBC en `poi`, entrar y recargar la pagina pinta ese punto (lista y marker) desde la base, sin un solo pedido de ingesta | AC-04, AC-12 |
| E-12 | `e2e/PlaceImportE2E.java` | El click en un marker abre la vista previa sin salir de `/admin/places/import`, y el punto de categoría de la vista previa se pinta con el token de marca de esa categoría (rojo de `RESTAURANT`) | AC-14 |
| E-13 | `e2e/PlaceImportE2E.java` | Con un POI que ya es un lugar (`savedPlaceId` en el stub): la fila muestra "Guardado", la vista previa avisa que ya está en el catálogo, el link "Ver el lugar guardado" apunta a `/admin/places/<id>/edit` y no hay acción de crear | AC-14 |

El importador tiene E2E sin tocar la red: el navegador ejecuta la vista real y `page.route` intercepta
`GET /admin/places/import/pois` y `GET /admin/places/import/refresh` antes de que salgan,
respondiendo el sobre `{pois, fetchedAt}` de un fixture (o un 502). El caso "lo guardado se pinta al
recargar" no se puede stubbear sin perder el punto, así que siembra la fila por JDBC
(`ResetDatabase.seedPoi`) y deja que la vista lea de verdad. Es el mismo criterio que usa la
integración con `OverpassClient` mockeado: el doble vive en el borde (red del browser / interfaz de
dominio / fila sembrada), nunca dentro de la lógica de la vista.

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
  - Query: `[out:json][timeout:60];nwr["amenity"="restaurant"]["name"](<sur>,<oeste>,<norte>,<este>);
    out center tags 300;` — el bounding box es el **area visible del mapa** que manda la vista,
    recortada a CABA (sur,oeste,norte,este; CABA sale del bbox de la relation 3082668 en
    Nominatim). Se consulta por **bounding box** y no por `area`: el endpoint publico responde 502
    a las consultas por area cuando esta saturado, y el area id que decia esta spec
    (`3600345769`) era la relation de **Huesca, Espana** — devolvia cero POIs. Con `["name"]` y
    `out ... 300` se acota la respuesta (99 KB en vez de 870 KB).
  - Latencia real medida del endpoint publico: **25–47 s** por consulta sin cachear. Por eso el
    presupuesto del servidor es 60 s y el timeout del `HttpClient` 90 s: con 30 s el cliente
    cortaba antes de que Overpass contestara.
  - Devuelve tags `name`, `addr:*`, `opening_hours`, `phone`/`contact:phone`, `website`,
    `cuisine`, y el tipo (`amenity=*` / `shop=*`). NO devuelve fotos ni ratings: importar
    imagenes o puntajes queda fuera de alcance.
  - `website` viene muchas veces **sin esquema** (`bar.example.com`). El parser le antepone
    `https://`: el form del ABM usa `type="url"` y ese valor bloquearia el guardado del alta
    pre-llenada. La normalizacion va en el borde (parser) y no en cada vista.
  - Uso automatizado moderado: ~100 queries/dia y 10 MB/dia en el server principal. Errores
    transitorios: **429** (limite por IP: esperar 30 s) y **502/503/504** (cola llena: esperar
    1 s). Se reintenta hasta dos veces en total; si sigue fallando, `refresh` responde 502 y la
    vista invita a probar de nuevo.
- **Ventana de frescura: lo único barato es no llamar.** Cada `refresh` es **una sola** consulta
  a Overpass, así que el tiempo lo domina la latencia del servidor escaneando el bbox (25–47 s),
  no el volumen de la respuesta. `poi_zone.fetched_at` ya sellaba la cobertura por zona+tag, y
  ahora también decide: si la zona se trajo dentro de `place.import.freshness-minutes` (default
  15), `refresh` responde con lo guardado y no toca la red. El sobre declara `refreshed` para que
  la vista pueda decir "se usaron los puntos guardados" en vez de parecer que el botón no hizo
  nada. **No se baja el tope de `out ... 300`**: Overpass evalúa la consulta completa y recorta
  al final, así que traer menos elementos no acelera la respuesta, y como el diff marca `REMOVED`
  todo lo guardado que no vuelve en la respuesta, un tope más chico daría de baja puntos válidos.
  Un `0` en la propiedad deshabilita la ventana (los tests del diff la usan así).
- **Nominatim — hechos.** `https://nominatim.openstreetmap.org/search` (o Photon) hace
  geocoding por nombre; sin key, requiere un `User-Agent` identificable, 1 req/s. No es
  necesario en el flujo principal del importador (Overpass ya devuelve coordenadas del POI
  elegido); queda documentado por si se agrega busqueda por nombre despues.
- **Atribucion ODbL obligatoria.** Todo mapa que muestre datos OSM (el importador) debe
  incluir `© OpenStreetMap contributors` en la attribution del tile layer, igual que ya hace
  `/explore` con
  `attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'`.
- **El panel de detalle de `/explore` muestra los datos nuevos.** `GET /api/places` serializa el
  domain `Place` completo, así que horarios, teléfono y web llegan sin cambios de contrato: el
  sidebar los pinta solo si existen (una fila por dato, con ícono de Lucide). El teléfono va como
  `tel:` sin espacios ni guiones (el texto mostrado los conserva) y la web se abre como `https`
  aunque el dato guardado no traiga esquema: un `javascript:` cargado a mano nunca se navega.
- **Mapping `amenity`/`shop` OSM → `PlaceCategory`.** Mapping explicito en
  `PlaceCategory.fromOsm(key, value)` (ver Referencia, paso 4). Lo no mapeado cae en `OTHER`,
  y el admin siempre puede cambiarlo antes de guardar (el select del form es el mismo para el
  alta manual y la pre-llena): la categoria sugerida es una sugerencia, no una decision.
- **El importador pinta desde la base; Overpass sólo cuando el admin lo pide.** La vista monta el
  mapa (centrado en CABA, zoom 13) y pide `GET /admin/places/import/pois` con el tag y los cuatro
  bordes: eso es una lectura de `poi` por bounding box, milisegundos. Lo mismo en cada
  `moveend`/`zoomend`. El botón ("Cargar puntos de esta zona" la primera vez, "Actualizar puntos de
  esta zona" cuando esa zona y ese tag ya se cargaron) es el único camino a
  `GET /admin/places/import/refresh`, que consulta Overpass (~30 s), ingesta el diff y devuelve el
  mismo sobre `{pois, fetchedAt}`. El motivo es el costo y la estabilidad: una consulta de 30 s por
  cada píxel movido, contra un endpoint público que se satura.
- **La consulta es la zona visible, recortada a CABA.** El backend valida la caja (bordes
  presentes, numéricos, no invertidos) y la intersecta con `BoundingBox.CABA`: una vista que se va
  de la ciudad responde 400 y la vista deshabilita el botón con el motivo. Esa intersección es lo
  que evita que un zoom out continental mande una consulta gigante al endpoint público, sin
  necesidad de un tope de superficie aparte: el techo es la ciudad.
- **Mover el mapa vuelve a leer, no vacía la lista.** La lista y los markers son lo que hay en la
  base para la caja visible; al panear o acercarse se pide esa caja de nuevo (local) y la lista se
  reemplaza por lo que corresponda. Si la zona nueva nunca se cargó, la vista lo dice ("esta zona
  todavía no se cargó") en vez de mostrar "no hay puntos", que es otra cosa.
- **Buscador por nombre del importador: local, no otra consulta.** El input "Filtrar por nombre"
  tamiza los POIs ya leídos (`computed` sobre `pois`), sin volver a golpear nada: el usuario escribe
  tecla por tecla y cada consulta cuesta ~30 s. Compara sin acentos ni mayúsculas ("cafe" encuentra
  "Café"). El filtro sobrevive a mover el mapa (los datos son los mismos) y se limpia al cambiar el
  tipo de lugar, porque ahí la lista es otra.
- **Los controles no se bloquean mientras carga.** El select y el buscador siguen usables; sólo el
  botón se deshabilita y el texto de estado dice qué está pasando ("Consultando OpenStreetMap: puede
  tardar hasta 30 s…" durante la ingesta, "Leyendo los puntos guardados…" en la primera lectura).
  Apretar de nuevo aborta la consulta anterior (`AbortController`) y sólo se aplica la última; una
  lectura nunca aborta una ingesta en curso, porque abortarla dejaría la zona a medio guardar.
- **El mapa se centra bien desde el arranque.** El contenedor nace midiendo 0 (Tailwind diferido),
  así que `invalidateSize` va con `pan` por defecto para conservar el centro geográfico; con
  `pan: false` el paso al tamaño real corría la vista ~5 km.
- **Una fila por POI, con la identidad de OSM.** Tabla `poi` con `source_key` unique
  (`osm:<type>/<id>`). Si el elemento no trae `id`, la clave cae a
  `fallback:<nombre>@<lat>,<lon>` — peor, pero evita duplicar en cada carga lo que no se puede
  volver a encontrar. Un POI sin coordenadas se descarta al ingestar: no se puede dibujar ni volver
  a leer por caja.
- **Cobertura aparte: `poi_zone`.** `(zone_key, tag_key, tag_value)` unique más `fetched_at`. Sin
  ella, "zona nunca cargada" y "zona cargada sin resultados" se ven iguales, y no hay forma de
  mostrar la antigüedad sin un timestamp por fila.
- **La zona es una grilla de 0.01° redondeada hacia afuera y recortada a CABA.** La caja pedida se
  agranda a la grilla (≈1.1 km) antes de consultar y de sellar la cobertura, así la zona siempre
  *contiene* la vista y dos vistas vecinas comparten zona. La tolerancia de `1e-9` existe porque
  `-34.62 * 100` da `-3462.0000000000005` en binario: sin ella, una vista ya alineada crecía una
  celda de más.
- **Diff semántico, no de filas.** El servicio compara campo por campo (`MapPoi.apply`) antes de
  guardar: recargar la misma zona con los mismos datos no emite un solo `UPDATE`. Lo que Overpass
  dejó de devolver pasa a `REMOVED` (baja lógica) y deja de renderizarse; si vuelve, se reactiva. Un
  502 no puede vaciar el mapa.
- **La llamada a Overpass queda fuera de la transacción.** El controller consulta `OverpassClient` y
  recién después llama a `PoiIngestService.ingest`, que es el único `@Transactional`: sostener una
  conexión del pool durante 30 s por click es la forma más rápida de dejar el sitio sin conexiones.
  Si Overpass falla, la respuesta es 502 y no se tocó ninguna fila.
- **Un POI elegido dos veces no crea dos lugares.** `places.external_id` guarda la identidad de
  origen (`osm:<type>/<id>`), nullable y unique: nullable porque un lugar hecho a mano no tiene
  identidad, y en SQL dos `NULL` no colisionan. El link del importador manda
  `externalId=osm:<type>/<id>`, `PlaceAdminController.formFromQuery` lo pone en el
  `EditPlaceRequest`, el form lo postea como hidden y `PlaceServiceImpl.create` busca por esa
  identidad: si la encuentra, actualiza esa fila con lo que vino del POI; si no, inserta. La clave
  de respaldo de `poi.source_key` (`fallback:<nombre>@<lat>,<lon>`) **no** se usa acá: se rompe con
  un rename y dejaría de deduplicar. `externalId` se escribe sólo cuando llega no vacío, así el
  hidden vacío (o un POST manipulado) no borra la identidad de un lugar ya importado. El campo
  aparece además en `GET /api/places`, porque la API serializa el domain `Place` entero.
- **El color del punto sale del mismo token que `/explore`, no de un hex nuevo.** El lector de los
  custom properties (`--category-restaurant`, …) vivía dentro de `pages/places-list.js`; se extrajo
  a `core/js/lib/category-colors.js` para que el importador lo reimporte en vez de duplicar la
  tabla. `BrandTokensTest` sigue exigiendo un solo origen: un token por categoría, ninguna
  categoría sin token y ningún hex de categoría en el JS.
- **El sobre del importador dejó de ser `OsmPoi`.** `OsmPoi` es lo que devuelve Overpass y no tiene
  categoría ni identidad (las resuelve `MapPoi` al guardar). Para pintar por categoría y marcar los
  puntos ya guardados, `ZonePois` pasa a llevar `MapPoi` y el controller arma
  `PoiView(name, address, latitude, longitude, openingHours, phone, website, category, externalId,
  savedPlaceId)`. `externalId` es `sourceKey` cuando empieza con `osm:` (una clave de respaldo no es
  identidad) y `savedPlaceId` es el `id` del lugar que ya tiene esa identidad, o `null`.
- **"Ya guardado" cuesta una consulta, no N.** `PlaceService.savedPlaceIds(...)` resuelve todo el
  lote con un `findByExternalIdIn` y descarta las identidades vacías antes de consultar; el sobre
  de hasta 300 puntos no dispara 300 selects. La misma consulta alimenta el aviso del alta
  (`/admin/places/new?externalId=…` publica `savedPlaceId` como atributo del modelo).
- **La vista previa no navega.** El click del marker y el de la fila llaman a `selectPoi(poi)`, que
  abre el panel sobre el mapa (`#poi-preview`, `role="region"`) con todos los datos; el alta se abre
  desde ahí con "Crear lugar". Un punto ya guardado no ofrece crear: ofrece "Ver el lugar guardado"
  al ABM de ese lugar, porque reimportar pisaría con datos de OSM lo que el admin editó a mano. El
  panel vive dentro del contenedor relativo del mapa (no es un `<aside>` hijo de la app) para no
  tocar los selectores del listado que usan los E2E.
- **Sin migraciones: `hbm2ddl.auto=update` alcanza.** `poi` y `poi_zone` son tablas nuevas, así que
  Hibernate las crea enteras con su unique y el índice `idx_poi_position`, y `places.external_id`
  es una columna nueva en una tabla existente; una base existente no necesita ningún paso manual.
  Si alguna vez una tabla quedó sin sus constraints, el `ALTER` es:

  ```sql
  ALTER TABLE places ADD CONSTRAINT places_external_id_key UNIQUE (external_id);
  ALTER TABLE poi ADD CONSTRAINT poi_source_key_key UNIQUE (source_key);
  CREATE INDEX idx_poi_position ON poi (latitude, longitude);
  ALTER TABLE poi_zone ADD CONSTRAINT poi_zone_zone_key_tag_key_tag_value_key
    UNIQUE (zone_key, tag_key, tag_value);
  ```
- **El importador depende del backend, nunca del navegador contra Overpass.** El navegador sólo
  conoce dos endpoints propios: `GET /admin/places/import/pois` (lectura de `poi`, sin red) y
  `GET /admin/places/import/refresh` (ingesta, el único que llama a `OverpassClient`). La interfaz
  en el dominio permite mockearla con `@MockBean` en los tests de integración (AC-10) y sustituir
  el proveedor en el futuro (ej. Photon) sin tocar la vista.
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

Y la identidad del POI de origen, para que dos importaciones del mismo punto no dupliquen el lugar
(AC-13). Nullable porque un lugar creado a mano no la tiene, y unique porque es la clave natural
del importador:

```java
@Column(name = "external_id", unique = true, length = 255)
private String externalId;
```

File: `src/main/java/com/valhalla/domain/place/PlaceRepository.java` — el ABM necesita borrar y
reconocer un POI ya importado:

```java
void deleteById(Long id);
Optional<Place> findByExternalId(String externalId);
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

El alta mapea el request a `Place` (nombre, descripcion, categoria, direccion, imageUrl,
latitud, longitud, horarios, telefono, web). `getPlaceById` ya existe.

`create` es un upsert por identidad, no un insert ciego (AC-13):

```java
Place place = existingFor(request.getExternalId()).orElseGet(Place::new);
```

`existingFor` devuelve `Optional.empty()` cuando el `externalId` viene vacío (un alta manual no
tiene nada que buscar) y `placeRepository.findByExternalId(...)` cuando viene con valor. En
`applyRequest` — compartido con la edicion — la identidad se escribe sólo si llega no vacía, para
que un hidden vacío no borre la de un lugar ya importado.

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
  String osmValue,   // "restaurant", "bar", ...
  String osmType,    // "node", "way", "relation" — identidad del elemento
  Long osmId         // id del elemento en OSM
) {}
```

File nuevo: `src/main/java/com/valhalla/domain/place/OverpassClient.java`:

```java
@FunctionalInterface
public interface OverpassClient {
  List<OsmPoi> findPois(String osmKey, String osmValue, BoundingBox box);
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
  `contact:phone` → `phone`; `type`/`id` del elemento van a `osmType`/`osmId` (si faltan quedan
  `null`: la identidad la resuelve `MapPoi.sourceKeyOf`).
- Sin cache en memoria: el cliente es un adaptador HTTP puro. Repetir una zona ya no se resuelve
  con un `ConcurrentHashMap` con TTL (se perdia en cada deploy y no se compartia entre
  instancias), sino leyendo `poi` y aplicando el diff de `PoiIngestService`.
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
  // GET "/import/pois"         -> JSON: lo guardado de la caja (sin red)
  // GET "/import/refresh"      -> JSON: Overpass + diff, y devuelve lo guardado
}
```

File nuevo: `src/main/java/com/valhalla/presentation/place/EditPlaceRequest.java` — espejo de
`EditUserRequest`: bean con `@NotBlank name`, `@NotNull category`, y opcionales `description`,
`address`, `imageUrl`, `latitude`, `longitude`, `openingHours`, `phone`, `website` y `externalId`
(este último hidden: no se edita a mano, sólo viaja del importador al alta). El mismo
DTO se usa para alta, edicion y pre-llena.

El pre-llena del importador es server-side: `GET /admin/places/new?name=...&address=...&latitude=...&longitude=...&openingHours=...&phone=...&website=...&category=...&externalId=osm:<tipo>/<id>`
arma el `EditPlaceRequest` inicial (paso 7 de E2E: I-17, I-18, I-27). El `category` del query param
es el resultado de `PlaceCategory.fromOsm(...)` ya mapeado a enum; si viene invalido, se
ignora y el form arranca en `OTHER`. El `externalId` viaja hasta el POST para que el alta reconozca
el POI ya importado (AC-13); si falta, el alta es manual y no busca nada. Cuando esa identidad ya es
un lugar, el handler publica `savedPlaceId` en el modelo y `place-form.html` muestra el aviso con el
link al ABM de ese lugar (AC-14, I-30/I-31).

Los dos endpoints del importador comparten forma y sobre, y solo uno toca la red:

- `GET /admin/places/import/pois?amenity=...&south=..&west=..&north=..&east=..`: valida la forma
  (un tag del allowlist, los cuatro bordes y ningun parametro de mas) y el recorte a CABA, y
  devuelve `{"pois":[…],"fetchedAt":"…"|null,"refreshed":false}` leyendo `poi` y `poi_zone`. Cero red.
- `GET /admin/places/import/refresh` con la misma forma: si la zona+tag se trajo dentro de
  `place.import.freshness-minutes` (default 15) **no consulta Overpass** y responde el mismo sobre
  con `refreshed: false`. Si esta vencida o nunca se trajo, consulta `OverpassClient.findPois` con
  la zona sellada, llama a `PoiIngestService.ingest` (el único `@Transactional`) y responde el
  sobre con `refreshed: true`. Si el cliente lanza (red / 429 agotado), responde 502 con mensaje y
  **no toca ninguna fila**; la vista lo muestra sin romperse.

Cada elemento de `pois` es un `PoiView` (no un `OsmPoi`): `name`, `address`, `latitude`,
`longitude`, `openingHours`, `phone`, `website`, `category` (nombre del enum, lo que pinta el
marker), `externalId` (`"osm:<tipo>/<id>"` o `null`) y `savedPlaceId` (el `id` del lugar con esa
identidad, o `null`). `ZonePois` lleva `MapPoi` justamente para no perder categoría ni identidad, y
el catálogo se consulta una sola vez por request con `PlaceService.savedPlaceIds(...)`.

`fetchedAt` en `null` significa "esta zona y este tag nunca se cargaron", que es distinto de
"cargada sin resultados". `refreshed` dice si esa misma request pagó Overpass: es lo que permite
distinguir "se actualizó y no cambió nada" de "no se consultó porque lo guardado todavía vale".

### 7. Vistas del ABM

Files nuevos en `src/main/webapp/WEB-INF/templates/pages/admin/`:

- `places.html` — espejo de `users.html`: tabla con nombre, categoria, direccion, imagen
  (preview o placeholder), acciones Edit / Delete; link "+ New Place" y link "Importar del
  mapa" hacia `/admin/places/import`. Delete como form POST con `_csrf` y
  `<input type="hidden" name="_method" value="DELETE">`, igual que en `users.html`.
- `place-form.html` — espejo de `user-form.html`: campos del `EditPlaceRequest`, select de
  `PlaceCategory.values()` (el admin siempre puede cambiar la categoria pre-llena, AC-06),
  `imageUrl` opcional con hint de placeholder, y submit POST (PUT via `_method` en edicion). Lleva
  un hidden `externalId` con `th:value="${placeForm.externalId}"`: la identidad del POI tiene que
  viajar en el POST, y el form envia valores del DOM (un atributo `data-*`, que solo alimenta el
  estado de Vue, no se postea). Con `savedPlaceId` en el modelo (alta abierta desde un POI que ya es
  un lugar) muestra un aviso con el link "Ver el lugar guardado" al ABM de ese lugar.
- `place-import.html` — Leaflet con el CSS vendorizado (version fija 1.9.4), tile layer OSM con
  attribution `© OpenStreetMap contributors`: mapa centrado en CABA, select de queries predefinidas
  (restaurantes, bares, cafes, tiendas, museos), lista de POIs y markers por POI. Al entrar y al
  mover el mapa pide `/admin/places/import/pois` (lee de la base); el boton pide `/refresh`. Los
  markers se pintan con el color de su categoria (`lib/category-colors.js`, los mismos tokens que
  `/explore`) y el click —marker o fila— abre la vista previa `#poi-preview` sobre el mapa, sin
  navegar: muestra los datos del punto y desde ahí "Crear lugar" lleva a `/admin/places/new?...`.
  Un punto que ya es un lugar se marca en la fila y en la vista previa, y ofrece "Ver el lugar
  guardado" en vez de crear.

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
`integration/PlaceImportFreshnessTest.java`,
`integration/PlaceAdminSecurityTest.java`, `integration/PlaceImageTest.java`,
`infrastructure/place/HttpOverpassClientTest.java`,
`domain/place/MapPoiTest.java` (U-19..U-22),
`infrastructure/place/PoiIngestServiceTest.java` (U-23..U-27),
`e2e/PlaceViewABME2E.java` (espejo de `e2e/UserViewABME2E.java`, con page objects en
`e2e/views/`) y `e2e/PlaceImportE2E.java` (E-07..E-11: el navegador corre la vista real y
`page.route` intercepta `/pois` y `/refresh`, asi que no sale a internet; el caso de lectura real
siembra la fila por JDBC). Los mock de `OverpassClient` en integracion son `@MockBean` (AC-10).

## Archivos a crear/modificar

| Archivo | Accion |
|---------|--------|
| `domain/place/Place.java` | Actualizar (columnas opcionales `openingHours`, `phone`, `website`) |
| `domain/place/PlaceCategory.java` | Actualizar (`fromOsm(key, value)`) |
| `domain/place/PlaceRepository.java` | Actualizar (`deleteById`, `findByExternalId`, `findByExternalIdIn`) |
| `domain/place/PlaceService.java` | Actualizar (create/update/delete, `savedPlaceIds`) |
| `domain/place/OsmPoi.java` | Crear (DTO del POI importado, con identidad `osmType`/`osmId`) |
| `domain/place/OverpassClient.java` | Crear (interfaz, mockeable en tests) |
| `domain/place/MapPoi.java` | Crear (entidad `poi`: identidad `source_key`, `apply` para el diff) |
| `domain/place/PoiZone.java` | Crear (entidad `poi_zone`: cobertura por zona y tag) |
| `domain/place/PoiStatus.java` | Crear (`ACTIVE`, `REMOVED`: baja logica) |
| `domain/place/ZonePois.java` | Crear (sobre `pois` como `MapPoi` + `fetchedAt`) |
| `domain/place/MapPoiRepository.java` | Crear (`findInBox`, `saveAll`) |
| `domain/place/PoiZoneRepository.java` | Crear (`find`, `save`) |
| `domain/planplace/PlanPlaceRepository.java` | Actualizar (`existsByPlaceId`) |
| `infrastructure/place/JpaPlanPlaceRepository.java` | Actualizar (`existsByPlaceId` derivado) |
| `infrastructure/place/PlaceRepositoryImpl.java` | Actualizar (`deleteById`) |
| `infrastructure/place/PlaceServiceImpl.java` | Actualizar (alta/edicion/baja protegida, upsert por `externalId`) |
| `infrastructure/place/HttpOverpassClient.java` | Crear (implementacion HTTP de `OverpassClient`, sin cache) |
| `infrastructure/place/JpaMapPoiRepository.java` | Crear (Spring Data de `MapPoi`) |
| `infrastructure/place/MapPoiRepositoryImpl.java` | Crear (baja por caja: `findInBox`, `saveAll`) |
| `infrastructure/place/JpaPoiZoneRepository.java` | Crear (Spring Data de `PoiZone`) |
| `infrastructure/place/PoiZoneRepositoryImpl.java` | Crear (implementacion del puerto) |
| `infrastructure/place/PoiIngestService.java` | Crear (snap de zona, diff, cobertura) |
| `infrastructure/PlaceDataSeeder.java` | Actualizar (12 lugares, `imageUrl` opcional) |
| `presentation/place/PlaceAdminController.java` | Crear (ABM + importador; publica `savedPlaceId` en el alta) |
| `presentation/place/PlaceImportController.java` | Crear (`/import`, `/pois` y `/refresh`; sobre de `PoiView`) |
| `presentation/place/PoiView.java` | Crear (DTO del importador: categoría, identidad y lugar guardado) |
| `presentation/place/EditPlaceRequest.java` | Crear (DTO del form, espejo de `EditUserRequest`) |
| `templates/pages/admin/places.html` | Crear (listado, espejo de `users.html`) |
| `templates/pages/admin/place-form.html` | Crear (alta/edicion, espejo de `user-form.html`, hidden `externalId`, aviso de punto guardado) |
| `templates/pages/admin/place-import.html` | Crear (Leaflet + capa Overpass + attribution OSM, vista previa `#poi-preview`) |
| `core/js/lib/category-colors.js` | Crear (lector único de los tokens de categoría, compartido con `/explore`) |
| `templates/pages/places/list.html` | Actualizar (`data-category` en el marker) |
| `domain/place/PlaceCategoryTest.java` | Actualizar (U-01..U-04) |
| `infrastructure/PlaceDataSeederTest.java` | Crear (U-05..U-09) |
| `infrastructure/place/HttpOverpassClientTest.java` | Crear (U-10..U-12) |
| `domain/place/MapPoiTest.java` | Crear (U-19..U-22) |
| `infrastructure/place/PoiIngestServiceTest.java` | Crear (U-23..U-27) |
| `integration/PlaceRestControllerTest.java` | Actualizar (I-01..I-05) |
| `integration/PlaceAdminControllerTest.java` | Crear (I-06..I-12, I-26, I-27, I-30, I-31) |
| `domain/place/PlaceServiceImplTest.java` | Actualizar (U-28..U-34, upsert por identidad y `savedPlaceIds`) |
| `infrastructure/place/PlaceRepositoryTest.java` | Actualizar (I-28, I-29, I-32, I-33, `findByExternalId`/`findByExternalIdIn`) |
| `integration/PlaceImageTest.java` | Crear (I-13, I-14) |
| `integration/PlaceImportControllerTest.java` | Crear (I-15..I-25, I-34, I-35; ventana de frescura en `0`) |
| `integration/PlaceImportFreshnessTest.java` | Crear (I-36..I-39, ventana de frescura en 15) |
| `integration/PlaceAdminSecurityTest.java` | Crear (S-01..S-04) |
| `e2e/PlacesViewE2E.java` | Actualizar (E-01..E-03, recalibrado sin hex) |
| `e2e/PlaceViewABME2E.java` | Crear (E-04..E-06) |
| `e2e/PlaceImportE2E.java` | Crear (E-07..E-13, `/pois` y `/refresh` stubbeados con `page.route`) |
| `e2e/views/PlaceImportPage.java` | Crear (page object de la vista de import) |
| `e2e/ResetDatabase.java` | Actualizar (`DELETE FROM places` + `ALTER SEQUENCE` antes de `plans`; `DELETE FROM poi`/`poi_zone` y `seedPoi`) |
