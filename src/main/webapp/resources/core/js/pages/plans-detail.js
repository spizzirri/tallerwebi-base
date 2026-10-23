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
import {
    buildMarker,
    categoryIconHtml,
    categoryIconNode,
    directionsLink,
    directionsUrl
} from '../lib/places-map.js';

const { createApp, ref, computed, onMounted } = Vue;
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

        const selectedPlaceId = ref(null);
        const selectedEntry = ref(null);
        const requestError = ref('');
        const saving = ref(false);
        const placeDialog = ref(null);

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

        const formatVisitDate = (value) => {
            const [year, month, day] = value.split('-');
            return `${day}/${month}/${year}`;
        };

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
                selectedEntry.value = entries.find(entry => entry.id === item.entryId);
                if (!selectedEntry.value) throw new Error('Este lugar ya no pertenece al plan. Actualiza la página.');
                placeDialog.value.showModal();
            } catch (error) { requestError.value = error.message; }
        };

        const updateDateTime = async (entry, field, value) => {
            if (saving.value || !entry) return;
            saving.value = true;
            requestError.value = '';
            // El PUT reemplaza fecha y hora juntas, así que se manda el par completo: el campo
            // cambiado y el otro tal como estaba (si no, cambiar uno borra el otro).
            const payload = {
                visitDate: field === 'visitDate' ? (value || null) : (entry.visitDate || null),
                visitTime: field === 'visitTime' ? (value || null) : (entry.visitTime || null)
            };
            try {
                const updated = await itineraryRequest('/' + entry.id, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                entry.visitDate = updated.visitDate;
                entry.visitTime = updated.visitTime;
                const item = planPlaces.value.find(p => p.entryId === entry.id);
                if (item) {
                    item.visitDate = updated.visitDate;
                    item.visitTime = updated.visitTime;
                }
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
                    planPlaces.value.push({ ...placeObj, entryId: entry.id, description: entry.description, visitDate: entry.visitDate, visitTime: entry.visitTime });
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
                try {
                    await itineraryRequest('/reorder', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(planPlaces.value.map(item => item.entryId)) });
                } catch (error) { planPlaces.value = previous; renderMarkers(); requestError.value = error.message; }
                finally { saving.value = false; }
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
            placeDialog, availablePlaces, formatVisitDate, directionsUrl,
            getCategoryLabel, iconHtml, categoryIconHtml, showPlace, updateDateTime, addPlace,
            removePlace, onDragStart, onDrop
        };
    }
}).mount('#plan-app');
