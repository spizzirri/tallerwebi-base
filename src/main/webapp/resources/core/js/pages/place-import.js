/**
 * The OSM importer: map viewport, predefined queries, the point list and the preview panel that
 * opens over the map instead of navigating to the alta. Extracted from
 * WEB-INF/templates/pages/admin/place-import.html (docs/guide-js.md R4).
 *
 * `Vue` and `L` are globals from the vendored classic scripts, which run before this
 * module. The CABA box comes from the page, because only Thymeleaf knows the backend value.
 *
 * Dos endpoints y una regla: leer es barato, consultar cuesta medio minuto. `/pois` se pide
 * al entrar y cada vez que el mapa se mueve, y sale de la base; `/refresh` solo cuando el
 * admin presiona el boton, porque es el unico que habla con Overpass.
 */
import { apiFetch } from '../lib/api.js';
import { categoryColor } from '../lib/category-colors.js';
import {
  coordinatesOf,
  createMap,
  drawPoiMarkers,
  intersects,
  normalizeName,
  orDash
} from '../lib/map-picker.js';

const CABA = (window.PLACE_IMPORT_SEED || {}).cabaBounds ?? null;
const { createApp, ref, computed, watch, onMounted } = Vue;

const ENDPOINT_STORED = '/admin/places/import/pois';
const ENDPOINT_REFRESH = '/admin/places/import/refresh';

/* Dos errores distintos a proposito: "no se pudo leer lo guardado" y "Overpass no contesto"
   piden cosas distintas del admin. */
const ERROR_STORED = 'No se pudieron leer los puntos guardados. Recargá la página.';
const ERROR_REFRESH =
  'No se pudieron traer los puntos de OpenStreetMap. Probá de nuevo en unos segundos.';

/* Cada cuanto se recalcula el "hace X" de la ultima carga, sin recargar la pagina. */
const CLOCK_TICK_MS = 30000;

/* Consultas predefinidas: el par OSM que viaja a /admin/places/import/pois y la categoria que
   el importador sugiere en el form. La sugerencia no decide nada: el select del ABM deja
   cambiarla antes de guardar (AC-06) y el mapeo autoritativo sigue siendo
   PlaceCategory.fromOsm(key, value). */
const QUERIES = [
  { label: 'Restaurantes', key: 'amenity', value: 'restaurant', category: 'RESTAURANT' },
  { label: 'Bares', key: 'amenity', value: 'bar', category: 'BAR' },
  { label: 'Cafés', key: 'amenity', value: 'cafe', category: 'CAFE' },
  { label: 'Tiendas', key: 'shop', value: 'clothes', category: 'SHOPPING' },
  { label: 'Museos', key: 'amenity', value: 'museum', category: 'MUSEUM' }
];

createApp({
  setup() {
    const queries = QUERIES;
    const selected = ref(QUERIES[0]);
    const pois = ref([]);
    const search = ref('');
    const error = ref('');
    /* Aviso de un refresh que no consulto Overpass porque la zona todavia valia: sin esto el
       boton "Actualizar" parece no hacer nada. */
    const note = ref('');
    const loading = ref(false);
    /* Ingesta en curso: el boton no puede disparar dos y una lectura no la cancela. */
    const refreshing = ref(false);
    /* "Todavia no leí" no es lo mismo que "leí y no habia nada": cambia el texto. */
    const loaded = ref(false);
    /* ISO-8601 de la ultima carga de esta zona y este tipo, o null si nunca se cargo. */
    const fetchedAt = ref(null);
    /* El punto elegido en el mapa o en la lista: abre la vista previa sobre el mapa en vez de
       navegar. null = no hay panel abierto. */
    const selectedPoi = ref(null);
    /* Reloj propio: el "hace X" tiene que envejecer aunque nadie toque la pagina. */
    const now = ref(Date.now());
    let map = null;
    let markers = [];
    let pending = 0;
    let controller = null;

    /* Bordes del area visible como numeros (no el objeto de Leaflet): Vue no envuelve en proxy
       un objeto ajeno y la comparacion queda aritmetica. */
    const area = ref(null);
    const refreshArea = () => {
      const bounds = map.getBounds();
      /* Antes de que Tailwind mida el mapa, el contenedor mide 0 y getBounds() devuelve
       esquinas coincidentes. Ese "area" de cero intersecta CABA (un punto dentro de la
       ciudad ya cumple a.south < b.north && a.north > b.south) y habilitaria el boton
       para una consulta sin zona: no hay area hasta que Leaflet vea un tamaño real. */
      const degenerate = bounds.getNorth() <= bounds.getSouth() ||
        bounds.getEast() <= bounds.getWest();
      area.value = degenerate ? null : {
        south: bounds.getSouth(),
        west: bounds.getWest(),
        north: bounds.getNorth(),
        east: bounds.getEast()
      };
      /* Refleja el zoom en el DOM: sirve para ver el estado y para que el E2E espere el
         cambio sin dormir (mismo criterio que data-category en los markers de /explore). */
      map.getContainer().dataset.zoom = map.getZoom();
      /* Cuenta los movimientos cerrados (inercia incluida): el E2E espera este numero para
         panear dos veces seguidas sin pisar la animacion anterior. */
      map.getContainer().dataset.moves =
        (Number(map.getContainer().dataset.moves) || 0) + 1;
    };
    const debounce = (fn, wait) => {
      let timer = null;
      return () => {
        clearTimeout(timer);
        timer = setTimeout(fn, wait);
      };
    };

    /* Cargar solo tiene sentido si la zona visible toca CABA: el backend recorta a la ciudad. */
    const canLoad = computed(() => !!area.value && intersects(area.value, CABA));
    /* La zona (y el tipo) que se esta mirando ya se cargo alguna vez. */
    const zoneLoaded = computed(() => !!fetchedAt.value);

    const filtered = computed(() => {
      const term = normalizeName(search.value.trim());
      if (!term) return pois.value;
      return pois.value.filter((poi) => normalizeName(poi.name).includes(term));
    });

    /* "N de M" cuando el filtro recorta; el total solo cuando no hay filtro activo. Una lectura es
       tan corta que no reemplaza el conteo: solo la ingesta (30 s) se anuncia. */
    const statusText = computed(() => {
      if (refreshing.value) return 'Consultando OpenStreetMap: puede tardar hasta 30 s...';
      if (!loaded.value) return loading.value ? 'Leyendo los puntos guardados...' : '';
      if (note.value) return note.value;
      return filtered.value.length < pois.value.length
        ? filtered.value.length + ' de ' + pois.value.length + ' punto(s)'
        : pois.value.length + ' punto(s) encontrados';
    });

    /* Antigüedad legible de lo que hay en el mapa. Se calcula, no se guarda: el texto cambia
       con el reloj aunque los datos no. */
    const loadedAgo = computed(() => {
      if (!fetchedAt.value) return '';
      const seconds = Math.max(0, Math.round((now.value - Date.parse(fetchedAt.value)) / 1000));
      if (seconds < 60) return 'recién';
      const minutes = Math.round(seconds / 60);
      if (minutes < 60) return 'hace ' + minutes + ' min';
      const hours = Math.round(minutes / 60);
      if (hours < 24) return 'hace ' + hours + ' h';
      return 'hace ' + Math.round(hours / 24) + ' día(s)';
    });

    /* Zona que nunca se cargo: hay que decirlo, porque "no hay puntos" y "no los pediste
       todavia" se parecen demasiado. Solo aplica cuando no hay nada que mostrar. */
    const neverLoaded = computed(() => loaded.value && !loading.value && !error.value &&
      !zoneLoaded.value && pois.value.length === 0);

    /* El boton dice lo que va a hacer, no lo que esta pasando: el "en curso" va en el texto de
       estado. Asi el texto no parpadea en cada relectura de la base. */
    const loadLabel = computed(() =>
      zoneLoaded.value ? 'Actualizar puntos de esta zona' : 'Cargar puntos de esta zona'
    );

    /* Identidad de la fila, la misma que guarda el backend en source_key: dos POIs homonimos en
       esquinas distintas son dos filas y el v-for no puede confundirlas. Un POI sin identidad de
       OSM cae en la clave de respaldo (nombre@lat,lon). */
    const keyOf = (poi) => poi.externalId ||
      poi.name + '@' + poi.latitude + ',' + poi.longitude;

    /* El alta espera los mismos params que arma el marker. encodeURIComponent en cada valor:
       los nombres de OSM traen acentos, comas y parentesis. */
    const formUrl = (poi) => {
      const param = (name, value) => value === null || value === undefined || value === ''
        ? ''
        : '&' + name + '=' + encodeURIComponent(value);
      /* La categoria del punto manda (es la que se guardo en la fila); el tipo elegido en el
         selector es solo el respaldo de un punto que no la trae. */
      return '/admin/places/new?category=' +
        encodeURIComponent(poi.category || selected.value.category) +
        param('name', poi.name) +
        param('address', poi.address) +
        param('latitude', poi.latitude) +
        param('longitude', poi.longitude) +
        param('openingHours', poi.openingHours) +
        param('phone', poi.phone) +
        param('website', poi.website) +
        param('externalId', poi.externalId);
    };

    /* El punto elegido, ya sea desde el mapa o desde la lista: las dos vias abren el panel. */
    const selectPoi = (poi) => {
      selectedPoi.value = poi;
    };

    const closePreview = () => {
      selectedPoi.value = null;
    };

    /* Los markers del mapa (color por categoria, popup y click) viven en lib/map-picker.js. */
    const drawMarkers = () => {
      markers = drawPoiMarkers(map, markers, filtered.value, selectPoi);
    };

    /* Los markers se redibujan solos cuando cambia la lista filtrada. */
    watch(filtered, drawMarkers);

    /* Si el elegido dejo de estar en la lista (filtro, carga nueva, cambio de tipo) el panel se
       cierra: no puede quedar mostrando un punto que ya no esta. */
    watch(filtered, () => {
      if (selectedPoi.value && !filtered.value.includes(selectedPoi.value)) closePreview();
    });

    /* Un solo camino para los dos endpoints: cambia la URL y el texto del error, no el contrato
       (sobre {pois, fetchedAt}) ni el manejo de la carrera entre pedidos. */
    const requestPois = async (endpoint, failure) => {
      if (!canLoad.value) return;
      const isRefresh = endpoint === ENDPOINT_REFRESH;
      /* Una lectura no cancela una ingesta: abortar el refresh dejaria la zona a medio guardar.
         Si termina, su respuesta ya trae la lista. */
      if (!isRefresh && refreshing.value) return;
      const current = ++pending;
      /* Si se aprieta dos veces, la anterior no se espera: se aborta y manda la ultima. */
      if (controller) controller.abort();
      controller = new AbortController();
      const box = area.value;
      const query = selected.value;
      const param = (name, value) => '&' + name + '=' + encodeURIComponent(value);
      loading.value = true;
      refreshing.value = isRefresh;
      error.value = '';
      try {
        const response = await apiFetch(
          endpoint + '?' + query.key + '=' + encodeURIComponent(query.value) +
            param('south', box.south) + param('west', box.west) +
            param('north', box.north) + param('east', box.east),
          { signal: controller.signal }
        );
        const data = await response.json();
        if (current !== pending) return;
        pois.value = data.pois || [];
        fetchedAt.value = data.fetchedAt || null;
        note.value = isRefresh && data.refreshed === false
          ? 'Esta zona se actualizó hace instantes: se usaron los puntos guardados.'
          : '';
        loaded.value = true;
      } catch (loadError) {
        if (current !== pending || loadError.name === 'AbortError') return;
        console.error(loadError);
        note.value = '';
        error.value = loadError.status === 401 ? loadError.message : failure;
      } finally {
        if (current === pending) {
          loading.value = false;
          refreshing.value = false;
        }
      }
    };

    /* Lo guardado de la zona visible: al entrar y en cada movimiento del mapa. */
    const readStored = () => requestPois(ENDPOINT_STORED, ERROR_STORED);
    /* El boton: Overpass + diff, y devuelve la misma lista ya actualizada. */
    const refreshZone = () => requestPois(ENDPOINT_REFRESH, ERROR_REFRESH);

    /* Mover el mapa ya no consulta Overpass: relee la base para la zona nueva. */
    const onMapMoved = debounce(() => {
      refreshArea();
      readStored();
    }, 120);

    /* Cambiar de tipo es otra lista: se relee (de la base) y el filtro viejo deja de tener
       sentido sobre puntos que ya no estan. */
    watch(selected, () => {
      search.value = '';
      readStored();
    });

    onMounted(() => {
      map = createMap('map', { center: [-34.6037, -58.3816], zoom: 13 });
      map.on('moveend zoomend', onMapMoved);
      /* Mismo camino que un movimiento: si el mapa todavia mide 0 no hay area y no se pide
         nada; el invalidateSize de arriba dispara el moveend que sí la tiene. */
      onMapMoved();
      setInterval(() => {
        now.value = Date.now();
      }, CLOCK_TICK_MS);
    });

    return {
      queries,
      selected,
      pois,
      search,
      filtered,
      statusText,
      loadedAgo,
      neverLoaded,
      loadLabel,
      zoneLoaded,
      error,
      loading,
      refreshing,
      loaded,
      canLoad,
      keyOf,
      refreshZone,
      formUrl,
      selectedPoi,
      selectPoi,
      closePreview,
      orDash,
      coordinatesOf,
      categoryColor
    };
  }
}).mount('#place-import-app');
