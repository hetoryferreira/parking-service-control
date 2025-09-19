package com.estapar.parking.application.error;

public class SectorNotFoundException extends NotFoundException {
    public SectorNotFoundException(String garageCode, String sectorCode) {
        super("SECTOR_NOT_FOUND", "Sector not found in garage " + garageCode + ": " + sectorCode);
    }
}