package com.estapar.parking.application.domain.repo;

import com.estapar.parking.application.domain.model.Spot;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpotRepository extends JpaRepository<Spot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select sp from Spot sp
           where sp.sector.id = :sectorId and sp.occupied = false
           order by sp.id asc
           """)
    List<Spot> lockFirstFreeBySectorPage(@Param("sectorId") Long sectorId, PageRequest pageable);

    default Optional<Spot> lockFirstFreeBySector(Long sectorId) {
        return lockFirstFreeBySectorPage(sectorId, PageRequest.of(0, 1)).stream().findFirst();
    }

    @Query(value = """
    SELECT sec.code
    FROM spot s
    JOIN garage g       ON g.id = s.garage_id
    JOIN garage_sector sec ON sec.id = s.sector_id
    WHERE g.code = :garageCode
      AND s.occupied = FALSE
      AND ABS(s.lat - :lat) <= :eps
      AND ABS(s.lng - :lng) <= :eps
    ORDER BY ABS(s.lat - :lat) + ABS(s.lng - :lng)
    LIMIT 1
    """, nativeQuery = true)
    Optional<String> findNearestSectorCodeByGarageAndCoords(
            @Param("garageCode") String garageCode,
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("eps") double eps
    );

    @Query("""
        select s
        from Spot s
        join fetch s.sector sec
        join s.garage g
        where g.code = :code
        order by s.id
    """)
    List<Spot> findByGarageCodeWithSector(@Param("code") String garageCode);
}