package com.estapar.parking.application.request;

import java.math.BigDecimal;
import java.time.Instant;

public record RevenueResponse(
        BigDecimal amount,
        String currency,
        Instant timestamp
) {}