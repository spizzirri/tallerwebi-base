package com.valhalla.domain.rating;

import java.util.UUID;

/**
 * [CAL] Agregado publico de calificaciones de un plan: promedio y cantidad. Es lo unico que sale al
 * HTML (exhibicion privada, ver 16-CAL.md). Top-level para poder usarlo como constructor
 * expression en JPQL.
 */
public record RatingSummary(UUID planId, Double average, Long count) {}
