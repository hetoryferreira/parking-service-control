package com.estapar.parking.application.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor
@AllArgsConstructor @Builder
@Entity @Table(name = "garage_sector",
        uniqueConstraints = @UniqueConstraint(name="uk_sector_garage_code", columnNames = {"garage_id","code"}))
public class GarageSector {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="garage_id", nullable=false)
    private Garage garage;

    @Column(nullable=false, length=32)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name="max_capacity", nullable=false)
    private Integer maxCapacity;

    @Column(nullable=false)
    private Integer occupied;

    @Column(name="is_closed", nullable=false)
    private boolean closed;

    @Column(name="created_at", updatable=false, insertable=false)
    private Instant createdAt;

    @Column(name="updated_at", insertable=false)
    private Instant updatedAt;

    public boolean isFull() { return occupied >= maxCapacity; }

    public void incOccupied() { this.occupied++; }
    public void decOccupied() { this.occupied = Math.max(0, this.occupied - 1); }
}
