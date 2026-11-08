/**
 * Alta de plan del ABM admin: los tres pasos del alta de usuario (datos, paradas y privacidad y
 * permisos), sin Vue. El wizard solo muestra y oculta pasos y arma la lista de paradas con el
 * helper `el`; el horario lo calcula el servidor con el mismo camino que el alta de usuario
 * (PlanController.toPlan y PlanScheduler), asi que el form manda los campos tal cual.
 *
 * El picker de lugares (pages/plan-places-picker.js) habla con este wizard por dos eventos en
 * window: plan-places:add trae el lugar elegido y plan-places:changed devuelve los ids del draft.
 */
import { el } from '../lib/dom.js';

const EVENT_ADD = 'plan-places:add';
const EVENT_CHANGED = 'plan-places:changed';
const STEP_NAMES = ['Datos del plan', 'Lugares', 'Privacidad y permisos'];
const LAST_STEP = 3;
const MSG_NO_PLACES = 'Agregue al menos un lugar desde el mapa antes de continuar.';
const INPUT_CLASS = 'w-full rounded-md border border-gray-300 bg-white px-2 py-1.5 text-xs';
const ICON_CLASS =
  'rounded p-1 text-gray-600 hover:bg-gray-200 disabled:opacity-30 disabled:hover:bg-transparent';

const form = document.getElementById('plan-create-form');
if (form) {
  setupWizard(form);
}

function setupWizard(root) {
  const steps = [...root.querySelectorAll('[data-step]')];
  const bars = [...root.querySelectorAll('[data-progress]')];
  const label = document.getElementById('plan-step-label');
  const list = document.getElementById('plan-draft-list');
  const emptyHint = document.getElementById('plan-draft-empty');
  const countText = document.getElementById('plan-draft-count');
  const hint = document.getElementById('plan-draft-hint');
  const transportOptions = document.getElementById('plan-transport-options');
  const prev = document.getElementById('plan-prev');
  const next = document.getElementById('plan-next');
  const submit = document.getElementById('plan-submit');

  /* Cada item: el lugar del catalogo y los campos de la fila, con los names del backend. */
  let draft = [];
  let current = 1;

  const hasPlaces = () => draft.length > 0;

  const showHint = (message) => {
    hint.textContent = message;
    hint.hidden = false;
  };

  const showStep = (number) => {
    current = number;
    steps.forEach((section) => {
      section.hidden = Number(section.dataset.step) !== number;
    });
    bars.forEach((bar) => {
      const filled = Number(bar.dataset.progress) <= number;
      bar.classList.toggle('bg-blue-600', filled);
      bar.classList.toggle('bg-gray-200', !filled);
    });
    label.textContent = 'Paso ' + number + ' de ' + LAST_STEP + ': ' + STEP_NAMES[number - 1];
    prev.hidden = number === 1;
    next.hidden = number === LAST_STEP;
    submit.hidden = number !== LAST_STEP;
    hint.hidden = true;
  };

  /* Lo que cambia la lista (alta, baja, orden) se dibuja de nuevo y se avisa al picker. */
  const sync = () => {
    renderDraft();
    window.dispatchEvent(
      new CustomEvent(EVENT_CHANGED, { detail: draft.map((item) => item.place.id) })
    );
  };

  const renderDraft = () => {
    list.replaceChildren(...draft.map((item, index) => stopNode(item, index)));
    emptyHint.hidden = hasPlaces();
    countText.hidden = !hasPlaces();
    countText.textContent =
      draft.length + (draft.length === 1 ? ' parada' : ' paradas');
    submit.disabled = !hasPlaces();
  };

  const iconButton = (name, glyph, disabled, onClick, extraClass = '') => {
    const button = el('button', {
      type: 'button',
      'aria-label': name,
      class: ICON_CLASS + ' ' + extraClass,
      text: glyph,
      disabled: disabled ? '' : null
    });
    button.addEventListener('click', onClick);
    return button;
  };

  const move = (index, offset) => {
    const target = index + offset;
    [draft[index], draft[target]] = [draft[target], draft[index]];
    sync();
  };

  const remove = (index) => {
    draft.splice(index, 1);
    sync();
  };

  /* Nota, traslado y minutos: cada input escribe en el item, no en el DOM, asi el re-render no
     pierde lo tipeado. */
  const stopNode = (item, index) => {
    const prefix = 'places[' + index + '].';
    const description = el('input', {
      type: 'text',
      name: prefix + 'description',
      placeholder: 'Nota (opcional)',
      'aria-label': 'Nota',
      class: INPUT_CLASS,
      value: item.description
    });
    description.addEventListener('input', () => {
      item.description = description.value;
    });

    const mode = el('select', {
      name: prefix + 'transportModeToNext',
      'aria-label': 'Traslado hacia la parada siguiente',
      class: INPUT_CLASS
    });
    mode.append(transportOptions.content.cloneNode(true));
    mode.value = item.transportModeToNext;
    mode.addEventListener('change', () => {
      item.transportModeToNext = mode.value;
    });

    const travel = el('input', {
      type: 'number',
      min: '1',
      name: prefix + 'travelMinutesToNext',
      placeholder: 'Automático',
      'aria-label': 'Minutos de traslado a la parada siguiente',
      class: INPUT_CLASS + ' w-24',
      value: item.travelMinutesToNext
    });
    travel.addEventListener('input', () => {
      item.travelMinutesToNext = travel.value;
    });

    const place = item.place;
    const details = [place.category, place.address].filter(Boolean).join(' · ');
    return el('li', {}, [
      el('div', { class: 'rounded-md border border-gray-200 bg-gray-50 p-3' }, [
        el('input', { type: 'hidden', name: prefix + 'placeId', value: place.id }),
        el('div', { class: 'flex items-center gap-2' }, [
          el('span', {
            class:
              'flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-gray-900 text-xs font-medium text-white',
            text: String(index + 1)
          }),
          el('span', {
            class: 'min-w-0 flex-1 truncate text-sm font-medium text-gray-900',
            text: place.name
          }),
          iconButton('Mover arriba', '\u2191', index === 0, () => move(index, -1)),
          iconButton(
            'Mover abajo',
            '\u2193',
            index === draft.length - 1,
            () => move(index, 1)
          ),
          iconButton('Quitar lugar', '\u00d7', false, () => remove(index), 'text-red-600 hover:bg-red-100')
        ]),
        el('p', { class: 'mt-1 text-xs text-gray-600', text: details }),
        el('div', { class: 'mt-2' }, [description])
      ]),
      el('div', { class: 'mt-1 flex items-center gap-2 pl-8 text-xs text-gray-500' }, [mode, travel])
    ]);
  };

  /* El picker manda el lugar elegido. Un lugar repetido no suma otra parada. */
  window.addEventListener(EVENT_ADD, (event) => {
    const place = event.detail;
    if (draft.some((item) => item.place.id === place.id)) return;
    draft.push({ place, description: '', transportModeToNext: '', travelMinutesToNext: '' });
    sync();
  });

  next.addEventListener('click', () => {
    if (current === 1) {
      // reportValidity muestra el primer campo invalido del paso y corta el avance.
      const valid = [...steps[0].querySelectorAll('input, select, textarea')].every((field) =>
        field.reportValidity()
      );
      if (!valid) return;
    }
    if (current === 2 && !hasPlaces()) {
      showHint(MSG_NO_PLACES);
      return;
    }
    showStep(current + 1);
  });

  prev.addEventListener('click', () => showStep(current - 1));

  // Solo el boton del paso 3 crea el plan: un Enter en otro paso (buscador, campos de parada) no sale.
  // Sin lugares no hay plan: el submit no sale aunque el boton quede habilitado por un bug.
  root.addEventListener('submit', (event) => {
    if (current === LAST_STEP && hasPlaces()) return;
    event.preventDefault();
    if (current !== LAST_STEP) return;
    showStep(2);
    showHint(MSG_NO_PLACES);
  });

  renderDraft();
  showStep(1);
}
