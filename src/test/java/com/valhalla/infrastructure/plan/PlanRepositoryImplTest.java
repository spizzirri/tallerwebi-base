package com.valhalla.infrastructure.plan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanParticipantCount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests of {@link PlanRepositoryImpl}: every port method must reach {@link
 * JpaPlanRepository} with the same arguments. The delegation is the whole behaviour of the
 * adapter, so each test asserts the interaction on the mocked JPA repository as well as the value
 * coming back.
 */
public class PlanRepositoryImplTest {

  private static final UUID PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000001");
  private static final UUID OTHER_PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000002");

  private JpaPlanRepository jpaPlanRepositoryMock;
  private PlanRepositoryImpl planRepository;

  @BeforeEach
  public void init() {
    this.jpaPlanRepositoryMock = mock(JpaPlanRepository.class);
    this.planRepository = new PlanRepositoryImpl(this.jpaPlanRepositoryMock);
  }

  @Test
  public void T_PLN_040_findAll_delegaEnJpaPlanRepository() {
    // given
    List<Plan> planes = List.of(plan(PLAN_ID), plan(OTHER_PLAN_ID));
    when(this.jpaPlanRepositoryMock.findAll()).thenReturn(planes);

    // when
    List<Plan> result = this.planRepository.findAll();

    // then
    assertThat(result, is(sameInstance(planes)));
    verify(this.jpaPlanRepositoryMock, times(1)).findAll();
  }

  @Test
  public void T_PLN_041_findById_delegaEnJpaPlanRepository() {
    // given
    Plan plan = plan(PLAN_ID);
    when(this.jpaPlanRepositoryMock.findById(PLAN_ID)).thenReturn(Optional.of(plan));

    // when
    Optional<Plan> result = this.planRepository.findById(PLAN_ID);

    // then
    assertThat(result.orElseThrow(), is(sameInstance(plan)));
    verify(this.jpaPlanRepositoryMock, times(1)).findById(PLAN_ID);
  }

  @Test
  public void T_PLN_042_findByAdministratorId_delegaEnJpaPlanRepository() {
    // given
    List<Plan> planes = List.of(plan(PLAN_ID));
    when(this.jpaPlanRepositoryMock.findByAdministratorId(7L)).thenReturn(planes);

    // when
    List<Plan> result = this.planRepository.findByAdministratorId(7L);

    // then
    assertThat(result, is(sameInstance(planes)));
    verify(this.jpaPlanRepositoryMock, times(1)).findByAdministratorId(7L);
  }

  @Test
  public void T_PLN_043_findByShortCode_delegaEnJpaPlanRepository() {
    // given
    Plan plan = plan(PLAN_ID);
    when(this.jpaPlanRepositoryMock.findByShortCode("ABCD1234")).thenReturn(Optional.of(plan));

    // when
    Optional<Plan> result = this.planRepository.findByShortCode("ABCD1234");

    // then
    assertThat(result.orElseThrow(), is(sameInstance(plan)));
    verify(this.jpaPlanRepositoryMock, times(1)).findByShortCode("ABCD1234");
  }

  @Test
  public void T_PLN_044_existsByShortCode_delegaEnJpaPlanRepository() {
    // given
    when(this.jpaPlanRepositoryMock.existsByShortCode("ABCD1234")).thenReturn(true);

    // when
    boolean result = this.planRepository.existsByShortCode("ABCD1234");

    // then
    assertTrue(result);
    verify(this.jpaPlanRepositoryMock, times(1)).existsByShortCode("ABCD1234");
  }

  @Test
  public void T_PLN_045_save_devuelveLoQueJpaPlanRepositoryGuardo() {
    // given
    Plan plan = plan(PLAN_ID);
    Plan managed = plan(PLAN_ID);
    when(this.jpaPlanRepositoryMock.save(plan)).thenReturn(managed);

    // when
    Plan result = this.planRepository.save(plan);

    // then
    assertThat(result, is(sameInstance(managed)));
    verify(this.jpaPlanRepositoryMock, times(1)).save(plan);
  }

  @Test
  public void T_PLN_046_deleteById_delegaEnJpaPlanRepository() {
    // when
    this.planRepository.deleteById(PLAN_ID);

    // then
    verify(this.jpaPlanRepositoryMock, times(1)).deleteById(PLAN_ID);
  }

  @Test
  public void U13_countParticipantsByPlanIds_delegaEnJpaPlanRepository() {
    // given
    List<UUID> planIds = List.of(PLAN_ID, OTHER_PLAN_ID);
    List<PlanParticipantCount> counts = List.of(
      new PlanParticipantCount(PLAN_ID, 4L),
      new PlanParticipantCount(OTHER_PLAN_ID, 0L)
    );
    when(this.jpaPlanRepositoryMock.countParticipantsByPlanIds(planIds)).thenReturn(counts);

    // when
    List<PlanParticipantCount> result = this.planRepository.countParticipantsByPlanIds(planIds);

    // then
    assertThat(result, is(sameInstance(counts)));
    verify(this.jpaPlanRepositoryMock, times(1)).countParticipantsByPlanIds(planIds);
  }

  private Plan plan(UUID id) {
    Plan plan = new Plan();
    plan.setId(id);
    plan.setName("Viaje a Bariloche");
    return plan;
  }
}
