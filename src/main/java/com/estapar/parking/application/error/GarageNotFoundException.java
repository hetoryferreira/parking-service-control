package com.estapar.parking.application.error;


public class GarageNotFoundException extends NotFoundException {
    public GarageNotFoundException(String garageCode) {
        super("GARAGE_NOT_FOUND", "Garage not found: " + garageCode);
    }
}