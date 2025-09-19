package com.estapar.parking.application.service;

import com.estapar.parking.application.domain.model.Garage;
import com.estapar.parking.application.domain.model.GarageSector;
import com.estapar.parking.application.domain.repo.GarageSectorRepository;
import com.estapar.parking.application.error.SectorClosedException;
import com.estapar.parking.application.request.SectorDetails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GarageSectorServiceImplTest {

    @Mock GarageService garageService;
    @Mock
    SpotServiceImpl spotService;
    @Mock GarageSectorRepository garageSectorRepository;

    @InjectMocks
    GarageSectorServiceImpl service;

    private Garage garage;
    private GarageSector sectorA;

    @BeforeEach
    void setUp() {
        garage = Garage.builder()
                .id(1L)
                .code("G1")
                .basePrice(new BigDecimal("10.00"))
                .maxCapacity(100)
                .occupied(10)
                .build();

        sectorA = GarageSector.builder()
                .id(10L)
                .garage(garage)
                .code("A")
                .name("Sector A")
                .maxCapacity(2)
                .occupied(1)
                .closed(false)
                .build();
    }

    // -------------- allocateOneSpot ----------------

    @Test
    void allocateOneSpot_happyPath_incrementsAndMayClose() {
        // garage OK
        when(garageService.getAndLockGarageByCode("G1"))
                .thenReturn(Optional.of(garage));

        // sector OK (não fechado, não cheio)
        when(garageSectorRepository.lockByGarageAndCode(1L, "A"))
                .thenReturn(Optional.of(sectorA));

        // Não precisamos stubar spotService.lockFirstFreeAndMarkOccupied(...)
        // porque seu retorno não é usado. O método será chamado e retornará null (default),
        // o que não afeta a lógica testada.

        var view = service.allocateOneSpot("G1", "A");

        // após incOccupied numa capacidade 2 com ocupado 1 => fica 2 e fecha.
        assertThat(sectorA.getOccupied()).isEqualTo(2);
        assertThat(sectorA.isClosed()).isTrue();

        // o repo.save deve ser chamado com o setor atualizado
        verify(garageSectorRepository).save(sectorA);

        // a view não sabemos os campos internos, mas ao menos não deve ser nula
        assertThat(view).isNotNull();

        // e o spotService deve ter sido chamado para pegar/ocupar vaga
        verify(spotService).lockFirstFreeAndMarkOccupied(sectorA.getId());
    }




    @Test
    void allocateOneSpot_sectorClosed_throwsSectorClosed() {
        var closed = GarageSector.builder()
                .id(11L).garage(garage).code("B").name("B")
                .maxCapacity(5).occupied(1).closed(true).build();

        when(garageService.getAndLockGarageByCode("G1"))
                .thenReturn(Optional.of(garage));
        when(garageSectorRepository.lockByGarageAndCode(1L, "B"))
                .thenReturn(Optional.of(closed));

        assertThatThrownBy(() -> service.allocateOneSpot("G1", "B"))
                .isInstanceOf(SectorClosedException.class);
        verifyNoInteractions(spotService);
        verify(garageSectorRepository, never()).save(any());
    }


    // -------------- findByGarageCode ----------------

    @Test
    void findByGarageCode_mapsEntitiesToDTOs() {
        var s1 = sectorA;
        var s2 = GarageSector.builder()
                .id(20L)
                .garage(garage)
                .code("B")
                .name("Sector B")
                .maxCapacity(50)
                .occupied(5)
                .closed(false)
                .build();

        when(garageSectorRepository.findByGarageCode("G1"))
                .thenReturn(List.of(s1, s2));

        List<SectorDetails> out = service.findByGarageCode("G1");

        assertThat(out).hasSize(2);
        assertThat(out.get(0).garageCode()).isEqualTo("G1");
        assertThat(out.get(0).sectorCode()).isEqualTo("A");
        assertThat(out.get(0).maxCapacity()).isEqualTo(2);
        assertThat(out.get(1).sectorCode()).isEqualTo("B");
        assertThat(out.get(1).occupied()).isEqualTo(5);
    }

    @Test
    void findByGarageCode_emptyList_returnsEmpty() {
        when(garageSectorRepository.findByGarageCode("G1"))
                .thenReturn(Collections.emptyList());

        var out = service.findByGarageCode("G1");
        assertThat(out).isEmpty();
    }

    @Test
    void findByGarageCode_repoReturnsNull_returnsEmpty() {
        when(garageSectorRepository.findByGarageCode("G1"))
                .thenReturn(null); // o método trata null

        var out = service.findByGarageCode("G1");
        assertThat(out).isEmpty();
    }
}
