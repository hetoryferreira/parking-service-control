package com.estapar.parking.application.service;


import com.estapar.parking.application.domain.model.Garage;
import com.estapar.parking.application.domain.repo.GarageRepository;
import com.estapar.parking.application.error.GarageFullException;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GarageServiceImplTest {

    @Mock GarageRepository garageRepo;
    @InjectMocks
    GarageServiceImpl service;

    @BeforeEach void init(){ MockitoAnnotations.openMocks(this); }
    @Test
    void reserveEntry_ok_incrementsAndSaves() {
        var g = Garage.builder().id(1L).code("1").maxCapacity(2).occupied(1).basePrice(BigDecimal.TEN).build();
        when(garageRepo.lockByCode("1")).thenReturn(Optional.of(g));

        var out = service.reserveEntry("1");

        assertThat(out.getOccupied()).isEqualTo(2);
        verify(garageRepo).save(g);
    }


    @Test
    void reserveEntry_full_throws() {
        var g = Garage.builder().id(1L).code("1").maxCapacity(1).occupied(1).build();
        when(garageRepo.lockByCode("1")).thenReturn(Optional.of(g));
        assertThatThrownBy(() -> service.reserveEntry("1")).isInstanceOf(GarageFullException.class);
    }

    @Test
    void getAndLockGarageByCode_ok() {
        var g = Garage.builder().id(1L).code("1").build();
        when(garageRepo.lockByCode("1")).thenReturn(Optional.of(g));
        assertThat(service.getAndLockGarageByCode("1")).contains(g);
    }
}
