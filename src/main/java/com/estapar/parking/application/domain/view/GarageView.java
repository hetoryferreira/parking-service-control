package com.estapar.parking.application.domain.view;


import java.math.BigDecimal;

public record GarageView(
        Long id,
        String code,
        String name,
        BigDecimal basePrice,
        Integer maxCapacity,
        Integer occupied,
        boolean full
) {}