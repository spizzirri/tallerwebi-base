/**
 * Add to Plan functionality for Places view.
 * Emits a custom event when the "+" button is clicked on a place card.
 * The Vue app in list.html listens for this event and handles the modal.
 */
(function() {
  'use strict';

  // Listen for clicks on add-to-plan buttons
  document.addEventListener('click', function(event) {
    const button = event.target.closest('[data-add-to-plan]');
    if (!button) return;

    event.preventDefault();
    event.stopPropagation();

    const placeId = button.getAttribute('data-add-to-plan');
    const placeName = button.getAttribute('data-place-name');
    const placeCategory = button.getAttribute('data-place-category');
    const placeLatitude = button.getAttribute('data-place-latitude');
    const placeLongitude = button.getAttribute('data-place-longitude');

    // Emit custom event for Vue to handle
    const addToPlanEvent = new CustomEvent('add-to-plan', {
      detail: {
        id: placeId,
        name: placeName,
        category: placeCategory,
        latitude: placeLatitude ? parseFloat(placeLatitude) : null,
        longitude: placeLongitude ? parseFloat(placeLongitude) : null
      }
    });

    document.dispatchEvent(addToPlanEvent);
  });

  // Listen for confirm-add-to-plan events from the Vue app
  document.addEventListener('confirm-add-to-plan', async function(event) {
    const { planId, placeId } = event.detail;

    try {
      const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
      const csrfHeaderName = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';

      if (!csrfToken) {
        console.error('CSRF token no encontrado: revisar que <meta name="_csrf"> exista en el head.');
      }

      const response = await fetch(`/api/plans/${planId}/places?placeId=${placeId}`, {
        method: 'POST',
        headers: {
          [csrfHeaderName]: csrfToken
        }
      });

      if (response.ok) {
        // Redirect to plan detail page
        window.location.href = `/plans/${planId}`;
      } else if (response.status === 409) {
        alert('El destino ya se encuentra en el plan');
      } else {
        const bodyText = await response.text().catch(() => '(sin cuerpo)');
        console.error(`Error agregando lugar al plan: status=${response.status}, body=${bodyText}`);
        alert('Error al agregar el lugar al plan');
      }
    } catch (error) {
      console.error('Error adding place to plan:', error);
      alert('Error al agregar el lugar al plan');
    }
  });

  console.log('Add to Plan module initialized');
})();
