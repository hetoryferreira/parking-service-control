package com.estapar.parking;

import org.slf4j.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.estapar.parking")
public class EstaparParkingApplication {
    private static final Logger log = LoggerFactory.getLogger(EstaparParkingApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EstaparParkingApplication.class, args);
        log.info("Estapar Parking Service started");
    }
}