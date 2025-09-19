package com.estapar.parking.application.webhook;

import com.estapar.parking.application.error.SectorRequiredOnParkedException;
import com.estapar.parking.application.request.SessionResponse;
import com.estapar.parking.application.request.WebhookRequest;
import jakarta.validation.Valid;

public interface WebhookCommand {
    SessionResponse handle(@Valid WebhookRequest req) throws SectorRequiredOnParkedException;
}
