package com.estapar.parking.application.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Request to close the session")
public record ExitRequest(
        @JsonProperty("license_plate") String licensePlate,
        @JsonProperty("exit_time")     OffsetDateTime exitTime,
        @JsonProperty("garage_code")   String garageCode,
        @JsonProperty("event_type")    EventTypeRequest eventType
) implements WebhookRequest {}
