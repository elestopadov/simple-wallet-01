package com.github.wallet.domain.event;

import com.github.wallet.domain.model.AccountId;

import java.time.Instant;

/**
 * Event published when an account is closed.
 */
public record AccountClosedEvent(
        AccountId accountId,
        String ownerName,
        Instant occurredAt
) implements DomainEvent {

    public AccountClosedEvent(final AccountId accountId, final String ownerName) {
        this(accountId, ownerName, Instant.now());
    }

    @Override
    public String description() {
        return "Account closed for '%s' (ID: %s)".formatted(ownerName, accountId);
    }
}
