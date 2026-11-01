package com.valhalla.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.valhalla.domain.place.*;
import com.valhalla.domain.plan.*;
import com.valhalla.domain.planplace.*;
import com.valhalla.domain.user.*;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@WebIntegrationTest
@Transactional
class PlanPlaceRestControllerTest extends PlanPlaceWebFixture {

  @Autowired
  private PlanPlaceService itineraryService;

  // El alta repetida del mismo lugar necesita el id de cada fila, y add() devuelve siempre la
  // primera: este helper solo confirma el alta y deja que el test lea el itinerario.
  private void postPlace(UUID placeId) throws Exception {
    mvc
      .perform(
        post(url).param("placeId", placeId.toString()).with(user("apl@test.com")).with(csrf())
      )
      .andExpect(status().isOk());
  }

  @Test
  void editorCanManageEveryItineraryOperation() {
    User editor = new User();
    editor.setEmail("editor@test.com");
    editor.setPassword("password");
    editor.setRole("USER");
    users.save(editor);
    plan.getParticipants().add(editor);
    plan.getEditors().add(editor);
    plans.save(plan);
    String email = editor.getEmail();
    PlanPlace firstEntry = itineraryService.addPlaceToPlan(plan.getId(), first, email);
    PlanPlace secondEntry = itineraryService.addPlaceToPlan(plan.getId(), second, email);
    itineraryService.updatePlanPlace(
      plan.getId(),
      firstEntry.getId(),
      new PlanPlaceUpdate(
        null,
        java.time.LocalDate.of(2026, 12, 1),
        java.time.LocalTime.of(10, 30),
        null,
        null
      ),
      email
    );
    itineraryService.reorderPlaces(
      plan.getId(),
      java.util.List.of(secondEntry.getId(), firstEntry.getId()),
      email
    );
    assertEquals(2, firstEntry.getSortOrder());
    assertEquals(java.time.LocalTime.of(10, 30), firstEntry.getVisitTime());
    itineraryService.removePlaceFromPlan(plan.getId(), firstEntry.getId(), email);
    assertEquals(1, entries.findByPlanId(plan.getId()).size());
  }

  @Test
  void detailIncludesPersistedVisitAndPlaceDataForParticipants() throws Exception {
    UUID id = add(first);
    PlanPlace entry = entries.findById(id).orElseThrow();
    entry.setDescription("Visita guiada");
    entry.setVisitDate(java.time.LocalDate.of(2026, 12, 1));
    entry.setVisitTime(java.time.LocalTime.of(10, 30));
    entries.save(entry);
    User participant = new User();
    participant.setEmail("participant@test.com");
    participant.setPassword("password");
    participant.setRole("USER");
    users.save(participant);
    plan.getParticipants().add(participant);
    plans.save(plan);
    mvc
      .perform(get(url).with(user(participant.getEmail())))
      .andExpect(status().isOk())
      .andExpect(jsonValue("/0/id", id))
      .andExpect(jsonValue("/0/description", "Visita guiada"))
      .andExpect(jsonValue("/0/visitDate", "2026-12-01"))
      .andExpect(jsonValue("/0/visitTime", "10:30"))
      .andExpect(jsonValue("/0/address", entry.getPlace().getAddress()))
      .andExpect(jsonValue("/0/category", entry.getPlace().getCategory().name()))
      .andExpect(jsonMissing("/0/password"))
      .andExpect(jsonMissing("/0/plan"));
  }

  @Test
  void repeatedPlacesKeepSeparateVisitDetails() throws Exception {
    add(first);
    add(first);
    var visits = entries.findByPlanId(plan.getId());
    visits.get(0).setDescription("Primera visita");
    visits.get(1).setDescription("Segunda visita");
    entries.save(visits.get(0));
    entries.save(visits.get(1));
    mvc
      .perform(get(url).with(user("apl@test.com")))
      .andExpect(status().isOk())
      .andExpect(jsonValue("/0/id", visits.get(0).getId()))
      .andExpect(jsonValue("/1/id", visits.get(1).getId()))
      .andExpect(jsonValue("/0/description", "Primera visita"))
      .andExpect(jsonValue("/1/description", "Segunda visita"));
  }

  @Test
  void I01_addPlace() throws Exception {
    add(first);
    assertEquals(1, entries.findByPlanId(plan.getId()).get(0).getSortOrder());
  }

  @Test
  void I02_samePlaceCanBeAddedTwice() throws Exception {
    add(first);
    add(first);
    var result = entries.findByPlanId(plan.getId());
    assertEquals(2, result.size());
    assertEquals(2, result.get(1).getSortOrder());
  }

  @Test
  void I03_orderedItineraryDoesNotExposeOwner() throws Exception {
    add(first);
    add(second);
    mvc
      .perform(get(url).with(user("apl@test.com")))
      .andExpect(status().isOk())
      .andExpect(jsonValue("/0/placeId", first))
      .andExpect(jsonValue("/1/sortOrder", 2))
      .andExpect(jsonMissing("/0/plan"));
  }

  @Test
  void I04_updatesScheduleAndClearsIt() throws Exception {
    UUID id = add(first);
    mvc
      .perform(
        put(url + "/" + id)
          .with(user("apl@test.com"))
          .with(csrf())
          .contentType("application/json")
          .content("{\"visitDate\":\"2026-12-01\",\"visitTime\":\"10:30\"}")
      )
      .andExpect(status().isOk())
      .andExpect(jsonValue("/visitDate", "2026-12-01"))
      .andExpect(jsonValue("/visitTime", "10:30"));
    mvc
      .perform(
        put(url + "/" + id)
          .with(user("apl@test.com"))
          .with(csrf())
          .contentType("application/json")
          .content("{}")
      )
      .andExpect(status().isOk());
    assertNull(entries.findById(id).orElseThrow().getVisitDate());
  }

  @Test
  void I05_addPlaceDerivaDuracionFechaYHora() throws Exception {
    plan.setEventDate(java.time.LocalDate.of(2026, 12, 1));
    plan.setEventTime(java.time.LocalTime.of(10, 0));
    plans.save(plan);
    Integer duration = places.findById(first).orElseThrow().getDefaultDurationMinutes();

    mvc
      .perform(post(url).param("placeId", first.toString()).with(user("apl@test.com")).with(csrf()))
      .andExpect(status().isOk())
      .andExpect(jsonValue("/durationMinutes", duration))
      .andExpect(jsonValue("/visitDate", "2026-12-01"))
      .andExpect(jsonValue("/visitTime", "10:00"))
      .andExpect(jsonValue("/timePinned", false));
  }

  @Test
  void I06_reorderConAutoScheduleMueveLasHorasAlNuevoOrden() throws Exception {
    plan.setEventDate(java.time.LocalDate.of(2026, 12, 1));
    plan.setEventTime(java.time.LocalTime.of(10, 0));
    plans.save(plan);
    // El mismo lugar dos veces: misma duracion y mismo punto, asi el traslado cae en el piso de 5
    // minutos y las horas del orden nuevo son exactas.
    postPlace(first);
    postPlace(first);
    var visits = entries.findByPlanId(plan.getId());
    UUID firstEntry = visits.get(0).getId();
    UUID secondEntry = visits.get(1).getId();

    mvc
      .perform(
        post(url + "/reorder")
          .with(user("apl@test.com"))
          .with(csrf())
          .contentType("application/json")
          .content("[\"" + secondEntry + "\",\"" + firstEntry + "\"]")
      )
      .andExpect(status().isOk());

    var reordered = entries.findByPlanId(plan.getId());
    assertEquals(secondEntry, reordered.get(0).getId());
    assertEquals(java.time.LocalTime.of(10, 0), reordered.get(0).getVisitTime());
    assertEquals(java.time.LocalTime.of(11, 35), reordered.get(1).getVisitTime());
  }

  @Test
  void I07_putConHoraExplicitaFijaLaParadaYRecalculaLasSiguientes() throws Exception {
    plan.setEventDate(java.time.LocalDate.of(2026, 12, 1));
    plan.setEventTime(java.time.LocalTime.of(10, 0));
    plans.save(plan);
    postPlace(first);
    postPlace(first);
    var visits = entries.findByPlanId(plan.getId());
    UUID pinnedId = visits.get(0).getId();
    UUID nextId = visits.get(1).getId();

    mvc
      .perform(
        put(url + "/" + pinnedId)
          .with(user("apl@test.com"))
          .with(csrf())
          .contentType("application/json")
          .content("{\"visitTime\":\"14:00\"}")
      )
      .andExpect(status().isOk())
      .andExpect(jsonValue("/visitTime", "14:00"))
      .andExpect(jsonValue("/timePinned", true));

    var updated = entries.findByPlanId(plan.getId());
    assertEquals(pinnedId, updated.get(0).getId());
    assertTrue(updated.get(0).isTimePinned());
    assertEquals(java.time.LocalTime.of(14, 0), updated.get(0).getVisitTime());
    assertEquals(java.time.LocalDate.of(2026, 12, 1), updated.get(0).getVisitDate());
    // La siguiente arranca a las 14:00 mas los 90 minutos de la parada fijada mas el piso de 5.
    assertEquals(nextId, updated.get(1).getId());
    assertEquals(java.time.LocalTime.of(15, 35), updated.get(1).getVisitTime());
  }

  @Test
  void I08_unEditorPuedeFijarElHorario() throws Exception {
    User editor = new User();
    editor.setEmail("editor@test.com");
    editor.setPassword("password");
    editor.setRole("USER");
    users.save(editor);
    plan.getParticipants().add(editor);
    plan.getEditors().add(editor);
    plan.setEventDate(java.time.LocalDate.of(2026, 12, 1));
    plan.setEventTime(java.time.LocalTime.of(10, 0));
    plans.save(plan);
    UUID id = add(first);

    mvc
      .perform(
        put(url + "/" + id)
          .with(user(editor.getEmail()))
          .with(csrf())
          .contentType("application/json")
          .content(
            "{\"visitDate\":\"2026-12-02\",\"visitTime\":\"09:30\"," +
            "\"durationMinutes\":45,\"transportModeToNext\":\"BIKE\"," +
            "\"travelMinutesToNext\":30}"
          )
      )
      .andExpect(status().isOk())
      .andExpect(jsonValue("/visitDate", "2026-12-02"))
      .andExpect(jsonValue("/visitTime", "09:30"))
      .andExpect(jsonValue("/durationMinutes", 45))
      .andExpect(jsonValue("/transportModeToNext", "BIKE"))
      .andExpect(jsonValue("/travelMinutesToNext", 30))
      .andExpect(jsonValue("/timePinned", true));

    PlanPlace saved = entries.findById(id).orElseThrow();
    assertTrue(saved.isTimePinned());
    assertEquals(java.time.LocalTime.of(9, 30), saved.getVisitTime());
    assertEquals(45, saved.getDurationMinutes());
    assertEquals(TransportMode.BIKE, saved.getTransportModeToNext());
    assertEquals(30, saved.getTravelMinutesToNext());
  }

  @Test
  void unTrasladoFijoNoNumericoDevuelve400YNoEscribe() throws Exception {
    UUID id = add(first);

    mvc
      .perform(
        put(url + "/" + id)
          .with(user("apl@test.com"))
          .with(csrf())
          .contentType("application/json")
          .content("{\"travelMinutesToNext\":\"treinta\"}")
      )
      .andExpect(status().isBadRequest());

    assertNull(entries.findById(id).orElseThrow().getTravelMinutesToNext());
  }

  @Test
  void unModoDeTrasladoDesconocidoDevuelve400YNoEscribe() throws Exception {
    UUID id = add(first);

    mvc
      .perform(
        put(url + "/" + id)
          .with(user("apl@test.com"))
          .with(csrf())
          .contentType("application/json")
          .content("{\"transportModeToNext\":\"TELEPORT\"}")
      )
      .andExpect(status().isBadRequest());

    PlanPlace unchanged = entries.findById(id).orElseThrow();
    assertNull(unchanged.getTransportModeToNext());
    assertFalse(unchanged.isTimePinned());
  }

  @Test
  void I09_deletesEntry() throws Exception {
    UUID id = add(first);
    mvc
      .perform(delete(url + "/" + id).with(user("apl@test.com")).with(csrf()))
      .andExpect(status().isOk());
    assertTrue(entries.findById(id).isEmpty());
  }

  @Test
  void I10_reorders() throws Exception {
    UUID firstEntry = add(first);
    UUID secondEntry = add(second);
    mvc
      .perform(
        post(url + "/reorder")
          .with(user("apl@test.com"))
          .with(csrf())
          .contentType("application/json")
          .content("[\"" + secondEntry + "\",\"" + firstEntry + "\"]")
      )
      .andExpect(status().isOk());
    assertEquals(second, entries.findByPlanId(plan.getId()).get(0).getPlace().getId());
  }

  @Test
  void invalidDateReturns400AndDoesNotModify() throws Exception {
    UUID id = add(first);
    mvc
      .perform(
        put(url + "/" + id)
          .with(user("apl@test.com"))
          .with(csrf())
          .contentType("application/json")
          .content("{\"visitDate\":\"bad\"}")
      )
      .andExpect(status().isBadRequest());
    assertNull(entries.findById(id).orElseThrow().getVisitDate());
  }

  @Test
  void invalidReorderReturns400() throws Exception {
    add(first);
    mvc
      .perform(
        post(url + "/reorder")
          .with(user("apl@test.com"))
          .with(csrf())
          .contentType("application/json")
          .content("[]")
      )
      .andExpect(status().isBadRequest());
    assertEquals(1, entries.findByPlanId(plan.getId()).get(0).getSortOrder());
  }
}
