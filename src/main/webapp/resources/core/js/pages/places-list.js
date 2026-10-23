/**
 * The Places view: map, filters and the plan panel, extracted from
 * WEB-INF/templates/pages/places/list.html (docs/guide-js.md R4).
 *
 * `Vue`, `L` and `lucide` are globals from the vendored classic scripts, which the browser
 * runs before this module: modules are deferred, so all three exist when this file runs.
 */
import { apiFetch } from '../lib/api.js';
import { el } from '../lib/dom.js';
import {
  buildMarker,
  categoryIconHtml as categoryIcon,
  categoryIconNode,
  directionsLink,
  directionsUrl
} from '../lib/places-map.js';

const { createApp, ref, computed, onMounted, watch, nextTick } = Vue;

createApp({
  setup() {
    const places = ref([]);
    const selectedPlace = ref(null);
    const selectedCategory = ref('');
    const searchQuery = ref('');
    const categories = ref([]);
    const plansQuery = ref('');
    const showCreatePlan = ref(false);
    const planNameInput = ref(null);
    const planStep = ref(1);
    const draftPlaces = ref([]);
    const showAddToPlan = ref(false);
    const placeToAdd = ref(null);
    const availablePlans = ref([]);
    const selectedPlanId = ref('');
    const publicPlans = ref([]);
    const error = ref('');
    const setError = (message) => {
      error.value = message;
    };
    /* A session expiry keeps the helper's sentence -- "log in again" is the only
       useful thing to say -- while every other failure keeps this page's copy. */
    const failure = (cause, fallback) => {
      console.error(cause);
      return cause.status === 401 ? cause.message : fallback;
    };
    let nextDraftKey = 0;
    let map = null;
    let mapResizeObserver = null;
    let markers = [];
    const markersByPlaceId = new Map();

    /* Tokens de categoría declarados una sola vez, en layouts/base :: head.
       Se leen del :root computado, así que ningún hex vive acá y el mapa
       del marker y el del panel comparten la misma fuente. */
    const CATEGORY_TOKEN_BY_ENUM = {
      'RESTAURANT': '--category-restaurant',
      'BAR': '--category-bar',
      'CAFE': '--category-cafe',
      'MUSEUM': '--category-museum',
      'PARK': '--category-park',
      'SHOPPING': '--category-shopping',
      'NIGHTLIFE': '--category-nightlife',
      'CULTURE': '--category-culture',
      'SPORT': '--category-sport',
      'OTHER': '--category-other'
    };
    const OTHER_COLOR_TOKEN = '--category-other';

    const categoryColor = (category) => {
      const rootStyle = getComputedStyle(document.documentElement);
      const token = CATEGORY_TOKEN_BY_ENUM[category] || OTHER_COLOR_TOKEN;
      // trim() importa: getPropertyValue devuelve el valor crudo del token,
      // con espacios alrededor en varios navegadores. El fallback también sale
      // de un token, para que ningún hex quede literal acá.
      return rootStyle.getPropertyValue(token).trim()
        || rootStyle.getPropertyValue(OTHER_COLOR_TOKEN).trim();
    };

    const filteredPlaces = computed(() => {
      return places.value.filter(place => {
        const matchCategory = !selectedCategory.value || place.category === selectedCategory.value;
        const matchSearch = !searchQuery.value ||
          place.name.toLowerCase().includes(searchQuery.value.toLowerCase());
        return matchCategory && matchSearch;
      });
    });

    const capitalize = (str) => {
      if (!str) return '';
      return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
    };

    /* Un icono de Lucide listo para v-html. La librería dibuja con stroke="currentColor", así
       que el color lo pone el contenedor. */
    const lucideIcon = (name, size = 16) =>
      lucide.createElement(lucide.icons[name], {
        width: size,
        height: size,
        'aria-hidden': 'true'
      }).outerHTML;

    /* OSM suele traer la web sin esquema ("parrilla.example.com"). Se asume https y nunca se
       navega a otro esquema: si el dato dice "javascript:...", el resultado es una URL HTTPS
       inválida en vez de código. Una web vacía devuelve '' y la fila no se muestra. */
    const websiteHref = (website) => {
      const raw = (website || '').trim();
      if (!raw) return '';
      return /^https?:\/\//i.test(raw) ? raw : 'https://' + raw;
    };

    const websiteLabel = (website) =>
      websiteHref(website).replace(/^https?:\/\//i, '').replace(/\/$/, '');

    /* tel: no lleva espacios ni guiones; el número que se muestra sí los conserva. */
    const telHref = (phone) => 'tel:' + (phone || '').replace(/[^\d+]/g, '');

    const clearFilters = () => {
      selectedCategory.value = '';
      searchQuery.value = '';
    };

    const applyFilters = () => {
      updateMarkers();
    };

    const overlayWidth = (id) => {
      const el = document.getElementById(id);
      return el ? el.offsetWidth : 0;
    };

    const focusPlace = async (place) => {
      const marker = markersByPlaceId.get(place.id);
      if (!map || !marker) return;

      // The details aside is rendered after the reactive update.
      await nextTick();
      map.stop();
      // Instant: a second animated setView issued mid-zoom-animation is superseded by
      // the first one's target, so rapid card clicks would land on the wrong marker.
      map.setView(marker.getLatLng(), Math.max(map.getZoom(), 16), { animate: false });
      // Plans (left) and details (right) overlay the map: center on the uncovered area.
      const offsetX = (overlayWidth('plans-aside') - overlayWidth('place-details-aside')) / 2;
      if (offsetX) map.panBy([-offsetX, 0], { animate: false });
      marker.openPopup();
    };

    const selectPlace = (place) => {
      selectedPlace.value = place;
      focusPlace(place);
    };

    const filteredPlans = computed(() => {
      const query = plansQuery.value.trim().toLowerCase();
      return publicPlans.value.filter(plan => !query || plan.name.toLowerCase().includes(query));
    });

    const openCreatePlan = async () => {
      showCreatePlan.value = true;
      await nextTick();
      if (planNameInput.value) planNameInput.value.focus();
    };

    const closeCreatePlan = () => {
      showCreatePlan.value = false;
      planStep.value = 1;
      draftPlaces.value = [];
    };

    const activePlan = ref(null);

    const loadActiveEntries = async (planId) => {
      const response = await apiFetch('/api/plans/' + planId + '/places');
      return response.json();
    };

    const openActivePlan = async (id, name) => {
      try {
        const entries = await loadActiveEntries(id);
        showCreatePlan.value = false;
        activePlan.value = { id, name, entries };
      } catch (error) {
        setError(failure(error, 'Error al cargar el plan.'));
      }
    };

    const closeActivePlan = () => {
      activePlan.value = null;
    };

    const addToActivePlan = async (place) => {
      const plan = activePlan.value;
      try {
        await apiFetch('/api/plans/' + plan.id + '/places?placeId=' + place.id, { method: 'POST' });
        plan.entries = await loadActiveEntries(plan.id);
      } catch (error) {
        // 409 is a product answer, not a failure: the place is already in the plan.
        setError(error.status === 409
          ? 'El destino ya se encuentra en el plan.'
          : failure(error, 'Error al agregar el lugar al plan.'));
      }
    };

    /* [MARK] Guardar/quitar desde el sidebar. Vue repinta la card en el lugar: el estado lo
       confirma el servidor (si el fetch falla, el icono no cambia). El confirm se pregunta
       solo al quitar, igual que en el fragment server-rendered. */
    const toggleBookmark = async (plan) => {
      const removing = plan.bookmarked === true;
      if (removing && !window.confirm('¿Quitar este plan de Guardados?')) return;
      try {
        await apiFetch('/api/plans/' + plan.id + '/bookmarks', {
          method: removing ? 'DELETE' : 'POST'
        });
        plan.bookmarked = !removing;
      } catch (error) {
        setError(failure(error, 'Error al actualizar Guardados.'));
      }
    };

    document.addEventListener('click', (event) => {
      const link = event.target.closest('[data-open-plan]');
      if (!link) return;
      event.preventDefault();
      openActivePlan(link.dataset.planId, link.dataset.planName);
    });

    document.addEventListener('keydown', (event) => {
      if (event.key === 'Escape' && activePlan.value) closeActivePlan();
    });

    document.addEventListener('keydown', (event) => {
      if (event.key === 'Escape' && showCreatePlan.value) closeCreatePlan();
    });

    // The same place may be added repeatedly, so rows are keyed by their own counter.
    const addToDraft = (place) => {
      draftPlaces.value.push({ key: nextDraftKey++, place, description: '', visitTime: '' });
    };

    const removeDraft = (index) => {
      draftPlaces.value.splice(index, 1);
    };

    const moveDraft = (index, offset) => {
      const target = index + offset;
      if (target < 0 || target >= draftPlaces.value.length) return;
      const [item] = draftPlaces.value.splice(index, 1);
      draftPlaces.value.splice(target, 0, item);
    };

    const nextPlanStep = () => {
      if (planNameInput.value && !planNameInput.value.reportValidity()) return;
      if (planStep.value === 2 && draftPlaces.value.length === 0) return;
      planStep.value = Math.min(3, planStep.value + 1);
    };

    const onPlanSubmit = (event) => {
      if (planStep.value < 3) {
        event.preventDefault();
        nextPlanStep();
      } else if (draftPlaces.value.length === 0) {
        event.preventDefault();
      }
    };

    const closeDetails = () => {
      selectedPlace.value = null;
      if (map) map.closePopup();
    };

    /* Leaflet parses a String popup as HTML, and the place name comes from the admin ABM
       and the OSM importer, neither of which sanitizes it: build the nodes instead. */
    const popupContent = (place) => el('div', {}, [
      el('strong', { text: place.name }),
      el('br'),
      place.category,
      // [MAPS] Como llegar desde el popup del marcador, como en explore: usa la
      // direccion de texto (no la coordenada) para no mandar a otra calle, con el
      // nombre delante para desambiguar.
      el('br'),
      directionsLink(place, { class: 'text-primary fw-medium d-inline-block mt-1' })
    ]);

    const updateMarkers = () => {
      if (!map) return;
      markers.forEach(m => map.removeLayer(m));
      markers = [];
      markersByPlaceId.clear();

      filteredPlaces.value.forEach(place => {
        if (place.latitude && place.longitude) {
          const color = categoryColor(place.category);

          // El icono usa stroke="currentColor", así que este color:white es lo que
          // lo pinta de blanco sobre el fondo de categoría. Antes venía hardcodeado
          // dentro de cada SVG.
          // data-category es el hook estable del E2E (AC-09): el color sigue pintando
          // el pin, pero el test no se ata a su hex.
          const badge = el('div', {
            dataset: { category: place.category },
            style: 'background-color:' + color + '; color:#fff; width:32px; height:32px; border-radius:50%; border:3px solid #fff; box-shadow:0 2px 6px rgba(0,0,0,0.3); display:flex; align-items:center; justify-content:center;'
          }, [categoryIconNode(place.category, 16)]);

          const marker = buildMarker(place, { badge, className: 'custom-marker', size: 32, popupAnchor: [0, -16] }).addTo(map);

          marker.bindPopup(popupContent(place), { autoPan: false });
          marker.on('click', () => {
            selectedPlace.value = place;
          });
          markers.push(marker);
          markersByPlaceId.set(place.id, marker);
        }
      });
    };

    const closeAddToPlan = () => {
      showAddToPlan.value = false;
      placeToAdd.value = null;
      availablePlans.value = [];
    };

    const confirmAddToPlan = async () => {
      if (!placeToAdd.value || !selectedPlanId.value) return;
      // Emit event for the external JS module to handle the API call
      const event = new CustomEvent('confirm-add-to-plan', {
        detail: {
          planId: selectedPlanId.value,
          placeId: placeToAdd.value.id
        }
      });
      document.dispatchEvent(event);
      closeAddToPlan();
    };

    const createNewPlan = () => {
      // La creación ya no vive en /plans/new: se abre el panel de creación de /explore con el
      // lugar pendiente ya cargado en el borrador. El modal "Agregar a plan" se cierra
      // después de copiar el lugar, porque tapa el panel (overlay z-2000 sobre aside z-1000).
      const pendingPlace = placeToAdd.value;
      closeAddToPlan();
      if (pendingPlace) {
        addToDraft(pendingPlace);
      }
      openCreatePlan();
    };

    // Listen for add-to-plan events from the external JavaScript module
    document.addEventListener('add-to-plan', async (event) => {
      const place = event.detail;

      if (showCreatePlan.value) {
        addToDraft(place);
        return;
      }
      if (activePlan.value) {
        await addToActivePlan(place);
        return;
      }

      placeToAdd.value = place;

      try {
        const response = await apiFetch('/api/plans');
        const userPlans = await response.json();

        const plansWithoutPlace = userPlans.filter(plan =>
          !plan.planPlaces.some(pp => pp.placeId === place.id)
        );

        // La modal se abre siempre (tengas 0, algunos o todos los
        // planes ocupados por este lugar): desde ahí siempre se puede
        // crear un plan nuevo, y si hay planes disponibles también se
        // puede elegir uno existente.
        availablePlans.value = plansWithoutPlace;
        selectedPlanId.value = '';
        showAddToPlan.value = true;
      } catch (error) {
        setError(failure(error, 'Error al cargar los planes.'));
      }
    });

    // The add-to-plan module has no error surface of its own: it reports here.
    document.addEventListener('plan-error', (event) => {
      setError(event.detail.message);
    });

    onMounted(async () => {
      await nextTick();

      // [PPV] Feed real de /explore: planes públicos próximos desde la API. Un fallo deja la
      // lista vacía (igual que el resto de la página), nunca rompe el mapa ni el panel.
      try {
        const publicResponse = await apiFetch('/api/plans/public');
        publicPlans.value = await publicResponse.json();
      } catch (error) {
        // A failed feed leaves the list empty, exactly like having no plans.
        console.error('Error fetching public plans:', error);
      }

      // The vendored Tailwind compiler runs deferred, so this can execute before any
      // stylesheet exists: L.map() inside that 1280x0 box bakes an offset center which
      // invalidateSize({ pan: false }) then preserves, leaving the opening view shifted
      // and the markers outside the window. Wait for the layout to settle first.
      const mapContainer = document.getElementById('map');
      const layoutDeadline = performance.now() + 2000;
      while (!mapContainer.clientHeight && performance.now() < layoutDeadline) {
        await new Promise((resolve) => requestAnimationFrame(resolve));
      }

      // Initialize map centered on Buenos Aires
      map = L.map('map', {
        center: [-34.6037, -58.3816],
        zoom: 13
      });
      // Plans aside overlays the left edge: center on the uncovered area. En mobile es un
      // bottom-sheet a todo el ancho (no tapa un costado), asi que no hay costado que
      // centrar: solo corre el mapa en escritorio. [MOBILE]
      if (window.innerWidth >= 768) {
          map.panBy([-overlayWidth('plans-aside') / 2, 0], { animate: false });
      }

      mapResizeObserver = new ResizeObserver(() => {
        map.invalidateSize({ pan: false });
      });
      mapResizeObserver.observe(map.getContainer());
      requestAnimationFrame(() => map.invalidateSize({ pan: false }));

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
      }).addTo(map);

      // Fetch places from API
      try {
        const response = await apiFetch('/api/places');
        places.value = await response.json();
      } catch (error) {
        setError(failure(error, 'Error al cargar los lugares.'));
      }

      // Extract unique categories
      const cats = new Set(places.value.map(p => p.category));
      categories.value = Array.from(cats).sort();

      // Add markers
      updateMarkers();

    });

    watch(filteredPlaces, () => {
      updateMarkers();
    });

    return {
      places,
      error,
      selectedPlace,
      selectedCategory,
      searchQuery,
      categories,
      filteredPlaces,
      capitalize,
      categoryColor,
      categoryIcon,
      lucideIcon,
      websiteHref,
      websiteLabel,
      telHref,
      directionsUrl,
      clearFilters,
      applyFilters,
      focusPlace,
      selectPlace,
      closeDetails,
      plansQuery,
      filteredPlans,
      toggleBookmark,
      showCreatePlan,
      planNameInput,
      openCreatePlan,
      closeCreatePlan,
      planStep,
      draftPlaces,
      activePlan,
      closeActivePlan,
      addToDraft,
      removeDraft,
      moveDraft,
      nextPlanStep,
      onPlanSubmit,
      showAddToPlan,
      placeToAdd,
      availablePlans,
      selectedPlanId,
      closeAddToPlan,
      confirmAddToPlan,
      createNewPlan
    };
  }
}).mount('#explore-app');
