package com.github.wallet.domain.event;

import java.time.Instant;

/**
 * Sealed interface for all domain events in the wallet system.
 * Enables exhaustive pattern matching in Java 21.
 */
public sealed interface DomainEvent
        permits AccountCreatedEvent,
                MoneyDepositedEvent,
                MoneyWithdrawnEvent,
                TransferCompletedEvent,
                AccountClosedEvent {

    /**
     * Timestamp when the event occurred.
     */
    Instant occurredAt();

    /**
     * Human-readable description of the event.
     */
    String description();
}
