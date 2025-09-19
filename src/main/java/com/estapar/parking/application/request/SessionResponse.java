package com.estapar.parking.application.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessionResponse(
        Long id,
        String plate,
        String garageCode,
        String status,
        LocalDateTime entryTime,
        LocalDateTime parkedTime,
        LocalDateTime exitTime,
        BigDecimal effectiveHourlyPrice,
        BigDecimal totalAmount
) {}
