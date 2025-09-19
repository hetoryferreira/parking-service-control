package com.estapar.parking.application.request;
import java.math.BigDecimal;
import java.util.List;

public record GarageConfigResponse(
        List<SectorItem> garage,
        List<SpotItem> spots
) {
    public record SectorItem(String sector, BigDecimal basePrice, Integer max_capacity) {}
    public record SpotItem(Long id, String sector, Double lat, Double lng) {}
}