package com.estapar.parking.application.error;

public class NotOpenSessionException extends ConflictException {
    public NotOpenSessionException(String plate) {
        super("NOT_OPEN_SESSION", "There is not open session for plate: " + plate);
    }
}