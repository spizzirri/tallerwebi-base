/**
 * Picker de lugares del ABM de planes: el mapa del catalogo guardado (tabla places) con los
 * filtros de categoria y nombre del importador. Muestra solo lo que esta dentro de la zona
 * visible del mapa y deja afuera lo que el plan ya tiene.
 *
 * Dos modos, decididos por el data-mode del root:
 * - edit: "Agregar al plan" postea el form del fragmento (POST /admin/plans/{id}/places/add).
 * - create: el alta es un wizard sin Vue (pages/plan-create.js). El picker avisa con el evento
 *   plan-places:add y recibe el draft completo en plan-places:changed para no ofrecer lo ya
 *   elegido.
 *
 * Los datos salen de /api/places (el catalogo que ya usa /explore); el recorte por zona es local.
 * `Vue` es global del layout. El mapa y la busqueda sin acentos viven en lib/map-picker.js.
 */
import { apiFetch } from '../lib/api.js';
import { categoryColor } from '../lib/category-colors.js';
import {
  coordinatesOf,
  createMap,
  drawPoiMarkers,
  hasCoordinates,
  normalizeName,
  orDash
} from '../lib/map-picker.js';

const { createApp, ref, computed, watch, nextTick, onMounted } = Vue;

const ENDPOINT_PLACES = '/api/places';
const EVENT_ADD = 'plan-places:add';
const EVENT_CHANGED = 'plan-places:changed';
const ERROR_LOAD = 'No se pudo leer el catálogo de lugares. Recargue la página.';

/* Etiquetas en castellano de las categorias del enum (PlaceCategory). */
const CATEGORY_LABELS = {
  RESTAURANT: 'Restaurante',
  BAR: 'Bar',
  CAFE: 'Café',
  MUSEUM: 'Museo',
  PARK: 'Parque',
  SHOPPING: 'Compras',
  NIGHTLIFE: 'Vida nocturna',
  CULTURE: 'Cultura',
  SPORT: 'Deporte',
  OTHER: 'Otro'
};

const BUENOS_AIRES = [-34.6037, -58.3816];
const CITY_ZOOM = 13;

/* Un degenerate (getBounds con esquinas coincidentes) pasa mientras el mapa mide 0: no hay zona
   hasta que Leaflet vea un tamaño real. Mismo criterio que el importador. */
const boundsOf = (map) => {
  const bounds = map.getBounds();
  if (bounds.getNorth() <= bounds.getSouth() || bounds.getEast() <= bounds.getWest()) return null;
  return {
    south: bounds.getSouth(),
    west: bounds.getWest(),
    north: bounds.getNorth(),
    east: bounds.getEast()
  };
};

const insideBox = (place, box) =>
  !!box &&
  hasCoordinates(place) &&
  place.latitude >= box.south &&
  place.latitude <= box.north &&
  place.longitude >= box.west &&
  place.longitude <= box.east;

const parseIds = (raw) => (raw ? raw.split(',').filter((id) => id.trim() !== '') : []);

createApp({
  setup() {
    const root = document.getElementById('plan-places-picker');
    const isEdit = root.dataset.mode === 'edit';

    const places = ref([]);
    const loading = ref(true);
    const error = ref('');
    const search = ref('');
    /* Categorias elegidas; lista vacia = todas. */
    const chosen = ref([]);
    /* Lugares ya en el plan (edit) o en el draft (create): no se ofrecen. */
    const excluded = ref(parseIds(root.dataset.excluded));
    const selected = ref(null);
    /* Zona visible como bordes numericos; null hasta que el mapa mide algo. */
    const area = ref(null);
    const addPlaceId = ref(null);
    const addForm = ref(null);

    let map = null;
    let markers = [];

    const categories = computed(() =>
      [...new Set(places.value.map((place) => place.category))].sort()
    );

    const categoryLabel = (category) => CATEGORY_LABELS[category] || CATEGORY_LABELS.OTHER;

    /* Lo que el usuario puede elegir ahora: dentro de la zona, con el filtro, sin lo ya agregado. */
    const results = computed(() => {
      const term = normalizeName(search.value.trim());
      return places.value.filter(
        (place) =>
          insideBox(place, area.value) &&
          !excluded.value.includes(String(place.id)) &&
          (chosen.value.length === 0 || chosen.value.includes(place.category)) &&
          (!term || normalizeName(place.name).includes(term))
      );
    });

    const statusText = computed(() => {
      if (loading.value) return 'Cargando el catálogo...';
      if (!area.value) return 'Mueva el mapa para ver los lugares de esta zona.';
      if (results.value.length === 0) return 'No hay lugares disponibles en esta zona.';
      return results.value.length + (results.value.length === 1 ? ' lugar' : ' lugares') +
        ' en la zona';
    });

    const isChosen = (category) => chosen.value.includes(category);

    const toggleCategory = (category) => {
      chosen.value = isChosen(category)
        ? chosen.value.filter((item) => item !== category)
        : [...chosen.value, category];
    };

    /* Borde y texto del chip con el color de la categoria: el mismo token que el mapa. */
    const chipStyle = (category) => {
      const color = categoryColor(category);
      return {
        borderColor: color,
        backgroundColor: isChosen(category) ? color : '#ffffff',
        color: isChosen(category) ? '#ffffff' : '#374151'
      };
    };

    const drawMarkers = () => {
      markers = drawPoiMarkers(map, markers, results.value, selectPlace);
    };

    watch(results, drawMarkers);

    /* Si el elegido deja de estar en la lista (zona, filtro o alta) el panel se cierra. */
    watch(results, () => {
      if (selected.value && !results.value.includes(selected.value)) selected.value = null;
    });

    function selectPlace(place) {
      selected.value = place;
    }

    const closePreview = () => {
      selected.value = null;
    };

    const addPlace = async (place) => {
      if (!isEdit) {
        window.dispatchEvent(new CustomEvent(EVENT_ADD, { detail: place }));
        selected.value = null;
        return;
      }
      addPlaceId.value = place.id;
      await nextTick();
      addForm.value.submit();
    };

    const refreshArea = () => {
      area.value = boundsOf(map);
    };

    const loadPlaces = async () => {
      try {
        const response = await apiFetch(ENDPOINT_PLACES);
        places.value = await response.json();
      } catch (loadError) {
        console.error(loadError);
        error.value = loadError.status === 401 ? loadError.message : ERROR_LOAD;
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      map = createMap('plan-places-map', { center: BUENOS_AIRES, zoom: CITY_ZOOM });
      /* 'resize' cubre el mapa que nace oculto: al mostrarse el paso 2 cambia el tamaño sin
         mover el centro, y ese cambio no dispara moveend. */
      map.on('moveend zoomend resize', refreshArea);
      refreshArea();
      loadPlaces();
      /* El draft del alta cambia desde el wizard: el picker deja de ofrecer lo ya elegido. */
      window.addEventListener(EVENT_CHANGED, (event) => {
        excluded.value = event.detail.map(String);
      });
    });

    return {
      isEdit,
      results,
      categories,
      categoryLabel,
      chosen,
      isChosen,
      toggleCategory,
      chipStyle,
      search,
      statusText,
      error,
      selected,
      selectPlace,
      closePreview,
      addPlace,
      addPlaceId,
      addForm,
      categoryColor,
      coordinatesOf,
      orDash
    };
  }
}).mount('#plan-places-picker');
