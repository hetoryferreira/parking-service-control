package com.estapar.parking.application.webhook;

import com.estapar.parking.application.request.EventTypeRequest;
import com.estapar.parking.application.request.SessionResponse;
import com.estapar.parking.application.request.WebhookRequest;
import com.estapar.parking.application.service.GarageService;
import com.estapar.parking.application.service.ParkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@WebhookQualifier(EventTypeRequest.ENTRY)
public class EntryWebhookCommand implements WebhookCommand {

    private final GarageService garageServiceImpl;
    private final ParkingService parkingService;

    @Transactional
    public SessionResponse handle(WebhookRequest req) {

        //ENTRY: apenas valida/fecha a garagem (global) e incrementa ocupação da GARAGE.
        var garage = garageServiceImpl.reserveEntry(req.garageCode());

        //calcula preço dinâmico na ENTRADA (com base na garagem)
        var effectivePrice = parkingService.computeDynamicPriceAtEntry(garage);

        // cria sessão como ENTRY
        return parkingService.entryWithoutSector(
                req.licensePlate(),
                garage,
                effectivePrice,
                garage.getCreatedAt()
        );
    }
}
