package com.estapar.parking.application.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to mark as PARKED")
public record ParkedRequest(
        @JsonProperty("license_plate") String licensePlate,
        @JsonProperty("lat")           Double lat,
        @JsonProperty("lng")           Double lng,
        @JsonProperty("garage_code")   String garageCode,
        @JsonProperty("event_type")    EventTypeRequest eventType
) implements WebhookRequest {}
