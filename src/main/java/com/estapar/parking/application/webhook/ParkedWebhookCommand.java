package com.estapar.parking.application.webhook;

import com.estapar.parking.application.error.NotOpenSessionException;
import com.estapar.parking.application.error.SpotNotFoundByCoordinatesException;
import com.estapar.parking.application.request.EventTypeRequest;
import com.estapar.parking.application.request.SessionResponse;
import com.estapar.parking.application.request.WebhookRequest;
import com.estapar.parking.application.request.ParkedRequest;
import com.estapar.parking.application.service.*;
import com.estapar.parking.application.domain.model.SessionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@WebhookQualifier(EventTypeRequest.PARKED)
public class ParkedWebhookCommand implements WebhookCommand {

    private final ParkingService parkingService;
    private final SpotService spotService;
    private final GarageSectorService GarageSectorServiceImpl;
    private static final double COORD_EPS = 0.0005d;

    @Override
    public SessionResponse handle(WebhookRequest req) {
        if (!(req instanceof ParkedRequest parked)) {
            throw new IllegalArgumentException("Expected ParkedRequest for PARKED event");
        }

        var session = parkingService
                .findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc(
                        parked.licensePlate(),
                        List.of(SessionStatus.ENTRY)
                )
                .orElseThrow(() -> new NotOpenSessionException(parked.licensePlate()));

        var garageCode = session.getGarage().getCode();

        var sectorCode =  spotService.findNearestSectorCodeByGarageAndCoords(garageCode, parked.lat(), parked.lng(), COORD_EPS)
                   .orElseThrow(() -> new SpotNotFoundByCoordinatesException(garageCode, parked.lat(), parked.lng()));

        var allocation = GarageSectorServiceImpl.allocateOneSpot(garageCode, sectorCode);

        var result = parkingService.parked(
                parked.licensePlate(),
                allocation.sector()
        );

        log.info("PARKED done plate={} sessionId={}", parked.licensePlate(), result.id());
        return result;
    }
}
