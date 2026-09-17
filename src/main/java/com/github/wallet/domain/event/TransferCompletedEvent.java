package com.github.wallet.domain.event;

import com.github.wallet.domain.model.AccountId;
import com.github.wallet.domain.model.Money;

import java.time.Instant;

/**
 * Event published when a transfer between accounts is completed.
 */
public record TransferCompletedEvent(
        AccountId sourceAccountId,
        AccountId targetAccountId,
        Money amount,
        Instant occurredAt
) implements DomainEvent {

    public TransferCompletedEvent(final AccountId sourceAccountId, final AccountId targetAccountId, final Money amount) {
        this(sourceAccountId, targetAccountId, amount, Instant.now());
    }

    @Override
    public String description() {
        return "Transferred %s from account %s to account %s".formatted(amount, sourceAccountId, targetAccountId);
    }
}
