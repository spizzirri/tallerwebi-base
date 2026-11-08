package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.valhalla.domain.notification.Notification;
import com.valhalla.domain.notification.NotificationService;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * [NOTIF] Centro de notificaciones contra la cadena real de seguridad y HSQLDB (T-NOTIF-B02):
 * alta por follow/participacion/plan nuevo, listado paginado, marcado individual y global, y la
 * regla de que nadie marca filas ajenas. Escenarios I-01..I-08 de docs/specs/25-NOTIF.md.
 */
@WebIntegrationTest
@Transactional
public class NotificationIntegrationTest {

  private static final String RECIPIENT_EMAIL = "notif.recipient@test.com";
  private static final String ACTOR_EMAIL = "notif.actor@test.com";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private PlanService planService;

  private MockMvc mockMvc;

  private UUID recipientId;
  private UUID actorId;

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
    this.recipientId = givenUser(RECIPIENT_EMAIL, "Grace", "Hopper");
    this.actorId = givenUser(ACTOR_EMAIL, "Ada", "Lovelace");
  }

  // --- I-01/I-02: alta por follow y listado ---

  @Test
  public void I01_followCreaLaNotificacionYElListadoLaMuestra() throws Exception {
    followAdaToGrace();

    String html =
      this.mockMvc.perform(get("/notifications").with(user(RECIPIENT_EMAIL)))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/notifications/list"))
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(html, containsString("Ada Lovelace"));
    assertThat(html, containsString("te empezó a seguir"));
    // AC-17: la fila recien creada se pinta como pendiente.
    assertThat(html, containsString("notification-row--unread"));
    assertThat(this.notificationService.unreadCount(this.recipientId), is(1L));
    assertThat(this.notificationService.unreadCount(this.actorId), is(0L));
  }

  // --- I-03: marcar una leida desde la lista ---

  @Test
  public void I03_markReadIndividualBajaElBadgeYEsIdempotente() throws Exception {
    followAdaToGrace();
    UUID notificationId = this.notificationService.list(this.recipientId, 0, 10).getFirst().getId();

    this.mockMvc.perform(
        post("/notifications/" + notificationId + "/read").with(user(RECIPIENT_EMAIL)).with(csrf())
      )
      .andExpect(status().is3xxRedirection());

    assertThat(this.notificationService.unreadCount(this.recipientId), is(0L));
    List<Notification> all = this.notificationService.list(this.recipientId, 0, 10);
    assertThat(all.getFirst().isRead(), is(true));

    // AC-17: leida, la fila pierde el estado de pendiente.
    String html =
      this.mockMvc.perform(get("/notifications").with(user(RECIPIENT_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    assertThat(html, containsString("notification-row--read"));
    assertThat(html, not(containsString("notification-row--unread")));
  }

  // --- I-04: no-leida y leida conviven en la vista ---

  @Test
  public void I04_dejarDeSeguirNoBorraNiMarcaLaNotificacion() throws Exception {
    followAdaToGrace();

    this.mockMvc.perform(
        post("/profile/" + this.recipientId + "/unfollow").with(user(ACTOR_EMAIL)).with(csrf())
      );
    // [NOTIF] AC-02: dejar de seguir NO borra la notificacion ni la marca: la marca es del lector.
    assertThat(this.notificationService.unreadCount(this.recipientId), is(1L));
  }

  // --- I-05: marcar todas desde la vista ---

  @Test
  public void I05_markAllReadVaciarElBadgeDeUnaAccion() throws Exception {
    followAdaToGrace();
    givenUser("notif.third@test.com", "Iris", "West");
    this.mockMvc.perform(
        post("/profile/" + this.recipientId + "/follow")
          .with(user("notif.third@test.com"))
          .with(csrf())
      );
    assertThat(this.notificationService.unreadCount(this.recipientId), is(2L));

    this.mockMvc.perform(post("/notifications/read-all").with(user(RECIPIENT_EMAIL)).with(csrf()))
      .andExpect(status().is3xxRedirection());

    assertThat(this.notificationService.unreadCount(this.recipientId), is(0L));
  }

  // --- I-06: paginacion ---

  @Test
  public void I06_listadoPaginaDeDiezConVentanaSiguiente() throws Exception {
    List<UUID> followers = new ArrayList<>();
    for (int i = 0; i < 11; i++) {
      String email = "notif.fan" + i + "@test.com";
      followers.add(givenUser(email, "Fan", String.valueOf(i)));
      this.mockMvc.perform(
          post("/profile/" + this.recipientId + "/follow").with(user(email)).with(csrf())
        );
    }

    String firstPage =
      this.mockMvc.perform(get("/notifications").with(user(RECIPIENT_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    String secondPage =
      this.mockMvc.perform(get("/notifications").param("page", "1").with(user(RECIPIENT_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(this.notificationService.count(this.recipientId), is(11L));
    // pagina 0: 10 filas y link a la siguiente; pagina 1: la fila restante
    assertThat(countOccurrences(firstPage, "data-testid=\"notification\""), is(10));
    assertThat(countOccurrences(secondPage, "data-testid=\"notification\""), is(1));
    assertThat(firstPage, containsString("href=\"/notifications?page=1\""));
    assertThat(secondPage, containsString("href=\"/notifications?page=0\""));
    assertThat(secondPage, not(containsString("href=\"/notifications?page=2\"")));
  }

  // --- I-07: nadie marca filas ajenas ---

  @Test
  public void I07_markReadSobreIdDeOtroDestinatarioEsNoOp() throws Exception {
    followAdaToGrace();
    UUID notificationId = this.notificationService.list(this.recipientId, 0, 10).getFirst().getId();
    UUID strangerId = givenUser("notif.stranger@test.com", "Str", "Aanger");

    this.mockMvc.perform(
        post("/notifications/" + notificationId + "/read")
          .with(user("notif.stranger@test.com"))
          .with(csrf())
      )
      .andExpect(status().is3xxRedirection());

    // la notificacion de Grace sigue pendiente: el id ajeno no la toco (AC-09)
    assertThat(this.notificationService.unreadCount(this.recipientId), is(1L));
    assertThat(this.notificationService.unreadCount(strangerId), is(0L));
  }

  // --- I-08: rutas propias del centro protegidas ---

  @Test
  public void I08_listadoSinSesionRedirigeAlLogin() throws Exception {
    this.mockMvc.perform(get("/notifications"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrlPattern("**/auth/login*"));
  }

  // --- I-09/I-10: participante nuevo y plan publicado ---

  @Test
  public void I09_joinPorShortCodeNotificaAlDuenoDelPlan() {
    // given: Grace es dueña de un plan publico; Ada lo descubre por el codigo
    Plan plan = new Plan();
    plan.setName("Viaje a Bariloche");
    plan.setIsPublic(true);
    Plan created = this.planService.createPlan(plan, RECIPIENT_EMAIL);

    // when: Ada se une por short code al plan publico de Grace
    this.planService.joinPlan(created.getShortCode(), ACTOR_EMAIL);

    // then: AC-02, Grace ve quien se sumo y a que plan
    List<Notification> notifications = this.notificationService.list(this.recipientId, 0, 10);
    assertThat("la mas nueva es el participante", notifications.getFirst().isRead(), is(false));
    assertThat(notifications.getFirst().getRelatedPlanName(), is("Viaje a Bariloche"));
    assertThat(notifications.getFirst().getActor().getId(), is(this.actorId));
  }

  @Test
  public void I10_planPublicoNuevoNotificaASeguidores() throws Exception {
    // given: Ada sigue a Grace y Grace publica
    followAdaToGrace();
    Plan plan = new Plan();
    plan.setName("Escapada");
    plan.setIsPublic(true);
    this.planService.createPlan(plan, RECIPIENT_EMAIL);

    // then: la seguidora recibe la notificacion de plan nuevo (AC-03)
    List<Notification> forAda = this.notificationService.list(this.actorId, 0, 10);
    assertThat(forAda, hasSize(1));
    assertThat(forAda.getFirst().getRelatedPlanName(), is("Escapada"));
    assertThat(forAda.getFirst().getActor().getId(), is(this.recipientId));
  }

  @Test
  public void I11_invitacionNotificaAlInvitadoYElCentroLaTraduce() throws Exception {
    // given: Grace crea un plan privado e invita a Ada por el canal de la app
    Plan plan = new Plan();
    plan.setName("Cena secreta");
    plan.setIsPublic(false);
    Plan created = this.planService.createPlan(plan, RECIPIENT_EMAIL);
    this.planService.inviteParticipant(created.getId(), ACTOR_EMAIL, "guest", RECIPIENT_EMAIL);

    // then: AC-03 (extension), Ada es la destinataria con el nombre del plan congelado
    List<Notification> forAda = this.notificationService.list(this.actorId, 0, 10);
    assertThat(forAda, hasSize(1));
    assertThat(forAda.getFirst().getRelatedPlanName(), is("Cena secreta"));
    assertThat(forAda.getFirst().getActor().getId(), is(this.recipientId));

    String html =
      this.mockMvc.perform(get("/notifications").with(user(ACTOR_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    assertThat(html, containsString("te invitó al plan"));
    assertThat(html, containsString("Cena secreta"));
    // AC-10: el destino es el aside de invitaciones pendientes, donde esta el Aceptar.
    assertThat(html, containsString("href=\"/plans#pending-invitations-heading\""));
    // AC-12: la fila no filtra el email del invitador
    assertThat(html, not(containsString(RECIPIENT_EMAIL)));
  }

  @Test
  public void I12_solicitudDeUnionNotificaAlDueno() throws Exception {
    // given: plan privado de Grace; Ada pide unirse
    Plan plan = new Plan();
    plan.setName("Taller secreto");
    plan.setIsPublic(false);
    Plan created = this.planService.createPlan(plan, RECIPIENT_EMAIL);
    this.planService.requestToJoin(created.getId(), ACTOR_EMAIL);

    // then: AC-15, el dueno ve la solicitud en el centro
    List<Notification> forGrace = this.notificationService.list(this.recipientId, 0, 10);
    assertThat(forGrace, hasSize(1));
    assertThat(forGrace.getFirst().getActor().getId(), is(this.actorId));
    assertThat(forGrace.getFirst().getRelatedPlanName(), is("Taller secreto"));

    String html =
      this.mockMvc.perform(get("/notifications").with(user(RECIPIENT_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    assertThat(html, containsString("solicitó unirse a tu plan"));
    assertThat(html, containsString("Taller secreto"));
    // AC-10: la solicitud se resuelve en la seccion Solicitudes del plan, no arriba de la pagina.
    assertThat(html, containsString("/plans/" + created.getId() + "#join-requests-heading"));
  }

  @Test
  public void I13_respuestaASolicitudNotificaAlSolicitante() throws Exception {
    // given: Ada solicito unirse al plan privado de Grace
    Plan plan = new Plan();
    plan.setName("Coro");
    plan.setIsPublic(false);
    Plan created = this.planService.createPlan(plan, RECIPIENT_EMAIL);
    this.planService.requestToJoin(created.getId(), ACTOR_EMAIL);
    UUID requestId =
      this.planService.getPendingJoinRequests(created.getId(), RECIPIENT_EMAIL).getFirst().id();

    // when: Grace acepta
    this.planService.respondToJoinRequest(created.getId(), requestId, "accept", RECIPIENT_EMAIL);

    // then: AC-16, Ada cierra su pendiente con la aprobacion (y Grace ademas ve el participante)
    List<Notification> forAda = this.notificationService.list(this.actorId, 0, 10);
    assertThat(forAda, hasSize(1));
    String html =
      this.mockMvc.perform(get("/notifications").with(user(ACTOR_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    assertThat(html, containsString("aprobó tu solicitud para el plan"));
    assertThat(html, containsString("Coro"));
    // AC-10: aprobada, la solicitud ya no corta: el link abre el plan.
    assertThat(html, containsString("href=\"/plans/" + created.getId() + "\""));

    // when: otra solicitud, ahora rechazada
    UUID thirdId = givenUser("notif.new@test.com", "Rita", "Respuesta");
    this.planService.requestToJoin(created.getId(), "notif.new@test.com");
    UUID second =
      this.planService.getPendingJoinRequests(created.getId(), RECIPIENT_EMAIL).getFirst().id();
    this.planService.respondToJoinRequest(created.getId(), second, "reject", RECIPIENT_EMAIL);

    // then: el rechazo tambien es una respuesta
    List<Notification> forNewUser = this.notificationService.list(thirdId, 0, 10);
    assertThat(forNewUser, hasSize(1));
    assertThat(forNewUser.getFirst().getRelatedPlanName(), is("Coro"));

    // AC-10: sin acceso al plan, el unico destino que resuelve algo es el landing publico, donde
    // se puede volver a solicitar.
    String rejectedHtml =
      this.mockMvc.perform(get("/notifications").with(user("notif.new@test.com")))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    assertThat(rejectedHtml, containsString("rechazó tu solicitud para el plan"));
    assertThat(rejectedHtml, containsString("/plans/" + created.getId() + "/public"));
  }

  // --- I-14: tirar una fila ---

  @Test
  public void I14_deleteSacaLaFilaPropiaYUnaAjenaNoHaceNada() throws Exception {
    followAdaToGrace();
    UUID notificationId = this.notificationService.list(this.recipientId, 0, 10).getFirst().getId();
    givenUser("notif.deleter@test.com", "Tercero", "Ajeno");

    // AC-13: sin CSRF el centro no acepta escrituras
    this.mockMvc.perform(
        post("/notifications/" + notificationId + "/delete").with(user(RECIPIENT_EMAIL))
      )
      .andExpect(status().isForbidden());
    assertThat(this.notificationService.count(this.recipientId), is(1L));

    // AC-09: un tercero tampoco puede tirar la fila de Grace
    this.mockMvc.perform(
        post("/notifications/" + notificationId + "/delete")
          .with(user("notif.deleter@test.com"))
          .with(csrf())
      )
      .andExpect(status().is3xxRedirection());
    assertThat(this.notificationService.count(this.recipientId), is(1L));

    // AC-18: Grace si la borra, y la bandeja queda vacia
    this.mockMvc.perform(
        post("/notifications/" + notificationId + "/delete")
          .with(user(RECIPIENT_EMAIL))
          .with(csrf())
      )
      .andExpect(status().is3xxRedirection());
    assertThat(this.notificationService.count(this.recipientId), is(0L));

    String html =
      this.mockMvc.perform(get("/notifications").with(user(RECIPIENT_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    assertThat(html, containsString("Todavía no tenés notificaciones."));
  }

  // --- helpers ---

  private void followAdaToGrace() throws Exception {
    this.mockMvc.perform(
        post("/profile/" + this.recipientId + "/follow").with(user(ACTOR_EMAIL)).with(csrf())
      )
      .andExpect(status().is3xxRedirection());
  }

  private static int countOccurrences(String haystack, String needle) {
    int count = 0;
    int from = 0;
    while (true) {
      int hit = haystack.indexOf(needle, from);
      if (hit < 0) {
        return count;
      }
      count++;
      from = hit + needle.length();
    }
  }

  private UUID givenUser(String email, String firstName, String lastName) {
    User user = new User();
    user.setEmail(email);
    user.setPassword("password123");
    user.setRole("USER");
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.activate();
    this.userRepository.save(user);
    return this.userRepository.findByEmail(email).orElseThrow().getId();
  }
}
