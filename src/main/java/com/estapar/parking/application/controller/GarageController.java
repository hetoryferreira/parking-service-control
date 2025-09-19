package com.estapar.parking.application.controller;

import com.estapar.parking.application.request.GarageConfigResponse;
import com.estapar.parking.application.service.GarageSectorService;
import com.estapar.parking.application.service.SpotServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import static com.estapar.parking.application.support.MdcUtils.withMdc;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GarageController {

    private final GarageSectorService garageSectorService;
    private final SpotServiceImpl spotService;

    @Operation(summary = "Configuração da Garagem (setores e vagas)")
    @GetMapping("/garage")
    @Transactional(readOnly = true)
    public ResponseEntity<GarageConfigResponse> getGarageConfig(
            @RequestParam(name = "garage_code", defaultValue = "1") String garageCode
    ) {

        return withMdc(
                Map.of("garageCode", garageCode, "endpoint", "/garage"),
                () -> {
                    log.info("GET /garage started. garageCode={}", garageCode);

                    var sectors = garageSectorService.findByGarageCode(garageCode);
                    var spots   = spotService.findByGarageCodeWithSector(garageCode);

                    var sectorItems = sectors.stream()
                            .map(s -> new GarageConfigResponse.SectorItem(
                                    s.garageCode(),
                                    s.garageBasePrice(),
                                    s.maxCapacity()
                            ))
                            .toList();

                    var spotItems = spots.stream()
                            .map(sp -> new GarageConfigResponse.SpotItem(
                                    sp.getId(),
                                    sp.getSector().getCode(),
                                    sp.getLat(),
                                    sp.getLng()
                            ))
                            .toList();
                    log.info("GET /garage finish. garageCode={}", garageCode);

                    return ResponseEntity.ok(new GarageConfigResponse(sectorItems, spotItems));
                }
        );
    }
}