/**
 * Preview del itinerario para el creador de planes: espeja `PlanScheduleCalculator` (Java) para
 * mostrar el horario mientras se arma el plan. Es solo preview: el horario que se guarda lo
 * calcula el servidor al crear o editar, y si los dos divergen gana el servidor.
 *
 * El cálculo es aritmética de reloj sobre minutos del día más un índice de jornada, nunca sobre
 * objetos `Date`: un `Date` mete zona horaria y horario de verano en una cuenta que no los tiene.
 */

export const CATEGORY_MINUTES = {
  RESTAURANT: 90,
  BAR: 60,
  CAFE: 45,
  MUSEUM: 90,
  PARK: 60,
  SHOPPING: 60,
  NIGHTLIFE: 120,
  CULTURE: 90,
  SPORT: 90,
  OTHER: 60
};

export const TRANSPORT_SPEEDS = { WALK: 4.5, BIKE: 14, BUS: 16, CAR: 22 };

export const TRANSPORT_LABELS = {
  WALK: 'A pie',
  BIKE: 'Bicicleta',
  BUS: 'Colectivo',
  CAR: 'Auto'
};

/** `dd/mm/yyyy` de una fecha ISO `YYYY-MM-DD`; vacío si no hay fecha. */
export function formatDate(isoDate) {
  if (!isoDate) return '';
  const [year, month, day] = isoDate.split('-');
  return day + '/' + month + '/' + year;
}

const DEFAULT_DURATION_MINUTES = 60;
const DEFAULT_DAY_END = '23:00';
const DEFAULT_MODE = 'WALK';
const DEFAULT_GAP_MINUTES = 15;
const MIN_GAP_MINUTES = 5;
const MAX_GAP_MINUTES = 90;
const GAP_ROUNDING_MINUTES = 5;
const ROAD_FACTOR = 1.3;
const EARTH_RADIUS_KM = 6371.0088;
const MINUTES_PER_HOUR = 60;
const MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR;
const CLOCK_PATTERN = /^(\d{1,2}):(\d{2})/;

/** Minutos del día de un `HH:mm`; `null` si el valor no es una hora. */
const minuteOfDay = (value) => {
  const match = CLOCK_PATTERN.exec(String(value || '').trim());
  return match ? Number(match[1]) * MINUTES_PER_HOUR + Number(match[2]) : null;
};

/** Duración de la visita: override, duración del lugar, default de la categoría, 60. */
export function effectiveMinutes(stop) {
  const override = stop?.durationMinutes;
  if (override > 0) return override;
  const place = stop?.place || {};
  if (place.defaultDurationMinutes > 0) return place.defaultDurationMinutes;
  return CATEGORY_MINUTES[place.category] ?? DEFAULT_DURATION_MINUTES;
}

const hasCoordinates = (stop) =>
  stop?.place?.latitude != null && stop?.place?.longitude != null;

/** Distancia en línea recta entre dos paradas, en kilómetros. */
const haversineKm = (from, to) => {
  const fromLatitude = (from.place.latitude * Math.PI) / 180;
  const toLatitude = (to.place.latitude * Math.PI) / 180;
  const deltaLatitude = toLatitude - fromLatitude;
  const deltaLongitude = ((to.place.longitude - from.place.longitude) * Math.PI) / 180;
  const sinLatitude = Math.sin(deltaLatitude / 2);
  const sinLongitude = Math.sin(deltaLongitude / 2);
  const haversine =
    sinLatitude * sinLatitude +
    Math.cos(fromLatitude) * Math.cos(toLatitude) * sinLongitude * sinLongitude;
  return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(haversine));
};

/**
 * Traslado hacia la parada siguiente. Con minutos fijados a mano en la parada de origen manda el
 * override (piso 1, sin el techo del cálculo); sin override, sin coordenadas 15, si no distancia
 * con rodeo y redondeo. Espeja el `gap()` de la calculadora del servidor.
 */
export function travelMinutes(from, to, mode) {
  const override = parseInt(from?.travelMinutesToNext, 10);
  if (Number.isFinite(override)) return Math.max(1, override);
  if (!hasCoordinates(from) || !hasCoordinates(to)) return DEFAULT_GAP_MINUTES;
  const speed = TRANSPORT_SPEEDS[mode] || TRANSPORT_SPEEDS[DEFAULT_MODE];
  const distanceKm = haversineKm(from, to) * ROAD_FACTOR;
  const minutes =
    Math.round(((distanceKm / speed) * MINUTES_PER_HOUR) / GAP_ROUNDING_MINUTES) *
    GAP_ROUNDING_MINUTES;
  return Math.max(MIN_GAP_MINUTES, Math.min(MAX_GAP_MINUTES, minutes));
}

/** `YYYY-MM-DD` de la jornada `dayIndex` después de `planDate`; aritmética UTC, sin zona. */
const dateOf = (planDate, dayIndex) => {
  const [year, month, day] = planDate.split('-').map(Number);
  const moment = new Date(Date.UTC(year, month - 1, day + dayIndex));
  return moment.toISOString().slice(0, 10);
};

/**
 * Reparte las paradas en jornadas desde `startTime`, igual que el servidor.
 *
 * @param {object} plan `{ planDate, startTime, dayEnd, mode, stops }`
 * @returns {Array<{date: string, dayIndex: number, startMinutes: number, endMinutes: number,
 *   minutes: number, legMinutes: number}>} un tramo por parada, en el orden recibido
 */
export function scheduleStops({ planDate, startTime, dayEnd, mode, stops } = {}) {
  const dayStart = minuteOfDay(startTime);
  if (!planDate || dayStart === null || !Array.isArray(stops) || stops.length === 0) return [];

  const windowEnd = minuteOfDay(dayEnd) ?? minuteOfDay(DEFAULT_DAY_END);
  const defaultMode = mode || DEFAULT_MODE;
  const schedule = [];
  let dayIndex = 0;
  let cursor = dayStart;

  stops.forEach((stop, index) => {
    const minutes = effectiveMinutes(stop);
    // Una parada que no entra en la ventana rueda una sola vez; si su duración sola ya
    // excede la ventana, desborda en vez de iterar.
    if (cursor + minutes > windowEnd) {
      dayIndex += 1;
      cursor = dayStart;
    }
    const hasNext = index + 1 < stops.length;
    const legMinutes = hasNext
      ? travelMinutes(stop, stops[index + 1], stop.transportModeToNext || defaultMode)
      : 0;
    schedule.push({
      date: dateOf(planDate, dayIndex),
      dayIndex,
      startMinutes: cursor,
      endMinutes: cursor + minutes,
      minutes,
      legMinutes
    });
    cursor += minutes + legMinutes;
  });

  return schedule;
}

/** `HH:mm` de unos minutos del día; da la vuelta como un reloj si se pasa de las 24:00. */
export function formatClock(minutesOfDay) {
  const total = ((Math.round(minutesOfDay) % MINUTES_PER_DAY) + MINUTES_PER_DAY) % MINUTES_PER_DAY;
  const hours = Math.floor(total / MINUTES_PER_HOUR);
  const minutes = total % MINUTES_PER_HOUR;
  return String(hours).padStart(2, '0') + ':' + String(minutes).padStart(2, '0');
}

/** Duración legible: `45m`, `1h 30m`, `2h`. */
export function formatDuration(minutes) {
  const total = Math.max(0, Math.round(minutes || 0));
  const hours = Math.floor(total / MINUTES_PER_HOUR);
  const rest = total % MINUTES_PER_HOUR;
  if (hours === 0) return rest + 'm';
  return rest === 0 ? hours + 'h' : hours + 'h ' + rest + 'm';
}
