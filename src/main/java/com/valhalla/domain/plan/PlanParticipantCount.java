package com.valhalla.domain.plan;

import java.util.UUID;

/**
 * [TOP] Conteo de participantes por plan. Top-level para poder usarlo como constructor
 * expression en JPQL, igual que RatingSummary.
 *
 * @param planId plan contado
 * @param count cantidad de participantes
 */
public record PlanParticipantCount(UUID planId, Long count) {}
