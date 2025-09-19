package com.estapar.parking.application.support;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "pricing")
public class PricingProperties {

    /** Limiares de ocupação (0–1) */
    private Thresholds thresholds = new Thresholds();

    /** Fatores multiplicadores aplicados ao preço-base */
    private Factors factors = new Factors();

    /** Config de arredondamento do preço */
    @Min(0)
    private int priceScale = 2;

    private RoundingMode priceRounding = RoundingMode.HALF_UP;

    /** Janela grátis (min) usada no cálculo de saída */
    @Min(0)
    private int freeMinutes = 30;

    @Getter @Setter
    public static class Thresholds {
        @DecimalMin("0.0") private double t25 = 0.25;
        @DecimalMin("0.0") private double t50 = 0.50;
        @DecimalMin("0.0") private double t75 = 0.75;
    }

    @Getter @Setter
    public static class Factors {
        @DecimalMin("0.0") private BigDecimal discount10  = new BigDecimal("0.90"); // -10%
        @DecimalMin("0.0") private BigDecimal base        = BigDecimal.ONE;         //  0%
        @DecimalMin("0.0") private BigDecimal surcharge10 = new BigDecimal("1.10"); // +10%
        @DecimalMin("0.0") private BigDecimal surcharge25 = new BigDecimal("1.25"); // +25%
    }
}
