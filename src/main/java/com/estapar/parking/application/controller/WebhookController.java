package com.estapar.parking.application.controller;

import com.estapar.parking.application.error.SectorRequiredOnParkedException;
import com.estapar.parking.application.request.WebhookRequest;
import com.estapar.parking.application.webhook.WebhookCommandFactory;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import static com.estapar.parking.application.support.MdcUtils.withMdc;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/webhook")
public class WebhookController {

    private final WebhookCommandFactory factory;

    @Operation(summary = "Webhook - ENTRY,PARKED,EXIT")
    @PostMapping
    public ResponseEntity<Void> receive(@Valid @RequestBody WebhookRequest req) {
        return withMdc(
                Map.of("plate", req.licensePlate(), "event", req.eventType().name(), "endpoint", "/webhook"),
                () -> {
                    log.info("WEBHOOK processing ->  status={}  licensePlate={} ",  req.eventType().name(), req.licensePlate());
                    var command = factory.get(req.eventType());
                    try {
                        var session = command.handle(req);
                    } catch (SectorRequiredOnParkedException e) {
                        throw new RuntimeException(e);
                    }
                    log.info("WEBHOOK processed  status={} licensePlate={}", req.eventType().name(), req.licensePlate());
                    return ResponseEntity.ok().build();
                }
        );
    }
}