package com.github.wallet.domain.port;

import com.github.wallet.domain.event.DomainEvent;

/**
 * Port (interface) for publishing domain events.
 * Allows decoupling of event production from consumption.
 */
public interface EventPublisher {

    /**
     * Publish a domain event to all registered subscribers.
     */
    void publish(DomainEvent event);
}
