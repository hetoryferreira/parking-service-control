package com.estapar.parking.application.webhook;

import com.estapar.parking.application.request.*;
import com.estapar.parking.application.service.ParkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@WebhookQualifier(EventTypeRequest.EXIT)
@RequiredArgsConstructor
public class ExitWebhookCommand implements WebhookCommand {
    private final ParkingService service;

    @Transactional
    public SessionResponse handle(@Valid WebhookRequest req) {

        if (!(req instanceof ExitRequest exit)) {
            throw new IllegalArgumentException("Expected ExitRequest for EXIT event");
        }

        log.info("EXIT command: plate={}", req.licensePlate());
        return service.exit(req.licensePlate(),exit.exitTime());
    }
}
