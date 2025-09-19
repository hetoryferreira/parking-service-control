package com.estapar.parking.application.service;

import com.estapar.parking.application.domain.model.Garage;
import com.estapar.parking.application.domain.repo.GarageRepository;
import com.estapar.parking.application.error.GarageFullException;
import com.estapar.parking.application.error.NotFoundException;
import com.estapar.parking.application.service.GarageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GarageServiceImpl implements GarageService {

    private final GarageRepository garageRepo;

    @Transactional
    public Garage reserveEntry(String garageCode) {
        MDC.remove("garageCode");
        log.info("ENTRY reserveEntry started. garageCode={}", garageCode);
        var garage = garageRepo.lockByCode(garageCode)
                .orElseThrow(() -> new NotFoundException("Garage not found , garageCode: ", garageCode));
        if (garage.isFull()) throw new GarageFullException(garage.getId());
        garage.incOccupied();
        garageRepo.save(garage);
        log.info("ENTRY reservation completed. garageId={} occupied={}", garage.getId(), garage.getOccupied());
        MDC.remove("garageCode");
        return garage;
    }

    @Transactional
    public Optional<Garage> getAndLockGarageByCode(String garageCode) {
        log.info("Locking garage by code. garageCode={}", garageCode);
        return garageRepo.lockByCode(garageCode);
    }
}
