package com.estapar.parking.application.controller;

import com.estapar.parking.application.request.RevenueResponse;
import com.estapar.parking.application.service.ParkingService;
import io.swagger.v3.oas.annotations.Operation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

 import java.math.BigDecimal;
import java.time.*;
import java.util.Map;
import static com.estapar.parking.application.support.MdcUtils.withMdc;
import jakarta.validation.constraints.Pattern;

@RequiredArgsConstructor
@Slf4j
@Validated
@RestController
public class RevenueController {

    private final ParkingService parkingService;

    @Operation(summary = "Revenue by date (yyyy-MM-dd) and sector; ignores time components")
    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenue(
            @RequestParam
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "date must be in format yyyy-MM-dd")
            String date,
            @RequestParam String sector,
            @RequestParam(name = "garage_code", defaultValue = "1") String garageCode
    ) {

        return withMdc(
                Map.of("date", date, "sector", sector, "endpoint", "/revenue"),
                () -> {

                    log.info("GET /revenue started. date={} sector={} garageCode={}", date, sector, garageCode);

                    var amount = parkingService
                            .sumRevenueByGarageAndSectorAndExitBetween(garageCode, sector, date);

                    if (amount == null) amount = BigDecimal.ZERO;

                    log.info("GET /revenue finish. date={} sector={} garageCode={}", date, sector, garageCode);

                    return ResponseEntity.ok(new RevenueResponse(amount.setScale(2), "BRL", Instant.now()));
                }
        );
    }
}










