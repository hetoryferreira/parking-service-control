package com.estapar.parking.application.domain.repo;

import com.estapar.parking.application.domain.model.ParkingSession;
import com.estapar.parking.application.domain.model.SessionStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {
    @EntityGraph(attributePaths = "garage")
    Optional<ParkingSession> findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc(String plate, List<SessionStatus> statuses);
    @Query("""
           select coalesce(sum(p.totalAmount), 0)
           from ParkingSession p
           where p.exitTime between :start and :end
           """)
    BigDecimal sumRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        select coalesce(sum(ps.totalAmount), 0)
        from ParkingSession ps
          join ps.garage g
          join ps.sector s
        where g.code = :garageCode
          and s.code = :sectorCode
          and ps.status = :exitStatus
          and ps.exitTime >= :from and ps.exitTime < :to
        """)
    BigDecimal sumRevenueByGarageAndSectorAndExitBetween(
            @Param("garageCode") String garageCode,
            @Param("sectorCode") String sectorCode,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("exitStatus") SessionStatus exitStatus
    );
}
