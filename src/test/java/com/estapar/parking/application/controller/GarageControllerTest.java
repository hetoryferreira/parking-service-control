package com.estapar.parking.application.controller;

import com.estapar.parking.application.domain.model.Garage;
import com.estapar.parking.application.domain.model.GarageSector;
import com.estapar.parking.application.domain.model.Spot;
import com.estapar.parking.application.request.SectorDetails;
import com.estapar.parking.application.service.GarageSectorService;
import com.estapar.parking.application.service.SpotServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GarageController.class)
class GarageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GarageSectorService garageSectorService;

    @MockBean
    private SpotServiceImpl spotService;

    private SectorDetails sampleSectorDetails(String garageCode, String sectorCode, int maxCapacity) {

        return new SectorDetails(
                1L,
                garageCode,
                BigDecimal.TEN,
                10L,
                sectorCode,
                "Sector " + sectorCode,
                maxCapacity,
                5,
                false
        );
    }

    private Spot sampleSpot(Long id, String garageCode, String sectorCode, double lat, double lng) {
        Garage g = Garage.builder()
                .id(1L)
                .code(garageCode)
                .basePrice(BigDecimal.TEN)
                .maxCapacity(100)
                .occupied(10)
                .build();

        GarageSector s = GarageSector.builder()
                .id(10L)
                .garage(g)
                .code(sectorCode)
                .name("Sector " + sectorCode)
                .maxCapacity(50)
                .occupied(5)
                .closed(false)
                .build();

        return Spot.builder()
                .id(id)
                .garage(g)
                .sector(s)
                .lat(lat)
                .lng(lng)
                .occupied(false)
                .build();
    }


    @Test
    @DisplayName("GET /garage sem garage_code usa default=1 e retorna arrays vazios quando não há dados")
    void getGarageConfig_defaultGarageCode_andEmptyResults() throws Exception {
        when(garageSectorService.findByGarageCode(eq("1"))).thenReturn(List.of());
        when(spotService.findByGarageCodeWithSector(eq("1"))).thenReturn(List.of());

        mockMvc.perform(get("/garage").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garage", hasSize(0)))
                .andExpect(jsonPath("$.spots", hasSize(0)));

        verify(garageSectorService).findByGarageCode(eq("1"));
        verify(spotService).findByGarageCodeWithSector(eq("1"));
    }

    @Test
    @DisplayName("GET /garage propaga exceção do service como 500 (default) se não tratada")
    void getGarageConfig_serviceThrows_returns500() throws Exception {
        when(garageSectorService.findByGarageCode(Mockito.anyString()))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/garage").param("garage_code", "X"))
                .andExpect(status().is5xxServerError());
    }
}
