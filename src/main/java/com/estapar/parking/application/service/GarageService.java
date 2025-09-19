package com.estapar.parking.application.service;

import com.estapar.parking.application.domain.model.Garage;

import java.util.Optional;

public interface GarageService {

    public Garage reserveEntry(String garageCode) ;

    public Optional<Garage> getAndLockGarageByCode(String garageCode);
}
