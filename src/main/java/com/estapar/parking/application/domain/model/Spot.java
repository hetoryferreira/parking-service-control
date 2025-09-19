package com.estapar.parking.application.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor
@AllArgsConstructor @Builder
@Entity @Table(name = "spot")
public class Spot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="garage_id", nullable=false)
    private Garage garage;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="sector_id", nullable=false)
    private GarageSector sector;

    private Double lat;
    private Double lng;

    @Column(nullable=false)
    private boolean occupied;
}
