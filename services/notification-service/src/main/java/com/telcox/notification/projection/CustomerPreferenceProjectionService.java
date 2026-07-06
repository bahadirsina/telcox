package com.telcox.notification.projection;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerPreferenceProjectionService {

    private final CustomerPreferenceProjectionRepository repository;
    private final Clock clock;

    @Autowired
    public CustomerPreferenceProjectionService(CustomerPreferenceProjectionRepository repository) {
        this(repository, Clock.systemUTC());
    }

    CustomerPreferenceProjectionService(
            CustomerPreferenceProjectionRepository repository,
            Clock clock
    ) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public boolean project(CustomerPreferenceChanged event) {
        Objects.requireNonNull(event, "event must not be null");

        Optional<CustomerPreferenceProjection> existing = repository.findById(event.customerId());
        if (existing.isEmpty()) {
            repository.save(new CustomerPreferenceProjection(event, OffsetDateTime.now(clock)));
            return true;
        }

        CustomerPreferenceProjection projection = existing.orElseThrow();
        if (event.eventId().equals(projection.getLastEventId())) {
            return false;
        }
        if (!event.sourceUpdatedAt().isAfter(projection.getSourceUpdatedAt())) {
            return false;
        }

        projection.apply(event, OffsetDateTime.now(clock));
        repository.save(projection);
        return true;
    }
}
