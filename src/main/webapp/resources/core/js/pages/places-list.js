/**
 * The Places view: map, filters and the plan panel, extracted from
 * WEB-INF/templates/pages/places/list.html (docs/guide-js.md R4).
 *
 * `Vue`, `L` and `lucide` are globals from the vendored classic scripts, which the browser
 * runs before this module: modules are deferred, so all three exist when this file runs.
 */
import { apiFetch } from '../lib/api.js';
import { categoryColor } from '../lib/category-colors.js';
import { el } from '../lib/dom.js';
import {
  effectiveMinutes,
  formatClock,
  formatDate,
  formatDuration,
  scheduleStops,
  TRANSPORT_LABELS
} from '../lib/plan-schedule.js';
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
    // [APL] Sesión server-side vía data-* (guide-js R3): /api/plans devuelve [] igual para
    // anónimo y para un usuario sin planes, así que el gate del picker no puede salir del
    // fetch. Sin el atributo (render fuera del controller) se asume anónimo.
    const isLogged =
      document.getElementById('explore-app')?.dataset.isLogged === 'true';
    const places = ref([]);
    const selectedPlace = ref(null);
    const selectedCategory = ref('');
    const searchQuery = ref('');
    const categories = ref([]);
    const plansQuery = ref('');
    const showCreatePlan = ref(false);
    const planNameInput = ref(null);
    const planDateInput = ref(null);
    const planStep = ref(1);
    const draftPlaces = ref([]);
    /* Los tres datos de los que sale el horario. La fecha vive en el input nativo, así que su
       ref es solo el espejo que necesita el preview. El fin del día no se expone: el preview (y
       el servidor) cierran siempre a las 23:00. */
    const planDate = ref('');
    const planStartTime = ref('10:00');
    const planTransportMode = ref('WALK');
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
    // [ITI] Capas del borrador sobre el mapa: handles propias, nunca las de los lugares.
    let draftMarkers = [];
    let draftRouteLine = null;

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
      planDate.value = '';
      planStartTime.value = '10:00';
      planTransportMode.value = 'WALK';
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

    /* [APL] Panel de detalle: agregar a plan inline, sin modal (AC-03). Los planes propios se
       piden una sola vez al montar. Se listan TODOS, aunque el plan ya contenga el lugar (T7):
       filtrarlo afuera escondia el picker entero para ese lugar, y tras un alta tambien se
       desmontaba el mensaje de exito. El plan contenedor queda deshabilitado y marcado; el
       backend no rechaza duplicados (no hay 409), asi que la UI es la unica guarda. */
    const planOptions = ref([]);
    const planPickId = ref('');
    const planBusy = ref(false);
    const planMessage = ref('');
    const planError = ref(false);

    const loadPlanOptions = async () => {
      const response = await apiFetch('/api/plans');
      const plans = await response.json();
      // Un payload no-array no debe romper el picker: se trata como "sin planes".
      planOptions.value = Array.isArray(plans) ? plans : [];
    };

    const availablePlans = computed(() => planOptions.value);

    const planContainsPlace = (plan) => {
      const place = selectedPlace.value;
      return (
        !!place &&
        (plan.planPlaces || []).some(entry => String(entry.placeId) === String(place.id))
      );
    };

    const firstAddablePlan = (plans) =>
      plans.find(plan => !planContainsPlace(plan))?.id || '';

    watch(selectedPlace, () => {
      planPickId.value = firstAddablePlan(availablePlans.value);
      planMessage.value = '';
      planError.value = false;
    });

    // Tras una alta el plan sigue listado pero ya contiene el lugar: si el select quedó
    // apuntando a él, vuelve al primero todavía agregable.
    watch(availablePlans, (plans) => {
      const current = plans.find(plan => plan.id === planPickId.value);
      if (!current || planContainsPlace(current)) {
        planPickId.value = firstAddablePlan(plans);
      }
    });

    const addSelectedToPlan = async () => {
      const plan = availablePlans.value.find(candidate => candidate.id === planPickId.value);
      if (planBusy.value || !plan || planContainsPlace(plan)) return;
      planBusy.value = true;
      planMessage.value = '';
      planError.value = false;
      try {
        await apiFetch('/api/plans/' + plan.id + '/places?placeId=' + selectedPlace.value.id,
          { method: 'POST' });
        await loadPlanOptions();
        planMessage.value = 'Agregado a ' + plan.name + '.';
      } catch (error) {
        planError.value = error.status === 409;
        planMessage.value = error.status === 409
          ? 'El destino ya se encuentra en el plan.'
          : failure(error, 'Error al agregar el lugar al plan.');
      } finally {
        planBusy.value = false;
      }
    };

    /* [MARK] Guardar/quitar desde el sidebar. Igual que el toggle server-rendered: el estado se
       pinta antes de la respuesta, el boton queda bloqueado mientras vuela y un fallo lo revierte
       y muestra el aviso de la vista. El confirm se pregunta solo al quitar. */
    const toggleBookmark = async (plan) => {
      if (plan.bookmarking) return;

      const removing = plan.bookmarked === true;
      if (removing && !window.confirm('¿Quitar este plan de Guardados?')) return;

      const previous = removing;
      plan.bookmarked = !removing;
      plan.bookmarking = true;
      setError('');

      try {
        const response = await apiFetch('/api/plans/' + plan.id + '/bookmarks', {
          method: removing ? 'DELETE' : 'POST'
        });
        if (response.status !== 204) throw new Error('Unexpected status ' + response.status);
      } catch (error) {
        plan.bookmarked = previous;
        setError(error.status === 404
          ? 'No se pudo guardar: el plan ya no está disponible.'
          : failure(error, 'No se pudo actualizar Guardados. Intentá de nuevo.'));
        if (error.status === 401) {
          window.setTimeout(() => window.location.assign('/auth/login'), 1200);
        }
      } finally {
        plan.bookmarking = false;
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
      draftPlaces.value.push({
        key: nextDraftKey++,
        place,
        description: '',
        transportModeToNext: '',
        travelMinutesToNext: ''
      });
    };

    // [T10] El panel de detalle con el wizard abierto agrega al plan EN PROCESO, igual que
    // el "+" de las tarjetas (listener add-to-plan), en vez de ofrecer crear otro plan.
    const addSelectedToDraft = () => {
      addToDraft(selectedPlace.value);
      planError.value = false;
      planMessage.value = 'Agregado al plan en proceso.';
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

    /* Preview del horario: lo calcula lib/plan-schedule.js, que espeja la calculadora del
       servidor. El horario que se guarda lo deriva el servidor igual, con estos mismos datos. */
    const schedule = computed(() => scheduleStops({
      planDate: planDate.value,
      startTime: planStartTime.value,
      mode: planTransportMode.value,
      stops: draftPlaces.value
    }));

    const shortDate = (isoDate) => isoDate.slice(8, 10) + '/' + isoDate.slice(5, 7);

    /* Las paradas agrupadas por la jornada que les tocó. El último tramo de cada día se omite:
       cruza a la jornada siguiente, no es un traslado del itinerario. */
    const dayGroups = computed(() => {
      const groups = [];
      schedule.value.forEach((slot, index) => {
        const item = draftPlaces.value[index];
        const isLastOfDay =
          index === schedule.value.length - 1 ||
          schedule.value[index + 1].dayIndex !== slot.dayIndex;
        let group = groups[groups.length - 1];
        if (!group || group.dayIndex !== slot.dayIndex) {
          group = {
            dayIndex: slot.dayIndex,
            label: 'Día ' + (slot.dayIndex + 1) + ' · ' + shortDate(slot.date),
            stops: []
          };
          groups.push(group);
        }
        group.stops.push({
          key: item.key,
          index,
          item,
          order: index + 1,
          timeRange: formatClock(slot.startMinutes) + ' – ' + formatClock(slot.endMinutes),
          duration: formatDuration(slot.minutes),
          leg: isLastOfDay ? null : { minutes: slot.legMinutes }
        });
      });
      return groups;
    });

    const dayCount = computed(() => dayGroups.value.length);

    const totalDuration = computed(() =>
      formatDuration(draftPlaces.value.reduce((total, item) => total + effectiveMinutes(item), 0)));

    const nextPlanStep = () => {
      if (planNameInput.value && !planNameInput.value.reportValidity()) return;
      if (planStep.value === 1 && planDateInput.value && !planDateInput.value.reportValidity()) {
        return;
      }
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
      directionsLink(place, { class: 'text-primary fw-medium d-inline-block mt-1' }),
      // [PSHARE-004] Ver ficha: desde el popup del mapa se llega a la pagina compartible
      // /places/{id} del lugar, sin salir del listado (el href es relativo a la raiz).
      el('br'),
      el('a', {
        href: '/places/' + place.id,
        text: 'Ver ficha',
        class: 'text-primary fw-medium d-inline-block mt-1'
      })
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

    /* [ITI] Borrador sobre el mapa (AC-13): al agregar lugares al wizard se dibujan
       marcadores numerados y una polyline punteada en el orden del itinerario, sin
       fitBounds (la vista la elige quien mira el mapa). Los popups se construyen con
       nodos: el nombre viene del ABM/importador, sin sanitizar (guide-js R7). El badge
       NO lleva data-category: el conteo E2E de lugares sigue atado solo a los pines. */
    const draftPopupContent = (place, slot, index, legMinutes) => el('div', {}, [
      el('strong', { text: (index + 1) + '. ' + place.name }),
      el('br'),
      'Llegada ' + formatClock(slot.startMinutes),
      el('br'),
      'Permanencia ' + formatDuration(slot.minutes),
      legMinutes === null ? null : el('br'),
      legMinutes === null ? null : 'Traslado ' + legMinutes + ' min'
    ]);

    const drawDraftOverlay = () => {
      if (!map) return;
      draftMarkers.forEach(m => map.removeLayer(m));
      draftMarkers = [];
      if (draftRouteLine) {
        map.removeLayer(draftRouteLine);
        draftRouteLine = null;
      }

      const coordinates = [];
      schedule.value.forEach((slot, index) => {
        const place = draftPlaces.value[index].place;
        if (!place.latitude || !place.longitude) return;
        coordinates.push([place.latitude, place.longitude]);

        const isLast = index === schedule.value.length - 1;
        const legMinutes = isLast ? null : slot.legMinutes;
        const badge = el('div', {
          class: 'draft-step',
          dataset: { draftStep: String(index + 1) },
          'aria-hidden': 'true'
        }, [String(index + 1)]);
        const marker = buildMarker(place, {
          badge,
          className: 'draft-marker',
          size: 26,
          popupAnchor: [0, -13]
        }).addTo(map);
        marker.bindPopup(draftPopupContent(place, slot, index, legMinutes), { autoPan: false });
        draftMarkers.push(marker);
      });

      if (coordinates.length > 1) {
        // Mismas opciones de la polyline punteada del detalle del plan (plans-detail.js).
        draftRouteLine = L.polyline(coordinates, {
          color: '#0d6efd',
          weight: 3,
          opacity: 0.8,
          dashArray: '6, 6'
        }).addTo(map);
      }
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

      // [APL] Planes propios para el picker del panel de detalle. El gate de visibilidad
      // es isLogged (server-side), no este fetch: sin sesión o sin planes la lista queda
      // vacía y el panel ofrece crear el primer plan; un fallo la deja igual.
      try {
        await loadPlanOptions();
      } catch (error) {
        console.error('Error fetching plan options:', error);
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
      // [ITI] Dibujo inicial del overlay del borrador (vacío al arrancar: es un no-op).
      drawDraftOverlay();

    });

    watch(filteredPlaces, () => {
      updateMarkers();
    });

    // [ITI] El overlay sigue al borrador: schedule se recalcula con cada cambio de
    // paradas, fecha, hora de inicio, modo o minutos fijos, así que un solo watch
    // cubre los cinco (y repinta los horarios de los popups al arrancar el día más tarde).
    watch(schedule, () => {
      drawDraftOverlay();
    });

    return {
      isLogged,
      places,
      error,
      selectedPlace,
      selectedCategory,
      searchQuery,
      categories,
      filteredPlaces,
      capitalize,
      formatDate,
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
      planDateInput,
      openCreatePlan,
      closeCreatePlan,
      planStep,
      draftPlaces,
      planDate,
      planStartTime,
      planTransportMode,
      transportLabels: TRANSPORT_LABELS,
      schedule,
      dayGroups,
      dayCount,
      totalDuration,
      activePlan,
      closeActivePlan,
      addToDraft,
      removeDraft,
      moveDraft,
      nextPlanStep,
      onPlanSubmit,
      availablePlans,
      planContainsPlace,
      planPickId,
      planBusy,
      planMessage,
      planError,
      addSelectedToPlan,
      addSelectedToDraft
    };
  }
}).mount('#explore-app');
