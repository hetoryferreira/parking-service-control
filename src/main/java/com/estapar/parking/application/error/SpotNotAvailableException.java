package com.estapar.parking.application.error;

public class SpotNotAvailableException extends ConflictException {
    public SpotNotAvailableException(String sectorCode) {
        super("SPOT_NOT_AVAILABLE", "No free spot available in sector: " + sectorCode);
    }
}