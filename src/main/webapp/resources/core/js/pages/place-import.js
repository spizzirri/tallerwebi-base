/**
 * The OSM importer: map viewport, predefined queries and the point list. Extracted from
 * WEB-INF/templates/pages/admin/place-import.html (docs/guide-js.md R4).
 *
 * `Vue` and `L` are globals from the vendored classic scripts, which run before this
 * module. The CABA box comes from the page, because only Thymeleaf knows the backend value.
 */
import { apiFetch } from '../lib/api.js';
import { el } from '../lib/dom.js';

const CABA = (window.PLACE_IMPORT_SEED || {}).cabaBounds ?? null;
const { createApp, ref, computed, watch, onMounted } = Vue;

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
    const loading = ref(false);
    /* "Todavia no busque" no es lo mismo que "busque y no habia nada": cambia el texto. */
    const loaded = ref(false);
    let map = null;
    let markers = [];
    let pending = 0;
    let controller = null;

    /* Bordes del area visible como numeros (no el objeto de Leaflet): Vue no envuelve en proxy
       un objeto ajeno y la comparacion queda aritmetica. */
    const area = ref(null);
    /* La zona que se consulto: sirve para avisar cuando el mapa se fue a otra parte. */
    const loadedArea = ref(null);
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
    const onMapMoved = debounce(refreshArea, 120);

    /* Sin acentos ni mayusculas: "cafe" tiene que encontrar "Café". El filtro es local a
       proposito — volver a Overpass por cada tecla costaria ~30s sobre datos que ya estan. */
    const normalizeName = (text) => (text || '').normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase();

    const intersects = (a, b) => a.south < b.north && a.north > b.south &&
      a.west < b.east && a.east > b.west;

    /* Cargar solo tiene sentido si la zona visible toca CABA: el backend recorta a la ciudad. */
    const canLoad = computed(() => !!area.value && intersects(area.value, CABA));

    /* El mapa se movio a una zona que la lista actual no cubre: se avisa, no se vacia. */
    const staleArea = computed(() => !!loadedArea.value && !!area.value &&
      !intersects(area.value, loadedArea.value));

    const filtered = computed(() => {
      const term = normalizeName(search.value.trim());
      if (!term) return pois.value;
      return pois.value.filter((poi) => normalizeName(poi.name).includes(term));
    });

    /* "N de M" cuando el filtro recorta; el total solo cuando no hay filtro activo. */
    const statusText = computed(() => {
      if (loading.value) return 'Buscando puntos en la zona visible...';
      if (!loaded.value) return '';
      return filtered.value.length < pois.value.length
        ? filtered.value.length + ' de ' + pois.value.length + ' punto(s)'
        : pois.value.length + ' punto(s) encontrados';
    });

    /* El alta espera los mismos params que arma el marker. encodeURIComponent en cada valor:
       los nombres de OSM traen acentos, comas y parentesis. */
    const formUrl = (poi) => {
      const param = (name, value) => value === null || value === undefined || value === ''
        ? ''
        : '&' + name + '=' + encodeURIComponent(value);
      return '/admin/places/new?category=' + encodeURIComponent(selected.value.category) +
        param('name', poi.name) +
        param('address', poi.address) +
        param('latitude', poi.latitude) +
        param('longitude', poi.longitude) +
        param('openingHours', poi.openingHours) +
        param('phone', poi.phone) +
        param('website', poi.website);
    };

    /* Sin hex nuevo: el color sale de los tokens de marca, igual que los markers de /explore. */
    const brandColor = () => {
      const tokens = getComputedStyle(document.documentElement);
      return tokens.getPropertyValue('--primary').trim() ||
        tokens.getPropertyValue('--black').trim();
    };

    const drawMarkers = () => {
      markers.forEach((marker) => map.removeLayer(marker));
      markers = filtered.value
        .filter((poi) => poi.latitude !== null && poi.longitude !== null)
        .map((poi) => {
          const color = brandColor();
          const marker = L.circleMarker([poi.latitude, poi.longitude], {
            radius: 9,
            color: color,
            weight: 3,
            fillColor: color,
            fillOpacity: 0.9
          }).addTo(map);
          /* Leaflet parses a String popup as HTML and the OSM name is unsanitized:
             the label is a node, not markup. */
          marker.bindPopup(el('strong', { text: poi.name }));
          marker.on('click', () => {
            window.location.href = formUrl(poi);
          });
          return marker;
        });
    };

    /* Los markers se redibujan solos cuando cambia la lista filtrada. */
    watch(filtered, drawMarkers);

    const loadPois = async () => {
      if (!canLoad.value) return;
      const current = ++pending;
      /* Si se aprieta dos veces, la anterior no se espera: se aborta y manda la ultima. */
      if (controller) controller.abort();
      controller = new AbortController();
      const box = area.value;
      const query = selected.value;
      const param = (name, value) => '&' + name + '=' + encodeURIComponent(value);
      loadedArea.value = box;
      loading.value = true;
      error.value = '';
      try {
        const response = await apiFetch(
          '/admin/places/import/pois?' + query.key + '=' + encodeURIComponent(query.value) +
            param('south', box.south) + param('west', box.west) +
            param('north', box.north) + param('east', box.east),
          { signal: controller.signal }
        );
        const data = await response.json();
        if (current !== pending) return;
        pois.value = data;
        search.value = '';
        loaded.value = true;
      } catch (loadError) {
        if (current !== pending || loadError.name === 'AbortError') return;
        console.error(loadError);
        error.value = loadError.status === 401 ? loadError.message :
          'No se pudieron traer los puntos de OpenStreetMap. Probá de nuevo en unos segundos.';
      } finally {
        if (current === pending) loading.value = false;
      }
    };

    /* Al entrar no se consulta nada: el mapa queda listo y la consulta la dispara el boton. */
    onMounted(() => {
      map = L.map('map', { center: [-34.6037, -58.3816], zoom: 13 });
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
      }).addTo(map);
      /* El compilador de Tailwind corre diferido: sin esto el mapa se crea midiendo 0 y queda
         centrado fuera de lugar (la misma razon que en /explore). El invalidateSize va con
         pan por defecto: conserva el centro geografico; con pan:false el paso de 0 al tamaño
         real desplazaba la vista varios kilometros. */
      new ResizeObserver(() => map.invalidateSize()).observe(map.getContainer());
      requestAnimationFrame(() => map.invalidateSize());
      map.on('moveend zoomend', onMapMoved);
      refreshArea();
    });

    return {
      queries,
      selected,
      pois,
      search,
      filtered,
      statusText,
      error,
      loading,
      loaded,
      canLoad,
      staleArea,
      loadPois,
      formUrl
    };
  }
}).mount('#place-import-app');
