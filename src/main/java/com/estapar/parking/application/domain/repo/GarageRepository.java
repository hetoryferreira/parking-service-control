package com.estapar.parking.application.domain.repo;

import com.estapar.parking.application.domain.model.Garage;
import com.estapar.parking.application.domain.model.GarageSector;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface GarageRepository extends JpaRepository<Garage, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from Garage g where g.code = :code")
    Optional<Garage> lockByCode(@Param("code") String code);

    public interface GarageSectorRepository extends JpaRepository<GarageSector, Long> {

        @Query("""
        select s
        from GarageSector s
        join fetch s.garage g
        where g.code = :code
        order by s.code
    """)
        List<GarageSector> findByGarageCode(@Param("code") String garageCode);
    }
}