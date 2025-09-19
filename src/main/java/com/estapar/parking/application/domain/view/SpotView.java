package com.estapar.parking.application.domain.view;

public record SpotView(
        Long id,
        String sectorCode,
        Double lat,
        Double lng,
        boolean occupied
) {}
