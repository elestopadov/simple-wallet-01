package com.github.wallet.domain.event;

import com.github.wallet.domain.model.AccountId;
import com.github.wallet.domain.model.Currency;

import java.time.Instant;

/**
 * Event published when a new account is created.
 */
public record AccountCreatedEvent(
        AccountId accountId,
        String ownerName,
        Currency currency,
        Instant occurredAt
) implements DomainEvent {

    public AccountCreatedEvent(final AccountId accountId, final String ownerName, final Currency currency) {
        this(accountId, ownerName, currency, Instant.now());
    }

    @Override
    public String description() {
        return "Account created for '%s' with currency %s".formatted(ownerName, currency);
    }
}
