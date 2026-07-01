package com.telcox.order.projection;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerSnapshotProjectionService {
    private final CustomerSnapshotProjectionRepository repository;
    private final Clock clock;

    @Autowired
    public CustomerSnapshotProjectionService(CustomerSnapshotProjectionRepository repository) {
        this(repository, Clock.systemUTC());
    }

    CustomerSnapshotProjectionService(CustomerSnapshotProjectionRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public boolean project(CustomerSnapshotChanged event) {
        Objects.requireNonNull(event, "event must not be null");
        return repository.findById(event.customerId()).map(existing -> {
            if (event.eventId().equals(existing.getLastEventId())
                    || !event.sourceUpdatedAt().isAfter(existing.getSourceUpdatedAt())) {
                return false;
            }
            existing.apply(event, OffsetDateTime.now(clock));
            repository.save(existing);
            return true;
        }).orElseGet(() -> {
            repository.save(new CustomerSnapshotProjection(event, OffsetDateTime.now(clock)));
            return true;
        });
    }
}
