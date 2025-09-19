package com.estapar.parking.application.domain.repo;
import com.estapar.parking.application.domain.model.GarageSector;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface GarageSectorRepository extends JpaRepository<GarageSector, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from GarageSector s where s.garage.id = :garageId and s.code = :code")
    Optional<GarageSector> lockByGarageAndCode(@Param("garageId") Long garageId, @Param("code") String code);

    @Query("""
        select s
        from GarageSector s
        join fetch s.garage g
        where g.code = :code
        order by s.code
    """)
    List<GarageSector> findByGarageCode(@Param("code") String garageCode);
}