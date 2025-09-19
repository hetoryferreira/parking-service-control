package com.estapar.parking.application.service;

import com.estapar.parking.application.domain.model.Spot;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SpotService {

    @Transactional
    Spot lockFirstFreeAndMarkOccupied(Long sectorId);

    Optional<String> findNearestSectorCodeByGarageAndCoords(
            String garageCode,
            double lat,
            double lng,
            double eps);
    List<Spot> findByGarageCodeWithSector(String garageCode);

    void save(Spot spot);
}
