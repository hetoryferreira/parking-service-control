package com.estapar.parking.application.service;

import com.estapar.parking.application.domain.model.Spot;
import com.estapar.parking.application.domain.repo.SpotRepository;
import com.estapar.parking.application.service.SpotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotServiceImpl implements SpotService {

    private final SpotRepository spotRepo;

    @Transactional
    public Spot lockFirstFreeAndMarkOccupied(Long sectorId) {
        log.info("Locking first free spot. sectorId={}", sectorId);
        var spotOpt = spotRepo.lockFirstFreeBySector(sectorId);
        var spot = spotOpt.get();
        spot.setOccupied(true);
        spotRepo.save(spot);
        log.info("Spot occupied. spotId={} sectorId={}", spot.getId(), sectorId);
        return spot;
    }

    public Optional<String> findNearestSectorCodeByGarageAndCoords(
            String garageCode, double lat, double lng, double eps) {

        log.info("Find findNearestSectorCodeByGarageAndCoords. garageCode={}", garageCode);

        return spotRepo.findNearestSectorCodeByGarageAndCoords(garageCode, lat, lng, eps);
    }

    @Transactional(readOnly = true)
    public List<Spot> findByGarageCodeWithSector(String garageCode) {
        log.info("Find findByGarageCodeWithSector. garageCode={}", garageCode);
        return spotRepo.findByGarageCodeWithSector(garageCode);
    }

    @Transactional
    public void save(Spot spot){

        if (spot == null) {
            log.warn("Attempted to save a null Spot. Ignoring.");
            return;
        }
        log.info("saveSpot. sectorId={}", spot.getSector().getId());

        spotRepo.save(spot);
    }
}
