package com.estapar.parking.application.service;

import com.estapar.parking.application.domain.model.Garage;
import com.estapar.parking.application.domain.model.GarageSector;
import com.estapar.parking.application.domain.model.ParkingSession;
import com.estapar.parking.application.domain.model.SessionStatus;
import com.estapar.parking.application.domain.repo.ParkingSessionRepository;
import com.estapar.parking.application.error.NotOpenSessionException;
import com.estapar.parking.application.error.OpenSessionExistsException;
import com.estapar.parking.application.request.SessionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingServiceImplTest {

    @Mock
    private ParkingSessionRepository repo;

    @Mock
    private PriceService priceService;

    @InjectMocks
    private ParkingServiceImpl service;

    private Garage garage;

    @BeforeEach
    void setup() {
        garage = Garage.builder()
                .id(10L).code("G1")
                .basePrice(new BigDecimal("10.00"))
                .maxCapacity(100).occupied(10)
                .build();
    }

    // ---------- computeDynamicPriceAtEntry ----------

    @Test
    void computeDynamicPriceAtEntry_delegatesToPriceService() {
        when(priceService.effectiveHourlyPrice(garage)).thenReturn(new BigDecimal("12.34"));
        BigDecimal out = service.computeDynamicPriceAtEntry(garage);
        assertThat(out).isEqualByComparingTo("12.34");
        verify(priceService).effectiveHourlyPrice(garage);
    }

    // ---------- entryWithoutSector ----------

    @Test
    void entryWithoutSector_whenOpenSessionExists_throws() {
        when(repo.findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc(eq("ABC1234"), anyList()))
                .thenReturn(Optional.of(ParkingSession.builder().id(1L).build()));

        assertThatThrownBy(() ->
                service.entryWithoutSector("ABC1234", garage, new BigDecimal("10.00"), Instant.now()))
                .isInstanceOf(OpenSessionExistsException.class);
    }

    @Test
    void entryWithoutSector_createsSessionAndReturnsResponse() {
        when(repo.findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc(eq("ABC1234"), anyList()))
                .thenReturn(Optional.empty());

        ArgumentCaptor<ParkingSession> captor = ArgumentCaptor.forClass(ParkingSession.class);

        // simula "save" devolvendo objeto com id
        when(repo.save(any(ParkingSession.class)))
                .thenAnswer(inv -> {
                    ParkingSession s = inv.getArgument(0);
                    s.setId(42L);
                    return s;
                });

        SessionResponse resp = service.entryWithoutSector(
                "ABC1234", garage, new BigDecimal("10.00"), Instant.parse("2024-01-01T00:00:00Z"));

        verify(repo).save(captor.capture());
        ParkingSession saved = captor.getValue();

        assertThat(saved.getPlate()).isEqualTo("ABC1234");
        assertThat(saved.getGarage()).isSameAs(garage);
        assertThat(saved.getStatus()).isEqualTo(SessionStatus.ENTRY);
        assertThat(saved.getEntryTime()).isEqualTo(LocalDateTime.ofInstant(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC));
        assertThat(saved.getEffectiveHourlyPrice()).isEqualByComparingTo("10.00");

        assertThat(resp).isNotNull();
        assertThat(resp.id()).isEqualTo(42L);
        assertThat(resp.plate()).isEqualTo("ABC1234");
    }

    // ---------- parked ----------

    @Test
    void parked_whenNoEntrySession_throwsNotOpen() {
        when(repo.findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc(eq("ABC1234"), anyList()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.parked("ABC1234", GarageSector.builder().id(1L).code("A").build()))
                .isInstanceOf(NotOpenSessionException.class);
    }

    @Test
    void parked_updatesSessionWithSectorAndStatusParked() {
        ParkingSession entry = ParkingSession.builder()
                .id(5L).plate("ABC1234")
                .garage(garage)
                .status(SessionStatus.ENTRY)
                .entryTime(LocalDateTime.now().minusMinutes(10))
                .build();

        when(repo.findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc(eq("ABC1234"), eq(List.of(SessionStatus.ENTRY))))
                .thenReturn(Optional.of(entry));

        when(repo.save(any(ParkingSession.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        GarageSector sector = GarageSector.builder().id(7L).code("A").build();

        SessionResponse resp = service.parked("ABC1234", sector);

        assertThat(resp).isNotNull();
        assertThat(entry.getStatus()).isEqualTo(SessionStatus.PARKED);
        assertThat(entry.getSector()).isSameAs(sector);
        assertThat(entry.getParkedTime()).isNotNull();
        verify(repo).save(entry);
    }

    // ---------- exit ----------

    @Test
    void exit_whenNoOpenSession_throwsNotOpen() {
        when(repo.findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc(eq("ABC1234"), anyList()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.exit("ABC1234", OffsetDateTime.now()))
                .isInstanceOf(NotOpenSessionException.class);
    }

    @Test
    void exit_setsExitAndAmountFromPricingService() {
        ParkingSession s = ParkingSession.builder()
                .id(9L).plate("abc1234") // vai normalizar
                .garage(garage)
                .status(SessionStatus.ENTRY)
                .entryTime(LocalDateTime.now().minusHours(2))
                .effectiveHourlyPrice(new BigDecimal("10.00"))
                .build();

        when(repo.findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc(eq("ABC1234"), anyList()))
                .thenReturn(Optional.of(s));

        when(priceService.calculateAmount(s)).thenReturn(new BigDecimal("20.00"));

        when(repo.save(any(ParkingSession.class))).thenAnswer(inv -> inv.getArgument(0));

        OffsetDateTime exitTime = OffsetDateTime.parse("2024-02-01T10:00:00Z");

        SessionResponse resp = service.exit("ABC1234", exitTime);

        assertThat(resp).isNotNull();
        assertThat(s.getStatus()).isEqualTo(SessionStatus.EXIT);
        assertThat(s.getExitTime()).isEqualTo(exitTime.toLocalDateTime());
        assertThat(s.getTotalAmount()).isEqualByComparingTo("20.00");
        verify(priceService).calculateAmount(s);
        verify(repo).save(s);
    }

    // ---------- sumRevenueByGarageAndSectorAndExitBetween ----------

    @Test
    void sumRevenue_validDate_callsRepositoryWithOneDayWindow() {
        when(repo.sumRevenueByGarageAndSectorAndExitBetween(
                eq("G1"), eq("A"),
                any(LocalDateTime.class), any(LocalDateTime.class),
                eq(SessionStatus.EXIT)
        )).thenReturn(new BigDecimal("123.45"));

        BigDecimal out = service.sumRevenueByGarageAndSectorAndExitBetween("G1", "A", "2025-01-01");

        assertThat(out).isEqualByComparingTo("123.45");
        verify(repo).sumRevenueByGarageAndSectorAndExitBetween(
                eq("G1"), eq("A"),
                eq(LocalDateTime.of(2025, 1, 1, 0, 0)),
                eq(LocalDateTime.of(2025, 1, 2, 0, 0)),
                eq(SessionStatus.EXIT)
        );
    }

    @Test
    void sumRevenue_invalidDate_throwsRuntimeWrappingBadRequest() {
        assertThatThrownBy(() ->
                service.sumRevenueByGarageAndSectorAndExitBetween("G1", "A", "2025/01/01")
        ).isInstanceOf(RuntimeException.class);
        verifyNoInteractions(repo);
    }

    // ---------- findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc ----------

    @Test
    void findFirstByPlate_whenNullOrBlank_returnsEmptyAndDoesNotHitRepo() {
        assertThat(service.findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc(null, List.of())).isEmpty();
        assertThat(service.findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc("   ", List.of())).isEmpty();
        verifyNoInteractions(repo);
    }

    @Test
    void findFirstByPlate_normalizesPlate_andDefaultsStatuses() {
        ParkingSession s = ParkingSession.builder().id(1L).plate("ABC1234").build();

        when(repo.findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc(
                eq("ABC1234"), eq(List.of(SessionStatus.ENTRY, SessionStatus.PARKED))
        )).thenReturn(Optional.of(s));

        var out = service.findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc("abc1234", List.of());
        assertThat(out).containsSame(s);
    }

    @Test
    void findFirstByPlate_usesProvidedStatuses() {
        ParkingSession s = ParkingSession.builder().id(1L).plate("ABC1234").build();

        when(repo.findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc(
                eq("ABC1234"), eq(List.of(SessionStatus.EXIT))
        )).thenReturn(Optional.of(s));

        var out = service.findFirstByPlateIgnoreCaseAndStatusInOrderByIdDesc(" abc1234 ", List.of(SessionStatus.EXIT));
        assertThat(out).containsSame(s);
    }
}
