package com.estapar.parking.application.service;

import com.estapar.parking.application.domain.model.Garage;
import com.estapar.parking.application.domain.model.ParkingSession;
import java.math.BigDecimal;

public interface PriceService {
    public BigDecimal effectiveHourlyPrice(Garage g) ;
    public BigDecimal calculateAmount(ParkingSession s) ;
}
