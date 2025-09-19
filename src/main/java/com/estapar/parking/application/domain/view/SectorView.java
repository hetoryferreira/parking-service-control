package com.estapar.parking.application.domain.view;

public record SectorView(
        Long id,
        String code,
        String name,
        Integer maxCapacity,
        Integer occupied,
        boolean closed,
        boolean full
) {}