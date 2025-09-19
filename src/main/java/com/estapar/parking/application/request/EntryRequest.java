package com.estapar.parking.application.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;


import java.time.OffsetDateTime;

@Schema(description = "Request to start a parking session")
public record EntryRequest(
        @Schema(example = "ZUL0001")
        @NotBlank @JsonProperty("license_plate") String licensePlate,

        @Schema(example = "G1")
        @NotBlank @JsonProperty("garage_code") String garageCode,

        @Schema(example = "2025-09-17T20:26:56Z")
        @NotNull  @JsonProperty("entry_time") OffsetDateTime entryTime,

        @Schema(example = "ENTRY")
        @NotNull  @JsonProperty("event_type") EventTypeRequest eventType
) implements WebhookRequest { }