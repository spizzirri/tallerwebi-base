/**
 * La ficha publica de un lugar (pages/places/detail.html): un mapa con el marker del lugar
 * y su popup. Extraido del template (docs/guide-js.md R4). El lugar llega como seed que
 * serializa Thymeleaf, porque solo Thymeleaf puede leer el modelo; nada aca fetcha, la
 * pagina es read-only.
 */

import { categoryColor } from '../lib/category-colors.js';
import { el } from '../lib/dom.js';
import { buildMarker, categoryIconNode } from '../lib/places-map.js';

const place = window.PLACE_DETAIL_SEED || {};

function renderMap() {
  const container = document.getElementById('place-map');
  // Sin coordenadas no hay mapa: el template ni siquiera pinta el contenedor.
  if (!container || place.latitude == null || place.longitude == null) {
    return;
  }

  const map = L.map(container).setView([place.latitude, place.longitude], 15);
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap'
  }).addTo(map);

  // Mismo badge que el mapa de /explore: circulo con el color de categoria (leido de los
  // tokens de marca, sin hex literal) y el icono Lucide pintado por stroke="currentColor".
  const badge = el(
    'div',
    {
      dataset: { category: place.category },
      style:
        'background-color:' +
        categoryColor(place.category) +
        '; color:#fff; width:32px; height:32px; border-radius:50%; border:3px solid #fff;' +
        ' box-shadow:0 2px 6px rgba(0,0,0,0.3); display:flex; align-items:center; justify-content:center;'
    },
    [categoryIconNode(place.category, 16)]
  );

  // El popup se arma con nodos, nunca con string concatenado: el nombre viene del ABM y
  // del importador, sin sanitizar (guide-js R7).
  const popup = el('div', {}, [
    el('strong', { text: place.name }),
    ...(place.category ? [el('br'), el('small', { text: place.category })] : [])
  ]);

  buildMarker(place, { badge, className: 'custom-marker', size: 32, popupAnchor: [0, -16] })
    .addTo(map)
    .bindPopup(popup);
}

renderMap();
