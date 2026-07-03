package com.telcox.order.projection;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderSnapshotProjectionServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-01T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void createsCustomerSnapshotAndIgnoresDuplicateEvent() {
        CustomerSnapshotProjectionRepository repository = mock(CustomerSnapshotProjectionRepository.class);
        CustomerSnapshotProjectionService service = new CustomerSnapshotProjectionService(repository, CLOCK);
        CustomerSnapshotChanged event = customerEvent(UUID.randomUUID(), OffsetDateTime.parse("2026-07-01T09:00:00Z"));
        when(repository.findById(event.customerId())).thenReturn(Optional.empty());

        assertThat(service.project(event)).isTrue();
        verify(repository).save(org.mockito.ArgumentMatchers.any(CustomerSnapshotProjection.class));

        CustomerSnapshotProjection existing = new CustomerSnapshotProjection(event, OffsetDateTime.now(CLOCK));
        CustomerSnapshotProjectionRepository duplicateRepository = mock(CustomerSnapshotProjectionRepository.class);
        when(duplicateRepository.findById(event.customerId())).thenReturn(Optional.of(existing));
        assertThat(new CustomerSnapshotProjectionService(duplicateRepository, CLOCK).project(event)).isFalse();
        verify(duplicateRepository, never()).save(existing);
    }

    @Test
    void updatesTariffSnapshotOnlyForNewerSourceTimestamp() {
        TariffSnapshotProjectionRepository repository = mock(TariffSnapshotProjectionRepository.class);
        TariffSnapshotChanged initial = tariffEvent(UUID.randomUUID(), OffsetDateTime.parse("2026-07-01T09:00:00Z"), "99.90");
        TariffSnapshotProjection existing = new TariffSnapshotProjection(initial, OffsetDateTime.now(CLOCK));
        when(repository.findById(initial.tariffId())).thenReturn(Optional.of(existing));
        TariffSnapshotProjectionService service = new TariffSnapshotProjectionService(repository, CLOCK);

        TariffSnapshotChanged older = tariffEvent(initial.tariffId(), OffsetDateTime.parse("2026-07-01T08:59:59Z"), "79.90");
        assertThat(service.project(older)).isFalse();

        TariffSnapshotChanged newer = tariffEvent(initial.tariffId(), OffsetDateTime.parse("2026-07-01T09:01:00Z"), "109.90");
        assertThat(service.project(newer)).isTrue();
        assertThat(existing.getAmount()).isEqualByComparingTo("109.90");
        verify(repository).save(existing);
    }

    private CustomerSnapshotChanged customerEvent(UUID eventId, OffsetDateTime updatedAt) {
        return new CustomerSnapshotChanged(eventId, UUID.randomUUID(), "C-1001", "Ada Lovelace", "ACTIVE", "VIP", updatedAt);
    }

    private TariffSnapshotChanged tariffEvent(UUID tariffId, OffsetDateTime updatedAt, String amount) {
        return new TariffSnapshotChanged(UUID.randomUUID(), tariffId, "T-5G", "5G Premium",
                new BigDecimal(amount), "TRY", "ACTIVE", updatedAt);
    }
}
