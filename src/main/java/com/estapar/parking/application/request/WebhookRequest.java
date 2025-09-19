package com.estapar.parking.application.request;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Base webhook request (polymorphic)")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "event_type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EntryRequest.class,  name = "ENTRY"),
        @JsonSubTypes.Type(value = ParkedRequest.class, name = "PARKED"),
        @JsonSubTypes.Type(value = ExitRequest.class,   name = "EXIT")
})
public sealed interface WebhookRequest permits EntryRequest, ParkedRequest, ExitRequest {
    @NotBlank @JsonProperty("license_plate") String licensePlate();
    @NotBlank @JsonProperty("garage_code")   String garageCode();

    @JsonProperty("event_type") EventTypeRequest eventType();
}
