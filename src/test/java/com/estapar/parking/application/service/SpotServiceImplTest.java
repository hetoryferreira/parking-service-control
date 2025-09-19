package com.estapar.parking.application.service;
import com.estapar.parking.application.domain.model.Garage;
import com.estapar.parking.application.domain.model.GarageSector;
import com.estapar.parking.application.domain.model.Spot;
import com.estapar.parking.application.domain.repo.SpotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotServiceImplTest {

    @Mock
    private SpotRepository spotRepository;

    @InjectMocks
    private SpotServiceImpl service;

    private Garage garage;
    private GarageSector sector;

    @BeforeEach
    void init() {
        garage = Garage.builder()
                .id(10L).code("1")
                .basePrice(java.math.BigDecimal.TEN)
                .maxCapacity(100).occupied(0)
                .build();

        sector = GarageSector.builder()
                .id(20L).code("A").name("Sector A")
                .maxCapacity(50).occupied(0).closed(false)
                .garage(garage)
                .build();
    }

    // ---------- lockFirstFreeAndMarkOccupied ----------

    @Test
    void lockFirstFreeAndMarkOccupied_happyPath_marksOccupiedAndSaves() {
        Spot freeSpot = Spot.builder()
                .id(100L)
                .garage(garage)
                .sector(sector)
                .lat(-23.0).lng(-46.0)
                .occupied(false)
                .build();

        when(spotRepository.lockFirstFreeBySector(20L)).thenReturn(Optional.of(freeSpot));
        when(spotRepository.save(any(Spot.class))).thenAnswer(inv -> inv.getArgument(0));

        Spot out = service.lockFirstFreeAndMarkOccupied(20L);

        assertThat(out).isSameAs(freeSpot);
        assertThat(out.isOccupied()).isTrue();
        verify(spotRepository).lockFirstFreeBySector(20L);
        verify(spotRepository).save(freeSpot);
    }

    @Test
    void lockFirstFreeAndMarkOccupied_whenNoSpot_throwsNoSuchElement() {
        when(spotRepository.lockFirstFreeBySector(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.lockFirstFreeAndMarkOccupied(20L))
                .isInstanceOf(NoSuchElementException.class);

        verify(spotRepository).lockFirstFreeBySector(20L);
        verify(spotRepository, never()).save(any());
    }

    // ---------- findNearestSectorCodeByGarageAndCoords ----------

    @Test
    void findNearestSectorCodeByGarageAndCoords_returnsValue() {
        when(spotRepository.findNearestSectorCodeByGarageAndCoords("1", -23.1, -46.2, 0.0005))
                .thenReturn(Optional.of("B"));

        var out = service.findNearestSectorCodeByGarageAndCoords("1", -23.1, -46.2, 0.0005);

        assertThat(out).contains("B");
        verify(spotRepository).findNearestSectorCodeByGarageAndCoords("1", -23.1, -46.2, 0.0005);
    }

    @Test
    void findNearestSectorCodeByGarageAndCoords_whenEmpty_returnsEmptyOptional() {
        when(spotRepository.findNearestSectorCodeByGarageAndCoords("1", -23.1, -46.2, 0.0005))
                .thenReturn(Optional.empty());

        var out = service.findNearestSectorCodeByGarageAndCoords("1", -23.1, -46.2, 0.0005);

        assertThat(out).isEmpty();
        verify(spotRepository).findNearestSectorCodeByGarageAndCoords("1", -23.1, -46.2, 0.0005);
    }

    // ---------- findByGarageCodeWithSector ----------

    @Test
    void findByGarageCodeWithSector_delegatesToRepository() {
        Spot s1 = Spot.builder().id(1L).garage(garage).sector(sector).occupied(false).build();
        Spot s2 = Spot.builder().id(2L).garage(garage).sector(sector).occupied(true).build();

        when(spotRepository.findByGarageCodeWithSector("1")).thenReturn(List.of(s1, s2));

        var out = service.findByGarageCodeWithSector("1");

        assertThat(out).containsExactly(s1, s2);
        verify(spotRepository).findByGarageCodeWithSector("1");
    }

    @Test
    void findByGarageCodeWithSector_whenNoResults_returnsEmptyList() {
        when(spotRepository.findByGarageCodeWithSector("1")).thenReturn(List.of());

        var out = service.findByGarageCodeWithSector("1");

        assertThat(out).isEmpty();
        verify(spotRepository).findByGarageCodeWithSector("1");
    }

    // ---------- save ----------

    @Test
    void save_whenNull_doesNotCallRepository() {
        service.save(null);
        verify(spotRepository, never()).save(any());
    }

    @Test
    void save_validSpot_callsRepositorySave() {
        Spot s = Spot.builder()
                .id(123L)
                .garage(garage)
                .sector(sector)
                .occupied(false)
                .build();

        when(spotRepository.save(s)).thenReturn(s);

        service.save(s);

        verify(spotRepository).save(s);
    }
}
