package com.estapar.parking.application.error;

public class SectorFullException extends ConflictException {
    public SectorFullException(String sectorCode) {
        super("SECTOR_FULL", "Sector is full: " + sectorCode);
    }
}