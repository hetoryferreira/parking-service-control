package com.estapar.parking.application.request;

import java.math.BigDecimal;

public record SectorDetails(
        Long garageId,
        String garageCode,
        BigDecimal garageBasePrice,
        Long sectorId,
        String sectorCode,
        String sectorName,
        Integer maxCapacity,
        Integer occupied,
        boolean closed
) {}
