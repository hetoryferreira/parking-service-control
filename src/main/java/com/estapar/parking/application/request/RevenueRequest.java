package com.estapar.parking.application.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to calculate revenue between two instants (ISO-8601)")
public record RevenueRequest(
        @NotBlank @Schema(example = "2023-01-01T00:00:00") String from,
        @NotBlank @Schema(example = "2023-01-01T23:59:59") String to
) {}
