package com.estapar.parking.application.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor
@AllArgsConstructor @Builder
@Entity @Table(name = "garage")
public class Garage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=32)
    private String code;

    private String name;

    @Column(name="base_price", nullable=false, precision=15, scale=2)
    private BigDecimal basePrice;

    @Column(name="max_capacity", nullable=false)
    private Integer maxCapacity;

    @Column(nullable=false)
    private Integer occupied;

    @Column(name="created_at", updatable=false, insertable=false)
    private Instant createdAt;

    @Column(name="updated_at", insertable=false)
    private Instant updatedAt;

    public boolean isFull() { return occupied >= maxCapacity; }
    public void incOccupied() { this.occupied++; }
    public void decOccupied() { this.occupied = Math.max(0, this.occupied - 1); }
}
