package com.estapar.parking.application.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
@AllArgsConstructor @Builder
@Entity @Table(name = "parking_session")
public class ParkingSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=12)
    private String plate;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="garage_id", nullable=false)
    private Garage garage;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "sector_id")
    private GarageSector sector;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=10)
    private SessionStatus status;

    @Column(name="entry_time")
    private LocalDateTime entryTime;

    @Column(name="parked_time")
    private LocalDateTime parkedTime;

    @Column(name="exit_time")
    private LocalDateTime exitTime;

    @Column(name="effective_hourly_price", precision=15, scale=2)
    private BigDecimal effectiveHourlyPrice;

    @Column(name="total_amount", precision=15, scale=2)
    private BigDecimal totalAmount;
}
