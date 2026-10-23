/**
 * How a place is drawn on a Leaflet map: the category icon, the coloured badge and the
 * marker that carries them. Extracted from the three views that were building the same
 * `divIcon` by hand (docs/guide-js.md, phase 5).
 *
 * The category-to-icon table used to exist twice, once with Font Awesome class names in
 * `plans-detail.js` and once with Lucide names in `places-list.js`: one icon language, one
 * table. Leaflet and Lucide are vendored classic scripts, so `L` and `lucide` are globals
 * here, exactly as they are in the pages.
 */

import { el } from './dom.js';

const CATEGORY_ICONS = {
  RESTAURANT: 'Utensils',
  NIGHTLIFE: 'Music',
  MUSEUM: 'Landmark',
  SHOPPING: 'ShoppingBag',
  CAFE: 'Coffee',
  PARK: 'Trees',
  CULTURE: 'BookOpen',
  BAR: 'Wine',
  SPORT: 'Trophy',
  OTHER: 'MapPin'
};

/** The Lucide name for a category; unknown categories fall back to the generic pin. */
export const categoryIconName = (category) => CATEGORY_ICONS[category] || CATEGORY_ICONS.OTHER;

/**
 * The category icon as an SVG element, for a marker badge. Lucide draws with
 * `stroke="currentColor"`, so the badge's colour is what paints it.
 */
export const categoryIconNode = (category, size = 16) =>
  lucide.createElement(lucide.icons[categoryIconName(category)], {
    width: size,
    height: size,
    'aria-hidden': 'true'
  });

/** The same icon as markup, for a Vue `v-html` slot. Trusted: it is our own SVG. */
export const categoryIconHtml = (category, size = 16) =>
  categoryIconNode(category, size).outerHTML;

/**
 * A Google Maps directions URL built from the place's ADDRESS text, not its coordinate:
 * the imported dataset carries the two out of step, and the coordinate sends drivers to
 * the wrong street. The name goes in front so vague addresses ("Palermo") resolve.
 */
export const directionsUrl = (place) =>
  'https://www.google.com/maps/dir/?api=1&destination=' +
  encodeURIComponent(((place.name || '') + ' ' + (place.address || '')).trim());

/** The "Cómo llegar" link the three popups share. `props` overrides the classes. */
export const directionsLink = (place, props = {}) =>
  el('a', {
    href: directionsUrl(place),
    target: '_blank',
    rel: 'noopener',
    text: 'Cómo llegar',
    ...props
  });

/**
 * A marker at the place's coordinates, carrying `options.badge` as its icon, NOT added to
 * a map: the caller chains `.addTo(map).bindPopup(popup)` so it stays obvious which map
 * and which popup it belongs to.
 *
 * `badge` is an Element (Leaflet 1.9 appends it). `size` drives both the icon box and the
 * anchor, which is always the badge's centre. `popupAnchor` is only passed when a view
 * needs the popup to clear the badge.
 */
export const buildMarker = (place, options = {}) => {
  const { badge, className = 'custom-div-icon', size = 30, popupAnchor } = options;
  const icon = L.divIcon({
    className,
    html: badge,
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2],
    ...(popupAnchor ? { popupAnchor } : {})
  });
  return L.marker([place.latitude, place.longitude], { icon });
};
