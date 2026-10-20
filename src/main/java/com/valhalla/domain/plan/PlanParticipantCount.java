package com.valhalla.domain.plan;

import java.util.UUID;

/**
 * [TOP] Conteo de participantes por plan. Top-level para poder usarlo como constructor
 * expression en JPQL, igual que RatingSummary.
 */
public record PlanParticipantCount(UUID planId, Long count) {}
