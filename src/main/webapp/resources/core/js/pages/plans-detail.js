/**
 * The plan detail view: map, itinerary and catalogue, extracted from
 * WEB-INF/templates/pages/plans/detail.html (docs/guide-js.md R4).
 *
 * The page hands this module four values through `window.PLAN_DETAIL_SEED` (see the
 * bootstrap script in the template): a module cannot read Thymeleaf, and the two lists are
 * object graphs that a data-* attribute cannot carry without a serializer.
 */
import { apiFetch } from '../lib/api.js';
import { el } from '../lib/dom.js';
import { formatClock, formatDate, formatDuration } from '../lib/plan-schedule.js';
import {
    buildMarker,
    categoryIconHtml,
    categoryIconNode,
    directionsLink,
    directionsUrl
} from '../lib/places-map.js';

const { createApp, ref, computed, onMounted, nextTick } = Vue;
const seed = window.PLAN_DETAIL_SEED || {};
const itineraryUrl = seed.itineraryUrl || '';
const catalogUrl = seed.catalogUrl || '';

/** An itinerary entry and a catalogue place arrive in different shapes; both end here. */
const parsePlace = (p) => {
    if (!p) return null;
    const target = p.place ? p.place : p;
    return {
        entryId: p.entryId || null,
        description: p.description,
        visitDate: p.visitDate,
        visitTime: p.visitTime,
        // [SCH] El detalle dibuja el rango con la duración y marca con el pin lo que el usuario
        // fijó a mano. El catálogo (/api/places) manda un Place y no trae ninguno de los tres:
        // quedan neutros para que una parada del catálogo no herede un horario que no tiene.
        durationMinutes: p.durationMinutes ?? null,
        transportModeToNext: p.transportModeToNext || '',
        timePinned: p.timePinned === true,
        id: target.id,
        name: target.name || 'Sin nombre',
        category: target.category || 'DEFAULT',
        // [MAPS] La direccion viaja en el modelo para armar el "Como llegar" de la ficha,
        // el popup y la lista del itinerario (el dataset trae direccion y coordenada
        // desfasadas, asi que el link usa el texto).
        address: target.address,
        latitude: target.latitude || target.lat,
        longitude: target.longitude || target.lng
    };
};

createApp({
    setup() {
        const allPlaces = ref((seed.allPlaces || []).map(parsePlace).filter(p => p && p.id));
        const planPlaces = ref((seed.itineraryPlaces || []).map(parsePlace).filter(p => p && p.id));
        // [SCH] Fecha de la jornada inicial: una parada que cae en ella no repite la fecha en la
        // lista, y las que se corren a otro día sí la muestran.
        const planDate = ref(seed.planDate || '');

        const selectedPlaceId = ref(null);
        const selectedEntry = ref(null);
        const requestError = ref('');
        const saving = ref(false);
        const placeDialog = ref(null);
        const planOptionsOpen = ref(false);
        const planOptionsButton = ref(null);
        const closePlanOptions = (event) => {
            if (!event.currentTarget.contains(event.relatedTarget)) {
                planOptionsOpen.value = false;
            }
        };

        // Leaflet owns these three and the template never reads them, so they are not refs.
        let map = null;
        let markers = [];
        let routeLine = null;
        let draggedIndex = null;

        const availablePlaces = computed(() => {
            const addedIds = planPlaces.value.map(p => p.id);
            return allPlaces.value.filter(p => !addedIds.includes(p.id));
        });

        const itineraryRequest = async (path = '', options = {}) => {
            try {
                const response = await apiFetch(itineraryUrl + path, options);
                const text = await response.text();
                return text ? JSON.parse(text) : null;
            } catch (error) {
                // The session-expiry sentence is the only one worth passing through;
                // this page owns its copy for everything else.
                if (error.status === 401) throw error;
                console.error(error);
                throw new Error('No se pudo actualizar o consultar el lugar. Intenta nuevamente.');
            }
        };

        /* El servidor recalcula el plan entero después de cada cambio de horario, de un reorden o
           de una baja: copiar solo los cuatro campos del horario sobre las paradas ya presentes
           evita reemplazar el array y perder el orden optimista de la UI. */
        const refreshSchedule = async () => {
            const entries = await itineraryRequest();
            entries.forEach((entry) => {
                const item = planPlaces.value.find(p => p.entryId === entry.id);
                if (!item) return;
                item.visitDate = entry.visitDate;
                item.visitTime = entry.visitTime;
                item.durationMinutes = entry.durationMinutes;
                item.timePinned = entry.timePinned;
            });
        };

        /** `dd/mm` para el encabezado del día, igual que el creador. */
        const shortDate = (isoDate) => isoDate.slice(8, 10) + '/' + isoDate.slice(5, 7);

        /** Minutos del día de un `HH:mm`; `null` cuando el valor no es una hora. */
        const clockMinutes = (value) => {
            if (!value) return null;
            const [hours, minutes] = value.split(':').map(Number);
            return hours * 60 + minutes;
        };

        /** Minutos del día en que termina la visita; `null` sin hora o sin duración. */
        const endMinutes = (entry) => {
            const start = clockMinutes(entry.visitTime);
            if (start === null || !entry.durationMinutes) return null;
            return start + entry.durationMinutes;
        };

        /* Lo que la parada muestra de horario: rango, duración y fecha, ya formateados. La ficha
           del lugar y la lista usan la misma cuenta, así que la comparten. */
        const describeStop = (item) => {
            const start = clockMinutes(item.visitTime);
            const end = endMinutes(item);
            return {
                // Sin duración no hay fin que calcular, pero la hora de inicio sí existe:
                // mostrarla vale más que declarar el horario "por definir".
                timeRange: start === null
                    ? ''
                    : end !== null
                        ? formatClock(start) + ' – ' + formatClock(end)
                        : formatClock(start),
                durationLabel: item.durationMinutes ? formatDuration(item.durationMinutes) : '',
                dateLabel: item.visitDate && item.visitDate !== planDate.value
                    ? formatDate(item.visitDate)
                    : ''
            };
        };

        /* Las paradas agrupadas por la fecha que les tocó, en el orden del itinerario. La
           numeración de cada parada es global (la misma que los marcadores del mapa), por eso el
           índice viaja desde el array completo y no desde el grupo. */
        const dayGroups = computed(() => {
            const groups = [];
            planPlaces.value.forEach((item, index) => {
                const date = item.visitDate || null;
                let group = groups[groups.length - 1];
                if (!group || group.date !== date) {
                    group = {
                        // La posición desambigua: un pin puede volver a una fecha ya usada un
                        // grupo antes (1 → 2 → 1) y Vue necesita claves únicas dentro del v-for.
                        key: 'day-' + groups.length + (date || ''),
                        date,
                        label: 'Día ' + (groups.length + 1) + (date ? ' · ' + shortDate(date) : ''),
                        stops: []
                    };
                    groups.push(group);
                }
                group.stops.push({ index, item, ...describeStop(item) });
            });
            return groups;
        });

        const dayCount = computed(() => dayGroups.value.length);

        const getCategoryLabel = (category) => {
            const labels = { RESTAURANT: 'Restaurante', BAR: 'Bar', CAFE: 'Café', MUSEUM: 'Museo', PARK: 'Parque', SHOPPING: 'Compras', NIGHTLIFE: 'Vida nocturna', CULTURE: 'Cultura', SPORT: 'Deporte', OTHER: 'Otro' };
            return labels[category] || 'Lugar';
        };

        /* A Lucide icon as markup for the template's v-html slots. Trusted: our own SVG.
           The template names icons the way HTML does ("map-pinned", "trash-2"), while
           `lucide.icons` keys are PascalCase: normalise before the lookup, or Lucide builds
           an empty <svg> and the icon is silently blank. */
        const iconHtml = (name, size = 16) => {
            const key = name.replace(/(?:^|-)([a-z0-9])/g, (_, letter) => letter.toUpperCase());
            return lucide.createElement(lucide.icons[key] || lucide.icons.MapPin, {
                width: size,
                height: size,
                'aria-hidden': 'true'
            }).outerHTML;
        };

        const renderMarkers = (focusPlaceId = null) => {
            markers.forEach(m => map.removeLayer(m));
            markers = [];
            if (routeLine) {
                map.removeLayer(routeLine);
                routeLine = null;
            }

            const isSinglePlace = planPlaces.value.length === 1;
            const routeLatLngs = [];
            const bounds = [];

            if (allPlaces.value.length > 0) {
                allPlaces.value.forEach((place) => {
                    if (place.latitude && place.longitude) {
                        const itineraryIndex = planPlaces.value.findIndex(p => p.id === place.id);
                        const isInPlan = itineraryIndex !== -1;

                        const categoryClass = 'cat-' + place.category;
                        const isNumbered = isInPlan && !isSinglePlace;

                        let activeClass = '';
                        if (isInPlan) {
                            activeClass = isSinglePlace ? 'single-place-pulse' : 'in-itinerary';
                        }

                        const badge = el(
                            'div',
                            { class: 'custom-map-icon ' + categoryClass + ' ' + activeClass },
                            [isNumbered
                                ? el('span', { text: String(itineraryIndex + 1) })
                                : categoryIconNode(place.category, 15)]
                        );

                        /* Leaflet parses a String popup as HTML, and both the name and the
                           category come from the catalogue: the popup is a node, never markup.
                           R7, and the reason lib/dom.js exists. */
                        let head;
                        if (isInPlan && isSinglePlace) {
                            head = [
                                el('b', { text: '📍 Único lugar seleccionado:' }),
                                el('br'),
                                el('strong', { text: place.name })
                            ];
                        } else if (isInPlan) {
                            head = [
                                el('b', { text: 'Paso ' + (itineraryIndex + 1) + ': ' + place.name }),
                                el('br'),
                                el('small', { class: 'text-success', text: '✔ En el itinerario' })
                            ];
                        } else {
                            head = [el('b', { text: place.name }), el('br'),
                                el('small', { text: place.category })];
                        }

                        const popup = el('div', {}, [
                            ...head,
                            // [MAPS] Como llegar desde el propio popup del mapa: usa la
                            // direccion de texto (no la coordenada, que el dataset trae
                            // desfasada), con el nombre delante para desambiguar; mismo
                            // criterio que el resto.
                            el('br'),
                            directionsLink(place, { class: 'text-primary underline-offset-4 text-decoration-none fw-medium' })
                        ]);

                        const marker = buildMarker(place, { badge })
                            .addTo(map)
                            .bindPopup(popup);

                        markers.push(marker);
                        bounds.push([place.latitude, place.longitude]);

                        if ((focusPlaceId && place.id === focusPlaceId) || (isSinglePlace && isInPlan && focusPlaceId)) {
                            map.flyTo([place.latitude, place.longitude], 13, { duration: 1.0 });
                            setTimeout(() => marker.openPopup(), 1000);
                        }
                    }
                });
            }

            if (planPlaces.value.length > 1) {
                planPlaces.value.forEach(place => {
                    if (place.latitude && place.longitude) {
                        routeLatLngs.push([place.latitude, place.longitude]);
                    }
                });

                routeLine = L.polyline(routeLatLngs, {
                    color: '#0d6efd',
                    weight: 3,
                    opacity: 0.8,
                    dashArray: '6, 6'
                }).addTo(map);
            }

            if (!focusPlaceId && bounds.length > 0) {
                map.fitBounds(bounds, { padding: [60, 60] });
            }
        };

        const initMap = () => {
            map = L.map('map').setView([-34.59, -58.40], 12);

            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; OpenStreetMap'
            }).addTo(map);

            renderMarkers();
        };

        const showPlace = async (item) => {
            requestError.value = '';
            try {
                const entries = await itineraryRequest();
                const entry = entries.find(candidate => candidate.id === item.entryId);
                if (!entry) throw new Error('Este lugar ya no pertenece al plan. Actualiza la página.');
                // La ficha muestra el horario ya formateado: el rango, la duración y el día se
                // calculan acá, igual que en la lista, y no en el template.
                selectedEntry.value = { ...entry, ...describeStop(entry) };
                // [MERGE] El spread de describeStop (feature) y el nextTick de main (DIS-B04) no
                // se pisan: el nextTick va después de montar selectedEntry, que es lo que el
                // <template v-if> necesita para renderizar antes de showModal.
                await nextTick();
                placeDialog.value.showModal();
            } catch (error) { requestError.value = error.message; }
        };

        /* El PUT reemplaza los cuatro campos del horario, así que el payload viaja completo: el
           campo editado y los otros tres tal como están. Los dos de fecha y hora solo se repiten
           si la parada está fijada a mano o si se está editando justamente uno de ellos; si no,
           van en null para que cambiar solo la duración no convierta una parada derivada en una
           fijada. */
        const updateEntry = async (entry, field, value) => {
            if (saving.value || !entry) return;
            saving.value = true;
            requestError.value = '';
            const editsSchedule = field === 'visitDate' || field === 'visitTime';
            const keepsSchedule = entry.timePinned || editsSchedule;
            const payload = {
                durationMinutes: field === 'durationMinutes'
                    ? (value === '' ? null : Number(value))
                    : (entry.durationMinutes ?? null),
                transportModeToNext: entry.transportModeToNext || null,
                visitDate: field === 'visitDate'
                    ? (value || null)
                    : (keepsSchedule ? (entry.visitDate || null) : null),
                visitTime: field === 'visitTime'
                    ? (value || null)
                    : (keepsSchedule ? (entry.visitTime || null) : null)
            };
            try {
                const updated = await itineraryRequest('/' + entry.id, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                Object.assign(entry, updated);
                // Fijar una parada corre las siguientes: sin este refresco la lista muestra las
                // horas viejas hasta recargar la página.
                await refreshSchedule();
                Object.assign(entry, describeStop(entry));
            } catch (error) { requestError.value = error.message; }
            finally { saving.value = false; }
        };

        const addPlace = async () => {
            if (saving.value) return;
            saving.value = true;
            requestError.value = '';
            const placeObj = allPlaces.value.find(p => p.id === selectedPlaceId.value);
            try {
                if (placeObj) {
                    const entry = await itineraryRequest('?placeId=' + placeObj.id, { method: 'POST' });
                    const addedId = placeObj.id;
                    planPlaces.value.push({
                        ...placeObj,
                        entryId: entry.id,
                        description: entry.description,
                        visitDate: entry.visitDate,
                        visitTime: entry.visitTime,
                        durationMinutes: entry.durationMinutes,
                        timePinned: entry.timePinned,
                        transportModeToNext: entry.transportModeToNext || ''
                    });
                    selectedPlaceId.value = null;
                    renderMarkers(addedId);
                }
            } catch (error) {
                requestError.value = error.message;
            } finally {
                saving.value = false;
            }
        };

        const removePlace = async (index) => {
            if (saving.value) return;
            saving.value = true;
            requestError.value = '';
            try {
                await itineraryRequest('/' + planPlaces.value[index].entryId, { method: 'DELETE' });
                planPlaces.value.splice(index, 1);
                renderMarkers();
                // El servidor recalcula el plan sin la parada borrada: las que quedan cambian de
                // hora y el refresco las trae.
                await refreshSchedule();
            } catch (error) { requestError.value = error.message; }
            finally { saving.value = false; }
        };

        const onDragStart = (index) => {
            draggedIndex = index;
        };

        const onDrop = async (dropIndex) => {
            if (saving.value) return;
            if (draggedIndex !== null && draggedIndex !== dropIndex) {
                saving.value = true;
                requestError.value = '';
                const previous = [...planPlaces.value];
                const movedItem = planPlaces.value.splice(draggedIndex, 1)[0];
                planPlaces.value.splice(dropIndex, 0, movedItem);
                draggedIndex = null;
                renderMarkers();
                let reordered = false;
                try {
                    await itineraryRequest('/reorder', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(planPlaces.value.map(item => item.entryId)) });
                    reordered = true;
                } catch (error) { planPlaces.value = previous; renderMarkers(); requestError.value = error.message; }
                finally { saving.value = false; }
                // [SCH] Reordenar vuelve a derivar el horario: el refresco trae las horas nuevas y
                // deja intacto el orden optimista que ya está pintado. Va fuera del try del
                // reorder para que un fallo acá no revierta un orden que el servidor sí guardó.
                if (reordered) {
                    try { await refreshSchedule(); } catch (error) { requestError.value = error.message; }
                }
            }
        };

        onMounted(async () => {
            try {
                const response = await apiFetch(catalogUrl);
                allPlaces.value = await response.json();
            } catch (error) {
                allPlaces.value = [];
                requestError.value = error.status === 401
                    ? error.message
                    : 'No se pudo cargar el listado de lugares.';
            }
            initMap();
        });

        return {
            allPlaces, planPlaces, selectedPlaceId, selectedEntry, requestError, saving,
            // El menú "Más opciones" usa estos controles desde la plantilla.
            planOptionsOpen, planOptionsButton, closePlanOptions,
            placeDialog, availablePlaces, dayGroups, dayCount, directionsUrl,
            getCategoryLabel, iconHtml, categoryIconHtml, showPlace, updateEntry, addPlace,
            removePlace, onDragStart, onDrop
        };
    }
}).mount('#plan-app');
