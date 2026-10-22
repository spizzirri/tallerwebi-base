package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.user.UserRepository;
import com.valhalla.domain.user.UserService;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@WebIntegrationTest
@Transactional
public class UserControllerTest {

  private static final String ADMIN_EMAIL = "admin@unlam.edu.ar";
  private static final String TEST_PASSWORD = "password123";
  private static final String TEST_ROLE = "USER";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PlanService planService;

  @Autowired
  private PlanRepository planRepository;

  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    this.mockMvc =
      MockMvcBuilders
        .webAppContextSetup(this.wac)
        .apply(springSecurity())
        .addFilter(new HiddenHttpMethodFilter())
        .build();
  }

  @Test
  public void shouldRedirectToLoginWhenNotAuthenticated() throws Exception {
    this.mockMvc.perform(get("/admin/users")).andExpect(status().is3xxRedirection());
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldShowUsersListWhenAuthenticated() throws Exception {
    this.mockMvc.perform(get("/admin/users"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/users"))
      .andExpect(model().attributeExists("users"))
      .andExpect(model().attributeExists("currentUserEmail"));
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldShowNewUserForm() throws Exception {
    this.mockMvc.perform(get("/admin/users/new"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/user-form"))
      .andExpect(model().attributeExists("userForm"))
      .andExpect(model().attribute("isEdit", false));
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldCreateUserAndRedirectToList() throws Exception {
    this.mockMvc.perform(
        post("/admin/users")
          .with(csrf())
          .param("firstName", "New")
          .param("lastName", "User")
          .param("email", "newuser@unlam.edu.ar")
          .param("role", "USER")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/users"));
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldReRenderFormWithErrorsWhenEmailIsInvalid() throws Exception {
    this.mockMvc.perform(
        post("/admin/users")
          .with(csrf())
          .param("firstName", "Test")
          .param("lastName", "User")
          .param("email", "not-an-email")
          .param("role", "USER")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/user-form"))
      .andExpect(model().attribute("error", "Invalid user data"))
      .andExpect(model().attributeExists("org.springframework.validation.BindingResult.userForm"));
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldReRenderFormWithErrorsWhenEmailIsMissing() throws Exception {
    this.mockMvc.perform(
        post("/admin/users")
          .with(csrf())
          .param("firstName", "Test")
          .param("lastName", "User")
          .param("role", "USER")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/user-form"))
      .andExpect(model().attribute("error", "Invalid user data"));
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldShowEditUserForm() throws Exception {
    userService.create("editable@unlam.edu.ar", TEST_PASSWORD, TEST_ROLE, "Editable", "User");
    UUID userId = userRepository.findByEmail("editable@unlam.edu.ar").get().getId();

    this.mockMvc.perform(get("/admin/users/" + userId + "/edit"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/user-form"))
      .andExpect(model().attributeExists("userForm"))
      .andExpect(model().attribute("isEdit", true))
      .andExpect(model().attribute("userId", userId));
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldUpdateUserAndRedirectToList() throws Exception {
    userService.create("updatable@unlam.edu.ar", TEST_PASSWORD, TEST_ROLE, "Updatable", "User");
    UUID userId = userRepository.findByEmail("updatable@unlam.edu.ar").get().getId();

    this.mockMvc.perform(
        post("/admin/users/" + userId)
          .with(csrf())
          .param("_method", "PUT")
          .param("firstName", "Updated")
          .param("lastName", "User")
          .param("email", "updated@unlam.edu.ar")
          .param("role", "ADMIN")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/users"));
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldDeactivateUserAndRedirectToList() throws Exception {
    userService.create(
      "deactivatable@unlam.edu.ar",
      TEST_PASSWORD,
      TEST_ROLE,
      "Deactivatable",
      "User"
    );
    UUID userId = userRepository.findByEmail("deactivatable@unlam.edu.ar").get().getId();

    this.mockMvc.perform(
        post("/admin/users/" + userId + "/deactivate").with(csrf()).param("_method", "PUT")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/users"));
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldDeleteUserAndRedirectToList() throws Exception {
    userService.create("deletable@unlam.edu.ar", TEST_PASSWORD, TEST_ROLE, "Deletable", "User");
    UUID userId = userRepository.findByEmail("deletable@unlam.edu.ar").get().getId();

    this.mockMvc.perform(
        post("/admin/users/" + userId + "/delete").with(csrf()).param("_method", "DELETE")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/users"));
  }

  /**
   * La regresión del 500 que reportamos: borrar un usuario con planes daba 500 porque
   * plans.administrator_id es una FK a users.id. El test que ya existía borraba a un usuario recién
   * creado, que por definición no tiene planes, así que no veía nada.
   *
   * <p>Este caso solo vale contra Postgres real: contra un esquema en memoria la FK no existe y el
   * borrado en el orden viejo pasaría.
   */
  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldDeleteAUserThatOwnsPlansWithoutFailing() throws Exception {
    String ownerEmail = "owner@unlam.edu.ar";
    userService.create(ownerEmail, TEST_PASSWORD, TEST_ROLE, "Owner", "OfPlans");
    UUID userId = userRepository.findByEmail(ownerEmail).get().getId();

    Plan plan = new Plan();
    plan.setName("A plan that blocks the delete");
    plan.setEventDate(LocalDate.now().plusDays(1));
    Plan saved = planService.createPlan(plan, ownerEmail);

    // Precondición explícita: el borrado en cascada no se puede probar si la consulta por planes no ve
    // la fila. Si esto falla, el caso es inconcluyente y el fallo hay que buscarlo acá, no en el
    // servicio.
    assertThat(this.planRepository.findByAdministratorId(userId), hasSize(1));

    // Sin el borrado en cascada este test falla con TransientObjectException ("persistent instance
    // references an unsaved transient instance of User"): Hibernate se da cuenta, antes de llegar
    // a la base, de que un Plan gestionado sigue apuntando al User que se está borrando. En
    // producción el mismo estado es la violación de la FK que el admin veía como 500.

    this.mockMvc.perform(
        post("/admin/users/" + userId + "/delete").with(csrf()).param("_method", "DELETE")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/users"));

    // El usuario y su plan tienen que estar los dos: que se fuera el usuario y quedara el plan
    // apuntando al aire sería otro bug.
    assertThat(userRepository.findByEmail(ownerEmail).isPresent(), is(false));
    assertThat(planRepository.findById(saved.getId()).isPresent(), is(false));
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldReRenderEditFormWithErrorWhenUpdateInputIsInvalid() throws Exception {
    userService.create(
      "updatable-invalid@unlam.edu.ar",
      TEST_PASSWORD,
      TEST_ROLE,
      "Updatable",
      "Invalid"
    );
    UUID userId = userRepository.findByEmail("updatable-invalid@unlam.edu.ar").get().getId();

    this.mockMvc.perform(
        post("/admin/users/" + userId)
          .with(csrf())
          .param("_method", "PUT")
          .param("firstName", "Updatable")
          .param("lastName", "Invalid")
          .param("email", "")
          .param("role", "ADMIN")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/user-form"))
      .andExpect(model().attribute("isEdit", true))
      .andExpect(model().attribute("userId", userId));
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldActivateUserAndRedirectToList() throws Exception {
    userService.create("activatable@unlam.edu.ar", TEST_PASSWORD, TEST_ROLE, "Activatable", "User");
    UUID userId = userRepository.findByEmail("activatable@unlam.edu.ar").get().getId();
    userService.deactivate(userId);

    this.mockMvc.perform(
        post("/admin/users/" + userId + "/activate").with(csrf()).param("_method", "PUT")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/users"));
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldRotatePasswordAndRedirectToList() throws Exception {
    userService.create("rotatable@unlam.edu.ar", TEST_PASSWORD, TEST_ROLE, "Rotatable", "User");
    UUID userId = userRepository.findByEmail("rotatable@unlam.edu.ar").get().getId();

    this.mockMvc.perform(
        post("/admin/users/" + userId + "/rotate-password").with(csrf()).param("_method", "PUT")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/users"));
  }

  @Test
  @WithMockUser(username = "admin@unlam.edu.ar", roles = { "ADMIN" })
  public void shouldShowGeneratedPasswordAfterCreate() throws Exception {
    this.mockMvc.perform(get("/admin/users").flashAttr("generatedPassword", "abc12345"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/users"))
      .andExpect(model().attribute("generatedPassword", "abc12345"));
  }
}
