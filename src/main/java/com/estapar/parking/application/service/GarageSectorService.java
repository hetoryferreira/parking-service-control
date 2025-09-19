package com.estapar.parking.application.service;
import com.estapar.parking.application.domain.model.GarageSector;
import com.estapar.parking.application.domain.view.AllocationView;
import com.estapar.parking.application.request.SectorDetails;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface GarageSectorService {
    public AllocationView allocateOneSpot(String garageCode, String sectorCode);

    public List<SectorDetails> findByGarageCode(String garageCode);
}
