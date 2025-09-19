package com.estapar.parking.application.error;

public class SectorClosedException extends ConflictException {
    public SectorClosedException(String sectorCode) {
        super("SECTOR_CLOSED", "Sector is closed: " + sectorCode);
    }
}