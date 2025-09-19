package com.estapar.parking.application.request;

import com.estapar.parking.application.domain.model.ParkingSession;

import java.math.BigDecimal;

public final class SessionResultMapper {
    private SessionResultMapper() {}

    public static SessionResponse from(ParkingSession e) {
        return new SessionResponse(
                e.getId(),
                e.getPlate(),
                e.getGarage().getCode(),
                e.getStatus().toString(),
                e.getEntryTime(),
                e.getParkedTime(),
                e.getExitTime(),
                BigDecimal.ONE,
                e.getTotalAmount()
        );
    }
}
