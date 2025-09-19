package com.estapar.parking.application.error;

public class GarageCodeRequiredException extends BadRequestException {
    public GarageCodeRequiredException() {
        super("GARAGE_CODE_REQUIRED", "Garage code is required");
    }
}