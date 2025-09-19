package com.estapar.parking.application.error;

public class GarageFullException extends ConflictException {
    public GarageFullException(Long garageId) {
        super("GARAGE_FULL", "Garage is full: " + garageId);
    }
}