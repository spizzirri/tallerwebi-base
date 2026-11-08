/**
 * Lo que comparten los dos mapas de lugares del admin: el importador del catalogo OSM
 * (pages/place-import.js) y el picker del ABM de planes (pages/plan-places-picker.js). Antes cada
 * uno tenia su copia de la misma logica de mapa, de busqueda sin acentos y de markers.
 *
 * `L` y `el` son lo que ya usan las vistas: Leaflet es un script clasico vendorizado y `el` es el
 * helper del DOM. Ningun HTML sale de aca: los popups llevan texto, no markup.
 */

import { categoryColor } from './category-colors.js';
import { el } from './dom.js';

const TILE_URL = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
const TILE_ATTRIBUTION =
  '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors';

/* Sin acentos ni mayusculas: "cafe" tiene que encontrar "Café". */
export const normalizeName = (text) =>
  (text || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase();

/* Dos cajas de bordes {south, west, north, east} se tocan. */
export const intersects = (a, b) =>
  a.south < b.north && a.north > b.south && a.west < b.east && a.east > b.west;

/* Un punto tiene coordenadas: el mapa no puede dibujar uno sin latitud y longitud. */
export const hasCoordinates = (poi) =>
  poi.latitude !== null && poi.latitude !== undefined &&
  poi.longitude !== null && poi.longitude !== undefined;

/* El panel muestra todo lo que el alta va a recibir, con un guion largo (U+2014) donde OSM no trajo
   nada: es el mismo caracter que el importador usaba antes de compartir este modulo. */
const EMPTY = '\u2014';

export const orDash = (value) => (value === null || value === undefined || value === '' ? EMPTY : value);

export const coordinatesOf = (poi) => (hasCoordinates(poi) ? poi.latitude + ', ' + poi.longitude : EMPTY);

/**
 * Mapa Leaflet con la capa base de OpenStreetMap. Cada vista decide el centro y el zoom; el
 * resto (tamaño real y redimensionado) es igual en las dos.
 */
export const createMap = (elementId, { center, zoom }) => {
  const map = L.map(elementId, { center, zoom });
  L.tileLayer(TILE_URL, { attribution: TILE_ATTRIBUTION }).addTo(map);
  /* El compilador de Tailwind corre diferido: sin esto el mapa se crea midiendo 0 y queda
     centrado fuera de lugar (la misma razon que en /explore). El invalidateSize va con pan por
     defecto: conserva el centro geografico. Tambien cubre el mapa que nace oculto (paso del
     wizard): el ResizeObserver lo redimensiona cuando se muestra. */
  new ResizeObserver(() => map.invalidateSize()).observe(map.getContainer());
  requestAnimationFrame(() => map.invalidateSize());
  return map;
};

/**
 * Redibuja los markers de categoria: quita los anteriores y dibuja uno por punto con
 * coordenadas. El color sale del mismo token que usa /explore, asi un punto se lee igual en los
 * dos mapas. Devuelve los markers nuevos, para que la vista pueda quitarlos despues.
 */
export const drawPoiMarkers = (map, previous, pois, onSelect) => {
  previous.forEach((marker) => map.removeLayer(marker));
  return pois.filter(hasCoordinates).map((poi) => {
    const color = categoryColor(poi.category);
    const marker = L.circleMarker([poi.latitude, poi.longitude], {
      radius: 9,
      color: color,
      weight: 3,
      fillColor: color,
      fillOpacity: 0.9
    }).addTo(map);
    /* Leaflet parsea un popup String como HTML y el nombre de OSM no viene sanitizado: el
       rotulo es un nodo, no markup. */
    marker.bindPopup(el('strong', { text: poi.name }));
    marker.on('click', () => onSelect(poi));
    return marker;
  });
};
