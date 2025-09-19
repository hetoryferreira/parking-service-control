package com.estapar.parking.application.error;


public class SpotNotFoundByCoordinatesException extends NotFoundException {
    public SpotNotFoundByCoordinatesException(String garageCode, Double lat, Double lng) {
        super("SPOT_NOT_FOUND_NEAR_COORDS",
                "No spot found near coordinates (" + lat + "," + lng + ") in garage " + garageCode);
    }
}