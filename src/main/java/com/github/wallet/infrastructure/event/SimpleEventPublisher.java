package com.github.wallet.infrastructure.event;

import com.github.wallet.domain.event.DomainEvent;
import com.github.wallet.domain.port.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Simple in-memory event publisher implementation.
 * Publishes domain events synchronously to all registered subscribers.
 * Final class — not designed for inheritance.
 */
public final class SimpleEventPublisher implements EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SimpleEventPublisher.class);

    private final List<Consumer<DomainEvent>> subscribers = new CopyOnWriteArrayList<>();

    @Override
    public void publish(final DomainEvent event) {
        Objects.requireNonNull(event, "Event must not be null");
        logger.debug("Publishing event: {}", event.getClass().getSimpleName());

        for (final Consumer<DomainEvent> subscriber : subscribers) {
            try {
                subscriber.accept(event);
            } catch (final Exception e) {
                logger.error("Error in event subscriber while processing {}: {}",
                        event.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    /**
     * Register a subscriber to receive all domain events.
     */
    public void subscribe(final Consumer<DomainEvent> subscriber) {
        Objects.requireNonNull(subscriber, "Subscriber must not be null");
        subscribers.add(subscriber);
        logger.debug("Subscriber registered. Total subscribers: {}", subscribers.size());
    }

    /**
     * Remove a subscriber.
     */
    public void unsubscribe(final Consumer<DomainEvent> subscriber) {
        subscribers.remove(subscriber);
    }

    /**
     * Get the number of registered subscribers.
     */
    public int subscriberCount() {
        return subscribers.size();
    }
}
