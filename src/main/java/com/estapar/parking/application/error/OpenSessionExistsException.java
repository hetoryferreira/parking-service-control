package com.estapar.parking.application.error;

public class OpenSessionExistsException extends ConflictException {
    public OpenSessionExistsException(String plate) {
        super("OPEN_SESSION_EXISTS", "There is an open session for plate: " + plate);
    }
}