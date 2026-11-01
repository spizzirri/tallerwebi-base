package com.valhalla.e2e;

import static org.junit.jupiter.api.Assertions.*;

import com.valhalla.e2e.views.PlansPage;
import org.junit.jupiter.api.Test;

public class AddPlaceToPlanE2E extends E2eBase {

  @Test
  void E01_addPlaceAndReadItineraryWithBrowserSession() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    plans.createPlanViaExplore("APL E2E", "Itinerario de prueba", "2026-12-31");
    String planId = page.url().substring(page.url().lastIndexOf('/') + 1);
    // /places no renderiza agregado (05-APL-FE): exercise the backend via fetch in the authenticated browser.
    Object result = page.evaluate(
      """
      async planId => {
        const tokenEl = document.querySelector('meta[name="csrf-token"]') || document.querySelector('input[name="_csrf"]');
        const headerEl = document.querySelector('meta[name="csrf-header"]');
        const token = tokenEl?.content || tokenEl?.value || '';
        const header = headerEl?.content || 'X-CSRF-TOKEN';
        if (!token) throw new Error('Missing CSRF token');
        const catalog = await (await fetch('/api/places')).json();
        if (!catalog || catalog.length === 0) throw new Error('No places in catalog');
        const url = '/api/plans/' + planId + '/places';
        const placeId = catalog[0].id;
        const added = await fetch(url + '?placeId=' + placeId, {
          method: 'POST',
          headers: { [header]: token }
        });
        if (!added.ok) {
          const body = await added.text().catch(() => '');
          throw new Error('Add failed: ' + added.status + ' ' + body);
        }
        const itinerary = await (await fetch(url)).json();
        return (
          Array.isArray(itinerary) &&
          itinerary.length >= 1 &&
          itinerary.some((e) => e.placeId === placeId)
        );
      }
      """,
      planId
    );
    assertEquals(Boolean.TRUE, result);
  }
}
