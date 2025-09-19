package com.estapar.parking.application.error;

public class SectorRequiredOnParkedException extends BadRequestException {
    public SectorRequiredOnParkedException() {
        super("SECTOR_REQUIRED_ON_PARKED", "Sector code is required on PARKED event");
    }
}