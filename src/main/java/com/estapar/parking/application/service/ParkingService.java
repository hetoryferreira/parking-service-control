package com.estapar.parking.application.service;

import com.estapar.parking.application.domain.model.Garage;
import com.estapar.parking.application.domain.model.GarageSector;
import com.estapar.parking.application.domain.model.ParkingSession;
import com.estapar.parking.application.domain.model.SessionStatus;
import com.estapar.parking.application.request.SessionResponse;
import java.math.BigDecimal;
import java.time.*;

import java.util.List;
import java.util.Optional;

public interface ParkingService {

    public BigDecimal computeDynamicPriceAtEntry(Garage garage) ;

    public SessionResponse entryWithoutSector(String licencePlate, Garage garage, BigDecimal effectivePrice, Instant entryAt);

    public SessionResponse parked(String licencePlate, GarageSector sector);

    public SessionResponse exit(String licencePlate, OffsetDateTime exitTime);

    public BigDecimal sumRevenueByGarageAndSectorAndExitBetween(String garageCode, String sectorCode, String date) ;
    public Optional<ParkingSession> findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc(String plate, List<SessionStatus> statuses);

}
