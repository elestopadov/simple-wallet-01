package com.github.wallet.infrastructure.event;

import com.github.wallet.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Event subscriber that logs all domain events.
 * Also stores events for audit trail / testing purposes.
 * Final class — not designed for inheritance.
 */
public final class LoggingEventSubscriber implements Consumer<DomainEvent> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingEventSubscriber.class);

    private final List<DomainEvent> eventLog = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void accept(final DomainEvent event) {
        logger.info("[EVENT] {} | {}", event.getClass().getSimpleName(), event.description());
        eventLog.add(event);
    }

    /**
     * Get all recorded events (for audit/testing).
     */
    public List<DomainEvent> getEventLog() {
        return Collections.unmodifiableList(new ArrayList<>(eventLog));
    }

    /**
     * Get the number of recorded events.
     */
    public int eventCount() {
        return eventLog.size();
    }

    /**
     * Clear the event log.
     */
    public void clear() {
        eventLog.clear();
    }
}
