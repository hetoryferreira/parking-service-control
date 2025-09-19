package com.estapar.parking.application.service;

import com.estapar.parking.application.domain.repo.GarageSectorRepository;
import com.estapar.parking.application.error.NotFoundException;
import com.estapar.parking.application.error.SectorClosedException;
import com.estapar.parking.application.domain.view.AllocationView;
import com.estapar.parking.application.request.SectorDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GarageSectorServiceImpl implements GarageSectorService {

    private final GarageService garageService;
    private final SpotServiceImpl spotService;
    private final GarageSectorRepository garageSectorRepository;

    @Transactional
    public AllocationView allocateOneSpot(String garageCode, String sectorCode) {
        MDC.put("garageCode", garageCode);
        log.info("Allocating one spot. garageCode={} sectorCode={}", garageCode, sectorCode);

        // lock garage (service)
        var garage = garageService.getAndLockGarageByCode(garageCode)
                .orElseThrow(() -> new NotFoundException("Garage not found , garageCode: ", garageCode));

        // lock sector (repo)
        var sector = garageSectorRepository.lockByGarageAndCode(garage.getId(), sectorCode)
                .orElseThrow(() -> new NotFoundException("Sector not found: sectorCode: ", sectorCode));

        if (sector.isClosed()) {
            log.warn("Sector is closed. garageCode={} sectorCode={}", garageCode, sectorCode);
            throw new SectorClosedException(sectorCode);
        }
        if (sector.isFull()) {
            log.warn("Sector is full. garageCode={} sectorCode={}", garageCode, sectorCode);
            throw new NotFoundException("Sector Full: ", sectorCode);
        }

        var spot = spotService.lockFirstFreeAndMarkOccupied(sector.getId());

        sector.incOccupied();
        if (sector.isFull()) {
            sector.setClosed(true);
        }
        garageSectorRepository.save(sector);
        MDC.remove("garageCode");
        return new AllocationView(
                sector
        );

    }

    @Transactional(readOnly = true)
    public List<SectorDetails> findByGarageCode(String garageCode) {
        MDC.put("garageCode", garageCode);
        log.info("Fetching sectors by garage code. garageCode={}", garageCode);

        var entities = garageSectorRepository.findByGarageCode(garageCode);
        if (entities == null || entities.isEmpty()) {
            log.warn("No sectors found for garageCode={}", garageCode);
            return Collections.emptyList();
        }

        var out = entities.stream()
                .map(s -> new SectorDetails(
                        s.getGarage().getId(),
                        s.getGarage().getCode(),
                        s.getGarage().getBasePrice(),
                        s.getId(),
                        s.getCode(),
                        s.getName(),
                        s.getMaxCapacity(),
                        s.getOccupied(),
                        s.isClosed()
                ))
                .toList();
        MDC.remove("garageCode");
        return out;
    }
}
