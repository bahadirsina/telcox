package com.telcox.notification.projection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerPreferenceProjectionServiceTest {

    private static final UUID CUSTOMER_ID = UUID.fromString("8d040a74-20af-48eb-ac1e-886047fe7221");
    private static final OffsetDateTime PROJECTED_AT =
            OffsetDateTime.parse("2026-06-26T10:00:00Z");

    @Mock
    private CustomerPreferenceProjectionRepository repository;

    private CustomerPreferenceProjectionService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-26T10:00:00Z"), ZoneOffset.UTC);
        service = new CustomerPreferenceProjectionService(repository, clock);
    }

    @Test
    void createsProjectionWhenCustomerIsUnknown() {
        CustomerPreferenceChanged event = event("2026-06-26T09:00:00Z", "EMAIL");
        when(repository.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

        assertThat(service.project(event)).isTrue();

        ArgumentCaptor<CustomerPreferenceProjection> captor =
                ArgumentCaptor.forClass(CustomerPreferenceProjection.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(captor.getValue().getPreferredChannel()).isEqualTo("EMAIL");
        assertThat(captor.getValue().getSourceUpdatedAt())
                .isEqualTo(OffsetDateTime.parse("2026-06-26T09:00:00Z"));
    }

    @Test
    void appliesNewerPreferenceEvent() {
        CustomerPreferenceProjection projection = new CustomerPreferenceProjection(
                event("2026-06-26T08:00:00Z", "EMAIL"),
                PROJECTED_AT
        );
        CustomerPreferenceChanged newer = event("2026-06-26T09:00:00Z", "SMS");
        when(repository.findById(CUSTOMER_ID)).thenReturn(Optional.of(projection));

        assertThat(service.project(newer)).isTrue();

        verify(repository).save(projection);
        assertThat(projection.getPreferredChannel()).isEqualTo("SMS");
        assertThat(projection.getLastEventId()).isEqualTo(newer.eventId());
    }

    @Test
    void ignoresDuplicateEvent() {
        CustomerPreferenceChanged event = event("2026-06-26T09:00:00Z", "EMAIL");
        CustomerPreferenceProjection projection =
                new CustomerPreferenceProjection(event, PROJECTED_AT);
        when(repository.findById(CUSTOMER_ID)).thenReturn(Optional.of(projection));

        assertThat(service.project(event)).isFalse();

        verify(repository, never()).save(any());
    }

    @Test
    void ignoresStaleEvent() {
        CustomerPreferenceProjection projection = new CustomerPreferenceProjection(
                event("2026-06-26T09:00:00Z", "SMS"),
                PROJECTED_AT
        );
        CustomerPreferenceChanged stale = event("2026-06-26T08:00:00Z", "EMAIL");
        when(repository.findById(CUSTOMER_ID)).thenReturn(Optional.of(projection));

        assertThat(service.project(stale)).isFalse();

        verify(repository, never()).save(any());
        assertThat(projection.getPreferredChannel()).isEqualTo("SMS");
    }

    private static CustomerPreferenceChanged event(String sourceUpdatedAt, String channel) {
        return new CustomerPreferenceChanged(
                UUID.randomUUID(),
                CUSTOMER_ID,
                "customer@telcox.test",
                "+905551112233",
                channel,
                true,
                true,
                true,
                false,
                true,
                OffsetDateTime.parse(sourceUpdatedAt)
        );
    }
}
