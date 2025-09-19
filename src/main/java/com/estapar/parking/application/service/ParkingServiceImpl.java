package com.estapar.parking.application.service;

import com.estapar.parking.application.domain.model.Garage;
import com.estapar.parking.application.domain.model.GarageSector;
import com.estapar.parking.application.domain.model.ParkingSession;
import com.estapar.parking.application.domain.model.SessionStatus;
import com.estapar.parking.application.error.NotOpenSessionException;
import com.estapar.parking.application.error.OpenSessionExistsException;
import com.estapar.parking.application.request.SessionResponse;
import com.estapar.parking.application.request.SessionResultMapper;
import com.estapar.parking.application.domain.repo.ParkingSessionRepository;
import com.estapar.parking.application.service.ParkingService;
import com.estapar.parking.application.service.PriceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

import java.math.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
@AllArgsConstructor
@Slf4j
@Service
public class ParkingServiceImpl implements ParkingService {
    private final ParkingSessionRepository parkingSessionRepository;
    private final PriceService pricingService;

    public BigDecimal computeDynamicPriceAtEntry(Garage garage) {
        return pricingService.effectiveHourlyPrice(garage);
    }

    @Transactional
    public SessionResponse entryWithoutSector(String licencePlate, Garage garage, BigDecimal effectivePrice, Instant entryAt) {

        MDC.put("plate", licencePlate);
        log.info("entryWithoutSector started. licencePlate={}", licencePlate);

        parkingSessionRepository.findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc(licencePlate, List.of(SessionStatus.ENTRY, SessionStatus.PARKED))
                .ifPresent(s -> {
                    throw new OpenSessionExistsException(licencePlate);
                });

        var s = ParkingSession.builder()
                .plate(licencePlate)
                .garage(garage)
                .status(SessionStatus.ENTRY)
                .entryTime(toLdt(entryAt))
                .effectiveHourlyPrice(effectivePrice)
                .build();
        var saved = parkingSessionRepository.save(s);

        log.info("entryWithoutSector finish. licencePlate={}", licencePlate);
        MDC.remove("plate");

        return SessionResultMapper.from(saved);
    }

    @Transactional
    public SessionResponse parked(String licencePlate, GarageSector sector) {
        log.info("parked finish. licencePlate={}", licencePlate);
        MDC.put("plate",licencePlate);
        var s = parkingSessionRepository.findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc(
                licencePlate, java.util.List.of(SessionStatus.ENTRY)
        ).orElseThrow(() -> new NotOpenSessionException(licencePlate));

        s.setStatus(SessionStatus.PARKED);
        s.setParkedTime(toLdt(Instant.now()));
        s.setSector(sector);
        var saved = parkingSessionRepository.save(s);

        var outSessionResult = SessionResultMapper.from(saved);

        log.info("PARKED done id={} amount={}", saved.getId(), saved.getTotalAmount());
        MDC.remove("plate");

        return  outSessionResult;
    }

    private static LocalDateTime toLdt(Instant i) {
        return i == null ? LocalDateTime.now() : LocalDateTime.ofInstant(i, ZoneOffset.UTC);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SessionResponse exit(String licencePlate, OffsetDateTime exitTime) {
        log.info("exit start. licencePlate={}", licencePlate);
        MDC.put("plate", licencePlate);

        String plate = normalizeLicencePlate(licencePlate);
        log.info("Webhook EXIT, plate {}" , plate);
        var s = parkingSessionRepository.findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc(plate, List.of(SessionStatus.ENTRY, SessionStatus.PARKED))
                .orElseThrow(() -> new NotOpenSessionException(licencePlate));
        s.setStatus(SessionStatus.EXIT);
        s.setExitTime(exitTime.toLocalDateTime());
        s.setTotalAmount(pricingService.calculateAmount(s));


        var saved = parkingSessionRepository.save(s);
        var outSessionResult = SessionResultMapper.from(saved);

        log.info("EXIT done id={} amount={}", saved.getId(), saved.getTotalAmount());
        MDC.remove("plate");
        return outSessionResult;
    }

    @Transactional(readOnly = true)
    public BigDecimal sumRevenueByGarageAndSectorAndExitBetween(String garageCode, String sectorCode, String date) {
        final LocalDate ld;
        try {
            ld = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            try {
                throw new BadRequestException("Invalid date. Expected yyyy-MM-dd.");
            } catch (BadRequestException ex) {
                throw new RuntimeException(ex);
            }
        }

        // sempre 1 dia: [00:00, nextDay 00:00)
        LocalDateTime from = ld.atStartOfDay();
        LocalDateTime to   = from.plusDays(1);
        return parkingSessionRepository.sumRevenueByGarageAndSectorAndExitBetween(garageCode, sectorCode, from, to,SessionStatus.EXIT);
    }

    @Transactional(readOnly = true)
    public Optional<ParkingSession> findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc(
            String plate,
            List<SessionStatus> statuses
    ) {
        // normaliza placa
        final String normalizedPlate = plate == null ? null : plate.trim().toUpperCase();

        if (normalizedPlate == null || normalizedPlate.isBlank()) {
            log.warn("Lookup aborted: empty/null plate received.");
            return Optional.empty();
        }

        final List<SessionStatus> effectiveStatuses =
                (statuses == null || statuses.isEmpty())
                        ? List.of(SessionStatus.ENTRY, SessionStatus.PARKED)
                        : List.copyOf(statuses);

        MDC.put("plate", normalizedPlate);
        long t0 = System.nanoTime();
        try {
            log.info("Looking up last session. plate={} statuses={}", normalizedPlate, effectiveStatuses);
            var result = parkingSessionRepository
                    .findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc(normalizedPlate, effectiveStatuses);
            log.info("Lookup finished. plate={} found={} elapsedMs={}",
                    normalizedPlate, result.isPresent(), (System.nanoTime() - t0) / 1_000_000);
            return result;
        } finally {
            MDC.remove("plate");
        }
    }

    private static String normalizeLicencePlate(String plate) {
        return plate == null ? null : plate.trim().toUpperCase();
    }
}