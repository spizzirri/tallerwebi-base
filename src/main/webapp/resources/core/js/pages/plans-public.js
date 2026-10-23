/**
 * The public plan view: one Leaflet map with the itinerary and its route. Extracted from
 * WEB-INF/templates/pages/plans/public.html (docs/guide-js.md R4).
 *
 * The stops come from the page as a seed object, because only Thymeleaf can serialize the
 * model. Nothing here fetches: a shared plan is read-only.
 */

import { el } from '../lib/dom.js';
import { buildMarker, directionsLink } from '../lib/places-map.js';

const stops = (window.PUBLIC_PLAN_SEED || {}).stops || [];

function renderMap() {
  const container = document.getElementById('plan-map');
  if (!container || stops.length === 0) {
    return;
  }

  const map = L.map('plan-map');
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap'
  }).addTo(map);

  const bounds = [];
  stops.forEach(function (stop, index) {
    if (!stop.latitude || !stop.longitude) {
      return;
    }
    // Mismo marcador que pages/plans/detail.html: circulo por categoria (cat-*), numerado
    // por parada y con el borde amarillo de "en el itinerario" (todas las paradas lo son
    // aca). El numero va en un nodo, no en un string con el nombre.
    const badge = el(
      'div',
      { class: 'custom-map-icon cat-' + (stop.category || 'DEFAULT') + ' in-itinerary' },
      [el('span', { text: String(index + 1) })]
    );
    // El popup se arma con DOM, no con string concatenado: un nombre de lugar con
    // "<script>" tiene que renderizarse como texto.
    const popup = el('div', {}, [
      el('strong', { text: stop.name }),
      ...(stop.category ? [el('br'), el('small', { text: stop.category })] : []),
      // [MAPS] Como llegar desde el popup, igual que en el detalle y en explore. Usa la
      // direccion de texto (no la coordenada) y el nombre delante para desambiguar.
      el('br'),
      directionsLink(stop, { class: 'text-primary fw-medium d-inline-block mt-1' })
    ]);
    buildMarker(stop, { badge }).addTo(map).bindPopup(popup);
    bounds.push([stop.latitude, stop.longitude]);
  });

  // Linea que une las paradas en orden, con las mismas opciones que el detalle. Como alla,
  // solo con 2+ paradas: una sola parada no tiene trayecto que dibujar.
  if (stops.length > 1) {
    const routeLatLngs = [];
    stops.forEach(function (stop) {
      if (stop.latitude && stop.longitude) {
        routeLatLngs.push([stop.latitude, stop.longitude]);
      }
    });
    L.polyline(routeLatLngs, {
      color: '#0d6efd',
      weight: 3,
      opacity: 0.8,
      dashArray: '6, 6'
    }).addTo(map);
  }

  if (bounds.length === 1) {
    map.setView(bounds[0], 14);
  } else if (bounds.length > 1) {
    map.fitBounds(bounds, { padding: [30, 30] });
  } else {
    // Ningun lugar trae coordenadas: el mapa queda en un default y la lista de abajo
    // sigue siendo la fuente de verdad del itinerario.
    map.setView([-34.59, -58.40], 12);
  }
}

renderMap();
