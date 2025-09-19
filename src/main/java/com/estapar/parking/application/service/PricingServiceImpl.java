package com.estapar.parking.application.service;
import com.estapar.parking.application.domain.model.Garage;
import com.estapar.parking.application.domain.model.ParkingSession;
import com.estapar.parking.application.service.PriceService;
import com.estapar.parking.application.support.PricingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingServiceImpl implements PriceService {

    private final PricingProperties props;

    /** Preço dinâmico calculado na ENTRADA conforme ocupação da GARAGE */
    public BigDecimal effectiveHourlyPrice(Garage g) {

        log.info("effectiveHourlyPrice start. garageID={}", g.getId());

        double occ = (g.getMaxCapacity() == 0)
                ? 0.0
                : (double) g.getOccupied() / (double) g.getMaxCapacity();

        var th = props.getThresholds();
        var fc = props.getFactors();

        BigDecimal factor =
                (occ <  th.getT25()) ? fc.getDiscount10()  :
                        (occ <= th.getT50()) ? fc.getBase()        :
                                (occ <= th.getT75()) ? fc.getSurcharge10() :
                                        fc.getSurcharge25();

        return g.getBasePrice()
                .multiply(factor)
                .setScale(props.getPriceScale(), props.getPriceRounding());
    }

    /**
     * Regras:
     * - janela grátis configurável
     * - após a janela grátis, cobra por HORA cheia
     */
    public BigDecimal calculateAmount(ParkingSession s) {

        var scale    = props.getPriceScale();
        var rounding = props.getPriceRounding();

        LocalDateTime entry = (s.getEntryTime() != null) ? s.getEntryTime() : LocalDateTime.now();
        LocalDateTime exit  = (s.getExitTime()  != null) ? s.getExitTime()  : LocalDateTime.now();

        if (!exit.isAfter(entry)) {
            return BigDecimal.ZERO.setScale(scale, rounding);
        }

        int free = props.getFreeMinutes();
        LocalDateTime freeUntil = entry.plusMinutes(free);

        if (!exit.isAfter(freeUntil)) {
            return BigDecimal.ZERO.setScale(scale, rounding);
        }

        BigDecimal hourly = (s.getEffectiveHourlyPrice() != null)
                ? s.getEffectiveHourlyPrice()
                : effectiveHourlyPrice(s.getGarage());

        long chargeableMinutes = Duration.between(freeUntil, exit).toMinutes();
        long hours = (chargeableMinutes + 59) / 60;

        return hourly.multiply(BigDecimal.valueOf(hours))
                .setScale(scale, rounding);
    }
}
