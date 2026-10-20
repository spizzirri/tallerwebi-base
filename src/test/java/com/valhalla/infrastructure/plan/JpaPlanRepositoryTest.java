package com.valhalla.infrastructure.plan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanParticipantCount;
import com.valhalla.domain.user.User;
import com.valhalla.infrastructure.user.JpaUserRepository;
import com.valhalla.integration.JpaIntegrationTest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@JpaIntegrationTest
public class JpaPlanRepositoryTest {

  @Autowired
  private JpaPlanRepository planRepository;

  @Autowired
  private JpaUserRepository userRepository;

  @Test
  @Transactional
  @Rollback
  public void T_PLN_013_save_guardaYAsignaId() {
    Plan plan = new Plan();
    plan.setName("Viaje a Bariloche");
    plan.setShortCode("ABC12345");

    Plan guardado = this.planRepository.save(plan);

    assertThat(guardado.getId(), is(notNullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void T_PLN_013_findByShortCode_existente() {
    Plan plan = new Plan();
    plan.setName("Viaje a Bariloche");
    plan.setShortCode("XYZ78901");
    this.planRepository.save(plan);

    Plan encontrado = this.planRepository.findByShortCode("XYZ78901").orElse(null);

    assertThat(encontrado, is(notNullValue()));
    assertThat(encontrado.getName(), is(equalTo("Viaje a Bariloche")));
  }

  @Test
  @Transactional
  public void T_PLN_013_findByShortCode_inexistente() {
    // An unknown code answers Optional.empty(), not null: the earlier assertion compared the
    // Optional against nullValue() and failed on every run.
    Optional<Plan> encontrado = this.planRepository.findByShortCode("NOEXISTE");

    assertThat(encontrado.isPresent(), is(false));
  }

  @Test
  @Transactional
  @Rollback
  public void T_PLN_013_delete_borraElPlan() {
    Plan plan = new Plan();
    plan.setName("Plan a borrar");
    plan.setShortCode("DEL12345");
    Plan guardado = this.planRepository.save(plan);

    this.planRepository.deleteById(guardado.getId());

    assertThat(this.planRepository.findById(guardado.getId()).isPresent(), is(false));
  }

  @Test
  @Transactional
  @Rollback
  public void T_PLN_013_findByAdministratorId_devuelveSoloLosDeEseUsuario() {
    User user1 = new User();
    user1.setEmail("user1@test.com");
    user1 = this.userRepository.save(user1);

    User user2 = new User();
    user2.setEmail("user2@test.com");
    user2 = this.userRepository.save(user2);

    Plan planDeUser1 = new Plan();
    planDeUser1.setName("Plan de user1");
    planDeUser1.setShortCode("USR00001");
    planDeUser1.setAdministrator(user1);
    this.planRepository.save(planDeUser1);

    Plan planDeUser2 = new Plan();
    planDeUser2.setName("Plan de user2");
    planDeUser2.setShortCode("USR00002");
    planDeUser2.setAdministrator(user2);
    this.planRepository.save(planDeUser2);

    List<Plan> resultado = this.planRepository.findByAdministratorId(user1.getId());

    assertThat(resultado.size(), is(equalTo(1)));
    assertThat(resultado.get(0).getName(), is(equalTo("Plan de user1")));
  }

  @Test
  @Transactional
  @Rollback
  public void I01_countParticipantsByPlanIds_cuentaSoloParticipantesYNoAlOwner() {
    User owner = saveUser("owner-top@test.com");
    User participant = saveUser("participant-top@test.com");
    User editor = saveUser("editor-top@test.com");

    Plan plan = new Plan();
    plan.setName("Plan con participantes");
    plan.setShortCode("TOP00001");
    plan.setAdministrator(owner);
    plan.getParticipants().add(participant);
    plan.getParticipants().add(editor);
    plan.getEditors().add(editor);
    Plan saved = this.planRepository.save(plan);

    List<PlanParticipantCount> counts =
      this.planRepository.countParticipantsByPlanIds(List.of(saved.getId()));

    assertThat(counts, hasSize(1));
    assertThat(counts.getFirst().planId(), is(equalTo(saved.getId())));
    // el owner vive en administrator y el editor suma una fila en plan_participants
    assertThat(counts.getFirst().count(), is(equalTo(2L)));
  }

  @Test
  @Transactional
  @Rollback
  public void I02_countParticipantsByPlanIds_sinParticipantesDevuelveCero() {
    User owner = saveUser("owner-alone@test.com");

    Plan plan = new Plan();
    plan.setName("Plan sin participantes");
    plan.setShortCode("TOP00002");
    plan.setAdministrator(owner);
    Plan saved = this.planRepository.save(plan);

    List<PlanParticipantCount> counts =
      this.planRepository.countParticipantsByPlanIds(List.of(saved.getId()));

    // el left join devuelve la fila con 0: no se completa el mapa con ceros en Java
    assertThat(counts, hasSize(1));
    assertThat(counts.getFirst().planId(), is(equalTo(saved.getId())));
    assertThat(counts.getFirst().count(), is(equalTo(0L)));
  }

  private User saveUser(String email) {
    User user = new User();
    user.setEmail(email);
    return this.userRepository.save(user);
  }
}
