/**
 * Add to Plan functionality for the Places view.
 * Emits a custom event when the "+" button is clicked on a place card; the Vue app in
 * pages/places/list.html listens for it and handles the modal. It answers back with
 * `plan-error`, because this module has no error surface of its own.
 */
import { apiFetch } from './lib/api.js';

const reportError = (message) => {
  document.dispatchEvent(new CustomEvent('plan-error', { detail: { message } }));
};

document.addEventListener('click', (event) => {
  const button = event.target.closest('[data-add-to-plan]');
  if (!button) return;

  event.preventDefault();
  event.stopPropagation();

  const placeLatitude = button.getAttribute('data-place-latitude');
  const placeLongitude = button.getAttribute('data-place-longitude');

  document.dispatchEvent(new CustomEvent('add-to-plan', {
    detail: {
      id: button.getAttribute('data-add-to-plan'),
      name: button.getAttribute('data-place-name'),
      category: button.getAttribute('data-place-category'),
      latitude: placeLatitude ? parseFloat(placeLatitude) : null,
      longitude: placeLongitude ? parseFloat(placeLongitude) : null
    }
  }));
});

document.addEventListener('confirm-add-to-plan', async (event) => {
  const { planId, placeId } = event.detail;

  try {
    await apiFetch(`/api/plans/${planId}/places?placeId=${placeId}`, { method: 'POST' });
    window.location.href = `/plans/${planId}`;
  } catch (error) {
    console.error('Error agregando lugar al plan:', error);
    reportError(error.status === 409
      ? 'El destino ya se encuentra en el plan.'
      : 'Error al agregar el lugar al plan.');
  }
});
